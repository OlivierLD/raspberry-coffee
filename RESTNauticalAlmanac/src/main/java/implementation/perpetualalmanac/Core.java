package implementation.perpetualalmanac;

import calc.GeomUtil;

import java.text.DecimalFormat;

/**
 * Valid from March 1st 1900 through Feb 28 2100.
 * <p>
 * Based on Henning Umland's formulas.
 */
public class Core {
	public static double[] compute(int year, int month, int day, int hours, int minutes, int seconds) {
		double ut = hours + (minutes / 60d) + (seconds / 3_600d);
		double T = (367d * year) -
				Math.floor(1.75 * (year + Math.floor((month + 9d) / 12d))) +
				Math.floor(275d * (month / 9d)) +
				day +
				(ut / 24d) -
				730531.5;
		if (verbose) System.out.println("T [days]:" + T);
		double g = (0.9856003 * T - 2.472) % 360d;
		while (g < 0) g += 360d;
		if (verbose) System.out.println("Mean Sun Anomaly [deg]:" + g);
		double lm = (0.9856474 * T - 79.53938) % 360d;
		while (lm < 0) lm += 360d;
		if (verbose) System.out.println("Mean Sun Longitude [deg]:" + lm);
		double lt = lm + 1.915 * Math.sin(Math.toRadians(g)) + 0.02 * Math.sin(2d * Math.toRadians(g));
		while (lt < 0) lt += 360d;
		if (verbose) System.out.println("True Sun Longitude [deg]:" + lt);
		double eps = 23.439 - 4d * T * 10E-7;
		if (verbose) System.out.println("Obliquity of Ecliptic [deg]:" + eps);
		double dec = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(lt)) * Math.sin(Math.toRadians(eps))));
		if (verbose) System.out.println("Declination [deg]:" + dec);
		double ra = (2d * Math.toDegrees(Math.atan((Math.cos(Math.toRadians(eps)) * Math.sin(Math.toRadians(lt)) / (Math.cos(Math.toRadians(dec)) + Math.cos(Math.toRadians(lt))))))) % 360d;
		while (ra < 0) ra += 360d;
		if (verbose) System.out.println("RA [deg]:" + ra);
		double ghaAries = (0.9856474 * T + 15d * ut + 100.46062) % 360d;
		while (ghaAries < 0) ghaAries += 360d;
		if (verbose) System.out.println("GHA Aries [deg]:" + ghaAries);
		double ghaSun = (ghaAries - ra) % 360d;
		while (ghaSun < 0) ghaSun += 360d;
		if (verbose) System.out.println("GHA Sun [deg]:" + ghaSun);
		double gat = ((ghaSun / 15d) + 12d) % 24d;
		double eot = gat - ut;
		if (eot > 0.3) eot -= 24d;
		if (eot < -0.3) eot += 24d;
		if (verbose) System.out.println("EoT [h]:" + eot);
		double r = 1.00014 - 0.01671 * Math.cos(Math.toRadians(g)) - 0.00014 * Math.cos(2d * Math.toRadians(g));
		double sd = 16d / r;
		if (verbose) System.out.println("Sun sd ['] : " + sd);

		return new double[]{dec, ghaSun, ghaAries, eot, sd, 0.15};
	}

	private static boolean verbose = false;
	private final static DecimalFormat df2 = new DecimalFormat("00.00");

	public static void main(String... args) {
		long before = System.currentTimeMillis();
		double[] data = compute(2_010, 2, 23, 12, 0, 0);
		long after = System.currentTimeMillis();
		System.out.println("Sun Declination : " + GeomUtil.decToSex(data[0], GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN));
		System.out.println("Sun GHA:        : " + GeomUtil.decToSex(data[1], GeomUtil.SWING, GeomUtil.NONE));
		System.out.println("Aries GHA:      : " + GeomUtil.decToSex(data[2], GeomUtil.SWING, GeomUtil.NONE));
		int m = (int) Math.floor(Math.abs(data[3]) * 60d);
		int s = (int) Math.round(3_600d * Math.abs(data[3]) - (m * 60d));
		System.out.println("Equation of Time: " + (data[3] > 0 ? "+" : "-") + Integer.toString(m) + "m" + Integer.toString(s) + "s");
		System.out.println("Sun sd          : " + df2.format(data[4]) + "'");
		System.out.println("Sun hp          : " + df2.format(data[5]) + "'");
		System.out.println("------------------");
		System.out.println("Computed in " + Long.toString(after - before) + " ms.");
	}
}
