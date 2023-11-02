package _cooperative_pixel_art.core;

import java.awt.*;

public interface IBrushManager extends AutoCloseable {
    void draw(Graphics2D g2);

    void setGrid(PixelGrid grid);

    void setView(PixelGridView view);

    void updatePosition(int x, int y);

    void updatePixel(int x, int y, int color);

    void addBrush(final IBrush brush);
    void removeBrush(final IBrush brush);

    void run();
}
