package sunflower;

import calc.DeadReckoning;
import calc.calculation.AstroComputer;
import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;
import nmea.parser.GeoPos;
import utils.StaticUtil;
import utils.StringUtils;
import utils.TimeUtil;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static utils.TimeUtil.delay;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 *
 * RPM : Revolution Per Minute, speed of the rotation.
 * Steps per Revolution: How many steps for 360 degrees.
 * nbSteps: How many steps should the shaft do when started.
 */
public class SunFlowerDriver {

	private static GeoPos devicePosition = null; // Can be fed from a GPS, or manually (System variable).
	private static double sunAzimuth = 0d;
	private static double sunElevation = 0d;

	private boolean simulating = false;

	private AdafruitMotorHAT mh;

	private AdafruitMotorHAT.AdafruitStepperMotor azimuthMotor;
	private AdafruitMotorHAT.AdafruitStepperMotor elevationMotor;

	private CelestialComputerThread astroThread = null;

	private double azimuthMotorRatio   = 1d / 40d;
	private double elevationMotorRatio = 1d / 6.4;

	private boolean keepGoing = true;
	private final static int DEFAULT_RPM = 30;
	private final static int DEFAULT_STEPS_PER_REV = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS;
	private int nbSteps;

	private final static boolean MOTOR_HAT_VERBOSE = "true".equals(System.getProperty("motor.hat.verbose"));

	public void setDevicePosition(double lat, double lng) {
		this.devicePosition = new GeoPos(lat, lng);
	}

	private static class MotorThread extends Thread {
		private AdafruitMotorHAT.AdafruitStepperMotor stepper;
		private int nbSteps;
		private AdafruitMotorHAT.MotorCommand motorCommand;
		private AdafruitMotorHAT.Style motorStyle;

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
				this.stepper.step(nbSteps, motorCommand, motorStyle, t -> {
					if (MOTOR_HAT_VERBOSE) {
						// t.printStackTrace();
						System.out.println(String.format("\t\tToo long! %s", t));
					}
				});
				if (MOTOR_HAT_VERBOSE) {
					long after = System.currentTimeMillis();
					System.out.println(String.format("\tMove completed in: %s", TimeUtil.fmtDHMS(TimeUtil.msToHMS(after - before))));
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private static class CelestialComputerThread extends Thread {
		private boolean keepCalculating = true;

		public void stopCalculating() {
			keepCalculating = false;
		}

		@Override
		public void run() {
			while (keepCalculating) {

				Date at = new Date();
				Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
				date.setTime(at);
				if ("true".equals(System.getProperty("astro.verbose", "false"))) {
					System.out.println("Starting Sun data calculation at " + date.getTime());
				}
				// TODO Make it non-static, and synchronized
				AstroComputer.calculate(
						date.get(Calendar.YEAR),
						date.get(Calendar.MONTH) + 1,
						date.get(Calendar.DAY_OF_MONTH),
						date.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						date.get(Calendar.MINUTE),
						date.get(Calendar.SECOND));
				if (devicePosition != null) {
					DeadReckoning dr = new DeadReckoning(AstroComputer.getSunGHA(), AstroComputer.getSunDecl(), devicePosition.lat, devicePosition.lng);
					dr.calculate();
					sunAzimuth = dr.getZ();
					sunElevation = dr.getHe();
					if ("true".equals(System.getProperty("astro.verbose", "false"))) {
						System.out.println(String.format("At %s, from %s, Z: %.02f, Elev: %.02f ", date.getTime(), devicePosition, sunAzimuth, sunElevation));
					}
				} else {
					System.out.println("No position yet!");
				}
				delay(1_000L);
			}
		}
	}

	private SunFlowerDriver() throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo");
		int rpm = Integer.parseInt(System.getProperty("rpm", String.valueOf(DEFAULT_RPM)));
		System.out.println(String.format("RPM set to %d.", rpm));

		nbSteps = Integer.parseInt(System.getProperty("steps", String.valueOf(200)));

		try {
			this.mh = new AdafruitMotorHAT(DEFAULT_STEPS_PER_REV); // Default addr 0x60
			this.azimuthMotor = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
			this.azimuthMotor.setSpeed(rpm); // Default 30 RPM
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			simulating = true;
		}
	}

	enum SupportedUserInput {

		FORWARD("FORWARD", "Set the direction to 'FORWARD'"),
		BACKWARD("BACKWARD", "Set the direction to 'BACKWARD'"),
		SINGLE("SINGLE", "Set the style to 'SINGLE'"),
		DOUBLE("DOUBLE", "Set the style to 'DOUBLE'"),
		INTERLEAVE("INTERLEAVE", "Set the style to 'INTERLEAVE'"),
		MICROSTEP("MICROSTEP", "Set the style to 'MICROSTEP'"),
		RPM("RPM xxx", "Set the Revolution Per Minute to 'xxx', as integer"),
		STEPS("STEPS yyy", "Set the Number of Steps to make to 'yyy', as integer"),
		STEPSPERREV("STEPSPERREV zzz", "Set the Steps Per Revolution to 'zzz', as integer"),
		GO("GO", "Apply current settings and runs the motor for the required number  of steps"),
		OUT("OUT", "Release the motor and exit."),
		QUIT("QUIT", "Same as 'OUT'"),
		HELP("HELP", "Display command list");

