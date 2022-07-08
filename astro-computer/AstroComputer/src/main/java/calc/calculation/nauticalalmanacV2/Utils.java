package calc.calculation.nauticalalmanacV2;

public class Utils {
	static double sind(double x) {
		return Math.sin(Math.toRadians(x));
	}

	static double cosd(double x) {
		return Math.cos(Math.toRadians(x));
	}

	static double tand(double x) {
		return Math.tan(Math.toRadians(x));
	}

	static double trunc(double x) {
		return 360 * (x / 360 - Math.floor(x / 360));
	}

	static double trunc2(double x) {
		return (2D * Math.PI) *
				(x / (2D * Math.PI) - Math.floor(x / (2D * Math.PI)));
	}

	static double cost(double x) {
		return Math.cos(Utils.trunc2(x));
	}
}
