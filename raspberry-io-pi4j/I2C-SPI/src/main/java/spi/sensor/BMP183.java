package spi.sensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This one has an SPI interface (not I2C)
 * https://www.adafruit.com/product/1900
 *
 * BMP183 SPI Barometric Pressure & Altitude Sensor
 *
 * Wiring: https://learn.adafruit.com/adafruit-bmp183-spi-barometric-pressure-and-altitude-sensor/wiring-and-test
 *
 * Connect Vin to the power supply, 3V or 5V is fine. Use the same voltage that the microcontroller logic is based off of. For most Arduinos, that is 5V
 *
 * GND : Ground
 * SCK : Clock
 * SDO : Dout -> MISO
 * SDI : Din  -> MOSI
 * CS  : Chip Select
 */
public class BMP183 {
	private final static boolean verbose = false;
	private static GpioController gpio;

	private static GpioPinDigitalInput misoInput = null;
	private static GpioPinDigitalOutput mosiOutput = null;
	private static GpioPinDigitalOutput clockOutput = null;
	private static GpioPinDigitalOutput chipSelectOutput = null;

	public final static class BMP183_REG {
		public final static int CAL_AC1 = 0xAA;
		public final static int CAL_AC2 = 0xAC;
		public final static int CAL_AC3 = 0xAE;
		public final static int CAL_AC4 = 0xB0;
		public final static int CAL_AC5 = 0xB2;
		public final static int CAL_AC6 = 0xB4;
		public final static int CAL_B1 = 0xB6;
		public final static int CAL_B2 = 0xB8;
		public final static int CAL_MB = 0xBA;
		public final static int CAL_MC = 0xBC;
		public final static int CAL_MD = 0xBE;

		// Chip ID. Value fixed to 0x55. Useful to check if communication works
		public final static int ID = 0xD0;
		public final static int ID_VALUE = 0x55;

		// VER Undocumented
		public final static int VER = 0xD1;

		// SOFT_RESET Write only. If set to 0xB6, will perform the same sequence as power on reset.
		public final static int SOFT_RESET = 0xE0;

		// CTRL_MEAS Controls measurements
		public final static int CTRL_MEAS = 0xF4;

		// DATA
		public final static int DATA = 0xF6;
	}

	// Commands
	public final static class BMP183_CMD {
		// Chip ID Value fixed to 0x55. Useful to check if communication works
		public final static int ID_VALUE = 0x55;

		// SPI bit to indicate READ or WRITE operation
		public final static int READWRITE = 0x80;

		// Read TEMPERATURE, Wait time 4.5 ms
		public final static int TEMP = 0x2E;
		public final static float TEMP_WAIT = 4.5f;

		// Read PRESSURE
		public final static int PRESS = 0x34; // 001

		// PRESSURE reading modes
		// Example usage: (PRESS | (OVERSAMPLE_2 << 4)
		public final static int OVERSAMPLE_0 = 0x0; // ultra low power, no oversampling, wait time 4.5 ms
		public final static float OVERSAMPLE_0_WAIT = 4.5f;
		public final static int OVERSAMPLE_1 = 0x1; // standard, 2 internal samples, wait time 7.5 ms
		public final static float OVERSAMPLE_1_WAIT = 7.5f;
		public final static int OVERSAMPLE_2 = 0x2; // high resolution, 4 internal samples, wait time 13.5 ms
		public final static float OVERSAMPLE_2_WAIT = 13.5f;
		public final static int OVERSAMPLE_3 = 0x3; // ultra high resolution, 8 internal samples, Wait time 25.5 ms
		public final static float OVERSAMPLE_3_WAIT = 25.5f;
	}

	private int cal_AC1 = 0;
	private int cal_AC2 = 0;
	private int cal_AC3 = 0;
	private int cal_AC4 = 0;
	private int cal_AC5 = 0;
	private int cal_AC6 = 0;
	private int cal_B1 = 0;
	private int cal_B2 = 0;
	private int cal_MB = 0;
	private int cal_MC = 0;
	private int cal_MD = 0;

	private static Pin spiClk = RaspiPin.GPIO_14; // clock (pin #23)
	private static Pin spiMiso = RaspiPin.GPIO_13; // data in.  MISO: Master In Slave Out (pin #21)
	private static Pin spiMosi = RaspiPin.GPIO_12; // data out. MOSI: Master Out Slave In (pin #19)
	private static Pin spiCs = RaspiPin.GPIO_10; // Chip Select (pin #24)

