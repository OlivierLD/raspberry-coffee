package nmea.parser;

import java.io.Serializable;
import java.text.DecimalFormat;

public class GeoPos implements Serializable {
	public double lat = 0.0;
	public double lng = 0.0;

	private final static DecimalFormat DF_22 = new DecimalFormat("00.00");
	private final static DecimalFormat DF_2  = new DecimalFormat("00");
	private final static DecimalFormat DF_3  = new DecimalFormat("000");

	public final static String DEGREE_SYMBOL = "\u00b0";

	//                                      0         1         2
	//                                      01234567890123456789012345. Useless beyond 'X' (x=23, pos 24)
	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public GeoPos(double l,
	              double g) {
		this.lat = l;
		this.lng = g;
	}

	public boolean equals(GeoPos compareto) {
		return (this.lat == compareto.lat && this.lng == compareto.lng);
	}

	public String toString() {
		return this.getLatInDegMinDec() + " / " + this.getLngInDegMinDec();
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
					throws RuntimeException
	{
		double deg = 0.0D;
		double min = 0.0D;
		double ret = 0.0D;
		try
		{
			deg = Double.parseDouble(degrees);
			min = Double.parseDouble(minutes);
			min *= (10.0 / 6.0);
			ret = deg + min / 100D;
		}
		catch(NumberFormatException nfe)
		{
			throw new RuntimeException("Bad number [" + degrees + "] [" + minutes + "]");
		}
		return ret;
	}

	public static double sexToDec(String degrees, String minutes, String seconds)
					throws RuntimeException
	{
		double deg = 0.0D;
		double min = 0.0D;
		double sec = 0.0D;
		double ret = 0.0D;
		try
		{
			deg = Double.parseDouble(degrees);
			min = Double.parseDouble(minutes);
			min *= (10.0 / 6.0);
			sec = Double.parseDouble(seconds);
			sec *= (10.0 / 6.0);
			min += ((sec / 0.6) / 100D);
			ret = deg + (min / 100D);
		}
		catch(NumberFormatException nfe)
		{
			throw new RuntimeException("Bad number");
		}
		return ret;
	}

	public static GeoPos init() {
		return new GeoPos(0d, 0d);
	}

	/**
	 * see http://en.wikipedia.org/wiki/Maidenhead_Locator_System
	 *
	 * @param lat [-90..+90]
	 * @param lng [-180..+180]
	 * @return The name of the grid square
	 */
	public static String gridSquare(double lat, double lng)
	{
		String gridSquare = "";

		lng += 180; // [0..360]
		lat +=  90; // [0..180]
		int first = (int) (lng / 20d);
		gridSquare += ALPHABET.charAt(first);
		int second = (int) (lat / 10d);
		gridSquare += ALPHABET.charAt(second);

		int third = (int)((lng % 20) / 2);
		gridSquare += Integer.toString(third);
		int fourth = (int)((lat % 10));
		gridSquare += Integer.toString(fourth);

		double d = lng - ((int)(lng / 2) * 2);
		int fifth = (int)(d * 12);
		gridSquare += ALPHABET.toLowerCase().charAt(fifth);
		double e = lat - (int)lat;
		int sixth = (int)(e * 24);
		gridSquare += ALPHABET.toLowerCase().charAt(sixth);

		return gridSquare;
	}

	public String gridSquare() {
		return gridSquare(this.lat, this.lng);
	}

	/**
	 * Just for tests
	 * @param args not used.
	 */
	public static void main(String... args) {
		double lat = sexToDec("24", "03.76");
		double lng = sexToDec("109", "59.50") * -1; // West
		System.out.println(String.format("Grid Square La Ventana: %s", new GeoPos(lat, lng).gridSquare()));

		lat = sexToDec("37", "46");
		lng = sexToDec("122", "31") * -1; // West
		System.out.println(String.format("Grid Square Ocean Beach (SF) : %s", new GeoPos(lat, lng).gridSquare()));
	}
}
