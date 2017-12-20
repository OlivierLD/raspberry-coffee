package i2c.samples.motorHAT;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

/*
 * See https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors
 */
public class StepperDemo {
	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitStepperMotor stepper;

	private boolean keepGoing = true;
	private final static String DEFAULT_RPM = "30";

	private static int nbStepsPerRev = AdafruitMotorHAT.AdafruitStepperMotor.DEFAULT_NB_STEPS;

	public StepperDemo() throws I2CFactory.UnsupportedBusNumberException {
		this.mh = new AdafruitMotorHAT(nbStepsPerRev); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		this.stepper.setSpeed(Double.parseDouble(System.getProperty("rpm", DEFAULT_RPM))); // Default 30 RPM
	}

	public void go() {
		keepGoing = true;
		while (keepGoing) {
			try {
				System.out.println("Single coil steps");
				System.out.println("  Forward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.SINGLE);
				System.out.println("  Backward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.SINGLE);
				System.out.println("Double coil steps");
				System.out.println("  Forward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.DOUBLE);
				System.out.println("  Backward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.DOUBLE);
				System.out.println("Interleaved coil steps");
				System.out.println("  Forward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.INTERLEAVE);
				System.out.println("  Backward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.INTERLEAVE);
				System.out.println("Microsteps");
				System.out.println("  Forward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
				System.out.println("  Backward");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.MICROSTEP);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.out.println("........... again");
		}
		System.out.println("Done with the demo");
	}

	public void stop() {
		this.keepGoing = false;
		if (mh != null) {
			try {
				mh.getMotor(AdafruitMotorHAT.Motor.M1).run(AdafruitMotorHAT.ServoCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M2).run(AdafruitMotorHAT.ServoCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M3).run(AdafruitMotorHAT.ServoCommand.RELEASE);
				mh.getMotor(AdafruitMotorHAT.Motor.M4).run(AdafruitMotorHAT.ServoCommand.RELEASE);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String args[]) throws Exception {
		StepperDemo demo = new StepperDemo();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			demo.stop();
			try { Thread.sleep(1_000); } catch (Exception absorbed) {}
		}));

		demo.go();

		System.out.println("Done.");
	}
}
