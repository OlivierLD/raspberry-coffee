package raspiradar;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;
import rangesensor.HC_SR04;
import utils.TimeUtil;

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
	private HC_SR04 hcSR04 = null;

	public RasPiRadar(int channel) throws I2CFactory.UnsupportedBusNumberException {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX);
	}

	public RasPiRadar(int channel, int servoMin, int servoMax) throws I2CFactory.UnsupportedBusNumberException {
		this.servoBoard = new PCA9685();
		this.hcSR04 = new HC_SR04();

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

	public double readDistance() {
		return hcSR04.readDistance();
	}

	public void stop() { // Set (back) to 0, free resources
		servoBoard.setPWM(servo, 0, 0);
	}

	public void free() {
		servoBoard.close();
		hcSR04.stop();
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
	private final static String DELAY = "--delay:";

	private static boolean loop = true;
	private static long delay = 100L;

	public static void main(String... args) throws Exception {
		int servoPort  = 0;

		// User prms
		for (String str : args) {
			if (str.startsWith(PCA9685_SERVO_PORT)) {
				String s = str.substring(PCA9685_SERVO_PORT.length());
				servoPort = Integer.parseInt(s);
			}
			if (str.startsWith(DELAY)) {
				String s = str.substring(DELAY.length());
				delay = Long.parseLong(s);
			}
			// TODO Trig & Echo pin numbers
		}

		System.out.println(String.format("Driving Servo on Channel %d", servoPort));
		System.out.println(String.format("Wait when scanning %d ms", delay));

		RasPiRadar rr = null;
		try {
			rr = new RasPiRadar(servoPort);
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			System.out.println("Not on a Pi? Moving on...");
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			loop = false;
		}));

		try {
			if (rr != null) {
				rr.stop(); // init
			}
			int inc = 1;
			int bearing = 0;
			double dist = 0;
			while (loop) {
				try {
					if (rr != null) {
						rr.setAngle(bearing);
						// Measure distance here, broadcast it witdouble dist = h bearing.
						dist = rr.readDistance();
					}

					System.out.println(String.format("Bearing %s%02d, distance %.02f m", (bearing < 0 ? "-" : "+"), Math.abs(bearing), dist));

					bearing += inc;
					if (bearing > 90 || bearing < -90) { // then flip
						inc *= -1;
						bearing += (2 * inc);
					}
					// Sleep here
					TimeUtil.delay(delay);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			if (rr != null) {
				rr.stop();
				TimeUtil.delay(500L); // Before freeing, get some time to get back to zero.
				rr.free();
			}
		}
		System.out.println("Done.");
	}

}
