package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.TimeUtil.delay;

/*
 * 3 Axis compass
 */
public class HMC5883L {
	private final static int HMC5883L_ADDRESS = 0x1E;

	private final static int HMC5883L_CONTINUOUS_SAMPLING    = 0x00;
	private final static int HMC5883L_13_GAIN_LSB_GAUSS_1090 = 0x20; // 1.3 gain LSb / Gauss 1090 (default)

	private final static int HMC5883_MAGGAIN_1_9                        = 0x40;  // +/- 1.9
	private final static int HMC5883_MAGGAIN_2_5                        = 0x60;  // +/- 2.5
	private final static int HMC5883_MAGGAIN_4_0                        = 0x80;  // +/- 4.0
	private final static int HMC5883_MAGGAIN_4_7                        = 0xA0;  // +/- 4.7
	private final static int HMC5883_MAGGAIN_5_6                        = 0xC0;  // +/- 5.6
	private final static int HMC5883_MAGGAIN_8_1                        = 0xE0;  // +/- 8.1

	private final static int HMC5883L_8_SAMPLES_15HZ         = 0x70; // Set to 8 samples @ 15Hz.

	private final static int HMC5883L_X_ADR = 0x03; // 0x03 & 0x04
	private final static int HMC5883L_Z_ADR = 0x05; // 0x05 & 0x06
	private final static int HMC5883L_Y_ADR = 0x07; // 0x07 & 0x08

	private final static float SCALE = 0.92f;

	private static float _hmc5883_Gauss_LSB_XY = 1100.0F;  // Varies with gain
	private static float _hmc5883_Gauss_LSB_Z  = 980.0F;   // Varies with gain


	private static boolean verbose = "true".equals(System.getProperty("hmc5883l.verbose", "false"));

	private I2CBus bus;
	private I2CDevice hcm5883l;

	public HMC5883L() throws I2CFactory.UnsupportedBusNumberException {
		this(HMC5883L_ADDRESS);
	}

	public HMC5883L(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPI version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			hcm5883l = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Complement to 2
	private short readWord_2C(int reg) throws IOException {
		short w = 0;

		byte high = (byte) (hcm5883l.read(reg) & 0xFF);
		byte low = (byte) (hcm5883l.read(reg + 1) & 0xFF);

		w = (short) ((((high & 0xFF) << 8) | (low & 0xFF)) & 0xFFFF); // Little endian

		if (w >= 0x8000) {
			w = (short) -((0xFFFF - w) + 1);
		}

		if (verbose) {
			System.out.println(String.format("ReadWord: 0x%s << 8 | 0x%s => 0x%s, dec: %d",
							Integer.toHexString(high & 0xFF).toUpperCase(),
							Integer.toHexString(low & 0xFF).toUpperCase(),
							Integer.toHexString(w).toUpperCase(),
							w));
		}
		return w;
	}

	/**
	 * @return Heading in Radians
	 * @throws IOException
	 */
	public double readHeading() throws IOException {
		double heading = 0f;
		if (true) {
			byte[] w = new byte[]{
				(byte) HMC5883L_8_SAMPLES_15HZ,
				(byte) HMC5883L_13_GAIN_LSB_GAUSS_1090,
				(byte) HMC5883L_CONTINUOUS_SAMPLING
			};
			hcm5883l.write(w, 0, 3); // BeginTrans, write 3 bytes, EndTrans.
		} else {
			hcm5883l.write((byte) 0x03);
		}

		double xOut = readWord_2C(HMC5883L_X_ADR);
		double yOut = readWord_2C(HMC5883L_Y_ADR);
		double zOut = readWord_2C(HMC5883L_Z_ADR);

//		xOut *= SCALE;
//		yOut *= SCALE;
//		zOut *= SCALE;

		xOut /= _hmc5883_Gauss_LSB_XY;
		yOut /= _hmc5883_Gauss_LSB_XY;
		zOut /= _hmc5883_Gauss_LSB_Z;

		if (verbose) {
			System.out.println("xOut:" + xOut);
			System.out.println("yOut:" + yOut);
			System.out.println("zOut:" + zOut);
		}

		if (verbose) {
			System.out.println(String.format("Heading: %f", Math.toDegrees(Math.atan2(yOut, xOut))));
			System.out.println(String.format("Pitch  : %f", Math.toDegrees(Math.atan2(yOut, zOut))));
			System.out.println(String.format("Roll   : %f", Math.toDegrees(Math.atan2(xOut, zOut))));
		}

		heading = Math.atan2(yOut, xOut);
		if (heading < 0) {
			heading += (2 * Math.PI);
		}
		return heading;
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static boolean go = true;
	private static void setGo(boolean b) {
		go = b;
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final NumberFormat NF = new DecimalFormat("##00.00");
		HMC5883L sensor = new HMC5883L();
		double hdg = 0;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (sensor) {
				setGo(false);
				sensor.close();
				delay(1_000L);
			}
		}));

		while (go) {
			try {
				hdg = sensor.readHeading();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
			System.out.println(String.format("%d >> Heading: %s deg.", System.currentTimeMillis(), NF.format(Math.toDegrees(hdg))));
			delay(500);
		}
		System.out.println("Bye.");
	}
}
