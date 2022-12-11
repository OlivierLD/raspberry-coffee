package i2c.servo;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import static utils.TimeUtil.delay;

/**
 * Servo Driver, <a href="https://www.adafruit.com/product/815">https://www.adafruit.com/product/815</a>
 * <br/>
 * In theory, PWM servos support those values:
 *<pre>
 * Servo Pulse | Standard |   Continuous
 * ------------+----------+-------------------
 *       1.5ms |   0 deg  |     Stop
 *       2.0ms |  90 deg  | FullSpeed forward
 *       1.0ms | -90 deg  | FullSpeed backward
 * ------------+----------+-------------------
 *</pre>
 * <b><i>BUT</i></b> this may vary a lot.<br/>
 * Servos like <a href="https://www.adafruit.com/product/169">https://www.adafruit.com/product/169</a> or <a href="https://www.adafruit.com/product/155">https://www.adafruit.com/product/155</a>
 * have min and max values like 0.5ms 2.5ms, which is quite different. Servos are analog devices...
 *
 * The best is probably to calibrate the servos before using them.<br/>
 * For that, you can use some utility functions provided below:
 * <ul>
 *  <li> {@link PCA9685#getPulseFromValue(int, int)}</li>
 *  <li> {@link PCA9685#getServoMinValue(int)}</li>
 *  <li> {@link PCA9685#getServoCenterValue(int)}</li>
 *  <li> {@link PCA9685#getServoMaxValue(int)}</li>
 *  <li> {@link PCA9685#getServoValueFromPulse(int, float)}</li>
 * </ul>
 */
public class PCA9685 {
	public final static int PCA9685_ADDRESS = 0x40;

	public final static int MODE1 = 0x00;
	public final static int MODE2 = 0x01;

	public final static int SUBADR1 = 0x02;
	public final static int SUBADR2 = 0x03;
	public final static int SUBADR3 = 0x04;

	public final static int PRESCALE   = 0xFE;
	public final static int LED0_ON_L  = 0x06;
	public final static int LED0_ON_H  = 0x07;
	public final static int LED0_OFF_L = 0x08;
	public final static int LED0_OFF_H = 0x09;

	public final static int ALL_LED_ON_L  = 0xFA;
	public final static int ALL_LED_ON_H  = 0xFB;
	public final static int ALL_LED_OFF_L = 0xFC;
	public final static int ALL_LED_OFF_H = 0xFD;

	private final static boolean verbose = "true".equals(System.getProperty("pca9685.verbose"));
	private int freq = 60;

	private I2CBus bus;
	private I2CDevice servoDriver;

	public PCA9685() throws I2CFactory.UnsupportedBusNumberException {
		this(PCA9685_ADDRESS); // 0x40 obtained through sudo i2cdetect -y 1
	}

