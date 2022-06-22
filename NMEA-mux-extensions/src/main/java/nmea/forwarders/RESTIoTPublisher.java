package nmea.forwarders;

import http.client.HTTPClient;
import http.HttpHeaders;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This is a {@link Forwarder}, forwarding chosen data to a REST server (POST).
 * In this case this is Adafruit.IO
 * <br>
 *   Data are (can be)
 *   - Air Temperature
 *   - Atmospheric pressure
 *   - Humidity
 *   - Wind Speed
 *   - Wind direction
 *   - Rain (precipitation rate)
 *   - Dew point temperature
 * <br>
 * It must be loaded dynamically. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Mux Web UI.
 * The REST api (of the /mux resource) is not aware of it.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.RESTIoTPublisher
 *   forward.XX.properties=rest.server.properties
 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 */
public class RESTIoTPublisher implements Forwarder {

	private static final String AIR_TEMP   = "air-temperature";
	private static final String ATM_PRESS  = "atm-press";
	private static final String HUMIDITY   = "humidity";
	private static final String TWS        = "tws";
	private static final String TWD        = "twd";
	private static final String PRATE      = "prate";
	private static final String DEWPOINT   = "dewpoint";
	private static final String TIME       = "time";     // Can be used to evaluate lifespan, on battery.

	private Properties properties;

	private final static long ONE_MINUTE = 60 * 1_000;

	private static long pushInterval = ONE_MINUTE;

	private long previousTempLog = 0;
	private long previousDewLog = 0;
	private long previousHumLog = 0;
	private long previousPressLog = 0;
	private long previousPRateLog = 0;
	private long previousTWSLog = 0;
	private long previousTWDLog = 0;
	private long previousTimeLog = 0;

	private final List<Pattern> feedPatterns = new ArrayList<>();

	/*
	 * @throws Exception
	 */
	public RESTIoTPublisher() throws Exception {
//		System.out.println(String.format(">> Instantiating %s", this.getClass().getName()));
	}

	private boolean goesThroughFilter(String feedName) {
		if (feedPatterns.isEmpty()) {
			String filters = this.properties.getProperty("feed.filter", ".*");
			for (String filter : filters.split(",")) {
				if ("true".equals(this.properties.getProperty("aio.verbose.1"))) {
					System.out.printf("- Managing pattern for [%s]\n", filter.trim());
				}
				Pattern pattern = Pattern.compile(filter.trim());
				feedPatterns.add(pattern);
			}
		}
		Optional<Pattern> firstMatch = feedPatterns.stream()
				.filter(pattern -> pattern.matcher(feedName).matches())
				.findFirst();
		return firstMatch.isPresent();
	}

	private void setFeedValue(String key, String baseUrl, String userName, String feed, String value) throws Exception {
		if (goesThroughFilter(feed)) {
			if ("true".equals(this.properties.getProperty("aio.verbose.1"))) {
				System.out.printf("\t>>> Feed Name [%s] will be logged\n", feed);
			}
			String url = String.format("%s/api/v2/%s/feeds/%s/data",
					baseUrl,
					userName,
					feed);
			Map<String, String> headers = new HashMap<>(1);
			headers.put("X-AIO-Key", key);
			headers.put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			json.put("value", Double.valueOf(value));
			if ("true".equals(this.properties.getProperty("aio.verbose.1"))) {
				System.out.printf("URL:%s, key:%s\n", url, key);
				System.out.printf("->->-> POSTing to feed [%s]: %s to %s\n", feed, json.toString(2), url);
				System.out.println("Headers:");
				headers.forEach((a, b) -> {
					System.out.printf("%s: %s\n", a, b);
				});
			}
			if ("true".equals(this.properties.getProperty("aio.push.to.server", "true"))) {
				HTTPClient.HTTPResponse response = HTTPClient.doPost(url, headers, json.toString());
				if (response.getCode() > 299 || "true".equals(this.properties.getProperty("aio.verbose.1"))) {
					System.out.printf("POST Ret: %d, %s\n", response.getCode(), response.getPayload());
				}
			}
		} else if ("true".equals(this.properties.getProperty("aio.verbose.1"))) {
			System.out.printf("\t>>> Feed Name [%s] is filtered (prevented)\n", feed);
		}
	}

