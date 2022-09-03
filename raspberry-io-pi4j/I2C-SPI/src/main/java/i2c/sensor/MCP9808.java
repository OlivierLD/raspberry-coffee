package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.SystemInfo;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.TimeUtil.delay;
import static utils.StringUtils.lpad;

/*
 * Temperature
 */
public class MCP9808 {
	// This next addresses is returned by "sudo i2cdetect -y 1".
	private final static int MCP9808_I2CADDR_DEFAULT = 0x18;
	// Registers
	private final static int MCP9808_REG_CONFIG = 0x01;
	private final static int MCP9808_REG_UPPER_TEMP = 0x02;
	private final static int MCP9808_REG_LOWER_TEMP = 0x03;
	private final static int MCP9808_REG_CRIT_TEMP = 0x04;
	private final static int MCP9808_REG_AMBIENT_TEMP = 0x05;
	private final static int MCP9808_REG_MANUF_ID = 0x06;
	private final static int MCP9808_REG_DEVICE_ID = 0x07;

	// Configuration register values.
	private final static int MCP9808_REG_CONFIG_SHUTDOWN = 0x0100;
	private final static int MCP9808_REG_CONFIG_CRITLOCKED = 0x0080;
	private final static int MCP9808_REG_CONFIG_WINLOCKED = 0x0040;
	private final static int MCP9808_REG_CONFIG_INTCLR = 0x0020;
	private final static int MCP9808_REG_CONFIG_ALERTSTAT = 0x0010;
	private final static int MCP9808_REG_CONFIG_ALERTCTRL = 0x0008;
	private final static int MCP9808_REG_CONFIG_ALERTSEL = 0x0002;
	private final static int MCP9808_REG_CONFIG_ALERTPOL = 0x0002;
	private final static int MCP9808_REG_CONFIG_ALERTMODE = 0x0001;

	private final static boolean verbose = false;

	private I2CBus bus;
	private I2CDevice mcp9808;

	public MCP9808() throws I2CFactory.UnsupportedBusNumberException {
		this(MCP9808_I2CADDR_DEFAULT);
	}

	public MCP9808(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}

			// Get device itself
			mcp9808 = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public int readU16BE(int register) throws Exception {
		final int TWO = 2;
		byte[] bb = new byte[TWO];
		int nbr = this.mcp9808.read(register, bb, 0, TWO);
		if (nbr != TWO) {
			throw new Exception("Cannot read 2 bytes from " + lpad(Integer.toHexString(register), 2, "0"));
		}
		if (verbose) {
			System.out.println("I2C: 0x" + lpad(Integer.toHexString(bb[0]), 2, "0") + lpad(Integer.toHexString(bb[1]), 2, "0"));
		}
		return ((bb[0] & 0xFF) << 8) + (bb[1] & 0xFF);
	}

	private boolean init() throws Exception {
		int mid = 0, did = 0;
		try {
			mid = readU16BE(MCP9808_REG_MANUF_ID);
			did = readU16BE(MCP9808_REG_DEVICE_ID);
		} catch (Exception e) {
			throw e;
		}
		if (verbose) {
			System.out.println("I2C: MID 0x" + lpad(Integer.toHexString(mid), 4, "0") + " (expected 0x0054)" +
					" DID 0x" + lpad(Integer.toHexString(did), 4, "0") + " (expected 0x0400)");
		}
		return (mid == 0x0054 && did == 0x0400);
	}

	public float readCelciusTemp() throws Exception {
		int raw = readU16BE(MCP9808_REG_AMBIENT_TEMP);
		float temp = raw & 0x0FFF;
		temp /= 16.0;
		if ((raw & 0x1000) != 0x0) {
			temp -= 256;
		}
		if (verbose) {
			System.out.println("DBG: C Temp: " + lpad(Integer.toHexString(raw & 0xFFFF), 4, "0") + ", " + temp);
		}
		return temp;
	}

	private final static NumberFormat NF = new DecimalFormat("##00.000");

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		System.out.println("MCP9808 Demo");
		MCP9808 sensor = new MCP9808();
		try {
			boolean ok = sensor.init();
			if (!ok) {
				System.out.println("Warning, init failed. Expect weird results...");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		for (int i = 0; i < 10; i++) {
			float temp = 0;
			try {
				temp = sensor.readCelciusTemp();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
			}
			System.out.println("Temperature: " + NF.format(temp) + " C");
			delay(1_000);
		}
		// Bonus : CPU Temperature
		try {
			System.out.println("CPU Temperature   :  " + SystemInfo.getCpuTemperature());
			System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
