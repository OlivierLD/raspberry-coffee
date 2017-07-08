package orientation;

import static ansi.EscapeSeq.ANSI_BOLD;
import static ansi.EscapeSeq.ANSI_DEFAULT_BACKGROUND;
import static ansi.EscapeSeq.ANSI_DEFAULT_TEXT;
import static ansi.EscapeSeq.ANSI_ERASE_TO_EOL;
import static ansi.EscapeSeq.ANSI_ITALIC;
import static ansi.EscapeSeq.ANSI_NORMAL;
import static ansi.EscapeSeq.ANSI_CLS;
import static ansi.EscapeSeq.ANSI_REVERSE;
import static ansi.EscapeSeq.BOTTOM_LEFT_CORNER_BOLD;
import static ansi.EscapeSeq.BOTTOM_RIGHT_CORNER_BOLD;
import static ansi.EscapeSeq.BOTTOM_T_BOLD;
import static ansi.EscapeSeq.CROSS_BOLD;
import static ansi.EscapeSeq.LEFT_T_BOLD;
import static ansi.EscapeSeq.RIGHT_T_BOLD;
import static ansi.EscapeSeq.SOLID_HORIZONTAL_BOLD;
import static ansi.EscapeSeq.SOLID_VERTICAL_BOLD;
import static ansi.EscapeSeq.TOP_LEFT_CORNER_BOLD;
import static ansi.EscapeSeq.TOP_RIGHT_CORNER_BOLD;
import static ansi.EscapeSeq.TOP_T_BOLD;
import static ansi.EscapeSeq.ansiLocate;
import calculation.AstroComputer;
import calculation.SightReductionUtil;
import http.HTTPServer;
import http.HTTPServerInterface;
import i2c.servo.pwm.PCA9685;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
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
 * -Dtilt.verbose=true
 * -Dservo.verbose=true
 * -Dservo.super.verbose=true|both|tilt|heading|none|false
 *
 * -Dtilt.servo.sign=-1
 * -Dheading.servo.sign=-1
 * -Dtilt.limit=0..90 <- Minimum elevation of the sun
 * -Dtilt.offset=0    <- Offset in degrees
 *
 * -Done.by.one=true <- true is the default
 *
 * For the main, as an example:
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 * -Dtest.servos=true
 *
 * -Dsmooth.moves=true
 *
 * -Dtime.provided=true
 *
 * -Ddemo.mode=true
 * -Dfrom.date=2017-06-28T05:53:00
 * -Dto.date=2017-06-28T20:33:00
 *
 * -Dhttp.port=9999
 */
public class SunFlower implements HTTPServerInterface {

	private static int[] headingServoID = new int[] { 14 };
	private static int[] tiltServoID = new int[] { 15 };

	private double latitude = 0D;
	private double longitude = 0D;

	private boolean keepWorking = true;

	private static double he = 0D, z = 0D;
	private static double deviceHeading = 0D;

	private static int tiltLimit = 0;
	private static int tiltOffset = 0;

	private int tiltServoSign = 1;
	private int headingServoSign = 1;

	private final static float SMOOTH_STEP = 1.0f;

	private enum superVerboseType {
		BOTH,
		TILT,
		HEADING,
		NONE
	}

	private boolean httpVerbose = false;
	private HTTPServer httpServer = null;
	private int httpPort = -1;

	private static boolean orientationVerbose = false;
	private static boolean astroVerbose = false;
	private static boolean tiltVerbose = false;
	private static boolean servoVerbose = false;
	private static superVerboseType servoSuperVerbose = superVerboseType.NONE;
	private static boolean testServos = false;
	private static boolean smoothMoves = false;
	private static boolean demo = false;
	private static boolean timeProvided = false;
	private static boolean interactive = true; // Ask to point the device before starting

	private static boolean servoMoveOneByOne = true;

	private static boolean headingServoMoving = false;
	private static boolean tiltServoMoving = false;

