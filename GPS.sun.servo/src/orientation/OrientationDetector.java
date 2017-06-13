package orientation;

import calculation.AstroComputer;
import calculation.SightReductionUtil;
import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import java.util.Calendar;
import java.util.TimeZone;
import user.util.GeomUtil;

/**
 * System variables:
 *
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 *
 * or GPS...
 */
public class OrientationDetector {

	private static double latitude = 0D;
	private static double longitude = 0D;

	private static boolean keepWorking = true;

	private static double he = 0D, z = 0D;

	private static void getSunData(double lat, double lng) {
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		AstroComputer.setDateTime(current.get(Calendar.YEAR),
						current.get(Calendar.MONTH) + 1,
						current.get(Calendar.DAY_OF_MONTH),
						current.get(Calendar.HOUR_OF_DAY),
						current.get(Calendar.MINUTE),
						current.get(Calendar.SECOND));
		AstroComputer.calculate();
		SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
						AstroComputer.getSunDecl(),
						lat,
						lng);
		sru.calculate();
		he = sru.getHe().doubleValue();
		z = sru.getZ().doubleValue();
	}

	public static void main(String... args) {
		String strLat = System.getProperty("latitude");
		if (strLat != null) {
			try {
				latitude = Double.parseDouble(strLat);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strLong = System.getProperty("longitude");
		if (strLong != null) {
			try {
				longitude = Double.parseDouble(strLong);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}

		System.out.println(String.format("Position %s / %s",
						GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW)));

		final LSM303 sensor;
		final LSM303Listener orientationListener;

		try {
			sensor = new LSM303();
			orientationListener = new LSM303Listener() {
				@Override
				public void dataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {
					System.out.println(String.format("Heading %01f, Pitch %.01f", heading, pitch));
					// Compare pitch and heading with He and Z
					// Pitch = 0 => He = 90. Pitch = -90 => He = 0
					double pitchDiff = (he - 90D) - pitch;
					if (pitchDiff > 1) {
						System.out.println("Higher!!");
					} else if (pitchDiff < -1) {
						System.out.println("Lower!!");
					}
					// Todo heading
				}

				@Override
				public void close() {
					super.close();
				}
			};
			sensor.setDataListener(orientationListener);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				synchronized (sensor) {
					sensor.setKeepReading(false);
					orientationListener.close();
					keepWorking = false;
					try {
						Thread.sleep(1_500L);
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
			}));

			System.out.println("Start listening to the LSM303");
			sensor.startReading();

		} catch (Throwable ex) {
			System.err.println(">>> OrientationDetector... <<<");
			ex.printStackTrace();
//		System.exit(1);
		}

		Thread timeThread = new Thread(() -> {
			while (keepWorking) {
				getSunData(latitude, longitude);
				System.out.println(String.format("From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
								GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
								GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
								he,
								z));
				try { Thread.sleep(1_000L); } catch (Exception ex) {}
			}
			System.out.println("Timer done.");
		});
		timeThread.start();
	}
}
