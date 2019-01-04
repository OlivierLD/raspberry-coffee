package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.listener.LSM303Listener;
import utils.StringUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * LSM303: Accelerometer + Magnetometer
 * <br>
 * Code adapted from the Adafruit Arduino libraries and https://github.com/adafruit/Adafruit_Python_LSM303
 * <br>
 * <h4>A bit of note about the device:</h4>
 * "The accelerometer allows you to measure acceleration or direction towards the center or the earth, and the magnetometer measure magnetic force, which is useful to detect magnetic north."
 * <br>
 * In other words, the accelerometer measures the gravity, it is <i>not</i> to be considered as a gyroscope...
 * <br>
 * Also, another funny one:
 * <ul>
 * <li>The data read from the accelerometer are read in the order X, Y, Z</li>
 * <li>The data read from the magnetometer are read in the order X, <b style='color: red;'>Z</b>, Y, funny isn't it?</li>
 * </ul>
 * And they both have different endianness.
 * <br>
 * It took me a while to figure this all out...
 * <p>
 * Magnetometer/accelerometer calibration see
 * https://forum.sparkfun.com/viewtopic.php?t=32575
 * http://www.varesano.net/blog/fabio/freeimu-magnetometer-and-accelerometer-calibration-gui-alpha-version-out
 * https://learn.adafruit.com/lsm303-accelerometer-slash-compass-breakout/calibration
 * <p>
 * Very good doc, explaining the calibration problem:
 * https://github.com/praneshkmr/node-lsm303/wiki/Understanding-the-calibration-of-the-LSM303-magnetometer-(compass)
 */
public class LSM303 {
	/*
	Prompt> sudo i2cdetect -y 1
       0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
  00:          -- -- -- -- -- -- -- -- -- -- -- -- --
  10: -- -- -- -- -- -- -- -- -- 19 -- -- -- -- 1e --
  20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  70: -- -- -- -- -- -- -- --
   */
	// Those 2 next addresses are returned by "sudo i2cdetect -y 1", see above.
	public final static int LSM303_ADDRESS_ACCEL = (0x32 >> 1); // 0011001x, 0x19
	public final static int LSM303_ADDRESS_MAG = (0x3C >> 1); // 0011110x, 0x1E <- that is an HMC5883L !
	// Default    Type
	public final static int LSM303_REGISTER_ACCEL_CTRL_REG1_A = 0x20; // 00000111   rw
	public final static int LSM303_REGISTER_ACCEL_CTRL_REG4_A = 0x23; // 00000000   rw
	public final static int LSM303_REGISTER_ACCEL_OUT_X_L_A = 0x28;
	public final static int LSM303_REGISTER_MAG_CRB_REG_M = 0x01;
	public final static int LSM303_REGISTER_MAG_MR_REG_M = 0x02;
	public final static int LSM303_REGISTER_MAG_OUT_X_H_M = 0x03;

	// Gain settings for setMagGain()
	public final static int LSM303_MAGGAIN_1_3 = 0x20; // +/- 1.3
	public final static int LSM303_MAGGAIN_1_9 = 0x40; // +/- 1.9
	public final static int LSM303_MAGGAIN_2_5 = 0x60; // +/- 2.5
	public final static int LSM303_MAGGAIN_4_0 = 0x80; // +/- 4.0
	public final static int LSM303_MAGGAIN_4_7 = 0xA0; // +/- 4.7
	public final static int LSM303_MAGGAIN_5_6 = 0xC0; // +/- 5.6
	public final static int LSM303_MAGGAIN_8_1 = 0xE0; // +/- 8.1

	private final static float _lsm303Accel_MG_LSB = 0.001F; // 1, 2, 4 or 12 mg per lsb
	private static float _lsm303Mag_Gauss_LSB_XY = 1_100.0F; // Varies with gain
	private static float _lsm303Mag_Gauss_LSB_Z = 980.0F;    // Varies with gain

	private float SENSORS_GRAVITY_EARTH = 9.80665f;        // Earth's gravity in m/s^2
	private float SENSORS_GRAVITY_MOON = 1.6f;             // The moon's gravity in m/s^2
	private float SENSORS_GRAVITY_SUN = 275.0f;            // The sun's gravity in m/s^2
	private float SENSORS_GRAVITY_STANDARD = SENSORS_GRAVITY_EARTH;
	private float SENSORS_MAGFIELD_EARTH_MAX = 60.0f;      // Maximum magnetic field on Earth's surface
	private float SENSORS_MAGFIELD_EARTH_MIN = 30.0f;      // Minimum magnetic field on Earth's surface
	private float SENSORS_PRESSURE_SEALEVELHPA = 1_013.25f;// Average sea level pressure is 1013.25 hPa
	private float SENSORS_DPS_TO_RADS = 0.017453293f;      // Degrees/s to rad/s multiplier
	private float SENSORS_GAUSS_TO_MICROTESLA = 100;       // Gauss to micro-Tesla multiplier

