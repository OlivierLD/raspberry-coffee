package images;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;

import javax.imageio.ImageIO;
import javax.media.jai.RenderedImageAdapter;
import javax.swing.ImageIcon;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImageUtil {
	public final static int NO_CHANGE = 0;
	public final static int SHARPEN = 1;
	public final static int BLUR = 2;

	public static BufferedImage switchColor(BufferedImage bi, Color c) {
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
//      if (get(bi, i, j).equals(Color.black)) { // Assuming black stays
				if (!get(bi, i, j).equals(Color.white)) { // Assuming white becomes tranparent
					set(bi, i, j, c);
				}
			}
		}
		return bi;
	}

	public static BufferedImage sharpen(BufferedImage bimg) {
		float data[] =
				{-1, -1, -1,
						-1, 9, -1,
						-1, -1, -1};
		Kernel kernel = new Kernel(3, 3, data);
		BufferedImageOp op = new ConvolveOp(kernel);
		bimg = op.filter(bimg, null);
		return bimg;
	}

	public static BufferedImage blur(BufferedImage bimg) {
		float blurMatrix[] =
				{0.0625f, 0.125f, 0.0625f,
						0.125f, 0.25f, 0.125f,
						0.0625f, 0.125f, 0.0625f};
		Kernel kernel = new Kernel(3, 3, blurMatrix);
		BufferedImageOp blurFilter = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		bimg = blurFilter.filter(bimg, null);
		return bimg;
	}

	public static BufferedImage blur(BufferedImage bimg, int matrixDim) {
		//  System.out.println("Blur dim:" + matrixDim);
		float blurMatrix[] = new float[matrixDim * matrixDim];
		for (int i = 0; i < blurMatrix.length; i++) {
			blurMatrix[i] = 1f / (float) (matrixDim * matrixDim);
		}
		Kernel kernel = new Kernel(matrixDim, matrixDim, blurMatrix);
		BufferedImageOp blurFilter = new ConvolveOp(kernel,
				ConvolveOp.EDGE_NO_OP, // ConvolveOp.EDGE_ZERO_FILL
				null);
		bimg = blurFilter.filter(bimg, null);
		return bimg;
	}

	public static BufferedImage turnColorTransparent(BufferedImage bi, Color c) {
		return turnColorTransparent(bi, c, NO_CHANGE);
	}

	public static BufferedImage turnColorTransparent(BufferedImage bi, Color c, int option) {
		for (int i = 0; i < bi.getWidth(); i++) {
			for (int j = 0; j < bi.getHeight(); j++) {
				Color thisColor = get(bi, i, j);
				if (!thisColor.equals(c)) {
					set(bi, i, j, thisColor);
				}
			}
		}
		if (option == SHARPEN) {
			bi = sharpen(bi);
		} else if (option == BLUR) {
			bi = blur(bi);
		}
		return bi;
	}

	public static void writeImageToFile(Image img,
	                                    String formatName,
	                                    String fileName) throws Exception {
		BufferedImage bi = null;
		if (img instanceof BufferedImage) {
			bi = (BufferedImage) img;
		} else {
			bi = toBufferedImage(img);
		}
		ImageIO.write(bi, formatName, new File(fileName));
	}

	public static ColorModel getColorModel(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		ColorModel cm = pg.getColorModel();
		return cm;
	}

	private static ColorModel generateColorModel() {
		// Generate 16-color model
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];

		r[0] = 0;
		g[0] = 0;
		b[0] = 0;
		r[1] = 0;
		g[1] = 0;
		b[1] = (byte) 192;
		r[2] = 0;
		g[2] = 0;
		b[2] = (byte) 255;
		r[3] = 0;
		g[3] = (byte) 192;
		b[3] = 0;
		r[4] = 0;
		g[4] = (byte) 255;
		b[4] = 0;
		r[5] = 0;
		g[5] = (byte) 192;
		b[5] = (byte) 192;
		r[6] = 0;
		g[6] = (byte) 255;
		b[6] = (byte) 255;
		r[7] = (byte) 192;
		g[7] = 0;
		b[7] = 0;
		r[8] = (byte) 255;
		g[8] = 0;
		b[8] = 0;
		r[9] = (byte) 192;
		g[9] = 0;
		b[9] = (byte) 192;
		r[10] = (byte) 255;
		g[10] = 0;
		b[10] = (byte) 255;
		r[11] = (byte) 192;
		g[11] = (byte) 192;
		b[11] = 0;
		r[12] = (byte) 255;
		g[12] = (byte) 255;
		b[12] = 0;
		r[13] = (byte) 80;
		g[13] = (byte) 80;
		b[13] = (byte) 80;
		r[14] = (byte) 192;
		g[14] = (byte) 192;
		b[14] = (byte) 192;
		r[15] = (byte) 255;
		g[15] = (byte) 255;
		b[15] = (byte) 255;

		return new IndexColorModel(4, 16, r, g, b);
	}

	public static Color get(BufferedImage image, int i, int j) {
		return new Color(image.getRGB(i, j));
	}

	// change color of pixel (i, j) to c

	public static void set(BufferedImage image, int i, int j, Color c) {
		image.setRGB(i, j, c.getRGB());
	}

	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return (cm == null ? false : cm.hasAlpha());
	}

	public static BufferedImage toBufferedImage(Image image) {
		return toBufferedImage(image, 0D);
	}

	/**
	 *
	 * @param image
	 * @param rotation in Radians
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image, double rotation) {
		if (image == null) {
			return null;
		}

		if (image instanceof BufferedImage && (Math.sin(rotation) == 0 && Math.cos(rotation) == 1)) { // No change needed
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null),
					transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null || rotation != 0D) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			if (rotation == 0D) {
				bimage = new BufferedImage(image.getWidth(null),
						image.getHeight(null),
						type);
			} else {
				if (Math.abs(rotation) == (Math.PI / 2D) || Math.abs(rotation) == (3 * Math.PI / 2D))
					bimage = new BufferedImage(image.getHeight(null),
							image.getWidth(null),
							type);
			}
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		Graphics2D g2d = (Graphics2D) g;
		// Rotate here?
		if (rotation != 0D) {
			AffineTransform at = new AffineTransform();
			double tx = (image.getWidth(null) / 2) - (image.getHeight(null) / 2);
			double ty = (image.getHeight(null) / 2) - (image.getWidth(null) / 2);
			at.translate(-tx, -ty);
			at.rotate(rotation, image.getWidth(null) / 2, image.getHeight(null) / 2);
			g2d.drawImage(image, at, null);
		} else {
			// Paint the image onto the buffered image
			g2d.drawImage(image, 0, 0, null);
		}
		g2d.dispose();

		return bimage;
	}

	public static Image readImage(String fileName)
			throws Exception {
		Image faxImg = null;
		if (fileName.toUpperCase().endsWith(".TIF") || fileName.toUpperCase().endsWith(".TIFF")) {
			File file = new File(fileName);
			if (file.exists()) {
				SeekableStream s = new FileSeekableStream(file);
				TIFFDecodeParam param = null;
				ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
				// Which of the multiple images in the TIFF file do we want to load
				// 0 refers to the first, 1 to the second and so on.
				RenderedImageAdapter ria = new RenderedImageAdapter(dec.decodeAsRenderedImage());
				faxImg = ria.getAsBufferedImage();
				s.close();
			}
		} else {
			faxImg = new ImageIcon(new File(fileName).toURI().toURL()).getImage();
		}
		return faxImg;
	}

	public static Image readImage(InputStream is, boolean tif)
			throws Exception {
		Image faxImg = null;
		if (tif) {
			TIFFDecodeParam param = null;
			ImageDecoder dec = ImageCodec.createImageDecoder("tiff", is, param);
			// Which of the multiple images in the TIFF file do we want to load
			// 0 refers to the first, 1 to the second and so on.
			RenderedImageAdapter ria = new RenderedImageAdapter(dec.decodeAsRenderedImage());
			faxImg = ria.getAsBufferedImage();
		} else {
			byte[] imageData = new byte[is.available()];
			int offset = 0;
			int numRead = 0;
			while (offset < imageData.length && (numRead = is.read(imageData, offset, imageData.length - offset)) >= 0)
				offset += numRead;

			faxImg = new ImageIcon(imageData).getImage();
		}
		is.close();
		return faxImg;
	}

	public static BufferedImage makeColorTransparent(Image img, Color color) {
		return makeColorTransparent(img, color, NO_CHANGE);
	}

	public static BufferedImage makeColorTransparent(Image img, Color color, int option) {
		return makeColorTransparent(img, color, option, null);
	}

	public static BufferedImage makeColorTransparent(Image img, Color color, int option, String[] messElements) {
		if (messElements != null) {
			System.out.println("Progressing..."); // messElements
		}
		BufferedImage image = toBufferedImage(img);
		BufferedImage dimg = new BufferedImage(image.getWidth(),
				image.getHeight(),
				BufferedImage.TYPE_INT_ARGB); // To be used for set/getRGB, Alpha Red Green Blue
		Graphics2D g = dimg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(image, null, 0, 0);
		g.dispose();
		for (int i = 0; i < dimg.getHeight(); i++) {
			for (int j = 0; j < dimg.getWidth(); j++) {
				if (dimg.getRGB(j, i) == color.getRGB()) {
					dimg.setRGB(j, i, 0x008F1C1C); // 00 8f 1c 1c
				}
			}
		}
		if (option == SHARPEN) {
			dimg = sharpen(dimg);
		} else if (option == BLUR) {
			dimg = blur(dimg);
		}
		return dimg;
	}

	public static BufferedImage switchColorAndMakeColorTransparent(Image img,
	                                                               Color turnThis,
	                                                               Color intoThat,
	                                                               Color colorToTransparent) {
		return switchColorAndMakeColorTransparent(img, turnThis, intoThat, colorToTransparent, NO_CHANGE);
	}

	public static BufferedImage switchColorAndMakeColorTransparent(Image img,
	                                                               Color turnThis,
	                                                               Color intoThat,
	                                                               Color colorToTransparent,
	                                                               int option) {
		return switchColorAndMakeColorTransparent(img, turnThis, intoThat, colorToTransparent, option, null);
	}

	public static BufferedImage switchColorAndMakeColorTransparent(Image img,
	                                                               Color turnThis,
	                                                               Color intoThat,
	                                                               Color colorToTransparent,
	                                                               int option,
	                                                               String[] messElements) {
		if (messElements != null) {
			System.out.println("Progressing..."); // messElements));
		}
		BufferedImage image = toBufferedImage(img);
		BufferedImage dimg = new BufferedImage(image.getWidth(),
				image.getHeight(),
				BufferedImage.TYPE_INT_ARGB); // To be used for set/getRGB, Alpha Red Green Blue
		Graphics2D g = dimg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(image, null, 0, 0);
		g.dispose();
		for (int i = 0; i < dimg.getHeight(); i++) {
			for (int j = 0; j < dimg.getWidth(); j++) {
				if (turnThis != null && dimg.getRGB(j, i) == turnThis.getRGB()) {
					dimg.setRGB(j, i, intoThat.getRGB()); // Switch Color
				}
				if ((dimg.getRGB(j, i) & 0x00FFFFFF) == (colorToTransparent.getRGB() & 0x00FFFFFF)) {
//        System.out.println("Making " + Integer.toHexString(dimg.getRGB(j, i) & 0x00ffffff) + " transparent.");
					dimg.setRGB(j, i, 0x008F1C1C); // 00 8f 1c 1c, Make transparent
				} else if (turnThis == null) {
//        System.out.println("Turning " + dimg.getRGB(j, i) + " into " + intoThat.getRGB());
					dimg.setRGB(j, i, intoThat.getRGB()); // Switch Color
				}
			}
		}
		if (option == SHARPEN) {
			dimg = sharpen(dimg);
		} else if (option == BLUR) {
			dimg = blur(dimg);
		}
		return dimg;
	}

	public static BufferedImage switchAnyColorAndMakeColorTransparent(Image img,
	                                                                  Color intoThat,
	                                                                  Color colorToTransparent) {
		return switchAnyColorAndMakeColorTransparent(img, intoThat, colorToTransparent, NO_CHANGE);
	}

	public static BufferedImage switchAnyColorAndMakeColorTransparent(Image img,
	                                                                  Color intoThat,
	                                                                  Color colorToTransparent,
	                                                                  int option) //,
	// String[] messElements)
	{
		return switchColorAndMakeColorTransparent(img, null, intoThat, colorToTransparent, option, null);
	}

	/*
	 * For some tests
	 */
	public static int countColors(Image img) {
		BufferedImage image = toBufferedImage(img);
		java.util.List<Color> colors = new ArrayList<>();
		if (image != null) { // Count number of colors
			int w = image.getWidth();
			int h = image.getHeight();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
//        String colorStr = Integer.toHexString(pixel);
					int red = (pixel & 0x00ff0000) >> 16;
					int green = (pixel & 0x0000ff00) >> 8;
					int blue = pixel & 0x000000ff;
					Color color = new Color(red, green, blue);
					//add the first color on array
					if (colors.size() == 0) {
						colors.add(color);
//          System.out.println("1. Added " + colorStr + " (" + color.getRGB() + ")");
					} else { // check for redudancy
						if (!(colors.contains(color))) {
							colors.add(color);
//            System.out.println("2. Added " + colorStr + " (" + color.getRGB() + ")");
						}
					}
				}
			}
