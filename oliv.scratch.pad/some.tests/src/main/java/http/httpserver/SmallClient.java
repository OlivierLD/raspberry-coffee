package http.httpserver;

import java.io.EOFException;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;


public class SmallClient {
	public static void main(String... args) throws Exception {
		int responseCode = 0;
		try {
			URL url = new URL("http://localhost:9999/device-access?dev=01&status=off");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			responseCode = conn.getResponseCode();
			System.out.println("Done. (" + responseCode + ")");
		} catch (EOFException eofe) {
			System.out.println("EOFException"); // That's ok, nothing is returned
		} catch (SocketException se) {
			System.out.println("SocketException"); // OK too.
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Response Code:" + responseCode);
	}
}
