package restclients;

import calc.GeomUtil;
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
	private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MMM-dd");
	private final static SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");

	// Get the station position:
	// GET http://localhost:9999/tide/tide-stations/Ocean%20Beach%2C%20California

	// Sun Data:
	// POST http://localhost:9999/astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=America%2FLos_Angeles
	// payload { latitude: 37.76661945, longitude: -122.5166988 }

	// Tide Table:
	// POST http://localhost:9999/tide/tide-stations/Ocean%20Beach%2C%20California/wh?from=2018-01-03T00:00:00&to=2018-01-04T00:00:01

	private final static EmailSender sender = new EmailSender("google");
	private static boolean keepLooping = true;

	private static boolean keepLooping() {
		return keepLooping;
	}

	public static void main(String... args) throws Exception {

		Thread loop = new Thread(() -> {
			while (keepLooping()) {
				try {
					go();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			System.out.println("Stopped");
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			keepLooping = false;
			System.out.println("Stopping.");
			try {
				Thread.sleep(2_000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}, "Shutdown Hook"));

		loop.start();
	}

	/**
	 * TODO Isolate prms in a properties file.
\	 */
	private final static void go() throws Exception {

		String serverName = "localhost", serverPort = "9999";

		String stationName = "Ocean Beach, California";
		String url = "";

		StringBuffer messageContent = new StringBuffer();

		double lat = 0d, lng = 0d;

		messageContent.append("<!DOCTYPE html><html><body>");
		messageContent.append(String.format("<h2>%s</h2>", stationName));

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

				messageContent.append(String.format(
						"<table style='font-family: Verdana;'><tr><td style='text-align: right;'>%s</td></tr><tr><td style='text-align: right;'>%s</td></tr></table>",
						GeomUtil.decToSex(lat, GeomUtil.HTML, GeomUtil.NS, GeomUtil.TRAILING_SIGN),
						GeomUtil.decToSex(lng, GeomUtil.HTML, GeomUtil.EW, GeomUtil.TRAILING_SIGN)));

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

		// Date
		messageContent.append("<hr/>");
		messageContent.append(String.format("<p style='margin: 10px;'><span style='font-family: Verdana;'>%s</span></p>", DATE_FMT.format(now.getTime())));
		messageContent.append("<hr/>");

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
				messageContent.append("<table>");
				for (String epoch : map.keySet()) {
					LinkedTreeMap linkedTreeMap = map.get(epoch);
					Date date = new Date(Long.parseLong(epoch));
//				System.out.println("Date > " + date.toString());
					double riseTime = (double)linkedTreeMap.get("riseTime");
					Date riseTimeDate = new Date(Math.round(riseTime));

					double setTime = (double)linkedTreeMap.get("setTime");
					Date setTimeDate = new Date(Math.round(setTime));

					double riseZ = (double)linkedTreeMap.get("riseZ");
					double setZ = (double)linkedTreeMap.get("setZ");

					messageContent.append(String.format("<tr><td>Sun Rise:</td><td>%s</td><td> - Z:</td><td>%d&deg;</td></tr>", TIME_FMT.format(riseTimeDate), Math.round(riseZ)));
					messageContent.append(String.format("<tr><td>Sun Set:</td><td>%s</td><td> - Z:</td><td>%d&deg;</td></tr>", TIME_FMT.format(setTimeDate), Math.round(setZ)));
					break;
				}
				messageContent.append("</table>");
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
		messageContent.append("<hr/>");
		try {
			HTTPClient.HTTPResponse response = HTTPClient.doPost(
					url,
					new HashMap<>(),
					"");
			// Parse payload
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(response.getPayload());
			messageContent.append("<pre>");
			try {
				LinkedTreeMap map = (LinkedTreeMap)gson.fromJson(stringReader, Map.class);
				List<LinkedTreeMap> table = (List<LinkedTreeMap>)map.get("table");
				messageContent.append("<table>");
				for (LinkedTreeMap tideEvent : table) {
					String type = (String)tideEvent.get("type");
					double value = (double)tideEvent.get("value");
					String unit = (String)tideEvent.get("unit");
					String fmtDate = (String)tideEvent.get("formattedDate");

					String line = String.format("<tr><td>%s</td><td style='text-align: right;'>%.02f %s</td><td> - </td><td>%s</td></tr>", type, value, unit, fmtDate);
					messageContent.append(line);
				}
				messageContent.append("</table>");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			messageContent.append("</pre>");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		messageContent.append("<hr/><i>by OlivSoft</i>");

		messageContent.append("</body></html>");
		String content = messageContent.toString();

		// 4 - Send email
//	final EmailSender sender = new EmailSender("google");
		String[] toEmails = { "olivier@lediouris.net", "olivier.lediouris@gmail.com" };
		sender.send(
				toEmails,
				"Ephemeris for " + DATE_FMT.format(now.getTime()),
				content,
				"text/html;charset=utf-8");
	}
}
