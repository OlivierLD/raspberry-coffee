package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
	/**
	 * Decode string to image
	 *
	 * @param imageString The string to decode
	 * @return decoded image
	 */
	public static BufferedImage decodeToImage(String imageString) {

		BufferedImage image = null;
		byte[] imageByte;
		try {
			Base64.Decoder mimeDecoder = Base64.getMimeDecoder();
			imageByte = mimeDecoder.decode(imageString);

			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = ImageIO.read(bis);
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * Decode string to image
	 *
	 * @param encodedString The string to decode
	 * @return decoded String
	 */
	public static String decodeToString(String encodedString) throws IllegalArgumentException {

		byte[] decodedBytes = null;
		try {
			Base64.Decoder mimeDecoder = Base64.getMimeDecoder();
			decodedBytes = mimeDecoder.decode(encodedString);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String(decodedBytes);
	}

	/**
	 * Encode image to string
	 *
	 * @param image The image to encode
	 * @param type  jpeg, bmp, ...
	 * @return encoded string
	 */
	public static String encodeToString(BufferedImage image, String type) {
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();

			imageString = new String(Base64.getMimeEncoder().encode(imageBytes), StandardCharsets.UTF_8.toString());

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageString;
	}

	/**
	 * Encode string to string
	 *
	 * @param string The string to encode
	 * @return encoded string
	 */
	public static String encodeToString(String string) {
		String encodedString = null;
		try {
			byte[] imageBytes = string.getBytes();
			encodedString = new String(Base64.getMimeEncoder().encode(imageBytes), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
		return encodedString;
	}

	public static void main(String... args) {
		System.out.println("A test. Reads a jpg, encodes it, decodes into a png.");
		if (false) {
			String IMG_RADIX = "Adafruit.IO.REST" + File.separator + "bonus";
			try {
				BufferedImage img = ImageIO.read(new File(IMG_RADIX + ".jpg"));
				BufferedImage newImg;
				String imgstr;
				imgstr = Base64Util.encodeToString(img, "jpg");
				System.out.println(imgstr);
				FileOutputStream fos = new FileOutputStream("Adafruit.IO.REST" + File.separator + "image.txt");
				fos.write(imgstr.getBytes());
				fos.close();
				newImg = Base64Util.decodeToImage(imgstr);
				ImageIO.write(newImg, "png", new File(IMG_RADIX + ".png"));
			} catch (IOException ioe) {
				String where = new File(".").getAbsolutePath();
				System.err.println("From " + where);
				ioe.printStackTrace();
			}
		}
		// String encoding
		System.out.println("=============================================");
		String toEncode = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		String encoded = Base64Util.encodeToString(toEncode);
		System.out.printf("1. Encoded (%d -> %d): %s\n", toEncode.length(), encoded.length(),  encoded);
		String decoded = Base64Util.decodeToString(encoded);
		System.out.println("2. Re-decoded:" + decoded);

		try {
			String decoded_ = Base64Util.decodeToString(encoded + "1234");
		} catch (IllegalArgumentException iae) {
			System.out.println("3. Bad Base64 String...");
		}

//		String sshKey = "AAAAB3NzaC1yc2EAAAADAQABAAACAQCrWhhJQo89W1SCtDKD9+e/CImjvcgZIRNNVmJkjv4Fxd8w6x6i0nAuSuMbc+0Jm4nD6Wl8oG1dc3k80BYQseO7h+vrYX8+ae7LixLrPSDi48MvgDyprJRtiXhVrdAvKCUSluPV8rzGTeX1szzaq15B8v83fZibRcFGX/f5srCkBNixvKZqXHqhLLNIGqcpUOsQwUTsQDRF3yVJ4cAaKmU38cYC9d10C/jazb6FKY2NTjvcPe4v0+PUFBTJoJYT2MwnC71gJHr0CKNKjrtCi1IgGsmsM6+ona5waa3yPGQwksIYoUa+vyeznBsYdoDdT/e4wfeWgX0apf17X9W68sem7Dsk6sFDBAEiyH1uFWfdKAo2AUwWQcOFpWwwqcbOCwT+OQPSuuoihs2r2DPPc/wb/5bC7bumKgr4rkcHfY7JRUVXfP81yR3lK0g/tBei7MB3hkDp2bhfO5LP4OMDjPfFyiBgLEZGS1U8cjLzg84eHk5weO1b71J25mMIWe6kNpsXfMN9QLJ34SSqaCcsEPv92v7cpQ29hYlGgqQxI57Za9oXkQrkrzeNsS6Tx8V0BTUNdKenIGV0VKSJ95MhA3OOscaFCJ/jWc2E0eH7ftnALhHgRqcWxLvCoI2J26sLO87rbJ6dARBLfAU9BQkNGjFDYUWGDhSoalqZVYrdHpaZ2Q==";
//		try {
//			String decoded_ = Base64Util.decodeToString(sshKey);
//			System.out.println("SSH Key decoded:" + decoded_);
//		} catch (IllegalArgumentException iae) {
//			System.out.println("4. Bad Base64 String...");
//		}
	}
}
