package weatherstation.logger;

import http.client.HTTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * REST Interface to MySQL
 * It is actually not dealing with MySQL, but with REST.
 * See the RESTG_URL variable below in then code.
 *
 * JSON payload looks like:
 * <p>
 * { "dir": 350.0,
 *   "volts": 3.4567,
 *   "speed": 12.345,
 *   "gust": 13.456,
 *   "rain": 0.1,
 *   "press": 101300.00,
 *   "temp": 18.34,
 *   "hum": 58.5,
 *   "dew": 34.56 }
 * <p>
 * The DB will take care of a timestamp.
 *
 * System variables:
 * mysql.logger.verbose default false
 * ws.rest.url default http://donpedro.lediouris.net/php/raspi/insert.wd.php
 * ws.between.logs default 5000
 */
public class MySQLLoggerImpl implements LoggerInterface {
	private long lastLogged = 0L; // Time of the last logging
	private static final long MINIMUM_BETWEEN_LOGS = 5_000L; // A System variable, in ms. "ws.between.logs"
	private final static NumberFormat DOUBLE_FMT = new DecimalFormat("#0.000");
	private final static String REST_URL = "http://donpedro.lediouris.net/php/raspi/insert.wd.php"; // Overridden by a System variable, "ws.rest.url"

	private String restURL = "";
	private long betweenLogs = MINIMUM_BETWEEN_LOGS;

	public MySQLLoggerImpl() {
		restURL = System.getProperty("ws.rest.url", REST_URL);
		betweenLogs = Long.parseLong(System.getProperty("ws.between.logs", Long.toString(betweenLogs)));
	}

	private String json2qs(JSONObject json, String jMember, String qsName) {
		String ret = null;
		try {
			Object o = json.get(jMember);
			if (o != null) {
				if (o instanceof Double) {
					double d = ((Double) o).doubleValue();
					ret = qsName + "=" + DOUBLE_FMT.format(d);
				} else if (o instanceof Float) {
					float f = ((Float) o).floatValue();
					ret = qsName + "=" + DOUBLE_FMT.format(f);
				} else if (o instanceof Integer) {
					int i = ((Integer) o).intValue();
					ret = qsName + "=" + DOUBLE_FMT.format(i);
				} else if (o instanceof Long) {
					long l = ((Long) o).longValue();
					ret = qsName + "=" + DOUBLE_FMT.format(l);
				} else
					System.out.println(">>> Un-managed type: Got a " + o.getClass().getName());
			} else
				System.out.println("No " + jMember);
		} catch (JSONException je) { /* Not there */ }
		return ret;
	}

	/**
	 * Produces a string like
	 * WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&DEW=34.56
	 */
	private String composeQS(JSONObject json) {

		if ("true".equals(System.getProperty("mysql.logger.verbose", "false"))) {
			System.out.println(String.format("MySQL Logger received [%s]", json.toString(2)));
		}

		String qs = "";
		String s = json2qs(json, "avgdir", "WDIR");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		} else {
			s = json2qs(json, "dir", "WDIR");
			if (s != null) {
				qs += ((qs.trim().isEmpty() ? "" : "&") + s);
			}
		}
		s = json2qs(json, "speed", "WSPEED");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "gust", "WGUST");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "rain", "RAIN");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "press", "PRMSL");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "temp", "ATEMP");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "hum", "HUM");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		s = json2qs(json, "dew", "DEW");
		if (s != null) {
			qs += ((qs.trim().isEmpty() ? "" : "&") + s);
		}
		return qs;
	}

	private double maxGust = 0D;
	// TODO Same pattern for all data, smooth speeds, angles,...

	@Override
	public void pushMessage(JSONObject json )
			throws Exception {
		long now = System.currentTimeMillis();
		// Record MaxGust here
		this.maxGust = Math.max(this.maxGust, json.getDouble("gust"));
		if (now - this.lastLogged > betweenLogs) {
			json.put("gust", maxGust); // Replace with maxGust
			// Reset MaxGust here
			this.maxGust = 0D;
//    System.out.print(" >>> Logging... ");
			String queryString = composeQS(json);
			this.lastLogged = now;
			/*
			 * Actual logging goes here.
			 * And yes, it is an insert, done through a GET. Limitation on Yahoo!'s php.
			 * URL would be like
			 * http://donpedro.lediouris.net/php/raspi/insert.wd.php?WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&DEW=34.56
			 */
			if ("true".equals(System.getProperty("mysql.logger.verbose", "false"))) {
				System.out.println("REST Request:" + restURL + "?" + queryString);
			}
			String response = HTTPClient.getContent(restURL + "?" + queryString);
			json = new JSONObject(response);
			if ("true".equals(System.getProperty("mysql.logger.verbose", "false"))) {
				System.out.println("Returned\n" + json.toString(2));
			}
		}
	}
	@Override
	public void close() {
		System.out.println("(MySQLLogger) Bye!");
	}
}
