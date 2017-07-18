package i2c.sensor;

import i2c.sensor.listener.LSM303Listener;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import utils.StringUtils;

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
 *   <li>The data read from the accelerometer are read in the order X, Y, Z</li>
 *   <li>The data read from the magnetometer are read in the order X, <b style='color: red;'>Z</b>, Y, funny isn't it?</li>
 * </ul>
 * And they both have different endianness.
 * <br>
 * It took me a while to figure this all out...
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
	public final static int LSM303_ADDRESS_MAG = (0x3C >> 1);   // 0011110x, 0x1E
	// Default    Type
	public final static int LSM303_REGISTER_ACCEL_CTRL_REG1_A = 0x20; // 00000111   rw
	public final static int LSM303_REGISTER_ACCEL_CTRL_REG4_A = 0x23; // 00000000   rw
	public final static int LSM303_REGISTER_ACCEL_OUT_X_L_A   = 0x28;
	public final static int LSM303_REGISTER_MAG_CRB_REG_M     = 0x01;
	public final static int LSM303_REGISTER_MAG_MR_REG_M      = 0x02;
	public final static int LSM303_REGISTER_MAG_OUT_X_H_M     = 0x03;

	// Gain settings for setMagGain()
	public final static int LSM303_MAGGAIN_1_3 = 0x20; // +/- 1.3
	public final static int LSM303_MAGGAIN_1_9 = 0x40; // +/- 1.9
	public final static int LSM303_MAGGAIN_2_5 = 0x60; // +/- 2.5
	public final static int LSM303_MAGGAIN_4_0 = 0x80; // +/- 4.0
	public final static int LSM303_MAGGAIN_4_7 = 0xA0; // +/- 4.7
	public final static int LSM303_MAGGAIN_5_6 = 0xC0; // +/- 5.6
	public final static int LSM303_MAGGAIN_8_1 = 0xE0; // +/- 8.1

	private final static float _lsm303Accel_MG_LSB = 0.001F; // 1, 2, 4 or 12 mg per lsb
	private static float _lsm303Mag_Gauss_LSB_XY = 1100.0F;  // Varies with gain
	private static float _lsm303Mag_Gauss_LSB_Z  =  980.0F;  // Varies with gain

	private float SENSORS_GRAVITY_EARTH = 9.80665f;        // < Earth's gravity in m/s^2
	private float SENSORS_GRAVITY_MOON = 1.6f;             // < The moon's gravity in m/s^2
	private float SENSORS_GRAVITY_SUN = 275.0f;            // < The sun's gravity in m/s^2
	private float SENSORS_GRAVITY_STANDARD = SENSORS_GRAVITY_EARTH;
	private float SENSORS_MAGFIELD_EARTH_MAX = 60.0f;      // < Maximum magnetic field on Earth's surface
	private float SENSORS_MAGFIELD_EARTH_MIN = 30.0f;      // < Minimum magnetic field on Earth's surface
	private float SENSORS_PRESSURE_SEALEVELHPA = 1013.25f; // < Average sea level pressure is 1013.25 hPa
	private float SENSORS_DPS_TO_RADS = 0.017453293f;      // < Degrees/s to rad/s multiplier
	private float SENSORS_GAUSS_TO_MICROTESLA = 100;       // < Gauss to micro-Tesla multiplier

	private I2CBus bus;
	private I2CDevice accelerometer, magnetometer;
	private byte[] accelData, magData;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
	private static boolean verboseRaw = "true".equals(System.getProperty("lsm303.verbose.raw", "false"));

	private static boolean verboseAcc = "true".equals(System.getProperty("lsm303.verbose.acc", "false"));
	private static boolean verboseMag = "true".equals(System.getProperty("lsm303.verbose.mag", "false"));


	private double pitch = 0D, roll = 0D, heading = 0D;

	private long wait = 1_000L;
	private LSM303Listener dataListener = null;

	private void setMagGain(int gain) throws IOException {
		magnetometer.write(LSM303_REGISTER_MAG_CRB_REG_M, (byte) gain);

		switch (gain) {
			case LSM303_MAGGAIN_1_3:
				_lsm303Mag_Gauss_LSB_XY = 1100F;
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
		if (verbose) {
			System.out.println("Starting sensors reading:");
		}
//		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPI version
			if (verbose)
				System.out.println("Connected to bus. OK.");

			// Get device itself
			accelerometer = bus.getDevice(LSM303_ADDRESS_ACCEL);
			magnetometer = bus.getDevice(LSM303_ADDRESS_MAG);
			if (verbose)
				System.out.println("Connected to devices. OK.");

      /*
       * Start sensing
       */
			// Enable accelerometer
			accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG1_A, (byte) 0x27); // 00100111
			accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG4_A, (byte) 0x00); // Low Res. For Hi Res, write 0b00001000, 0x08
			if (verbose)
				System.out.println("Accelerometer OK.");

			// Enable magnetometer
			magnetometer.write(LSM303_REGISTER_MAG_MR_REG_M, (byte) 0x00);

			int gain = LSM303_MAGGAIN_1_3;
			setMagGain(gain);
			if (verbose)
				System.out.println("Magnetometer OK.");

			startReading();
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//		}
	}

	public void setDataListener(LSM303Listener dataListener) {
		this.dataListener = dataListener;
	}

	// Create a separate thread to read the sensors
	public void startReading() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					readingSensors();
				} catch (IOException ioe) {
					System.err.println("Reading thread:");
					ioe.printStackTrace();
				}
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
		return  this.roll;
	}
	public double getHeading() {
		return  this.heading;
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
			accelData = new byte[6];
			magData = new byte[6];

			accelerometer.write((byte)(LSM303_REGISTER_ACCEL_OUT_X_L_A | 0x80));

			int r = accelerometer.read(accelData, 0, 6);
			if (r != 6) {
				System.out.println("Error reading accel data, < 6 bytes");
			}
			// raw Acc data
			int accelX = accel12(accelData, 0);
			int accelY = accel12(accelData, 2);
			int accelZ = accel12(accelData, 4);

			if (verboseAcc) {
				System.out.println(String.format("Raw(int)Acc XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X)", accelX, accelY, accelZ, accelX & 0xFFFF, accelY & 0xFFFF, accelZ & 0xFFFF));
			}

			float accX = (float) accelX * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;
			float accY = (float) accelY * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;
			float accZ = (float) accelZ * _lsm303Accel_MG_LSB * SENSORS_GRAVITY_STANDARD;

			/*
				pitch = atan (x / sqrt(y^2 + z^2));
				roll  = atan (y / sqrt(x^2 + z^2));
			 */
			double pitchDegrees = Math.toDegrees(Math.atan(accX / Math.sqrt((accY * accY) + (accZ * accZ))));
			double rollDegrees  = Math.toDegrees(Math.atan(accY / Math.sqrt((accX * accX) + (accZ * accZ))));

			setPitch(pitchDegrees); // TODO make sure the range is [-180..180]
			setRoll(rollDegrees);   // TODO make sure the range is [-180..180]

			if (verboseAcc) {
				System.out.println("Pitch & Roll with Accelerometer:");
				System.out.println(String.format("\tX:%f, Y:%f, Z:%f", accX, accY, accZ));
				System.out.println(String.format("\tPitch:%f, Roll:%f", pitchDegrees, rollDegrees));
			}

			// Request magnetometer measurements.
			magnetometer.write((byte)LSM303_REGISTER_MAG_OUT_X_H_M);
			// Reading magnetometer measurements.
			r = magnetometer.read(magData, 0, 6);
			if (r != 6) {
				System.out.println("Error reading mag data, < 6 bytes");
			} else if (verboseMag) {
				dumpBytes(magData, 6);
			}
			// Mag raw data. !!! Warning !!! Order here is X, Z, Y
			int magX = mag16(magData, 0);
			int magZ = mag16(magData, 2); // Yes, Z
			int magY = mag16(magData, 4); // Then Y

//		float magneticX = (float) magX / _lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA;
//		float magneticY = (float) magY / _lsm303Mag_Gauss_LSB_XY * SENSORS_GAUSS_TO_MICROTESLA;
//		float magneticZ = (float) magZ / _lsm303Mag_Gauss_LSB_Z * SENSORS_GAUSS_TO_MICROTESLA;
//		float heading = - (float) Math.toDegrees(Math.atan2(magneticY, magneticX)); // Same as below (the ratio remains the same).
			float heading = (float) Math.toDegrees(Math.atan2((double)magY, (double)magX));
			while (heading < 0) heading += 360f;

			setHeading(heading);

			if (verboseMag) {
				System.out.println(String.format("Raw(int)Mag XYZ %d %d %d (0x%04X, 0x%04X, 0x%04X), HDG:%f", magX, magY, magZ, magX & 0xFFFF, magY & 0xFFFF, magZ & 0xFFFF, heading));
			}

			if (verboseRaw) {
				System.out.println(String.format("RawAcc (XYZ) (%d, %d, %d)\tRawMag (XYZ) (%d, %d, %d)", accelX, accelY, accelZ, magX, magY, magZ));
			}

			if (dataListener != null) {
				// Use the values as you want here.
				dataListener.dataDetected(accX, accY, accZ, magX, magY, magZ, heading, (float)pitchDegrees, (float)rollDegrees);
			} else {
				if (verbose) {
					System.out.println(String.format("heading: %s (mag), pitch: %s, roll: %s",
									Z_FMT.format(heading),
									Z_FMT.format(pitch),
									Z_FMT.format(roll)));
				}
			}
			try {
				Thread.sleep(this.wait);
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
	}

	private static int accel12(byte[] list, int idx) {
		int n = (list[idx] & 0xFF) | ((list[idx + 1] & 0xFF) << 8); // Low, high bytes
		if (n > 32767) n -= 65536;              // 2's complement signed
		return n >> 4;                          // 12-bit resolution
	}

	private static int mag16(byte[] list, int idx) {
		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF);   // High, low bytes
		return (n < 32768 ? n : n - 65536);                           // 2's complement signed
	}

	private static void dumpBytes(byte[] ba, int len) {
		String str = String.format("%d bytes: ", len);
		for (int i=0; i<len; i++) {
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
		LSM303 sensor = new LSM303();

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