	private I2CBus bus;
	private I2CDevice accelerometer = null, magnetometer = null;
	private byte[] accelData, magData;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
	private static boolean verboseRaw = "true".equals(System.getProperty("lsm303.verbose.raw", "false"));

	private static boolean verboseAcc = "true".equals(System.getProperty("lsm303.verbose.acc", "false"));
	private static boolean verboseMag = "true".equals(System.getProperty("lsm303.verbose.mag", "false"));

	private static boolean useLowPassFilter = "true".equals(System.getProperty("lsm303.low.pass.filter", "true")); // default true
	private static boolean logForCalibration = "true".equals(System.getProperty("lsm303.log.for.calibration", "false"));

	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L; // Default
	private LSM303Listener dataListener = null;

	// Keys for the calibration map
	public final static String MAG_X_OFFSET = "MagXOffset";
	public final static String MAG_Y_OFFSET = "MagYOffset";
	public final static String MAG_Z_OFFSET = "MagZOffset";

	public final static String MAG_X_COEFF = "MagXCoeff";
	public final static String MAG_Y_COEFF = "MagYCoeff";
	public final static String MAG_Z_COEFF = "MagZCoeff";

	public final static String ACC_X_OFFSET = "AccXOffset";
	public final static String ACC_Y_OFFSET = "AccYOffset";
	public final static String ACC_Z_OFFSET = "AccZOffset";

	public final static String ACC_X_COEFF = "AccXCoeff";
	public final static String ACC_Y_COEFF = "AccYCoeff";
	public final static String ACC_Z_COEFF = "AccZCoeff";

	private final static Map<String, Double> DEFAULT_MAP = new HashMap<>();

	static {
		DEFAULT_MAP.put(MAG_X_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_Y_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_Z_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_X_COEFF, 1d);
		DEFAULT_MAP.put(MAG_Y_COEFF, 1d);
		DEFAULT_MAP.put(MAG_Z_COEFF, 1d);
		DEFAULT_MAP.put(ACC_X_OFFSET, 0d);
		DEFAULT_MAP.put(ACC_Y_OFFSET, 0d);
		DEFAULT_MAP.put(ACC_Z_OFFSET, 0d);
		DEFAULT_MAP.put(ACC_X_COEFF, 1d);
		DEFAULT_MAP.put(ACC_Y_COEFF, 1d);
		DEFAULT_MAP.put(ACC_Z_COEFF, 1d);
	}

	private Map<String, Double> calibrationMap = new HashMap<>(DEFAULT_MAP);

	public enum EnabledFeature {
		MAGNETOMETER,
		ACCELEROMETER,
		BOTH
	}

	public void setCalibrationValue(String key, double val) {
		// WARNING!! The values depend heavily on USE_NORM value.
		calibrationMap.put(key, val);
	}

	public Map<String, Double> getCalibrationMap() {
		return calibrationMap;
	}

	private void setMagGain(int gain) throws IOException {
		magnetometer.write(LSM303_REGISTER_MAG_CRB_REG_M, (byte) gain);

		switch (gain) {
			case LSM303_MAGGAIN_1_3:
				_lsm303Mag_Gauss_LSB_XY = 1_100F;
				_lsm303Mag_Gauss_LSB_Z = 980F;
				break;
			case LSM303_MAGGAIN_1_9:
				_lsm303Mag_Gauss_LSB_XY = 855F;
				_lsm303Mag_Gauss_LSB_Z = 760F;
				break;
			case LSM303_MAGGAIN_2_5:
				_lsm303Mag_Gauss_LSB_XY = 670F;
				_lsm303Mag_Gauss_LSB_Z = 600F;
				break;
			case LSM303_MAGGAIN_4_0:
				_lsm303Mag_Gauss_LSB_XY = 450F;
				_lsm303Mag_Gauss_LSB_Z = 400F;
				break;
			case LSM303_MAGGAIN_4_7:
				_lsm303Mag_Gauss_LSB_XY = 400F;
				_lsm303Mag_Gauss_LSB_Z = 355F;
				break;
			case LSM303_MAGGAIN_5_6:
				_lsm303Mag_Gauss_LSB_XY = 330F;
				_lsm303Mag_Gauss_LSB_Z = 295F;
				break;
			case LSM303_MAGGAIN_8_1:
				_lsm303Mag_Gauss_LSB_XY = 230F;
				_lsm303Mag_Gauss_LSB_Z = 205F;
				break;
		}
	}

