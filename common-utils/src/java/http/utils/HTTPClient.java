package http.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Very limited HTTP Client, just suitable for what is
 * required - so far - in this project.
 */
public class HTTPClient {
	public static String getContent(String url) throws Exception {
		String ret = null;
		try {
			byte content[] = readURL(new URL(url));
			ret = new String(content);
		} catch (Exception e) {
			throw e;
		}
		return ret;
	}

	private static byte[] readURL(URL url) throws Exception {
		byte content[] = null;
		try {
			URLConnection newURLConn = url.openConnection();
			InputStream is = newURLConn.getInputStream();
			byte aByte[] = new byte[2];
			int nBytes;
			long started = System.currentTimeMillis();
			int nbLoop = 1;
			while ((nBytes = is.read(aByte, 0, 1)) != -1) {
				content = appendByte(content, aByte[0]);
				if (content.length > (nbLoop * 1_000)) {
					long now = System.currentTimeMillis();
					long delta = now - started;
					double rate = (double) content.length / ((double) delta / 1_000D);
					System.out.println("Downloading at " + rate + " bytes per second.");
					nbLoop++;
				}
			}
		} catch (IOException e) {
			System.err.println("ReadURL for " + url.toString() + "\nnewURLConn failed :\n" + e);
			throw e;
		} catch (Exception e) {
			System.err.println("Exception for: " + url.toString());
		}
		return content;
	}

	public static byte[] appendByte(byte c[], byte b) {
		int newLength = c != null ? c.length + 1 : 1;
		byte newContent[] = new byte[newLength];
		for (int i = 0; i < newLength - 1; i++)
			newContent[i] = c[i];

		newContent[newLength - 1] = b;
		return newContent;
	}

}
