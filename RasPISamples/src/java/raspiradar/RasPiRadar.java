package raspiradar;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;
import rangesensor.HC_SR04;
import utils.PinUtil;
import utils.TimeUtil;

import java.util.function.Consumer;

/**
 * One servo (PCA9685) [-90..90] to orient the Sonic Sensor
 * One HC-SR04 to measure the distance
 */
public class RasPiRadar {

	private boolean verbose = "true".equals(System.getProperty("radar.verbose"));
	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;
	private HC_SR04 hcSR04 = null;

	public static class DirectionAndRange {
		double range;
		int direction;

		public DirectionAndRange() { }
		public DirectionAndRange(int direction, double range) {
			this.direction = direction;
			this.range = range;
		}
		public DirectionAndRange direction(int direction) {
			this.direction = direction;
			return this;
		}
		public DirectionAndRange range(double range) {
			this.range = range;
			return this;
		}
	}

	private Consumer<DirectionAndRange> dataConsumer = (data) -> {
		System.out.println(String.format("Bearing %s%02d, distance %.02f cm", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
	};

	public RasPiRadar(int channel) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, null, null);
	}

	public RasPiRadar(int channel, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, trig, echo);
	}

	public RasPiRadar(int channel, int servoMin, int servoMax, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
	  this.servoBoard = new PCA9685();

		try {
			if (trig != null && echo != null) {
				this.hcSR04 = new HC_SR04(trig, echo);
			} else {
				this.hcSR04 = new HC_SR04();
			}
		} catch (UnsatisfiedLinkError usle) {
			throw usle;
		}

		if (verbose) {
			System.out.println("HC_SR04 wiring:");
			String[] map = new String[2];
			map[0] = String.valueOf(PinUtil.findByPin(this.hcSR04.getTrigPin()).pinNumber()) + ":" + "Trigger";
			map[1] = String.valueOf(PinUtil.findByPin(this.hcSR04.getEchoPin()).pinNumber()) + ":" + "Echo";

			PinUtil.print(map);
		}

		this.servoMin = servoMin;
		this.servoMax = servoMax;
		this.diff = servoMax - servoMin;

		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		this.servo = channel;
		System.out.println("Channel " + channel + " all set. Min:" + servoMin + ", Max:" + servoMax + ", diff:" + diff);
	}

	public void setDataConsumer(Consumer<DirectionAndRange> dataConsumer) {
		this.dataConsumer = dataConsumer;
	}

	public void consumeData(DirectionAndRange dar) {
		this.dataConsumer.accept(dar);
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
	private final static String DELAY              = "--delay:";
	private final static String TRIGGER_PIN        = "--trigger-pin:";
	private final static String ECHO_PIN           = "--echo-pin:";
	private final static String JUST_RESET         = "--just-reset";

	private static boolean loop = true;
	private static long delay = 100L;
	private static boolean justReset = false;

	public static void main(String... args) {

		Consumer<DirectionAndRange> defaultDataConsumer = (data) -> {
			System.out.println(String.format("Default>> Bearing %s%02d, distance %.02f cm", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
		};

		int servoPort  = 0;

		Integer trig = null, echo = null;

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
			// Trig & Echo pin PHYSICAL numbers (1..40)
			if (str.startsWith(TRIGGER_PIN)) {
				String s = str.substring(TRIGGER_PIN.length());
				trig = Integer.parseInt(s);
			}
			if (str.startsWith(ECHO_PIN)) {
				String s = str.substring(ECHO_PIN.length());
				echo = Integer.parseInt(s);
			}
			if (str.equals(JUST_RESET)) {
				justReset = true;
			}
		}
		if (echo != null ^ trig != null) {
			throw new RuntimeException("Echo & Trigger pin numbers must be provided together, or not at all.");
		}

		System.out.println(String.format("Driving Servo on Channel %d", servoPort));
		System.out.println(String.format("Wait when scanning %d ms", delay));

		RasPiRadar rpr = null;
		try {
			if (echo == null && trig == null) {
				rpr = new RasPiRadar(servoPort);
			} else {
				rpr = new RasPiRadar(servoPort, PinUtil.getPinByPhysicalNumber(trig), PinUtil.getPinByPhysicalNumber(echo));
			}
		} catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError notOnAPi) {
			System.out.println("Not on a Pi? Moving on...");
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			loop = false;
		}));

		try {
			if (rpr != null) {
				rpr.stop(); // init
			}
			if (justReset) {
				loop = false;
			}

			int inc = 1;
			int bearing = 0;
			double dist = 0;
			while (loop) {
				try {
					if (rpr != null) {
						rpr.setAngle(bearing);
						// Measure distance here, broadcast it witdouble dist = h bearing.
						dist = rpr.readDistance();
						// Consumer
						rpr.consumeData(new DirectionAndRange(bearing, dist));
					} else { // For dev...
						defaultDataConsumer.accept(new DirectionAndRange(bearing, dist));
					}

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
			if (rpr != null) {
				rpr.stop();
				TimeUtil.delay(1_000L); // Before freeing, get some time to get back to zero.
				rpr.free();
			}
		}
		System.out.println("Done.");
	}
}
