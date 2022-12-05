package i2c.sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.utils.EndianReaders;

import java.io.IOException;

/**
 * Time of Flight Distance Sensor
 * Adapted from from https://github.com/adafruit/Adafruit_CircuitPython_VL53L0X/blob/master/adafruit_vl53l0x.py
 * Driver for the VL53L0X https://www.adafruit.com/product/3317
 */
public class VL53L0X {
	public final static int VL53L0X_I2CADDR = 0x29;

	private final static int SYSRANGE_START = 0x00;
	private final static int SYSTEM_THRESH_HIGH = 0x0C;
	private final static int SYSTEM_THRESH_LOW = 0x0E;
	private final static int SYSTEM_SEQUENCE_CONFIG = 0x01;
	private final static int SYSTEM_RANGE_CONFIG = 0x09;
	private final static int SYSTEM_INTERMEASUREMENT_PERIOD = 0x04;
	private final static int SYSTEM_INTERRUPT_CONFIG_GPIO = 0x0A;
	private final static int GPIO_HV_MUX_ACTIVE_HIGH = 0x84;
	private final static int SYSTEM_INTERRUPT_CLEAR = 0x0B;
	private final static int RESULT_INTERRUPT_STATUS = 0x13;
	private final static int RESULT_RANGE_STATUS = 0x14;
	private final static int RESULT_CORE_AMBIENT_WINDOW_EVENTS_RTN = 0xBC;
	private final static int RESULT_CORE_RANGING_TOTAL_EVENTS_RTN = 0xC0;
	private final static int RESULT_CORE_AMBIENT_WINDOW_EVENTS_REF = 0xD0;
	private final static int RESULT_CORE_RANGING_TOTAL_EVENTS_REF = 0xD4;
	private final static int RESULT_PEAK_SIGNAL_RATE_REF = 0xB6;
	private final static int ALGO_PART_TO_PART_RANGE_OFFSET_MM = 0x28;
	private final static int I2C_SLAVE_DEVICE_ADDRESS = 0x8A;
	private final static int MSRC_CONFIG_CONTROL = 0x60;
	private final static int PRE_RANGE_CONFIG_MIN_SNR = 0x27;
	private final static int PRE_RANGE_CONFIG_VALID_PHASE_LOW = 0x56;
	private final static int PRE_RANGE_CONFIG_VALID_PHASE_HIGH = 0x57;
	private final static int PRE_RANGE_MIN_COUNT_RATE_RTN_LIMIT = 0x64;
	private final static int FINAL_RANGE_CONFIG_MIN_SNR = 0x67;
	private final static int FINAL_RANGE_CONFIG_VALID_PHASE_LOW = 0x47;
	private final static int FINAL_RANGE_CONFIG_VALID_PHASE_HIGH = 0x48;
	private final static int FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT = 0x44;
	private final static int PRE_RANGE_CONFIG_SIGMA_THRESH_HI = 0x61;
	private final static int PRE_RANGE_CONFIG_SIGMA_THRESH_LO = 0x62;
	private final static int PRE_RANGE_CONFIG_VCSEL_PERIOD = 0x50;
	private final static int PRE_RANGE_CONFIG_TIMEOUT_MACROP_HI = 0x51;
	private final static int PRE_RANGE_CONFIG_TIMEOUT_MACROP_LO = 0x52;
	private final static int SYSTEM_HISTOGRAM_BIN = 0x81;
	private final static int HISTOGRAM_CONFIG_INITIAL_PHASE_SELECT = 0x33;
	private final static int HISTOGRAM_CONFIG_READOUT_CTRL = 0x55;
	private final static int FINAL_RANGE_CONFIG_VCSEL_PERIOD = 0x70;
	private final static int FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI = 0x71;
	private final static int FINAL_RANGE_CONFIG_TIMEOUT_MACROP_LO = 0x72;
	private final static int CROSSTALK_COMPENSATION_PEAK_RATE_MCPS = 0x20;
	private final static int MSRC_CONFIG_TIMEOUT_MACROP = 0x46;
	private final static int SOFT_RESET_GO2_SOFT_RESET_N = 0xBF;
	private final static int IDENTIFICATION_MODEL_ID = 0xC0;
	private final static int IDENTIFICATION_REVISION_ID = 0xC2;
	private final static int OSC_CALIBRATE_VAL = 0xF8;
	private final static int GLOBAL_CONFIG_VCSEL_WIDTH = 0x32;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_0 = 0xB0;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_1 = 0xB1;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_2 = 0xB2;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_3 = 0xB3;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_4 = 0xB4;
	private final static int GLOBAL_CONFIG_SPAD_ENABLES_REF_5 = 0xB5;
	private final static int GLOBAL_CONFIG_REF_EN_START_SELECT = 0xB6;
	private final static int DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD = 0x4E;
	private final static int DYNAMIC_SPAD_REF_EN_START_OFFSET = 0x4F;
	private final static int POWER_MANAGEMENT_GO1_POWER_FORCE = 0x80;
	private final static int VHV_CONFIG_PAD_SCL_SDA__EXTSUP_HV = 0x89;
	private final static int ALGO_PHASECAL_LIM = 0x30;
	private final static int ALGO_PHASECAL_CONFIG_TIMEOUT = 0x30;
	private final static int VCSEL_PERIOD_PRE_RANGE = 0;
	private final static int VCSEL_PERIOD_FINAL_RANGE = 1;