	private double B5 = 0d, B6 = 0d;
	private int UT = 0, UP = 0; // Uncompensated Temp & Press

	private final static float DELAY = 1f / 1_000.0f; // SCK frequency 1 MHz ( 1/1000 ms)

	public BMP183() throws Exception {
		initBMP183();
		// Check communication / read ID
		// int ret = this.readU8(BMP183_REG.ID);
		int ret = readByte(BMP183_REG.ID);
		if (ret != BMP183_CMD.ID_VALUE) {
			System.out.println("BMP183 returned 0x" + Integer.toHexString(ret) + " instead of 0x55. Communication failed, expect problems...");
			shutdownBMP183();
			System.exit(1);
		} else {
			if (verbose) {
				System.out.println("Communication established.");
			}
			readCalibrationData();
		}
	}

	private static void initBMP183() {
		gpio = GpioFactory.getInstance();
		mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);

		misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");
	}

	public static void shutdownBMP183() {
		gpio.shutdown();
	}

	public void readCalibrationData() throws Exception {
		// Reads the calibration data from the IC
		cal_AC1 = mkInt16(readWord(BMP183_REG.CAL_AC1));   //  INT16
		cal_AC2 = mkInt16(readWord(BMP183_REG.CAL_AC2));   //  INT16
		cal_AC3 = mkInt16(readWord(BMP183_REG.CAL_AC3));   //  INT16
		cal_AC4 = mkUInt16(readWord(BMP183_REG.CAL_AC4));  // UINT16
		cal_AC5 = mkUInt16(readWord(BMP183_REG.CAL_AC5));  // UINT16
		cal_AC6 = mkUInt16(readWord(BMP183_REG.CAL_AC6));  // UINT16
		cal_B1 = mkInt16(readWord(BMP183_REG.CAL_B1));    //  INT16
		cal_B2 = mkInt16(readWord(BMP183_REG.CAL_B2));    //  INT16
		cal_MB = mkInt16(readWord(BMP183_REG.CAL_MB));    //  INT16
		cal_MC = mkInt16(readWord(BMP183_REG.CAL_MC));    //  INT16
		cal_MD = mkInt16(readWord(BMP183_REG.CAL_MD));    //  INT16
		if (verbose) {
			showCalibrationData();
		}
	}

	private static int mkInt16(int val) {
		int ret = val & 0x7FFF;
		if (val > 0x7FFF) {
			ret -= 0x8000;
//    if (verbose) {
//      System.out.println(val + " becomes " + ret);
//    }
		}
		return ret;
	}

	private static int mkUInt16(int val) {
		int ret = val & 0xFFFF;
		return ret;
	}

	private void showCalibrationData() {
		// Displays the calibration values for debugging purposes
		System.out.println(">>> DBG: AC1 = " + cal_AC1);
		System.out.println(">>> DBG: AC2 = " + cal_AC2);
		System.out.println(">>> DBG: AC3 = " + cal_AC3);
		System.out.println(">>> DBG: AC4 = " + cal_AC4);
		System.out.println(">>> DBG: AC5 = " + cal_AC5);
		System.out.println(">>> DBG: AC6 = " + cal_AC6);
		System.out.println(">>> DBG: B1  = " + cal_B1);
		System.out.println(">>> DBG: B2  = " + cal_B2);
		System.out.println(">>> DBG: MB  = " + cal_MB);
		System.out.println(">>> DBG: MC  = " + cal_MC);
		System.out.println(">>> DBG: MD  = " + cal_MD);
	}

	private final static int WRITE = 0;
	private final static int READ = 1;

	/**
	 * @param addr   Register
	 * @param value  value to write
	 * @param rw     READ or WRITE
	 * @param length length in bits
	 * @return the expected value.
	 */
	private int spiTransfer(int addr, int value, int rw, int length) {
		// Bit banging at address "addr", "rw" indicates READ (1) or WRITE (0) operation
		int retValue = 0;
		int spiAddr;
		if (rw == WRITE) {
			spiAddr = addr & (~BMP183_CMD.READWRITE);
		} else {
			spiAddr = addr | BMP183_CMD.READWRITE;
		}
		// System.out.println("SPI ADDR: 0x" + Integer.toHexString(spiAddr) + ", mode:" + rw);

		chipSelectOutput.low();
		delay(DELAY);
		for (int i = 0; i < 8; i++) {
			int bit = spiAddr & (0x01 << (7 - i));
			if (bit != 0) {
				mosiOutput.high();
			} else {
				mosiOutput.low();
			}
			clockOutput.low();
			delay(DELAY);
			clockOutput.high();
			delay(DELAY);
		}
		if (rw == READ) {
			for (int i = 0; i < length; i++) {
				clockOutput.low();
				delay(DELAY);
				int bit = misoInput.getState().getValue(); // TODO Check that
				clockOutput.high();
				retValue = (retValue << 1) | bit;
				delay(DELAY);
			}
		}
		if (rw == WRITE) {
			for (int i = 0; i < length; i++) {
				int bit = value & (0x01 << (length - 1 - i));
				if (bit != 0) {
					mosiOutput.high();
				} else {
					mosiOutput.low();
				}
				clockOutput.low();
				delay(DELAY);
				clockOutput.high();
				delay(DELAY);
			}
		}
		chipSelectOutput.high();
		return retValue;
	}

	private int readByte(int addr) {
		int retValue = spiTransfer(addr, 0, READ, 8);
		return retValue;
	}

	private int readWord(int addr) {
		return readWord(addr, 0);
	}

	// Read word from SPI interface from address "addr", option to extend read by up to 3 bits
	private int readWord(int addr, int extraBits) {
		int retValue = spiTransfer(addr, 0, READ, 16 + extraBits);
		return retValue;
	}

	private void writeByte(int addr, int value) {
		spiTransfer(addr, value, WRITE, 8);
	}

	// Start temperature measurement
	public double measureTemperature() {
		writeByte(BMP183_REG.CTRL_MEAS, BMP183_CMD.TEMP);
		delay(BMP183_CMD.TEMP_WAIT);
		// Read uncmpensated temperature
		this.UT = readWord(BMP183_REG.DATA);
		return calculateTemperature();
	}

	// Calculate temperature in [degC]
	private double calculateTemperature() {
		double x1 = (this.UT - this.cal_AC6) * this.cal_AC5 / Math.pow(2, 15);
		double x2 = this.cal_MC * Math.pow(2, 11) / (x1 + this.cal_MD);
		this.B5 = x1 + x2;
		double t = (this.B5 + 8) / Math.pow(2, 4);
		return t / 10d;
	}

	public double measurePressure() {
		// Measure temperature is required for calculations
		double temp = measureTemperature();
		// Read 3 samples of uncompensated pressure
		int[] up = new int[3];
		for (int i = 0; i < 3; i++) {
			writeByte(BMP183_REG.CTRL_MEAS, BMP183_CMD.PRESS | (BMP183_CMD.OVERSAMPLE_3 << 4));
			delay(BMP183_CMD.OVERSAMPLE_3_WAIT);
			up[i] = readWord(BMP183_REG.DATA, 3);
		}
		this.UP = (up[0] + up[1] + up[2]) / 3;
		return calculatePressure();
	}

	private double calculatePressure() {
		this.B6 = this.B5 - 4_000;
		double x1 = (this.cal_B2 * (this.B6 * this.B6 / Math.pow(2, 12))) / Math.pow(2, 11);
		double x2 = this.cal_AC2 * this.B6 / Math.pow(2, 11);
		double x3 = x1 + x2;
		double b3 = ((((this.cal_AC1 * 4 + (int) x3) << BMP183_CMD.OVERSAMPLE_3) + 2) / 4d);
		x1 = this.cal_AC3 * this.B6 / Math.pow(2, 13);
		x2 = (this.cal_B1 * (this.B6 * this.B6 / Math.pow(2, 12))) / Math.pow(2, 16);
		x3 = ((x1 + x2) + 2) / Math.pow(2, 2);
		double b4 = (this.cal_AC4 * ((int) x3 + 32768) / Math.pow(2, 15));
		double b7 = (this.UP - (int) b3) * (50_000 >> BMP183_CMD.OVERSAMPLE_3);
		double p = ((b7 * 2) / b4);
		x1 = (p / Math.pow(2, 8)) * (p / Math.pow(2, 8));
		x1 = (x1 * 3038) / Math.pow(2, 16);
		x2 = (-7357 * p) / Math.pow(2, 16);
		return p + (x1 + x2 + 3791) / Math.pow(2, 4);
	}

	private void delay(float ms) { // in ms
		long _ms = (long) ms;
		int ns = (int) ((ms - _ms) * 1E6);
//  System.out.println("Wait:" + _ms + " ms, " + ns + " ns");
		try {
			Thread.sleep(_ms, ns);
		} catch (Exception ex) {
			System.err.println("Wait for:" + ms + ", => " + _ms + " ms, " + ns + " ns");
			ex.printStackTrace();
		}
	}
}