	public PCA9685(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get I2C bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get the device itself
			servoDriver = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
			// Reseting
			servoDriver.write(MODE1, (byte) 0x00);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * @param freq 40..1000
	 */
	public void setPWMFreq(int freq) {
		this.freq = freq;
		float preScaleVal = 25_000_000.0f; // 25MHz
		preScaleVal /= 4_096.0;            // 4096: 12-bit
		preScaleVal /= freq;
		preScaleVal -= 1.0;
		if (verbose) {
			System.out.println("Setting PWM frequency to " + freq + " Hz");
			System.out.println("Estimated pre-scale: " + preScaleVal);
		}
		double preScale = Math.floor(preScaleVal + 0.5);
		if (verbose) {
			System.out.println("Final pre-scale: " + preScale);
		}
		try {
			byte oldMode = (byte) servoDriver.read(MODE1);
			byte newMode = (byte) ((oldMode & 0x7F) | 0x10); // sleep
			servoDriver.write(MODE1, newMode);               // go to sleep
			servoDriver.write(PRESCALE, (byte) (Math.floor(preScale)));
			servoDriver.write(MODE1, oldMode);
			delay(5);
			servoDriver.write(MODE1, (byte) (oldMode | 0x80));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * @param channel 0..15
	 * @param on      0..4095 (2^12 positions)
	 * @param off     0..4095 (2^12 positions)
	 */
	public void setPWM(int channel, int on, int off) throws IllegalArgumentException {
		if (channel < 0 || channel > 15) {
			throw new IllegalArgumentException(String.format("Channel must be in [0..15], was %d", channel));
		}
		if (on < 0 || on > 4_095) {
			throw new IllegalArgumentException(String.format("On must be in [0..4095], was %d", on));
		}
		if (off < 0 || off > 4_095) {
			throw new IllegalArgumentException(String.format("Off must be in [0..4095], was %d", off));
		}
		if (on > off) {
			throw new IllegalArgumentException("OFF must be greater than ON");
		}
		try {
			servoDriver.write(LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
			servoDriver.write(LED0_ON_H + 4 * channel, (byte) (on >> 8));
			servoDriver.write(LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
			servoDriver.write(LED0_OFF_H + 4 * channel, (byte) (off >> 8));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * @param channel 0..15
	 * @param pulseMS in ms.
	 */
	public void setServoPulse(int channel, float pulseMS) {
		int pulse = getServoValueFromPulse(this.freq, pulseMS);
		this.setPWM(channel, 0, pulse);
	}

	/**
	 * Free resources
	 */
	public void close() {
		if (this.bus != null) {
			try {
				this.bus.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main__(String... args) throws I2CFactory.UnsupportedBusNumberException {
		int freq = 60;
		if (args.length > 0) {
			freq = Integer.parseInt(args[0]);
		}
		PCA9685 servoBoard = new PCA9685();
		servoBoard.setPWMFreq(freq); // Set frequency to 60 Hz by default
		int servoMin = 122; // min value for servos like https://www.adafruit.com/product/169 or https://www.adafruit.com/product/155
		int servoMax = 615; // max value for servos like https://www.adafruit.com/product/169 or https://www.adafruit.com/product/155

		final int CONTINUOUS_SERVO_CHANNEL = 0;
		final int STANDARD_SERVO_CHANNEL   = 1;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0); // Stop the standard one
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		}, "Shutdown Hook"));

		for (int i = 0; i < 5; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMin);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMin);
			delay(1_000);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMax);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMax);
			delay(1_000);
		}
		servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0);   // Stop the standard one

		for (int i = servoMin; i <= servoMax; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, i);
			delay(10);
		}
		for (int i = servoMax; i >= servoMin; i--) {
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, i);
			delay(10);
		}

		servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0);   // Stop the standard one

		for (int i = servoMin; i <= servoMax; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, i);
			delay(10);
		}
		for (int i = servoMax; i >= servoMin; i--) {
			System.out.println("i=" + i);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, i);
			delay(10);
		}

		servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0);   // Stop the standard one

		if (true) {
			System.out.println("Now, servoPulse");
			servoBoard.setPWMFreq(250);
			// The same with setServoPulse
			for (int i = 0; i < 5; i++) {
				servoBoard.setServoPulse(STANDARD_SERVO_CHANNEL, 1f);
				servoBoard.setServoPulse(CONTINUOUS_SERVO_CHANNEL, 1f);
				delay(1_000);
				servoBoard.setServoPulse(STANDARD_SERVO_CHANNEL, 2f);
				servoBoard.setServoPulse(CONTINUOUS_SERVO_CHANNEL, 2f);
				delay(1_000);
			}
			// Stop, Middle
			servoBoard.setServoPulse(STANDARD_SERVO_CHANNEL, 1.5f);
			servoBoard.setServoPulse(CONTINUOUS_SERVO_CHANNEL, 1.5f);

			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		}

		System.out.println("Done with the demo.");
	}

	// "Theoretical" values
	private final static float CENTER_PULSE = 1.5f;
	private final static float MIN_PULSE = 1f;
	private final static float MAX_PULSE = 2f;

	/**
	 * Just for display
	 * @param freq Frequency, Hz
	 * @param targetPulse Target pulse, ms
	 */
	public static void displayServoValue(int freq, float targetPulse) {
		System.out.printf("At %d Hz, for a target pulse of %.02f \u00b5s, servo value is %d\n", freq, targetPulse, getServoValueFromPulse(freq, targetPulse));
	}

	/**
	 *
	 * @param freq in Hz
	 * @param targetPulse in ms
	 * @return the value as an int
	 */
	public static int getServoValueFromPulse(int freq, float targetPulse) {
		double pulseLength = 1_000_000; // 1s = 1,000,000 us per pulse. "us" is to be read "micro (mu) sec".
		pulseLength /= freq;  // 40..1000 Hz
		pulseLength /= 4_096; // 12 bits of resolution. 4096 = 2^12
		int pulse = (int) Math.round((targetPulse * 1_000) / pulseLength); // in millisec
		if (verbose) {
			System.out.printf("%.04f \u00b5s per bit, pulse: %d\n", pulseLength, pulse); // bit? cycle?
		}
		return pulse;
	}

	public static double getPulseFromValue(int freq, int value) {
		double msPerPeriod = 1_000.0 / (double)freq;
		return msPerPeriod * ((double)value / 4_096.0);
	}

	public static int getServoMinValue(int freq) {
		return getServoValueFromPulse(freq, MIN_PULSE);
	}
	public static int getServoCenterValue(int freq) {
		return getServoValueFromPulse(freq, CENTER_PULSE);
	}
	public static int getServoMaxValue(int freq) {
		return getServoValueFromPulse(freq, MAX_PULSE);
	}

	public static void main(String... args) {
    int[] frequencies = new int[] { 60, 50, 250, 1_000 };

    for (int freq : frequencies) {
	    System.out.printf("For freq %d, min is %d, center is %d, max is %d\n", freq, getServoMinValue(freq), getServoCenterValue(freq), getServoMaxValue(freq));
    }

    int min = 122, max = 615; // min and max values for servos like https://www.adafruit.com/product/169 or https://www.adafruit.com/product/155 at 60 Hz
    int freq = 60;
		System.out.printf("At %d Hz, %d pulses %.04f ms, %d pulses %.04f ms\n", freq, min, getPulseFromValue(freq, min), max, getPulseFromValue(freq, max));

		int value_05 = getServoValueFromPulse(freq, 0.5f);
		int value_25 = getServoValueFromPulse(freq, 2.5f);
		System.out.printf("At %d Hz, value for 0.5ms is %d, value for 2.5ms is %d\n", freq, value_05, value_25);
	}

	public static void main___(String... args) throws I2CFactory.UnsupportedBusNumberException {
		int freq = 60;
		if (args.length > 0) {
			freq = Integer.parseInt(args[0]);
		}
		PCA9685 servoBoard = new PCA9685();
		servoBoard.setPWMFreq(freq); // Set frequency to 60 Hz
		int servoMin = getServoMinValue(freq);   // 130;   // was 150. Min pulse length out of 4096
		int servoMax = getServoMaxValue(freq);   // was 600. Max pulse length out of 4096

		final int CONTINUOUS_SERVO_CHANNEL = 0;
		final int STANDARD_SERVO_CHANNEL = 1;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0); // Stop the standard one
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		}, "Shutdown Hook"));

		System.out.printf("min: %d, max: %d\n", servoMin, servoMax);
		for (int i = 0; i < 5; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMin);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMin);
			delay(1_000);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMax);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMax);
			delay(1_000);
		}
		servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0);   // Stop the standard one
		delay(1_000);

		System.out.println("With hard coded values (Suitable for 60 Hz)");
		servoMin = 122;
		servoMax = 615;
		System.out.printf("min: %d, max: %d\n", servoMin, servoMax);
		for (int i = 0; i < 5; i++) {
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMin);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMin);
			delay(1_000);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMax);
			servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, servoMax);
			delay(1_000);
		}

		servoBoard.setPWM(CONTINUOUS_SERVO_CHANNEL, 0, 0); // Stop the continuous one
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 0);   // Stop the standard one
		System.out.println("Ouala.");
	}
}
