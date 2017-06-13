package orientation;

import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
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

		try {
			LSM303 sensor = new LSM303();
			LSM303Listener orientationListener = new LSM303Listener() {
				@Override
				public void dataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {
					super.dataDetected(accX, accY, accZ, magX, magY, magZ, heading, pitch, roll);
					// TODO Implement
					System.out.println(String.format("Heading %01f, Pitch %.01f", heading, pitch));
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
					try {
						Thread.sleep(1_000L);
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
			}));

			if (sensor != null) {
				System.out.println("Start listening to the LSM303");
				sensor.startReading();
			} else {
				System.out.println("Check your sensor...");
			}
		} catch (Throwable ex) {
			System.err.println("OrientationDetector...");
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
