package dev.shefer.searchengine.util;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public class AverageHashImageHash {

    public static long getPerceptualHash(final File imageFile) throws IOException {
        return getPerceptualHash(ImageIO.read(imageFile));
    }

    public static long getPerceptualHash(final Path imageFile) throws IOException {
        return getPerceptualHash(ImageIO.read(imageFile.toFile()));
    }

    public static long getPerceptualHash(final ImageInputStream imageInputStream) throws IOException {
        return getPerceptualHash(ImageIO.read(imageInputStream));
    }

    public static long getPerceptualHash(final InputStream inputStream) throws IOException {
        return getPerceptualHash(ImageIO.read(inputStream));
    }

    public static long getPerceptualHash(final URL imageUrl) throws IOException {
        return getPerceptualHash(ImageIO.read(imageUrl));
    }

    public static long getPerceptualHash(final Image image) {
        // Start by rescaling the image to an 8x8 square (a total of 64 pixels, each of which will ultimately map to a
        // bit in our hash). This may involve some squishing (or, in rare cases, stretching), but that's fine for our
        // purposes. We also want to go to greyscale so we only have a single channel to worry about.
        final BufferedImage scaledImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        {
            final Graphics2D graphics = scaledImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            graphics.drawImage(image, 0, 0, 8, 8, null);

            graphics.dispose();
        }

        final int[] pixels = new int[64];
        scaledImage.getData().getPixels(0, 0, 8, 8, pixels);

        final int average;
        {
            int total = 0;

            for (int pixel : pixels) {
                total += pixel;
            }

            average = total / 64;
        }

        long hash = 0;

        for (final int pixel : pixels) {
            hash <<= 1;

            if (pixel > average) {
                hash |= 1;
            }
        }

        return hash;
    }
}