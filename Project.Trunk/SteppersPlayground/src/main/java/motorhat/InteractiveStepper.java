package motorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;
import utils.StaticUtil;
import utils.StringUtils;
import utils.TimeUtil;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 *
 * RPM : Revolution Per Minute, speed of the rotation.
 * Steps per Revolution: How many steps for 360 degrees.
 * nbSteps: How many steps should the shaft do when started.
 */
public class InteractiveStepper {
	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;

	private boolean keepGoing = true;
	private final static int DEFAULT_RPM = 30;
	private final static int DEFAULT_STEPS_PER_REV = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS;
	private int nbSteps;

	private final static boolean MOTOR_HAT_VERBOSE = "true".equals(System.getProperty("motor.hat.verbose"));

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
				if (true || MOTOR_HAT_VERBOSE) {
					long after = System.currentTimeMillis();
					System.out.println(String.format("\tMove completed in: %s", TimeUtil.fmtDHMS(TimeUtil.msToHMS(after - before))));
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private InteractiveStepper() throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo");
		int rpm = Integer.parseInt(System.getProperty("rpm", String.valueOf(DEFAULT_RPM)));
		System.out.println(String.format("RPM set to %d.", rpm));

		nbSteps = Integer.parseInt(System.getProperty("steps", String.valueOf(200)));

		this.mh = new AdafruitMotorHAT(DEFAULT_STEPS_PER_REV); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		this.stepper.setSpeed(rpm); // Default 30 RPM
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

		displayHelp();

		while (keepGoing) {
			try {
				System.out.println(String.format(
						    "--- Current Status ---------------------------------------------------------------------\n" +
								"Motor # %d, RPM set to %.02f, %d Steps per Rev, %.03f millisec per step, taking %d steps.\n" +
								 " -> this will be %.01f degrees in ~ %s ms\n" +
								"Command %s, Style %s \n" +
								"----------------------------------------------------------------------------------------",
						this.stepper.getMotorNum(),
						this.stepper.getRPM(),
						this.stepper.getStepPerRev(),
						this.stepper.getSecPerStep() * 1_000,
						nbSteps,
						(360d * (double)nbSteps / (double)this.stepper.getStepPerRev()),
						NumberFormat.getInstance().format(nbSteps * this.stepper.getSecPerStep() * 1_000),
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
								this.stepper.setRPM(rpm);
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
								this.stepper.setStepPerRev(steps);
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
					motorThread = new MotorThread(this.stepper, nbSteps, motorCommand, motorStyle);
					motorThread.start();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
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
		InteractiveStepper demo = new InteractiveStepper();
		System.out.println("Hit Ctrl-C to stop the demo (or OUT at the prompt)");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			demo.stop();
			try { Thread.sleep(5_000); } catch (Exception absorbed) {
				System.err.println("Ctrl-C: Oops!");
				absorbed.printStackTrace();
			}
		}, "Shutdown Hook"));

		demo.go();

		System.out.println("Bye.");
	}
}
