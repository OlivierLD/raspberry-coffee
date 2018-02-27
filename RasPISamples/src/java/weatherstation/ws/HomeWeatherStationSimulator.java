package weatherstation.ws;

import org.json.JSONObject;

import weatherstation.logger.LoggerInterface;

/**
 * Use this when the Weather Station is not available (ie you're not on the RPi)
 * There is an optional WebSocket server though. Its default URL is ws://localhost:9876/
 * <p>
 * See the script weather.simulator for details on the way to start this.
 */
public class HomeWeatherStationSimulator {
	private static boolean go = true;
	private static LoggerInterface logger = null;

	public static void main(String... args) throws Exception {
		final Thread coreThread = Thread.currentThread();
		WebSocketFeeder wsf = null; // new WebSocketFeeder();
		if ("true".equals(System.getProperty("ws.log"))) { // ws stands for WebSocket (NOT Weather Station)
			// Uses -Dws.uri
			wsf = new WebSocketFeeder();
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

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("\nUser interrupted.");
				go = false;
				synchronized (coreThread) {
					coreThread.notify();
				}
			}
		});

		double windSpeed = 0d;
		double windGust = 0d;
		float windDir = 0f;
		double voltage = 0;
		double pressure = 101300;
		double humidity = 50;
		double temperature = 15;
		double rainamount = 0;

		while (go) {
			double ws = generateRandomValue(windSpeed, 3, 0, 65);
			double wg = generateRandomValue(windGust, 5, 0, 65);
			float wd = (float) generateRandomValue(windDir, 10, 0, 360);
			float mwd = HomeWeatherStation.getAverageWD(wd);
			double volts = generateRandomValue(voltage, 3, 0, 65);
			float temp = (float) generateRandomValue(temperature, 2, -10, 50);
			float press = (float) generateRandomValue(pressure, 100, 98_000, 105_000);
			float hum = (float) generateRandomValue(humidity, 5, 0, 100);
			float rain = (float) generateRandomValue(rainamount, 1, 0, 3);
			JSONObject windObj = new JSONObject();
			windObj.put("dir", wd);
			windObj.put("avgdir", mwd);
			windObj.put("volts", volts);
			windObj.put("speed", ws);
			windObj.put("gust", wg);
			windObj.put("temp", temp);
			windObj.put("press", press);
			windObj.put("hum", hum);
			windObj.put("rain", rain);
		  /*
       * Sample message:
       * { "dir": 350.0,
       *   "avgdir": 345.67,
       *   "volts": 3.4567,
       *   "speed": 12.345,
       *   "gust": 13.456,
       *   "press": 101300.00,
       *   "temp": 18.34,
       *   "rain": 0.1,
       *   "hum": 58.5 }
       */

			if (wsf != null) {
				System.out.println("Pushing " + windObj.toString());
				try {
					wsf.pushMessage(windObj.toString());
					System.out.println(">> WS pushed OK");
				} catch (Exception ex) {
					System.err.println("WS Push error:");
					ex.printStackTrace();
				}
			}
			if (logger != null) {
				try {
					logger.pushMessage(windObj);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			windSpeed = ws;
			windGust = wg;
			windDir = wd;
			voltage = volts;
			pressure = press;
			temperature = temp;
			humidity = hum;
			rainamount = rain;

			try {
				synchronized (coreThread) {
					coreThread.wait(1_000L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (wsf != null) {
			wsf.shutdown();
		}
		System.out.println("Done.");
	}

	public static double generateRandomValue(double from, double diffRange, double min, double max) {
		double d = from;
		while (true) {
			double rnd = 0.5 - Math.random();
			rnd *= diffRange;
			if (d + rnd >= min && d + rnd <= max) {
				d += rnd;
				break;
			}
		}
		return d;
	}
}
