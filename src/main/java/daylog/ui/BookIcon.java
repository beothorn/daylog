package daylog.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This draws the tray icon: an open book (blue cover, two white pages with text lines), at any square size.
 * The book fills the whole square (a 16 unit design grid, 1 unit margin) because panels like Ubuntu's give icons little room
 * and a book with empty borders looks small and off-center.
 */
final class BookIcon {

    private BookIcon() {
    }

    /** Draws the book scaled to the given size in pixels, on a transparent background. */
    static BufferedImage draw(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double unit = size / 16.0;

        graphics.setColor(new Color(0x2B6CB0));
        graphics.fillRoundRect(scaled(0, unit), scaled(1.5, unit), scaled(16, unit), scaled(13.5, unit), scaled(3, unit), scaled(3, unit));

        graphics.setColor(Color.WHITE);
        graphics.fillPolygon(page(unit, 1.2, 7.6));
        graphics.fillPolygon(page(unit, 14.8, 8.4));

        graphics.setColor(new Color(0x2B6CB0));
        graphics.setStroke(new BasicStroke((float) Math.max(1, unit * 0.9)));
        graphics.drawLine(scaled(8, unit), scaled(2.5, unit), scaled(8, unit), scaled(14, unit));

        graphics.setColor(new Color(0x9DB7D5));
        graphics.setStroke(new BasicStroke((float) Math.max(0.6, unit * 0.5)));
        for (double lineY : new double[] {6, 8.2, 10.4}) {
            graphics.drawLine(scaled(3, unit), scaled(lineY, unit), scaled(6.2, unit), scaled(lineY, unit));
            graphics.drawLine(scaled(9.8, unit), scaled(lineY, unit), scaled(13, unit), scaled(lineY, unit));
        }
        graphics.dispose();
        return image;
    }

    /** One page: a rectangle from the outer edge x to the spine x, slightly taller towards the spine. */
    private static Polygon page(double unit, double outerX, double spineX) {
        Polygon page = new Polygon();
        page.addPoint(scaled(outerX, unit), scaled(3.2, unit));
        page.addPoint(scaled(spineX, unit), scaled(2.5, unit));
        page.addPoint(scaled(spineX, unit), scaled(14, unit));
        page.addPoint(scaled(outerX, unit), scaled(13.2, unit));
        return page;
    }

    /** Converts a coordinate in the 16 unit design grid to pixels. */
    private static int scaled(double gridValue, double unit) {
        return (int) Math.round(gridValue * unit);
    }
}
