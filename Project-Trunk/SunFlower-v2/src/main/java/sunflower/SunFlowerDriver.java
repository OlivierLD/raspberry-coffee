package sunflower;

import calc.DeadReckoning;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import nmea.parser.RMC;
import sunflower.gps.GPSReader;
import sunflower.utils.ANSIUtil;
import utils.StaticUtil;
import utils.TimeUtil;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.TimeUtil.delay;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 *
 * RPM : Revolution Per Minute, speed of the rotation.
 * Steps per Revolution: How many steps for 360 degrees.
 * nbSteps: How many steps should the shaft do when started.
 *
 * By default, date for Celestial calculations is current date.
 * It can be simulated for demo or development with the following System variables:
 * JAVA_OPTS="$JAVA_OPTS -Ddate.simulation=true"
 * JAVA_OPTS="$JAVA_OPTS -Dstart.date.simulation=2020-03-06T20:00:00"
 *
 * Date from GPS (Serial port, baud.rate)
 * JAVA_OPTS="$JAVA_OPTS -Ddate.from.gps=true"
 * JAVA_OPTS="$JAVA_OPTS -Dgps.serial.port=/dev/ttyS80"
 * JAVA_OPTS="$JAVA_OPTS -Dgps.serial.baud.rate=4800"
 *
 * JAVA_OPTS="$JAVA_OPTS -Dincrement.per.second=600"
 * JAVA_OPTS="$JAVA_OPTS -Dbetween.astro.loops=10" (only applied if date.simulation=true)
 * JAVA_OPTS="$JAVA_OPTS -Dfirst.move.slack=30" in seconds (only applied if date.simulation=true). Resumes calculation after this amount of time after the first move of the device
 *
 * -Duse.step.accumulation=true, set it to false to use actual angles
 *
 * Also:
 * -Dno.motor.movement=true will NOT move the motors.
 * Use it with -Dmotor.hat.verbose=true
 *
 * -Dspecial.debug.verbose=true, as you may expect.
 *
 * -Delevation.inverted=true|false
 * -Dazimuth.inverted=true|false
 *
 * -Dwith.ssd1306=true|false (default false)
 */
public class SunFlowerDriver {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // HTTPServer.class.getName());
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss Z");
	private final static SimpleDateFormat SDF_OLED = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		SDF_OLED.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	private final static int STEPS_PER_CIRCLE = 200;

	private static long loopDelay = 1_000L;
	private static final boolean useStepsAccumulation = "true".equals(System.getProperty("use.step.accumulation", "true")); // Set to false NOT to use it
	private static final boolean withSSD1306 = "true".equals(System.getProperty("with.ssd1306"));
	private static final boolean sunFlowerVerbose = "true".equals(System.getProperty("sun.flower.verbose", "true"));

	static {
		if ("true".equals(System.getProperty("date.simulation"))) {
			String betweenLoops = System.getProperty("between.astro.loops", String.valueOf(loopDelay / 1_000L)); // In seconds
			try {
				loopDelay = Long.parseLong(betweenLoops);
				loopDelay *= 1_000L;
			} catch (NumberFormatException nfe) {
				System.err.println("Bad between.astro.loops value [%s], keeping default.");
			}
		}
	}

	// Depends on the kind of gear you are using, worm gear, pinion, etc...
	private static final boolean elevationInverted = "true".equals(System.getProperty("elevation.inverted"));
	private static final boolean azimuthInverted = "true".equals(System.getProperty("azimuth.inverted"));

	private double elevationOffset = 0D;
	private double azimuthOffset = 0D;
	// Introduce Heading (default 180? <- Northern Hemisphere, when L > Dsun)
	private final static double DEFAULT_DEVICE_HEADING = 180D;
	private double deviceHeading = DEFAULT_DEVICE_HEADING;

	public void setDeviceHeading(double hdg) {
		if (hdg != this.deviceHeading) { // Then orient the device?
			System.out.println(String.format("Heading has changed from %.02f to %.02f", this.deviceHeading, hdg));
		}
		this.deviceHeading = hdg;
	}

	private final SunFlowerDriver instance = this;
	private final List<String> commandHistory = new ArrayList<>();

	private static class Position {
		double latitude;
		double longitude;

		public Position(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		@Override
		public String toString() {
			return String.format(
					"%s / %s",
					GeomUtil.decToSex(this.latitude, GeomUtil.SWING, GeomUtil.NS),
					GeomUtil.decToSex(this.longitude, GeomUtil.SWING, GeomUtil.EW));
		}
	}
	private static Position devicePosition = null; // Can be fed from a GPS, or manually (System variable).
	private static double sunAzimuth   = 180d;
	private static double sunElevation =  -1d;
	private static double sunDecl =  Double.NaN;
	private static double sunGHA =  Double.NaN;
	private static Date solarDate = null;

	private boolean simulating = false;

	private AdafruitMotorHAT mh;
	private SSD1306 oled;
	private static SwingLedPanel substitute;
	private ScreenBuffer sb;


	private AdafruitMotorHAT.AdafruitStepperMotor azimuthMotor;
	private AdafruitMotorHAT.AdafruitStepperMotor elevationMotor;

	// Origins
	private final static double PARKED_ELEVATION =  90d;
	private final static double PARKED_AZIMUTH   = 180d;

	// Init
	private double currentDeviceElevation = PARKED_ELEVATION;
	private double currentDeviceAzimuth = PARKED_AZIMUTH;

	// Offsets from Origins above
	private int currentDeviceElevationStepOffset = 0;
	private int currentDeviceAzimuthStepOffset = 0;

	private CelestialComputerThread astroThread = null;
	private MotorThread elevationMotorThread = null;
	private MotorThread azimuthMotorThread = null;

	private final double MIN_DIFF_FOR_MOVE = 0.5;
	private double minDiffForMove = MIN_DIFF_FOR_MOVE;

	// Default. Try SINGLE, DOUBLE, MICROSTEP, INTERLEAVE...
	// SINGLE is less accurate
	// DOUBLE is fine but heats the motors
	// MICROSTEP sounds - for this project - like a good option
	private final static AdafruitMotorHAT.Style DEFAULT_MOTOR_STYLE = AdafruitMotorHAT.Style.MICROSTEP;  // Default. Try SINGLE, DOUBLE, MICROSTEP, INTERLEAVE...

	private static AdafruitMotorHAT.Style findStyle(String styleStr) {
		for (AdafruitMotorHAT.Style style : AdafruitMotorHAT.Style.values()) {
			if (styleStr.equals(style.toString())) {
				return style;
			}
		}
		return DEFAULT_MOTOR_STYLE;
	}

	private final AdafruitMotorHAT.Style motorStyle = findStyle(System.getProperty("stepper.style", DEFAULT_MOTOR_STYLE.toString()));

	// Default values. SHOULD be overridden.
	private static double azimuthMotorRatio   = 1d / 40d; // Set with System variable "azimuth.ratio"
	private static double elevationMotorRatio = 1d / 7.11111; // 18:128, Set with System variable "elevation.ratio"

	private boolean keepGoing = true;
	private final static int DEFAULT_RPM = 30;
	private final static int DEFAULT_STEPS_PER_REV = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS;

