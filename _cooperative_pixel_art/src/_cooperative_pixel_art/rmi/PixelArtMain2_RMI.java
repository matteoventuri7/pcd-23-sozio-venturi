package _cooperative_pixel_art.rmi;

import _cooperative_pixel_art.rabbitqm.PixelArtMain_RabbitMQ;

import java.util.Random;

public class PixelArtMain2_RMI {

	public static int randomColor() {
		Random rand = new Random();
		return rand.nextInt(256 * 256 * 256);
	}

	public static void main(String[] args) throws Exception {
		Thread.sleep(5000);

		PixelArtMain_RMI.main(new String[]{"localhost", "false", "PixelArt 2"});
	}
}

