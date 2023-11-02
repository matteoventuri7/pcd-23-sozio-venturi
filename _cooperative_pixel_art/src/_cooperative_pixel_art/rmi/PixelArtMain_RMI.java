package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.core.Brush;
import _cooperative_pixel_art.core.IBrushManager;
import _cooperative_pixel_art.core.PixelGrid;
import _cooperative_pixel_art.core.PixelGridView;
import _cooperative_pixel_art.rabbitqm.BrushManagerRabbitMQ;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class PixelArtMain_RMI {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) {
		String remoteHost = "localhost", localHost="localhost";
		boolean iAmPrincipal = true;
		String title = "PixelArt MAIN";
		if(args != null && args.length > 0){
			remoteHost = args[0];
		}
		if(args != null && args.length > 1){
			iAmPrincipal = args[1].equals("true");
		}
		if(args != null && args.length > 2){
			title = args[2];
		}
		if(args != null && args.length > 3){
			localHost = args[3];
		}

		var localBrush = new Brush(UUID.randomUUID().toString(), 0, 0, randomColor());

		IBrushManager brushManager;

		if(iAmPrincipal){
			brushManager = new RmiServerBrushManager(localBrush, localHost, IRemoteBrushManager.remoteName);
		} else{
			brushManager = new RmiClientBrushManager(localBrush, remoteHost, IRemoteBrushManager.remoteName, localHost);
		}

		PixelGrid grid = new PixelGrid(40, 40);

		PixelGridView view = new PixelGridView(grid, brushManager, title,800, 800);

		brushManager.setGrid(grid);
		brushManager.setView(view);

		view.addMouseMovedListener((x, y) -> {
			brushManager.updatePosition(x, y);
		});

		view.addPixelGridEventListener((x, y) -> {
			brushManager.updatePixel(x, y, localBrush.getColor());
		});

		view.addColorChangedListener(localBrush::setColor);

		view.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try {
					brushManager.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		brushManager.run();

		view.display();
	}
}

