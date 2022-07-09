package raspiradar.jni;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import rangesensor.JNI_HC_SR04;
import utils.PinUtil;
import utils.TimeUtil;
import utils.gpio.StringToGPIOPin;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One servo (PCA9685) [-90..90] to orient the Sonic Sensor
 * One HC-SR04 (JNI version) to measure the distance
 */
public class RasPiJNIRadar {

	private boolean verbose = "true".equals(System.getProperty("radar.verbose"));
	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;
	private JNI_HC_SR04 hcSR04 = null;

	/**
	 * The class emitted when data are read.
	 * This is what the consumer is fed with (See {@link #dataConsumer})
	 */
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
		public int direction() {
			return this.direction;
		}
		public double range() {
			return this.range;
		}
	}

	// For simulation
	private static double range = 10D;
	private static Double simulateUserRange() {
		double inc = Math.random();
		int sign = System.nanoTime() % 2 == 0 ? +1 : -1;
		range += (sign * inc);
		if (range < 0 || range > 100) {
			range -= (2 * (sign * inc));
		}
		return range;
	}

	/**
	 * When data are read, they're sent to this Consumer.
	 * Can be used for user interface. REST, Serial, etc.
	 */
	private Consumer<DirectionAndRange> dataConsumer = data -> {
		System.out.println(String.format("Default ObjectDataConsumer -> Bearing %s%02d, distance %.02f m", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
	};

	private Supplier<Double> rangeSimulator = null;
	public void setRangeSimulator(Supplier<Double> rangeSimulator) {
		this.rangeSimulator = rangeSimulator;
	}

	public RasPiJNIRadar(int channel) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(false, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, null, null);
	}
	public RasPiJNIRadar(boolean moveOn, int channel) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(moveOn, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, null, null);
	}

	public RasPiJNIRadar(boolean moveOn, int channel, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(moveOn, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, trig, echo);
	}

	public RasPiJNIRadar(int channel, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(false, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, trig, echo);
	}

	public RasPiJNIRadar(boolean moveOn, int channel, int servoMin, int servoMax, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
	  try {
	  	this.servoBoard = new PCA9685();
	  } catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError ex) {
	  	if (!moveOn) {
	  		throw ex;
		  }
	  }

		try {
			this.hcSR04 = new JNI_HC_SR04();
			if (trig != null && echo != null) {
				this.hcSR04.init(PinUtil.getWiringPiNumber(trig.getName()), PinUtil.getWiringPiNumber(echo.getName()));
			} else {
				this.hcSR04.init();
			}
		} catch (UnsatisfiedLinkError usle) {
	  	if (!moveOn) {
			  throw usle;
		  } else {
			  this.setRangeSimulator(RasPiJNIRadar::simulateUserRange);
		  }
		}

		if (verbose) {
			System.out.println("HC-SR04 wiring:");
			String[] map = new String[2];
			map[0] = String.valueOf(trig != null ? PinUtil.findByPin(trig.getName()).pinNumber() : PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(4)).pinNumber()) + ":" + "Trigger";
			map[1] = String.valueOf(echo != null ? PinUtil.findByPin(echo.getName()).pinNumber() : PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(5)).pinNumber()) + ":" + "Echo";

			PinUtil.print(map);
		}

		this.servoMin = servoMin;
		this.servoMax = servoMax;
		this.diff = servoMax - servoMin;

		if (servoBoard != null) {
			int freq = 60;
			servoBoard.setPWMFreq(freq); // Set frequency in Hz
		}

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
		if (servoBoard != null) {
			servoBoard.setPWM(servo, 0, pwm);
		}
	}

	public void setPWM(int pwm) {
		if (servoBoard != null) {
			servoBoard.setPWM(servo, 0, pwm);
		}
	}

	public double readDistance() {
		if (hcSR04 != null) {
			return hcSR04.readRange();
		} else {
			if (this.rangeSimulator != null) {
				return this.rangeSimulator.get();
			} else {
				return 0;
			}
		}
	}

	public void stop() { // Set (back) to 0, free resources
		if (servoBoard != null) {
			servoBoard.setPWM(servo, 0, 0);
		}
	}

	public void free() {
		if (servoBoard != null) {
			servoBoard.close();
		}
		if (hcSR04 != null) {
//		hcSR04.stop();
		}
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
	private final static String JUST_ONE_LOOP      = "--just-one-loop";

	private static boolean loop = true;
	private static long delay = 100L;
	private static boolean justReset = false;
	private static boolean justOneLoop = false;

	public static void main(String... args) {

		Consumer<DirectionAndRange> defaultDataConsumer = (data) -> {
			System.out.println(String.format("Default (static) RasPiRadar Consumer >> Bearing %s%02d, distance %.02f m", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
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
			if (str.equals(JUST_ONE_LOOP)) {
				justOneLoop = true;
			}
		}
		if (echo != null ^ trig != null) {
			throw new RuntimeException("Echo & Trigger pin numbers must be provided together, or not at all.");
		}

		System.out.println(String.format("Driving Servo on Channel %d", servoPort));
		System.out.println(String.format("Wait when scanning %d ms", delay));

		RasPiJNIRadar rpr = null;
		try {
			if (echo == null && trig == null) {
				rpr = new RasPiJNIRadar(true, servoPort);
			} else {
				rpr = new RasPiJNIRadar(true, servoPort,
						StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(trig)),
						StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(echo)));
			}
		} catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError notOnAPi) {
			System.out.println("Not on a Pi? Moving on...");
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> loop = false, "Shutdown Hook"));

		rpr.setDataConsumer(data -> {
			// TODO Damping?
			System.out.println(String.format("Injected Data Consumer >> Bearing %s%02d, distance %.02f m", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
		});
		// For simulation, override if needed
//	rpr.setRangeSimulator(RasPiRadar::simulateUserRange);

		try {
			if (rpr != null) {
				rpr.stop(); // init
			}
			if (justReset) {
				loop = false;
				TimeUtil.delay(1_000L);
			}

			int inc = 1;
			int bearing = 0;
			double dist = 0;
			int hitExtremity = 0;
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
					if (justOneLoop && hitExtremity == 2 && bearing == 0) {
						loop = false;
					}

					bearing += inc;
					if (bearing > 90 || bearing < -90) { // then flip
						hitExtremity += 1;
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
