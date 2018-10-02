package servo;

import static utils.TimeUtil.delay;

/*
 * Standard, using I2C and the PCA9685 servo board
 */
public class StandardServoTest {
	/**
	 * To test the servo - namely, the min & max values.
	 *
	 * @param args The number of the servo to test [0..15]
	 * @throws Exception when anything goes wrong
	 */
	public static void main(String... args) throws Exception {
		int channel = 14;
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println("Servo Channel " + channel);
		StandardServo ss = new StandardServo(channel);
		try {
			ss.stop();

			ss.setAngle(0f);

			System.out.println("0 to 45....");
			for (float f=0; f<=45; f++) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(10);
			}
			System.out.println("45 to -45....");
			delay(1_000);
			for (float f=45; f>=-45; f--) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(10);
			}
			System.out.println("-45 to 0....");
			delay(1_000);
			for (float f=-45; f<=0; f++) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(10);
			}
		} finally {
			ss.stop();
		}
		System.out.println("Done!");
	}
}
