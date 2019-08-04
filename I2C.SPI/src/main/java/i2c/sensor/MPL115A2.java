package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.TimeUtil.delay;

/*
 * Pressure, Temperature
 */
public class MPL115A2 {
	public final static int MPL115A2_ADDRESS = 0x60;    // 1100000

	public final static int MPL115A2_REGISTER_PRESSURE_MSB = 0x00;
	public final static int MPL115A2_REGISTER_PRESSURE_LSB = 0x01;
	public final static int MPL115A2_REGISTER_TEMP_MSB = 0x02;
	public final static int MPL115A2_REGISTER_TEMP_LSB = 0x03;
	public final static int MPL115A2_REGISTER_A0_COEFF_MSB = 0x04;
	public final static int MPL115A2_REGISTER_A0_COEFF_LSB = 0x05;
	public final static int MPL115A2_REGISTER_B1_COEFF_MSB = 0x06;
	public final static int MPL115A2_REGISTER_B1_COEFF_LSB = 0x07;
	public final static int MPL115A2_REGISTER_B2_COEFF_MSB = 0x08;
	public final static int MPL115A2_REGISTER_B2_COEFF_LSB = 0x09;
	public final static int MPL115A2_REGISTER_C12_COEFF_MSB = 0x0A;
	public final static int MPL115A2_REGISTER_C12_COEFF_LSB = 0x0B;
	public final static int MPL115A2_REGISTER_STARTCONVERSION = 0x12;

	private static boolean verbose = "true".equals(System.getProperty("mpl115a2.verbose", "false"));

	private I2CBus bus;
	private I2CDevice mpl115a2;

	private float _mpl115a2_a0 = 0f;
	private float _mpl115a2_b1 = 0f;
	private float _mpl115a2_b2 = 0f;
	private float _mpl115a2_c12 = 0f;

	public MPL115A2() throws I2CFactory.UnsupportedBusNumberException {
		this(MPL115A2_ADDRESS);
	}

	public MPL115A2(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			mpl115a2 = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void begin()
			throws Exception {
		readCoefficients();
	}

	private void readCoefficients() throws IOException {
		try {
			mpl115a2.write((byte) MPL115A2_REGISTER_A0_COEFF_MSB);

			byte[] buf = new byte[8];
			mpl115a2.read(buf, 0, 8);

			if (verbose) {
				for (int i = 0; i < buf.length; i++) {
					System.out.println(Integer.toString(i) + " : 0x" + Integer.toHexString(buf[i]));
				}
			}

			int a0coeff = (buf[0] << 8) | (buf[1] & 0xFF);
			int b1coeff = (buf[2] << 8) | (buf[3] & 0xFF);
			int b2coeff = (buf[4] << 8) | (buf[5] & 0xFF);
			int c12coeff = ((buf[6] << 8) | (buf[7] & 0xFF)) >> 2;

			_mpl115a2_a0 = (float) a0coeff / 8;
			_mpl115a2_b1 = (float) b1coeff / 8192;
			_mpl115a2_b2 = (float) b2coeff / 16384;
			_mpl115a2_c12 = (float) c12coeff / 4194304.0f;

			if (verbose) {
				System.out.println("A0  = 0x" + Integer.toString((a0coeff & 0xFFFF), 16).toUpperCase());
				System.out.println("B1  = 0x" + Integer.toString((b1coeff & 0xFFFF), 16).toUpperCase());
				System.out.println("B2  = 0x" + Integer.toString((b2coeff & 0xFFFF), 16).toUpperCase());
				System.out.println("C12 = 0x" + Integer.toString((c12coeff & 0xFFFF), 16).toUpperCase());
				System.out.println();
				System.out.println("a0  = " + Float.toString(_mpl115a2_a0));
				System.out.println("b1  = " + Float.toString(_mpl115a2_b1));
				System.out.println("b2  = " + Float.toString(_mpl115a2_b2));
				System.out.println("c12 = " + Float.toString(_mpl115a2_c12));
				System.out.println("------------------------------------");
			}
		} catch (IOException ioe) {
			throw ioe;
		}
	}

	public final static int PRESSURE_IDX = 0;
	public final static int TEMPERATURE_IDX = 1;

	public float[] measure() throws IOException {
		float[] result = new float[]{0f, 0f};

		byte[] w = new byte[]{(byte) MPL115A2_REGISTER_STARTCONVERSION, (byte) 0x00};
		mpl115a2.write(w, 0, 2); // BeginTrans, write 2 bytes, EndTrans.

		delay(5);  // Wait a bit for the conversion to complete (takes 3ms max)

		mpl115a2.write((byte) MPL115A2_REGISTER_PRESSURE_MSB);

		byte[] buf = new byte[4];
		mpl115a2.read(buf, 0, 4);

		if (verbose) {
			for (int i = 0; i < buf.length; i++) {
				System.out.println("Data: " + Integer.toString(i) + " : 0x" + Integer.toHexString(buf[i] & 0xFF));
			}
		}

		int pressure = (((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF)) >> 6;
		int temperature = (((buf[2] & 0xFF) << 8) | (buf[3] & 0xFF)) >> 6;

		float pressureComp = _mpl115a2_a0 + (_mpl115a2_b1 + _mpl115a2_c12 * temperature) * pressure + _mpl115a2_b2 * temperature;

		if (verbose) {
			System.out.println("Raw pressure   :" + pressure);
			System.out.println("Raw temperature:" + temperature);
			System.out.println("pressureComp   :" + pressureComp);
		}

		result[PRESSURE_IDX] = ((65.0F / 1023.0F) * pressureComp) + 50.0F;  // kPa
		result[TEMPERATURE_IDX] = (temperature - 498.0F) / -5.35F + 25.0F;     // C

		return result;
	}

	public float readTemperature()
			throws Exception {
		float temp = measure()[TEMPERATURE_IDX];
		return temp;
	}

	public float readPressure()
			throws Exception {
		float press = measure()[PRESSURE_IDX];
		return press;
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final NumberFormat NF = new DecimalFormat("##00.00");
		MPL115A2 sensor = new MPL115A2();
		float press = 0;
		float temp = 0;

		try {
			sensor.begin();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		final int NB_LOOP = 1;
		for (int i = 0; i < NB_LOOP; i++) {
			try {
				temp = sensor.readTemperature();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}

			try {
				press = sensor.readPressure();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}

			System.out.println("Temperature: " + NF.format(temp) + " C");
			System.out.println("Pressure   : " + NF.format(press * 10) + " hPa");

			delay(1_000);
		}
		sensor.close();
	}
}
