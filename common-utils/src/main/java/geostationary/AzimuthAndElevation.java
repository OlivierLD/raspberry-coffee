package geostationary;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Calculate azimuth, elevation and tilt, to orient an antenna toward a GeoStationary satellite
 *
 * See http://www.csgnetwork.com/geosatposcalc.html
 * See http://www.dishpointer.com/
 */
public class AzimuthAndElevation {

	public enum Satellite {

		ASIA_PACIFIC("I-4 F1 Asia-Pacific", 143.5),
		EMEA("I-4 F2 EMEA (Europe, Middle East and Africa)", 63.0),
		AMERICAS("I-4 F3 Americas", -97.6), // -98.4),
		ALPHASAT("Alphasat", 24.9);

		private double longitude;
		private String satName;
		Satellite(String satName, double longitude) {
			this.satName = satName;
			this.longitude = longitude;
		}

		public String satName() { return this.satName; }
		public double longitude() { return this.longitude; }
	}

	private static double azimuth(double satLong, double earthStationLat, double earthStationLong) {
		double deltaG = Math.toRadians(earthStationLong - satLong);
		double earthStationAzimuth = 180 + Math.toDegrees(Math.atan(Math.tan(deltaG) / Math.sin((Math.toRadians(earthStationLat)))));
		if (earthStationLat < 0) {
			earthStationAzimuth -= 180;
		}
		if (earthStationAzimuth < 0) {
			earthStationAzimuth += 360.0;
		}
		return earthStationAzimuth;
	}

	private final static double R1 = 1D + 35_786D / 6_378.16;

	private static double elevation(double satLong, double earthStationLat, double earthStationLong) {
		double deltaG = Math.toRadians(earthStationLong - satLong);

		double latRad = Math.toRadians(earthStationLat);
		double v1 = R1 * Math.cos(latRad) * Math.cos(deltaG) - 1;
		double v2 = R1 * Math.sqrt(1 - Math.cos(latRad) * Math.cos(latRad) * Math.cos(deltaG) * Math.cos(deltaG));
		return Math.toDegrees(Math.atan(v1/v2)); // earthStationElevation
	}

	private static double tilt(double satLong, double earthStationLat, double earthStationLong) {
		double deltaG = Math.toRadians(earthStationLong - satLong);
		double latRad = Math.toRadians(earthStationLat);
		return (Math.toDegrees(Math.atan(Math.sin(deltaG) / Math.tan(latRad))));
	}

	public static Result aim(Satellite target, double fromLDegrees, double fromGDegrees) {
		double zDeg  = azimuth(target.longitude(), fromLDegrees, fromGDegrees);
		double elDeg = elevation(target.longitude(), fromLDegrees, fromGDegrees);
		double tilt  = tilt(target.longitude(), fromLDegrees, fromGDegrees);
		return new Result(zDeg, elDeg, tilt);
	}

	public static class Result {
		private final double zDegrees;
		private final double elevDegrees;
		private final double tilt;
		public Result(double z, double el, double tilt) {
			this.zDegrees    = z;
			this.elevDegrees = el;
			this.tilt        = tilt;
		}
	}

	public static void main(String... args) {
		// 2010 48th Ave, SF
		double lat =   37.7489;
		double lng = -122.5070;

		Satellite toUse = null;
		double maxAlt = - Double.MAX_VALUE;
		for (Satellite toAim : Satellite.values()) {
	//	Satellite toAim = Satellite.ASIA_PACIFIC;
			Result result = aim(toAim, lat, lng);
			NumberFormat NF = new DecimalFormat("##0.0");
			System.out.println(String.format("%s: Z: %s\272 (true), el: %s\272, tilt: %s\272", toAim.satName(), NF.format(result.zDegrees), NF.format(result.elevDegrees), NF.format(result.tilt)));
			if (result.elevDegrees > maxAlt) {
				maxAlt = result.elevDegrees;
				toUse = toAim;
			}
		}
		System.out.println(String.format("\nuse %s", (toUse != null ? toUse.satName() : "???")));
	}
}
