package images;

import images.gifutil.GIFInputStream;
import images.gifutil.GIFOutputStream;
import images.gifutil.Gif;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ImageHTTPClient {

	public static Image getFax(final String urlString, String fileName) throws Exception {

		boolean verbose = "true".equals(System.getProperty("fax.image.verbose", "false"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_z");

		String retFile = "";
		String urlStr = urlString;
		Image image = null;
		Gif gifImage = null;
		try {
			if (verbose) {
				System.out.println("...reading (1) " + urlStr);
			}
			String fName = fileName;
			if (fName == null) {
				fName = sdf.format(new Date()) + ".jpg"; // Default name.
			}
			try {
				URL faxUrl = new URL(urlStr);
				if (fName.endsWith(".gif")) {
					URLConnection urlConn = faxUrl.openConnection();
					gifImage = new Gif();
					gifImage.init(new GIFInputStream(urlConn.getInputStream()));
				} else {
					boolean tif = urlStr.toUpperCase().endsWith(".TIFF") || urlStr.toUpperCase().endsWith(".TIF");
					if (tif) {
						InputStream is = faxUrl.openStream();
						image = ImageUtil.readImage(is, tif);
					} else if (urlStr.toUpperCase().endsWith(".BZ2")) {
						System.out.println("BZ2 support available soon");
					} else {
						image = ImageIO.read(faxUrl);
					}
				}
			} catch (final Exception e) {
				throw e;
			}
			if (image != null) {
				File f = new File(fName);

				if (fName.endsWith(".jpg")) {
					ImageIO.write((RenderedImage) image, "jpg", f);
				} else if (fName.endsWith(".gif")) {
					gifImage.write(new GIFOutputStream(new FileOutputStream(f)));
				} else if (fName.endsWith(".png")) {
					ImageIO.write((RenderedImage) image, "png", f);
				} else {
					System.out.println("Extension not supported (" + fName + ")");
				}
				retFile = f.getAbsolutePath();
				if (verbose) {
					System.out.println("New Fax available " + retFile);
				}
			} else {
				System.out.println(String.format("Image not found %s", urlStr));
			}
		} catch (Exception e) {
			throw e;
		}
		return image;
	}
}
