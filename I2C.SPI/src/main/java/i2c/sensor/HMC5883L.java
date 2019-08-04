package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import utils.StringUtils;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * 3 Axis compass
 * TODO Reuse the code of LSM303
 */
public class HMC5883L {
	private final static int HMC5883L_ADDRESS = 0x1E;

	private final static int HMC5883L_REGISTER_MR_REG_M  = 0x02;
	private final static int HMC5883L_REGISTER_OUT_X_H_M = 0x03;

	private final static float SCALE = 0.92F;

	private I2CDevice magnetometer;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose    = "true".equals(System.getProperty("hmc5883l.verbose", "false"));
	private static boolean verboseRaw = "true".equals(System.getProperty("hmc5883l.verbose.raw", "false"));
	private static boolean verboseMag = "true".equals(System.getProperty("hmc5883l.verbose.mag", "false"));

	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L;

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

	private void readingSensors()
			throws IOException {
		while (keepReading) {
			byte[] magData = new byte[6];

			double magX = 0, magY = 0, magZ = 0;

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
				magX = mag16(magData, 0) * SCALE;
				magZ = mag16(magData, 2) * SCALE; // Yes, Z
				magY = mag16(magData, 4) * SCALE; // Then Y

				heading = (float) Math.toDegrees(Math.atan2(magY, magX));
				while (heading < 0) {
					heading += 360f;
				}
				setHeading(heading);

				pitch = Math.toDegrees(Math.atan2(magY, magZ)); // See how it's done in LSM303...
				setPitch(pitch);
				roll = Math.toDegrees(Math.atan2(magX, magZ));
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

			try {
				Thread.sleep(this.wait);
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
	}

	private static int mag16(byte[] list, int idx) {
		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF); // High, low bytes
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
		System.out.println("Verbose: " + verbose);
		HMC5883L sensor = new HMC5883L();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nBye.");
			synchronized (sensor) {
				sensor.stopReading();
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
