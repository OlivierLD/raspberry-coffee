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
import org.fusesource.jansi.AnsiConsole;
import ansi.EscapeSeq;
import user.util.GeomUtil;

/**
 * The job of this one is to point North, and keep pointing to it.
 * You have an LSM303 attached to a standard servo.
 * Servos are driven by a PCA9685 board.
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
 * ansi console -Dansi.console=true
 * Manual Sun position entry -Dmanual.entry=true
 *
 * or GPS input for the position... (later).
 */
public class PanelOrienterV1 {

	private static PanelOrienterV1 instance = null;

	private static double declination = 14D; // E+, W-
	private static int targetWindow = 1;

	private static double latitude = 0D;
	private static double longitude = 0D;

	private static boolean keepWorking = true;

	private static double he = 0D, z = 0D;

	private static boolean orientationVerbose = false;
	private static boolean astroVerbose = false;
	private static boolean servoVerbose = false;
	private static boolean testServos = false;

	private static boolean manualEntry = false;

	private static boolean ansiConsole = true;
	private final static String PAD = EscapeSeq.ANSI_ERASE_TO_EOL;

	private static void getSunData(double lat, double lng) {
		if (manualEntry) {
			System.out.println("Enter [q] at the prompt to quit");
			String strZ = userInput(String.format("\nZ (0..360) now %.02f  > ", z));
			if ("Q".equalsIgnoreCase(strZ)) {
				manualEntry = false;
				invert = false;
				currentHeadingServoAngle = 0;
				previousTiltAngle = 0;
				servosZero();
			} else {
				if (strZ.trim().length() > 0) {
					try {
						z = Double.parseDouble(strZ);
						if (z < 0 || z > 360) {
							System.err.println("Between 0 and 360, please.");
							z = 0;
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						return;
					}
				}
				String strHe = userInput(String.format("He (-90..90) now %.02f > ", he));
				if ("Q".equalsIgnoreCase(strHe)) {
					manualEntry = false;
					invert = false;
					currentHeadingServoAngle = 0;
					previousTiltAngle = 0;
					servosZero();
				} else {
					if (strHe.trim().length() > 0) {
						try {
							he = Double.parseDouble(strHe);
							if (he < -90 || he > 90) {
								System.err.println("Between -90 and 90, please.");
								he = 90;
							}
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
							return;
						}
					}
				}
			}
			// Return here
		} else {
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
	}

	private static int servoHeading = 14;
	private static int servoTilt    = 15;

	private static int currentHeadingServoAngle = 0;
	private static int previousTiltAngle = 0;

	private static boolean invert = false; // Used when the angle for the servoHeading is lower than -90 or greater than +90

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
//		System.out.println("Driving Servos on Channels " + servoHeading + " and " + servoTilt);
			this.servoBoard = new PCA9685();
			this.servoBoard.setPWMFreq(freq); // Set frequency in Hz
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		if (servoVerbose && !manualEntry) {
			String mess = String.format("Servo %d, angle %.02f\272, pwm: %d", servo, f, pwm);
			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, (servo == servoHeading ? 8 : 9)) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
			} else {
				System.out.println(mess);
			}
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

	private static void servosZero() {
		if (instance != null) {
			instance.setAngle(servoHeading, 0f);
			instance.setAngle(servoTilt, 0f);
		}
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

		manualEntry = "true".equals(System.getProperty("manual.entry", "false"));
		ansiConsole = "true".equals(System.getProperty("ansi.console", "false"));

		testServos = "true".equals(System.getProperty("test.servos", "false"));

		if (manualEntry && ansiConsole) {
			System.out.println("Manual Entry and ANSI COnsole are mutually exclusive. Please choose one, and only one... Thank you.");
			System.exit(1);
		}

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

		final LSM303 sensor;
		final LSM303Listener orientationListener;

		instance = new PanelOrienterV1();
		// Set to 0
		servosZero();

		setCalibrating(false);

		if (testServos) {
			instance.setAngle(servoHeading, -90f);
			instance.setAngle(servoTilt, -90f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setAngle(servoHeading, 90f);
			instance.setAngle(servoTilt, 90f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setAngle(servoHeading, (float) currentHeadingServoAngle);
			instance.setAngle(servoTilt, 0f);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			System.out.println("Test done.");
		}

		if (ansiConsole) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + "Driving Servos toward the Sun");
		}
		String mess = String.format("Position %s / %s, Mag Decl. %.01f, tolerance %d\272 each way. Heading servo: %d, Tilt servo: %d",
						GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
						declination,
						targetWindow,
						servoHeading,
						servoTilt);
		if (ansiConsole) {
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
		} else {
			System.out.println("----------------------------------------------");
			System.out.println(mess);
			System.out.println("----------------------------------------------");
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
					if (isCalibrating() || (orientationVerbose && !manualEntry)) {
						String mess = String.format(
										"Board orientation (LSM303 listener): Heading %.01f (mag), %.01f (true), Target Z: %.01f, servo-angle: %d %s %s ",
										heading,
										(heading + declination),
										z,
										currentHeadingServoAngle,
										headingMessage,
										(invert ? String.format("(inverted to %.02f)", invertHeading((float) currentHeadingServoAngle)) : ""));
						if (ansiConsole) {
							AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 3) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
						} else {
							System.out.println(mess);
						}
					}
					// Drive servo accordingly, to point to Z.
					if (delta != 0 && !isCalibrating()) {

						// Here, orient BOTH servos, with or without invert.

						if (false) { // Go step-by-step
							currentHeadingServoAngle += delta;
						} else { // All at once
							currentHeadingServoAngle = (int) -(z - 180);
						}

						/*
						 * If out of [-90..90], invert.
						 */
						if (currentHeadingServoAngle < -90 || currentHeadingServoAngle > 90) {
							invert = true;
						} else {
							invert = false;
						}

						if (he > 0) { // Daytime
							if (astroVerbose && !manualEntry) {
								String mess = String.format("From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
												GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
												he,
												z);
								if (ansiConsole) {
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 4) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
								} else {
									System.out.println(mess);
								}
							}
							int angle = (int)Math.round(90 - he);
							if (invert) {
								angle = -angle;
							}
							if (angle != previousTiltAngle) {
								if (servoVerbose && !manualEntry) {
									String mess = String.format(">>> Tilt servo angle now: %d %s", angle, (invert ? "(inverted)" : ""));
									if (ansiConsole) {
										AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 5) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
									} else {
										System.out.println(mess);
									}
								}
								instance.setAngle(servoTilt, (float) angle);
								previousTiltAngle = angle;
							}
						} else { // Night time
							invert = false;
							if (servoVerbose && !manualEntry) {
								String mess = "Night time, parked...           ";
								if (ansiConsole) {
									AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 4) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
								} else {
									System.out.println(mess);
								}
							}
							int angle = 0;
							if (angle != previousTiltAngle) {
								instance.setAngle(servoTilt, (float) angle);
								previousTiltAngle = angle;
							}
							currentHeadingServoAngle = 0;
						}
						if (orientationVerbose && !manualEntry) {
							String mess = String.format(">>> Heading servo angle now %d %s", currentHeadingServoAngle, (invert ? String.format("(inverted to %.02f)", invertHeading((float) currentHeadingServoAngle)) : ""));
							if (ansiConsole) {
								AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 6) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
							} else {
								System.out.println(mess);
							}
						}
						instance.setAngle(servoHeading, invert ? invertHeading((float) currentHeadingServoAngle) : (float) currentHeadingServoAngle);
					}
				}

				@Override
				public void close() {
					super.close();
				}
			};
			sensor.setDataListener(orientationListener);

			// TODO Point LSM303 to the lower pole: S if you are in the North hemisphere, N if you are in the South hemisphere.
			// TODO Tropical zone case
			mess = "Point the LSM303 to the South, hit [Return] when ready.";

			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + EscapeSeq.ANSI_REVERSE + mess + PAD);
			} else {
				System.out.println(mess);
			}

			z = 180;
			setCalibrating(true);
			userInput("");
			if (ansiConsole) { // Cleanup
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + PAD);
			}
			setCalibrating(false);
			// Done calibrating

			Thread timeThread = new Thread(() -> {
//				int previous = 0;
				while (keepWorking) {
					// Sun position calculation geos here
					getSunData(latitude, longitude);
					try { Thread.sleep(1_000L); } catch (Exception ex) {}
				}
				System.out.println("Timer done.");
			});
			mess = "Starting the timer loop";
			if (ansiConsole){
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + mess + PAD);
			} else {
				System.out.println(mess);
			}
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
				if (ansiConsole) {
					AnsiConsole.systemUninstall();
				}
			}));

			mess = "Start listening to the LSM303";
			if (ansiConsole){
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15) + mess + PAD);
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