	public LSM303() throws I2CFactory.UnsupportedBusNumberException, IOException {
		this(EnabledFeature.BOTH, true);
	}

	public LSM303(boolean autoStart) throws I2CFactory.UnsupportedBusNumberException, IOException {
		this(EnabledFeature.BOTH, autoStart);
	}

	public LSM303(EnabledFeature feature, boolean autoStart) throws I2CFactory.UnsupportedBusNumberException, IOException {
		if (verbose) {
			System.out.println("Starting sensors reading.");
		}
		// Get i2c bus
		bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
		if (verbose) {
			System.out.println("Connected to bus. OK.");
		}
		// Get device itself
		if (feature.equals(EnabledFeature.ACCELEROMETER) || feature.equals(EnabledFeature.BOTH)) {
			try {
				accelerometer = bus.getDevice(LSM303_ADDRESS_ACCEL);
			} catch (IOException ioe) {
				throw new IOException("Error getting the Accelerometer device", ioe);
			}
		}
		if (feature.equals(EnabledFeature.MAGNETOMETER) || feature.equals(EnabledFeature.BOTH)) {
			try {
				magnetometer = bus.getDevice(LSM303_ADDRESS_MAG);
			} catch (IOException ioe) {
				throw new IOException("Error getting the Magnetometer device", ioe);
			}
		}
		if (verbose) {
			System.out.println("Connected to devices. OK.");
		}
		/*
		 * Start sensing
		 */
		// Enable accelerometer
		if (accelerometer != null) {
			accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG1_A, (byte) 0x27); // 00100111
			accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG4_A, (byte) 0x00); // Low Res. For Hi Res, write 0b00001000, 0x08
			if (verbose) {
				System.out.println("Accelerometer OK.");
			}
		}

		// Enable magnetometer
		if (magnetometer != null) {
			magnetometer.write(LSM303_REGISTER_MAG_MR_REG_M, (byte) 0x00);

			int gain = LSM303_MAGGAIN_1_3;
			setMagGain(gain);
			if (verbose) {
				System.out.println("Magnetometer OK.");
			}
		}
		if (autoStart) {
			startReading();
		} else if (verbose) {
			System.out.println("Not starting from the constructor");
		}
	}

	public void setDataListener(LSM303Listener dataListener) {
		this.dataListener = dataListener;
	}

	// Create a separate thread to read the sensors
	public void startReading() {
		Runnable task = () -> {
			try {
				readingSensors();
			} catch (IOException ioe) {
				System.err.println("Reading thread:");
				ioe.printStackTrace();
			}
		};
		new Thread(task).start();
	}

	private void setPitch(double pitch) {
		this.pitch = pitch;
	}

	private void setRoll(double roll) {
		this.roll = roll;
	}

	private void setHeading(double heading) {
		this.heading = heading;
	}

	public double getPitch() {
		return this.pitch;
	}

	public double getRoll() {
		return this.roll;
	}

	public double getHeading() {
		return this.heading;
	}

	public void setWait(long wait) {
		this.wait = wait;
	}

	private boolean keepReading = true;

	public void setKeepReading(boolean keepReading) {
		this.keepReading = keepReading;
	}

	private final float ALPHA = 0.15f; // For the low pass filter (smoothing)

	private final static boolean USE_NORM = true; // TODO See what the default should be...

	private void readingSensors()
			throws IOException {
		if (logForCalibration) {
			System.out.println("rawAccX;rawAccY;rawAccZ;rawMagX;rawMagY;rawMagZ;accX;accY;accZ;magX;magY;magZ;accNorm;magNorm");
		}

		while (keepReading) {
			accelData = new byte[6];
			magData = new byte[6];

			int accelX = 0, accelY = 0, accelZ = 0;
			double accX = 0d, accY = 0d, accZ = 0d;
			double accXfiltered = 0d, accYfiltered = 0d, accZfiltered = 0d;
			int magneticX = 0, magneticY = 0, magneticZ = 0;
			double magX = 0d, magY = 0d, magZ = 0d;
			double magXfiltered = 0d, magYfiltered = 0d, magZfiltered = 0d;

			double pitchDegrees = -Double.MAX_VALUE, rollDegrees = -Double.MAX_VALUE;
			double heading = 0f;
			double magNorm = 0d, accNorm = 0d;

			if (accelerometer != null) {
				accelerometer.write((byte) (LSM303_REGISTER_ACCEL_OUT_X_L_A | 0x80));

				int r = accelerometer.read(accelData, 0, 6);
				if (r != 6) {
					System.out.println("Error reading accel data, < 6 bytes");
				}
				// raw Acc data
				accelX = accel12(accelData, 0);
				accelY = accel12(accelData, 2);
				accelZ = accel12(accelData, 4);

				if (verboseAcc) {
					System.out.println(String.format("Raw(int)Acc XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X)", accelX, accelY, accelZ, accelX & 0xFFFF, accelY & 0xFFFF, accelZ & 0xFFFF));
				}

				accX = accelX * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;
				accY = accelY * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;
				accZ = accelZ * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;

				accX = calibrationMap.get(ACC_X_OFFSET) + (accX * calibrationMap.get(ACC_X_COEFF));
				accY = calibrationMap.get(ACC_Y_OFFSET) + (accY * calibrationMap.get(ACC_Y_COEFF));
				accZ = calibrationMap.get(ACC_Z_OFFSET) + (accZ * calibrationMap.get(ACC_Z_COEFF));

				accNorm = Math.sqrt((accX * accX) + (accY * accY) + (accZ * accZ));

				if (verboseAcc) {
					System.out.println(String.format("Acc norm: %f", accNorm));
				}
				if (USE_NORM && accNorm != 0) {
					accX /= accNorm;
					accY /= accNorm;
					accZ /= accNorm;
				}

				if (useLowPassFilter) {
					accXfiltered = lowPass(ALPHA, accX, accXfiltered);
					accYfiltered = lowPass(ALPHA, accY, accYfiltered);
					accZfiltered = lowPass(ALPHA, accZ, accZfiltered);
				} else {
					accXfiltered = accX;
					accYfiltered = accY;
					accZfiltered = accZ;
				}
				/*
					pitch = atan (x / sqrt(y^2 + z^2));
					roll  = atan (y / sqrt(x^2 + z^2));
				 */
				pitchDegrees = Math.toDegrees(Math.atan(accXfiltered / Math.sqrt((accYfiltered * accYfiltered) + (accZfiltered * accZfiltered))));
				rollDegrees = Math.toDegrees(Math.atan(accYfiltered / Math.sqrt((accXfiltered * accXfiltered) + (accZfiltered * accZfiltered))));

				setPitch(pitchDegrees);
				setRoll(rollDegrees);

				if (verboseAcc) {
					System.out.println("Pitch & Roll with Accelerometer:");
					System.out.println(String.format("\tX:%f, Y:%f, Z:%f", accXfiltered, accYfiltered, accZfiltered));
					System.out.println(String.format("\tPitch:%f, Roll:%f", pitchDegrees, rollDegrees));
				}
			}
			// Request magnetometer measurements.
			if (magnetometer != null) {
				magnetometer.write((byte) LSM303_REGISTER_MAG_OUT_X_H_M);
				// Reading magnetometer measurements.
				int r = magnetometer.read(magData, 0, 6);
				if (r != 6) {
					System.out.println("Error reading mag data, < 6 bytes");
				} else if (verboseMag) {
					dumpBytes(magData, 6);
				}
				// Mag raw data. !!! Warning !!! Order here is X, Z, Y
				magneticX = mag16(magData, 0); // X
				magneticZ = mag16(magData, 2); // Yes, Z
				magneticY = mag16(magData, 4); // Then Y

				magX = magneticX;
				magY = magneticY;
				magZ = magneticZ;

				magX = (calibrationMap.get(MAG_X_OFFSET) + (magX * calibrationMap.get(MAG_X_COEFF)));
				magY = (calibrationMap.get(MAG_Y_OFFSET) + (magY * calibrationMap.get(MAG_Y_COEFF)));
				magZ = (calibrationMap.get(MAG_Z_OFFSET) + (magZ * calibrationMap.get(MAG_Z_COEFF)));

				// TODO See that...
//		  magX = magX / _lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA;
//		  magY = magY / _lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA;
//		  magZ = magZ / _lsm303Mag_Gauss_LSB_Z * SENSORS_GAUSS_TO_MICROTESLA;

				magNorm = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

				if (USE_NORM && magNorm != 0) {
					magX /= magNorm;
					magY /= magNorm;
					magZ /= magNorm;
				}

				if (useLowPassFilter) {
					magXfiltered = lowPass(ALPHA, magX, magXfiltered);
					magYfiltered = lowPass(ALPHA, magY, magYfiltered);
					magZfiltered = lowPass(ALPHA, magZ, magZfiltered);
				} else {
					magXfiltered = magX;
					magYfiltered = magY;
					magZfiltered = magZ;
				}

				double magXcomp = magXfiltered;
				double magYcomp = magYfiltered;
				if (pitchDegrees != -Double.MAX_VALUE && rollDegrees != -Double.MAX_VALUE) {
					magXcomp = (magXfiltered * Math.cos(Math.toRadians(pitchDegrees))) + (magZfiltered * Math.sin(Math.toRadians(pitchDegrees)));
					magYcomp = (magYfiltered * Math.cos(Math.toRadians(rollDegrees))) + (magZfiltered * Math.sin(Math.toRadians(rollDegrees)));
				}
				heading = Math.toDegrees(Math.atan2(magYcomp, magXcomp));
				while (heading < 0) {
					heading += 360f;
				}
				setHeading(heading);
			}
			if (verboseMag) {
				System.out.println(String.format("Raw(int)Mag XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X), HDG:%f", magneticX, magneticY, magneticZ, magneticX & 0xFFFF, magneticY & 0xFFFF, magneticZ & 0xFFFF, heading));
			}

			if (verboseRaw) {
				System.out.println(String.format("RawAcc (XYZ) (%d, %d, %d)\tRawMag (XYZ) (%d, %d, %d)", accelX, accelY, accelZ, magneticX, magneticY, magneticZ));
			}

			if (dataListener != null) {
				// Use the values as you want here.
				dataListener.dataDetected(accelX, accelY, accelZ, magneticX, magneticY, magneticZ, heading, pitchDegrees, rollDegrees);
			} else {
				if (verbose) {
					System.out.println(
							String.format("heading: %s (mag), pitch: %s, roll: %s",
									Z_FMT.format(heading),
									Z_FMT.format(pitch),
									Z_FMT.format(roll)));
				}
			}
			if (logForCalibration) {
				System.out.println(
						String.format("%d;%d;%d;%d;%d;%d;%f;%f;%f;%f;%f;%f;%f;%f",
								accelX,       // Raw data
								accelY,
								accelZ,
								magneticX,
								magneticY,
								magneticZ,
								accXfiltered, // filtered (smoothed, low pass filter)
								accYfiltered,
								accZfiltered,
								magXfiltered,
								magYfiltered,
								magZfiltered,
								accNorm,      // norms
								magNorm));
			}
			try {
				if (this.wait > 0) {
					Thread.sleep(this.wait);
				}
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
		if (verbose) {
			System.out.println("Exiting LSM303 reading thread");
		}
	}

	private static double lowPass(double alpha, double value, double acc) {
		return (value * alpha) + (acc * (1d - alpha));
	}

	private static int accel12(byte[] list, int idx) {
//	int n = (list[idx] & 0xFF) | ((list[idx + 1] & 0xFF) << 8); // Low, high bytes
		int n = ((list[idx + 1] & 0xFF) << 8) | (list[idx] & 0xFF); // Low, high bytes
		if (n > 32_767) {
			n -= 65_536;              // 2's complement signed
		}
		return n >> 4;              // 12-bit resolution
	}

	private static int mag16(byte[] list, int idx) {
		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF);   // High, low bytes
		return (n < 32_768 ? n : n - 65_536);                         // 2's complement signed
	}

	private static void dumpBytes(byte[] ba, int len) {
		String str = String.format("%d bytes: ", len);
		for (int i = 0; i < len; i++) {
			str += (StringUtils.lpad(Integer.toHexString(ba[i] & 0xFF).toUpperCase(), 2, "0") + " ");
		}
		System.out.println(str);
	}

	/**
	 * This is for tests.
	 * Keep reading until Ctrl+C is received.
	 *
	 * @param args
	 * @throws I2CFactory.UnsupportedBusNumberException
	 */
	public static void main(String... args) {
//	verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
//	System.out.println("Verbose: " + verbose);

//	System.setProperty("lsm303.log.for.calibration", "true");

		try {
			LSM303 sensor = new LSM303(false);
			sensor.setWait(250); // 1/4 sec

			// Calibration values
			if (!"true".equals(System.getProperty("lsm303.log.for.calibration"))) {
				sensor.setCalibrationValue(LSM303.MAG_X_OFFSET, 9);
				sensor.setCalibrationValue(LSM303.MAG_Y_OFFSET, -16);

				System.out.println(sensor.getCalibrationMap());
			}

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (verbose) {
					System.out.println("\nQuitting...");
				}
				synchronized (sensor) {
					sensor.setKeepReading(false);
					try {
						Thread.sleep(2 * sensor.wait);
						if (verbose) {
							System.out.println("Bye.");
						}
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
			}));
			sensor.startReading();
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			System.err.println("Bad bus. Not on a Raspberry Pi?");
			ubne.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("IO Error");
			ioe.printStackTrace();
		}
	}
}
