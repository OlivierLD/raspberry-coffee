package orientation;

import calculation.AstroComputer;
import calculation.SightReductionUtil;
import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import i2c.servo.pwm.PCA9685;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.TimeZone;
import user.util.GeomUtil;

/**
 * The job of this one is to point North, and keep pointing to it.
 * You have an LSM303 attached to a standard servo.
 *
 * In addition, this one <b>calculates</b> the Sun elevation and orients a second servo accordingly.
 *
 * System variables:
 *
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 * declination -Ddeclination=14
 * tolerance -Dtolerance=1
 *
 * TODO ANSI console
 *
 * or GPS... (later).
 */
public class PanelOrienterV1 {

	private static double declination = 14D; // E+, W-
	private static int targetWindow = 1;

	private static double latitude = 0D;
	private static double longitude = 0D;

	private static boolean keepWorking = true;

	private static double he = 0D, z = 0D;

	private static boolean orientationVerbose = false;
	private static boolean astroVerbose = false;
	private static boolean servoVerbose = false;

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

	private static int servoHeading = 14;
	private static int servoTilt    = 15;

	private static int currentServoAngle = 0;
	private static boolean invert = false;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private static int freq = 60;

	private PCA9685 servoBoard = null;
	private static boolean calibrating = false;

	private static void setCalibrating(boolean b) {
		calibrating = b;
	}
	private static boolean isCalibrating() {
		return calibrating;
	}

	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	private static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retString;
	}
	public PanelOrienterV1() {
		try {
			System.out.println("Driving Servos on Channels " + servoHeading + " and " + servoTilt);
			this.servoBoard = new PCA9685();
			this.servoBoard.setPWMFreq(freq); // Set frequency in Hz

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		if (servoVerbose) {
			System.out.println(String.format("Servo %d, angle %.02f\272, pwm: %d", servo, f, pwm));
		}
		try {
			servoBoard.setPWM(servo, 0, pwm);
		} catch (IllegalArgumentException iae) {
			System.err.println(String.format("Cannot set servo %d to PWM %d", servo, pwm));
			iae.printStackTrace();
		}
	}

	public void stop(int servo) { // Set to 0
		servoBoard.setPWM(servo, 0, 0);
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}

	private static float invertHeading(float h) {
		float inverted = 0f;
		if (h < 0) {
			inverted = h + 180f;
		} else {
			inverted = h - 180f;
		}
		return inverted;
	}


	public static void main(String... args) {

		servoHeading = 14;
		servoTilt = 15;
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

		// Read System Properties
		orientationVerbose = "true".equals(System.getProperty("orient.verbose", "false"));
		servoVerbose = "true".equals(System.getProperty("servo.verbose", "false"));
		astroVerbose = "true".equals(System.getProperty("astro.verbose", "false"));

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
		String strDec = System.getProperty("declination");
		if (strDec != null) {
			try {
				declination = Double.parseDouble(strDec);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strTol = System.getProperty("tolerance", "1");
		if (strTol != null) {
			try {
				targetWindow = Integer.parseInt(strTol);
				if (targetWindow < 1) {
					throw new IllegalArgumentException("Tolerance must be an int, greater than 1");
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}

		System.out.println("----------------------------------------------");
		System.out.println(String.format("Position %s / %s, Mag Decl. %.01f, tolerance %d\272 each way. Heading servo: %d, Tilt servo: %d",
						GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
						declination,
						targetWindow,
						servoHeading,
						servoTilt));
		System.out.println("----------------------------------------------");

		final LSM303 sensor;
		final LSM303Listener orientationListener;

		final PanelOrienterV1 instance = new PanelOrienterV1();
		// Set to 0
		instance.setAngle(servoHeading, (float)currentServoAngle);
		instance.setAngle(servoTilt, 0f);

		setCalibrating(false);
		boolean withTest = false; // PRM
		String resp = userInput("Test servos? [Y] > ");
		if ("Y".equalsIgnoreCase(resp)) {
			withTest = true;
		}
		if (withTest) {
			instance.setAngle(servoHeading, -90f);
			instance.setAngle(servoTilt, -90f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setAngle(servoHeading, 90f);
			instance.setAngle(servoTilt, 90f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setAngle(servoHeading, (float)currentServoAngle);
			instance.setAngle(servoTilt, 0f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			System.out.println("Test done.");
		}

		try {
			sensor = new LSM303();
			orientationListener = new LSM303Listener() {
				@Override
				public void dataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {

					if (invert) {
						heading += 180;
						while (heading > 360) {
							heading -= 360;
						}
					}

					// Heading
					double headingDiff = z - (heading + declination);
					if (headingDiff < -180) {
						headingDiff += 360D;
					}
					String headingMessage = "Heading OK";
					int delta = 0;
					if (headingDiff > targetWindow) {
						headingMessage = String.format("Target is on the right by %.02f\272", Math.abs(headingDiff));
						delta = -1; 
					} else if (headingDiff < -targetWindow) {
						headingMessage = String.format("Target is on the left by %.02f\272", Math.abs(headingDiff));
						delta = 1;
					}
					if (orientationVerbose) {
						System.out.println(String.format(
										"Board orientation (LSM303 listener): Heading %.01f (mag), %.01f (true), Target Z: %.01f, %s %s servo-angle: %d",
										heading,
										(heading + declination),
										z,
										headingMessage,
										(invert ? String.format("(inverted to %.02f)", invertHeading((float) currentServoAngle)) : ""),
										currentServoAngle));
					}
					// Drive servo accordingly, to point to Z.
					if (delta != 0 && !isCalibrating()) {

						// TODO: Here, orient BOTH servos, with or without invert.

						if (false) { // Go step-by-step
							currentServoAngle += delta;
						} else { // All at once
							currentServoAngle = (int) -(z - 180);
						}
						// If out of [-90..90], invert.
						if (currentServoAngle < -90 || currentServoAngle > 90) {
							invert = true;
						} else {
							invert = false;
						}
						if (orientationVerbose) {
							System.out.println(String.format(">>> Setting servo #%d to %d %s", servoHeading, currentServoAngle, (invert ? String.format("(inverted to %.02f)", invertHeading((float) currentServoAngle)) : "")));
						}
						instance.setAngle(servoHeading, invert ? invertHeading((float) currentServoAngle) : (float) currentServoAngle);
					}
				}

				@Override
				public void close() {
					super.close();
				}
			};
			sensor.setDataListener(orientationListener);

			// TODO Point LSM303 to the lower pole: S if you are in the North hemisphere, N if you are in the South hemisphere.
			System.out.println("Point the LSM303 to the South, hit [Return] when ready.");
			z = 180;
			setCalibrating(true);
			userInput("");
			setCalibrating(false);
			// Done calibrating

			Thread timeThread = new Thread(() -> {
				int previous = 0;
				while (keepWorking) {
					// Sun position calculation geos here
					getSunData(latitude, longitude);
					if (he > 0) { // Daytime
						if (astroVerbose) {
							System.out.println(String.format("From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
											GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
											GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
											he,
											z));
						}
						int angle = (int)Math.round(90 - he);
						if (angle != previous) {
							System.out.println(String.format(">>> Tilt servo angle now: %d %s", angle, (invert ? "(inverted)" : "")));
							instance.setAngle(servoTilt, invert ? (float) -angle : (float) angle);
							previous = angle;
						}
					} else { // Night time
						System.out.println("Night time, parked...");
						int angle = 0;
						if (angle != previous) {
							instance.setAngle(servoTilt, (float) angle);
							previous = angle;
						}
					}
					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
				System.out.println("Timer done.");
			});
			System.out.println("Starting the timer loop");
			timeThread.start();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				instance.stop(servoHeading);
				instance.stop(servoTilt);
				synchronized (sensor) {
					sensor.setKeepReading(false);
					orientationListener.close();
					try {
						Thread.sleep(1_500L);
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
				instance.setAngle(servoHeading, 0f);
				instance.setAngle(servoTilt, 0f);
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
			}));

			System.out.println("Start listening to the LSM303");
			sensor.startReading();

		} catch (Throwable ex) {
			System.err.println(">>> OrientationDetector... <<< BAM!");
			ex.printStackTrace();
//		System.exit(1);
		}
	}
}
