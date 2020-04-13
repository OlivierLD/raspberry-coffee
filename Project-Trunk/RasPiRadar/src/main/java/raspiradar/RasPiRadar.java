package raspiradar;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import rangesensor.HC_SR04;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One servo (PCA9685) [-90..90] to orient the Sonic Sensor
 * One HC-SR04 to measure the distance
 *
 * This class has no main.
 *
 * Works as expected in standalone, not that well from Processing on the Pi (too demanding?)
 */
public class RasPiRadar {

	private static boolean verbose = "true".equals(System.getProperty("radar.verbose"));
	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;

	private PCA9685 servoBoard = null;
	private HC_SR04 hcSR04 = null;

	private static final int BUFFER_LENGTH = 10;
	private static List<Double> buffer = new ArrayList<>(BUFFER_LENGTH);

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
	private static double range = 100D;
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
		System.out.println(String.format("Default ObjectDataConsumer -> Bearing %s%02d, distance %.02f cm", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), data.range));
	};

	private Supplier<Double> rangeSimulator = null;
	public void setRangeSimulator(Supplier<Double> rangeSimulator) {
		this.rangeSimulator = rangeSimulator;
	}

	public RasPiRadar(int channel) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(false, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, null, null);
	}
	public RasPiRadar(boolean moveOn, int channel) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(moveOn, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, null, null);
	}

	public RasPiRadar(boolean moveOn, int channel, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(moveOn, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, trig, echo);
	}

	public RasPiRadar(int channel, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
		this(false, channel, DEFAULT_SERVO_MIN, DEFAULT_SERVO_MAX, trig, echo);
	}

	public RasPiRadar(boolean moveOn, int channel, int servoMin, int servoMax, Pin trig, Pin echo) throws I2CFactory.UnsupportedBusNumberException, UnsatisfiedLinkError {
	  try {
	  	this.servoBoard = new PCA9685();
	  } catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError ex) {
	  	if (!moveOn) {
	  		throw ex;
		  }
	  }

		try {
			if (trig != null && echo != null) {
				this.hcSR04 = new HC_SR04(trig, echo);
			} else {
				this.hcSR04 = new HC_SR04();
			}
		} catch (UnsatisfiedLinkError usle) {
	  	if (!moveOn) {
			  throw usle;
		  } else {
			  this.setRangeSimulator(RasPiRadar::simulateUserRange);
		  }
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

	public HC_SR04 getHcSR04() {
	  return this.hcSR04;
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
			return hcSR04.readDistance();
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
			hcSR04.stop();
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
}
