package raspisamples.servo;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

/*
 * Standard, using I2C and the PCA9685 servo board
 */
public class StandardServoTest {
	public static void delay(long howMuch) {
		try {
			Thread.sleep(howMuch);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public StandardServoTest(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public StandardServoTest(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
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
	 * @param args The number of the servo to test [0..15]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int channel = 14;
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println("Servo Channel " + channel);
		StandardServoTest ss = new StandardServoTest(channel);
		try {
			ss.stop();

			ss.setAngle(0f);



			for (float f=0; f<=45; f++) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(500);
			}
			for (float f=45; f>=-45; f--) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(500);
			}
			for (float f=-45; f<=0; f++) {
				System.out.println("In degrees:" + f);
				ss.setAngle(f);
				delay(500);
			}
		} finally {
			ss.stop();
		}

		System.out.println("Done.");
	}
}