	private final static boolean SUN_FLOWER_VERBOSE = "true".equals(System.getProperty("sun.flower.verbose"));
	private final static boolean MOTOR_HAT_VERBOSE = "true".equals(System.getProperty("motor.hat.verbose"));
	private final static boolean TOO_LONG_EXCEPTION_VERBOSE = "true".equals(System.getProperty("too.long.exception.verbose", "true"));
	private final static boolean ASTRO_VERBOSE = "true".equals(System.getProperty("astro.verbose", "false"));
	private final static boolean MOVES_VERBOSE = "true".equals(System.getProperty("moves.verbose", "false"));

	private static int minimumAltitude = -1;
	static {
		try {
			minimumAltitude = Integer.parseInt(System.getProperty("minimum.elevation", String.valueOf(minimumAltitude)));
		} catch (NumberFormatException nfe) {
			System.err.println(nfe.toString());

		}
	}

	public static class MoveCompleted {
		private final Date date;
		private final long epoch;
	  	private final int nbSteps;
		private final long elapsed;

		public MoveCompleted(Date date, int nbSteps, long elapsed) {
			this.date = date;
			this.epoch = date.getTime();
			this.nbSteps = nbSteps;
			this.elapsed = elapsed;
		}

		public int getNbSteps() {
			return nbSteps;
		}

		public long getElapsed() {
			return elapsed;
		}

		public Date getDate() {
			return date;
		}

		public long getEpoch() {
			return epoch;
		}

		@Override
		public String toString() {
			return String.format(
					"Move (%d steps) completed in %s",
					this.nbSteps,
					TimeUtil.fmtDHMS(TimeUtil.msToHMS(this.elapsed)));
		}
	}

	public static class DeviceData {
		private final Date date;
		private final Position devicePosition;
		private final long epoch;
		private final double azimuth;
		private final double elevation;
		private final double azimuthOffset;
		private final double elevationOffset;
		private final double deviceHeading;
		private final int azimuthStepOffset;
		private final int elevationStepOffset;

		public DeviceData(Date date,
						  Position devicePosition,
						  double azimuth,
						  double elevation,
						  double azimuthOffset,
						  double elevationOffset,
						  double deviceHeading,
						  int azimuthStepOffset,
						  int elevationStepOffset) {
			this.date = date;
			this.devicePosition = devicePosition;
			this.epoch = date.getTime();
			this.azimuth = azimuth;
			this.elevation = elevation;
			this.azimuthOffset = azimuthOffset;
			this.elevationOffset = elevationOffset;
			this.deviceHeading = deviceHeading;
			this.azimuthStepOffset = azimuthStepOffset;
			this.elevationStepOffset = elevationStepOffset;
		}

		public Date getDate() {
			return date;
		}

		public double getAzimuth() {
			return azimuth;
		}

		public double getElevation() {
			return elevation;
		}

		public long getEpoch() {
			return epoch;
		}

		public double getAzimuthOffset() {
			return azimuthOffset;
		}

		public double getElevationOffset() {
			return elevationOffset;
		}

		public double getDeviceHeading() {
			return deviceHeading;
		}

		public Position getDevicePosition() {
			return devicePosition;
		}

		public int getAzimuthStepOffset() {
			return azimuthStepOffset;
		}

		public int getElevationStepOffset() {
			return elevationStepOffset;
		}

		@Override
		public String toString() {
			return String.format(
					"%s, Device: Azimuth: %.02f, Elevation: %.02f (Offsets: Z: %.02f, Elev: %.02f), Device Heading: %.02f",
					this.date,
					this.azimuth,
					this.elevation,
					this.azimuthOffset,
					this.elevationOffset,
					this.deviceHeading);
		}
	}

	public static class SunData {
		private final Date date;
		private final long epoch;
		private final double azimuth;
		private final double elevation;
		private final double decl;
		private final double gha;
		private final Date solarDate; // Warning: UTC! Add UTC your system Offset.
		private final long solarEpoch;

		public SunData(Date date, double azimuth, double elevation, double decl, double gha, Date solarDate, long solarEpoch) {
			this.date = date;
			this.epoch = date.getTime();
			this.azimuth = azimuth;
			this.elevation = elevation;
			this.decl = decl;
			this.gha = gha;
			this.solarDate = solarDate;
			this.solarEpoch = solarEpoch;
		}

		public Date getDate() {
			return date;
		}

		public double getAzimuth() {
			return azimuth;
		}

		public double getElevation() {
			return elevation;
		}

		public long getEpoch() {
			return epoch;
		}

		public double getDecl() {
			return decl;
		}

		public double getGha() {
			return gha;
		}

		public Date getSolarDate() {
			return solarDate;
		}

		public long getSolarEpoch() {
			return solarEpoch;
		}

		public String toString() {
			return String.format(
					"%s, Sun: Azimuth: %.02f, Elevation: %.02f",
					this.date,
					this.azimuth,
					this.elevation);
		}
	}

	public static class DeviceAzimuthStart {
		private final Date date;
		private final long epoch;
		private final double deviceAzimuth;
		private final double sunAzimuth;

		public DeviceAzimuthStart(Date date, double deviceAzimuth, double sunAzimuth) {
			this.date = date;
			this.epoch = date.getTime();
			this.deviceAzimuth = deviceAzimuth;
			this.sunAzimuth = sunAzimuth;
		}

		public Date getDate() {
			return date;
		}

		public double getDeviceAzimuth() {
			return deviceAzimuth;
		}

		public double getSunAzimuth() {
			return sunAzimuth;
		}

		public long getEpoch() {
			return epoch;
		}

		@Override
		public String toString() {
			return String.format(
					"At %s, setting device Azimuth from %.02f to %.02f degrees (a %.02f degrees move)",
					this.date,
					this.deviceAzimuth,
					this.sunAzimuth,
					Math.abs(this.deviceAzimuth - this.sunAzimuth));
		}
	}

	public static class DeviceElevationStart {

		private final Date date;
		private final long epoch;
		private final double deviceElevation;
		private final double sunElevation;

		public DeviceElevationStart(Date date, double deviceElevation, double sunElevation) {
			this.date = date;
			this.epoch = date.getTime();
			this.deviceElevation = deviceElevation;
			this.sunElevation = sunElevation;
		}

		public Date getDate() {
			return date;
		}

		public double getDeviceElevation() {
			return deviceElevation;
		}

		public double getSunElevation() {
			return sunElevation;
		}

		public long getEpoch() {
			return epoch;
		}

		@Override
		public String toString() {
			return String.format(
					"At %s, setting device Elevation from %.02f to %.02f degrees (a %.02f degrees move)",
					this.date,
					this.deviceElevation,
					this.sunElevation,
					Math.abs(this.deviceElevation - this.sunElevation));
		}
	}

	public static class MoveDetails {
		private final Date date;
		private final long epoch;
		private final int nbSteps;
		private final AdafruitMotorHAT.MotorCommand motorCommand;
		private final int motorNum;

		public MoveDetails(Date date, int nbSteps, AdafruitMotorHAT.MotorCommand motorCommand, int motorNum) {
			this.date = date;
			this.epoch = date.getTime();
			this.nbSteps = nbSteps;
			this.motorCommand = motorCommand;
			this.motorNum = motorNum;
		}

