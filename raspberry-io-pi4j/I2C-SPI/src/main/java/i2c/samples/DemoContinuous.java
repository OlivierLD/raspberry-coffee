package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;

import static utils.TimeUtil.delay;

/*
 * For PCA9685
 * Continuous, all the way, clockwise, counterclockwise
 * Note: This DOES NOT work as documented.
 */
public class DemoContinuous {
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
		int servoMin = 340;
		int servoMax = 410;
		int servoStopsAt = 375;

		servoBoard.setPWM(servo, 0, 0);   // Stop the servo
		delay(2_000L);
		System.out.println("Let's go");

		for (int i = servoStopsAt; i <= servoMax; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(servo, 0, i);
			delay(500);
		}
		System.out.println("Servo Max");
		delay(1_000);
		for (int i = servoMax; i >= servoMin; i--) {
			System.out.println("i=" + i);
			servoBoard.setPWM(servo, 0, i);
			delay(500);
		}
		System.out.println("Servo Min");
		delay(1_000);
		for (int i = servoMin; i <= servoStopsAt; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(servo, 0, i);
			delay(500);
		}
		delay(2_000);
		servoBoard.setPWM(servo, 0, 0);   // Stop the servo
		System.out.println("Done.");
	}
}
