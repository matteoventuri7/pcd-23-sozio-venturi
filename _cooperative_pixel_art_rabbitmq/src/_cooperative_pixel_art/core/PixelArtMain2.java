package _cooperative_pixel_art.core;

import java.util.Random;
import java.util.UUID;

public class PixelArtMain2 {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) throws Exception {
		Thread.sleep(5000);

		PixelArtMain.main(new String[]{"localhost", "brushes", "false", "PixelArt"});
	}
}

