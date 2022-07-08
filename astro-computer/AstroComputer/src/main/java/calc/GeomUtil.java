package calc;

import utils.DumpUtil;
import utils.StringUtils;

import java.text.DecimalFormat;

public final class GeomUtil {
	public static final int HTML = 0;
	public static final int SHELL = 1;
	public static final int SWING = 2;
	public static final int NO_DEG = 3;
	public static final int UNICODE = 5;
	public static final int DEFAULT_DEG = 4;

	public static final int NONE = 0;
	public static final int NS = 1;
	public static final int EW = 2;

	public static final int LEADING_SIGN = 0;
	public static final int TRAILING_SIGN = 1;

	public final static String DEGREE_SYMBOL = "\u00b0";

	public static class PolyAngle {

		public double getAngleInDegrees() {
			return angleInDegrees;
		}

		private double angleInDegrees;
		public static final short DEGREES = 0;
		public static final short HOURS = 1;

		public PolyAngle() {
		}

		public PolyAngle(double d, short type) {
			switch (type) {
				case 0:
					angleInDegrees = d;
					break;

				case 1:
//      angleInDegrees = GeomUtil.ra2ha(d);
					break;
			}
		}

		public PolyAngle(String str, short type) {
		}
	}

	private final static double KM_EQUATORIAL_EARTH_RADIUS = 6378.1370D;
	private final static double NM_EQUATORIAL_EARTH_RADIUS = 3443.9184665227D;
	private final static double MILE_EQUATORIAL_EARTH_RADIUS = 3964.0379117464D;

