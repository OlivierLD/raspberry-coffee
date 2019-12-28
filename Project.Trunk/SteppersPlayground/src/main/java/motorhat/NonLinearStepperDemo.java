package motorhat;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 */
public class NonLinearStepperDemo {

	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;
	private int nbSteps;

	private NonLinearStepperDemo() throws I2CFactory.UnsupportedBusNumberException {

		System.out.println("Starting Stepper Demo, non-linear speed.");
		nbSteps = Integer.parseInt(System.getProperty("steps", String.valueOf(AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS)));

		this.mh = new AdafruitMotorHAT(nbSteps); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
	}

	private void go() {
		try {

			nbSteps = 3;
			for (double speed = 0.1; speed <= 60.0; speed += 0.1) {
				this.stepper.setSpeed(speed);
				System.out.println(String.format(
						"-----------------------------------------------------------------------------------\n" +
								"Motor #%d, RPM set to %f, %d Steps per Rev, %f sec per step, %d steps per move.",
						this.stepper.getMotorNum(),
						this.stepper.getRPM(),
						this.stepper.getStepPerRev(),
						this.stepper.getSecPerStep(),
						nbSteps));
				this.stepper.step(nbSteps, AdafruitMotorHAT.MotorCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
				System.out.println(String.format("Speed now %f", speed));
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("... Done with this demo ...");
//	try { Thread.sleep(1_000); } catch (Exception ex) {} // Wait for the motors to be released.
		stop();
	}

	private void stop() {
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
	 * hat.debug, default false
	 *
	 * @param args Not used
	 * @throws Exception if anything fails...
	 */
	public static void main(String... args) throws Exception {
		NonLinearStepperDemo demo = new NonLinearStepperDemo();
		System.out.println("Hit Ctrl-C to stop the demo before its end");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			demo.stop();
			try {
				Thread.sleep(5_000);
			} catch (Exception absorbed) {
				System.err.println("Ctrl-C: Oops!");
				absorbed.printStackTrace();
			}
		}, "Shutdown Hook"));

		demo.go();

		System.out.println("Bye.");
	}
}
