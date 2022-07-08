package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;

import static utils.TimeUtil.delay;

/*
 * For PCA9685
 * Standard, all the way, clockwise, counterclockwise
 */
public class DemoStandard {
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

//  final int CONTINUOUS_SERVO_CHANNEL = 14;
		final int STANDARD_SERVO_CHANNEL = (argChannel != -1) ? argChannel : 15;

		int servo = STANDARD_SERVO_CHANNEL;
		int servoMin = 122;
		int servoMax = 615;
		int diff = servoMax - servoMin;
		System.out.println("Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);

		try {
			servoBoard.setPWM(servo, 0, 0);   // Stop the standard one
			delay(2_000);
			System.out.println("Let's go, 1 by 1");
			for (int i = servoMin; i <= servoMax; i++) {
				System.out.println("i=" + i + ", " + (-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
				servoBoard.setPWM(servo, 0, i);
				delay(10);
			}
			for (int i = servoMax; i >= servoMin; i--) {
				System.out.println("i=" + i + ", " + (-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
				servoBoard.setPWM(servo, 0, i);
				delay(10);
			}
			servoBoard.setPWM(servo, 0, 0);   // Stop the standard one
			delay(2_000);
			System.out.println("Let's go, 1 deg by 1 deg");
			for (int i = servoMin; i <= servoMax; i += (diff / 180)) {
				System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
				servoBoard.setPWM(servo, 0, i);
				delay(10);
			}
			for (int i = servoMax; i >= servoMin; i -= (diff / 180)) {
				System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - servoMin) / (float) diff) * 180f)));
				servoBoard.setPWM(servo, 0, i);
				delay(10);
			}
			servoBoard.setPWM(servo, 0, 0);   // Stop the standard one
			delay(2_000);

			float[] degValues = {-10, 0, -90, 45, -30, 90, 10, 20, 30, 40, 50, 60, 70, 80, 90, 0};
			for (float f : degValues) {
				int pwm = degreeToPWM(servoMin, servoMax, f);
				System.out.println(f + " degrees (" + pwm + ")");
				servoBoard.setPWM(servo, 0, pwm);
				delay(1500);
			}
		} finally {
			servoBoard.setPWM(servo, 0, 0);   // Stop the standard one
		}

		System.out.println("Done.");
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}
}
