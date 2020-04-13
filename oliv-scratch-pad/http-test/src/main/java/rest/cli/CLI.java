package rest.cli;

import org.json.JSONObject;
import http.client.HTTPClient;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.util.HashMap;

public class CLI {

	private static String serverName = "localhost";
	private static String serverPort = "9990";

	private final static boolean DEBUG = true;

	public static void main(String... args) {

		String url = String.format("http://%s:%s/%s", serverName, serverPort, "agent/status");
		try {
			String response = HTTPClient.doGet(url, new HashMap<>());
			// 200

			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(response);
				if (DEBUG) {
					System.out.println(String.format("JSON: %s", jsonObject.toString(2)));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println(String.format(">> %s", response));

		} catch (ConnectException ce) { // 500
			ce.printStackTrace();
		} catch (FileNotFoundException fnfe) { // 400, 404

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
