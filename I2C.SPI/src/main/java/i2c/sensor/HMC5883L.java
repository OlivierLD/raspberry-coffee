package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import utils.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * 3 Axis compass
 * TODO Reuse the code of LSM303? Or use this one in the LSM303 code?
 */
public class HMC5883L {
	private final static int HMC5883L_ADDRESS = 0x1E;

	private final static int HMC5883L_REGISTER_MR_REG_M  = 0x02;
	private final static int HMC5883L_REGISTER_OUT_X_H_M = 0x03;

	private final static float SCALE = 1F; // 0.92F; // TODO This is a constant... is that any useful?
	private final float ALPHA = 0.15f; // For the low pass filter (smoothing)

	private I2CDevice magnetometer;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose    = "true".equals(System.getProperty("hmc5883l.verbose", "false"));
	private static boolean verboseRaw = "true".equals(System.getProperty("hmc5883l.verbose.raw", "false"));
	private static boolean verboseMag = "true".equals(System.getProperty("hmc5883l.verbose.mag", "false"));

	private static boolean useLowPassFilter = "true".equals(System.getProperty("hmc5883l.low.pass.filter", "true")); // default true
	private static boolean logForCalibration = "true".equals(System.getProperty("hmc5883l.log.for.calibration", "false"));

	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L;

	// Keys for the calibration map
	private final static String MAG_X_OFFSET = "MagXOffset";
	private final static String MAG_Y_OFFSET = "MagYOffset";
	private final static String MAG_Z_OFFSET = "MagZOffset";

	private final static String MAG_X_COEFF = "MagXCoeff";
	private final static String MAG_Y_COEFF = "MagYCoeff";
	private final static String MAG_Z_COEFF = "MagZCoeff";

	private final static Map<String, Double> DEFAULT_MAP = new HashMap<>();

	static {
		DEFAULT_MAP.put(MAG_X_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_Y_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_Z_OFFSET, 0d);
		DEFAULT_MAP.put(MAG_X_COEFF, 1d);
		DEFAULT_MAP.put(MAG_Y_COEFF, 1d);
		DEFAULT_MAP.put(MAG_Z_COEFF, 1d);
	}

	private Map<String, Double> calibrationMap = new HashMap<>(DEFAULT_MAP);

	private void setCalibrationValue(String key, double val) {
		// WARNING!! The values depend heavily on USE_NORM value.
		calibrationMap.put(key, val);
	}

	private Map<String, Double> getCalibrationMap() {
		return calibrationMap;
	}

