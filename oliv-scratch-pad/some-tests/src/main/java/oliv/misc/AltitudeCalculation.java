package oliv.misc;

public class AltitudeCalculation {

	private static double standardSeaLevelPressure = 101382d;

	public static float altitude(float pressure, float temperature) {
		double altitude = 0.0;
		if (standardSeaLevelPressure != 0) {
			altitude = ((Math.pow(standardSeaLevelPressure / pressure, 1 / 5.257)  - 1) * (temperature + 273.25)) / 0.0065;
		}
		return (float)altitude;
	}

	public static void main(String... args) {
		System.out.println(String.format("Altitude %f m", altitude(101380f, 21.3f)));
	}
}
