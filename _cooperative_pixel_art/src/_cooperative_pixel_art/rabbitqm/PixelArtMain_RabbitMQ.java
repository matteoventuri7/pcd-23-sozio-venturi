package _cooperative_pixel_art.rabbitqm;

import _cooperative_pixel_art.core.Brush;
import _cooperative_pixel_art.core.IBrushManager;
import _cooperative_pixel_art.core.PixelGrid;
import _cooperative_pixel_art.core.PixelGridView;

import java.util.Random;
import java.util.UUID;

public class PixelArtMain_RabbitMQ {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) throws Exception {
		String host = "localhost";
		String exchangeName = "brushes";
		boolean iAmBroken = true;
		String title = "PixelArt MAIN";
		if(args != null && args.length > 0){
			host = args[0];
		}
		if(args != null && args.length > 1){
			exchangeName = args[1];
		}
		if(args != null && args.length > 2){
			iAmBroken = args[2].equals("true");
		}
		if(args != null && args.length > 3){
			title = args[3];
		}

		var localBrush = new Brush(UUID.randomUUID().toString(), 0, 0, randomColor());

		IBrushManager brushManager = new BrushManagerRabbitMQ(localBrush, host, exchangeName, iAmBroken);

		PixelGrid grid = new PixelGrid(40, 40);

		PixelGridView view = new PixelGridView(grid, brushManager, title,800, 800);

		brushManager.setGrid(grid);
		brushManager.setView(view);

		view.addMouseMovedListener((x, y) -> {
			brushManager.updatePosition(x, y);
			view.refresh();
		});

		view.addPixelGridEventListener((x, y) -> {
			brushManager.updatePixel(x, y, localBrush.getColor());
			view.refresh();
		});

		view.addColorChangedListener(localBrush::setColor);

		view.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try {
					brushManager.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		brushManager.run();

		view.display();
	}
}

