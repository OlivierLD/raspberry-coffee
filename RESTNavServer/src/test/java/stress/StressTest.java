package stress;

import http.HttpHeaders;
import http.client.HTTPClient;

import java.util.HashMap;
import java.util.Map;

/**
 * requires the REST server to be up and running.
 * See url below.
 * From the launchers folder, run ./demoLauncher.sh --option:1
 */
public class StressTest {

	private static String url = "http://localhost:9999/astro/sun-now";

	private static boolean go = true;
	private static String payload = "{ position: { latitude: 38, longitude: -122 }}"; // Payload

	public static void main(String... args) throws Exception {

		for (int i=0; i<10; i++) {
			Thread thread = new Thread(String.format("Thread-%d", i)) {

				public void run() {
					Map<String, String> headers = new HashMap<>();
					headers.put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
					while (go) {
						try {
							System.out.println(String.format("%s >> %s", this.getName(), HTTPClient.doPost(url, headers, payload).getPayload()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			thread.start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Stop requested");
			go = false;
		}, "Shutdown Hook"));
	}
}
