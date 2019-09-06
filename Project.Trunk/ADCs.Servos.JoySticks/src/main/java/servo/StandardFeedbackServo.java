package servo;

import analogdigitalconverter.mcp3008.MCP3008Reader;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import utils.StringUtils;

import static utils.TimeUtil.delay;

/*
 * Standard, using I2C and the PCA9685 servo board
 * Feedback comes from an MCP3008
 */
public class StandardFeedbackServo {

	private static int ADC_CHANNEL =
					MCP3008Reader.MCP3008_input_channels.CH1.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static boolean go = true;

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff     = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public StandardFeedbackServo(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public StandardFeedbackServo(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
		this.servoBoard = new PCA9685();

		this.servoMin = servoMin;
		this.servoMax = servoMax;
		this.diff = servoMax - servoMin;

		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		this.servo = channel;
		System.out.println("Servo Channel " + channel + " all set. Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);
		System.out.println("ADC Channel:" + ADC_CHANNEL);
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

	/*
	 * pwm in [0..1023]
	 */
	private static float pwmToDegree(int min, int max, int pwm) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return ((pwm - min) / oneDeg) - 90;
	}

	public static void main_(String... args) {
		int pwm = degreeToPWM(DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, 52);
		float deg = pwmToDegree(DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, pwm);
		System.out.println(String.format("From %f, to %d, back to %f", 52f, pwm, deg));
	}

	/**
	 * To test the servo - namely, the min & max values.
	 * Displays the feedback value.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		int channel = 7; // For the servo
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw e;
			}
		}
		System.out.println("Servo Channel " + channel);

		StandardFeedbackServo ss = new StandardFeedbackServo(channel);
		ss.setAngle(0); // Set to 0

		MCP3008Reader.initMCP3008();

		// Read the ADC in a thread
		Thread adcReader = new Thread(() -> {
			int prevAdc = 0;
			int tolerance = 10;

			while (go) {
				int adc = MCP3008Reader.readMCP3008(ADC_CHANNEL);
				int diffAdc = Math.abs(adc - prevAdc);
				if (diffAdc > tolerance) {
					System.out.println(String.format(">>  (diff:%d, prev=%04d) adc: %04d (0x%s, 0&%s) => Deg:%+03d\272",
									diffAdc,
									prevAdc,
									adc,
									StringUtils.lpad(Integer.toString(adc, 16).toUpperCase(), 3, "0"), // 1023 ; 0x3FF
									StringUtils.lpad(Integer.toString(adc, 2), 10, "0"),
									Math.round(pwmToDegree(DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, adc))));
					prevAdc = adc;
				}
			}
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait(100L);
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			System.out.println("Bye, freeing resources.");
			MCP3008Reader.shutdownMCP3008();
		});
		adcReader.start();

		Thread.sleep(10_000); // Turn it the way you want...

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
				System.out.println(String.format("In degrees:%f (pwm %d)", f, degreeToPWM(DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, f)));
				ss.setAngle(f);
				delay(1_500);
			}
		} finally {
			ss.stop();
			go = false;
		}

		System.out.println("Done.");
	}
}
