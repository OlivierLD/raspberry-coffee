package oliv.moon;

// http://master.grad.hr/hdgg/kog_stranica/kog18/06myers-KoG18.pdf
public class MoonTilt {

	static double zSunDeg = 121.96;
	static double zMoonDeg = 161.82;
	static double altSunDeg = 14.3;
	static double altMoonDeg = 25.033;

	// All angles in Radians
	static double zSun = Math.toRadians(zSunDeg);     //  94d);
	static double zMoon = Math.toRadians(zMoonDeg);    // 136d);
	static double elevSun = Math.toRadians(altSunDeg);   //  24d);
	static double elevMoon = Math.toRadians(altMoonDeg); //  34d);

	public static void main(String... args) {

		// V1, like a test...

		double deltaZ = zSun - zMoon;

		double tanAlpha =
				((Math.cos(elevSun) * Math.tan(elevMoon)) - (Math.sin(elevMoon) * Math.cos(deltaZ))) / (Math.sin(deltaZ));
		double alpha = Math.atan(tanAlpha);
		System.out.println(String.format("V1 - Tilt: %.03f\272 (from horizontal)", Math.toDegrees(alpha)));

		// V2 (From JS implementation)
		// Take the first triangle, from the Moon.
		deltaZ = zMoonDeg - zSunDeg; // - zMoonDeg;
		if (deltaZ > 180) { // like 358 - 2, should be 358 - 362.
			deltaZ -= 360;
		}
		double deltaElev = altMoonDeg - altSunDeg; //  - altMoonDeg;
		alpha = Math.toDegrees(Math.atan2(deltaElev, deltaZ)); // atan2 from -Pi to Pi
		if (deltaElev > 0) {
			if (deltaZ > 0) { // positive angle, like 52
				alpha *= -1;
			} else { // Angle > 90, like 116
				if (alpha < 90) {
					alpha -= 90;
				} else {
					alpha = 180 - alpha;
				}
			}
		} else {
			if (deltaZ > 0) { // negative angle, like -52
				alpha *= -1;
			} else { // Negative, < -90, like -116
				// alpha += 90; // TODO Tweak that like above too
				if (alpha > -90) {
					alpha += 90;
				} else {
					alpha = - 180 - alpha;
				}
			}
		}
		System.out.println(String.format("V2 - Tilt: %.03f\272 (from horizontal)", alpha)); // should be 55
	}

}
