package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class GeoPos implements Serializable {
	public double lat = 0.0;
	public double lng = 0.0;

	private final static 		DecimalFormat DF_22 = new DecimalFormat("00.00");
	private final static 		DecimalFormat DF_2  = new DecimalFormat("00");
	private final static 		DecimalFormat DF_3  = new DecimalFormat("000");

	public GeoPos(double l,
	              double g) {
		this.lat = l;
		this.lng = g;
	}

	public boolean equals(GeoPos compareto) {
		return (this.lat == compareto.lat && this.lng == compareto.lng);
	}

	public String toString() {
		return getLatInDegMinDec() + " / " + getLngInDegMinDec();
	}

	public String getLatInDegMinDec() {
		int degree = (int) lat;
		String sgn = (degree >= 0) ? "N" : "S";
		double minutes = Math.abs(lat - degree);
		double hexMin = 100.0 * minutes * (6.0 / 10.0);
		return sgn + "  " + DF_2.format((long) Math.abs(degree)) + "\u00b0" + DF_22.format(hexMin) + "'";
	}

	public String getLngInDegMinDec() {
		int degree = (int) lng;
		String sgn = (degree >= 0) ? "E" : "W";
		double minutes = Math.abs(lng - degree);
		double hexMin = 100.0 * minutes * (6.0 / 10.0);
		return sgn + " " + DF_3.format((long) Math.abs(degree)) + "\u00b0" + DF_22.format(hexMin) + "'";
	}

	public static GeoPos init() {
		return new GeoPos(0d, 0d);
	}
}
