package oliv.misc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLEncoding {
	public static void main(String... args)
	throws UnsupportedEncodingException {
		String encoding = "UTF-8";
		String toEncode = "This is an encoded String, with comma: Toc!";
		String encoded = URLEncoder.encode(toEncode, encoding);
		System.out.format("Encoded [%s]\n", encoded);
		System.out.println("Back:");
		String backToReadable = URLDecoder.decode(encoded, encoding);
		System.out.format("Decoded [%s]\n", backToReadable);
	}
}