	private static boolean manualEntry = false;
	private static boolean ansiConsole = false;
	private final static String PAD = ANSI_ERASE_TO_EOL;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
	private final static SimpleDateFormat SDF_NO_Z = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	private final static SimpleDateFormat SDF_INPUT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // Duration fmt.
	static {
		SDF_NO_Z.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	private static boolean foundPCA9685 = true;

	private static Date getSolarDate(double longitude, Date utc) {
		double toHours = longitude / 15d;
		long ms = utc.getTime();
		Date solar = new Date(ms + Math.round(toHours * 3_600_000));
		return solar;
	}

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
			if (astroVerbose && !ansiConsole) {
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

	private RESTImplementation restImplementation;

	/**
	 * Implements the management of the REST requests (see {@link RESTImplementation})
	 * Dedicated Admin Server.
	 *
	 * @param request the parsed request.
	 * @return the response, along with its HTTP status code.
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_IMPLEMENTED);
		response = restImplementation.processRequest(request, response); // All the skill is here.
		if (this.httpVerbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	public SunFlower(int[] headinServoNumber, int[] tiltServoNumber) {

//		Properties properties = System.getProperties();
//		properties.list(System.out);

		// For celestial calculations:
		System.setProperty("deltaT", System.getProperty("deltaT", "68.8033")); // 2017-Jun-01

		headingServoID = headinServoNumber;
		tiltServoID = tiltServoNumber;

		// Read System Properties
		orientationVerbose = "true".equals(System.getProperty("orient.verbose", "false"));
		tiltVerbose = "true".equals(System.getProperty("tilt.verbose", "false"));
		servoVerbose = "true".equals(System.getProperty("servo.verbose", "false"));
		String superVerbose = System.getProperty("servo.super.verbose", "none");
		switch (superVerbose) {
			case "both":
			case "true":
				servoSuperVerbose = superVerboseType.BOTH;
				break;
			case "tilt":
				servoSuperVerbose = superVerboseType.TILT;
				break;
			case "heading":
				servoSuperVerbose = superVerboseType.HEADING;
				break;
			case "none":
			case "false":
			default:
				servoSuperVerbose = superVerboseType.NONE;
				break;
		}
		astroVerbose = "true".equals(System.getProperty("astro.verbose", "false"));

		manualEntry = "true".equals(System.getProperty("manual.entry", "false"));
		ansiConsole = "true".equals(System.getProperty("ansi.console", "false"));
		demo = "true".equals(System.getProperty("demo.mode", "false"));
		timeProvided = "true".equals(System.getProperty("time.provided", "false"));

		if (demo && timeProvided) {
			throw new IllegalArgumentException("demo.mode and time.provided are mutually exclusive.");
		}

		interactive = "true".equals(System.getProperty("interactive", "true"));
		smoothMoves = "true".equals(System.getProperty("smooth.moves", "false"));

		servoMoveOneByOne = "true".equals(System.getProperty("one.by.one", "true"));

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

		String httpPortStr = System.getProperty("http.port");
		if (httpPortStr != null) {
			try {
				httpPort = Integer.parseInt(httpPortStr);
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
				System.err.println("+------------------------------------------------------------");
				System.err.println("| PCA9685 was NOT initialized.\n| Check your wiring, or make sure you are on a Raspberry PI...");
				System.err.println("| Moving on anyway...");
				System.err.println("+------------------------------------------------------------");
			}
		} catch (UnsatisfiedLinkError usle) {
			foundPCA9685 = false;
			System.err.println("+---------------------------------------------------------------------");
			System.err.println("| You might not be on a Raspberry PI, or PI4J/WiringPi is not there...");
			System.err.println("| Moving on anyway...");
			System.err.println("+---------------------------------------------------------------------");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		if (httpPort > 0) {
			restImplementation = new RESTImplementation(this);
			startHttpServer(httpPort);
		}
	}

	private boolean noServoIsMoving() {
		return !(headingServoMoving || tiltServoMoving);
	}

	private static void setHeadingServoMoving(boolean b) {
		headingServoMoving = b;
	}
	public void setHeadingServoAngle(final float f) {
		if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
			System.out.println(String.format("H> Servo heading set required to %.02f (previous %d), moving:%s", f, previousHeadingAngle, (headingServoMoving ? "yes" : "no")));
		}
		float startFrom = previousHeadingAngle;

		if ((servoMoveOneByOne ? noServoIsMoving() : !headingServoMoving) && smoothMoves && Math.abs(startFrom - f) > 1) {
			headingServoMoving = true;
			// Smooth move for steps > 1
			if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
				System.out.println(String.format("H>> Start a smooth move from heading %.02f to %.02f", startFrom, f));
			}
			Thread smoothy = new Thread(() -> {
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
					System.out.println(String.format("H>> Starting smooth thread for heading %.02f to %.02f", startFrom, f));
				}
				int sign = (startFrom > f) ? -1 : 1;
				float pos = startFrom;
				while (Math.abs(pos - f) >= SMOOTH_STEP) {
					if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
						System.out.println(String.format("H> Setting heading to %.02f, delta=%.02f (target %.02f)", pos, Math.abs(pos - f), f));
					}
					for (int id : headingServoID) {
						setAngle(id, pos);
					}
//				setAngle(headingServoID, pos);
					pos += (sign * SMOOTH_STEP);
					try { Thread.sleep(10L); } catch (Exception ex) {}
				}
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
					System.out.println(String.format("H>...Heading thread done, delta=%.02f", Math.abs(pos - f)));
				}
				setHeadingServoMoving(false);
			});
			smoothy.start();
		} else {
			if (servoMoveOneByOne ? noServoIsMoving() : !headingServoMoving) {
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.HEADING)) {
					System.out.println(String.format("H> Abrupt heading set to %.02f", f));
				}
				Arrays.stream(headingServoID).forEach(id -> setAngle(id, f));
//			setAngle(headingServoID, f);
			}
		}
	}

	private static void setTiltServoMoving(boolean b) {
		tiltServoMoving = b;
	}
	public void setTiltServoAngle(final float f) {
		if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
			System.out.println(String.format("T> Servo tilt set required to %.02f (previous %d), moving:%s", f, previousTiltAngle, (tiltServoMoving ? "yes" : "no")));
		}
		float startFrom = previousTiltAngle;
		float goToAngle = applyLimitAndOffset(f);
		if ((servoMoveOneByOne ? noServoIsMoving() : !tiltServoMoving) && smoothMoves && Math.abs(startFrom - goToAngle) > 1) {
			tiltServoMoving = true;
			// Smooth move for steps > 1
			if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
				System.out.println(String.format("T> Start a smooth move from tilt %.02f to %.02f (%.02f)", startFrom, f, goToAngle));
			}
			Thread smoothy = new Thread(() -> {
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
					System.out.println(String.format("T> Starting smooth thread for tilt %.02f to %.02f (%.02f)", startFrom, f, goToAngle));
				}
				int sign = (startFrom > goToAngle) ? -1 : 1;
				float pos = startFrom;
				while (Math.abs(pos - goToAngle) >= SMOOTH_STEP) {
					if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
						System.out.println(String.format("T> Setting tilt to %.02f, delta=%.02f", pos, Math.abs(pos - f)));
					}
//				setAngle(tiltServoID, pos);
					for (int id : tiltServoID) {
						setAngle(id, pos);
					}
					pos += (sign * SMOOTH_STEP);
					try { Thread.sleep(10L); } catch (Exception ex) {}
				}
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
					System.out.println(String.format("T>...Tilt thread done, delta=%.02f", Math.abs(pos - goToAngle)));
				}
				setTiltServoMoving(false);
			});
			smoothy.start();
		} else {
			if (servoMoveOneByOne ? noServoIsMoving() : !tiltServoMoving) {
				if (servoSuperVerbose.equals(superVerboseType.BOTH) || servoSuperVerbose.equals(superVerboseType.TILT)) {
					System.out.println(String.format("T> Abrupt tilt set to %.02f (%.02f)", f, goToAngle));
				}
//			setAngle(tiltServoID, goToAngle);
				for (int id : tiltServoID) {
					setAngle(id, goToAngle);
				}
			}
		}
	}

	private void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		if (servoVerbose && !manualEntry) {
			if (ansiConsole) {
				displayAnsiData();
			} else {
				String mess = String.format("Servo %d, angle %.02f\272, pwm: %d", servo, f, pwm);
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
		for (int id : headingServoID) {
			stop(id);
		}
	}

	public void stopTiltServo() {
//	stop(tiltServoID);
		for (int id: tiltServoID) {
			stop(id);
		}
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

	public static class GeographicPosition {
		double latitude;
		double longitude;
		public GeographicPosition(double l, double g) {
			this.latitude = l;
			this.longitude = g;
		}
	}
	public GeographicPosition getPosition() {
		return new GeographicPosition(latitude, longitude);
	}

	public static class ServoValues {
		HeadingServo heading;
		TiltServo tilt;


		public ServoValues(int h, int t) {
			this.heading = new HeadingServo(headingServoID, h);
			this.tilt = new TiltServo(tiltServoID, t, tiltOffset, tiltLimit);
		}

		public static class TiltServo {
			int[] pins;
			int value;
			int offset;
			int limit;
			public TiltServo(int[] p, int v, int o, int l) {
				this.pins = p;
				this.value = v;
				this.offset = o;
				this.limit = l;
			}
		}

		public static class HeadingServo {
			int[] pins;
			int value;
			public HeadingServo(int[] p, int v) {
				this.pins = p;
				this.value = v;
			}
		}
	}
	public ServoValues getServoValues() {
		return new ServoValues(ansiHeadingServoAngle, ansiTiltServoAngle);
	}

	public static class Dates {
		String system;
		String utc;
		String solar;
		public Dates(String system, String utc, String solar) {
			this.system = system;
			this.utc = utc;
			this.solar = solar;
		}
	}
	public Dates getDates() {
		return new Dates((ansiSystemDate != null ? SDF.format(ansiSystemDate) : "null"),
						(ansiSystemDate != null ? SDF_UTC.format(ansiSystemDate) : "null"),
						(ansiSolarDate != null ? SDF_NO_Z.format(ansiSolarDate) : "null"));
	}

	public static class SunData {
		double h;
		double z;
		public SunData(double he, double azimuth) {
			this.h = he;
			this.z = azimuth;
		}
	}
	public SunData getSunData() {
		return new SunData(he, z);
	}

	public static class AllData {
		GeographicPosition pos;
		ServoValues servos;
		Dates dates;
		SunData sunData;
		double heading;
		public AllData(
						GeographicPosition pos,
						ServoValues servos,
						Dates dates,
						SunData sunData,
						double heading) {
			this.pos = pos;
			this.servos = servos;
			this.dates = dates;
			this.sunData = sunData;
			this.heading = heading;
		}
	}
	public AllData getAllData() {
		return new AllData(getPosition(), getServoValues(), getDates(), getSunData(), getDeviceHeading());
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

			if (ansiConsole) {
				displayAnsiData();
			} else if (servoVerbose) {
				System.out.println("----------------------------------------------");
				String posMess = String.format("Position %s / %s, Heading servo: #%s, Tilt servo: #%s, Tilt: limit %d, offset %d",
								GeomUtil.decToSex(getLatitude(), GeomUtil.SWING, GeomUtil.NS),
								GeomUtil.decToSex(getLongitude(), GeomUtil.SWING, GeomUtil.EW),
								Arrays.stream(headingServoID).boxed().map(String::valueOf).collect(Collectors.joining(",")),
								Arrays.stream(tiltServoID).boxed().map(String::valueOf).collect(Collectors.joining(",")),
								tiltLimit,
								tiltOffset);
				System.out.println(posMess);
				System.out.println("----------------------------------------------");
			}

			ansiDeviceHeading = deviceHeading;
			Date date = timeProvided ? current.getTime() : new Date();
			ansiSystemDate = date;
			ansiSolarDate = getSolarDate(getLongitude(), date);

			if (he > 0) { // Daytime
				if (orientationVerbose && !manualEntry) {
					String mess = String.format(
									"Heading servo : Aiming Z: %.01f, servo-angle (bearing): %d %s - device heading: %.01f.",
									z,
									headingServoAngle,
									(invert ? String.format("(inverted to %.02f)",
													invertHeading((float) headingServoAngle)) : ""),
									deviceHeading);
					if (ansiConsole) {
						displayAnsiData();
					} else {
						System.out.println(mess);
					}
				}
				if (astroVerbose && !manualEntry) {
					if (ansiConsole) {
						displayAnsiData();
					} else {
						String mess = String.format("+ Calculated: From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
										GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
										GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
										he,
										z);
						System.out.println(mess);
					}
				}
				int angle = -(int)(tiltServoSign * Math.round(90 - he));
				if (invert) {
					angle = -angle;
				}
				ansiTiltServoAngle = angle;
				if ((servoMoveOneByOne ? noServoIsMoving() : !tiltServoMoving) && angle != previousTiltAngle) {
					if (tiltVerbose && !manualEntry) {
						if (ansiConsole) {
							displayAnsiData();
						} else {
							String mess = String.format(">>> Tilt servo angle now: %d %s%s", angle, (invert ? "(inverted)" : ""), (angle != applyLimitAndOffset(angle) ? String.format(", limited to %.02f", applyLimitAndOffset(angle)) : ""));
							System.out.println(mess);
						}
					}
					if (servoMoveOneByOne ? noServoIsMoving() : !tiltServoMoving) {
						if (angle != previousTiltAngle) {
//						System.out.println(String.format("??? Setting tilt angle from %d to %d", previousTiltAngle, angle));
							this.setTiltServoAngle((float) angle);
							previousTiltAngle = angle;
						}
					}
				}
			} else { // Night time
				invert = false;
				ansiTiltServoAngle = 0;
				if (tiltVerbose && !manualEntry) {
					if (ansiConsole) {
						displayAnsiData();
					} else {
						String mess = "Night time, tilt parked...";
						System.out.println(mess);
					}
				}
				int angle = 0;
				if (servoMoveOneByOne ? noServoIsMoving() : !tiltServoMoving) {
					if (angle != previousTiltAngle) {
						this.setTiltServoAngle((float) angle);
						previousTiltAngle = angle;
					}
				}
				headingServoAngle = 0; // for the night
			} // End day or night
			ansiHeadingServoAngle = headingServoAngle;
			if (orientationVerbose && !manualEntry) {
				if (ansiConsole) {
					displayAnsiData();
				} else {
					String mess = String.format(">>> Heading servo angle now %d %s", headingServoAngle, (invert ? String.format("(inverted to %.02f)", invertHeading((float) headingServoAngle)) : ""));
					System.out.println(mess);
				}
			}
			if (servoMoveOneByOne ? noServoIsMoving() : !headingServoMoving) {
				float newHeadingAngle = invert ? invertHeading((float) headingServoAngle) : (float) headingServoAngle;
				if (headingServoAngle != previousHeadingAngle) {
					this.setHeadingServoAngle(newHeadingAngle);
				}
				previousHeadingAngle = (int)Math.round(newHeadingAngle); // Store the actual version of the angle, possibly inverted.
			}
		}
	}

	private Calendar current;
	private Date fromDate = null, toDate = null;

	public void setCurrentDateTime(Date date) {
		if (current == null) {
			current = new GregorianCalendar(TimeZone.getTimeZone("etc/UTC"));
		}
		current.setTime(date);
	}

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
		} else if (timeProvided) {
			if (current == null) {
				current = new GregorianCalendar(TimeZone.getTimeZone("etc/UTC"));
			}
		}

		// Timer Thread
		Thread timeThread = new Thread(() -> {
			while (keepWorking) {
				// Sun position calculation goes here
				if (demo) {
					getSunDataForDate(latitude, longitude, current);
					current.add(Calendar.MINUTE, 1);
//				System.out.println(String.format(">>> %s", SDF.format(current.getTime())));
					if (current.getTime().after(toDate)) {
						keepWorking = false;
					}
				} else if (timeProvided) {
					getSunDataForDate(latitude, longitude, current);
					System.out.println(String.format(">>> %s", SDF.format(current.getTime())));
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
			AnsiConsole.out.println(ansiLocate(1, 1) + mess + PAD);
		} else {
			System.out.println(mess);
		}
		timeThread.start();
	}

	public void stopWorking() {
		this.keepWorking = false;
		for (int id : headingServoID) {
			stop(id);
		}
		for (int id : tiltServoID) {
			stop(id);
		}

		try {
			if (!smoothMoves) {
				setHeadingServoAngle(0f);
				setTiltServoAngle(0f);
				Thread.sleep(1_000L);
			} else {
				setHeadingServoAngle(0f);
				while (!noServoIsMoving()) {
					Thread.sleep(1_000L);
				}
				setTiltServoAngle(0f);
				while (!noServoIsMoving()) {
					Thread.sleep(1_000L);
				}
			}
		} catch (InterruptedException ie) {
			System.err.println(ie.getMessage());
		}
		if (httpServer != null) {
			httpServer.stopRunning();
		}
	}

	private static float applyLimitAndOffset(float angle) {
		float corrected = angle - tiltOffset;
		if (Math.abs(angle) > (90 - tiltLimit)) {
			corrected = (angle > 0) ? (90 - tiltLimit) : (tiltLimit - 90);
		}
		return corrected;
	}

	public static void main__(String... args) {
		tiltLimit = 20;

		float[] angles = { 90f, 89f, 85f, 80f, 79f };
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

		headingServoID = new int[] { 14 };
		tiltServoID = new int[] { 15 };

		// Supported parameters --heading:14 --tilt:15
		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith("--heading:")) {
					try {
						List<Integer> hsIDs = new ArrayList<>();
						String[] strIds = prm.substring("--heading:".length()).split(",");
						Arrays.stream(strIds).forEach(sid -> {
							hsIDs.add(Integer.parseInt(sid));
						});
						headingServoID = hsIDs.stream().mapToInt(x -> x).toArray();
//					headingServoID = Integer.parseInt(prm.substring("--heading:".length()));
					} catch (Exception e) {
						throw e;
					}
				} else if (prm.startsWith("--tilt:")) {
					try {
						List<Integer> tsIDs = new ArrayList<>();
						String[] strIds = prm.substring("--tilt:".length()).split(",");
						Arrays.stream(strIds).forEach(sid -> {
							tsIDs.add(Integer.parseInt(sid));
						});
						tiltServoID = tsIDs.stream().mapToInt(x -> x).toArray();
//					tiltServoID = Integer.parseInt(prm.substring("--tilt:".length()));
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}

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
			AnsiConsole.out.println(ansiLocate(1, 1) + ANSI_CLS);
		}
		String mess = String.format("Position %s / %s, Heading servo: #%s. Tilt servo: #%s. Tilt: limit %d, offset %d",
						GeomUtil.decToSex(instance.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(instance.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
						Arrays.stream(headingServoID).boxed().map(String::valueOf).collect(Collectors.joining(",")),
						Arrays.stream(tiltServoID).boxed().map(String::valueOf).collect(Collectors.joining(",")),
						tiltLimit,
						tiltOffset);
		if (!ansiConsole) {
			System.out.println("----------------------------------------------");
			System.out.println(mess);
			System.out.println("----------------------------------------------");
		}

		try {
			z = instance.getLatitude() > 0 ? 180 : 0;
			if (interactive) {
				// Point the device to the lower pole: S if you are in the North hemisphere, N if you are in the South hemisphere.
				mess = String.format("Point the Device to the true %s, hit [Return] when ready.", instance.getLatitude() > 0 ? "South" : "North");

				if (ansiConsole) {
					AnsiConsole.out.println(ansiLocate(1, 1) + ANSI_REVERSE + mess + PAD);
				} else {
					System.out.println(mess);
				}

				instance.setCalibrating(true);
				userInput("");
				if (ansiConsole) { // Cleanup
					AnsiConsole.out.println(ansiLocate(1, 1) + PAD);
				}
			}
			instance.setCalibrating(false);
			// Done calibrating
			instance.setDeviceHeading(z);

			instance.startWorking();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				instance.stopWorking();
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


	public void startHttpServer(int port) {
		try {
			this.httpServer = new HTTPServer(port, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Date ansiSolarDate = null;
	private static Date ansiSystemDate = null;
	private int ansiHeadingServoAngle = 0;
	private int ansiTiltServoAngle = 0;
	private double ansiDeviceHeading = 0d;

	/**
	 * Returns a string of nb times the str parameter.
	 * @param str the string to use
	 * @param nb number of times
	 * @return the expected string.
	 */
	private static String drawXChar(String str, int nb) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<nb; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	/**
	 * Right pad, with blanks
	 * @param s
	 * @param len
	 * @return
	 */
	private static String rpad(String s, int len) {
		return rpad(s, len, " ");
	}
	private static String rpad(String s, int len, String pad) {
		String str = s;
		while (str.length() < len) {
			str += pad;
		}
		return str;
	}

	/**
	 * Left pad, with blanks
	 * @param s
	 * @param len
	 * @return
	 */
	private static String lpad(String s, int len) {
		return lpad(s, len, " ");
	}
	private static String lpad(String s, int len, String pad) {
		String str = s;
		while (str.length() < len) {
			str = pad + str;
		}
		return str;
	}

	/**
	 * Box codes are available at https://en.wikipedia.org/wiki/Box-drawing_character
	 * Display the data in an ANSI box, refreshed every time is is displayed.
	 */
	private  void displayAnsiData() {
		int line = 1; // Start from that line
		// Frame top
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						TOP_LEFT_CORNER_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						TOP_RIGHT_CORNER_BOLD +
						PAD);
		// Title. Note: The italic escape code is correct. But it does not work on all platforms.
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD  + ANSI_BOLD + ANSI_ITALIC + rpad("           Solar Panel Orientation ", 45) + ANSI_NORMAL + SOLID_VERTICAL_BOLD + PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Servo info, heading
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
						rpad(String.format(" Heading Servo(s) # %s.",
										Arrays.stream(headingServoID).boxed().map(String::valueOf).collect(Collectors.joining(","))), 45) + SOLID_VERTICAL_BOLD +
						PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Servo info, tilt
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
						rpad(String.format(" Tilt Servo(s) # %s. limit %d, offset %d",
										Arrays.stream(tiltServoID).boxed().map(String::valueOf).collect(Collectors.joining(",")),
										tiltLimit,
										tiltOffset), 45) + SOLID_VERTICAL_BOLD +
						PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Position
		String lat = GeomUtil.decToSex(getLatitude(), GeomUtil.SWING, GeomUtil.NS);
		String lng = GeomUtil.decToSex(getLongitude(), GeomUtil.SWING, GeomUtil.EW);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
						rpad(" Position L/G", 15) + SOLID_VERTICAL_BOLD +
						rpad(lpad(" " + lat, 13), 14) + SOLID_VERTICAL_BOLD +
						rpad(lpad(" " + lng, 13), 14) + SOLID_VERTICAL_BOLD +
						PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// System date
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" System Date ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + (ansiSystemDate != null ? SDF.format(ansiSystemDate) : "null"), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		// UTC date
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" UTC ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + (ansiSystemDate != null ? SDF_UTC.format(ansiSystemDate) : "null"), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		// Solar date
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" Solar Date ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + (ansiSolarDate != null ? SDF_NO_Z.format(ansiSolarDate) : "null"), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
    // Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Dead Reckoning
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" He ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + String.format("%6.02f\272", he), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" Z ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + String.format("%6.02f\272", z) + " true", 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Servos
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" Heading Servo ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + String.format("%s%s", lpad(String.format("%+02d", ansiHeadingServoAngle), 3, " "), (invert? String.format(" (inverted to %+.0f)",	invertHeading((float) ansiHeadingServoAngle)) :"")), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" Tilt Servo ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + String.format("%s%s%s", lpad(String.format("%+02d", ansiTiltServoAngle), 3, " "),
										(invert ? " (inverted)":""),
										(ansiTiltServoAngle != applyLimitAndOffset(ansiTiltServoAngle) ? String.format(", limited: %.0f", applyLimitAndOffset(ansiTiltServoAngle)) : "")), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						RIGHT_T_BOLD +
						PAD);
		// Device heading
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD + lpad(" Device Hdg ", 15) +
						SOLID_VERTICAL_BOLD +
						rpad(" " + String.format("%.01f\272", ansiDeviceHeading), 29) +
						SOLID_VERTICAL_BOLD +
						PAD);
		// Frame bottom
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						BOTTOM_LEFT_CORNER_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 15) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						SOLID_HORIZONTAL_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, 14) +
						BOTTOM_RIGHT_CORNER_BOLD +
						PAD);
	}
}
