package daylog.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This draws the tray icon: a small book (white cover with a spine stripe and text lines) on a blue tile, at any square size.
 * Choices that make it look right in trays we cannot see: the blue tile fills the whole square so there are no transparent pixels
 * (some trays paint those black); every shape sits on whole units of a 16 unit grid so it stays crisp at 16, 24 and 32 pixels;
 * and it is drawn four times bigger and shrunk by averaging each 4x4 block, which gives smooth edges at sizes like 22 or 24
 * without relying on the scaler of the JDK.
 */
final class BookIcon {

    private static final int SUPERSAMPLING = 4;
    private static final Color TILE = new Color(0x2B6CB0);
    private static final Color TEXT_LINE = new Color(0x2B6CB0);

    private BookIcon() {
    }

    /** Draws the book for a square of the given size in pixels. */
    static BufferedImage draw(int size) {
        int bigSize = size * SUPERSAMPLING;
        BufferedImage big = new BufferedImage(bigSize, bigSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = big.createGraphics();
        double unit = bigSize / 16.0;

        graphics.setColor(TILE);
        graphics.fillRect(0, 0, bigSize, bigSize);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(scaled(3, unit), scaled(2, unit), scaled(10, unit), scaled(12, unit));

        graphics.setColor(TEXT_LINE);
        graphics.fillRect(scaled(5, unit), scaled(2, unit), scaled(1, unit), scaled(12, unit));
        graphics.fillRect(scaled(7, unit), scaled(5, unit), scaled(4, unit), scaled(1, unit));
        graphics.fillRect(scaled(7, unit), scaled(8, unit), scaled(4, unit), scaled(1, unit));
        graphics.fillRect(scaled(7, unit), scaled(11, unit), scaled(2, unit), scaled(1, unit));
        graphics.dispose();
        return shrunk(big, size);
    }

    /** Shrinks the big drawing by averaging every SUPERSAMPLING x SUPERSAMPLING block of pixels into one pixel. */
    private static BufferedImage shrunk(BufferedImage big, int size) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        int blockPixels = SUPERSAMPLING * SUPERSAMPLING;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int red = 0;
                int green = 0;
                int blue = 0;
                for (int blockY = 0; blockY < SUPERSAMPLING; blockY++) {
                    for (int blockX = 0; blockX < SUPERSAMPLING; blockX++) {
                        int rgb = big.getRGB(x * SUPERSAMPLING + blockX, y * SUPERSAMPLING + blockY);
                        red += (rgb >> 16) & 0xFF;
                        green += (rgb >> 8) & 0xFF;
                        blue += rgb & 0xFF;
                    }
                }
                result.setRGB(x, y, ((red / blockPixels) << 16) | ((green / blockPixels) << 8) | (blue / blockPixels));
            }
        }
        return result;
    }

    /** Converts a coordinate in the 16 unit design grid to pixels. */
    private static int scaled(double gridValue, double unit) {
        return (int) Math.round(gridValue * unit);
    }
}