		public Date getDate() {
			return date;
		}

		public int getNbSteps() {
			return nbSteps;
		}

		public AdafruitMotorHAT.MotorCommand getMotorCommand() {
			return motorCommand;
		}

		public int getMotorNum() {
			return motorNum;
		}

		public long getEpoch() {
			return epoch;
		}

		@Override
		public String toString() {
			return String.format("This will be %d steps %s on motor #%d", this.nbSteps, this.motorCommand, this.motorNum);
		}
	}

	public static class DeviceInfo {
		private final Date date;
		private final long epoch;
		private final String message;

		public DeviceInfo(Date date, String message) {
			this.date = date;
			this.epoch = date.getTime();
			this.message = message;
		}

		public Date getDate() {
			return date;
		}

		public String getMessage() {
			return message;
		}

		public long getEpoch() {
			return epoch;
		}

		@Override
		public String toString() {
			return String.format("%s %s", this.date.toString(), this.message);
		}
	}

	private void logWithTime(String message) {
//		System.out.println(String.format("%s - %s", SDF.format(new Date()), message));
		getInstance().getLogger().info(String.format("%s - %s", SDF.format(new Date()), message));
	}

	public void setElevationOffset(double elevationOffset) {
		this.elevationOffset = elevationOffset;
	}

	public void setAzimuthOffset(double azimuthOffset) {
		this.azimuthOffset = azimuthOffset;
	}

	public void setDevicePosition(double lat, double lng) {
		this.devicePosition = new Position(lat, lng);
	}

	// TODO a queue to separate the MotorThreads?

	private class MotorThread extends Thread {
		private final AdafruitMotorHAT.AdafruitStepperMotor stepper;
		private final int nbSteps;
		private final AdafruitMotorHAT.MotorCommand motorCommand;
		private final AdafruitMotorHAT.Style motorStyle;

		MotorThread(AdafruitMotorHAT.AdafruitStepperMotor stepper,
		            int nbSteps,
		            AdafruitMotorHAT.MotorCommand motorCommand,
		            AdafruitMotorHAT.Style motorStyle) {
			this.stepper = stepper;
			this.nbSteps = nbSteps;
			this.motorCommand = motorCommand;
			this.motorStyle = motorStyle;
		}

	  	@Override
		public void run() {
			try {
				long before = System.currentTimeMillis();
				if (MOTOR_HAT_VERBOSE) {
					System.out.println("+----------------------------------------------");
					System.out.println(String.format("| Starting move of %d steps on motor #%d (%s)", nbSteps, this.stepper.getMotorNum(), this.stepper.getMotorNum() == 1 ? "Z" : "Elev"));
					System.out.println("+----------------------------------------------");
				}
				if ("false".equals(System.getProperty("no.motor.movement", "false"))) {
					this.stepper.step(nbSteps, motorCommand, motorStyle, t -> {
						if (MOTOR_HAT_VERBOSE && TOO_LONG_EXCEPTION_VERBOSE) {
							// t.printStackTrace();
							System.out.println(String.format("\t\tToo long! %s", t));
						}
					});
				} else {
					delay(500); // Half a sec, simulation
				}
				long after = System.currentTimeMillis();
				if (MOTOR_HAT_VERBOSE) {
					System.out.println("+----------------------------------------------");
					System.out.println(String.format("| Completed move of %d steps on motor #%d in %d ms", nbSteps, this.stepper.getMotorNum(), (after - before)));
					System.out.println("+----------------------------------------------");
				}
				MoveCompleted payload = new MoveCompleted(new Date(), this.nbSteps, (after - before));
				instance.publish(this.stepper.getMotorNum() == 1 ? EventType.MOVING_AZIMUTH_END : EventType.MOVING_ELEVATION_END, payload);
				if (MOTOR_HAT_VERBOSE) {
					System.out.println(String.format("\t%s on motor #%d",
							payload.toString(),
							this.stepper.getMotorNum()));
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
			if (MOTOR_HAT_VERBOSE) {
				System.out.println("MotorThread run completed.");
			}
		}
	}

	private static Date getSolarDateFromEOT(Date utc, double latitude, double longitude) {
		AstroComputerV2 acv2 = new AstroComputerV2();
		Calendar current = GregorianCalendar.getInstance();
		current.setTime(utc);
		acv2.setDateTime(current.get(Calendar.YEAR),
				current.get(Calendar.MONTH) + 1,
				current.get(Calendar.DAY_OF_MONTH),
				current.get(Calendar.HOUR_OF_DAY),
				current.get(Calendar.MINUTE),
				current.get(Calendar.SECOND));
		acv2.calculate();
//		SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
//				AstroComputer.getSunDecl(),
//				latitude,
//				longitude);
//		sru.calculate();
		// Get Equation of time, used to calculate solar time.
		double eot = acv2.getSunMeridianPassageTime(latitude, longitude); // in decimal hours

		long ms = utc.getTime();
		Date solar = new Date(ms + Math.round((12 - eot) * 3_600_000));
		return solar;
	}

	private static class CelestialComputerThread extends Thread {
		private boolean keepCalculating = true;
		private Calendar previousDate = null; // Used for simulation, when required
		private int incrementPerSecond = -1;  // Used for simulation

		public void stopCalculating() {
			keepCalculating = false;
		}

		@Override
		public void run() {
			boolean firstMove = true;
			while (keepCalculating) {
				Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Can come from GPS
				if ("true".equals(System.getProperty("date.simulation"))) {
					if (previousDate == null) {
						System.out.println("\tWill simulate the date for ASTRO calculation");
						// Init the date
						String startDate = System.getProperty("start.date.simulation"); // UTC Date
						if (startDate == null) {
							throw new RuntimeException("date.simulation required, start.date.simulation must be provided");
						} else {
							// Expect Duration Format 2020-03-06T07:20:00
							//                        |    |  |  |  |  |
							//                        |    |  |  |  |  17
							//                        |    |  |  |  14
							//                        |    |  |  11
							//                        |    |  8
							//                        |    5
							//                        0
							String patternStr = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}";
							Pattern pattern = Pattern.compile(patternStr);
							Matcher matcher = pattern.matcher(startDate);
							if (!matcher.matches()) {
								throw new RuntimeException(String.format("%s does not match expected format %s", startDate, patternStr));
							} else {
								System.out.println(String.format("\tSimulation starting %s", startDate));
								int year = Integer.parseInt(startDate.substring(0, 4));
								int month = Integer.parseInt(startDate.substring(5, 7));
								int day = Integer.parseInt(startDate.substring(8, 10));
								int hour = Integer.parseInt(startDate.substring(11, 13));
								int min = Integer.parseInt(startDate.substring(14, 16));
								int sec = Integer.parseInt(startDate.substring(17));
								// TODO Validate the fields...
								date.set(Calendar.YEAR, year);
								date.set(Calendar.MONTH, month - 1);
								date.set(Calendar.DAY_OF_MONTH, day);
								date.set(Calendar.HOUR_OF_DAY, hour);
								date.set(Calendar.MINUTE, min);
								date.set(Calendar.SECOND, sec);
								//
								previousDate = date;
							}
						}
					} else { // Increment, in seconds
						if (incrementPerSecond < 0) {
							String strInc = System.getProperty("increment.per.second");
							if (strInc == null) {
								throw new RuntimeException("date.simulation required, increment.per.second must be provided");
							}
							incrementPerSecond = Integer.parseInt(strInc);
							if (incrementPerSecond < 1) {
								throw new RuntimeException("increment.per.second must be greater than 0");
							}
							System.out.println(String.format("\tIncrementing date by %d s every %d second(s).", incrementPerSecond, (loopDelay / 1_000L)));
						} else {
							previousDate.add(Calendar.SECOND, incrementPerSecond);
							date.setTime(previousDate.getTime());
						}
					}
				} else {
					if (gpsDate == null) {
						Date at = new Date();
						date.setTime(at);
					} else {
						date.setTime(gpsDate);  // From GPS
					}
				}
				if (ASTRO_VERBOSE) {
					System.out.println("Starting Sun data calculation at " + date.getTime());
				}
				// TODO Make it non-static, and synchronized ?..
				AstroComputerV2 acv2 = new AstroComputerV2();
				acv2.calculate(date.get(Calendar.YEAR),
										date.get(Calendar.MONTH) + 1,
										date.get(Calendar.DAY_OF_MONTH),
										date.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
										date.get(Calendar.MINUTE),
										date.get(Calendar.SECOND));
				sunDecl = acv2.getSunDecl();
				sunGHA = acv2.getSunGHA();
				if (devicePosition != null) {
					DeadReckoning dr = new DeadReckoning(acv2.getSunGHA(),
							acv2.getSunDecl(),
							devicePosition.getLatitude(),
							devicePosition.getLongitude())
							.calculate();
					sunAzimuth = dr.getZ();
					sunElevation = dr.getHe();
					// Calculate Solar Date and TIme
					solarDate = getSolarDateFromEOT(date.getTime(), devicePosition.getLatitude(), devicePosition.getLongitude());
					if (ASTRO_VERBOSE) {
						System.out.println(String.format("At %s, from %s, Z: %.02f, Elev: %.02f ", date.getTime(), devicePosition, sunAzimuth, sunElevation));
					}
				} else {
					System.out.println("No position yet!");
				}
				long firstSlack = loopDelay;
				if ("true".equals(System.getProperty("date.simulation"))) {
					String firstMoveSlack = System.getProperty("first.move.slack");
					if (firstMoveSlack != null) {
						firstSlack = Integer.parseInt(firstMoveSlack) * 1_000L;
					}
				}
				delay(firstMove ? firstSlack : loopDelay);
				firstMove = false;
			}
		}
	}

