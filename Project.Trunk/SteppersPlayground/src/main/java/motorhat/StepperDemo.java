package motorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 */
public class StepperDemo {
	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;

	private boolean keepGoing = true;
	private final static int DEFAULT_RPM = 30;
	private int nbSteps;

	private StepperDemo() throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo");
		int rpm = Integer.parseInt(System.getProperty("rpm", String.valueOf(DEFAULT_RPM)));
		System.out.println(String.format("RPM set to %d.", rpm));

		nbSteps = Integer.parseInt(System.getProperty("steps", String.valueOf(AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS)));

		this.mh = new AdafruitMotorHAT(nbSteps); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		this.stepper.setSpeed(rpm); // Default 30 RPM
	}

	private void go() {
		keepGoing = true;
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
		StepperDemo demo = new StepperDemo();
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
