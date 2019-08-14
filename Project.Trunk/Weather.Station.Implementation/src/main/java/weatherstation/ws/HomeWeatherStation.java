package weatherstation.ws;

import org.json.JSONObject;
import utils.WeatherUtil;
import weatherstation.SDLWeather80422;
import weatherstation.logger.LoggerInterface;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
 * <p>
 * Feeds all the loggers with a json object like
 * { "dir": 350.0,
 * "avgdir": 345.67,
 * "volts": 3.4567,
 * "speed": 12.345,
 * "gust": 13.456,
 * "rain": 0.1,
 * "press": 101300.00,
 * "temp": 18.34,
 * "dew": 9.87,
 * "hum": 58.5 }
 * <p>
 * - Logging...
 * - Sending Data to some DB (REST interface)
 * TODO
 * - Add orientable camera
 * <p>
 * Use -Ddata.logger=<LoggerClassName(s)> for logging
 *
 * LoggerClassName must implement the LoggerInterface
 *
 * -Dweather.station.verbose
 * -Dshow.rain
 */
public class HomeWeatherStation {

	private static int SAMPLE_TIME = 5; // 5 seconds

	private static boolean go = true;
	private static List<LoggerInterface> loggers = null;

	private final static int AVG_BUFFER_SIZE = 100;
	private static List<Float> avgWD = new ArrayList<>(AVG_BUFFER_SIZE);

	public static void main(String... args) throws Exception {
		final Thread coreThread = Thread.currentThread();

		String loggerClassNames = System.getProperty("data.logger", null); // comma separated list of LoggerInterface
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

		SDLWeather80422 weatherStation = new SDLWeather80422(); // With default parameters.
		weatherStation.setWindMode(SDLWeather80422.SdlMode.SAMPLE, SAMPLE_TIME);

		int BUFFER_SIZE = 3_600; // Because we loop every second
		List<Float> prateList = new ArrayList<>(BUFFER_SIZE);

		while (go) { // Main loop
			if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
				System.out.println("-> While go...");
			}
			double ws = weatherStation.getCurrentWindSpeed();
			double wg = weatherStation.getWindGust();
			float wd = weatherStation.getCurrentWindDirection();
			float mwd = getAverageWD(wd);
			double volts = weatherStation.getCurrentWindDirectionVoltage();
			float rain = weatherStation.getCurrentRainTotal();

			// rain is an accumulator, it is in mm, not mm/h
			prateList.add(rain);
			while (prateList.size() > BUFFER_SIZE) {
				prateList.remove(0); // drop if more than 1 hour old
			}
			double rainAmountPastHour = prateList.stream().collect(Collectors.summingDouble(rainAmount -> (double)rainAmount));
			float prate = (float)rainAmountPastHour;
			prate *= (3_600F / prateList.size());

			if ("true".equals(System.getProperty("show.rain", "false"))) {
				System.out.println(">> Rain : " + NumberFormat.getInstance().format(rain) + " mm, PRate :" + NumberFormat.getInstance().format(prate) + " mm/h");
			}

			JSONObject windObj = new JSONObject();
			windObj.put("dir", wd);
			windObj.put("avgdir", mwd);
			windObj.put("volts", volts);
			windObj.put("speed", ws);
			windObj.put("gust", wg);
			windObj.put("rain", prate);

			// Add temperature, pressure, humidity, dew point
			if (weatherStation.isBMP180Available() || weatherStation.isHTU21DFAvailable()) {
				Float hum = null, press = null, temp = null;
				if (weatherStation.isBMP180Available()) {
					try {
						temp = weatherStation.readTemperature();
						press = weatherStation.readPressure();
						windObj.put("temp", temp);
						windObj.put("press", press);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (weatherStation.isHTU21DFAvailable()) {
					try {
						hum = weatherStation.readHumidity();
						windObj.put("hum", hum);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (temp != null && hum != null) {
					double dew = WeatherUtil.dewPointTemperature(hum, temp);
					windObj.put("dew", dew);
				}
			}
//			float cpuTemp = 0; // SystemInfo.getCpuTemperature();
//			windObj.put("cputemp", cpuTemp);
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
			 *   "dew": 9.87 }
			 *
			 *   'volts' can be converted using {@link Utilities.voltageToDegrees}
			 */
			try {
				String message = windObj.toString();
				if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
					System.out.println("-> Wind Message:" + message);
				}
				if (loggers != null) {
					loggers.stream()
							.forEach(logger -> {
								try {
									logger.pushMessage(windObj);
								} catch (Exception ex) {
									System.err.println(String.format("At %s:", (new Date()).toString()));
									ex.printStackTrace();
								}
							});
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				synchronized (coreThread) {
					coreThread.wait(1_000L); // One second delay
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		weatherStation.shutdown();
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