	public enum EventType {
		CELESTIAL_DATA,
		DEVICE_DATA,
		MOVING_ELEVATION_START,
		MOVING_AZIMUTH_START,
		MOVING_ELEVATION_START_2,
		MOVING_AZIMUTH_START_2,
		MOVING_ELEVATION_END,
		MOVING_AZIMUTH_END,
		MOVING_ELEVATION_INFO,
		MOVING_AZIMUTH_INFO,
		DEVICE_INFO
	}

	public static int getTypeIndex(EventType eventType) {
		int index = -1;
		int i = 0;
		for (EventType et : EventType.values()) {
			if (et.equals(eventType)) {
				index = i;
				break;
			} else {
				i++;
			}
		}
		return index;
	}

	public static abstract class SunFlowerEventListener {
		public abstract void onNewMessage(EventType messageType, Object messageContent);
	}

	private final List<SunFlowerEventListener> listeners = new ArrayList<>();

	public void subscribe(SunFlowerEventListener listener) {
		listeners.add(listener);
	}
	public void unsubscribe(SunFlowerEventListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	private void publish(EventType messageType, Object messageContent) {
		listeners.forEach(listener -> listener.onNewMessage(messageType, messageContent));
	}

	public SunFlowerDriver() {

		if (sunFlowerVerbose) {
			System.out.println("Starting SunFlowerDriver");
		}

		String initialHeading = System.getProperty("initial.heading", String.valueOf(this.deviceHeading));
		try {
			this.deviceHeading = Double.parseDouble(initialHeading);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}

		int rpm = Integer.parseInt(System.getProperty("rpm", String.valueOf(DEFAULT_RPM))); // 30
//		System.out.println(String.format("RPM set to %d.", rpm));

		try {
			this.mh = new AdafruitMotorHAT(DEFAULT_STEPS_PER_REV); // Default addr 0x60

			this.azimuthMotor = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);   // Azimuth
			this.azimuthMotor.setSpeed(rpm); // Default 30 RPM

			this.elevationMotor = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M3_M4); // Elevation
			this.elevationMotor.setSpeed(rpm); // Default 30 RPM
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			simulating = true;
		}

		if (withSSD1306) {
			int width = 128;
			int height = 32;
			sb = new ScreenBuffer(width, height);
			try {
				oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS, width, height); // I2C interface
				oled.begin();
				oled.clear();
				//  oled.display();
			} catch (Throwable error) {
				// Not on a RPi? Try JPanel.
				oled = null;
				System.out.println("Displaying substitute Swing Led Panel");
				SwingLedPanel.ScreenDefinition screenDef = SwingLedPanel.ScreenDefinition.SSD1306_128x32;
				substitute = new SwingLedPanel(screenDef);
				substitute.setLedColor(Color.red);
				substitute.setVisible(true);
				int fontFactor = 2;
				sb.text("Substitute.", 2, (2 * fontFactor) + 5, fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
				substitute.setBuffer(sb.getScreenBuffer());
				substitute.display();
			}
		}
	}

	private final static class MotorPayload {
		AdafruitMotorHAT.MotorCommand motorCommand = AdafruitMotorHAT.MotorCommand.FORWARD;
		int nbSteps = 0;
	}

	private static MotorPayload getMotorPayload(double from, double to, double ratio) {
		return getMotorPayload(from, to, ratio, false);
	}
	private static MotorPayload getMotorPayload(double from, double to, double ratio, boolean inverted) {
		return getMotorPayload(Double.NaN, 0, from, to, ratio, inverted);
	}

