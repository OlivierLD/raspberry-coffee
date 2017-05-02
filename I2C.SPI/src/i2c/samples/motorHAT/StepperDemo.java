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

	public StepperDemo() throws I2CFactory.UnsupportedBusNumberException {
		this.mh = new AdafruitMotorHAT(); // Default addr 0x60
		this.stepper = mh.getStepper(AdafruitMotorHAT.AdafruitStepperMotor.PORT_M1_M2);
		this.stepper.setSpeed(30d); // 30 RPM

		while (true) {
			try {
				System.out.println("Single coil steps");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.SINGLE);
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.SINGLE);
				System.out.println("Double coil steps");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.DOUBLE);
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.DOUBLE);
				System.out.println("Interleaved coil steps");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.INTERLEAVE);
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.INTERLEAVE);
				System.out.println("Microsteps");
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.FORWARD, AdafruitMotorHAT.Style.MICROSTEP);
				this.stepper.step(100, AdafruitMotorHAT.ServoCommand.BACKWARD, AdafruitMotorHAT.Style.MICROSTEP);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String args[]) throws Exception {
		StepperDemo omd = new StepperDemo();

		System.out.println("Done.");
	}
}
