package adafruit.io.sample;

import adafruit.io.rest.HttpClient;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import http.HttpHeaders;

import static adafruit.io.Base64Util.encodeToString;

/**
 * Have the PI Camera taking snapshots in a loop, like
 * <pre>
 *   while :
 *   do
 *     raspistill -rot 180 --width 200 --height 150 --output snap.jpg --nopreview
 *     sleep 10
 *   done
 * </pre>
 * The code below reads the image at snap.jpg, and posts it on Adafruit.IO
 * See in the <code>web</code> directory, <code>image.html</code> for how to read it.
 */
public class POSTSnapshot {
	private final static boolean DEBUG = true;
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
		if (DEBUG) {
			System.out.println("POST: " + ret);
		}
		return ret;
	}

	private static String IMG_PATH = "./snap.jpg";
	private static boolean keepLooping = true;

	private static void setLoop(boolean b) {
		keepLooping = b;
	}

	private static boolean getLoop() {
		return keepLooping;
	}

	public static void main(String... args) throws Exception {
		String key = System.getProperty("key");
		if (key == null) {
			System.out.println("... Provide a key (see doc).");
			System.exit(1);
		}
		System.out.println("Ctrl + C to stop.");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> setLoop(false), "Shutdown Hook"));

		while (getLoop()) {
			try {
				BufferedImage img = ImageIO.read(new File(IMG_PATH));
				String imgstr = encodeToString(img, "jpg");
				System.out.println(imgstr);
				int val = POSTSnapshot.postImage(key, imgstr);
				System.out.println(String.format("Ret Code: %d", val));
			} catch (IOException ioe) {
				String where = new File(".").getAbsolutePath();
				System.err.println("From " + where);
				ioe.printStackTrace();
			}
			try {
				Thread.sleep(10_000);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
