package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.IBrush;
import _cooperative_pixel_art.core.PixelGrid;

import java.io.Serializable;
import java.util.List;

public interface IHistory extends Serializable {
    List<IBrush> getBrushes();
    PixelGrid getGrid();
}
