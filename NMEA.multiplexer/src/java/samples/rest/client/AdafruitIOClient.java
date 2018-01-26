package samples.rest.client;

import http.client.HTTPClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static utils.StaticUtil.userInput;

public class AdafruitIOClient {

	private static String key = "";

	private static String ONOFF_FEED = "onoff";
	private static String AIR_TEMP   = "air-temperature";
	private static String ATM_PRESS  = "atm-press";
	private static String HUMIDITY   = "humidity";
	private static String TWS        = "tws";
	private static String TWD        = "twd";
	private static String PRATE      = "prate";

	// TODO Camera

	private static String FEED_TO_USE = PRATE;

	private boolean DEBUG = true;

	public void setFeedValue(String key, String value) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + FEED_TO_USE + "/data";
		Map<String, String> headers = new HashMap<>(1);
		headers.put("X-AIO-Key", key);
		JSONObject json = new JSONObject();
		json.put("value", new Double(value));
		System.out.println("POSTing " + json.toString(2) + " to " + url);
		HTTPClient.HTTPResponse response = HTTPClient.doPost(url, headers, json.toString());
		if (DEBUG) {
			System.out.println(String.format("POST Ret: %d, %s", response.getCode(), response.getPayload()));
		}
	}

	public AdafruitIOClient() {
	}

	public static void main(String... args) {

		AdafruitIOClient.key = System.getProperty("aio.key", "").trim();

		if (AdafruitIOClient.key.trim().isEmpty()) {
			throw new RuntimeException("Require the key as System variables (-Daio.key)");
		}

		try {
			AdafruitIOClient postFeeder = new AdafruitIOClient();

			System.out.println("Enter value, or Q to exit, and then [Return]");
			boolean go = true;
			while (go) {
				String str = userInput("> ");
				if ("Q".equalsIgnoreCase(str)) {
					go = false;
					System.out.println("Bye.");
				} else {
					String data = str;
					postFeeder.setFeedValue(AdafruitIOClient.key, data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
