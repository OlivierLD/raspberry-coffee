package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import i2c.sensor.utils.EndianReaders;
import java.io.IOException;

/**
 * I2C Time of Flight distance sensor, 30 to 1000 mm.
 * https://www.adafruit.com/products/3317
 *
 * Adapted from https://github.com/johnbryanmoore/VL53L0X_rasp_python/blob/master/python_lib/vl53l0x_python.c
 *
 * See also https://github.com/pololu/vl53l0x-arduino
 *
 * Basic implementation
 *
 * @deprecated See {@link VL53L0X} instead
 */
public class VL53L0X_v1 {
	public final static int VL53L0X_I2CADDR = 0x29;

	private final static int VL53L0X_REG_IDENTIFICATION_MODEL_ID = 0x00c0;
	private final static int VL53L0X_REG_IDENTIFICATION_REVISION_ID = 0x00c2;
	private final static int VL53L0X_REG_PRE_RANGE_CONFIG_VCSEL_PERIOD = 0x0050;
	private final static int VL53L0X_REG_FINAL_RANGE_CONFIG_VCSEL_PERIOD = 0x0070;
	private final static int VL53L0X_REG_SYSRANGE_START = 0x000;

	private final static int VL53L0X_REG_RESULT_INTERRUPT_STATUS = 0x0013;
	private final static int VL53L0X_REG_RESULT_RANGE_STATUS = 0x0014;

	public final static int VL53L0X_GOOD_ACCURACY_MODE      = 0;   // Good Accuracy mode
	public final static int VL53L0X_BETTER_ACCURACY_MODE    = 1;   // Better Accuracy mode
	public final static int VL53L0X_BEST_ACCURACY_MODE      = 2;   // Best Accuracy mode
	public final static int VL53L0X_LONG_RANGE_MODE         = 3;   // Longe Range mode
	public final static int VL53L0X_HIGH_SPEED_MODE         = 4;   // High Speed mode

	private static boolean verbose = "true".equals(System.getProperty("vl53l0x.debug", "false"));

	private I2CBus bus;
	private I2CDevice vl53l0x;

	public VL53L0X_v1() throws I2CFactory.UnsupportedBusNumberException {
		this(VL53L0X_I2CADDR);
	}

	public VL53L0X_v1(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			vl53l0x = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int getRevision() {
		int revision = 0;
		try {
			revision = readU8(VL53L0X_REG_IDENTIFICATION_REVISION_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return revision;
	}

	public int getDeviceID() {
		int revision = 0;
		try {
			revision = readU8(VL53L0X_REG_IDENTIFICATION_MODEL_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return revision;
	}

	public void startRanging(int mode) throws Exception {
		if (mode >= VL53L0X_GOOD_ACCURACY_MODE && mode <= VL53L0X_HIGH_SPEED_MODE) {
			this.vl53l0x.write(VL53L0X_REG_SYSRANGE_START, (byte)0x01);
			// Waiting for the device to be ready
			final int NB_TRY = 100; // 1 sec max
			boolean ok = false;
			int nb = 0;
			int value = 0;
			while (!ok && nb++ < NB_TRY) {
				try { Thread.sleep(10); } catch (InterruptedException ie) {} // 10ms
				value = readU8(VL53L0X_REG_RESULT_RANGE_STATUS);
				if ((value & 0x01) == 0x01) {
					ok = true;
					if (verbose) {
						System.out.println("Device ready");
					}
				}
			}
			if ((value & 0x01) != 0x01) {
				// Not ready
				if (verbose) {
					System.out.println("Device NOT ready");
				}
				throw new RuntimeException("Device not ready");
			}
		} // TODO else (IllegalPrmException)
	}

	public void stopRanging() {
		// TODO Implement
	}

	public VL53L0XData getVL53L0XData() throws Exception {
		byte[] data = readBlockData(0x14, 12);

		if (verbose) {
			StringBuffer sb = new StringBuffer(); // No way to stream a byte[] ... :(
			for (byte b : data) {
				sb.append(String.format("%02X ", b));
			}
			System.out.println(sb.toString().trim());
		}

		int ambientCount = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
		int signalCount = ((data[8] & 0xFF) << 8) | (data[9] & 0xFF);
		int distance = ((data[10] & 0xFF) << 8) | (data[11] & 0xFF);
		int deviceRangeStatusInternal = ((data[0] & 0x78) >> 3);

		return new VL53L0XData(ambientCount, signalCount, deviceRangeStatusInternal, distance);
	}

	public int getDistance() throws Exception {
		return this.getVL53L0XData().distance;
	}

	private static class VL53L0XData {
		int ambientCount;
		int signalCount;
		int distance;
		int deviceRangeStatusInternal;

		public VL53L0XData(int ambientCount, int signalCount, int deviceRangeStatusInternal, int distance) {
			this.ambientCount = ambientCount;
			this.signalCount = signalCount;
			this.deviceRangeStatusInternal = deviceRangeStatusInternal;
			this.distance = distance;
		}
	}

	private int readU8(int register) throws Exception {
		return EndianReaders.readU8(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readS8(int register) throws Exception {
		return EndianReaders.readS8(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readU16LE(int register) throws Exception {
		return EndianReaders.readU16LE(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readS16LE(int register) throws Exception {
		return EndianReaders.readS16LE(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private byte[] readBlockData(int register, int nb) throws IOException {
		byte[] data = new byte[nb];
		this.vl53l0x.read(register, data, 0, nb);
		return data;
	}

	// For tests
	public static void main(String... args) {

//		int[] spam = new int[] { 0x00, 0x01, 0x03, 0x02 };
//		System.out.println(Arrays.stream(spam)
//						.boxed()
//						.map(b -> String.format("0x%02X", b))
//						.collect(Collectors.joining(" ")));

		try {
			VL53L0X_v1 vl53l0x = new VL53L0X_v1();
			System.out.println(String.format("Revision %d, device ID %d ", vl53l0x.getRevision(), vl53l0x.getDeviceID()));
			System.out.println();
			vl53l0x.startRanging(VL53L0X_v1.VL53L0X_BETTER_ACCURACY_MODE);
			int howMany = 1_000;
			for (int i=0; i<howMany; i++) {
				int distance = vl53l0x.getDistance();
				System.out.println(String.format("Distance:%d mm", distance));
				if (i < (howMany - 2)) {
					try {
						Thread.sleep(10); // 10ms
					} catch (InterruptedException ie) {
					}
				}
			}
			System.out.println("Done.");
		} catch (UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
