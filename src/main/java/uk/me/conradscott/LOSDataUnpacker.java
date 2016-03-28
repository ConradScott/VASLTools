package uk.me.conradscott;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

public final class LOSDataUnpacker {
    private LOSDataUnpacker() {
    }

    public static void main(final String... args) {
        for (final String arg : args) {
            try (final ObjectInput in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(
                    arg))))) {
                extractLOSData(in);

            } catch (final FileNotFoundException ignored) {
                System.err.println("File not found \"" + arg + '"');
            } catch (final IOException e) {
                System.err.println("IO exception: " + e.getMessage());
            }
        }
    }

    private static void extractLOSData(final DataInput in) throws IOException {
        // read the map-level data
        final int width = in.readInt();
        final int height = in.readInt();
        final int gridWidth = in.readInt();
        final int gridHeight = in.readInt();

        final BufferedImage elevation = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_BYTE_GRAY);
        final BufferedImage terrain = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_BYTE_GRAY);

        final Collection<Integer> terrainCodes = new TreeSet<>();
        final Collection<Integer> elevations = new TreeSet<>();

        // read the terrain and elevations grids
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                final int pixelElevation = in.readByte() & 0xFF;
                elevations.add(pixelElevation);
                elevation.setRGB(x, y, pixelElevation);

                final int pixelTerrainCode = in.readByte() & 0xFF;
                terrainCodes.add(pixelTerrainCode);
                terrain.setRGB(x, y, pixelTerrainCode);
            }
        }

        writePNG(elevation, "elevation.png");
        writePNG(terrain, "terrain.png");

        System.out.println("Elevations:");
        elevations.forEach(System.out::println);
        System.out.println("Terrain codes:");
        terrainCodes.forEach(System.out::println);
    }

    private static void writePNG(final RenderedImage image, final String pathname) throws IOException {
        final File out = new File(pathname);
        ImageIO.write(image, "png", out);
    }
}
