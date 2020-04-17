package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import utils.StaticUtil;

import static utils.TimeUtil.delay;

/*
 * For PCA9685
 * Continuous, interactive demo.
 *
 * Note: This DOES NOT work as documented.
 */
public class DemoInteractiveContinuous {
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {

		int argChannel = -1;
		if (args.length > 0) {
			// First arg is channel #
			argChannel = Integer.parseInt(args[0]);
			if (argChannel > 15 || argChannel < 0) {
				throw new IllegalArgumentException("Channel in [0..15] please!");
			}
		}
		PCA9685 servoBoard = new PCA9685();
		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		final int CONTINUOUS_SERVO_CHANNEL = (argChannel != -1) ? argChannel : 14;
//  final int STANDARD_SERVO_CHANNEL   = 15;

		int servo = CONTINUOUS_SERVO_CHANNEL;
		int servoMin     = 340;
		int servoStopsAt = 375;
		int servoMax     = 410;

		System.out.println(String.format("Servo #%d, [%d..%d].", CONTINUOUS_SERVO_CHANNEL, servoMin, servoMax));

		servoBoard.setPWM(servo, 0, 0);   // Stop the servo
		delay(2_000L);
		System.out.println(String.format("Let's go. Enter values in [%d..%d], middle: %d, 'S' to stop the servo, 'Q' to quit.", servoMin, servoMax, servoStopsAt));

		boolean keepLooping = true;
		while (keepLooping) {
			String userInput = StaticUtil.userInput("You say: > ");
			if (userInput.equalsIgnoreCase("Q")) {
				keepLooping = false;
			} else if (userInput.equalsIgnoreCase("S")) {
				servoBoard.setPWM(servo, 0, 0);   // Stop the servo
			} else {
				try {
					int pwmValue = Integer.parseInt(userInput);
					if (pwmValue < servoMin) {
						System.err.println(String.format("Bad value, min is %d (%d)", servoMin, pwmValue));
					} else if (pwmValue > servoMax) {
						System.err.println(String.format("Bad value, max is %d (%d)", servoMax, pwmValue));
					} else {
						servoBoard.setPWM(servo, 0, pwmValue); // Do it!
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		servoBoard.setPWM(servo, 0, 0);   // Stop the servo
		System.out.println("Done, bye.");
	}
}