	private final static boolean SPECIAL_DEBUG_VERBOSE = "true".equals(System.getProperty("special.debug.verbose"));
	// Recalculate from origin and get the diff with the currentStepOffset if origin != NaN
	// We start from the origin - and considering the current step offset, to avoid accumulating rounding errors.
	private static MotorPayload getMotorPayload(double origin, int currentStepOffset, double from, double to, double ratio, boolean inverted) {
		MotorPayload motorPayload = new MotorPayload();

		if (SPECIAL_DEBUG_VERBOSE) {
			System.out.println("+-----------------------------------------------------");
			System.out.println(String.format("Calculating motor payload, origin: %.02f, currentOffset: %d, from %.02f to %.02f (ratio: %.02f)",
					origin, currentStepOffset, from, to, ratio));
			System.out.println("+-----------------------------------------------------");
		}

//		motorPayload.motorCommand = (to > from) ?
//				(!inverted ? AdafruitMotorHAT.MotorCommand.FORWARD : AdafruitMotorHAT.MotorCommand.BACKWARD) :
//				(!inverted ? AdafruitMotorHAT.MotorCommand.BACKWARD : AdafruitMotorHAT.MotorCommand.FORWARD);

	  	// Motor: 200 steps: 360 degrees.
		// Device: 360 degrees = (200 / ratio) steps.
		if (Double.isNaN(origin) || !useStepsAccumulation) {
			int deltaSteps = (int) Math.round(((to - from) / 360d) * STEPS_PER_CIRCLE / ratio);
			motorPayload.motorCommand = (deltaSteps > 0) ?
					(!inverted ? AdafruitMotorHAT.MotorCommand.FORWARD : AdafruitMotorHAT.MotorCommand.BACKWARD) :
					(!inverted ? AdafruitMotorHAT.MotorCommand.BACKWARD : AdafruitMotorHAT.MotorCommand.FORWARD);
			motorPayload.nbSteps = Math.abs(deltaSteps);
		} else {
			// From origin
			int stepsFromOrigin = (int) Math.round(((to - origin) / 360d) * STEPS_PER_CIRCLE / ratio);
			int diff = stepsFromOrigin - currentStepOffset;

			if (SPECIAL_DEBUG_VERBOSE) {
				System.out.println(String.format(
						"\t** Before move: current offset: %d, from %.02f to %.02f, inverted:%s, StepsFromOrig: %d, diff:%d",
						currentStepOffset,
						from,
						to,
						inverted ? "true" : "false",
						stepsFromOrigin,
						diff));
			}

			diff *= (inverted ? -1 : 1);
			motorPayload.motorCommand = (diff > 0) ? AdafruitMotorHAT.MotorCommand.FORWARD : AdafruitMotorHAT.MotorCommand.BACKWARD;
//					(!inverted ? AdafruitMotorHAT.MotorCommand.FORWARD : AdafruitMotorHAT.MotorCommand.BACKWARD) :
//					(!inverted ? AdafruitMotorHAT.MotorCommand.BACKWARD : AdafruitMotorHAT.MotorCommand.FORWARD);

			if (SPECIAL_DEBUG_VERBOSE) {
				System.out.println(String.format(
						"Moving from %.02f to %.02f: %d step(s) (instead of %d, steps from origin: %d).",
						from,
						to,
						diff,
						(int) Math.round(((to - from) / 360d) * STEPS_PER_CIRCLE / ratio),
						stepsFromOrigin));
			}
			motorPayload.nbSteps = Math.abs(diff);
		}
		return motorPayload;
	}

