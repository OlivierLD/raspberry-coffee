package misc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

public class BMPReader {
	public static void main(String... args) {
		File bmpFile = new File("img/pic.240x240.bmp");
		try {
			BufferedImage image = ImageIO.read(bmpFile);
			System.out.println(String.format("Image was read, w: %d, h: %d", image.getWidth(), image.getHeight()));
			int[] pixel = new int[4]; // RGBA
			Raster data = image.getData();
			for (int row=0; row<image.getHeight(); row++) {
				for (int col=0; col<image.getWidth(); col++) {
					data.getPixel(col, row, pixel);
//					int rgb = (((pixel[0]>>3)<<11)|((pixel[1]>>2)<<5)|(pixel[2]>>3));
//					System.out.println(String.format("x:%d y:%d, pix: %d %d %d %d => %06x", col, row, pixel[0], pixel[1], pixel[2], pixel[3], rgb));
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done");
	}
}
