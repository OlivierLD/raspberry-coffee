package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.StringUtils.lpad;
import static utils.TimeUtil.delay;

/*
 * Humidity, Temperature
 */
public class HTU21DF {
	public final static int HTU21DF_ADDRESS = 0x40;
	// HTU21DF Registers
	public final static int HTU21DF_READTEMP = 0xE3;
	public final static int HTU21DF_READHUM = 0xE5;

	public final static int HTU21DF_READTEMP_NH = 0xF3; // NH = no hold
	public final static int HTU21DF_READHUMI_NH = 0xF5;

	public final static int HTU21DF_WRITEREG = 0xE6;
	public final static int HTU21DF_READREG = 0xE7;
	public final static int HTU21DF_RESET = 0xFE;

	private static boolean verbose = "true".equals(System.getProperty("htu21df.verbose", "false"));

	private I2CBus bus;
	private I2CDevice htu21df;

	public HTU21DF() throws I2CFactory.UnsupportedBusNumberException {
		this(HTU21DF_ADDRESS);
	}

	public HTU21DF(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}

			// Get device itself
			htu21df = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public boolean begin() throws Exception {
		try {
			reset();
		} catch (Exception ex) {
			System.err.println("Reset:" + ex.toString());
		}
		int r = 0;
		try {
			htu21df.write((byte) HTU21DF_READREG);
			r = htu21df.read();
			if (verbose) {
				System.out.println("DBG: Begin: 0x" + lpad(Integer.toHexString(r), 2, "0"));
			}
		} catch (Exception ex) {
			System.err.println("Begin:" + ex.toString());
		}
		return (r == 0x02);
	}

	public void reset() throws Exception {
		//  htu21df.write(HTU21DF_ADDRESS, (byte)HTU21DF_RESET);
		try {
			htu21df.write((byte) HTU21DF_RESET);
			if (verbose) {
				System.out.println("DBG: Reset OK");
			}
		} finally {
			delay(15); // Wait 15ms
		}
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public float readTemperature() throws Exception {
		// Reads the raw temperature from the sensor
		if (verbose) {
			System.out.println("Read Temp: Written 0x" + lpad(Integer.toHexString((HTU21DF_READTEMP & 0xff)), 2, "0"));
		}
		htu21df.write((byte) (HTU21DF_READTEMP)); //  & 0xff));
		delay(50); // Wait 50ms
		byte[] buf = new byte[3];
	  /*int rc  = */
		htu21df.read(buf, 0, 3);
		int msb = buf[0] & 0xFF;
		int lsb = buf[1] & 0xFF;
		int crc = buf[2] & 0xFF;
		int raw = ((msb << 8) + lsb) & 0xFFFC;

		//  while (!Wire.available()) {}

		if (verbose) {
			System.out.println("Temp -> 0x" + lpad(Integer.toHexString(msb), 2, "0") + " " + "0x" +
							lpad(Integer.toHexString(lsb), 2, "0") + " " + "0x" + lpad(Integer.toHexString(crc), 2, "0"));
			System.out.println("DBG: Raw Temp: " + (raw & 0xFFFF) + ", " + raw);
		}

		float temp = raw; // t;
		temp *= 175.72;
		temp /= 65_536;
		temp -= 46.85;

		if (verbose) {
			System.out.println("DBG: Temp: " + temp);
		}
		return temp;
	}

	public float readHumidity() throws Exception {
		// Reads the raw (uncompensated) humidity from the sensor
		htu21df.write((byte) HTU21DF_READHUM);
		delay(50); // Wait 50ms
		byte[] buf = new byte[3];
    /* int rc  = */
		htu21df.read(buf, 0, 3);
		int msb = buf[0] & 0xFF;
		int lsb = buf[1] & 0xFF;
		int crc = buf[2] & 0xFF;
		int raw = ((msb << 8) + lsb) & 0xFFFC;

		//  while (!Wire.available()) {}

		if (verbose) {
			System.out.println("Hum -> 0x" + lpad(Integer.toHexString(msb), 2, "0") + " " + "0x" +
							lpad(Integer.toHexString(lsb), 2, "0") + " " + "0x" + lpad(Integer.toHexString(crc), 2, "0"));
			System.out.println("DBG: Raw Humidity: " + (raw & 0xFFFF) + ", " + raw);
		}

		float hum = raw;
		hum *= 125;
		hum /= 65_536;
		hum -= 6;

		if (verbose) {
			System.out.println("DBG: Humidity: " + hum);
		}
		return hum;
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final NumberFormat NF = new DecimalFormat("##00.00");
		HTU21DF sensor = new HTU21DF();
		float hum = 0;
		float temp = 0;

		try {
			if (!sensor.begin()) {
				System.out.println("Sensor not found!");
				System.exit(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		try {
			hum = sensor.readHumidity();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}

		try {
			temp = sensor.readTemperature();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}

		System.out.println("Temperature: " + NF.format(temp) + " C");
		System.out.println("Humidity   : " + NF.format(hum) + " %");
	}
}
