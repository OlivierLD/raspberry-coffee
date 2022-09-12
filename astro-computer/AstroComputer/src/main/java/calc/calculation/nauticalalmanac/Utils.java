package calc.calculation.nauticalalmanac;

public class Utils {
	public static double sind(double x) {
		return Math.sin(Math.toRadians(x));
	}

	public static double cosd(double x) {
		return Math.cos(Math.toRadians(x));
	}

	public static double tand(double x) {
		return Math.tan(Math.toRadians(x));
	}

	public static double trunc(double x) {
		return 360 * (x / 360 - Math.floor(x / 360));
	}

	public static double trunc2(double x) {
		return (2D * Math.PI) *
				(x / (2D * Math.PI) - Math.floor(x / (2D * Math.PI)));
	}

	public static double cost(double x) {
		return Math.cos(Utils.trunc2(x));
	}
}
