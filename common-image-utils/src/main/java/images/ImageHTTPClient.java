package images;

import images.gifutil.GIFInputStream;
import images.gifutil.GIFOutputStream;
import images.gifutil.Gif;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageHTTPClient {

	public static Image getFax(final String urlString, String fileName) throws Exception {

		boolean verbose = "true".equals(System.getProperty("fax.image.verbose", "false"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_z");

		Image image = null;
		Gif gifImage = null;

		if (verbose) {
			System.out.println("...reading (1) " + urlString);
		}
		String fName = fileName;
		if (fName == null) {
			fName = sdf.format(new Date()) + ".jpg"; // Default name.
		}

		URL faxUrl = new URL(urlString);
		if (fName.endsWith(".gif")) {
			URLConnection urlConn = faxUrl.openConnection();
			gifImage = new Gif();
			gifImage.init(new GIFInputStream(urlConn.getInputStream()));
		} else if (urlString.toUpperCase().endsWith(".TIFF") || urlString.toUpperCase().endsWith(".TIF")) {
			InputStream is = faxUrl.openStream();
			image = ImageUtil.readImage(is, true);
		} else if (urlString.toUpperCase().endsWith(".BZ2")) {
			System.out.println("BZ2 support available soon");
		} else {
			image = ImageIO.read(faxUrl);
		}

		File f = new File(fName);
		if (gifImage != null) { // Gif
			gifImage.write(new GIFOutputStream(new FileOutputStream(f)));
		}
		if (image != null) {   // other formats
			if (fName.endsWith(".jpg")) {
				ImageIO.write((RenderedImage) image, "jpg", f);
			} else if (fName.endsWith(".png")) {
				ImageIO.write((RenderedImage) image, "png", f);
			} else {
				System.out.println("Extension not supported (" + fName + ")");
			}
		} else {
			System.out.println(String.format("Image not found %s", urlString));
		}
		if (verbose) {
			System.out.println(String.format("New Fax available %s", f.getAbsolutePath()));
		}
		return image;
	}
}
