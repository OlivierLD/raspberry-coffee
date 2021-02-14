package util;

import calc.GreatCirclePoint;

public final class MercatorUtil {

	public MercatorUtil() {
	}

	/**
	 * Computes the Increasing Latitude. Mercator formula.
	 *
	 * @param lat in degrees
	 * @return Increasing Latitude, in degrees.
	 */
	public static double getIncLat(double lat) {
//		double angle = (Math.PI / 4D) + (Math.toRadians(lat) / 2D);
//		double tan = Math.tan(angle);
		double il = Math.log(Math.tan((Math.PI / 4D) + (Math.toRadians(lat) / 2D)));
		return Math.toDegrees(il);
	}

	public static double getInvIncLat(double il) {
		double ret = Math.toRadians(il);
		ret = Math.exp(ret);
		ret = Math.atan(ret);
		ret -= (Math.PI / 4d); // 0.78539816339744828D;
		ret *= 2;
		ret = Math.toDegrees(ret);
		return ret;
	}

	public static GreatCirclePoint deadReckoning(GreatCirclePoint p, double d, double r) {
		return deadReckoning(p.getL(), p.getG(), d, r);
	}

	public static GreatCirclePoint deadReckoning(double l, double g, double d, double r) {
		double deltaL = (d / 60D) * Math.cos(Math.toRadians(r));
		double l2 = l + deltaL;
		double lc1 = getIncLat(l);
		double lc2 = getIncLat(l2);
		double deltaLc = lc2 - lc1;
		double deltaG = deltaLc * Math.tan(Math.toRadians(r));
		double g2 = g + deltaG;
		return new GreatCirclePoint(l2, g2);
	}

	// Ratio on *one* degree, that is the trick.
	public static double getIncLatRatio(double lat) {
		if (lat == 0d)
			return 1d;
		else {
			double bottom = lat - 1d;
			if (bottom < 0d) bottom = 0d;
			return ((lat - bottom) / (getIncLat(lat) - getIncLat(bottom)));
		}
	}
}
