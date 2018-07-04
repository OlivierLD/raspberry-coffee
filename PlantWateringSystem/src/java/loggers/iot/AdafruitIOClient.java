package loggers.iot;

import com.google.gson.JsonObject;
import http.client.HTTPClient;
import loggers.LogData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static utils.StaticUtil.userInput;

public class AdafruitIOClient implements Consumer<LogData> {

	private static String key = System.getProperty("aio.key", "");

	private static boolean DEBUG = "true".equals(System.getProperty("aio.verbose"));

	@Override
	public void accept(LogData feedData) {
		String url = "http://io.adafruit.com/api/feeds/" + feedData.feed().value() + "/data";
		Map<String, String> headers = new HashMap<>(1);
		headers.put("X-AIO-Key", AdafruitIOClient.key.trim()); // System property
		JsonObject json = new JsonObject();
		json.addProperty("value", feedData.value());

		if (DEBUG) {
			System.out.println("POSTing " + json.toString() + " to " + url);
		}
		try {
			HTTPClient.HTTPResponse response = HTTPClient.doPost(url, headers, json.toString());
			if (DEBUG) {
				System.out.println(String.format("POST Ret: %d, %s", response.getCode(), response.getPayload()));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
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
					LogData feedData = new LogData()
							.feed(LogData.FEEDS.HUM)
							.value(Double.parseDouble(data));
					postFeeder.accept(feedData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