	private void logTime(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousTempLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						TIME,
						String.valueOf(value));
				previousTimeLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logAirTemp(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousTempLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						AIR_TEMP,
						String.valueOf(value));
				previousTempLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logDewTemp(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousDewLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						DEWPOINT,
						String.valueOf(value));
				previousDewLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logHumidity(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousHumLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						HUMIDITY,
						String.valueOf(value));
				previousHumLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logPressure(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousPressLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						ATM_PRESS,
						String.valueOf(value));
				previousPressLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logPRate(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousPRateLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						PRATE,
						String.valueOf(value));
				previousPRateLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logTWS(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousTWSLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						TWS,
						String.valueOf(value));
				previousTWSLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void logTWD(double value) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - previousTWDLog) > pushInterval) {
			try {
				setFeedValue(this.properties.getProperty("aio.key"),
						this.properties.getProperty("aio.url"),
						this.properties.getProperty("aio.user.name"),
						TWD,
						String.valueOf(value));
				previousTWDLog = now;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);

		// Log time. Not based on a specific sentence id.
		long now = System.currentTimeMillis();
		logTime(now);

		if (StringParsers.validCheckSum(str)) {
//		String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			if ("true".equals(this.properties.getProperty("aio.verbose.2"))) {
				System.out.printf("\t->->-> From NMEA data [%s]\n", str.trim());
			}
			if ("MDA".equals(sentenceId)) {
				StringParsers.MDA mda = StringParsers.parseMDA(str);
//				System.out.println(String.format(
//						"MDA >> Hum: %.02f%%, AirT: %.02f\272, PRMSL: %.02f mB, TWD: %.02f\272, TWS: %.02f kts",
//						mda.relHum,
//						mda.airT,
//						mda.pressBar * 1_000,
//						mda.windDirM,
//						mda.windSpeedK));
				if (mda.relHum != null) {
					logHumidity(mda.relHum);
				}
				if (mda.airT != null) {
					logAirTemp(mda.airT);
				}
				if (mda.dewC != null) {
					logDewTemp(mda.dewC);
				}
				if (mda.pressBar != null) {
					logPressure(mda.pressBar * 1_000);
				}
				if (mda.windDirT != null) {
					logTWD(mda.windDirT);
				}
				if (mda.windSpeedK != null) {
					logTWS(mda.windSpeedK);
				}
			} else if ("XDR".equals(sentenceId)) {
				List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(str);
				xdrElements.stream().forEach(xdr -> {
//				System.out.println(String.format("XDR: %s -> %s", xdr.getTypeNunit(), xdr.toString()));
					if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.TEMPERATURE)) {
						logAirTemp(xdr.getValue());
					} else if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.HUMIDITY)) {
						logHumidity(xdr.getValue());
					} else if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.PRESSURE_P)) {
						logPressure(xdr.getValue() / 100);
					} else if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.GENERIC)) { // Consider it as prate...
						logPRate(xdr.getValue());
					}
				});
			} else {  // Other sentences ignored (like GLL)
				if ("true".equals(this.properties.getProperty("aio.verbose.2"))) {
					System.out.println("\tNothing to log (REST)");
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing (REST-> Adafruit-IO) to " + this.getClass().getName());
	}

	public static class RESTBean {
		private final String cls;
		private final String type = "REST-forwarder";

		public RESTBean(RESTIoTPublisher instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new RESTBean(this);
	}

	@Override
	public void setProperties(Properties props) {

		this.properties = props;

//		for (Object obj : this.properties.keySet()) {
//			String property = this.properties.getProperty(obj.toString());
//			System.out.println(String.format("%s = %s", obj.toString(), property));
//		}

		try {
			pushInterval = 1_000L * Long.parseLong(this.properties.getProperty("aio.push.interval", "60"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
