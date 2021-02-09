package oliv.moon;

// http://master.grad.hr/hdgg/kog_stranica/kog18/06myers-KoG18.pdf
public class MoonTilt {

//	let zSun = 173.70;
//	let zMoon = 171.666;
//	let altSun = 27.036;
//	let altMoon = 27.09;

	static double zSunDeg = 208.318764;
	static double zMoonDeg = 232.899508;
	static double altSunDeg = 32.783581;
	static double altMoonDeg = 5.074876;

	// Expect 48.423

	// All angles in Radians
	static double zSun = Math.toRadians(zSunDeg);     //  94d);
	static double zMoon = Math.toRadians(zMoonDeg);    // 136d);
	static double elevSun = Math.toRadians(altSunDeg);   //  24d);
	static double elevMoon = Math.toRadians(altMoonDeg); //  34d);

	public static void main(String... args) {

		double deltaZ, alpha;
		if (false) {
			// V1, like a test...

			deltaZ = zSun - zMoon;

			double tanAlpha =
					((Math.cos(elevSun) * Math.tan(elevMoon)) - (Math.sin(elevMoon) * Math.cos(deltaZ))) / (Math.sin(deltaZ));
			alpha = Math.atan(tanAlpha);
			System.out.println(String.format("V1 - Tilt: %.03f\272 (from horizontal)", Math.toDegrees(alpha)));
		}
		// V2 (From JS implementation)
		// Take the first triangle of the moon-to-sun great circle, from the Moon.
		// TODO Fix that shit!
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
