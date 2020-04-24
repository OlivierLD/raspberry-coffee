package oliv.moon;

// http://master.grad.hr/hdgg/kog_stranica/kog18/06myers-KoG18.pdf
public class MoonTilt {

	// All angles in Radians
	static double zSun = Math.toRadians(94d);
	static double zMoon = Math.toRadians(136d);
	static double elevSun = Math.toRadians(24d);
	static double elevMoon = Math.toRadians(34d);

	public static void main(String... args) {
		double deltaZ = zSun - zMoon;

		double tanAlpha =
				((Math.cos(elevSun) * Math.tan(elevMoon)) - (Math.sin(elevMoon) * Math.cos(deltaZ))) / (Math.sin(deltaZ));
		double alpha = Math.atan(tanAlpha);
		System.out.println(String.format("Tilt: %.03f\272 (from horizontal)", Math.toDegrees(alpha)));
	}

}
