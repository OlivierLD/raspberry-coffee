package image.util.samples;

import images.ImageHTTPClient;
import images.ImageUtil;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SeveralFaxes {

	public static void main(String... args)
			throws Exception {
		final String FAX_NAME_1 = "http://www.opc.ncep.noaa.gov/P_sfc_full_ocean.gif";  // Surface, current
		final String FAX_NAME_2 = "http://www.opc.ncep.noaa.gov/shtml/P_06hr500bw.gif"; // 500mb, current
		final String FAX_NAME_3 = "http://www.prh.noaa.gov/hnl/graphics/stream.gif";    // Streamlines

		final String IMG_NAME_1 = "NOAA_sfc_1.png";
		final String IMG_NAME_2 = "NOAA_500_2.png";
		final String IMG_NAME_3 = "NOAA_Stream_2.png";

		Image fax1 = ImageHTTPClient.getFax(FAX_NAME_1, "web" + File.separator + IMG_NAME_1);
		BufferedImage bimg1 = ImageUtil.toBufferedImage(fax1);
		// Transparent blue, blur
		bimg1 = ImageUtil.switchColorAndMakeColorTransparent(bimg1, Color.black, Color.blue, Color.white, ImageUtil.BLUR);
		ImageUtil.writeImageToFile(bimg1, "png", "web" + File.separator + "_" + IMG_NAME_1);

		Image fax2 = ImageHTTPClient.getFax(FAX_NAME_2, "web" + File.separator + IMG_NAME_2);
		BufferedImage bimg2 = ImageUtil.toBufferedImage(fax2);
		// Transparent red, blur
		bimg2 = ImageUtil.switchColorAndMakeColorTransparent(bimg2, Color.black, Color.red, Color.white, ImageUtil.BLUR);
		ImageUtil.writeImageToFile(bimg2, "png", "web" + File.separator + "_" + IMG_NAME_2);

		Image fax3 = ImageHTTPClient.getFax(FAX_NAME_3, "web" + File.separator + IMG_NAME_3);
		BufferedImage bimg3 = ImageUtil.toBufferedImage(fax3);
		// Transparent, blur, no color change.
		bimg3 = ImageUtil.makeColorTransparent(bimg3, Color.white, ImageUtil.BLUR);
		ImageUtil.writeImageToFile(bimg3, "png", "web" + File.separator + "_" + IMG_NAME_3);

		// Transform template
		BufferedReader br = new BufferedReader(new FileReader("web" + File.separator + "template.html"));
		BufferedWriter bw = new BufferedWriter(new FileWriter("web" + File.separator + "index.html"));
		String line = "";
		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.contains("<FAX_1>")) {
					line = line.replace("<FAX_1>", "_" + IMG_NAME_1);
				}
				if (line.contains("<FAX_2>")) {
					line = line.replace("<FAX_2>", "_" + IMG_NAME_2);
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
	}
}
