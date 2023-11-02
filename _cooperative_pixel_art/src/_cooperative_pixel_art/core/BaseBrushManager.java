package _cooperative_pixel_art.core;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseBrushManager implements IBrushManager{
    private static final int BRUSH_SIZE = 10;
    private static final int STROKE_SIZE = 2;
    protected final Set<IBrush> brushes = new HashSet<>();
    protected final IBrush localBrush;
    protected PixelGrid grid;
    protected PixelGridView view;

    protected BaseBrushManager(IBrush localBrush) {
        this.localBrush = localBrush;
    }

    public void setGrid(PixelGrid grid) {
        this.grid=grid;
    }

    public void setView(PixelGridView view) {
        this.view = view;
    }
    
    public void draw(final Graphics2D g) {
        brushes.forEach(brush -> {
            g.setColor(new Color(brush.getColor()));
            var circle = new java.awt.geom.Ellipse2D.Double(brush.getX() - BRUSH_SIZE / 2.0, brush.getY() - BRUSH_SIZE / 2.0, BRUSH_SIZE, BRUSH_SIZE);
            // draw the polygon
            g.fill(circle);
            g.setStroke(new BasicStroke(STROKE_SIZE));
            g.setColor(Color.BLACK);
            g.draw(circle);
        });
    }

    public void addBrush(final IBrush brush) {
        System.out.println("Adding brush " + brush.getId());
        brushes.add(brush);
    }

    public void removeBrush(final IBrush brush) {
        brushes.remove(brush);
    }

    public void updatePosition(int x, int y) {
        localBrush.updatePosition(x,y);
        view.refresh();
    }

    public void updatePixel(int x, int y, int color) {
        grid.set(x, y, color);
        view.refresh();
    }
}
