package image.util.samples;

import images.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;

public class ChangeColor {
	/**
	 * Turn 2 black and white, opaque faxes, into a transparent blue one and a transparent red on.
	 * Then display them in a web page, superimposed.
	 *
	 * TODO Add blur & sharp, drawing underneath
	 *
	 * @param args CLI parameters
	 * @throws Exception Oops
	 */
	public static void main(String... args)
			throws Exception {
		final String IMG_NAME_1 = "NOAA_sfc_2017_10_16_07_31_42_PDT.png";
		final String IMG_NAME_2 = "NOAA_sfc_2017_08_02_07_52_35_PDT.png";
		final String IMG_NAME_3 = "NOAA_sfc_2017_08_02_07_52_35_PDT.png"; // Yes, same as above.

		long before = System.currentTimeMillis();
	  BufferedImage bimg = ImageIO.read(new File(IMG_NAME_1));
		long after = System.currentTimeMillis();
		long reading = after - before;

	  int width          = bimg.getWidth();
	  int height         = bimg.getHeight();
	  System.out.println("Image size is " + width + " x " + height);

		before = System.currentTimeMillis();
		// Transparent blue
		bimg = ImageUtil.switchColorAndMakeColorTransparent(bimg, Color.black, Color.blue, Color.white);
		after = System.currentTimeMillis();
		long tx = after - before;

		before = System.currentTimeMillis();
		ImageUtil.writeImageToFile(bimg, "png", "web" + File.separator + IMG_NAME_1);
		after = System.currentTimeMillis();
		long writing = after - before;

		NumberFormat nf = NumberFormat.getInstance();

		System.out.printf("Reading took %s ms\n", nf.format(reading));
		System.out.printf("Transforming took %s ms\n", nf.format(tx));
		System.out.printf("Writing took %s ms\n", nf.format(writing));

		System.out.printf("All together %s ms\n", nf.format(reading + tx + writing));

		bimg = ImageIO.read(new File(IMG_NAME_2));
		// Transparent red
		bimg = ImageUtil.switchColorAndMakeColorTransparent(bimg, Color.black, Color.red, Color.white);
		ImageUtil.writeImageToFile(bimg, "png", "web" + File.separator + IMG_NAME_2);

//	bimg = ImageIO.read(new File(IMG_NAME_3));
		bimg = ImageUtil.toBufferedImage(ImageUtil.readImage(IMG_NAME_3), Math.toRadians(90));
		// Transparent pink
		bimg = ImageUtil.switchColorAndMakeColorTransparent(bimg, Color.black, Color.magenta, Color.white);
		ImageUtil.writeImageToFile(bimg, "png", "web" + File.separator + "_" + IMG_NAME_3);

		// Transform template
		BufferedReader br = new BufferedReader(new FileReader("web" + File.separator + "template.html"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("web" + File.separator + "index.html"));
		String line = "";
		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.contains("<FAX_1>")) {
					line = line.replace("<FAX_1>", IMG_NAME_1);
				}
				if (line.contains("<FAX_2>")) {
					line = line.replace("<FAX_2>", IMG_NAME_2);
				}
				if (line.contains("<FAX_3>")) {
					line = line.replace("<FAX_3>", "_" + IMG_NAME_3);
				}
				bw.write(line + "\n");
			}
		}
		br.close();
		bw.close();

		Desktop.getDesktop().browse(new File("./web/index.html").toURI());

//		Image image = ImageUtil.readImage(IMG_NAME_1);
//		BufferedImage bimg2 = ImageUtil.toBufferedImage(image);
//		System.out.printf("Image is%s a BufferedImage.", (image instanceof BufferedImage) ? "" : " not"));
	}
}