//    System.out.println("There are " + colors.size() + " colors in this image");
		}
		return colors.size();
	}

	public static Color mostUsedColor(Image img) {
		Color mostUsedColor = null;
		BufferedImage image = toBufferedImage(img);
		Map<Color, Integer> nbPixelPerColor = new HashMap<>();
		if (image != null) { // Count number of colors
			int w = image.getWidth();
			int h = image.getHeight();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
//        String colorStr = Integer.toHexString(pixel);
					int red = (pixel & 0x00ff0000) >> 16;
					int green = (pixel & 0x0000ff00) >> 8;
					int blue = pixel & 0x000000ff;
					Color color = new Color(red, green, blue);
					nbPixelPerColor.put(color, (nbPixelPerColor.get(color) == null) ? 1 : nbPixelPerColor.get(color) + 1);
				}
			}
			Set<Color> keys = nbPixelPerColor.keySet();

			int max = 0;
			for (Color c : keys) {
				//   System.out.println(Integer.toHexString(c.getRGB()).toUpperCase() + " = " + nbPixelPerColor.get(c) + " pixel(s).");
				if (nbPixelPerColor.get(c) > max) {
					max = nbPixelPerColor.get(c);
					mostUsedColor = c;
				}
			}
			System.out.println("Max pixels:" + max);
		}
		return mostUsedColor;
	}

	public static double luminance(int pixel) {
		int red = (pixel & 0x00ff0000) >> 16;
		int green = (pixel & 0x0000ff00) >> 8;
		int blue = pixel & 0x000000ff;
		double lum = (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
		return lum;
	}

	public static void minMaxLum(Image img) {
		BufferedImage image = toBufferedImage(img);
		double minLum = Double.MAX_VALUE;
		double maxLum = -Double.MAX_VALUE;
		if (image != null) {
			int w = image.getWidth();
			int h = image.getHeight();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
					double lum = luminance(pixel);
					minLum = Math.min(lum, minLum);
					maxLum = Math.max(lum, maxLum);
				}
			}
		}
		System.out.println("Luminance: min:" + minLum + ", max:" + maxLum);
	}

	public static Point findMaxLum(Image img) {
		Point max = null;
		BufferedImage image = toBufferedImage(img);

		int nbPt = 0;
		long accX = 0, accY = 0;
		double maxLum = -Double.MAX_VALUE;
		if (image != null) {
			int w = image.getWidth();
			int h = image.getHeight();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
					double lum = luminance(pixel);
					if (lum == maxLum) {
						nbPt++;
						accX += x;
						accY += y;
					} else if (lum > maxLum) {
						maxLum = lum;
						accX = x;
						accY = y;
						nbPt = 1;
					}
				}
			}
		}
		if (nbPt != 0) {
			max = new Point((int) Math.round(accX / nbPt),
											(int) Math.round(accY / nbPt));
		}
		return max;
	}

	public static Point findSpot(Image img, Color c) {
		BufferedImage image = toBufferedImage(img);
		long accX = 0, accY = 0, nbPix = 0;
		if (image != null) {
			int w = image.getWidth();
			int h = image.getHeight();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y);
//        String colorStr = Integer.toHexString(pixel); System.out.println(">> " + colorStr);
					int red = (pixel & 0x00ff0000) >> 16;
					int green = (pixel & 0x0000ff00) >> 8;
					int blue = pixel & 0x000000ff;
					Color color = new Color(red, green, blue);
					if (color.getRed() == c.getRed() && color.getGreen() == c.getGreen() && color.getBlue() == c.getBlue()) {
//          System.out.println("Found color at " + x + "/" + y);
						accX += x;
						accY += y;
						nbPix += 1;
					}
//        if (color.getRed() == 50 && color.getGreen() == 205 && color.getBlue() == 49)
//          System.out.println("Found at " + x + "/" + y);
//        if (color.getRed() != 50 || color.getGreen() != 205 || color.getBlue() != 49)
//          System.out.println("Found " + color.toString() + " at " + x + "/" + y);
				}
			}
		}
		Point spot = null;
		if (nbPix != 0) {
			spot = new Point((int) Math.round(accX / nbPix),
											 (int) Math.round(accY / nbPix));
		}
		return spot;
	}
}