	public HMC5883L() throws I2CFactory.UnsupportedBusNumberException, IOException {
		if (verbose) {
			System.out.println("Starting sensors reading:");
		}
//		try {
		// Get i2c bus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
		if (verbose) {
			System.out.println("Connected to bus. OK.");
		}
		magnetometer = bus.getDevice(HMC5883L_ADDRESS);

		if (verbose) {
			System.out.println("Connected to devices. OK.");
		}
		Properties hmc5883lCalProps = new Properties();
		try {
			hmc5883lCalProps.load(new FileReader(System.getProperty("hmc5883l.cal.prop.file", "hmc5883l.cal.properties")));
		} catch (Exception ex) {
			System.out.println("Defaulting Calibration Properties");
		}
		// Calibration values
		if (!"true".equals(System.getProperty("hmc5883l.log.for.calibration"))) {
			// WARNING: Those value might not fit your device!!! They ~fit one of mines...
			// MAG offsets
			this.setCalibrationValue(HMC5883L.MAG_X_OFFSET, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_X_OFFSET, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_X_OFFSET)))));
			this.setCalibrationValue(HMC5883L.MAG_Y_OFFSET, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_Y_OFFSET, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_Y_OFFSET)))));
			this.setCalibrationValue(HMC5883L.MAG_Z_OFFSET, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_Z_OFFSET, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_Z_OFFSET)))));
			// MAG coeffs
			this.setCalibrationValue(HMC5883L.MAG_X_COEFF, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_X_COEFF, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_X_COEFF)))));
			this.setCalibrationValue(HMC5883L.MAG_Y_COEFF, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_Y_COEFF, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_Y_COEFF)))));
			this.setCalibrationValue(HMC5883L.MAG_Z_COEFF, Double.parseDouble(hmc5883lCalProps.getProperty(HMC5883L.MAG_Z_COEFF, String.valueOf(DEFAULT_MAP.get(HMC5883L.MAG_Z_COEFF)))));
			System.out.println("Calibration parameters:" + this.getCalibrationMap());
		}

		/*
		 * Start sensing
		 */
		if (magnetometer != null) {
			magnetometer.write(HMC5883L_REGISTER_MR_REG_M, (byte) 0x00);
			if (verbose) {
				System.out.println("Magnetometer OK.");
			}
		}
		startReading();
	}

	// Create a separate thread to read the sensors
	private void startReading() {
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

	private void stopReading() {
		this.keepReading = false;
	}

	private static double lowPass(double alpha, double value, double acc) {
		return (value * alpha) + (acc * (1d - alpha));
	}

	private void readingSensors()
			throws IOException {
		while (keepReading) {
			byte[] magData = new byte[6];

			double magX = 0, magY = 0, magZ = 0;
			double magXFiltered = 0d, magYFiltered = 0d, magZFiltered = 0d;

			// Request magnetometer measurements.
			if (magnetometer != null) {
				magnetometer.write((byte) HMC5883L_REGISTER_OUT_X_H_M);
				// Reading magnetometer measurements.
				int r = magnetometer.read(magData, 0, 6);
				if (r != 6) {
					System.out.println("Error reading mag data, < 6 bytes");
				} else if (verboseMag) {
					dumpBytes(magData);
				}
				// Mag raw data. !!! Warning !!! Order here is X, Z, Y
//				magX = mag16(magData, 0) * SCALE; // X
//				magZ = mag16(magData, 2) * SCALE; // Yes, Z, not Y
//				magY = mag16(magData, 4) * SCALE; // And then Y

				magX = mag16(magData, 0) * SCALE; // X
				magY = mag16(magData, 2) * SCALE; // Y
				magZ = mag16(magData, 4) * SCALE; // Z

				if (!logForCalibration) {
					magX = calibrationMap.get(MAG_X_COEFF) * (calibrationMap.get(MAG_X_OFFSET) + magX);
					magY = calibrationMap.get(MAG_Y_COEFF) * (calibrationMap.get(MAG_Y_OFFSET) + magY);
					magZ = calibrationMap.get(MAG_Z_COEFF) * (calibrationMap.get(MAG_Z_OFFSET) + magZ);
				}

				if (useLowPassFilter) {
					magXFiltered = lowPass(ALPHA, magX, magXFiltered);
					magYFiltered = lowPass(ALPHA, magY, magYFiltered);
					magZFiltered = lowPass(ALPHA, magZ, magZFiltered);
				} else {
					magXFiltered = magX;
					magYFiltered = magY;
					magZFiltered = magZ;
				}

				if (logForCalibration) {
//					if (!(Math.abs(magX) > 1_000) && !(Math.abs(magY) > 1_000) && !(Math.abs(magZ) > 1_000)) { // Skip aberrations
						System.out.println(String.format("%d;%d;%d;%.03f;%.03f;%.03f", (int) magX, (int) magY, (int) magZ, magXFiltered, magYFiltered, magZFiltered));
//					}
				}

				heading = (float) Math.toDegrees(Math.atan2(magYFiltered, magXFiltered));
				while (heading < 0) {
					heading += 360f;
				}
				setHeading(heading);

				pitch = Math.toDegrees(Math.atan2(magYFiltered, magZFiltered)); // See how it's done in LSM303... See what's best.
				setPitch(pitch);
				roll = Math.toDegrees(Math.atan2(magXFiltered, magZFiltered));
				setRoll(roll);
			}
//		if (verboseMag) {
//			System.out.println(String.format("Raw(int)Mag XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X), HDG:%f", magX, magY, magZ, magX & 0xFFFF, magY & 0xFFFF, magZ & 0xFFFF, heading));
//		}

			if (verboseRaw) {
				System.out.println(String.format("RawMag (XYZ) (%f, %f, %f)", magX, magY, magZ));
			}

			if (verbose) {
				System.out.println(String.format(
						"heading: %s (mag), pitch: %s, roll: %s",
						Z_FMT.format(heading),
						Z_FMT.format(pitch),
						Z_FMT.format(roll)));
			}

			if (this.wait > 0) {
				try {
					Thread.sleep(this.wait);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
			}
		}
	}

	private static int mag16(byte[] list, int idx) {
//		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF); // High, low bytes
//		return (n < 0x8000 ? n : n - 0x10000);                      // 2's complement signed

		int n = ((list[idx] & 0xFF) | ((list[idx + 1] & 0xFF) << 8));
		return (n < 0x8000 ? n : n - 0x10000);                      // 2's complement signed
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
	 *
	 * @param args Unused
	 * @throws I2CFactory.UnsupportedBusNumberException as you can imagine
	 */
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException, IOException {
		verbose = "true".equals(System.getProperty("hmc5883l.verbose", "false"));
//		System.out.println("Verbose: " + verbose);

		if (logForCalibration) {
			System.out.println("magX;magY;magZ;filterMagX;filterMagY;filterMagZ");
		}

		HMC5883L sensor = new HMC5883L();
		sensor.setWait(250);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (!logForCalibration) {
				System.out.println("\nBye.");
			}
			synchronized (sensor) {
				sensor.stopReading();
				try {
					Thread.sleep(sensor.wait);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
			}
		}, "Shutdown Hook"));
		sensor.startReading();
	}
}
