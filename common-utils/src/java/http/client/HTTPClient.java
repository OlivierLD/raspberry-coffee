package http.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Very limited Java HTTP Client, just suitable for what is
 * required - so far - in this project.
 */
public class HTTPClient {

	private final static boolean DEBUG = "true".equals(System.getProperty("http.client.verbose", "false"));

	public static String doGet(String urlStr, Map<String, String> headers) throws Exception {
		int responseCode = 0;
		String getContent = "";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			for (String h : headers.keySet()) {
				conn.setRequestProperty(h, headers.get(h));
			}
			conn.setUseCaches(false);
			responseCode = conn.getResponseCode();

			if (DEBUG) System.out.println("Done. (" + responseCode + ")");

			InputStream is = conn.getInputStream();
			byte aByte[] = new byte[2];
			int nBytes;

			byte content[] = null;
			int nbLoop = 1;
			long started = System.currentTimeMillis();

			while ((nBytes = is.read(aByte, 0, 1)) != -1) {
				content = appendByte(content, aByte[0]);
				if (content.length > (nbLoop * 1_000)) {
					long now = System.currentTimeMillis();
					long delta = now - started;
					double rate = (double) content.length / ((double) delta / 1_000D);
					if (DEBUG) System.out.println("Downloading at " + rate + " bytes per second.");
					nbLoop++;
				}
			}
			conn.disconnect();
			getContent = new String(content);
		} catch (EOFException eofe) {
			System.out.println("EOFException"); // That's ok, nothing is returned
			eofe.printStackTrace();
		} catch (SocketException se) {
			System.out.println("SocketException"); // OK too.
			se.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return getContent;
	}

	public static int doPost(String urlStr, Map<String, String> headers, String payload) throws Exception {
		int responseCode = 0;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//  String userCredentials = "username:password";
//  String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
//  conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestMethod("POST");
		for (String h : headers.keySet()) {
			conn.setRequestProperty(h, headers.get(h));
		}
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", "" + Integer.toString(payload.getBytes().length));
		// conn.setRequestProperty("Content-Language", "en-US");
		conn.setUseCaches(false);

		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(payload.getBytes());
		os.flush();
		os.close();

		responseCode = conn.getResponseCode();
		return responseCode;
	}

	public static int doCustomVerb(String verb, String urlStr, Map<String, String> headers, String payload) throws Exception {
		int responseCode = 0;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//  String userCredentials = "username:password";
//  String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
//  conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestMethod(verb);
		for (String h : headers.keySet()) {
			conn.setRequestProperty(h, headers.get(h));
		}
		if (payload != null) {
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", "" + Integer.toString(payload.getBytes().length));
			// conn.setRequestProperty("Content-Language", "en-US");
			conn.setUseCaches(false);

			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(payload.getBytes());
			os.flush();
			os.close();
		}

		responseCode = conn.getResponseCode();

		return responseCode;
	}

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
