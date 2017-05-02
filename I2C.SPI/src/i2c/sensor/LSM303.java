package i2c.sensor;

import i2c.sensor.listener.LSM303Listener;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import com.pi4j.system.SystemInfo;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * Accelerometer + Magnetometer
 * (Compass & Gyro)
 */
public class LSM303 {
	// Minimal constants carried over from Arduino library
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
	public final static int LSM303_ADDRESS_MAG = (0x3C >> 1); // 0011110x, 0x1E
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

	private I2CBus bus;
	private I2CDevice accelerometer, magnetometer;
	private byte[] accelData, magData;

	private final static NumberFormat Z_FMT = new DecimalFormat("000");
	private static boolean verbose = false;

	private long wait = 1000L;
	private LSM303Listener dataListener = null;

	public LSM303() throws I2CFactory.UnsupportedBusNumberException {
		if (verbose) {
			System.out.println("Starting sensors reading:");
		}
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
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
			accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG4_A, (byte) 0x00);
			if (verbose)
				System.out.println("Accelerometer OK.");

			// Enable magnetometer
			magnetometer.write(LSM303_REGISTER_MAG_MR_REG_M, (byte) 0x00);
			int gain = LSM303_MAGGAIN_1_3;
			magnetometer.write(LSM303_REGISTER_MAG_CRB_REG_M, (byte) gain);
			if (verbose)
				System.out.println("Magnetometer OK.");

			startReading();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
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

			int r = accelerometer.read(LSM303_REGISTER_ACCEL_OUT_X_L_A | 0x80, accelData, 0, 6);
			if (r != 6) {
				System.out.println("Error reading accel data, < 6 bytes");
			}
			int accelX = accel12(accelData, 0);
			int accelY = accel12(accelData, 2);
			int accelZ = accel12(accelData, 4);

			// Reading magnetometer measurements.
			r = magnetometer.read(LSM303_REGISTER_MAG_OUT_X_H_M, magData, 0, 6);
			if (r != 6) {
				System.out.println("Error reading mag data, < 6 bytes");
			}

			int magX = mag16(magData, 0);
			int magY = mag16(magData, 2);
			int magZ = mag16(magData, 4);

			float heading = (float) Math.toDegrees(Math.atan2(magY, magX));
			while (heading < 0)
				heading += 360f;
			float pitch = (float) Math.toDegrees(Math.atan2(magX, magZ)); // TODO -180, 180
			float roll = (float) Math.toDegrees(Math.atan2(magY, magZ)); // TODO -180, 180


			// Bonus : CPU Temperature
			float cpuTemp = Float.MIN_VALUE;
			float cpuVoltage = Float.MIN_VALUE;
			try {
				cpuTemp = SystemInfo.getCpuTemperature();
				cpuVoltage = SystemInfo.getCpuVoltage();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (dataListener != null)
				dataListener.dataDetected(accelX, accelY, accelZ, magX, magY, magZ, heading);
			else {
				System.out.println("accel (X: " + accelX +
								", Y: " + accelY +
								", Z: " + accelZ +
								") mag (X: " + magX +
								", Y: " + magY +
								", Z: " + magZ +
								", heading: " + Z_FMT.format(heading) +
								", pitch: " + Z_FMT.format(pitch) +
								", roll: " + Z_FMT.format(roll) + ")" +
								(cpuTemp != Float.MIN_VALUE ? " Cpu Temp:" + cpuTemp : "") +
								(cpuVoltage != Float.MIN_VALUE ? " Cpu Volt:" + cpuVoltage : ""));
			}
			//Use the values as you want
			// ...
			try {
				Thread.sleep(this.wait);
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}
		}
	}

	private static int accel12(byte[] list, int idx) {
		int n = (list[idx] & 0xFF) | ((list[idx + 1] & 0xFF) << 8); // Low, high bytes
		if (n > 32767)
			n -= 65536;                           // 2's complement signed
		return n >> 4;                          // 12-bit resolution
	}

	private static int mag16(byte[] list, int idx) {
		int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF);   // High, low bytes
		return (n < 32768 ? n : n - 65536);       // 2's complement signed
	}

	public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException {
		LSM303 sensor = new LSM303();
		sensor.startReading();
	}
}
