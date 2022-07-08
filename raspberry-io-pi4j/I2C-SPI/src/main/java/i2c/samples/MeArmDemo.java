package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;

import static utils.StaticUtil.userInput;
import static utils.TimeUtil.delay;

/*
 * Executes a given set fo commands to drive a MeArm.
 *
 * Uses a PCA9685 (I2C) to drive a MeArm
 */
public class MeArmDemo {
	// Servo MG90S
	private final static int SERVO_MIN = 130; // -90 degrees at 60 Hertz
	private final static int SERVO_MAX = 675; //  90 degrees at 60 Hertz

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		PCA9685 servoBoard = new PCA9685();
		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		final int LEFT_SERVO_CHANNEL = 0; // Up and down. Range 350 (all the way up) 135 (all the way down), Centered at ~230
		final int CLAW_SERVO_CHANNEL = 1; // Open and close. Range 130 (open) 400 (closed)
		final int BOTTOM_SERVO_CHANNEL = 2; // Right and Left. 130 (all the way right) 675 (all the way left). Center at ~410
		final int RIGHT_SERVO_CHANNEL = 4; // Back and forth. 130 (too far back, limit to 300) 675 (all the way ahead), right at ~430

		final int WAIT = 25;
		// Test the 4 servos.
		try {
			// Stop the servos
			servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
			delay(1_000);

			// Center the arm
			servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 410);
			servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
			delay(250);
			// Stand up
			servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 430);
			servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
			delay(250);
			// Middle
			servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 230);
			servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
			delay(250);
			// Open and close the claw
			// 130 Open, 400 closed
			System.out.println("Opening the claw");
			move(servoBoard, CLAW_SERVO_CHANNEL, 400, 130, 10, WAIT); // Open it
			delay(250);
			System.out.println("Give me something to grab.");
			utils.TextToSpeech.speak("Hey, give me something to grab, hit return when I can catch it.");
			userInput("Hit return when I can catch it.");
			System.out.println("Closing the claw");
			move(servoBoard, CLAW_SERVO_CHANNEL, 130, 400, 10, WAIT); // Close it
			delay(250);
			System.out.println("Thank you!");
			utils.TextToSpeech.speak("Thank you!");

			// Turn left and drop it.
			System.out.println("Turning left");
			move(servoBoard, BOTTOM_SERVO_CHANNEL, 410, 670, 10, WAIT); // Turn left
			delay(500);
			System.out.println("Reaching ahead");
			move(servoBoard, RIGHT_SERVO_CHANNEL, 430, 550, 10, WAIT); // Move ahead
			delay(500);
			System.out.println("Higher");
			move(servoBoard, LEFT_SERVO_CHANNEL, 230, 350, 10, WAIT); // Move up
			delay(500);
			System.out.println("Dropping");
			move(servoBoard, CLAW_SERVO_CHANNEL, 400, 130, 10, WAIT); // Drop it
			delay(500);
			System.out.println("Down");
			move(servoBoard, LEFT_SERVO_CHANNEL, 350, 230, 10, WAIT); // Move down
			delay(500);
			System.out.println("Backwards");
			move(servoBoard, RIGHT_SERVO_CHANNEL, 550, 430, 10, WAIT); // Move back
			delay(500);
			System.out.println("Re-centering");
			move(servoBoard, BOTTOM_SERVO_CHANNEL, 670, 410, 10, WAIT); // Come back
			delay(500);
			System.out.println("Closing");
			move(servoBoard, CLAW_SERVO_CHANNEL, 130, 400, 10, WAIT); // Close it
			delay(500);
		} finally {
			// Stop the servos
			servoBoard.setPWM(LEFT_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(RIGHT_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(CLAW_SERVO_CHANNEL, 0, 0);
			servoBoard.setPWM(BOTTOM_SERVO_CHANNEL, 0, 0);
		}
		System.out.println("Done.");
	}

	private static void move(PCA9685 servoBoard, int channel, int from, int to, int step, int wait) {
		servoBoard.setPWM(channel, 0, 0);
		int inc = step * (from < to ? 1 : -1);
		for (int i = from; (from < to && i <= to) || (to < from && i >= to); i += inc) {
			servoBoard.setPWM(channel, 0, i);
			delay(wait);
		}
		servoBoard.setPWM(channel, 0, 0);
	}
}
