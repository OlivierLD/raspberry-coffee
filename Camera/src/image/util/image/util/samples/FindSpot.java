package image.util.image.util.samples;

import image.util.ImageUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import static image.util.ImageUtil.countColors;
import static image.util.ImageUtil.findMaxLum;
import static image.util.ImageUtil.findSpot;
import static image.util.ImageUtil.mostUsedColor;
import static image.util.ImageUtil.writeImageToFile;

public class FindSpot {
	public static void main(String[] args) throws Exception {
//  final String IMG_NAME = "white.spot.jpg"; // "red.dot.jpg"; // "P8150115.JPG";
//  final String IMG_NAME = "P8150115.JPG";
		final String IMG_NAME = "snap.jpg";

//  BufferedImage bimg = ImageIO.read(new File(IMG_NAME));
//  int width          = bimg.getWidth();
//  int height         = bimg.getHeight();
//  System.out.println("Image is " + width + " x " + height);

		Image image = ImageUtil.readImage(IMG_NAME);
		if (false) {
			int nbc = countColors(image);
			Color muc = mostUsedColor(image);
			System.out.println("Most used color is :" + muc.toString() + " (among " + nbc + ")");
		}
		System.out.println("Finding white spot...");
		long before = System.currentTimeMillis();
//  Point spot = findSpot(image, new Color(254, 0, 0));
		Point spot = findSpot(image, Color.white);
		long after = System.currentTimeMillis();
		if (spot == null)
			System.out.println("Spot not found...");
		else
			System.out.println("Spot found at " + spot.x + "/" + spot.y);
		System.out.println("... in " + Long.toString(after - before) + " ms");

		System.out.println("Finding bright spot...");
		before = System.currentTimeMillis();
		spot = findMaxLum(image);
		after = System.currentTimeMillis();
		if (spot == null)
			System.out.println("MaxLum Spot not found...");
		else
			System.out.println("MaxLum Spot found at " + spot.x + "/" + spot.y);
		System.out.println("... in " + Long.toString(after - before) + " ms");

		System.out.println("Image dim:" + image.getWidth(null) + "x" + image.getHeight(null));

		// For the test: Write new image, plotting the spot
		System.out.println("Plotting bright spot on plotted.png...");
		BufferedImage offImage = new BufferedImage(image.getWidth(null),
				image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = offImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.setColor(Color.red);
		g2.fillOval(spot.x - 2, spot.y - 2, 4, 4);
		g2.drawLine(spot.x, spot.y - 10, spot.x, spot.y + 10);
		g2.drawLine(spot.x - 10, spot.y, spot.x + 10, spot.y);
		g2.dispose();
		writeImageToFile(offImage, "png", "plotted.png");

//  minMaxLum(image);
	}
}
