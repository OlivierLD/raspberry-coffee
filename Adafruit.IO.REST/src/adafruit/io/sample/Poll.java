package adafruit.io.sample;

import adafruit.io.rest.HttpClient;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Poll {
	private final static boolean DEBUG = false;
	private final static String FEED_NAME = "onoff";

	private static String getOnOffValue(String key) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + FEED_NAME;
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-AIO-Key", key);
		String content = HttpClient.doGet(url, headers);
		if (DEBUG)
			System.out.println("GET\n" + content);
		JSONObject json = new JSONObject(content);
		String lastValue = json.getString("last_value");
		if (DEBUG)
			System.out.println("Feed value:" + lastValue);
		return lastValue;
	}

	@SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
	public static void main(@SuppressWarnings("unused") String[] args) throws Exception {
		String key = System.getProperty("key");
		if (key == null) {
			System.out.println("... Provide a key (see doc).");
			System.exit(1);
		}

		String val = Poll.getOnOffValue(key);
		System.out.println("Starting from " + val);
		boolean same = true;
		while (same) {
			System.out.print(".");
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
			String newVal = Poll.getOnOffValue(key);
			same = newVal.equals(val);
		}
		System.out.println("Yo!");
	}
}
