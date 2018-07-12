package raspisamples.servo;

import analogdigitalconverter.ADCReader;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import static utils.StaticUtil.userInput;

/*
 * Standard, using I2C and the PCA9685 servo board
 * User interface (CLI).
 */
public class ServoAndPotentiometer {

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public ServoAndPotentiometer(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public ServoAndPotentiometer(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
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
	 * @param args
	 * @throws Exception
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
		System.out.println("Driving Servo on Channel " + channel);
		ServoAndPotentiometer ss = new ServoAndPotentiometer(channel);
		ADCReader mcp3008 = new ADCReader();

		boolean loop = true;
		System.out.println("Enter the angle at the prompt (-90..90), and q to quit.");

		try {
			ss.stop();
			while (loop) {

				String userInput = userInput("Angle (or Q) > ");
				if ("Q".equals(userInput.toUpperCase())) {
					loop = false;
				} else {
					try {
						float angle = Float.parseFloat(userInput);
						ss.setAngle(angle);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} finally {
			ss.stop();
		}
		System.out.println("Done.");
	}
}
