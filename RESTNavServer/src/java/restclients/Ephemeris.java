package restclients;

import http.client.HTTPClient;

import java.util.HashMap;

public class Ephemeris {
	public static void main(String... args) {
		// POST http://localhost:9999/astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=America%2FLos_Angeles
		// payload { latitude: 37.76661945, longitude: -122.5166988 }
		HTTPClient client = new HTTPClient();
		try {
			HTTPClient.HTTPResponse response = HTTPClient.doPost(
					"http://localhost:9999/astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=America%2FLos_Angeles",
					new HashMap<String, String>(),
					"{ latitude: 37.76661945, longitude: -122.5166988 }");
			System.out.println("!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
