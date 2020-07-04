package weatherstation;

import adafruit.io.rest.HttpClient;
import http.HttpHeaders;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class POSTImage {
	private final static boolean DEBUG = "true".equals(System.getProperty("base64.verbose", "false"));
	private final static String FEED_NAME = "picture";

	private static int postImage(String key, String base64) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + FEED_NAME + "/data";
		Map<String, String> headers = new HashMap<>(2);
		headers.put("X-AIO-Key", key);
		headers.put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
		JSONObject json = new JSONObject();
		json.put("value", base64);
		String imgPayload = json.toString();
		int ret = HttpClient.doPost(url, headers, imgPayload);
		if (DEBUG) System.out.println("POST: " + ret);
		return ret;
	}

	public static void main(String... args) throws Exception {
		String key = System.getProperty("key");
		if (key == null) {
			throw new IllegalArgumentException("Need the Adafruit key as System Variable -Dkey");
		}
		if (args.length != 1) {
			throw new IllegalArgumentException("Need the encode image file name as parameter");
		}
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					sb.append(line);
				}
			}
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String img = sb.toString();

		if (DEBUG) {
			System.out.println(img);
		}
		if (key == null) {
			System.out.println("... Provide a key (see doc).");
			System.exit(1);
		}

		int val = POSTImage.postImage(key, img);
		System.out.println(String.format("Ret Code: %d", val));
		System.out.println("Yo!");
	}
}