	private static int decodeTimeout(int val) {
		// format:"(LSByte * 2^MSByte) + 1"
		return (int)((val &0xFF) * Math.pow(2.0,((val & 0xFF00) >> 8)) + 1);
	}

	private static int encodeTimeout(int timeoutMclks) {
		// format: "(LSByte * 2^MSByte) + 1"
		timeoutMclks = (int)((timeoutMclks) & 0xFFFF);
		int ls_byte = 0;
		int ms_byte = 0;
		if (timeoutMclks > 0) {
			ls_byte = timeoutMclks - 1;
			while (ls_byte > 255) {
				ls_byte >>= 1;
				ms_byte += 1;
			}
			return ((ms_byte << 8) | (ls_byte & 0xFF)) & 0xFFFF;
		}
		return 0;
	}

	private static int timeoutMclksToMicroSeconds(int timeoutPeriodMclks, int vcselPeriodPclks) {
		int macroPeriodNs = (int)(((2_304 * (vcselPeriodPclks) * 1_655) + 500) / 1_000f);
		return (int)(((timeoutPeriodMclks * macroPeriodNs) + (int)(macroPeriodNs / 2f)) / 1_000f);
	}

	private final static boolean verbose = "true".equals(System.getProperty("vl53l0x.debug", "false"));

	private final I2CBus bus;
	private final I2CDevice vl53l0x;

	private int ioTimeout = 0;
	private int stopVariable = 0;
	private int configControl = 0;
	private float signalRateLimit = 0f;

	private int measurementTimingBudgetMicrosec = 0, measurementTimingBudget = 0;

	public VL53L0X() throws IOException, I2CFactory.UnsupportedBusNumberException {
		this(VL53L0X_I2CADDR);
	}

	public VL53L0X(int address) throws IOException, I2CFactory.UnsupportedBusNumberException {
		this(address, 0);
	}

