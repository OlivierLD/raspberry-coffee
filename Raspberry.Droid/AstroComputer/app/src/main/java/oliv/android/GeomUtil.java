package oliv.android;

import java.text.DecimalFormat;

public final class GeomUtil {
	public static final int HTML = 0;
	public static final int SHELL = 1;
	public static final int SWING = 2;
	public static final int NO_DEG = 3;
	public static final int DEFAULT_DEG = 4;

	public static final int NONE = 0;
	public static final int NS = 1;
	public static final int EW = 2;

	public static final int LEADING_SIGN = 0;
	public static final int TRAILING_SIGN = 1;

	public final static String DEGREE_SYMBOL = "\u00b0";


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
			if (fullString.indexOf("\"") > -1) {
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
		double deg = 0.0D;
		double min = 0.0D;
		double ret = 0.0D;
		try {
			deg = Double.parseDouble(degrees);
			min = Double.parseDouble(minutes);
			min *= (10.0 / 6.0);
			ret = deg + min / 100D;
		} catch (NumberFormatException nfe) {
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

}
