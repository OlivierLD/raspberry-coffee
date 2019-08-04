package orientation;

import ansi.EscapeSeq;
import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import org.fusesource.jansi.AnsiConsole;

import static utils.StaticUtil.userInput;

/**
 * This is an implementation example:
 * It shows how to use the panel orienter (SunFlower)
 * in addition with a LSM303 for the device heading.
 *
 * Along the same lines, position can be provided, from a GPS for example.
 * And instead of using an LSM303, the heading could come from an NMEA Station...
 *
 * The "working" class is {@link SunFlower}.
 */
public class SimplePanelOrienter {

	private static double declination = 14d;

	public static void main(String... args) {

		int servoHeading = 14;
		int servoTilt = 15;

		boolean ansiConsole = "true".equals(System.getProperty("ansi.console", "false"));

		// Supported parameters --heading:14 --tilt:15
		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith("--heading:")) {
					try {
						servoHeading = Integer.parseInt(prm.substring("--heading:".length()));
					} catch (Exception e) {
						throw e;
					}
				} else if (prm.startsWith("--tilt:")) {
					try {
						servoTilt = Integer.parseInt(prm.substring("--tilt:".length()));
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}

		SunFlower instance = new SunFlower(new int[] { servoHeading }, new int[] { servoTilt });

		String strLat = System.getProperty("default.sf.latitude");
		if (strLat != null) {
			try {
				instance.setLatitude(Double.parseDouble(strLat));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strLong = System.getProperty("default.sf.longitude");
		if (strLong != null) {
			try {
				instance.setLongitude(Double.parseDouble(strLong));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strDec = System.getProperty("declination", "14");
		try {
			declination = Double.parseDouble(strDec);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		final LSM303 sensor;
		final LSM303Listener lsm303Listener;

		// Set to 0
		instance.servosZero();
		instance.setCalibrating(false);

		try {
			sensor = new LSM303(); // TODO Calibration parameters!
			lsm303Listener = new LSM303Listener() {
				@Override
				public void dataDetected(double accX, double accY, double accZ, double magX, double magY, double magZ, double heading, double pitch, double roll) {
					instance.setDeviceHeading(heading + declination);
				}

				@Override
				public void close() {
					super.close();
				}
			};
			sensor.setDataListener(lsm303Listener);

			// Point the device to the lower pole: S if you are in the North hemisphere, N if you are in the South hemisphere.
			String mess = String.format("Point the Device to the true %s, hit [Return] when ready.", instance.getLatitude() > 0 ? "South" : "North");

			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + EscapeSeq.ANSI_REVERSE + mess +  EscapeSeq.ANSI_ERASE_TO_EOL);
			} else {
				System.out.println(mess);
			}

			instance.setCalibrating(true);
			userInput("");
			if (ansiConsole) { // Cleanup
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + EscapeSeq.ANSI_ERASE_TO_EOL);
			}
			instance.setCalibrating(false);
			// Done calibrating
			instance.setDeviceHeading(instance.getLatitude() > 0 ? 180 : 0);

			instance.startWorking();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				instance.stopWorking();
				instance.stopHeadingServo();
				instance.stopTiltServo();
				synchronized (sensor) {
					sensor.setKeepReading(false);
					lsm303Listener.close();
					try {
						Thread.sleep(1_500L);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
				instance.setHeadingServoAngle(0f);
				instance.setTiltServoAngle(0f);
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				if (ansiConsole) {
					AnsiConsole.systemUninstall();
				}
			}));

			mess = "Start listening to the LSM303";
			if (ansiConsole){
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + mess + EscapeSeq.ANSI_ERASE_TO_EOL);
			} else {
				System.out.println(mess);
			}
			sensor.startReading();

		} catch (Throwable ex) {
			System.err.println(">>> Panel Orienter... <<< BAM!");
			ex.printStackTrace();
//		System.exit(1);
		}
	}
}
