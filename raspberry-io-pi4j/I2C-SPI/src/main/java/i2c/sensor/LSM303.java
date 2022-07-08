package i2c.sensor;

import calc.GeomUtil;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.listener.LSM303Listener;
import utils.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
 * <li>The data read from the magnetometer are read in the order X, <b style='color: red;'>Z</b>, Y, <u>funny</u> isn't it?</li>
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
 *
 * Uses the following System variables:
 * -Dlsm303.low.pass.filter default true
 * -Dlsm303.verbose default false
 * -Dlsm303.verbose.raw default false
 * -Dlsm303.verbose.mag default false
 * -Dlsm303.verbose.acc default false
 * -Dlsm303.log.for.calibration default false
 *
 * -Dlsm303.pitch.roll.adjust default true
 *
 * -Dlsm303.cal.prop.file default "lsm303.cal.properties"
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

  Note: 1E is also the address of the HMC5883L
   */
	// Those 2 next addresses are returned by "sudo i2cdetect -y 1", see above.
	private final static int LSM303_ADDRESS_ACCEL = (0x32 >> 1); // 0011001x, 0x19
	private final static int LSM303_ADDRESS_MAG = (0x3C >> 1);   // 0011110x, 0x1E <- that is an HMC5883L !
	// Default    Type
	private final static int LSM303_REGISTER_ACCEL_CTRL_REG1_A = 0x20; // 00000111   rw
	private final static int LSM303_REGISTER_ACCEL_CTRL_REG4_A = 0x23; // 00000000   rw
	private final static int LSM303_REGISTER_ACCEL_OUT_X_L_A = 0x28;
	private final static int LSM303_REGISTER_MAG_CRB_REG_M = 0x01;
	private final static int LSM303_REGISTER_MAG_MR_REG_M = 0x02;
	private final static int LSM303_REGISTER_MAG_OUT_X_H_M = 0x03;

	// Gain settings for setMagGain()
	private final static int LSM303_MAGGAIN_1_3 = 0x20; // +/- 1.3
	private final static int LSM303_MAGGAIN_1_9 = 0x40; // +/- 1.9
	private final static int LSM303_MAGGAIN_2_5 = 0x60; // +/- 2.5
	private final static int LSM303_MAGGAIN_4_0 = 0x80; // +/- 4.0
	private final static int LSM303_MAGGAIN_4_7 = 0xA0; // +/- 4.7
	private final static int LSM303_MAGGAIN_5_6 = 0xC0; // +/- 5.6
	private final static int LSM303_MAGGAIN_8_1 = 0xE0; // +/- 8.1