	private void parkDevice() {
		if (currentDeviceElevation != PARKED_ELEVATION || currentDeviceAzimuth != PARKED_AZIMUTH) {
			if (ASTRO_VERBOSE) {
				System.out.println("---------------------------------------------------");
				System.out.println(">> Parking the device");
				System.out.println(String.format("\t Elev: from %.02f to %.02f", currentDeviceElevation, PARKED_ELEVATION));
				System.out.println(String.format("\t Z   : from %.02f to %.02f", currentDeviceAzimuth, PARKED_AZIMUTH));
				System.out.println("---------------------------------------------------");
			}
			this.publish(EventType.DEVICE_INFO, new DeviceInfo(new Date(), "Parking the device"));
			// Put Z to 0 or 180, Elev. to 90.

			// Parking from currentDeviceElevation to PARKED_ELEVA            response_content = json.dumps(response).encode()NOTION
			System.out.println(String.format("\t - Parking elevation %.02f -> %.02f", currentDeviceElevation, PARKED_ELEVATION));
			MotorPayload parkElev = getMotorPayload( // The 2 first parameters use the accumulated number of steps
				    PARKED_ELEVATION,
					currentDeviceElevationStepOffset,
					currentDeviceElevation,
					PARKED_ELEVATION,
					elevationMotorRatio,
					elevationInverted);
			String mess_1 = String.format("(Elev) This will be %d steps %s", parkElev.nbSteps, parkElev.motorCommand);
			if (MOVES_VERBOSE) {
				System.out.println(String.format("Parking %s", mess_1));
			}
			this.publish(EventType.MOVING_ELEVATION_INFO, new DeviceInfo(new Date(), mess_1));
			if (!simulating) {
				elevationMotorThread = new MotorThread(this.elevationMotor, parkElev.nbSteps, parkElev.motorCommand, motorStyle);
				elevationMotorThread.start();
			}
			currentDeviceElevation = PARKED_ELEVATION;
			currentDeviceElevationStepOffset += (parkElev.nbSteps * (parkElev.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (elevationInverted ? -1 : 1));

			// Parking from currentDeviceAzimuth to PARKED_AZIMUTH
			System.out.println(String.format("\t - Parking azimuth %.02f -> %.02f", currentDeviceAzimuth, PARKED_AZIMUTH));
			MotorPayload parkZ = getMotorPayload(  // The 2 first parameters use the accumulated number of steps
				    PARKED_AZIMUTH,
					currentDeviceAzimuthStepOffset,
					currentDeviceAzimuth,
					PARKED_AZIMUTH,
					azimuthMotorRatio,
					azimuthInverted);
			String mess_2 = String.format("(Z) This will be %d steps %s", parkZ.nbSteps, parkZ.motorCommand);
			if (MOVES_VERBOSE) {
				System.out.println(String.format("Parking %s", mess_2));
			}
			this.publish(EventType.MOVING_AZIMUTH_INFO, new DeviceInfo(new Date(), mess_2));
			if (!simulating) {
				azimuthMotorThread = new MotorThread(this.azimuthMotor, parkZ.nbSteps, parkZ.motorCommand, motorStyle);
				azimuthMotorThread.start();
			}
			currentDeviceAzimuth = PARKED_AZIMUTH;
			currentDeviceAzimuthStepOffset += (parkZ.nbSteps * (parkZ.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (azimuthInverted ? -1 : 1));
		} else {
			this.publish(EventType.DEVICE_INFO, new DeviceInfo(new Date(), "Device was parked"));
		}
	}

	/**
	 * Make the value a multiple of minDiffForMove
	 * @param sunValue the value to adjust, Azimuth or Elevation.
	 * @return the adjusted value
	 */
	private double adjustDeviceValue(double sunValue) {
		return adjustDeviceValue(sunValue, 0D, 180D);
	}
	private double adjustDeviceValue(double sunValue, double offset) {
		return adjustDeviceValue(sunValue, offset, 180D);
	}
	private double adjustDeviceValue(double sunValue, double offset, double heading) {
		double adjusted = sunValue + offset + (180 - heading); // if heading goes right, device must go left.
		if (adjusted % minDiffForMove != 0D) {
			adjusted = Math.round(adjusted * (1 / minDiffForMove)) / (1 / minDiffForMove);
		}
		return adjusted;
	}

	private void displayOled() {
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		boolean oneLine = false;
		int fontFactor = 2;
		if (oneLine) {
			fontFactor = 3;
			String display = String.format("%.01f/%.01f", currentDeviceElevation, currentDeviceAzimuth);
			sb.text(display, 2, (2 * fontFactor) + 1 /*(fontFact * 8)*/, fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
		} else {
			String lineOne = String.format(
					"El: %.02f", // "Dev. Elevation %.02f",
					currentDeviceElevation);
			String lineTwo = String.format(
					"Z:  %.02f", // "Dev. Azimuth %.02f",
					currentDeviceAzimuth);
			sb.text(lineOne, 2, 1 + (fontFactor * 3) + (0 * (fontFactor * 8)), fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(lineTwo, 2, 1 + (fontFactor * 3) + (1 * (fontFactor * 8)), fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
		}
		if (oled != null) {
			oled.setBuffer(sb.getScreenBuffer());
			try {
				oled.display();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (substitute != null) {
			substitute.setBuffer(sb.getScreenBuffer());
			substitute.display();
		}
	}

	private static Date gpsDate = null;
	private GPSReader gpsReader = null;

	public void start() {
		keepGoing = true;

		// If GSP required, start Serial Thread
		boolean dateFromGPS = "true".equals(System.getProperty("date.from.gps")); // =true"
		String serialPort = System.getProperty("gps.serial.port"); // =/dev/ttyS80", "/dev/tty.usbmodem141101"
		String baudRateStr = System.getProperty("gps.serial.baud.rate", "4800"); // =4800"

		if (dateFromGPS) {

			Thread gpsThread = new Thread(() -> {
				System.out.println("Start Reading the GPS"); // Date and Position
				// Get date and position from the GPS.
				Consumer<RMC> gpsConsumer = rmc -> {
					if ("true".equals(System.getProperty("gps.verbose", "false"))) {
						System.out.printf("GPS Date:%s, Position %s\n", rmc.getRmcDate(), rmc.getGp());
					}
					gpsDate = rmc.getRmcDate();
					if (rmc.getGp() != null) {
						setDevicePosition(rmc.getGp().lat, rmc.getGp().lng);
					}
					int fontFactor = 1;
					String displayDate = SDF_OLED.format(gpsDate);
					String latitude = "L: ";
					String longitude = "G: ";
					if (rmc.getGp() != null) {
						latitude += GeomUtil.decToSex(rmc.getGp().lat, GeomUtil.NO_DEG, GeomUtil.NS);
						longitude += GeomUtil.decToSex(rmc.getGp().lng, GeomUtil.NO_DEG, GeomUtil.EW);
					} else {
						latitude += "-";
						longitude += "-";
					}

					sb.clear();
					sb.text(displayDate, 2, 9, fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
					sb.text(latitude, 2, 19, fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
					sb.text(longitude, 2, 29, fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
					if (substitute != null) {
						substitute.setBuffer(sb.getScreenBuffer());
						substitute.display();
					}
					if (oled != null) {
						oled.setBuffer(sb.getScreenBuffer());
						try {
							oled.display();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				};
				this.gpsReader = new GPSReader(gpsConsumer, "RMC");

				gpsReader.startReading(serialPort, Integer.parseInt(baudRateStr));
			}, "GPSThread");
			gpsThread.start();
		}

		System.out.println("Stating AstroThread...");
		astroThread = new CelestialComputerThread();
		astroThread.start(); // Start calculating

		if (withSSD1306) {
			displayOled();
		}

		while (keepGoing) {
			Date date = (gpsDate != null ? gpsDate : new Date()); // System date... May come from a GPS (RMC)
			DeviceData deviceData = new DeviceData(date,
					devicePosition, currentDeviceAzimuth, currentDeviceElevation,
					azimuthOffset, elevationOffset, deviceHeading, currentDeviceAzimuthStepOffset, currentDeviceElevationStepOffset);
			SunData sunData = new SunData(date, sunAzimuth, sunElevation, sunDecl, sunGHA, solarDate, (solarDate != null ? solarDate.getTime() : 0L));
			this.publish(EventType.DEVICE_DATA, deviceData);
			this.publish(EventType.CELESTIAL_DATA, sunData);

			if (ASTRO_VERBOSE) {
				System.out.println(String.format(
						"Device : %s\nSun : %s",
						deviceData,
						sunData));
			}

			// Important: Re-frame the values of SunData with minDiffForMove, @see adjustDeviceValue
			if (astroThread.isAlive() && sunElevation >= 0) {
				boolean hasMoved = false;
				double adjustedAzimuth = adjustDeviceValue(sunAzimuth, azimuthOffset, this.deviceHeading);  // Use deviceHeading here
//				System.out.println(String.format("\tAzimuth adjusted from %.02f, with %.02f, to %.02f", sunAzimuth, azimuthOffset, adjustedAzimuth));

				if (Math.abs(currentDeviceAzimuth - adjustedAzimuth) >= minDiffForMove) { // Start a new thread each time a move is requested
					hasMoved = true;
					this.publish(EventType.MOVING_AZIMUTH_START, new DeviceAzimuthStart(new Date(), currentDeviceAzimuth, adjustedAzimuth));
					MotorPayload data = getMotorPayload(  // The 2 first parameters use the accumulated number of steps
						    PARKED_AZIMUTH,
							currentDeviceAzimuthStepOffset,
							currentDeviceAzimuth,
							adjustedAzimuth,
							azimuthMotorRatio,
							azimuthInverted);
					currentDeviceAzimuthStepOffset += (data.nbSteps * (data.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (azimuthInverted ? -1 : 1));
					if (SPECIAL_DEBUG_VERBOSE) {
						System.out.println(String.format(
								"\tAzimuthStepOffset now %d (command %s, inverted %s)",
								currentDeviceAzimuthStepOffset,
								data.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? "Frwd" : "Bkwd",
								azimuthInverted ? "true" : "false"));
					}
					double effectiveMove = ((data.motorCommand.equals(AdafruitMotorHAT.MotorCommand.FORWARD) ? 1 : -1) * ((data.nbSteps * (360d / 200d)) * azimuthMotorRatio));

					if (!simulating) {
						this.publish(EventType.MOVING_AZIMUTH_START_2, new MoveDetails(new Date(), data.nbSteps, data.motorCommand, this.azimuthMotor.getMotorNum()));
						if (azimuthMotorThread == null || (azimuthMotorThread != null && !azimuthMotorThread.isAlive())) {
							azimuthMotorThread = new MotorThread(this.azimuthMotor, data.nbSteps, data.motorCommand, motorStyle);
							azimuthMotorThread.start();
						} else {
							String mess3 = "Azimuth thread is already busy at work.";
							this.publish(EventType.MOVING_AZIMUTH_INFO, new DeviceInfo(new Date(), mess3));
						}
					}
					if (SPECIAL_DEBUG_VERBOSE) {
						System.out.println(String.format("Z. NbSteps(%s): %d => %.02f deg, ratio: %f, current Z: %.02f, Adj Z: %.02f, Effective: %.03f",
								data.motorCommand, data.nbSteps, (data.nbSteps * (360d / 200d)), azimuthMotorRatio, currentDeviceAzimuth, adjustedAzimuth, effectiveMove));
					}
					currentDeviceAzimuth += effectiveMove; // = adjustedAzimuth;
				}
				double adjustedElevation = adjustDeviceValue(Math.max(sunElevation, minimumAltitude), elevationOffset); // FIXME that one might have a problem?..
//				logWithTime(String.format("Elev: sun:%f, min:%d, currentDev:%f, adjusted:%f, minForMove:%f",
//						sunElevation,
//						minimumAltitude,
//						currentDeviceElevation,
//						adjustedElevation,
//						minDiffForMove));
				if (Math.abs(currentDeviceElevation - adjustedElevation) >= minDiffForMove) {
//					logWithTime("\tMoving!");
					hasMoved = true;
					this.publish(EventType.MOVING_ELEVATION_START, new DeviceElevationStart(new Date(), currentDeviceElevation, adjustedElevation));
					MotorPayload data = getMotorPayload(  // The 2 first parameters use the accumulated number of steps
							PARKED_ELEVATION,
							currentDeviceElevationStepOffset,
							currentDeviceElevation,
							adjustedElevation,
							elevationMotorRatio,
							elevationInverted);
					currentDeviceElevationStepOffset += (data.nbSteps * (data.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (elevationInverted ? -1 : 1));
					if (SPECIAL_DEBUG_VERBOSE) {
						System.out.println(String.format("\tElevationStepOffset now %d", currentDeviceElevationStepOffset));
					}
					double effectiveMove = ((data.motorCommand.equals(AdafruitMotorHAT.MotorCommand.FORWARD) ? 1 : -1) * ((data.nbSteps * (360d / 200d)) * elevationMotorRatio));

					if (!simulating) {
						this.publish(EventType.MOVING_ELEVATION_START_2, new MoveDetails(new Date(), data.nbSteps, data.motorCommand, this.elevationMotor.getMotorNum()));
					}
					if (!simulating) {
						if (elevationMotorThread == null || (elevationMotorThread != null && !elevationMotorThread.isAlive())) {
							elevationMotorThread = new MotorThread(this.elevationMotor, data.nbSteps, data.motorCommand, motorStyle);
							elevationMotorThread.start();
						} else {
							String mess3 = "Elevation thread is already busy at work.";
							this.publish(EventType.MOVING_ELEVATION_INFO, new DeviceInfo(new Date(), mess3));
						}
					}
					if (SPECIAL_DEBUG_VERBOSE) {
						System.out.println(String.format("Elev. NbSteps(%s): %d => %.02f deg, ratio: %f, current Elev: %.02f, Adj Elev: %.02f, Effective: %.03f",
								data.motorCommand, data.nbSteps, (data.nbSteps * (360d / 200d)), elevationMotorRatio, currentDeviceElevation, adjustedElevation, effectiveMove));
					}
					currentDeviceElevation += effectiveMove; // = adjustedElevation;
//				} else {
//					logWithTime("\t...NOT moving.");
				}
				if (hasMoved) {
					if (ASTRO_VERBOSE) {
						System.out.println(String.format("Sun's position is now: Elev: %s, Z: %.02f", GeomUtil.decToSex(sunElevation, GeomUtil.NO_DEG, GeomUtil.NONE), sunAzimuth));
					}
					if (MOVES_VERBOSE) {
//					DeviceData deviceData = new DeviceData(date, devicePosition, currentDeviceAzimuth, currentDeviceElevation, azimuthOffset, elevationOffset, deviceHeading);
						System.out.println(String.format(
								">> Device has moved, now: Elevation %.02f (stepOffset %d), Azimuth %.02f (stepOffset %d)",
								currentDeviceElevation,
								currentDeviceElevationStepOffset,
								currentDeviceAzimuth,
								currentDeviceAzimuthStepOffset));
					}
					// OLED Screen?
					if (withSSD1306) {
						displayOled();
					}
				}
			} else { // Park device
				parkDevice();
			}
			// Bottom of the loop
			delay(loopDelay);
		}
		// End
		if (withSSD1306) {
			// Clear the screen before shutting down.
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
			if (oled != null) {
				oled.setBuffer(sb.getScreenBuffer());
				try {
					oled.display();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				oled.shutdown();
			} else if (substitute != null) {
				substitute.setBuffer(sb.getScreenBuffer());
				substitute.display();
				substitute.dispose();
			}
		}
		System.out.println("\n\n\n... Done with the SunFlowerDriver program ...");
//	try { Thread.sleep(1_000); } catch (Exception ex) {} // Wait for the motors to be released.
	}

	/**
	 * Move the device from user's input, not from astro thread
	 */
	public void startManualCalibration() {
		String lastCommand = "";
		System.out.println("------------------------------ C A L I B R A T I O N ----------------------------");
		System.out.println("To change the Azimuth (Z) value, enter 'Z=12.34', the value goes from 0 to 360.");
		System.out.println("To change the Elevation (E) value, enter 'E=23.45', the values goes from 0 to 90.");
		System.out.println("Enter 'HIST' to see what you've done so far.");
		System.out.println("Enter 'PARK' to park the device.");
		System.out.println("Enter 'Q' to quit.");
		System.out.println("---------------------------------------------------------------------------------");
		boolean keepAsking = true;
		while (keepAsking) {
			System.out.println(String.format("Current status: Z=%.02f, Elev.=%.02f", currentDeviceAzimuth, currentDeviceElevation));
			if (withSSD1306) {
				displayOled();
			}

			String userInput = StaticUtil.userInput("> ");
			if (!userInput.isEmpty()) {
				lastCommand = userInput;
				if (!userInput.trim().equalsIgnoreCase("HIST")) {
					commandHistory.add(lastCommand);
				}
				if (userInput.trim().equalsIgnoreCase("Q") || userInput.trim().equalsIgnoreCase("QUIT")) {
					keepAsking = false;
				} else if (userInput.trim().equalsIgnoreCase("HIST")) {
					commandHistory.forEach(System.out::println);
				} else if (userInput.trim().equalsIgnoreCase("PARK")) {
					this.parkDevice();
				} else {
					String[] userData = userInput.trim().split("=");
					if (userData.length != 2) {
						System.out.println(String.format("Unknown input [%s], try something else.", userInput));
					} else {
						if (!userData[0].equalsIgnoreCase("Z") && !userData[0].equalsIgnoreCase("E")) {
							System.out.println(String.format("Choose E or Z, not [%s]", userData[0]));
						} else {
							double value = Double.NaN;
							try {
								value = Double.parseDouble(userData[1]);
							} catch (NumberFormatException nfe) {
								System.out.println(String.format("Bad numeric value [%s]", userData[1]));
							}
							if ( ! Double.isNaN(value) ) {
								// System.out.println(String.format("Setting %s to %f", userData[0].toUpperCase(), value));
								if (userData[0].equalsIgnoreCase("Z")) {
									if (value < 0 || value > 360) {
										System.out.println(String.format("Bad Azimuth value: %f, should be in [0..360]", value));
									} else {
										MotorPayload data = getMotorPayload(  // The 2 first parameters use the accumulated number of steps
												PARKED_AZIMUTH,
												currentDeviceAzimuthStepOffset,
												currentDeviceAzimuth,
												value,
												azimuthMotorRatio,
												azimuthInverted);
										if (!simulating) {
											if (azimuthMotorThread == null || (azimuthMotorThread != null && !azimuthMotorThread.isAlive())) {
												azimuthMotorThread = new MotorThread(this.azimuthMotor, data.nbSteps, data.motorCommand, motorStyle);
												azimuthMotorThread.start();
											} else {
												String mess3 = "Azimuth thread is already busy at work.";
												System.out.println(mess3);
											}
										}
										currentDeviceAzimuth = value;
										currentDeviceAzimuthStepOffset += (data.nbSteps * (data.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (azimuthInverted ? -1 : 1));
										if (SPECIAL_DEBUG_VERBOSE) {
											System.out.println(String.format("\tAzimuthStepOffset now %d", currentDeviceAzimuthStepOffset));
										}
									}
								}
								if (userData[0].equalsIgnoreCase("E")) {
									if (value < 0 || value > 90) {
										System.out.println(String.format("Bad Elevation value: %f, should be in [0..90]", value));
									} else {
										MotorPayload data = getMotorPayload(  // The 2 first parameters use the accumulated number of steps
												PARKED_ELEVATION,
												currentDeviceElevationStepOffset,
												currentDeviceElevation,
												value,
												elevationMotorRatio,
												elevationInverted);
										if (!simulating) {
											if (elevationMotorThread == null || (elevationMotorThread != null && !elevationMotorThread.isAlive())) {
												elevationMotorThread = new MotorThread(this.elevationMotor, data.nbSteps, data.motorCommand, motorStyle);
												elevationMotorThread.start();
											} else {
												String mess3 = "Elevation thread is already busy at work.";
												System.out.println(mess3);
											}
										}
										currentDeviceElevation = value;
										currentDeviceElevationStepOffset += (data.nbSteps * (data.motorCommand == AdafruitMotorHAT.MotorCommand.FORWARD ? 1 : -1) * (elevationInverted ? -1 : 1));
										if (SPECIAL_DEBUG_VERBOSE) {
											System.out.println(String.format("\tElevationStepOffset now %d", currentDeviceElevationStepOffset));
										}
									}
								}
							}
						}
					}
				}
			} else {
				System.out.println(String.format("Last command was [%s]", lastCommand));
			}
		}
		if (withSSD1306) {
			if (oled != null) {
				oled.shutdown();
			} else if (substitute != null) {
				substitute.dispose();
			}
		}
	}

	public void stop() {
		this.keepGoing = false;
		// Park the device
		parkDevice();
		long howMany = 0;
		while ((azimuthMotorThread != null && azimuthMotorThread.isAlive()) || (elevationMotorThread != null && elevationMotorThread.isAlive())) {
//			System.out.println("Waiting for the device to be parked");
			// TODO Move ANSI colors out of here.
			this.publish(EventType.DEVICE_INFO, new DeviceInfo(new Date(), ANSIUtil.ansiSetTextColor(howMany++ % 2 == 0 ? ANSIUtil.ANSI_GREEN : ANSIUtil.ANSI_RED) + "Waiting for the device to be parked"));
			delay(1_000L);
		}
		if (mh != null) {
			try { // Release all
				mh.getMotor(AdafruitMotorHAT.Motor.M1).run(AdafruitMotorHAT.MotorCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M2).run(AdafruitMotorHAT.MotorCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M3).run(AdafruitMotorHAT.MotorCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M4).run(AdafruitMotorHAT.MotorCommand.RELEASE);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		if (astroThread != null) {
			astroThread.stopCalculating();
		}
	}

	public void init() {
		// Display all system variables
		if (false) {
			Properties properties = System.getProperties();
			properties.forEach((k, v) -> System.out.println(k + ":" + v));
		}

		if (!"true".equals(System.getProperty("calibration"))) {
			if (SUN_FLOWER_VERBOSE) {
				System.out.println("Hit Ctrl-C to stop the SunFlowerDriver program and park the device");
			}
		}

		if (SUN_FLOWER_VERBOSE) {
			System.out.println(String.format(">> Motor Style set to %s", motorStyle.toString()));
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			System.out.println("\nShutting down, releasing resources.");
			if (gpsReader != null) {
				gpsReader.stopReading();
			}
			this.stop();
			try { Thread.sleep(5_000); } catch (Exception absorbed) {
				System.err.println("Ctrl-C: Oops!");
				absorbed.printStackTrace();
			}
		}, "Shutdown Hook"));

		String strLat = System.getProperty("device.lat");
		String strLng = System.getProperty("device.lng");
		if (strLat != null && strLng != null) {
			try {
				double lat = Double.parseDouble(strLat);
				double lng = Double.parseDouble(strLng);
				this.setDevicePosition(lat, lng);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		String minDiffStr = System.getProperty("min.diff.for.move", String.valueOf(MIN_DIFF_FOR_MOVE));
		try {
			minDiffForMove = Double.parseDouble(minDiffStr);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		// Ratios:
		String zRatioStr = System.getProperty("azimuth.ratio");
		if (zRatioStr != null) {
			String[] zData = zRatioStr.split(":");
			if (zData.length != 2) {
				throw new IllegalArgumentException(String.format("Expecting a value like '1:234', not %s", zRatioStr));
			}
			try {
				double num = Double.parseDouble(zData[0]);
				double den = Double.parseDouble(zData[1]);
				azimuthMotorRatio = num / den;
			} catch (NumberFormatException nfe) {
				System.err.println("Bad value");
				throw nfe;
			}
		}
		String elevRatioStr = System.getProperty("elevation.ratio");
		if (elevRatioStr != null) {
			String[] elevData = elevRatioStr.split(":");
			if (elevData.length != 2) {
				throw new IllegalArgumentException(String.format("Expecting a value like '1:234', not %s", elevRatioStr));
			}
			try {
				double num = Double.parseDouble(elevData[0]);
				double den = Double.parseDouble(elevData[1]);
				elevationMotorRatio = num / den;
			} catch (NumberFormatException nfe) {
				System.err.println("Bad value");
				throw nfe;
			}
		}
	}

	Logger getLogger() {
		return SunFlowerDriver.LOGGER;
	}

	synchronized SunFlowerDriver getInstance() {
		return instance;
	}

	/**
	 * System properties:
	 * rpm, default 30
	 * hat.debug, default false
	 * motor.style
	 * ... more.
	 *
	 * @param args Not used
	 */
	public static void main(String... args) {
		SunFlowerDriver sunFlowerDriver = new SunFlowerDriver();
		sunFlowerDriver.subscribe(new SunFlowerEventListener() {

			private EventType lastMessageType = null;

			@Override
			public void onNewMessage(EventType messageType, Object messageContent) {
				// Basic, just an example, a verbose spit.
				if (messageType != lastMessageType) {
					if (messageType != EventType.DEVICE_INFO &&
							messageType != EventType.CELESTIAL_DATA &&
							messageType != EventType.DEVICE_DATA) {
						System.out.println(String.format("Listener: %s: %s", messageType, messageContent.toString()));
					}
					lastMessageType = messageType;
				}
			}
		});
		sunFlowerDriver.init();

		if ("true".equals(System.getProperty("calibration"))) {
			sunFlowerDriver.startManualCalibration();
		} else {
			sunFlowerDriver.start();
		}

		System.out.println(">> Exiting SunFlowerDriver, Bye!");
	}
}
