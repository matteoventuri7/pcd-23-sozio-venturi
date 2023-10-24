package _cooperative_pixel_art.core;

import java.util.Random;
import java.util.UUID;

public class PixelArtMain {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) throws Exception {
		String host = "localhost";
		if(args != null && args.length > 0){
			host = args[0];
		}

		var localBrush = new BrushManager.Brush(UUID.randomUUID().toString(), 0, 0, randomColor());

		try(var brushManager = new BrushManager(localBrush, host, "brushes")) {

			PixelGrid grid = new PixelGrid(40, 40);

			PixelGridView view = new PixelGridView(grid, brushManager, 800, 800);

			brushManager.setGrid(grid);
			brushManager.setView(view);

			view.addMouseMovedListener((x, y) -> {
				brushManager.updatePosition(x, y);
				view.refresh();
			});

			view.addPixelGridEventListener((x, y) -> {
				brushManager.updatePixel(x, y, localBrush.getColor());
				grid.set(x, y, localBrush.getColor());
				view.refresh();
			});

			view.addColorChangedListener(localBrush::setColor);

			view.display();
		}
	}
}
