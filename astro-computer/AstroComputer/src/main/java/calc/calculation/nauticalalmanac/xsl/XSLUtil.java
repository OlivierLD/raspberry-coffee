package calc.calculation.nauticalalmanac.xsl;

import calc.GeomUtil;

import java.text.DecimalFormat;

public class XSLUtil {
	private final static DecimalFormat df4 = new DecimalFormat("###0.0000");
	private final static DecimalFormat df3 = new DecimalFormat("###0.000");
	private final static DecimalFormat df2 = new DecimalFormat("###0.00");
	private final static DecimalFormat df1 = new DecimalFormat("###0.0");

	private final static DecimalFormat dfi2 = new DecimalFormat("00");

	public final static String formatI2(double d) {
		return dfi2.format(d);
	}

	public final static String formatX4(double d) {
		return df4.format(d);
	}

	public final static String formatX3(double d) {
		return df3.format(d);
	}

	public final static String formatX2(double d) {
		return df2.format(d);
	}

	public final static String formatX1(double d) {
		return df1.format(d);
	}

	public final static String toNbsp(String s) {
		return s.replaceAll(" ", "&nbsp;");
	}

	public final static String initCap(String s) {
		String str = "";
		try {
			str = s.toLowerCase();
			char[] ca = str.toCharArray();
			char c = new String(new char[]{ca[0]}).toUpperCase().toCharArray()[0];
			ca[0] = c;
			str = new String(ca);
		} catch (Exception ignore) {
		}

		return str;
	}

	public final static String decToSex(double d, int a, int b) {
		return GeomUtil.decToSex(d, a, b);
	}

	public final static String decToSex(double d, int a, int b, int c) {
		return GeomUtil.decToSex(d, a, b, c);
	}

	public final static String decToSexTrunc(double d, int a, int b) {
		return GeomUtil.decToSex(d, a, b, true);
	}

	/**
	 * @param d eot in hours
	 * @return formatted string
	 */
	public final static String eotToString(double d) {
		int m = (int) Math.floor(Math.abs(d) * 60d);
		int s = (int) Math.round((3600d * Math.abs(d)) - (m * 60d));
		return ((d > 0 ? "+" : "-") + Integer.toString(m) + "m" + Integer.toString(s) + "s");
	}

}
