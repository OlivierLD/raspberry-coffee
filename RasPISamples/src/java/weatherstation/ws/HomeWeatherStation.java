package weatherstation.ws;

import org.json.JSONObject;
import weatherstation.SDLWeather80422;
import weatherstation.http.HTTPServer;
import weatherstation.logger.LoggerInterface;

import java.nio.channels.NotYetConnectedException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The real project
 * <p>
 * Loops every 1000 ms, reads data from the SDL 80422:
 * - Wind:
 * - Speed (in km/h)
 * - Direction
 * - Gust (in km/h)
 * - Volts
 * - Rain (in mm)
 * - BMP180: (if available)
 * - Temperature (in Celcius)
 * - Pressure (in Pa)
 * - HTU21DF: (if available)
 * - Relative Humidity (%)
 * - CPU Temperature (in Celcius)
 * <p>
 * if -Dws.log=true
 * Feeds a WebSocket server with a json object like
 * { "dir": 350.0,
 * "avgdir": 345.67,
 * "volts": 3.4567,
 * "speed": 12.345,
 * "gust": 13.456,
 * "rain": 0.1,
 * "press": 101300.00,
 * "temp": 18.34,
 * "hum": 58.5,
 * "cputemp": 34.56 }
 * <p>
 * - Logging...
 * - Sending Data to some DB (REST interface)
 * TODO
 * - Add orientable camera
 * <p>
 * Use -Dws.verbose=true for more output.
 * Use -Ddata.logger=<LoggerClassName> for logging
 *
 * LoggerClassName must implement the LoggerInterface
 */
public class HomeWeatherStation {
	private static boolean go = true;
	private static LoggerInterface logger = null;

	private final static int AVG_BUFFER_SIZE = 100;
	private static List<Float> avgWD = new ArrayList<>(AVG_BUFFER_SIZE);

	public static void main(String... args) throws Exception {
		final Thread coreThread = Thread.currentThread();
		final WebSocketFeeder wsf;
		if ("true".equals(System.getProperty("ws.log", "false"))) { // ws stands for WebSocket (NOT Weather Station)
			// Uses -Dws.uri
			wsf = new WebSocketFeeder();
		} else {
			wsf = null;
		}

		String loggerClassName = System.getProperty("data.logger", null);
		if (loggerClassName != null) {
			try {
				Class<? extends LoggerInterface> logClass = Class.forName(loggerClassName).asSubclass(LoggerInterface.class);
				logger = logClass.newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nUser interrupted.");
			go = false;
			synchronized (coreThread) {
				coreThread.notify();
			}
		}));

		HTTPServer httpServer = null;
		if ("true".equals(System.getProperty("weather.station.http", "true"))) {
			httpServer = new HTTPServer(); // Created and started
		}

		SDLWeather80422 weatherStation = new SDLWeather80422(); // With default parameters.
		weatherStation.setWindMode(SDLWeather80422.SdlMode.SAMPLE, 5);

		while (go) {
			if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
				System.out.println("-> While go...");
			}

			double ws = weatherStation.currentWindSpeed();
			double wg = weatherStation.getWindGust();
			float wd = weatherStation.getCurrentWindDirection();
			float mwd = getAverageWD(wd);
			double volts = weatherStation.getCurrentWindDirectionVoltage();
			float rain = weatherStation.getCurrentRainTotal();

			if ("true".equals(System.getProperty("show.rain", "false"))) {
				System.out.println(">> Rain : " + NumberFormat.getInstance().format(rain) + " mm");
			}

			JSONObject windObj = new JSONObject();
			windObj.put("dir", wd);
			windObj.put("avgdir", mwd);
			windObj.put("volts", volts);
			windObj.put("speed", ws);
			windObj.put("gust", wg);
			windObj.put("rain", rain);
			// Add temperature, pressure, humidity
			if (weatherStation.isBMP180Available()) {
				try {
					float temp = weatherStation.readTemperature();
					float press = weatherStation.readPressure();
					windObj.put("temp", temp);
					windObj.put("press", press);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (weatherStation.isHTU21DFAvailable()) {
				try {
					float hum = weatherStation.readHumidity();
					windObj.put("hum", hum);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			float cpuTemp = 0; // SystemInfo.getCpuTemperature();
			windObj.put("cputemp", cpuTemp);
			/*
			 * Sample message:
			 * { "dir": 350.0,
			 *   "avgdir": 345.67,
			 *   "volts": 3.4567,
			 *   "speed": 12.345,
			 *   "gust": 13.456,
			 *   "rain": 0.1,
			 *   "press": 101300.00,
			 *   "temp": 18.34,
			 *   "hum": 58.5,
			 *   "cputemp": 34.56 }
			 */
			try {
				String message = windObj.toString();
				if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
					System.out.println("-> Wind Message:" + message);
				}
				if (httpServer != null) {
					httpServer.setData(message);
				}
				if (wsf != null) {
					if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
						System.out.println("-> Sending message (wsf)");
					}
					// TODO JSON or NMEA?
					wsf.pushMessage(message);
				} else {
					if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
						System.out.println("-> NOT Sending message (wsf)");
					}
				}
				if (logger != null) {
					try {
						logger.pushMessage(windObj);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} catch (NotYetConnectedException nyce) {
				System.err.println(" ... Not yet connected, check your WebSocket server");
				try {
					wsf.initWebSocketConnection();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				synchronized (coreThread) {
					coreThread.wait(1_000L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		weatherStation.shutdown();
		if (wsf != null) {
			wsf.shutdown();
		}
		System.out.println("Done.");
	}

	public static float getAverageWD(float wd) {
		avgWD.add(wd);
		while (avgWD.size() > AVG_BUFFER_SIZE) {
			avgWD.remove(0);
		}
		return averageDir();
	}

	private static float averageDir() {
		double sumCos = 0, sumSin = 0;
		int len = avgWD.size();
		//var sum = 0;
		for (int i = 0; i < len; i++) {
			//  sum += va[i];
			sumCos += Math.cos(Math.toRadians(avgWD.get(i)));
			sumSin += Math.sin(Math.toRadians(avgWD.get(i)));
		}
		double avgCos = sumCos / len;
		double avgSin = sumSin / len;

		double aCos = Math.toDegrees(Math.acos(avgCos));
		//var aSin = toDegrees(Math.asin(avgSin));
		double avg = aCos;
		if (avgSin < 0) {
			avg = 360 - avg;
		}
		return (float) avg;
	}
}
