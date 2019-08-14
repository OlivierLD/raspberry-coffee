package weatherstation.ws;

import org.json.JSONObject;

import utils.WeatherUtil;
import weatherstation.logger.LoggerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use this when the Weather Station is not available (ie you're not on the RPi)
 * There is an optional WebSocket server though. Its default URL is ws://localhost:9876/
 * <p>
 * See the script weather.simulator for details on the way to start this.
 */
public class HomeWeatherStationSimulator {
	private static boolean go = true;
	private static List<LoggerInterface> loggers = null;

	public static void main(String... args) throws Exception {
		final Thread coreThread = Thread.currentThread();

		String loggerClassNames = System.getProperty("data.logger", null);
		if (loggerClassNames != null) {
			String[] loggerClasses = loggerClassNames.split(",");
			loggers = new ArrayList<>();
			for (String loggerClassName : loggerClasses) {
				if (loggerClassName.trim().length() > 0) {
					try {
						Class<? extends LoggerInterface> logClass = Class.forName(loggerClassName).asSubclass(LoggerInterface.class);
						loggers.add(logClass.newInstance());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nUser interrupted.");
			go = false;
			synchronized (coreThread) {
				if (loggers != null && loggers.size() > 0) {
					loggers.stream().forEach(logger -> logger.close());
				}
				coreThread.notify();
			}
		}, "Shutdown Hook"));

		double windSpeed = 0d;
		double windGust = 0d;
		float windDir = 0f;
		double voltage = 0;
		double pressure = 101300;
		double humidity = 50;
		double temperature = 15;
		double rainamount = 0;
		double dew = 0;

		int BUFFER_SIZE = 3_600; // Because we loop every second
		List<Float> prateList = new ArrayList<>(BUFFER_SIZE);

		while (go) {
			double ws = generateRandomValue(windSpeed, 3, 0, 65);
			double wg = generateRandomValue(windGust, 5, 0, 65);
			float wd = (float) generateRandomValue(windDir, 10, 0, 360);
			float mwd = HomeWeatherStation.getAverageWD(wd);
			double volts = generateRandomValue(voltage, 3, 0, 65);
			float temp = (float) generateRandomValue(temperature, 2, -10, 50);
			float press = (float) generateRandomValue(pressure, 100, 98_000, 105_000);
			float hum = (float) generateRandomValue(humidity, 5, 0, 100);
			float rain = (float) generateRandomValue(rainamount, 0.0001, 0, 0.001);
			dew = WeatherUtil.dewPointTemperature(hum, temp);

			// rain is an accumulator, it is in mm, not mm/h
			prateList.add(rain);
			while (prateList.size() > BUFFER_SIZE) {
				prateList.remove(0); // drop if more than 1 hour old
			}
			double rainAmountPastHour = prateList.stream().collect(Collectors.summingDouble(rainAmount -> (double)rainAmount));
			float prate = (float)rainAmountPastHour;
			prate *= (3_600F / prateList.size());

			JSONObject windObj = new JSONObject();
			windObj.put("dir", wd);
			windObj.put("avgdir", mwd);
			windObj.put("volts", volts);
			windObj.put("speed", ws);
			windObj.put("gust", wg);
			windObj.put("temp", temp);
			windObj.put("press", press);
			windObj.put("hum", hum);
			windObj.put("rain", prate);
			windObj.put("dew", dew);
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
       *   "hum": 58.5,
        *  "dew": 9.87 }
       */

			if (loggers != null) {
				loggers.stream()
						.forEach(logger -> {
							try {
								if ("true".equals(System.getProperty("simulator.verbose", "false"))) {
									System.out.println(String.format(">> Pushing %s", windObj));
								}
								logger.pushMessage(windObj);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
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
					coreThread.wait(1_000L); // 1 sec.
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
