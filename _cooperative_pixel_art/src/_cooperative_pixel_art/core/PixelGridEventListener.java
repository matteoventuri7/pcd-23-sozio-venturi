package _cooperative_pixel_art.core;

import java.io.IOException;

public interface PixelGridEventListener {
	void selectedCell(int x, int y) throws IOException;
}
