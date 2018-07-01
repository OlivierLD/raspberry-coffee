package adafruit.io.sample;

import adafruit.io.rest.HttpClient;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Poll {
	private final static boolean DEBUG = true;
	private final static String FEED_NAME = "air-temperature";

	private static String getFeedValue(String key) throws Exception {
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

		String val = Poll.getFeedValue(key);
		System.out.println("Starting from " + val);
		boolean same = true;
		while (same) {
			System.out.print(".");
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			String newVal = Poll.getFeedValue(key);
			same = newVal.equals(val);
		}
		System.out.println("Yo!");
	}
}
