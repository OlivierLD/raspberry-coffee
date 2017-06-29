package orientation;

import ansi.EscapeSeq;
import calculation.AstroComputer;
import calculation.SightReductionUtil;
import i2c.servo.pwm.PCA9685;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.fusesource.jansi.AnsiConsole;
import user.util.GeomUtil;

/**
 * Servos are driven by a PCA9685 board.
 *
 * In addition, <b>calculates</b> the Sun elevation and orients 2 servos accordingly.
 *
 * System variables:
 * ansi console -Dansi.console=true
 * Manual Sun position entry -Dmanual.entry=true
 * -Dorient.verbose=true
 * -Dastro.verbose=true
 * -Dservo.verbose=true
 *
 * -Dtilt.servo.sign=-1
 * -Dheading.servo.sign=-1
 * -Dtilt.limit=0..90 <- Minimum elevation of the sun
 * -Dtilt.offset=0    <- Offset in degrees
 *
 * For the main, as an example:
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 * -Dtest.servos=true
 *
 * -Dsmooth.moves=true
 *
 * -Ddemo.mode=true
 * -Dfrom.date=2017-06-28T05:53:00
 * -Dto.date=2017-06-28T20:33:00
 */
public class SunFlower {

	private double latitude = 0D;
	private double longitude = 0D;

	private boolean keepWorking = true;

	private static double he = 0D, z = 0D;
	private static double deviceHeading = 0D;

	private static int tiltLimit = 0;
	private static int tiltOffset = 0;

	private int tiltServoSign = 1;
	private int headingServoSign = 1;

	private static boolean orientationVerbose = false;
	private static boolean astroVerbose = false;
	private static boolean servoVerbose = false;
	private static boolean testServos = false;
	private static boolean smoothMoves = false;
	private static boolean demo = false;

	private static boolean headingServoMoving = false;
	private static boolean tiltServoMoving = false;

	private static boolean manualEntry = false;
	private static boolean ansiConsole = false;
	private final static String PAD = EscapeSeq.ANSI_ERASE_TO_EOL;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
	private final static SimpleDateFormat SDF_INPUT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // Duration

	private static boolean foundPCA9685 = true;

	private void getSunData(double lat, double lng) {
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		getSunDataForDate(lat, lng, current);
	}

