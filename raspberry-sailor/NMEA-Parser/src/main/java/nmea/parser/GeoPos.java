package nmea.parser;

import calc.GeomUtil;

import java.io.Serializable;
import java.text.DecimalFormat;

public class GeoPos implements Serializable {
	public double lat = 0.0;
	public double lng = 0.0;
	public String gridSquare = "";

	private final static DecimalFormat DF_22 = new DecimalFormat("00.00");
	private final static DecimalFormat DF_2  = new DecimalFormat("00");
	private final static DecimalFormat DF_3  = new DecimalFormat("000");

	public final static String DEGREE_SYMBOL = "\u00b0";

	public GeoPos(double l,
	              double g) {
		this.lat = l;
		this.lng = g;
		this.gridSquare = this.gridSquare();
	}

	public boolean equals(GeoPos compareTo) {
		return (this.lat == compareTo.lat && this.lng == compareTo.lng);
	}

	@Override
	public String toString() {
		if (this.gridSquare == null || this.gridSquare.isEmpty()) {
			this.gridSquare = this.gridSquare();
		}
		return this.getLatInDegMinDec() + " / " + this.getLngInDegMinDec() + (this.gridSquare.isEmpty() ? " (-)" : String.format(" (%s)", this.gridSquare));
	}

	public String getLatInDegMinDec() {
		int degree = (int) lat;
		String sgn = (degree >= 0) ? "N" : "S";
		double minutes = Math.abs(lat - degree);
		double hexMin = 100.0 * minutes * (6.0 / 10.0);
		return sgn + "  " + DF_2.format((long) Math.abs(degree)) + DEGREE_SYMBOL + DF_22.format(hexMin) + "'";
	}

	public String getLngInDegMinDec() {
		int degree = (int) lng;
		String sgn = (degree >= 0) ? "E" : "W";
		double minutes = Math.abs(lng - degree);
		double hexMin = 100.0 * minutes * (6.0 / 10.0);
		return sgn + " " + DF_3.format((long) Math.abs(degree)) + DEGREE_SYMBOL + DF_22.format(hexMin) + "'";
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
		}
		catch(NumberFormatException nfe) {
			throw new RuntimeException(String.format("Bad number [%s] [%s]", degrees, minutes));
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
		}
		catch(NumberFormatException nfe) {
			throw new RuntimeException(String.format("Bad number [%s] [%s] [%s]", degrees, minutes, seconds));
		}
		return ret;
	}

	public static GeoPos init() {
		return new GeoPos(0d, 0d);
	}

	/**
	 * see http://en.wikipedia.org/wiki/Maidenhead_Locator_System
	 * and also https://www.karhukoti.com/maidenhead-grid-square-locator/?grid=CM87
	 *
	 * Generate the grid square from the lat and lng.
	 */
	public String gridSquare() {
		return GeomUtil.gridSquare(this.lat, this.lng);
	}

	public GeoPos updateGridSquare() {
		this.gridSquare = this.gridSquare();
//		System.out.println(String.format(">> from %f/%f => GRID Square: %s", this.lat, this.lng, this.gridSquare));
		return this;
	}
}
