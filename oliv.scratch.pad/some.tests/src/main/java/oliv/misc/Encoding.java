package oliv.misc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Encoding {
	public static void main(String... args)
	throws UnsupportedEncodingException {
		String encoding = "UTF-8";
		String toEncode = "This is an en coded String, with comma!";
		String encoded = URLEncoder.encode(toEncode, encoding);
		System.out.format("Encoded [%s]\n", encoded);
		System.out.println("Back:");
		String backToReadable = URLDecoder.decode(encoded, encoding);
		System.out.format("Decoded [%s]\n", backToReadable);
	}
}
