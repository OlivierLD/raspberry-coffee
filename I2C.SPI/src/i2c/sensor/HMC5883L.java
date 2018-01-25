package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import utils.StringUtils;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.TimeUtil.delay;

/*
 * 3 Axis compass
 */
public class HMC5883L {
	private final static int HMC5883L_ADDRESS = 0x1E;


	public final static int HMC5883L_REGISTER_CRB_REG_M = 0x01;
	public final static int HMC5883L_REGISTER_MR_REG_M  = 0x02;
	public final static int HMC5883L_REGISTER_OUT_X_H_M = 0x03;

	// Gain settings for setMagGain()
	public final static int HMC5883L_GAIN_1_3 = 0x20; // +/- 1.3
	public final static int HMC5883L_GAIN_1_9 = 0x40; // +/- 1.9
	public final static int HMC5883L_GAIN_2_5 = 0x60; // +/- 2.5
	public final static int HMC5883L_GAIN_4_0 = 0x80; // +/- 4.0
	public final static int HMC5883L_GAIN_4_7 = 0xA0; // +/- 4.7
	public final static int HMC5883L_GAIN_5_6 = 0xC0; // +/- 5.6
	public final static int HMC5883L_GAIN_8_1 = 0xE0; // +/- 8.1

//	private static float _hmc5883l_Gauss_LSB_XY = 1100.0F;  // Varies with gain
//	private static float _hmc5882l_Gauss_LSB_Z = 980.0F;  // Varies with gain

	private I2CBus bus;
	private I2CDevice magnetometer = null;
	private byte[] magData;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
	private static boolean verboseRaw = "true".equals(System.getProperty("lsm303.verbose.raw", "false"));

	private static boolean verboseMag = "true".equals(System.getProperty("lsm303.verbose.mag", "false"));


	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L;

//	private void setMagGain(int gain) throws IOException {
//		magnetometer.write(HMC5883L_REGISTER_CRB_REG_M, (byte) gain);
//
//		switch (gain) {
//			case HMC5883L_GAIN_1_3:
//				_hmc5883l_Gauss_LSB_XY = 1_100F;
//				_hmc5882l_Gauss_LSB_Z = 980F;
//				break;
//			case HMC5883L_GAIN_1_9:
//				_hmc5883l_Gauss_LSB_XY = 855F;
//				_hmc5882l_Gauss_LSB_Z = 760F;
//				break;
//			case HMC5883L_GAIN_2_5:
//				_hmc5883l_Gauss_LSB_XY = 670F;
//				_hmc5882l_Gauss_LSB_Z = 600F;
//				break;
//			case HMC5883L_GAIN_4_0:
//				_hmc5883l_Gauss_LSB_XY = 450F;
//				_hmc5882l_Gauss_LSB_Z = 400F;
//				break;
//			case HMC5883L_GAIN_4_7:
//				_hmc5883l_Gauss_LSB_XY = 400F;
//				_hmc5882l_Gauss_LSB_Z = 355F;
//				break;
//			case HMC5883L_GAIN_5_6:
//				_hmc5883l_Gauss_LSB_XY = 330F;
//				_hmc5882l_Gauss_LSB_Z = 295F;
//				break;
//			case HMC5883L_GAIN_8_1:
//				_hmc5883l_Gauss_LSB_XY = 230F;
//				_hmc5882l_Gauss_LSB_Z = 205F;
//				break;
//		}
//	}

	public HMC5883L() throws I2CFactory.UnsupportedBusNumberException, IOException {
		if (verbose) {
			System.out.println("Starting sensors reading:");
		}
//		try {
		// Get i2c bus
		bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPI version
		if (verbose) {
			System.out.println("Connected to bus. OK.");
		}
		magnetometer = bus.getDevice(HMC5883L_ADDRESS);

		if (verbose) {
			System.out.println("Connected to devices. OK.");
		}
		/*
		 * Start sensing
		 */
		// Enable magnetometer
		if (magnetometer != null) {
			magnetometer.write(HMC5883L_REGISTER_MR_REG_M, (byte) 0x00);

			int gain = HMC5883L_GAIN_1_3;
//		setMagGain(gain);
			if (verbose) {
				System.out.println("Magnetometer OK.");
			}
		}

		startReading();
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

	private void readingSensors()
			throws IOException {
		while (keepReading) {
			magData = new byte[6];

			int accelX = 0, accelY = 0, accelZ = 0;
			float accX = 0, accY = 0, accZ = 0;
			int magX = 0, magY = 0, magZ = 0;
			double pitchDegrees = 0d, rollDegrees = 0d;
			float heading = 0;

			// Request magnetometer measurements.
			if (magnetometer != null) {
				magnetometer.write((byte) HMC5883L_REGISTER_OUT_X_H_M);
				// Reading magnetometer measurements.
				int r = magnetometer.read(magData, 0, 6);
				if (r != 6) {
					System.out.println("Error reading mag data, < 6 bytes");
				} else if (verboseMag) {
					dumpBytes(magData, 6);
				}
				// Mag raw data. !!! Warning !!! Order here is X, Z, Y
				magX = mag16(magData, 0);
				magZ = mag16(magData, 2); // Yes, Z
				magY = mag16(magData, 4); // Then Y

				heading = (float) Math.toDegrees(Math.atan2((double) magY, (double) magX));
				while (heading < 0) {
					heading += 360f;
				}

				setHeading(heading);
			}
			if (verboseMag) {
				System.out.println(String.format("Raw(int)Mag XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X), HDG:%f", magX, magY, magZ, magX & 0xFFFF, magY & 0xFFFF, magZ & 0xFFFF, heading));
			}

			if (verboseRaw) {
				System.out.println(String.format("RawAcc (XYZ) (%d, %d, %d)\tRawMag (XYZ) (%d, %d, %d)", accelX, accelY, accelZ, magX, magY, magZ));
			}

			if (verbose) {
				System.out.println(String.format("heading: %s (mag), pitch: %s, roll: %s",
						Z_FMT.format(heading),
						Z_FMT.format(pitch),
						Z_FMT.format(roll)));
			}

			try {
				Thread.sleep(this.wait);
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
	}

	private static int mag16(byte[] list, int idx) {
		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF);   // High, low bytes
		return (n < 32768 ? n : n - 65536);                           // 2's complement signed
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
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException, IOException {
		verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
		System.out.println("Verbose: " + verbose);
		HMC5883L sensor = new HMC5883L();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nBye.");
			synchronized (sensor) {
				sensor.setKeepReading(false);
				try {
					Thread.sleep(sensor.wait);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
			}
		}));
		sensor.startReading();
	}
}
