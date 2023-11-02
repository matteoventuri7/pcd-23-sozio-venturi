package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.IBrush;
import _cooperative_pixel_art.core.PixelGrid;

import java.util.List;

public class History implements IHistory{

    private final List<IBrush> brushes;
    private final PixelGrid grid;

    public History(List<IBrush> brushes, PixelGrid grid) {
        this.brushes = brushes;
        this.grid = grid;
    }

    @Override
    public List<IBrush> getBrushes() {
        return brushes;
    }

    @Override
    public PixelGrid getGrid() {
        return grid;
    }
}