	public VL53L0X(int address, int timeout) throws IOException, I2CFactory.UnsupportedBusNumberException {
		this.ioTimeout = timeout;

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
		// Check identification registers for expected values.
		// From section 3.2 of the datasheet.
		if (this.readU8(0xC0) != 0xEE || this.readU8(0xC1) != 0xAA || this.readU8(0xC2) != 0x10) {
			throw new RuntimeException("Failed to find expected ID register values. Check wiring!");
		}
		// Initialize access to the sensor.  This is based on the logic from:
		// https://github.com/pololu/vl53l0x-arduino/blob/master/VL53L0X.cpp
		// Set I2C standard mode.
		this.vl53l0x.write((byte) 0x88, (byte) 0x00);
		this.vl53l0x.write((byte) 0x80, (byte) 0x01);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x00, (byte) 0x00);
		this.stopVariable = this.readU8(0x91);
		this.vl53l0x.write((byte) 0x00, (byte) 0x01);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x80, (byte) 0x00);

		// disable SIGNAL_RATE_MSRC (bit 1) and SIGNAL_RATE_PRE_RANGE (bit 4) limit checks
		this.configControl = this.readU8(MSRC_CONFIG_CONTROL) | 0x12;
		this.vl53l0x.write((byte) MSRC_CONFIG_CONTROL, (byte) this.configControl);
		// set final range signal rate limit to 0.25 MCPS (million counts per second)
		this.signalRateLimit = 0.25f;
		this.vl53l0x.write((byte) SYSTEM_SEQUENCE_CONFIG, (byte) 0xFF);

		SPADInfo spadInfo = getSpadInfo();
		// The SPAD map (RefGoodSpadMap) is read by VL53L0X_get_info_from_device() in the API, but the same data seems to
		// be more easily readable from GLOBAL_CONFIG_SPAD_ENABLES_REF_0 through _6, so read it from there.
		byte[] refSpadMap = new byte[7];
		refSpadMap[0] = (byte) GLOBAL_CONFIG_SPAD_ENABLES_REF_0;

		this.vl53l0x.write(refSpadMap, 0, 1);
