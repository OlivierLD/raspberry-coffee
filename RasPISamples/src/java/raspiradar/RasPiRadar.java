package raspiradar;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

/**
 * One servo (PCA9685) [-90..90] to orient the Sonic Sensor
 * One HC-SR04 to measure the distance
 */
public class RasPiRadar {

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;

	public RasPiRadar(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public RasPiRadar(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
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

	private final static String PCA9685_SERVO_PORT = "--servo-port:";

	private static boolean loop = true;

	public static void main(String... args) throws Exception {
		int servoPort  = 0;

		// User prms
		for (String str : args) {
			if (str.startsWith(PCA9685_SERVO_PORT)) {
				String s = str.substring(PCA9685_SERVO_PORT.length());
				servoPort = Integer.parseInt(s);
			}
		}

		System.out.println("Driving Servo on Channel " + servoPort);

		RasPiRadar ss = null;
		try {
			ss = new RasPiRadar(servoPort);
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			System.out.println("Not on a Pi? Moving on...");
		}

		// ADCReader mcp3008 = new ADCReader(); // with default wiring

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			loop = false;
		}));

		try {
			if (ss != null) {
				ss.stop(); // init
			}
			int inc = 1;
			int bearing = 0;
			while (loop) {
				try {
					if (ss != null) {
						ss.setAngle(bearing);
					}
					// Measure distance here

					bearing += inc;
					if (bearing > 90 || bearing < -90) {
						inc *= -1;
						bearing += (2 * inc);
					}
					System.out.println(String.format("Bearing now %+d", bearing));
					// TODO Sleep here?
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			if (ss != null) {
				ss.stop();
			}
		}
		System.out.println("Done.");
	}

}
