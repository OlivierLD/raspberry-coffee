package weatherstation;

import utils.Base64Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageEncoder {
	public static void main(String... args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Need the file (image) name as parameter");
		}
		String imageFileName = args[0];
		String imageType = imageFileName.substring(imageFileName.lastIndexOf(".") + 1); // like 'jpg'
		try {
			BufferedImage img = ImageIO.read(new File(imageFileName));
			String imgstr = Base64Util.encodeToString(img, imageType);
			System.out.println(imgstr);
		} catch (IOException ioe) {
			String where = new File(".").getAbsolutePath();
			System.err.println("From " + where);
			ioe.printStackTrace();
		}
	}
}
