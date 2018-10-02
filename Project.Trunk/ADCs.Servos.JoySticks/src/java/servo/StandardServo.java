package servo;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import static utils.TimeUtil.delay;

/*
 * Standard, using I2C and the PCA9685 servo board
 */
public class StandardServo {

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public StandardServo(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public StandardServo(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
		this.servoBoard = new PCA9685();

		this.servoMin = servoMin;
		this.servoMax = servoMax;
		this.diff = servoMax - servoMin;

		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		this.servo = channel;
		System.out.println("Channel " + channel + " all set. Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);

	}

	public void setAngle(float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		// System.out.println(f + " degrees (" + pwm + ")");
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void setPWM(int pwm) {
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void stop() { // Set to 0
		servoBoard.setPWM(servo, 0, 0);
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}

	/**
	 * To test the servo - namely, the min & max values.
	 *
	 * @param args Servo number to test (int)
	 * @throws Exception when something goes wrong, like an invalid servo number.
	 */
	public static void main(String... args) throws Exception {
		int channel = 14;
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw e;
			}
		}
		System.out.println("Servo Channel " + channel);
		StandardServo ss = new StandardServo(channel);
		try {
			ss.stop();
			delay(2_000);
			System.out.println("Let's go, 1 by 1 (" + ss.servoMin + " to " + ss.servoMax + ")");
			for (int i = ss.servoMin; i <= ss.servoMax; i++) {
				System.out.println("i=" + i + ", " + (-90f + (((float) (i - ss.servoMin) / (float) ss.diff) * 180f)));
				ss.setPWM(i);
				delay(10);
			}
			for (int i = ss.servoMax; i >= ss.servoMin; i--) {
				System.out.println("i=" + i + ", " + (-90f + (((float) (i - ss.servoMin) / (float) ss.diff) * 180f)));
				ss.setPWM(i);
				delay(10);
			}
			ss.stop();
			delay(2_000);
			System.out.println("Let's go, 1 deg by 1 deg, forward");
			for (int i = ss.servoMin; i <= ss.servoMax; i += (ss.diff / 180)) {
				System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - ss.servoMin) / (float) ss.diff) * 180f)));
				ss.setPWM(i);
				delay(10);
			}
			System.out.println("... backward");
			for (int i = ss.servoMax; i >= ss.servoMin; i -= (ss.diff / 180)) {
				System.out.println("i=" + i + ", " + Math.round(-90f + (((float) (i - ss.servoMin) / (float) ss.diff) * 180f)));
				ss.setPWM(i);
				delay(10);
			}
			ss.stop();
			delay(2_000);

			System.out.println("More randomly:");
			float[] degValues = {-10, 0, -90, 45, -30, 90, 10, 20, 30, 40, 50, 60, 70, 80, 90, 0};
			for (float f : degValues) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(1_500);
			}
		} finally {
			ss.stop();
		}

		System.out.println("Done.");
	}
}
