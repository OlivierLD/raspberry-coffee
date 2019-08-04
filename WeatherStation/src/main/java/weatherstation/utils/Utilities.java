package weatherstation.utils;

import utils.WeatherUtil;

import java.text.NumberFormat;

public class Utilities {
	private final static double VARY_VALUE = 0.05d;
	private final static boolean verbose = "true".equals(System.getProperty("fuzzy.verbose", "false"));

	public static boolean fuzzyCompare(double thisValue, double thatValue) {
		boolean b = false;
		if (thatValue > (thisValue * (1.0 - VARY_VALUE)) &&
				thatValue < (thisValue * (1.0 + VARY_VALUE))) {
			if (verbose) {
				System.out.println(thatValue + " is in ]" + (thisValue * (1.0 - VARY_VALUE)) + ", " + (thisValue * (1.0 + VARY_VALUE)) + "[");
			}
			b = true;
		}
		return b;
	}

	public enum Voltage { // No, there is no error. 3.3 uses 0.66.
		V3_3(0.66f),
		V5(1.0f);

		private final float adjust;

		Voltage(float adjust) {
			this.adjust = adjust;
		}

		public float adjust() {
			return this.adjust;
		}
	}

	private static double getAdjustment(Voltage v) {
		return v.adjust();
	}

	public static double voltageToDegrees(double value, double defaultWindDir) {
		return voltageToDegrees(value, defaultWindDir, Voltage.V3_3);
	}

	/*
	 * Returns a value every 22.5 degrees (360 / 16).
	 */
	public static double voltageToDegrees(double value, double defaultWindDir, Voltage v) {
		if (fuzzyCompare(3.84 * getAdjustment(v), value)) {
			return 0d; // N
		}
		if (fuzzyCompare(1.98 * getAdjustment(v), value)) {
			return 22.5; // NNE
		}
		if (fuzzyCompare(2.25 * getAdjustment(v), value)) {
			return 45; // NE
		}
		if (fuzzyCompare(0.41 * getAdjustment(v), value)) {
			return 67.5; // ENE
		}
		if (fuzzyCompare(0.45 * getAdjustment(v), value)) {
			return 90.0; // E
		}
		if (fuzzyCompare(0.32 * getAdjustment(v), value)) {
			return 112.5; // ESE
		}
		if (fuzzyCompare(0.90 * getAdjustment(v), value)) {
			return 135.0; // SE
		}
		if (fuzzyCompare(0.62 * getAdjustment(v), value)) {
			return 157.5; // SSE
		}
		if (fuzzyCompare(1.40 * getAdjustment(v), value)) {
			return 180; // S
		}
		if (fuzzyCompare(1.19 * getAdjustment(v), value)) {
			return 202.5; // SSW
		}
		if (fuzzyCompare(3.08 * getAdjustment(v), value)) {
			return 225; // SW
		}
		if (fuzzyCompare(2.93 * getAdjustment(v), value)) {
			return 247.5; // WSW
		}
		if (fuzzyCompare(4.62 * getAdjustment(v), value)) {
			return 270.0; // W
		}
		if (fuzzyCompare(4.04 * getAdjustment(v), value)) {
			return 292.5; // WNW
		}
		if (fuzzyCompare(4.34 * getAdjustment(v), value)) { // chart in manufacturers documentation seems wrong
			return 315.0; // NW
		}
		if (fuzzyCompare(3.43 * getAdjustment(v), value)) {
			return 337.5; // NNW
		}

		if ("true".equals(System.getProperty("sdl.weather.station.wind.verbose", "false"))) {
			System.out.println("Default windDir.");
		}
		return defaultWindDir;
	}

	public static long currentTimeMicros() {
//	long milli = System.currentTimeMillis();
		long nanoTime = System.nanoTime();
//	return milli * 1_000;
		return Math.round(nanoTime / 1_000D);
	}

	/** For tests
	 *
	 * @param args unused.
	 */
	public static void main(String... args) {
		double hum = 65.45;
		double temp = 18.2;
		System.out.println(String.format("Hum: %.2f%%, Temp: %.2f\u00b0C, Dew Point Temp: %.2f\u00b0C", hum, temp, WeatherUtil.dewPointTemperature(hum, temp)));

		hum = 30.0;
		temp = 12.0;
		System.out.println(String.format("Hum: %.2f%%, Temp: %.2f\u00b0C, Dew Point Temp: %.2f\u00b0C", hum, temp, WeatherUtil.dewPointTemperature(hum, temp)));

		hum = 10.0;
		temp = 25.0;
		System.out.println(String.format("Hum: %.2f%%, Temp: %.2f\u00b0C, Dew Point Temp: %.2f\u00b0C", hum, temp, WeatherUtil.dewPointTemperature(hum, temp)));

		hum = 90.0;
		temp = 50.0;
		System.out.println(String.format("Hum: %.2f%%, Temp: %.2f\u00b0C, Dew Point Temp: %.2f\u00b0C", hum, temp, WeatherUtil.dewPointTemperature(hum, temp)));

		boolean go = true;
		System.out.println("Enter [q] to quit, when prompted for temperature.");
		while (go) {
			String one = utils.StaticUtil.userInput(" Temp in Celcius > ");
			if ("Q".equalsIgnoreCase(one)) {
				go = false;
			} else {
				String two = utils.StaticUtil.userInput(" Rel. Hum in %   > ");
				try {
					temp = Double.parseDouble(one);
					hum = Double.parseDouble(two);
					System.out.println(String.format("Hum: %.2f%%, Temp: %.2f\u00b0C, Dew Point Temp: %.2f\u00b0C", hum, temp, WeatherUtil.dewPointTemperature(hum, temp)));
				} catch (NumberFormatException nfe) {
					System.err.println(nfe);
				}
			}
		}

		for (int i=0; i<10; i++) {
			long time = currentTimeMicros();
			System.out.println(String.format("%s => %d (in \u00b5s)", NumberFormat.getInstance().format(time), time));
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
		}

	}
}
