package restclients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import email.EmailSender;
import http.client.HTTPClient;

import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ephemeris {

	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final static SimpleDateFormat TZ_ABR = new SimpleDateFormat("z");
	private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
	private final static SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");

	// Get the station position:
	// GET http://localhost:9999/tide/tide-stations/Ocean%20Beach%2C%20California

	// Sun Data:
	// POST http://localhost:9999/astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=America%2FLos_Angeles
	// payload { latitude: 37.76661945, longitude: -122.5166988 }

	// Tide Table:
	// POST http://localhost:9999/tide/tide-stations/Ocean%20Beach%2C%20California/wh?from=2018-01-03T00:00:00&to=2018-01-04T00:00:01


	/**
	 * Requires the following parameters:
	 *
	 * - Server name and port
	 * - Service resource path
	 * - from, to, TZ
	 * - latitude and longitude (From tide station)
	 *
	 * @param args
	 */
	public static void main(String... args) throws Exception {

		String serverName = "localhost";
		String serverPort = "9999";

		String stationName = "Ocean Beach, California";
		String url = "";

		double lat = 0d, lng = 0d;

		// 1 - Get station position
		String tideResourcePath = String.format("tide/tide-stations/%s", URLEncoder.encode(stationName, "UTF-8").replace("+", "%20"));
		url = String.format("http://%s:%s/%s", serverName, serverPort, tideResourcePath);
		try {
			String response = HTTPClient.doGet(url, new HashMap<>());
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(response);
			try {
				List<LinkedTreeMap> list = gson.fromJson(stringReader, List.class);
				if (list.size()  > 1) {
					// Weird. Honk.
				}
				lat = (double)list.get(0).get("latitude");
				lng = (double)list.get(0).get("longitude");

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 2 - Get the Sun data (rise and set)
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0); // Today at 00:00:00

		long today = now.getTimeInMillis();
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTimeInMillis(today + (24 * 3_600_000) + 1_000); // Tomorrow at 00:00:01

		String astroResourcePath = "astro/sun-between-dates";
		String from = DURATION_FMT.format(now.getTime());      // "2017-09-01T00:00:00";
		String to   = DURATION_FMT.format(tomorrow.getTime()); // "2017-09-02T00:00:01";
		String tz = URLEncoder.encode("America/Los_Angeles", "UTF-8");
		String latitude = String.valueOf(lat);
		String longitude = String.valueOf(lng);

		url = String.format("http://%s:%s/%s?from=%s&to=%s&tz=%s", serverName, serverPort, astroResourcePath, from, to, tz);
		String payload = String.format("{ latitude: %s, longitude: %s }", latitude, longitude);

		try {
			HTTPClient.HTTPResponse response = HTTPClient.doPost(
					url,
					new HashMap<>(),
					payload);
//		System.out.println(String.format("Code:%d\nPayload=%s", response.getCode(), response.getPayload()));
			// Parse payload
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(response.getPayload());
			try {
				Map<String, LinkedTreeMap> map = gson.fromJson(stringReader, Map.class);
				for (String epoch : map.keySet()) {
					LinkedTreeMap linkedTreeMap = map.get(epoch);
					Date date = new Date(Long.parseLong(epoch));
					System.out.println("Date > " + date.toString());
					double riseTime = (double)linkedTreeMap.get("riseTime");
					Date riseTimeDate = new Date(Math.round(riseTime));
					System.out.println("Sun Rise > " + riseTimeDate.toString());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 3 - Get the tide data
		// POST http://localhost:9999/tide/tide-stations/Ocean%20Beach%2C%20California/wh?from=2018-01-03T00:00:00&to=2018-01-04T00:00:01
		String tideResourcePath2 = String.format(
				"tide/tide-stations/%s/wh?from=%s&to=%s",
				URLEncoder.encode(stationName, "UTF-8").replace("+", "%20"),
				from,
				to);
		url = String.format("http://%s:%s/%s", serverName, serverPort, tideResourcePath2);
		try {
			HTTPClient.HTTPResponse response = HTTPClient.doPost(
					url,
					new HashMap<>(),
					"");
			// Parse payload
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(response.getPayload());
			try {
				LinkedTreeMap map = (LinkedTreeMap)gson.fromJson(stringReader, Map.class);
				List<LinkedTreeMap> table = (List<LinkedTreeMap>)map.get("table");
				for (LinkedTreeMap tideEvent : table) {
					String type = (String)tideEvent.get("type");
					double value = (double)tideEvent.get("value");
					String unit = (String)tideEvent.get("unit");
					String fmtDate = (String)tideEvent.get("formattedDate");

					String line = String.format("%s: %.02f %s, %s", type, value, unit, fmtDate);
					System.out.println(line);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 4 - Compose and send email
		final EmailSender sender = new EmailSender("google");
		String[] emails = { "olivier@lediouris.net", "olivier.lediouris@gmail.com" };
		sender.send(emails,
				"Ephemeris",
				"<html><body><h1>HTML Content goes here</h1></body></html>",
				"text/html;charset=utf-8");
	}
}
