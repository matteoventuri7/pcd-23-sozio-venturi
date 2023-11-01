package _cooperative_pixel_art.rabbitqm;

import java.util.Random;

public class PixelArtMain2_RabbitMQ {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) throws Exception {
		Thread.sleep(5000);

		PixelArtMain_RabbitMQ.main(new String[]{"localhost", "brushes", "false", "PixelArt 2"});

		Thread.sleep(30000);

		PixelArtMain_RabbitMQ.main(new String[]{"localhost", "brushes", "false", "PixelArt 3"});
	}
}

