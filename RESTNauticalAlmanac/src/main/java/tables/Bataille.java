package tables;

import calc.GeomUtil;
import java.text.DecimalFormat;

public class Bataille {
	//private final static DecimalFormat fmt3  = new DecimalFormat("000");
	private final static DecimalFormat fmt2 = new DecimalFormat("#0");
	private final static DecimalFormat fmt03 = new DecimalFormat("##0.#");
	private final static DecimalFormat fmt12 = new DecimalFormat("0.00");
	private final static DecimalFormat fmt22 = new DecimalFormat("#0.00");

	public static void table1() {
		double[] P = {0, 5, 10, 15, 20, 24, 27, 30, 32, 34, 36, 38, 40, 42, 44, 45,
				46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
				62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77,
				78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 87.5, 88, 88.5, 89, 89.5, 90};
		System.out.println("  <table id='1'>");
		for (int lat = 0; lat <= 90; lat++) {
			System.out.println("    <lat val='" + fmt2.format(lat) + "'>");
			for (int i = 0; i < P.length; i++) {
				double v = calculateTable1(P[i], (double) lat);
				System.out.println("      <value p='" + fmt03.format(P[i]) + "' h='" + GeomUtil.formatDegInHM(P[i]) + "' h2='" + GeomUtil.formatDegInHM(180 - P[i]) + "'>" + fmt12.format(v) + "</value>");
			}
			System.out.println("    </lat>");
		}
		System.out.println("  </table>");
	}

	public static double calculateTable1(double p, double l) {
		double v = Math.cos(Math.toRadians(p)) * Math.sin(Math.toRadians(l));
		return v;
	}

	public static void table2() {
		System.out.println("  <table id='2'>");
		for (int l = 0; l < 90; l++) {
			System.out.println("    <lat val='" + fmt2.format(l) + "' h1='" + GeomUtil.formatDegInHM(90 - l) + "' h2='" + GeomUtil.formatDegInHM(90 + l) + "'>");
			for (int p = 0; p < 90; p++) {
				double v = calculateTable2(p, l);
				System.out.println("      <value p='" + fmt2.format(p) + "'>" + fmt22.format(v) + "</value>");
			}
			System.out.println("    </lat>");
		}
		System.out.println("  </table>");
	}

	public static double calculateTable2(double p, double l) {
		double v = Math.cos(Math.toRadians(l)) * Math.tan(Math.toRadians(p));
		return v;
	}

	public static void allTheTables() {
		System.out.println("<?xml version='1.0' encoding='utf-8'?>");
		System.out.println("<bataille>");
		table1();
		table2();
		System.out.println("</bataille>");
	}

	public static void main(String... args) {
		allTheTables();
	}
}