		private final String command;
		private final String description;
		SupportedUserInput(String command, String description) {
			this.command = command;
			this.description = description;
		}

		String command() {
			return this.command;
		}
		String description() {
			return this.description;
		}
	}

	private void displayHelp() {
		int longestCommand = Arrays.stream(SupportedUserInput.values())
				.map(sui -> sui.command().length())
				.max(Integer::compare)
				.get();
		System.out.println("Set your options, and enter 'GO' to start the motor.");
		System.out.println("Options are (lowercase supported):");
		Arrays.asList(SupportedUserInput.values())
				.forEach(sui -> System.out.println(String.format("     - %s\t%s",
						StringUtils.rpad(sui.command(), longestCommand + 1),
						sui.description())));
	}

	private void go() {
		keepGoing = true;
		AdafruitMotorHAT.MotorCommand motorCommand = AdafruitMotorHAT.MotorCommand.FORWARD; // Default
		AdafruitMotorHAT.Style motorStyle = AdafruitMotorHAT.Style.SINGLE;                  // Default

		MotorThread motorThread = null;
		astroThread = new CelestialComputerThread();
		astroThread.start();

		displayHelp();

		while (keepGoing) {
			if (!simulating) {
				try {
					System.out.println(String.format(
							"--- Current Status ---------------------------------------------------------------------\n" +
									"Motor # %d, RPM set to %.02f, %d Steps per Rev, %.03f millisec per step, taking %d steps.\n" +
									" -> this will be %.01f degrees in ~ %s ms\n" +
									"Command %s, Style %s \n" +
									"----------------------------------------------------------------------------------------",
							this.azimuthMotor.getMotorNum(),
							this.azimuthMotor.getRPM(),
							this.azimuthMotor.getStepPerRev(),
							this.azimuthMotor.getSecPerStep() * 1_000,
							nbSteps,
							(360d * (double) nbSteps / (double) this.azimuthMotor.getStepPerRev()),
							NumberFormat.getInstance().format(nbSteps * this.azimuthMotor.getSecPerStep() * 1_000),
							motorCommand, motorStyle));

					String userInput = StaticUtil.userInput("Your option ? > ").toUpperCase();
					boolean startMotor = false;

					switch (userInput) {
						case "FORWARD":
							motorCommand = AdafruitMotorHAT.MotorCommand.FORWARD;
							break;
						case "BACKWARD":
							motorCommand = AdafruitMotorHAT.MotorCommand.BACKWARD;
							break;
						case "SINGLE":
							motorStyle = AdafruitMotorHAT.Style.SINGLE;
							break;
						case "DOUBLE":
							motorStyle = AdafruitMotorHAT.Style.DOUBLE;
							break;
						case "INTERLEAVE":
							motorStyle = AdafruitMotorHAT.Style.INTERLEAVE;
							break;
						case "MICROSTEP":
							motorStyle = AdafruitMotorHAT.Style.MICROSTEP;
							break;
						case "GO":
							startMotor = true;
							break;
						case "HELP":
							displayHelp();
							break;
						case "OUT":
						case "QUIT":
							keepGoing = false;
							if (motorThread != null) {
								motorThread.interrupt();
							}
							stop();
							break;
						default:
							if (userInput.startsWith("RPM ")) {
								try {
									int rpm = Integer.parseInt(userInput.substring("RPM ".length()));
									this.azimuthMotor.setRPM(rpm);
								} catch (NumberFormatException nfe) {
									nfe.printStackTrace();
								}
							} else if (userInput.startsWith("STEPS ")) {
								try {
									nbSteps = Integer.parseInt(userInput.substring("STEPS ".length()));
								} catch (NumberFormatException nfe) {
									nfe.printStackTrace();
								}
							} else if (userInput.startsWith("STEPSPERREV ")) {
								try {
									int steps = Integer.parseInt(userInput.substring("STEPSPERREV ".length()));
									this.azimuthMotor.setStepPerRev(steps);
								} catch (NumberFormatException nfe) {
									nfe.printStackTrace();
								}
							} else {
								if (!"".equals(userInput)) {
									System.out.println(String.format("[%s] not supported.", userInput));
								}
							}
							break;
					}

					if (startMotor) {
						motorThread = new MotorThread(this.azimuthMotor, nbSteps, motorCommand, motorStyle);
						motorThread.start();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("... Done with the demo ...");
//	try { Thread.sleep(1_000); } catch (Exception ex) {} // Wait for the motors to be released.
	}

	private void stop() {
		this.keepGoing = false;
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

	/**
	 * System properties:
	 * rpm, default 30
	 * hat.debug, default false
	 *
	 * @param args Not used
	 * @throws Exception if anything fails...
	 */
	public static void main(String... args) throws Exception {
		SunFlowerDriver sunFlowerDriver = new SunFlowerDriver();
		System.out.println("Hit Ctrl-C to stop the demo (or OUT at the prompt)");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			sunFlowerDriver.stop();
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
				sunFlowerDriver.setDevicePosition(lat, lng);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		sunFlowerDriver.go();

		System.out.println("Bye.");
	}
}
