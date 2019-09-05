package i2c.samples.motorHAT;

import com.pi4j.io.i2c.I2CFactory;
import i2c.motor.adafruitmotorhat.AdafruitMotorHAT;

import java.io.IOException;

/**
 * A robot, with 2 wheels.
 * Each wheel is moved by a DC motor.
 */
public class Robot {
	private int addr = 0x60;    // The I2C address of the motor HAT, default is 0x60.
	private AdafruitMotorHAT.Motor leftMotorID = AdafruitMotorHAT.Motor.M1; // The ID of the left motor, default is 1.
	private AdafruitMotorHAT.Motor rightMotorID = AdafruitMotorHAT.Motor.M2; // The ID of the right motor, default is 2.
	private int leftTrim = 0;  // Amount to offset the speed of the left motor, can be positive or negative and use useful for matching the speed of both motors.  Default is 0.
	private int rightTrim = 0;  // Amount to offset the speed of the right motor (see above).
	private boolean stopOnExit = true; //  Boolean to indicate if the motors should stop on program exit. Default is true (highly recommended to keep this value to prevent damage to the bot on program crash!).

	private AdafruitMotorHAT mh;
	private AdafruitMotorHAT.AdafruitDCMotor leftMotor;
	private AdafruitMotorHAT.AdafruitDCMotor rightMotor;

	public Robot() throws I2CFactory.UnsupportedBusNumberException {
		this.mh = new AdafruitMotorHAT();
		this.leftMotor = mh.getMotor(leftMotorID);
		this.rightMotor = mh.getMotor(rightMotorID);
		// Start with the motors turned off
		try {
			this.leftMotor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
			this.rightMotor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// if stopOnExit...
	}

	public final static void delay(float delay) {
		try {
			Thread.sleep(Math.round(delay * 1_000L));
		} catch (InterruptedException ie) {
		}
	}

	public void stop() {
		try {
			this.leftMotor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
			this.rightMotor.run(AdafruitMotorHAT.ServoCommand.RELEASE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void leftSpeed(int speed) throws IllegalArgumentException, IOException {
		if (speed < 0 || speed > 255) {
			throw new IllegalArgumentException("Speed must be an int belonging to [0, 255]");
		}
		int leftSpeed = speed + this.leftTrim;
		leftSpeed = Math.max(0, Math.min(255, leftSpeed));
		this.leftMotor.setSpeed(leftSpeed);
	}

	public void rightSpeed(int speed) throws IllegalArgumentException, IOException {
		if (speed < 0 || speed > 255) {
			throw new IllegalArgumentException("Speed must be an int belonging to [0, 255]");
		}
		int rightSpeed = speed + this.rightTrim;
		rightSpeed = Math.max(0, Math.min(255, rightSpeed));
		this.rightMotor.setSpeed(rightSpeed);
	}

	public void forward(int speed) throws IOException {
		forward(speed, 0);
	}

	public void forward(int speed, float seconds) throws IOException {
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
		this.leftMotor.run(AdafruitMotorHAT.ServoCommand.FORWARD);
		this.rightMotor.run(AdafruitMotorHAT.ServoCommand.FORWARD);
		if (seconds > 0) {
			delay(seconds);
			this.stop();
		}
	}

	public void backward(int speed) throws IOException {
		backward(speed, 0);
	}

	public void backward(int speed, float seconds) throws IOException {
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
		this.leftMotor.run(AdafruitMotorHAT.ServoCommand.BACKWARD);
		this.rightMotor.run(AdafruitMotorHAT.ServoCommand.BACKWARD);
		if (seconds > 0) {
			delay(seconds);
			this.stop();
		}
	}

	public void right(int speed) throws IOException {
		right(speed, 0);
	}

	public void right(int speed, float seconds) throws IOException {
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
		this.leftMotor.run(AdafruitMotorHAT.ServoCommand.FORWARD);
		this.rightMotor.run(AdafruitMotorHAT.ServoCommand.BACKWARD);
		if (seconds > 0) {
			delay(seconds);
			this.stop();
		}
	}

	public void left(int speed) throws IOException {
		left(speed, 0);
	}

	public void left(int speed, float seconds) throws IOException {
		this.leftMotor.setSpeed(speed);
		this.rightMotor.setSpeed(speed);
		this.leftMotor.run(AdafruitMotorHAT.ServoCommand.BACKWARD);
		this.rightMotor.run(AdafruitMotorHAT.ServoCommand.FORWARD);
		if (seconds > 0) {
			delay(seconds);
			this.stop();
		}
	}
}