	protected static double haversineRaw(double lat1, double long1, double lat2, double long2) {
		double deltaG = Math.toRadians(long2 - long1);
		double deltaL = Math.toRadians(lat2 - lat1);
		double a = Math.pow(Math.sin(deltaL / 2.0), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(deltaG / 2.0), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return c;
	}

	public static double haversineKm(double lat1, double long1, double lat2, double long2) {
		return haversineRaw(lat1, long1, lat2, long2) * KM_EQUATORIAL_EARTH_RADIUS;
	}

	public static double haversineNm(double lat1, double long1, double lat2, double long2) {
		return haversineRaw(lat1, long1, lat2, long2) * NM_EQUATORIAL_EARTH_RADIUS;
	}

	public static double haversineMile(double lat1, double long1, double lat2, double long2) {
		return haversineRaw(lat1, long1, lat2, long2) * MILE_EQUATORIAL_EARTH_RADIUS;
	}

	public static double bearingDiff(double bearingA, double bearingB) {
		double diff = Math.abs(bearingA - bearingB);
		while (diff > 180) {
			diff = 360 - diff;
		}
		return diff;
	}
	/**
	 * Calculates great-circle (orthodromie) bearing between two points from and to.
	 * @param fromL latitude from, in degrees
	 * @param fromG longitude from, in degrees
	 * @param toL latitude to, in degrees
	 * @param toG latitude to, in degrees
	 * @return bearing from-to, in degrees on [0..360]
	 */
	public static double bearingFromTo(double fromL, double fromG, double toL, double toG) {
		double deltaG = toG - fromG;
		double x = Math.cos(Math.toRadians(toL)) * Math.sin(Math.toRadians(deltaG));
		double y = (Math.cos(Math.toRadians(fromL)) * Math.sin(Math.toRadians(toL))) -
				(Math.sin(Math.toRadians(fromL)) * Math.cos(Math.toRadians(toL)) * Math.cos(Math.toRadians(deltaG)));
		double b = Math.toDegrees(Math.atan2(x, y));
		while (b < 0) { // on0to360
			b += 360;
		}
		return b;
	}

	/**
	 * Converts [-180, 180] to [0, 360], -45 to 315, etc
	 * @param angle original
	 * @return tweaked
	 */
	public static double on0to360(double angle) {
		return (angle < 0) ? angle + 360 : angle;
	}

	/**
	 * Get the direction
	 *
	 * @param x horizontal displacement
	 * @param y vertical displacement
	 * @return the angle, in degrees, [0..360]
	 */
	public static double getDir(float x, float y) {
		return on0to360(Math.toDegrees(Math.atan2(x, y)));
	}

	/**
	 * @param fullString like [N 37 55.49], or [N 37 55'12.49"]
	 * @return The expected string
	 * @throws RuntimeException when failing
	 */
	public static double sexToDec(String fullString) throws RuntimeException {
		try {
			String sgn = fullString.substring(0, 1);
			int degSignIndex = fullString.indexOf(DEGREE_SYMBOL);
			if (degSignIndex < 0) {
				degSignIndex = fullString.indexOf(DEGREE_SYMBOL);
			}
			String degrees = fullString.substring(2, degSignIndex);
			String minutes = "";
			String seconds = "";
			if (fullString.contains("\"")) {
				minutes = fullString.substring(degSignIndex + 1, fullString.indexOf("'"));
				seconds = fullString.substring(fullString.indexOf("'") + 1, fullString.indexOf("\""));
			} else {
				minutes = fullString.substring(degSignIndex + 1);
			}
			double d = 0L;
			if (!seconds.trim().isEmpty()) {
				d = sexToDec(degrees, minutes, seconds);
			} else {
				d = sexToDec(degrees, minutes);
			}
			if (sgn.equals("S") || sgn.equals("W")) {
				d = -d;
			}
			return d;
		} catch (Exception e) {
			throw new RuntimeException("For [" + fullString + "] :" + e.getMessage());
		}
	}

	public static double sexToDec(String degrees, String minutes)
			throws RuntimeException {
		double ret;
		try { // TODO removeNullsFromString should have been done before.
			double deg = Double.parseDouble(degrees);
			double min = Double.parseDouble(minutes);
			min *= (10.0 / 6.0);
			ret = deg + (min / 100D);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			System.err.println("Degrees:");
			DumpUtil.displayDualDump(degrees, System.err);
			System.err.println("Minutes:");
			DumpUtil.displayDualDump(minutes, System.err);
			throw new RuntimeException("Bad number [" + degrees + "] [" + minutes + "]");
		}
		return ret;
	}

	public static double sexToDec(String degrees, String minutes, String seconds)
			throws RuntimeException {
		double deg = 0.0D;
		double min = 0.0D;
		double sec = 0.0D;
		double ret = 0.0D;
		try {
			deg = Double.parseDouble(degrees);
			min = Double.parseDouble(minutes);
			min *= (10.0 / 6.0);
			sec = Double.parseDouble(seconds);
			sec *= (10.0 / 6.0);
			min += ((sec / 0.6) / 100D);
			ret = deg + (min / 100D);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Bad number");
		}
		return ret;
	}

	public static String decToSex(double v) {
		return decToSex(v, GeomUtil.SHELL);
	}

	public static String decToSex(double v, int display) {
		return decToSex(v, GeomUtil.HTML, display);
	}

	public static String decToSex(double v, int output, int displayType) {
		return decToSex(v, output, displayType, TRAILING_SIGN);
	}

	public static String decToSex(double v, int output, int displayType, boolean truncMinute) {
		return decToSex(v, output, displayType, TRAILING_SIGN, truncMinute);
	}

	public static String decToSex(double v, int output, int displayType, int signPosition) {
		return decToSex(v, output, displayType, signPosition, false);
	}

	public static String decToSex(double v, int output, int displayType, int signPosition, boolean truncMinute) {
		String s = "";
		double absVal = Math.abs(v);
		double intValue = Math.floor(absVal);
		double dec = absVal - intValue;
		int i = (int) intValue;
		dec *= 60D;
		DecimalFormat df = (truncMinute ? new DecimalFormat("00") : new DecimalFormat("00.00"));
		if (output == GeomUtil.HTML) {
			s = Integer.toString(i) + "&deg;" + df.format(dec) + "'";
		} else if (output == GeomUtil.SWING) {
			s = Integer.toString(i) + '\260' + df.format(dec) + "'";
		} else if (output == GeomUtil.UNICODE) {
			s = Integer.toString(i) + '\u00b0' + df.format(dec) + "'";
		} else if (output == GeomUtil.NO_DEG) {
			s = Integer.toString(i) + ' ' + df.format(dec) + "'";
		} else {
			s = Integer.toString(i) + '\272' + df.format(dec) + "'";
		}
		if (v < 0.0D) {
			switch (displayType) {
				case NONE:
					s = "-" + s;
					break;
				case NS:
					s = (signPosition == TRAILING_SIGN ? s + "S" : "S " + StringUtils.lpad(s, (output == HTML) ? 13 : 9));
					break;
				case EW:
					s = (signPosition == TRAILING_SIGN ? s + "W" : "W " + StringUtils.lpad(s, (output == HTML) ? 14 : 10));
					break;
			}
		} else {
			switch (displayType) {
				case NONE:
					s = " " + s;
					break;
				case NS:
					s = (signPosition == TRAILING_SIGN ? s + "N" : "N " + StringUtils.lpad(s, (output == HTML) ? 13 : 9));
					break;
				case EW:
					s = (signPosition == TRAILING_SIGN ? s + "E" : "E " + StringUtils.lpad(s, (output == HTML) ? 14 : 10));
					break;
			}
		}
		return s;
	}

	public static String angle2Hour(double angle) {
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		double deg = angle;
		for (deg += 180; deg < 0.0D; deg += 360D) ;
		for (; deg > 360D; deg -= 360D) ;
		double nbMinArc = deg * 60D;
		double nbH = Math.floor(nbMinArc / (double) 900);
		nbMinArc -= nbH * (double) 900;
		double dnbM = (4D * nbMinArc) / 60D;
		double nbM = Math.floor(dnbM);
		double nbS = (dnbM - nbM) * 60D;
		hValue = nf.format(nbH) + ":" + nf.format(nbM) + ":" + nf.format(nbS);
		return hValue;
	}

	public static double degrees2hours(double d) {
		return d / 15D;
	}

	public static double hours2degrees(double d) {
		return d * 15D;
	}

	public static String formatInHours(double deg) {
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		DecimalFormat nf2 = new DecimalFormat("00.0");
		double nbMinArc = deg * 60D;
		double nbH = nbMinArc / (double) 900;
		nbMinArc -= Math.floor(nbH) * (double) 900;
		double dnbM = (4D * nbMinArc) / 60D;
		double nbS = (dnbM - Math.floor(dnbM)) * 60D;
		hValue = nf.format(Math.floor(nbH)) + ":" + nf.format(Math.floor(dnbM)) + ":" + nf2.format(nbS);
		return hValue;
	}

	public static String formatDegInHM(double deg) {
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		double nbMinArc = deg * 60D;
		double nbH = nbMinArc / (double) 900;
		nbMinArc -= Math.floor(nbH) * (double) 900;
		double dnbM = (4D * nbMinArc) / 60D;
//  double nbS = (dnbM - Math.floor(dnbM)) * 60D;
		hValue = nf.format(Math.floor(nbH)) + "h" + nf.format(Math.floor(dnbM));
		return hValue;
	}

	public static String formatHMS(double h) {
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		DecimalFormat nf2 = new DecimalFormat("00.000");
		double min = (h - Math.floor(h)) * 60D;
		double sec = (min - Math.floor(min)) * 60D;
		hValue = nf.format(Math.floor(h)) + ":" + nf.format(Math.floor(min)) + ":" + nf2.format(sec);
		return hValue;
	}

	public static String formatHM(double h) {
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		double min = (h - Math.floor(h)) * 60D;
		hValue = nf.format(Math.floor(h)) + ":" + nf.format(min);
		return hValue;
	}

	public static String formatDMS(double d) {
		return formatDMS(d, "\272");
	}

	public static String formatDMS(double d, String degChar) {
		boolean positive = true;
		if (d < 0) {
			d = -d;
			positive = false;
		}
		String hValue = "";
		DecimalFormat nf = new DecimalFormat("00");
		DecimalFormat nf2 = new DecimalFormat("00.000");
		double min = (d - Math.floor(d)) * 60D;
		double sec = (min - Math.floor(min)) * 60D;
		hValue = nf.format(Math.floor(d)) + degChar + nf.format(Math.floor(min)) + "'" + nf2.format(sec) + "\"";
		return (positive ? "" : "-") + hValue;
	}

	/**
	 * Note: Approximate. Should also consider equation of time at this date and location.
	 * @param g   Longitude
	 * @param hms UT HMS
	 * @return Solar Time (epoch)
	 */
	public static double getLocalSolarTime(double g, double hms) {
		double ahh = degrees2hours(g);
		double localSolar = hms + ahh;
		while (localSolar < 0) localSolar += 24;
		while (localSolar > 24) localSolar -= 24;
		return localSolar;
	}

	public static String signedDegrees(int i) {
		String prefix = "N";
		if (i == 0) prefix = "";
		if (i < 0) prefix = "S";
		return prefix + " " + Integer.toString(Math.abs(i));
	}

	/*
	 * See http://en.wikipedia.org/wiki/Maidenhead_Locator_System
	 */
	public static String gridSquare(double lat, double lng) {
		String gridSquare = "";

		lng += 180;
		lat += 90;
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		//                 0         1         2
		//                 01234567890123456789012345. Useless beyond X
		int first = (int) (lng / 20d);
		gridSquare += alphabet.charAt(first);
		int second = (int) (lat / 10d);
		gridSquare += alphabet.charAt(second);

		int third = (int) ((lng % 20) / 2);
		gridSquare += Integer.toString(third);
		int fourth = (int) ((lat % 10));
		gridSquare += Integer.toString(fourth);

		double d = lng - ((int) (lng / 2) * 2);
		int fifth = (int) (d * 12);
		gridSquare += alphabet.toLowerCase().charAt(fifth);
		double e = lat - (int) lat;
		int sixth = (int) (e * 24);
		gridSquare += alphabet.toLowerCase().charAt(sixth);

		return gridSquare;
	}
}