//		self._device.readinto(ref_spad_map, start=1)
		this.vl53l0x.read(refSpadMap, 1, 6); // TODO Verify

		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) DYNAMIC_SPAD_REF_EN_START_OFFSET, (byte) 0x00);
		this.vl53l0x.write((byte) DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD, (byte) 0x2C);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) GLOBAL_CONFIG_REF_EN_START_SELECT, (byte) 0xB4);
		int firstSpadToEnable = spadInfo.isAperture ? 12 : 0;
		int spadsEnabled = 0;
		for (int i = 0; i < 48; i++) {
			// This bit is lower than the first one that should be enabled,
			// or (reference_spad_count) bits have already been enabled, so zero this bit.
			if (i < firstSpadToEnable || spadsEnabled == spadInfo.count) {
				refSpadMap[1 + (int) (i / 8)] &= ~(1 << (i % 8));
			} else {
				spadsEnabled += 1;
			}
		}

		this.vl53l0x.write(refSpadMap);

		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x00, (byte) 0x00);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x09, (byte) 0x00);
		this.vl53l0x.write((byte) 0x10, (byte) 0x00);
		this.vl53l0x.write((byte) 0x11, (byte) 0x00);
		this.vl53l0x.write((byte) 0x24, (byte) 0x01);
		this.vl53l0x.write((byte) 0x25, (byte) 0xFF);
		this.vl53l0x.write((byte) 0x75, (byte) 0x00);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x4E, (byte) 0x2C);
		this.vl53l0x.write((byte) 0x48, (byte) 0x00);
		this.vl53l0x.write((byte) 0x30, (byte) 0x20);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x30, (byte) 0x09);
		this.vl53l0x.write((byte) 0x54, (byte) 0x00);
		this.vl53l0x.write((byte) 0x31, (byte) 0x04);
		this.vl53l0x.write((byte) 0x32, (byte) 0x03);
		this.vl53l0x.write((byte) 0x40, (byte) 0x83);
		this.vl53l0x.write((byte) 0x46, (byte) 0x25);
		this.vl53l0x.write((byte) 0x60, (byte) 0x00);
		this.vl53l0x.write((byte) 0x27, (byte) 0x00);
		this.vl53l0x.write((byte) 0x50, (byte) 0x06);
		this.vl53l0x.write((byte) 0x51, (byte) 0x00);
		this.vl53l0x.write((byte) 0x52, (byte) 0x96);
		this.vl53l0x.write((byte) 0x56, (byte) 0x08);
		this.vl53l0x.write((byte) 0x57, (byte) 0x30);
		this.vl53l0x.write((byte) 0x61, (byte) 0x00);
		this.vl53l0x.write((byte) 0x62, (byte) 0x00);
		this.vl53l0x.write((byte) 0x64, (byte) 0x00);
		this.vl53l0x.write((byte) 0x65, (byte) 0x00);
		this.vl53l0x.write((byte) 0x66, (byte) 0xA0);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x22, (byte) 0x32);
		this.vl53l0x.write((byte) 0x47, (byte) 0x14);
		this.vl53l0x.write((byte) 0x49, (byte) 0xFF);
		this.vl53l0x.write((byte) 0x4A, (byte) 0x00);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x7A, (byte) 0x0A);
		this.vl53l0x.write((byte) 0x7B, (byte) 0x00);
		this.vl53l0x.write((byte) 0x78, (byte) 0x21);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x23, (byte) 0x34);
		this.vl53l0x.write((byte) 0x42, (byte) 0x00);
		this.vl53l0x.write((byte) 0x44, (byte) 0xFF);
		this.vl53l0x.write((byte) 0x45, (byte) 0x26);
		this.vl53l0x.write((byte) 0x46, (byte) 0x05);
		this.vl53l0x.write((byte) 0x40, (byte) 0x40);
		this.vl53l0x.write((byte) 0x0E, (byte) 0x06);
		this.vl53l0x.write((byte) 0x20, (byte) 0x1A);
		this.vl53l0x.write((byte) 0x43, (byte) 0x40);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x34, (byte) 0x03);
		this.vl53l0x.write((byte) 0x35, (byte) 0x44);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x31, (byte) 0x04);
		this.vl53l0x.write((byte) 0x4B, (byte) 0x09);
		this.vl53l0x.write((byte) 0x4C, (byte) 0x05);
		this.vl53l0x.write((byte) 0x4D, (byte) 0x04);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x44, (byte) 0x00);
		this.vl53l0x.write((byte) 0x45, (byte) 0x20);
		this.vl53l0x.write((byte) 0x47, (byte) 0x08);
		this.vl53l0x.write((byte) 0x48, (byte) 0x28);
		this.vl53l0x.write((byte) 0x67, (byte) 0x00);
		this.vl53l0x.write((byte) 0x70, (byte) 0x04);
		this.vl53l0x.write((byte) 0x71, (byte) 0x01);
		this.vl53l0x.write((byte) 0x72, (byte) 0xFE);
		this.vl53l0x.write((byte) 0x76, (byte) 0x00);
		this.vl53l0x.write((byte) 0x77, (byte) 0x00);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x0D, (byte) 0x01);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x80, (byte) 0x01);
		this.vl53l0x.write((byte) 0x01, (byte) 0xF8);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x01);
		this.vl53l0x.write((byte) 0x8E, (byte) 0x01);
		this.vl53l0x.write((byte) 0x00, (byte) 0x01);
		this.vl53l0x.write((byte) 0xFF, (byte) 0x00);
		this.vl53l0x.write((byte) 0x80, (byte) 0x00);
		this.vl53l0x.write((byte) SYSTEM_INTERRUPT_CONFIG_GPIO, (byte) 0x04);
		int gpioHvMuxActiveHigh = this.readU8(GPIO_HV_MUX_ACTIVE_HIGH);
		this.vl53l0x.write((byte) GPIO_HV_MUX_ACTIVE_HIGH, (byte) (gpioHvMuxActiveHigh & ~0x10)); // active low
		this.vl53l0x.write((byte) SYSTEM_INTERRUPT_CLEAR, (byte) 0x01);
		this.measurementTimingBudgetMicrosec = this.measurementTimingBudget;
		this.vl53l0x.write((byte) SYSTEM_SEQUENCE_CONFIG, (byte) 0xE8);
		this.measurementTimingBudget = this.measurementTimingBudgetMicrosec;
		this.vl53l0x.write((byte) SYSTEM_SEQUENCE_CONFIG, (byte) 0x01);
		this.performSingleRefCalibration(0x40);
		this.vl53l0x.write((byte) SYSTEM_SEQUENCE_CONFIG, (byte) 0x02);
		this.performSingleRefCalibration(0x00);
		// restore the previous Sequence Config
		this.vl53l0x.write((byte) SYSTEM_SEQUENCE_CONFIG, (byte) 0xE8);

		if (verbose) {
			System.out.println("Constructor OK.");
		}
	}

	private int readU8(int register) throws IOException {
		return EndianReaders.readU8(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readS8(int register) throws IOException {
		return EndianReaders.readS8(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readU16LE(int register) throws IOException {
		return EndianReaders.readU16LE(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readU16BE(int register) throws IOException {
		return EndianReaders.readU16BE(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private int readS16LE(int register) throws IOException {
		return EndianReaders.readS16LE(this.vl53l0x, VL53L0X_I2CADDR, register, verbose);
	}

	private byte[] readBlockData(int register, int nb) throws IOException {
		byte[] data = new byte[nb];
		this.vl53l0x.read(register, data, 0, nb);
		return data;
	}

	private void writeU16(int address, int val) throws IOException {
		this.vl53l0x.write(address & 0xFF, new byte[] { (byte)((val >> 8) & 0xFF), (byte)(val & 0xFF) });
	}

	private static class SPADInfo {
		int count;
		boolean isAperture;

		public SPADInfo setCount(int count) {
			this.count = count;
			return this;
		}
		public SPADInfo setAperture(boolean aperture) {
			this.isAperture = aperture;
			return this;
		}
	}
	/**
	 * Get reference SPAD count and type, returned as a 2 - tuple of
	 * count and boolean is_aperture. Based on code from:
	 * https://github.com/pololu/vl53l0x-arduino/blob/master/VL53L0X.cpp
	 *
	 * For this Java version, we use {@link SPADInfo} so much more elegant ;)
	 * We miss the tuples in Java, though. I'll do a Scala implementation, later.
	 */
	private SPADInfo getSpadInfo() throws IOException {
		this.vl53l0x.write((byte)0x80, (byte)0x01);
		this.vl53l0x.write((byte)0xFF, (byte)0x01);
		this.vl53l0x.write((byte)0x00, (byte)0x00);
		this.vl53l0x.write((byte)0xFF, (byte)0x06);
		this.vl53l0x.write((byte)0x83, (byte)(this.readU8(0x83) | 0x04));
		this.vl53l0x.write((byte)0xFF, (byte)0x07);
		this.vl53l0x.write((byte)0x81, (byte)0x01);
		this.vl53l0x.write((byte)0x80, (byte)0x01);
		this.vl53l0x.write((byte)0x94, (byte)0x6b);
		this.vl53l0x.write((byte)0x83, (byte)0x00);
		long start = System.currentTimeMillis();
		while (this.readU8(0x83) == 0x00) {
			if (this.ioTimeout > 0 && ((System.currentTimeMillis() - start) / 1000) >= this.ioTimeout) {
				throw new RuntimeException ("Timeout waiting for VL53L0X!");
			}
		}
		this.vl53l0x.write((byte)0x83, (byte)0x01);
		int tmp = this.readU8(0x92);
		int count = tmp & 0x7F;
		boolean isAperture = ((tmp >> 7) & 0x01) == 1;
		this.vl53l0x.write((byte)0x81, (byte)0x00);
		this.vl53l0x.write((byte)0xFF, (byte)0x06);
		this.vl53l0x.write((byte)0x83, (byte)(this.readU8(0x83) & ~0x04));
		this.vl53l0x.write((byte)0xFF, (byte)0x01);
		this.vl53l0x.write((byte)0x00, (byte)0x01);
		this.vl53l0x.write((byte)0xFF, (byte)0x00);
		this.vl53l0x.write((byte)0x80, (byte)0x00);
		return (new SPADInfo().setCount(count).setAperture(isAperture));
	}

	private void performSingleRefCalibration(int vhvInitByte)
	throws IOException {
		// based on VL53L0X_perform_single_ref_calibration() from ST API.
		this.vl53l0x.write((byte)SYSRANGE_START, (byte)(0x01 | vhvInitByte & 0xFF));
		long start = System.currentTimeMillis();
		while ((this.readU8(RESULT_INTERRUPT_STATUS) & 0x07) == 0){
			if (this.ioTimeout > 0 && ((System.currentTimeMillis() - start) / 1_000) >= this.ioTimeout) {
				throw new RuntimeException("Timeout waiting for VL53L0X!");
			}
		}
		this.vl53l0x.write((byte)SYSTEM_INTERRUPT_CLEAR, (byte)0x01);
		this.vl53l0x.write((byte)SYSRANGE_START, (byte)0x00);
	}

	private int getVcselPulsePeriod(int vcsel_period_type) throws IOException {
		if (vcsel_period_type == VCSEL_PERIOD_PRE_RANGE ) {
			int val = this.readU8(PRE_RANGE_CONFIG_VCSEL_PERIOD);
			return (((val) + 1) & 0xFF) << 1;
		} else if (vcsel_period_type == VCSEL_PERIOD_FINAL_RANGE) {
			int val = this.readU8(FINAL_RANGE_CONFIG_VCSEL_PERIOD);
			return (((val) + 1) & 0xFF) << 1;
		}
		return 255;
	}

	public static class SequenceStep {
		boolean tcc, dss, msrc, preRange, finalRange;

		public SequenceStep tcc(boolean tcc) {
			this.tcc = tcc;
			return this;
		}
		public SequenceStep dss(boolean dss) {
			this.dss = dss;
			return this;
		}
		public SequenceStep msrc(boolean msrc) {
			this.msrc = msrc;
			return this;
		}
		public SequenceStep preRange(boolean preRange) {
			this.preRange = preRange;
			return this;
		}
		public SequenceStep finalRange(boolean finalRange) {
			this.finalRange = finalRange;
			return this;
		}
	}

	// based on VL53L0X_GetSequenceStepEnables() from ST API
	private SequenceStep getSequenceStepEnables() throws IOException {
		int	sequenceConfig = this.readU8(SYSTEM_SEQUENCE_CONFIG);
		return (new SequenceStep()
				.tcc(((sequenceConfig >> 4) & 0x1) > 0)
				.dss(((sequenceConfig >> 3) & 0x1) > 0)
				.msrc(((sequenceConfig >> 2) & 0x1) > 0)
				.preRange(((sequenceConfig >> 6) & 0x1) > 0)
				.finalRange(((sequenceConfig >> 7) & 0x1) > 0));
	}

	public static class SequenceStepTimeouts {
		int msrcDssTccMicrosec,
				preRangeMicrosec,
				finalRangeMicorsec,
				finalRangeVcselPeriodPclks,
				preRangeMclks;
		public SequenceStepTimeouts msrcDssTccMicrosec(int msrcDssTccMicrosec) {
			this.msrcDssTccMicrosec = msrcDssTccMicrosec;
			return this;
		}
		public SequenceStepTimeouts preRangeMicrosec(int preRangeMicrosec) {
			this.preRangeMicrosec = preRangeMicrosec;
			return this;
		}
		public SequenceStepTimeouts finalRangeMicorsec(int finalRangeMicorsec) {
			this.finalRangeMicorsec = finalRangeMicorsec;
			return this;
		}
		public SequenceStepTimeouts finalRangeVcselPeriodPclks(int finalRangeVcselPeriodPclks) {
			this.finalRangeVcselPeriodPclks = finalRangeVcselPeriodPclks;
			return this;
		}
		public SequenceStepTimeouts preRangeMclks(int preRangeMclks) {
			this.preRangeMclks = preRangeMclks;
			return this;
		}
	}

	/* based on get_sequence_step_timeout() from ST API but modified by pololu here:
	 *    https://github.com/pololu/vl53l0x-arduino/blob/master/VL53L0X.cpp
	 */
	private SequenceStepTimeouts getSequenceStepTimeouts(boolean preRange) throws Exception {
		int preRangeVcselPeriodPclks = this.getVcselPulsePeriod(VCSEL_PERIOD_PRE_RANGE);
		int msrcDssTccMclks = (this.readU8(MSRC_CONFIG_TIMEOUT_MACROP) + 1) & 0xFF;
		int msrcDssTccMicrosec = timeoutMclksToMicroSeconds(msrcDssTccMclks, preRangeVcselPeriodPclks);
		int preRangeMclks = decodeTimeout(this.readU16BE(PRE_RANGE_CONFIG_TIMEOUT_MACROP_HI));
		int preRangeMicrosec = timeoutMclksToMicroSeconds(preRangeMclks, preRangeVcselPeriodPclks);
		int finalRangeVcselPeriodPclks = this.getVcselPulsePeriod(VCSEL_PERIOD_FINAL_RANGE);
		int finalRangeMclks = decodeTimeout(this.readU16BE(FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI));
		if (preRange) {
			finalRangeMclks -= preRangeMclks;
		}
		int finalRangeUs = timeoutMclksToMicroSeconds(finalRangeMclks, finalRangeVcselPeriodPclks);
		return (new SequenceStepTimeouts()
				.msrcDssTccMicrosec(msrcDssTccMicrosec)
				.preRangeMicrosec(preRangeMicrosec)
				.finalRangeMicorsec(finalRangeUs)
				.finalRangeVcselPeriodPclks(finalRangeVcselPeriodPclks)
				.preRangeMclks(preRangeMclks));
	}

	/* The signal rate limit in mega counts per second. */
	public float getSignalRateLimit() throws Exception {
		int val = this.readU16BE(FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT);
		// Return value converted from 16 - bit 9.7 fixed point to float.
		this.signalRateLimit = (float)val / (float)(1 << 7);
		return this.signalRateLimit;
	}

	public void setSignalRateLimit(float val) throws IOException {
		assert(0.0 <= val && val <= 511.99);
		// Convert to 16 - bit 9.7 fixed point value from a float.
		int	value = (int)(val * (1 << 7));
		this.writeU16(FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT, value);
	}

	/* The measurement timing budget in microseconds. */
	public int getMeasurementTimingBudget() throws Exception {
		int budget_us = 1_910 + 960;  // Start overhead +end overhead.
		boolean tcc, dss, msrc, pre_range, final_range;
		SequenceStep sequenceStep = this.getSequenceStepEnables();
		SequenceStepTimeouts step_timeouts = this.getSequenceStepTimeouts(sequenceStep.preRange);
		if (sequenceStep.tcc) {
			budget_us += (step_timeouts.msrcDssTccMicrosec + 590);
		}
		if (sequenceStep.dss) {
			budget_us += (2 * (step_timeouts.msrcDssTccMicrosec + 690));
		} else if (sequenceStep.msrc) {
			budget_us += (step_timeouts.msrcDssTccMicrosec + 660);
		}
		if (sequenceStep.preRange) {
			budget_us += (step_timeouts.preRangeMicrosec + 660);
		}
		if (sequenceStep.finalRange) {
			budget_us += (step_timeouts.finalRangeMicorsec + 550);
		}
		this.measurementTimingBudgetMicrosec = budget_us;
		return budget_us;
	}

	public void setMeasurementTimingBudget(int budgetMicrosec) throws Exception {
		assert(budgetMicrosec >= 20_000);
		int usedBudgetMicrosec = 1_320 + 960;  // Start(diff from get) + end overhead
		SequenceStep sequenceStepEnables = this.getSequenceStepEnables();
		SequenceStepTimeouts sequenceStepTimeouts = this.getSequenceStepTimeouts(sequenceStepEnables.preRange);
		if (sequenceStepEnables.tcc) {
			usedBudgetMicrosec += (sequenceStepTimeouts.msrcDssTccMicrosec + 590);
		}
		if (sequenceStepEnables.dss) {
			usedBudgetMicrosec += (2 * (sequenceStepTimeouts.msrcDssTccMicrosec + 690));
		} else if (sequenceStepEnables.msrc) {
			usedBudgetMicrosec += (sequenceStepTimeouts.msrcDssTccMicrosec + 660);
		}
		if (sequenceStepEnables.preRange) {
			usedBudgetMicrosec += (sequenceStepTimeouts.preRangeMicrosec + 660);
		}
		if (sequenceStepEnables.finalRange) {
			usedBudgetMicrosec += 550;
		}
		// Note that the final range timeout is determined by the timing
		// budget and the sum of all other timeouts within the sequence.
		// If there is no room for the final range timeout, then an error
		// will be set.Otherwise the remaining time will be applied to
		// the final range.
		if (usedBudgetMicrosec > budgetMicrosec) {
			throw new RuntimeException("Requested timeout too big.");
		}
		int finalRangeTimeoutMicrosec = budgetMicrosec - usedBudgetMicrosec;
		int finalRangeTimeoutMclks = timeoutMclksToMicroSeconds(finalRangeTimeoutMicrosec, sequenceStepTimeouts.finalRangeVcselPeriodPclks);
		if (sequenceStepEnables.preRange) {
			finalRangeTimeoutMclks += sequenceStepTimeouts.preRangeMclks;
		}
		this.writeU16(FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI, encodeTimeout(finalRangeTimeoutMclks));
		this.measurementTimingBudgetMicrosec = budgetMicrosec;
	}

	/**
	 * Perform a single reading of the range for an object in front of the sensor and return the distance in millimeters.
	 *
	 * Adapted from readRangeSingleMillimeters & readRangeContinuousMillimeters in pololu code at:
	 * https://github.com/pololu/vl53l0x-arduino/blob/master/VL53L0X.cpp
   *
	 * @return the distance in mm
	 */
	public int range() throws IOException {
		this.vl53l0x.write((byte)0x80, (byte)0x01);
		this.vl53l0x.write((byte)0xFF, (byte)0x01);
		this.vl53l0x.write((byte)0x00, (byte)0x00);
		this.vl53l0x.write((byte)0x91, (byte)this.stopVariable);
		this.vl53l0x.write((byte)0x00, (byte)0x01);
		this.vl53l0x.write((byte)0xFF, (byte)0x00);
		this.vl53l0x.write((byte)0x80, (byte)0x00);
		this.vl53l0x.write((byte)SYSRANGE_START, (byte)0x01);
		long start = System.currentTimeMillis();
		while ((this.readU8(SYSRANGE_START) & 0x01) > 0) {
			if (this.ioTimeout > 0 && ((System.currentTimeMillis() - start) / 1_000) >= this.ioTimeout) {
				throw new RuntimeException("Timeout waiting for VL53L0X!");
			}
		}
		start = System.currentTimeMillis();
		while ((this.readU8(RESULT_INTERRUPT_STATUS) & 0x07) == 0) {
			if (this.ioTimeout > 0 && ((System.currentTimeMillis() - start) / 1_000) >= this.ioTimeout) {
				throw new RuntimeException("Timeout waiting for VL53L0X!");
			}
		}
		// assumptions: Linearity Corrective Gain is 1000 (default)
		// fractional ranging is not enabled
		int rangeMm = this.readU16BE(RESULT_RANGE_STATUS + 10);
		this.vl53l0x.write((byte)SYSTEM_INTERRUPT_CLEAR, (byte)0x01);
		return rangeMm;
	}

	// For tests
	public static void main(String... args) {
		try {
			VL53L0X vl53l0x = new VL53L0X();
			int previousDist = -1;
			while (true) {
				int mm = vl53l0x.range();
				if (previousDist != mm) {
					System.out.printf("Range: %d mm\n", mm);
				}
				previousDist = mm;
				try {
					Thread.sleep(50L);
				} catch (InterruptedException iex) {

				}
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			ubne.printStackTrace();
		}
	}
}
