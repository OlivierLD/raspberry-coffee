package adafruit.io.rest;

import java.io.EOFException;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Shows how to interact with Adafruit-IO using a REST interface.
 * See doc at https://io.adafruit.com/api/docs/#!/Feeds/all
 */
public class HttpClient {
	private final static boolean DEBUG = "true".equals(System.getProperty("rest.debug"));

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

			if (DEBUG) {
				System.out.println("Done. (" + responseCode + ")");
			}

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

		// response payload ?
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
		String postContent = new String(content);
		if (DEBUG) {
			System.out.println(String.format("POST returns %s", postContent));
		}
		return responseCode;
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		synchronized (is) {
			synchronized (os) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = is.read(buffer);
					if (bytesRead == -1)
						break;
					os.write(buffer, 0, bytesRead);
				}
			}
		}
	}

	public static byte[] appendByte(byte c[], byte b) {
		int newLength = c != null ? c.length + 1 : 1;
		byte newContent[] = new byte[newLength];
		for (int i = 0; i < newLength - 1; i++)
			newContent[i] = c[i];

		newContent[newLength - 1] = b;
		return newContent;
	}

	private static void help() {
		String help = "Please provide your Adafruit IO key using -Dkey=50c0707070c070302030a01040d020a0a0908050\n" +
						"You need to have created a feed, named 'onoff', as a switch.";

		System.out.println(help);
	}

	/**
	 * Sample. Reads and feeds a toggle button named "onoff", with values "ON" or "OFF".
	 * The Adafruit-IO key is to be provided as a system variable.
	 *
	 * @param args unused
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		String key = System.getProperty("key");
		if (key == null) {
			HttpClient.help();
			System.exit(1);
		}
		String feedName = "onoff";

		String url = "https://io.adafruit.com/api/feeds/" + feedName;
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-AIO-Key", key);
		String content = HttpClient.doGet(url, headers);
		if (DEBUG) System.out.println("GET\n" + content);
		JSONObject json = new JSONObject(content);
		String lastValue = json.getString("last_value");
		System.out.println("Feed value:" + lastValue);

		url += "/data";
		JSONObject off = new JSONObject();
		off.put("value", lastValue.equals("ON") ? "OFF" : "ON");
		System.out.println("POSTing " + off.toString(2) + " to " + url);
		int httpCode = HttpClient.doPost(url, headers, off.toString());
		System.out.println("POST Ret:" + httpCode);

		System.out.println("Reading temperature");
		feedName = "air-temperature";

		url = "https://io.adafruit.com/api/feeds/" + feedName;
		content = HttpClient.doGet(url, headers);
		System.out.println("Read OK");
		if (DEBUG) System.out.println("GET\n" + content);
		JSONObject tempData = new JSONObject(content);
		System.out.println(tempData.toString(2));
		System.out.println("Last temp:" + tempData.getDouble("last_value") + "\272C");

		try {
			url = "https://io.adafruit.com/api/feeds/" + feedName + "/data";
			content = HttpClient.doGet(url, headers);
			System.out.println("Read OK");
			if (DEBUG) System.out.println("GET\n" + content);
			JSONArray data = new JSONArray(content);
			System.out.println(data.length() + " elements in the feed");
			for (int i = 0; i < data.length(); i++) {
				JSONObject js = data.getJSONObject(i);
				System.out.println("Value:" + js.getDouble("value") + "\272C at " + js.getString("updated_at"));
			}
			// Updating
			JSONObject newTemp = new JSONObject();
			newTemp.put("value", tempData.getDouble("last_value") + 0.1);
			System.out.println("POSTing " + newTemp.toString(2) + " to " + url);
			httpCode = HttpClient.doPost(url, headers, newTemp.toString());
			System.out.println("POST Ret:" + httpCode);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
