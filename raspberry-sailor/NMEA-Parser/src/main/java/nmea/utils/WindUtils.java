package nmea.utils;

public class WindUtils {
	// Beaufort Scale                              0   1   2   3    4    5    6    7    8    9   10   11   12
	public final static double[] BEAUFORT_SCALE = {0d, 1d, 4d, 7d, 11d, 16d, 22d, 28d, 34d, 41d, 48d, 56d, 64d};

	public static int getBeaufort(double d) {
		int b = 0;
		for (int i = 0; i < BEAUFORT_SCALE.length; i++) {
			if (d < BEAUFORT_SCALE[i]) {
				b = i - 1;
				break;
			}
		}
		return b;
	}

	public static String getRoseSpeedAndDirection(double tws, double twd) {
		String windStr = "";
		int beaufort = getBeaufort(tws);
		windStr = "F " + Integer.toString(beaufort);
		windStr += (", " + getRoseDir(twd));
		return windStr;
	}

	public static String getRoseDir(double twd) {
		String rose = "";
		float delta = 11.25f; // Un quart, \u00BC

		String[] data = new String[]{
				"N", "N\u00BCNE", "NNE", "NE\u00BCN", "NE", "NE\u00BCE", "ENE", "E\u00BCNE",
				"E", "E\u00BCSE", "ESE", "SE\u00BCE", "SE", "SE\u00BCS", "SSE", "S\u00BCSE",
				"S", "S\u00BCSW", "SSW", "SW\u00BCS", "SW", "SW\u00BCW", "WSW", "W\u00BCSW",
				"W", "W\u00BCNW", "WNW", "NW\u00BCW", "NW", "NW\u00BCN", "NNW", "N\u00BCNW"
		};

		int index = 0;
		if (twd > (360 - (delta / 2f)) || twd <= (delta / 2f)) {
			index = 0;
		} else {
			for (int i = 0; i < data.length; i++) {
				//      System.out.println("--- i=" + i + ", is [" + Double.toString((i + 0.5) * delta) + "<" + twd + "<=" + Double.toString((i + 1.5) * delta) + " ?");
				if (twd > ((i + 0.5) * delta) && twd <= ((i + 1.5) * delta)) {
					index = i + 1;
					break;
				}
			}
		}
		rose = data[index];
		return rose;
	}
}
