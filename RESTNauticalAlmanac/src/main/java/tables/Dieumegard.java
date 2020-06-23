package tables;

import calc.GeomUtil;
import java.text.DecimalFormat;

/**
 * Dynamically calculate the data for the 4 Dieumegard tables
 * Those data are published in XML format, so they can be transformed later on,
 * using fop, for example, by the appropriate stylesheet.
 */
public class Dieumegard {
	private final static DecimalFormat fmt4 = new DecimalFormat("0000");
	private final static DecimalFormat fmt3 = new DecimalFormat("000");
	private final static DecimalFormat fmt2 = new DecimalFormat("00");
	private final static DecimalFormat fmt14 = new DecimalFormat("0.0000");

	public static void table1() {
		System.out.println("  <table id='1'>");
		for (int m = 0; m <= 60; m++) {
			System.out.println("    <min val='" + fmt2.format(m) + "'>");
			for (int d = 0; d < 360; d++) {
				double ah = GeomUtil.sexToDec(fmt3.format(d), fmt2.format(m));
				double value = calculateTable1(ah);
				int neg = 0;
				if (value < 0.0 && value > -0.00001) {
					value = 0.0;
				}
				while (value < 0.0) {
					value += 1;
					neg++;
				}
				if (!Double.isInfinite(value)) {
					System.out.println("      <value deg='" + fmt3.format(d) + "' neg='" + neg + "'>");
					System.out.println("        <int>" + ((int) value) + "</int>");//fmt14.format(value)
					int mantisse = (int) Math.round((value - (int) value) * 10000.0);
					System.out.println("        <mant>" + fmt4.format(mantisse) + "</mant>");
					System.out.println("      </value>");
				} else {
					System.out.println("      <value deg='" + fmt3.format(d) + "' neg='" + neg + "'>");
					System.out.println("        <int></int>");
					System.out.println("        <mant></mant>");
					System.out.println("      </value>");
				}
			}
			System.out.println("    </min>");
		}
		System.out.println("  </table>");
	}

	private static double calculateTable1(double ah) {
		double d = 1 - Math.cos(Math.toRadians(ah));
		double log = log10(d);
		double colog = -log;

		return colog;
	}

	public static void table2() {
		System.out.println("  <table id='2'>");
		for (int m = 0; m <= 60; m++) {
			System.out.println("    <min val='" + fmt2.format(m) + "'>");
			for (int d = 0; d < 76; d++) {
				double dec = GeomUtil.sexToDec(fmt3.format(d), fmt2.format(m));
				double value = calculateTable2(dec);
				System.out.println("      <value deg='" + fmt3.format(d) + "'>" + fmt14.format(value) + "</value>");
			}
			System.out.println("    </min>");
		}
		System.out.println("  </table>");
	}

	private static double calculateTable2(double dec) {
		double d = Math.cos(Math.toRadians(dec));
		double log = log10(d);
		double colog = -log;

		return colog;
	}

	public static void table3() {
		System.out.println("  <table id='3'>");
		for (int du = 0; du < 100; du++) {
			System.out.println("    <du val='" + fmt2.format(du) + "'>");
			for (int mc = 0; mc <= 90; mc++) {
				double d = (double) (du + (mc * 100)) / 10000.0;
				double x = calculateTable3(d);
				System.out.println("      <value mc='" + fmt2.format(mc) + "'>" + fmt14.format(x) + "</value>");
			}
			System.out.println("    </du>");
		}
		System.out.println("  </table>");
	}

	private static double calculateTable3(double d) {
		double log = log10(d);
		double colog = -log;

		return colog;
	}

	public static void tableA() {
		System.out.println("  <table id='A'>");
		for (int m = 0; m <= 60; m++) {
			System.out.println("    <min val='" + fmt2.format(m) + "'>");
			for (int d = 0; d < 90; d++) {
				double x = GeomUtil.sexToDec(fmt3.format(d), fmt2.format(m));
				double value = calculateTableA(x);
				System.out.println("      <value deg='" + fmt3.format(d) + "'>" + fmt14.format(value) + "</value>");
			}
			System.out.println("    </min>");
		}
		System.out.println("  </table>");
	}

	private static double calculateTableA(double x) {
		double d = 1.0 - Math.cos(Math.toRadians(x));

		return d;
	}

	private static double log10(double a) {
		return Math.log(a) / Math.log(10);
	}

	public static void allTheTables() {
		System.out.println("<?xml version='1.0' encoding='utf-8'?>");
		System.out.println("<dieumegard>");
		table1();
		table2();
		table3();
		tableA();
		System.out.println("</dieumegard>");
	}

	public static void main(String... args) {
//  System.out.println("Tables de Dieumegard");
		allTheTables();

//    double test = calculateTable1(GeomUtil.sexToDec("300", "0"));
//    System.out.println("300 0=" + fmt14.format(test));
//    double d = calculateTable3(0.7550);
//    System.out.println("Table3:" + d);
	}
}
