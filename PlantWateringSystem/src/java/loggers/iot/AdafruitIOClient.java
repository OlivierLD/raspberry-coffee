package loggers.iot;

import com.google.gson.JsonObject;
import http.client.HTTPClient;
import loggers.DataLoggerInterface;
import loggers.LogData;

import java.util.HashMap;
import java.util.Map;

import static utils.StaticUtil.userInput;

/**
 * Requires your Adafruit-IO Key in the System proerty "aio.key" to work properly.
 * Use -Daio.key=AHGSFHGFHGSFHGFS
 */
public class AdafruitIOClient implements DataLoggerInterface {

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
			if (response.getCode() == 403) {
				System.out.println(String.format("Your request to %s returned a %d, are you sure of your key?", url, 403));
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		if (DEBUG) {
			System.out.println(String.format("Closing logger [%s]", this.getClass().getName()));
		}
	}

	public AdafruitIOClient() {
		if (DEBUG) {
			System.out.println(String.format("Creating logger [%s]", this.getClass().getName()));
		}
	}

	// Interactive main, for tests.
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
					String data = str; // Feed Humidity feed, for tests
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
