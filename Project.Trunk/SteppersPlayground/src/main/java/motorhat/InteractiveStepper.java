package motorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;
import utils.StaticUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 *
 * RPM : Revolution Per Minute, speed of the rotation.
 * Steps per Revolution: How many steps for 360 degrees.
 * nbSteps: How many steps should the shaft do.
 */
public class InteractiveStepper {
	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;

	private boolean keepGoing = true;
	private final static int DEFAULT_RPM = 30;
	private final static int DEFAULT_STEPS_PER_REV = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS;
	private int nbSteps;

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
				this.stepper.step(nbSteps, motorCommand, motorStyle);
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	private InteractiveStepper() throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo");
		int rpm = Integer.parseInt(System.getProperty("rpm", String.valueOf(DEFAULT_RPM)));
		System.out.println(String.format("RPM set to %d.", rpm));

		nbSteps = Integer.parseInt(System.getProperty("steps", String.valueOf("150")));

		this.mh = new AdafruitMotorHAT(DEFAULT_STEPS_PER_REV); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		this.stepper.setSpeed(rpm); // Default 30 RPM
	}

	private final List<String> supportedUserInput = Arrays.asList(
			"FORWARD",
			"BACKWARD",
			"BRAKE",
			"RELEASE",
			"SINGLE",
			"DOUBLE",
			"INTERLEAVE",
			"MICROSTEP",
			"RPM XXX",
      "STEPS YYYY",
			"STEPSPERREV ZZZZ",
			"GO",
			"OUT"
	);

	private void go() {
		keepGoing = true;
		AdafruitMotorHAT.MotorCommand motorCommand = AdafruitMotorHAT.MotorCommand.FORWARD;
		AdafruitMotorHAT.Style motorStyle = AdafruitMotorHAT.Style.SINGLE;

		MotorThread motorThread = null;
		while (keepGoing) {
			try {
				System.out.println(String.format(
						    "--- Current Status ---------------------------------------------------------------------\n" +
								"Motor # %d, RPM set to %.02f, %d Steps per Rev, %f millisec per step, taking %d steps.\n" +
								"----------------------------------------------------------------------------------------",
						this.stepper.getMotorNum(),
						this.stepper.getRPM(),
						this.stepper.getStepPerRev(),
						this.stepper.getSecPerStep() * 1_000,
						nbSteps));
				System.out.println("Enter your options:");
				System.out.println("Command:");
				supportedUserInput.forEach(cmd -> System.out.println(String.format("     - %s", cmd)));

				String userInput = StaticUtil.userInput("? > ").toUpperCase();
				boolean startMotor = false;

				switch (userInput) {
					case "FORWARD":
						motorCommand = AdafruitMotorHAT.MotorCommand.FORWARD;
						break;
					case "BACKWARD":
						motorCommand = AdafruitMotorHAT.MotorCommand.BACKWARD;
						break;
					case "BRAKE":
						motorCommand = AdafruitMotorHAT.MotorCommand.BRAKE;
						break;
					case "RELEASE":
						motorCommand = AdafruitMotorHAT.MotorCommand.RELEASE;
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
					case "OUT":
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
								int steps = Integer.parseInt(userInput.substring("STEPS ".length()));
								nbSteps = steps;
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
							System.out.println(String.format("%s not supported.", userInput));
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
		System.out.println("Hit Ctrl-C to stop the demo");
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
