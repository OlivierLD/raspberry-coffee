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

	private final static int HMC5883L_CONTINUOUS_SAMPLING = 0x00;
	private final static int HMC5883L_SELECT_MODE         = 0x02;

	private final static int HMC5883L_X_ADR_0 = 0x03;
	private final static int HMC5883L_X_ADR_1 = 0x04;
	private final static int HMC5883L_Y_ADR_0 = 0x07;
	private final static int HMC5883L_Y_ADR_1 = 0x08;
	private final static int HMC5883L_Z_ADR_0 = 0x05;
	private final static int HMC5883L_Z_ADR_1 = 0x06;

	private final static float SCALE = 0.00092F;

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

	/**
	 * @return Heading in Radians - MAGNETIC Heading.
	 * @throws IOException
	 */
	public double readHeading() throws IOException {

		hcm5883l.write((byte)HMC5883L_X_ADR_1);
		int low = hcm5883l.read();
		hcm5883l.write((byte)HMC5883L_X_ADR_0);
		int high = hcm5883l.read();

		int x = (short)((low & 0xFF) | ((high & 0xFF) << 8));

		hcm5883l.write((byte)HMC5883L_Y_ADR_1);
		low = hcm5883l.read();
		hcm5883l.write((byte)HMC5883L_Y_ADR_0);
		high = hcm5883l.read();

		int y = (short)((low & 0xFF) | ((high & 0xFF) << 8));

		hcm5883l.write((byte)HMC5883L_Z_ADR_1);
		low = hcm5883l.read();
		hcm5883l.write((byte)HMC5883L_Z_ADR_0);
		high = hcm5883l.read();

		int z = (short)((low & 0xFF) | ((high & 0xFF) << 8));


		double xOut = x * SCALE;
		double yOut = y * SCALE;
		double zOut = z * SCALE;

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

		double heading = Math.atan2(yOut, xOut);
		if (heading < 0) {
			heading += (2 * Math.PI);
		}
		if (heading > (2 * Math.PI)) {
			heading -= (2 * Math.PI);
		}
		return heading;
	}

	public void init() throws IOException {
		hcm5883l.write((byte)HMC5883L_SELECT_MODE);
		hcm5883l.write((byte)HMC5883L_CONTINUOUS_SAMPLING);
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

	public static void main(String... args)
			throws I2CFactory.UnsupportedBusNumberException,
						 IOException {
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

		sensor.init();

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