	private void getSunDataForDate(double lat, double lng, Calendar current) {
		if (manualEntry) {
			System.out.println("Enter [q] at the prompt to quit");
			String strZ = userInput(String.format("\nZ (0..360) now %.02f  > ", z));
			if ("Q".equalsIgnoreCase(strZ)) {
				manualEntry = false;
				invert = false;
				previousHeadingAngle = 0;
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
					previousHeadingAngle = 0;
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
	//	Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
			if (astroVerbose) {
				System.out.println(String.format(">>> Sun Calculation for %s", SDF.format(current.getTime())));
			}
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

	private static int headingServoID = 14;
	private static int tiltServoID = 15;

	private static int previousHeadingAngle = 0;
	private static int previousTiltAngle = 0;

	private static boolean invert = false; // Used when the angle for the headingServoID is lower than -90 or greater than +90

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private static int freq = 60;

	private PCA9685 servoBoard = null;
	private boolean calibrating = false;

	public void setCalibrating(boolean b) {
		calibrating = b;
	}
	private boolean isCalibrating() {
		return calibrating;
	}

	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retString;
	}

	public SunFlower(int headinServoNumber, int tiltServoNumber) {
		headingServoID = headinServoNumber;
		tiltServoID = tiltServoNumber;

		// Read System Properties
		orientationVerbose = "true".equals(System.getProperty("orient.verbose", "false"));
		servoVerbose = "true".equals(System.getProperty("servo.verbose", "false"));
		astroVerbose = "true".equals(System.getProperty("astro.verbose", "false"));

		manualEntry = "true".equals(System.getProperty("manual.entry", "false"));
		ansiConsole = "true".equals(System.getProperty("ansi.console", "false"));
		demo = "true".equals(System.getProperty("demo.mode", "false"));

		smoothMoves = "true".equals(System.getProperty("smooth.moves", "false"));

		String strTiltServoSign = System.getProperty("tilt.servo.sign");
		if (strTiltServoSign != null) {
			try {
				int sign = Integer.parseInt(strTiltServoSign);
				if (sign != 1 && sign != -1) {
					System.err.println("Only 1 or -1 are supported for tilt.servo.sign");
				} else {
					tiltServoSign = sign;
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		String strHeadingServoSign = System.getProperty("heading.servo.sign");
		if (strHeadingServoSign != null) {
			try {
				int sign = Integer.parseInt(strHeadingServoSign);
				if (sign != 1 && sign != -1) {
					System.err.println("Only 1 or -1 are supported for heading.servo.sign");
				} else {
					headingServoSign = sign;
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		try {
//		System.out.println("Driving Servos on Channels " + headingServoID + " and " + tiltServoID);
			this.servoBoard = new PCA9685();
			try {
				this.servoBoard.setPWMFreq(freq); // Set frequency in Hz
			} catch (NullPointerException npe) {
				foundPCA9685 = false;
				System.err.println("------------------------------------------------------------");
				System.err.println("PCA9685 was NOT initialized.\nCheck your wiring, or make sure you are on a Raspberry PI...");
				System.err.println("Moving on anyway...");
				System.err.println("------------------------------------------------------------");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private boolean noServoIsMoving() {
		return !(headingServoMoving || tiltServoMoving);
	}

	private static void setHeadingServoMoving(boolean b) {
		headingServoMoving = b;
	}
	public void setHeadingServoAngle(final float f) {
		System.out.println(String.format("H> Servo heading set required to %.02f (previous %d), moving:%s", f, previousHeadingAngle, (headingServoMoving?"yes":"no")));
		float startFrom = previousHeadingAngle;
		if (noServoIsMoving() /*!headingServoMoving*/ && smoothMoves && Math.abs(startFrom - f) > 5) {
			headingServoMoving = true;
			// Smooth move for steps > 5
			if (servoVerbose) {
				System.out.println(String.format("H> Start a smooth move from heading %.02f to %.02f", startFrom, f));
			}
			Thread smoothy = new Thread(() -> {
				System.out.println(String.format("H> Starting smooth thread for heading %.02f to %.02f", startFrom, f));
				int sign = (startFrom > f) ? -1 : 1;
				float pos = startFrom;
				while (Math.abs(pos - f) > 1) {
					System.out.println(String.format("H> Setting heading to %.02f, delta=%.02f", pos, Math.abs(pos - f)));
					setAngle(headingServoID, pos);
					pos += (sign * 1);
					try { Thread.sleep(10L); } catch (Exception ex) {}
				}
				System.out.println(String.format("H>...Heading thread done, delta=%.02f", Math.abs(pos - f)));
				setHeadingServoMoving(false);
			});
//		headingServoMoving = true; // TODO Remove?
			smoothy.start();
		} else {
			if (noServoIsMoving() /*!headingServoMoving*/) {
				System.out.println(String.format("H> Abrupt heading set to %.02f", f));
				setAngle(headingServoID, f);
			}
		}
	}

	private static void setTiltServoMoving(boolean b) {
		tiltServoMoving = b;
	}
	public void setTiltServoAngle(final float f) {
		System.out.println(String.format("T> Servo tilt set required to %.02f (previous %d), moving:%s", f, previousTiltAngle, (tiltServoMoving?"yes":"no")));
		float startFrom = previousTiltAngle;
		if (noServoIsMoving() /*!tiltServoMoving*/ && smoothMoves && Math.abs(startFrom - f) > 5) {
			tiltServoMoving = true;
			// Smooth move for steps > 5
			if (servoVerbose) {
				System.out.println(String.format("T> Start a smooth move from tilt %.02f to %.02f", startFrom, f));
			}
			Thread smoothy = new Thread(() -> {
				System.out.println(String.format("T> Starting smooth thread for tilt %.02f to %.02f", startFrom, f));
				int sign = (startFrom > f) ? -1 : 1;
				float pos = startFrom;
				while (Math.abs(pos - f) > 1) {
					System.out.println(String.format("T> Setting tilt to %.02f, delta=%.02f", pos, Math.abs(pos - f)));
					setAngle(tiltServoID, pos);
					pos += (sign * 1);
					try { Thread.sleep(10L); } catch (Exception ex) {}
				}
				System.out.println(String.format("T>...Tilt thread done, delta=%.02f", Math.abs(pos - f)));
				setTiltServoMoving(false);
			});
//		tiltServoMoving = true; // TODO Remove?
			smoothy.start();
		} else {
			if (noServoIsMoving() /*!tiltServoMoving*/) {
				System.out.println(String.format("T> Abrupt tilt set to %.02f", f));
				setAngle(tiltServoID, applyLimitAndOffset(f));
			}
		}
	}

	private void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		if (servoVerbose && !manualEntry) {
			String mess = String.format("Servo %d, angle %.02f\272, pwm: %d", servo, f, pwm);
			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, (servo == headingServoID ? 8 : 9)) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
			} else {
				System.out.println(mess);
			}
		}
		try {
			if (foundPCA9685) {
				servoBoard.setPWM(servo, 0, pwm);
			}
		} catch (IllegalArgumentException iae) {
			System.err.println(String.format("Cannot set servo %d to PWM %d", servo, pwm));
			iae.printStackTrace();
		}
	}

	public void stopHeadingServo() {
		stop(headingServoID);
	}

	public void stopTiltServo() {
		stop(tiltServoID);
	}

	private void stop(int servo) { // Set to 0
		if (foundPCA9685) {
			this.servoBoard.setPWM(servo, 0, 0);
		}
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

	public void servosZero() {
		this.setHeadingServoAngle(0f);
		this.setTiltServoAngle(0f);
	}

	/**
	 * Set the true heading of the device
	 * @param heading True heading in degrees
	 */
	public void setDeviceHeading(double heading) {
		deviceHeading = heading;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public static double getDeviceHeading() {
		return deviceHeading;
	}

	public void orientServos() {

		if (!this.isCalibrating()) {
			// Here, orient BOTH servos, with or without invert.
			// normalizedServoAngle ranges from 0 to 180 (counter-clockwise), 0 to -180 (clockwise)
			double bearing = (z - deviceHeading); // Default deviceHeading=180
			while (bearing < -180) { // Ex: -190 => 170
				bearing += 360;
			}
			while (bearing > 180) { // Ex: 190 => -170
				bearing -= 360;
			}
			int headingServoAngle = -(int)(headingServoSign * Math.round(bearing));
			/*
			 * If out of [-90..90], invert.
			 */
			if (headingServoAngle < -90 || headingServoAngle > 90) {
				invert = true;
			} else {
				invert = false;
			}

			if (he > 0) { // Daytime
				if (orientationVerbose && !manualEntry) {
					String mess = String.format(
									"Heading servo : Aiming Z: %.01f, servo-angle: %d %s - device heading: %.01f.",
									z,
									headingServoAngle,
									(invert ? String.format("(inverted to %.02f)", invertHeading((float) headingServoAngle)) : ""),
									deviceHeading);
					if (ansiConsole) {
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + "Driving Servos toward the Sun, " + SDF.format(new Date()) + PAD);
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 3) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
					} else {
						System.out.println(mess);
					}
				}
				if (astroVerbose && !manualEntry) {
					String mess = String.format("+ Calculated: From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
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
				int angle = -(int)(tiltServoSign * Math.round(90 - he));
				if (invert) {
					angle = -angle;
				}
				if (noServoIsMoving() /*!tiltServoMoving*/ && angle != previousTiltAngle) {
					if (servoVerbose && !manualEntry) {
						String mess = String.format(">>> Tilt servo angle now: %d %s", angle, (invert ? "(inverted)" : ""));
						if (ansiConsole) {
							AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 5) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
						} else {
							System.out.println(mess);
						}
					}
					if (noServoIsMoving() /*!tiltServoMoving*/) {
						if (angle != previousTiltAngle) {
							System.out.println(String.format("??? Setting tilt angle from %d to %d", previousTiltAngle, angle));
							this.setTiltServoAngle((float) angle);
							previousTiltAngle = angle;
						}
					}
				}
			} else { // Night time
				invert = false;
				if (servoVerbose && !manualEntry) {
					String mess = "Night time, parked...";
					if (ansiConsole) {
						AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 4) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
					} else {
						System.out.println(mess);
					}
				}
				int angle = 0;
				if (noServoIsMoving() /*!tiltServoMoving*/) {
					if (angle != previousTiltAngle) {
						this.setTiltServoAngle((float) angle);
						previousTiltAngle = angle;
					}
				}
				headingServoAngle = 0; // for the night
			} // End day or night
			if (orientationVerbose && !manualEntry) {
				String mess = String.format(">>> Heading servo angle now %d %s", headingServoAngle, (invert ? String.format("(inverted to %.02f)", invertHeading((float) headingServoAngle)) : ""));
				if (ansiConsole) {
					AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 6) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
				} else {
					System.out.println(mess);
				}
			}
			if (noServoIsMoving() /*!headingServoMoving*/) {
				if (headingServoAngle != previousHeadingAngle) {
					this.setHeadingServoAngle(invert ? invertHeading((float) headingServoAngle) : (float) headingServoAngle);
				}
				previousHeadingAngle = headingServoAngle;
			}
		}
	}

	private Calendar current;
	private Date fromDate = null, toDate = null;

	public void startWorking() {
		String mess;

		if (demo) {
			String strFromDate = System.getProperty("from.date");
			String strToDate = System.getProperty("to.date");
			if (strFromDate == null || strToDate == null) {
				System.out.println("-Dfrom.date and -Dto.date are both required in demo mode");
				System.exit(1);
			} else {
				try {
					fromDate = SDF_INPUT.parse(strFromDate);
					toDate = SDF_INPUT.parse(strToDate);
					current = new GregorianCalendar(TimeZone.getTimeZone("etc/UTC"));
					current.setTime(fromDate);
				} catch (Exception ex) {
					System.err.println(String.format("Bad date format, expecting %s", SDF_INPUT));
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}

		Thread timeThread = new Thread(() -> {
			while (keepWorking) {
				// Sun position calculation goes here
				if (demo) {
					getSunDataForDate(latitude, longitude, current);
					current.add(Calendar.MINUTE, 1);
					System.out.println(String.format(">>> %s", SDF.format(current.getTime())));
					if (current.getTime().after(toDate)) {
						keepWorking = false;
					}
				} else {
					getSunData(latitude, longitude);
				}
				this.orientServos();
				try {
					Thread.sleep(demo ? 10L : 1_000L);
				} catch (Exception ex) {
				}
			}
			System.out.println("Timer done.");
		});
		mess = "Starting the timer loop";
		if (ansiConsole){
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 11) + mess + PAD);
		} else {
			System.out.println(mess);
		}
		timeThread.start();
	}

	public void stopWorking() {
		this.keepWorking = false;
	}

	private static float applyLimitAndOffset(float angle) {
		float corrected = angle - tiltOffset;
		if (Math.abs(angle) > (90 - tiltLimit)) {
			corrected = (angle > 0) ? (90 - tiltLimit) : (tiltLimit - 90);
		}
		return corrected;
	}

	public static void main__(String... args) {
		tiltLimit = 5;

		float[] angles = { 90f, 89f, 85f, 80f };
		for (float angle : angles) {
			System.out.println(String.format("For %.02f => corrected to %.02f", angle, applyLimitAndOffset(angle)));
			System.out.println(String.format("For %.02f => corrected to %.02f", -angle, applyLimitAndOffset(-angle)));
		}
	}

	public static void main_(String... args) {
		String from = "2017-06-28T05:53:00";
		String to = "2017-06-28T20:33:00";

		try {
			Date fromDate = SDF_INPUT.parse(from);
			Date toDate = SDF_INPUT.parse(to);
			Calendar current = new GregorianCalendar(TimeZone.getTimeZone("etc/UTC"));
			current.setTime(fromDate);

			System.out.println("Starting");
			long before = System.currentTimeMillis();
			boolean keepWorking = true;
			while (keepWorking) {
				current.add(Calendar.MINUTE, 1);
				System.out.println(String.format(">>> %s", SDF.format(current.getTime())));
				if (current.getTime().after(toDate)) {
					keepWorking = false;
				}
				try {
					Thread.sleep(10L);
				} catch (Exception ex) {
				}
			}
			long after = System.currentTimeMillis();
			System.out.println(String.format("Completed in %s ms", NumberFormat.getInstance().format(after - before)));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {

		headingServoID = 14;
		tiltServoID = 15;

		// Supported parameters --heading:14 --tilt:15
		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith("--heading:")) {
					try {
						headingServoID = Integer.parseInt(prm.substring("--heading:".length()));
					} catch (Exception e) {
						throw e;
					}
				} else if (prm.startsWith("--tilt:")) {
					try {
						tiltServoID = Integer.parseInt(prm.substring("--tilt:".length()));
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}

		String strTiltLimit = System.getProperty("tilt.limit");
		if (strTiltLimit != null) {
			try {
				tiltLimit = Integer.parseInt(strTiltLimit);
				if (tiltLimit < 0 || tiltLimit > 90) {
					System.err.println("Tilt limit must be in [0..90], setting it to 0");
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		String strTiltOffset = System.getProperty("tilt.offset");
		if (strTiltOffset != null) {
			try {
				tiltOffset = Integer.parseInt(strTiltOffset);
				if (tiltOffset < -90 || tiltOffset > 90) {
					System.err.println("Tilt offset must be in [-90..90], setting it to 0");
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		testServos = "true".equals(System.getProperty("test.servos", "false"));

		SunFlower instance = new SunFlower(headingServoID, tiltServoID);
		if (manualEntry && ansiConsole) {
			System.out.println("Manual Entry and ANSI Console are mutually exclusive. Please choose one, and only one... Thank you.");
			System.exit(1);
		}

		String strLat = System.getProperty("latitude");
		if (strLat != null) {
			try {
				instance.setLatitude(Double.parseDouble(strLat));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strLong = System.getProperty("longitude");
		if (strLong != null) {
			try {
				instance.setLongitude(Double.parseDouble(strLong));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}

		// Set to 0
		instance.servosZero();
		instance.setCalibrating(false);

		if (testServos) {
			instance.setHeadingServoAngle(-90f);
			instance.setTiltServoAngle(-90);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setHeadingServoAngle(90f);
			instance.setTiltServoAngle(90);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			instance.setHeadingServoAngle(0f);
			instance.setTiltServoAngle(0);
			try { Thread.sleep(1_000L); } catch (Exception ex) {}
			System.out.println("Test done.");
		}

		if (ansiConsole) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 1) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + "Driving Servos toward the Sun, " + SDF.format(new Date()) + PAD);
		}
		String mess = String.format("Position %s / %s, Heading servo: #%d, Tilt servo: #%d",
						GeomUtil.decToSex(instance.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(instance.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
						headingServoID,
						tiltServoID);
		if (ansiConsole) {
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 2) + EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT + EscapeSeq.ANSI_BOLD + mess + PAD);
		} else {
			System.out.println("----------------------------------------------");
			System.out.println(mess);
			System.out.println("----------------------------------------------");
		}

		try {
			// Point the device to the lower pole: S if you are in the North hemisphere, N if you are in the South hemisphere.
			mess = String.format("Point the Device to the true %s, hit [Return] when ready.", instance.getLatitude() > 0 ? "South" : "North");

			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 11) + EscapeSeq.ANSI_REVERSE + mess + PAD);
			} else {
				System.out.println(mess);
			}

			z = instance.getLatitude() > 0 ? 180 : 0;
			instance.setCalibrating(true);
			userInput("");
			if (ansiConsole) { // Cleanup
				AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 11) + PAD);
			}
			instance.setCalibrating(false);
			// Done calibrating
			instance.setDeviceHeading(z);

			instance.startWorking();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				instance.stopWorking();
				instance.stop(headingServoID);
				instance.stop(tiltServoID);

				instance.setHeadingServoAngle(0f);
				instance.setTiltServoAngle(0f);
				try {
					if (!smoothMoves) {
						Thread.sleep(1_000L);
					} else {
						while (!instance.noServoIsMoving()) {
							Thread.sleep(1_000L);
						}
					}
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
				if (ansiConsole) {
					AnsiConsole.systemUninstall();
				}
				System.out.println("Finished!");
			}));
		} catch (Throwable ex) {
			System.err.println(">>> Panel Orienter... <<< BAM!");
			ex.printStackTrace();
//		System.exit(1);
		}
	}
}
