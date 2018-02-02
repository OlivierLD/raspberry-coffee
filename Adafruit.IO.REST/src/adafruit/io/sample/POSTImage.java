package adafruit.io.sample;

import adafruit.io.rest.HttpClient;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class POSTImage {
	private final static boolean DEBUG = true;
	private final static String FEED_NAME = "picture";

	private static int postImage(String key, String base64) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + FEED_NAME + "/data";
		Map<String, String> headers = new HashMap<>(2);
		headers.put("X-AIO-Key", key);
		headers.put("Content-Type", "application/json");
		JSONObject json = new JSONObject();
		json.put("value", base64);
		String imgPayload = json.toString();
		int ret = HttpClient.doPost(url, headers, imgPayload);
		if (DEBUG) System.out.println("POST: " + ret);
		return ret;
	}

	public static void main(String... args) throws Exception {
		String key = System.getProperty("key");
		String img =
				"/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a" +
						"HBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIy" +
						"MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCABxAFoDASIA" +
						"AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA" +
						"AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3" +
						"ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm" +
						"p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA" +
						"AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx" +
						"BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK" +
						"U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3" +
						"uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwC6RTCK" +
						"lIphFMojIpMVJim0AQsKhYVZeoWH+fSkBWIqKQVOwqKQUgKjiq7irMlV3FIZXcVDipmqLNAjuyKb" +
						"ipDUb5VC2CxHQDqx7AVYhuKTA5JOABkk9BUuh/Ztcf7Kt3Hbakc4s5xtZsf3T/F+FeeeLNeu5NQu" +
						"dK2eRBC/lyp/E7A9z6cdvSk2M2NV8W2Vt+7tj58pPG0cf5+lSaVB4m1hI50sZEicEIZAAH+n0xUX" +
						"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" +
						"7x/nRRQB/9k=";
		if (key == null) {
			System.out.println("... Provide a key (see doc).");
			System.exit(1);
		}

		int val = POSTImage.postImage(key, img);
		System.out.println(String.format("Ret Code: %d", val));
		System.out.println("Yo!");
	}
}
