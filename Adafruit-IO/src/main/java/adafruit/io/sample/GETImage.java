package adafruit.io.sample;

import adafruit.io.rest.HttpClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GETImage {
	private final static boolean DEBUG = false;
	private final static String FEED_NAME = "picture";

	private static String getImageValue(String key) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + FEED_NAME;
		Map<String, String> headers = new HashMap<>(1);
		headers.put("X-AIO-Key", key);
		String content = HttpClient.doGet(url, headers);
		if (DEBUG) {
			System.out.println("GET\n" + content);
		}
		JSONObject json = new JSONObject(content);
		String lastValue = json.getString("last_value");
		if (DEBUG) {
			System.out.println("Feed value:" + lastValue);
		}
		return lastValue;
	}

	public static void main(String... args) throws Exception {
		String key = System.getProperty("key");
		if (key == null) {
			System.out.println("... Provide a key (see doc).");
			System.exit(1);
		}

		String val = GETImage.getImageValue(key);
		System.out.println("Encoded image:" + val);
		System.out.println("Yo!");
	}
}
