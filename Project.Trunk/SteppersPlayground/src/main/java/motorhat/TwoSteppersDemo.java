package motorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 */
public class TwoSteppersDemo {
	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;

	private boolean keepGoing = true;
	private final static String DEFAULT_RPM = "30";
	int nbSteps = 100;

	private static int nbStepsPerRev = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS; // 200 steps per rev

	public TwoSteppersDemo(int motorNum) throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo");
		int rpm = Integer.parseInt(System.getProperty("rpm", DEFAULT_RPM));
		System.out.println(String.format("RPM set to %d.", rpm));

		nbSteps = Integer.parseInt(System.getProperty("steps", "100"));

		this.mh = new AdafruitMotorHAT(nbStepsPerRev); // Default addr 0x60
		this.stepper = mh.getStepper(motorNum);
		this.stepper.setSpeed(rpm); // Default 30 RPM
	}

	public void go() {
		keepGoing = true;

		Thread demoThread = new Thread(() -> {
			while (keepGoing) {
				try {
					System.out.println(String.format(
							"-----------------------------------------------------------------------------------\n" +
							"Motor # %d, RPM set to %f, %d Steps per Rev, %f sec per step, %d steps per move.\n" +
							"-----------------------------------------------------------------------------------",
							this.stepper.getMotorNum(),
							this.stepper.getRPM(),
							this.stepper.getStepPerRev(),
							this.stepper.getSecPerStep(),
							nbSteps));
					if (keepGoing) {
						System.out.println("-- 1. Single coil steps --");
						System.out.println("  Forward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.FORWARD, AdafruitMotorHAT.Style.SINGLE);
					}
					if (keepGoing) {
						System.out.println("  Backward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.BACKWARD, AdafruitMotorHAT.Style.SINGLE);
					}
					if (keepGoing) {
						System.out.println("-- 2. Double coil steps --");
						System.out.println("  Forward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.FORWARD, AdafruitMotorHAT.Style.DOUBLE);
					}
					if (keepGoing) {
						System.out.println("  Backward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.BACKWARD, AdafruitMotorHAT.Style.DOUBLE);
					}
					if (keepGoing) {
						System.out.println("-- 3. Interleaved coil steps --");
						System.out.println("  Forward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.FORWARD, AdafruitMotorHAT.Style.INTERLEAVE);
					}
					if (keepGoing) {
						System.out.println("  Backward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.BACKWARD, AdafruitMotorHAT.Style.INTERLEAVE);
					}
					if (keepGoing) {
						System.out.println("-- 4. Microsteps --");
						System.out.println("  Forward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
					}
					if (keepGoing) {
						System.out.println("  Backward");
						this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.BACKWARD, AdafruitMotorHAT.Style.MICROSTEP);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				System.out.println("==== Again! ====");
			}
		});

		demoThread.start();

		System.out.println("... Done with the demo ...");
//	try { Thread.sleep(1_000); } catch (Exception ex) {} // Wait for the motors to be released.
	}

	public void stop() {
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
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {

		final TwoSteppersDemo demoOne = new TwoSteppersDemo(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		final TwoSteppersDemo demoTwo = new TwoSteppersDemo(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M3_M4);

		System.out.println("Hit Ctrl-C to stop the demo");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (demoOne != null) {
				demoOne.stop();
			}
			if (demoTwo != null) {
				demoTwo.stop();
			}
			try {
				Thread.sleep(5_000);
			} catch (Exception absorbed) {
			}
		}, "Shutdown Hook"));

		demoOne.go();
		try { Thread.sleep(1_500); } catch (Exception ignore) {}
		demoTwo.go();

		System.out.println("Bye.");
	}
}