//	private final static float _lsm303Accel_MG_LSB = 0.001F; // 1, 2, 4 or 12 mg per lsb
	private final static float _lsm303Accel_MG_LSB = 16_704.0F;
	private static float _lsm303Mag_Gauss_LSB_XY = 1_100.0F; // Varies with gain
	private static float _lsm303Mag_Gauss_LSB_Z = 980.0F;    // Varies with gain

	private float SENSORS_GRAVITY_EARTH = 9.80665f;        // Earth's gravity in m/s^2
	private float SENSORS_GRAVITY_MOON = 1.6f;             // The moon's gravity in m/s^2
	private float SENSORS_GRAVITY_SUN = 275.0f;            // The sun's gravity in m/s^2
	private float SENSORS_GRAVITY_STANDARD = SENSORS_GRAVITY_EARTH; // Earth, by default ;)
	private float SENSORS_MAGFIELD_EARTH_MAX = 60.0f;      // Maximum magnetic field on Earth's surface
	private float SENSORS_MAGFIELD_EARTH_MIN = 30.0f;      // Minimum magnetic field on Earth's surface
	private float SENSORS_PRESSURE_SEALEVELHPA = 1_013.25f;// Average sea level pressure is 1013.25 hPa, on Earth
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

	private static boolean pitchRollHeadingAdjust = "true".equals(System.getProperty("lsm303.pitch.roll.adjust", "true"));

	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L; // Default
	private LSM303Listener dataListener = null;

	// Keys for the calibration map
	private final static String MAG_X_OFFSET = "MagXOffset";
	private final static String MAG_Y_OFFSET = "MagYOffset";
	private final static String MAG_Z_OFFSET = "MagZOffset";

	private final static String MAG_X_COEFF = "MagXCoeff";
	private final static String MAG_Y_COEFF = "MagYCoeff";
	private final static String MAG_Z_COEFF = "MagZCoeff";

	private final static String ACC_X_OFFSET = "AccXOffset";
	private final static String ACC_Y_OFFSET = "AccYOffset";
	private final static String ACC_Z_OFFSET = "AccZOffset";

	private final static String ACC_X_COEFF = "AccXCoeff";
	private final static String ACC_Y_COEFF = "AccYCoeff";
	private final static String ACC_Z_COEFF = "AccZCoeff";

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

	private void setCalibrationValue(String key, double val) {
		// WARNING!! The values depend heavily on USE_NORM value.
		calibrationMap.put(key, val);
	}

	private Map<String, Double> getCalibrationMap() {
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

	public LSM303(EnabledFeature feature) throws I2CFactory.UnsupportedBusNumberException, IOException {
		this(feature, true);
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

		Properties lsm303CalProps = new Properties();
		String calPropFileName = System.getProperty("lsm303.cal.prop.file", "lsm303.cal.properties");
		try {
			lsm303CalProps.load(new FileReader(calPropFileName));
		} catch (Exception ex) {
			System.out.println(String.format("File %s: %s. Defaulting Calibration Properties", calPropFileName, ex.toString()));
		}
		// Calibration values
		if (!"true".equals(System.getProperty("lsm303.log.for.calibration"))) {
			// WARNING: Those value might not fit your device!!! They ~fit one of mines...

			// MAG offsets
			this.setCalibrationValue(LSM303.MAG_X_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_X_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_X_OFFSET)))));
			this.setCalibrationValue(LSM303.MAG_Y_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_Y_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_Y_OFFSET)))));
			this.setCalibrationValue(LSM303.MAG_Z_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_Z_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_Z_OFFSET)))));
			// MAG coeffs
			this.setCalibrationValue(LSM303.MAG_X_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_X_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_X_COEFF)))));
			this.setCalibrationValue(LSM303.MAG_Y_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_Y_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_Y_COEFF)))));
			this.setCalibrationValue(LSM303.MAG_Z_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.MAG_Z_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.MAG_Z_COEFF)))));

			// ACC offsets
			this.setCalibrationValue(LSM303.ACC_X_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_X_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_X_OFFSET)))));
			this.setCalibrationValue(LSM303.ACC_Y_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_Y_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_Y_OFFSET)))));
			this.setCalibrationValue(LSM303.ACC_Z_OFFSET, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_Z_OFFSET, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_Z_OFFSET)))));
			// ACC coeffs
			this.setCalibrationValue(LSM303.ACC_X_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_X_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_X_COEFF)))));
			this.setCalibrationValue(LSM303.ACC_Y_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_Y_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_Y_COEFF)))));
			this.setCalibrationValue(LSM303.ACC_Z_COEFF, Double.parseDouble(lsm303CalProps.getProperty(LSM303.ACC_Z_COEFF, String.valueOf(DEFAULT_MAP.get(LSM303.ACC_Z_COEFF)))));

			System.out.println("Calibration parameters:" + this.getCalibrationMap());
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
		new Thread(task, "lsm303-reader").start();
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

	private final static boolean USE_NORM = false; // TODO See what the default should be...

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
			double accXFiltered = 0d, accYFiltered = 0d, accZFiltered = 0d;
			int magneticX = 0, magneticY = 0, magneticZ = 0;
			double magX = 0d, magY = 0d, magZ = 0d;
			double magXFiltered = 0d, magYFiltered = 0d, magZFiltered = 0d;

			double pitchDegrees = -Double.MAX_VALUE, rollDegrees = -Double.MAX_VALUE;
			double heading = 0f;
			double magNorm = 0d, accNorm = 0d;

			if (accelerometer != null) {
				try {
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

					accNorm = Math.sqrt((accX * accX) + (accY * accY) + (accZ * accZ));

					if (verboseAcc) {
						System.out.println(String.format("Acc norm: %f", accNorm));
					}
					if (USE_NORM && accNorm != 0) {
						accX /= accNorm;
						accY /= accNorm;
						accZ /= accNorm;
					}

					accX = calibrationMap.get(ACC_X_COEFF) * (calibrationMap.get(ACC_X_OFFSET) + accX);
					accY = calibrationMap.get(ACC_Y_COEFF) * (calibrationMap.get(ACC_Y_OFFSET) + accY);
					accZ = calibrationMap.get(ACC_Z_COEFF) * (calibrationMap.get(ACC_Z_OFFSET) + accZ);

					if (useLowPassFilter) {
						accXFiltered = lowPass(ALPHA, accX, accXFiltered);
						accYFiltered = lowPass(ALPHA, accY, accYFiltered);
						accZFiltered = lowPass(ALPHA, accZ, accZFiltered);
					} else {
						accXFiltered = accX;
						accYFiltered = accY;
						accZFiltered = accZ;
					}
				/*
					pitch = atan (x / sqrt(y^2 + z^2));
					roll  = atan (y / sqrt(x^2 + z^2));
				 */
					pitchDegrees = Math.toDegrees(Math.atan(accXFiltered / Math.sqrt((accYFiltered * accYFiltered) + (accZFiltered * accZFiltered))));
					rollDegrees = Math.toDegrees(Math.atan(accYFiltered / Math.sqrt((accXFiltered * accXFiltered) + (accZFiltered * accZFiltered))));

					setPitch(pitchDegrees);
					setRoll(rollDegrees);

					if (verboseAcc) {
						System.out.println("Pitch & Roll with Accelerometer:");
						System.out.println(String.format("\tX:%f, Y:%f, Z:%f", accXFiltered, accYFiltered, accZFiltered));
						System.out.println(String.format("\tPitch:%f, Roll:%f", pitchDegrees, rollDegrees));
					}
				} catch (IOException ioe) {
					System.err.println("Error writing to Accelerometer");
					ioe.printStackTrace();
				}
			}
			// Request magnetometer measurements.
			if (magnetometer != null) {
				try {
					magnetometer.write((byte) LSM303_REGISTER_MAG_OUT_X_H_M);
					// Reading magnetometer measurements.
					int r = magnetometer.read(magData, 0, 6);
					if (r != 6) {
						System.out.println("Error reading mag data, < 6 bytes");
					} else if (verboseMag) {
						dumpBytes(magData);
					}
					// Mag raw data. !!! Warning !!! Order here is X, Z, Y
					magneticX = mag16(magData, 0); // X
					magneticZ = mag16(magData, 2); // Yes, Z
					magneticY = mag16(magData, 4); // Then Y

					magX = magneticX;
					magY = magneticY;
					magZ = magneticZ;

					magNorm = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

					if (USE_NORM && magNorm != 0) {
						magX /= magNorm;
						magY /= magNorm;
						magZ /= magNorm;
					}

					magX = calibrationMap.get(MAG_X_COEFF) * (calibrationMap.get(MAG_X_OFFSET) + magX);
					magY = calibrationMap.get(MAG_Y_COEFF) * (calibrationMap.get(MAG_Y_OFFSET) + magY);
					magZ = calibrationMap.get(MAG_Z_COEFF) * (calibrationMap.get(MAG_Z_OFFSET) + magZ);

					// TODO See that..., optional? They're all constants...
//				magX /= (_lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA);
//				magY /= (_lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA);
//				magZ /= (_lsm303Mag_Gauss_LSB_Z * SENSORS_GAUSS_TO_MICROTESLA);

					if (useLowPassFilter) {
						magXFiltered = lowPass(ALPHA, magX, magXFiltered);
						magYFiltered = lowPass(ALPHA, magY, magYFiltered);
						magZFiltered = lowPass(ALPHA, magZ, magZFiltered);
					} else {
						magXFiltered = magX;
						magYFiltered = magY;
						magZFiltered = magZ;
					}

					double magXComp = magXFiltered;
					double magYComp = magYFiltered;

					double beforeAdjust = GeomUtil.getDir((float) magYComp, (float) magXComp); // For dev

					if (pitchRollHeadingAdjust && accelerometer != null && pitchDegrees != -Double.MAX_VALUE && rollDegrees != -Double.MAX_VALUE) {
						magXComp = (magXFiltered * Math.cos(Math.toRadians(pitchDegrees))) + (magZFiltered * Math.sin(Math.toRadians(pitchDegrees)));
						magYComp = (magYFiltered * Math.cos(Math.toRadians(rollDegrees))) + (magZFiltered * Math.sin(Math.toRadians(rollDegrees)));
					}

					double afterAdjust = GeomUtil.getDir((float) magYComp, (float) magXComp); // For development

					heading = Math.toDegrees(Math.atan2(magYComp, magXComp));
					while (heading < 0) {
						heading += 360f;
					}

					if (verboseRaw) {
						System.out.println(String.format("RAW mag data (2): X:%f Y:%f => (before %.02f, after %.02f, delta %.02f) (HDG %.02f) ", magXComp, magYComp, beforeAdjust, afterAdjust, (afterAdjust - beforeAdjust), heading));
					}

					setHeading(heading);
				} catch (IOException ioe) {
					System.err.println("Error writing to Magnetometer");
					ioe.printStackTrace();
				}
			} else {
				if (verbose) {
					System.out.println("magnetometer is null");
				}
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

								accXFiltered, // filtered (smoothed, low pass filter)
								accYFiltered,
								accZFiltered,

								magXFiltered,
								magYFiltered,
								magZFiltered,

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

	private static void dumpBytes(byte[] ba) {
		StringBuilder sb = new StringBuilder();
		int len = 6;
		sb.append(String.format("%d bytes: ", len));
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%s ", StringUtils.lpad(Integer.toHexString(ba[i] & 0xFF).toUpperCase(), 2, "0")));
		}
		System.out.println(sb.toString());
	}

	/**
	 * This is for tests.
	 * Keep reading until Ctrl+C is received.
	 * can use --feature:XXX as CLI parameter, XXX can be BOTH, MAGNETOMETER, or ACCELEROMETER
	 *
	 * @param args Duh
	 */
	public static void main(String... args) {
//	verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
//	System.out.println("Verbose: " + verbose);

//	System.setProperty("lsm303.log.for.calibration", "true");

		EnabledFeature feature = EnabledFeature.BOTH;

		if (args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith("--feature:")) {
					String theOne = arg.substring("--feature:".length());
					for (EnabledFeature f : EnabledFeature.values()) {
						if (f.toString().equals(theOne)) {
							feature = f;
							break;
						}
					}
				}
			}
		}

		try {
			LSM303 sensor = new LSM303(feature, false);
			sensor.setWait(250); // 1/4 sec between reads

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
			}, "Shutdown Hook"));
			sensor.startReading();
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			System.err.println("Bad bus. Not on a Raspberry Pi?");
			ubne.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("IO Error, device not found?");
			ioe.printStackTrace();
		}
	}
}
