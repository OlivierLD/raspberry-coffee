package i2c.sensor;

import i2c.sensor.utils.BitOps;

import i2c.sensor.utils.L3GD20Dictionaries;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.util.Map;

import static utils.TimeUtil.delay;

/**
 * Triple axis Gyro
 */
public class L3GD20 {
	public final static int L3GD20ADDRESS = 0x6b;

	public final static int L3GD20_REG_R_WHO_AM_I = 0x0f; // Device identification register
	public final static int L3GD20_REG_RW_CTRL_REG1 = 0x20; // Control register 1
	public final static int L3GD20_REG_RW_CTRL_REG2 = 0x21; // Control register 2
	public final static int L3GD20_REG_RW_CTRL_REG3 = 0x22; // Control register 3
	public final static int L3GD20_REG_RW_CTRL_REG4 = 0x23; // Control register 4
	public final static int L3GD20_REG_RW_CTRL_REG5 = 0x24; // Control register 5
	public final static int L3GD20_REG_RW_REFERENCE = 0x25; // Reference value for interrupt generation
	public final static int L3GD20_REG_R_OUT_TEMP = 0x26; // Output temperature
	public final static int L3GD20_REG_R_STATUS_REG = 0x27; // Status register
	public final static int L3GD20_REG_R_OUT_X_L = 0x28; // X-axis angular data rate LSB
	public final static int L3GD20_REG_R_OUT_X_H = 0x29; // X-axis angular data rate MSB
	public final static int L3GD20_REG_R_OUT_Y_L = 0x2a; // Y-axis angular data rate LSB
	public final static int L3GD20_REG_R_OUT_Y_H = 0x2b; // Y-axis angular data rate MSB
	public final static int L3GD20_REG_R_OUT_Z_L = 0x2c; // Z-axis angular data rate LSB
	public final static int L3GD20_REG_R_OUT_Z_H = 0x2d; // Z-axis angular data rate MSB
	public final static int L3GD20_REG_RW_FIFO_CTRL_REG = 0x2e; // Fifo control register
	public final static int L3GD20_REG_R_FIFO_SRC_REG = 0x2f; // Fifo src register
	public final static int L3GD20_REG_RW_INT1_CFG_REG = 0x30; // Interrupt 1 configuration register
	public final static int L3GD20_REG_R_INT1_SRC_REG = 0x31; // Interrupt source register
	public final static int L3GD20_REG_RW_INT1_THS_XH = 0x32; // Interrupt 1 threshold level X MSB register
	public final static int L3GD20_REG_RW_INT1_THS_XL = 0x33; // Interrupt 1 threshold level X LSB register
	public final static int L3GD20_REG_RW_INT1_THS_YH = 0x34; // Interrupt 1 threshold level Y MSB register
	public final static int L3GD20_REG_RW_INT1_THS_YL = 0x35; // Interrupt 1 threshold level Y LSB register
	public final static int L3GD20_REG_RW_INT1_THS_ZH = 0x36; // Interrupt 1 threshold level Z MSB register
	public final static int L3GD20_REG_RW_INT1_THS_ZL = 0x37; // Interrupt 1 threshold level Z LSB register
	public final static int L3GD20_REG_RW_INT1_DURATION = 0x38; // Interrupt 1 duration register

	public final static int L3GD20_MASK_CTRL_REG1_Xen = 0x01; // X enable
	public final static int L3GD20_MASK_CTRL_REG1_Yen = 0x02; // Y enable
	public final static int L3GD20_MASK_CTRL_REG1_Zen = 0x04; // Z enable
	public final static int L3GD20_MASK_CTRL_REG1_PD = 0x08; // Power-down
	public final static int L3GD20_MASK_CTRL_REG1_BW = 0x30; // Bandwidth
	public final static int L3GD20_MASK_CTRL_REG1_DR = 0xc0; // Output data rate
	public final static int L3GD20_MASK_CTRL_REG2_HPCF = 0x0f; // High pass filter cutoff frequency
	public final static int L3GD20_MASK_CTRL_REG2_HPM = 0x30; // High pass filter mode selection
	public final static int L3GD20_MASK_CTRL_REG3_I2_EMPTY = 0x01; // FIFO empty interrupt on DRDY/INT2
	public final static int L3GD20_MASK_CTRL_REG3_I2_ORUN = 0x02; // FIFO overrun interrupt on DRDY/INT2
	public final static int L3GD20_MASK_CTRL_REG3_I2_WTM = 0x04; // FIFO watermark interrupt on DRDY/INT2
	public final static int L3GD20_MASK_CTRL_REG3_I2_DRDY = 0x08; // Date-ready on DRDY/INT2
	public final static int L3GD20_MASK_CTRL_REG3_PP_OD = 0x10; // Push-pull / Open-drain
	public final static int L3GD20_MASK_CTRL_REG3_H_LACTIVE = 0x20; // Interrupt active configuration on INT1
	public final static int L3GD20_MASK_CTRL_REG3_I1_BOOT = 0x40; // Boot status available on INT1
	public final static int L3GD20_MASK_CTRL_REG3_I1_Int1 = 0x80; // Interrupt enabled on INT1
	public final static int L3GD20_MASK_CTRL_REG4_SIM = 0x01; // SPI Serial interface selection
	public final static int L3GD20_MASK_CTRL_REG4_FS = 0x30; // Full scale selection
	public final static int L3GD20_MASK_CTRL_REG4_BLE = 0x40; // Big/little endian selection
	public final static int L3GD20_MASK_CTRL_REG4_BDU = 0x80; // Block data update
	public final static int L3GD20_MASK_CTRL_REG5_OUT_SEL = 0x03; // Out selection configuration
	public final static int L3GD20_MASK_CTRL_REG5_INT_SEL = 0xc0; // INT1 selection configuration
	public final static int L3GD20_MASK_CTRL_REG5_HPEN = 0x10; // High-pass filter enable
	public final static int L3GD20_MASK_CTRL_REG5_FIFO_EN = 0x40; // Fifo enable
	public final static int L3GD20_MASK_CTRL_REG5_BOOT = 0x80; // Reboot memory content
	public final static int L3GD20_MASK_STATUS_REG_ZYXOR = 0x80; // Z, Y, X axis overrun
	public final static int L3GD20_MASK_STATUS_REG_ZOR = 0x40; // Z axis overrun
	public final static int L3GD20_MASK_STATUS_REG_YOR = 0x20; // Y axis overrun
	public final static int L3GD20_MASK_STATUS_REG_XOR = 0x10; // X axis overrun
	public final static int L3GD20_MASK_STATUS_REG_ZYXDA = 0x08; // Z, Y, X data available
	public final static int L3GD20_MASK_STATUS_REG_ZDA = 0x04; // Z data available
	public final static int L3GD20_MASK_STATUS_REG_YDA = 0x02; // Y data available
	public final static int L3GD20_MASK_STATUS_REG_XDA = 0x01; // X data available
	public final static int L3GD20_MASK_FIFO_CTRL_REG_FM = 0xe0; // Fifo mode selection
	public final static int L3GD20_MASK_FIFO_CTRL_REG_WTM = 0x1f; // Fifo treshold - watermark level
	public final static int L3GD20_MASK_FIFO_SRC_REG_FSS = 0x1f; // Fifo stored data level
	public final static int L3GD20_MASK_FIFO_SRC_REG_EMPTY = 0x20; // Fifo empty bit
	public final static int L3GD20_MASK_FIFO_SRC_REG_OVRN = 0x40; // Overrun status
	public final static int L3GD20_MASK_FIFO_SRC_REG_WTM = 0x80; // Watermark status
	public final static int L3GD20_MASK_INT1_CFG_ANDOR = 0x80; // And/Or configuration of interrupt events
	public final static int L3GD20_MASK_INT1_CFG_LIR = 0x40; // Latch interrupt request
	public final static int L3GD20_MASK_INT1_CFG_ZHIE = 0x20; // Enable interrupt generation on Z high
	public final static int L3GD20_MASK_INT1_CFG_ZLIE = 0x10; // Enable interrupt generation on Z low
	public final static int L3GD20_MASK_INT1_CFG_YHIE = 0x08; // Enable interrupt generation on Y high
	public final static int L3GD20_MASK_INT1_CFG_YLIE = 0x04; // Enable interrupt generation on Y low
	public final static int L3GD20_MASK_INT1_CFG_XHIE = 0x02; // Enable interrupt generation on X high
	public final static int L3GD20_MASK_INT1_CFG_XLIE = 0x01; // Enable interrupt generation on X low
	public final static int L3GD20_MASK_INT1_SRC_IA = 0x40; // Int1 active
	public final static int L3GD20_MASK_INT1_SRC_ZH = 0x20; // Int1 source Z high
	public final static int L3GD20_MASK_INT1_SRC_ZL = 0x10; // Int1 source Z low
	public final static int L3GD20_MASK_INT1_SRC_YH = 0x08; // Int1 source Y high
	public final static int L3GD20_MASK_INT1_SRC_YL = 0x04; // Int1 source Y low
	public final static int L3GD20_MASK_INT1_SRC_XH = 0x02; // Int1 source X high
	public final static int L3GD20_MASK_INT1_SRC_XL = 0x01; // Int1 source X low
	public final static int L3GD20_MASK_INT1_THS_H = 0x7f; // MSB
	public final static int L3GD20_MASK_INT1_THS_L = 0xff; // LSB
	public final static int L3GD20_MASK_INT1_DURATION_WAIT = 0x80; // Wait number of samples or not
	public final static int L3GD20_MASK_INT1_DURATION_D = 0x7f; // Duration of int1 to be recognized

	private static boolean verbose = false;

	private I2CBus bus;
	private I2CDevice l3dg20;
	private double gain = 1D;

	// For calibration purposes
	private double meanX = 0;
	private double maxX = 0;
	private double minX = 0;
	private double meanY = 0;
	private double maxY = 0;
	private double minY = 0;
	private double meanZ = 0;
	private double maxZ = 0;
	private double minZ = 0;

	public L3GD20() throws I2CFactory.UnsupportedBusNumberException {
		this(L3GD20ADDRESS);
	}

	public L3GD20(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
			if (verbose)
				System.out.println("Connected to bus. OK.");

			// Get device itself
			l3dg20 = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void writeToRegister(int register, int mask, int value) throws Exception {
		int current = readU8(register);
		int newValue = BitOps.setValueUnderMask(value, current, mask);
		this.l3dg20.write(register, (byte) newValue);
	}

	public int readFromRegister(int register, int mask) throws Exception {
		int current = readU8(register);
		return BitOps.getValueUnderMask(current, mask);
	}

	private String readFromRegisterWithDictionaryMatch(int register, int mask, Map<String, Byte> dictionary) throws Exception {
		int current = this.readFromRegister(register, mask);
		for (String key : dictionary.keySet()) {
			if (dictionary.get(key) == (byte) current) {
				return key;
			}
		}
		return null;
	}

	private void writeToRegisterWithDictionaryCheck(int register, int mask, String value, Map<String, Byte> dictionary, String dictName) throws Exception {
		if (!dictionary.containsKey(value)) {
			throw new RuntimeException("Value [" + value + "] not in range of " + dictName);
		}
		this.writeToRegister(register, mask, dictionary.get(value));
	}

	/*
	 * To be called after configuration, before measuring
	 */
	public void init() throws Exception {
		String fullScaleValue = getFullScaleValue();
		if (fullScaleValue.equals(L3GD20Dictionaries._250_DPS)) {
			this.gain = 0.00875;
		} else if (fullScaleValue.equals(L3GD20Dictionaries._500_DPS)) {
			this.gain = 0.0175;
		} else if (fullScaleValue.equals(L3GD20Dictionaries._2000_DPS)) {
			this.gain = 0.07;
		}
	}

	public void calibrateX() throws Exception {
		System.out.println("Calibrating X, please do not move the sensor...");
		double[] buff = new double[20];
		for (int i = 0; i < 20; i++) {
			while (this.getAxisDataAvailableValue()[0] == 0) {
				delay(1L);
			}
			buff[i] = this.getRawOutXValue();
		}
		this.meanX = getMean(buff);
		this.maxX = getMax(buff);
		this.minX = getMin(buff);
	}

	public void calibrateY() throws Exception {
		System.out.println("Calibrating Y, please do not move the sensor...");
		double[] buff = new double[20];
		for (int i = 0; i < 20; i++) {
			while (this.getAxisDataAvailableValue()[1] == 0) {
				delay(1L);
			}
			buff[i] = this.getRawOutYValue();
		}
		this.meanY = getMean(buff);
		this.maxY = getMax(buff);
		this.minY = getMin(buff);
	}

	public void calibrateZ() throws Exception {
		System.out.println("Calibrating Z, please do not move the sensor...");
		double[] buff = new double[20];
		for (int i = 0; i < 20; i++) {
			while (this.getAxisDataAvailableValue()[2] == 0) {
				delay(1L);
			}
			buff[i] = this.getRawOutZValue();
		}
		this.meanZ = getMean(buff);
		this.maxZ = getMax(buff);
		this.minZ = getMin(buff);
	}

	public void calibrate() throws Exception {
		this.calibrateX();
		this.calibrateY();
		this.calibrateZ();
	}

	private static double getMax(double[] da) {
		double max = da[0];
		for (double d : da) {
			max = Math.max(max, d);
		}
		return max;
	}

	private static double getMin(double[] da) {
		double min = da[0];
		for (double d : da) {
			min = Math.min(min, d);
		}
		return min;
	}

	private static double getMean(double[] da) {
		double mean = 0;
		for (double d : da) {
			mean += d;
		}
		return mean / da.length;
	}

	public int[] getAxisOverrunValue() throws Exception {
		int zor = 0;
		int yor = 0;
		int xor = 0;
		if (this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZYXOR) == 0x01) {
			zor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZOR);
			yor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_YOR);
			xor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_XOR);
		}
		return new int[]{xor, yor, zor};
	}

	public int[] getAxisDataAvailableValue() throws Exception {
		int zda = 0;
		int yda = 0;
		int xda = 0;
		if (this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZYXDA) == 0x01) {
			zda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZDA);
			yda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_YDA);
			xda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_XDA);
		}
		return new int[]{xda, yda, zda};
	}

	private double getRawOutXValue() throws Exception {
		int l = this.readFromRegister(L3GD20_REG_R_OUT_X_L, 0xff);
		int h_u2 = this.readFromRegister(L3GD20_REG_R_OUT_X_H, 0xff);
		int h = BitOps.twosComplementToByte(h_u2);
		int value = 0;
		if (h < 0) {
			value = (h * 256 - l);
		} else if (h >= 0) {
			value = (h * 256 + l);
		}
		return value * this.gain;
	}

	private double getRawOutYValue() throws Exception {
		int l = this.readFromRegister(L3GD20_REG_R_OUT_Y_L, 0xff);
		int h_u2 = this.readFromRegister(L3GD20_REG_R_OUT_Y_H, 0xff);
		int h = BitOps.twosComplementToByte(h_u2);
		int value = 0;
		if (h < 0) {
			value = (h * 256 - l);
		} else if (h >= 0) {
			value = (h * 256 + l);
		}
		return value * this.gain;
	}

	private double getRawOutZValue() throws Exception {
		int l = this.readFromRegister(L3GD20_REG_R_OUT_Z_L, 0xff);
		int h_u2 = this.readFromRegister(L3GD20_REG_R_OUT_Z_H, 0xff);
		int h = BitOps.twosComplementToByte(h_u2);
		int value = 0;
		if (h < 0) {
			value = (h * 256 - l);
		} else if (h >= 0) {
			value = (h * 256 + l);
		}
		return value * this.gain;
	}

	public double[] getRawOutValues() throws Exception {
		return new double[]{this.getRawOutXValue(), this.getRawOutYValue(), this.getRawOutZValue()};
	}

	public double getCalOutXValue() throws Exception {
		double calX = 0d;
		double x = this.getRawOutXValue();
		if (x >= this.minX && x <= this.maxX) {
			calX = 0d;
		} else {
			calX = x - this.meanX;
		}
		return calX;
	}

	public double getCalOutYValue() throws Exception {
		double calY = 0d;
		double y = this.getRawOutYValue();
		if (y >= this.minY && y <= this.maxY) {
			calY = 0d;
		} else {
			calY = y - this.meanY;
		}
		return calY;
	}

	public double getCalOutZValue() throws Exception {
		double calZ = 0d;
		double z = this.getRawOutZValue();
		if (z >= this.minZ && z <= this.maxZ) {
			calZ = 0d;
		} else {
			calZ = z - this.meanZ;
		}
		return calZ;
	}

	public double[] getCalOutValue() throws Exception {
		return new double[]{this.getCalOutXValue(), this.getCalOutYValue(), this.getCalOutZValue()};
	}

	/*
	 * All getters and setters
	 */
	public String getFullScaleValue() throws Exception {
		return this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_CTRL_REG4, L3GD20_MASK_CTRL_REG4_FS, L3GD20Dictionaries.FullScaleMap);
	}

	public void setFullScaleValue(String value) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_CTRL_REG4, L3GD20_MASK_CTRL_REG4_FS, value, L3GD20Dictionaries.FullScaleMap, "FullScaleMap");
	}

	public String returnConfiguration() {
		return "To be implemented...";
	}

	public int getDeviceId() throws Exception {
		return this.readFromRegister(L3GD20_REG_R_WHO_AM_I, 0xff);
	}

	public void setAxisXEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_CTRL_REG1,
						L3GD20_MASK_CTRL_REG1_Xen,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isAxisXEnabled() throws Exception {
		String enabled = this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_Xen, L3GD20Dictionaries.EnabledMap);
		return enabled.equals(L3GD20Dictionaries.TRUE);
	}

	public void setAxisYEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_CTRL_REG1,
						L3GD20_MASK_CTRL_REG1_Yen,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isAxisYEnabled() throws Exception {
		String enabled = this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_Yen, L3GD20Dictionaries.EnabledMap);
		return enabled.equals(L3GD20Dictionaries.TRUE);
	}

	public void setAxisZEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_CTRL_REG1,
						L3GD20_MASK_CTRL_REG1_Zen,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isAxisZEnabled() throws Exception {
		String enabled = this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_Zen, L3GD20Dictionaries.EnabledMap);
		return enabled.equals(L3GD20Dictionaries.TRUE);
	}

	public void setPowerMode(String mode) throws Exception {
		if (!L3GD20Dictionaries.PowerModeMap.containsKey(mode)) {
			throw new RuntimeException("Value [" + mode + "] not accepted for PowerMode");
		}
		if (mode.equals(L3GD20Dictionaries.POWER_DOWN)) {
			this.writeToRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_PD, 0);
		} else if (mode.equals(L3GD20Dictionaries.SLEEP)) {
			this.writeToRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_PD |
					L3GD20_MASK_CTRL_REG1_Zen |
					L3GD20_MASK_CTRL_REG1_Yen |
					L3GD20_MASK_CTRL_REG1_Xen, 8);
		} else if (mode.equals(L3GD20Dictionaries.NORMAL)) {
			this.writeToRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_PD, 1);
		}
	}

	public String getPowerMode() throws Exception {
		int powermode = this.readFromRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_PD | L3GD20_MASK_CTRL_REG1_Xen | L3GD20_MASK_CTRL_REG1_Yen | L3GD20_MASK_CTRL_REG1_Zen);
		int dictval = -1;
		if (!BitOps.checkBit(powermode, 3)) {
			dictval = 0;
		} else if (powermode == 0b1000) {
			dictval = 1;
		} else if (BitOps.checkBit(powermode, 3)) {
			dictval = 2;
		}
		String key = "Unknown";
		for (String s : L3GD20Dictionaries.PowerModeMap.keySet()) {
			if (L3GD20Dictionaries.PowerModeMap.get(s) == dictval) {
				key = s;
				break;
			}
		}
		return key;
	}

	public void setFifoModeValue(String value) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_FIFO_CTRL_REG,
						L3GD20_MASK_FIFO_CTRL_REG_FM,
						value,
						L3GD20Dictionaries.FifoModeMap,
						"FifoModeMap");
	}

	public String getFifoModeValue() throws Exception {
		return this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_FIFO_CTRL_REG, L3GD20_MASK_FIFO_CTRL_REG_FM, L3GD20Dictionaries.FifoModeMap);
	}

	public void setDataRateAndBandwidth(int datarate, float bandwidth) throws Exception {
		if (!L3GD20Dictionaries.DataRateBandWidthMap.keySet().contains(datarate)) {
			throw new RuntimeException("Data rate:[" + Integer.toString(datarate) + "] not in range of data rate values.");
		}
		if (!L3GD20Dictionaries.DataRateBandWidthMap.get(datarate).keySet().contains(bandwidth)) {
			throw new RuntimeException("Bandwidth: [" + Float.toString(bandwidth) + "] cannot be assigned to data rate: [" + Integer.toString(datarate) + "]");
		}
		int bits = L3GD20Dictionaries.DataRateBandWidthMap.get(datarate).get(bandwidth);
		this.writeToRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_DR | L3GD20_MASK_CTRL_REG1_BW, bits);
	}

	public Number[] getDataRateAndBandwidth() throws Exception {
		Number dr = null, bw = null;
		int current = this.readFromRegister(L3GD20_REG_RW_CTRL_REG1, L3GD20_MASK_CTRL_REG1_DR | L3GD20_MASK_CTRL_REG1_BW);
		for (Integer drKey : L3GD20Dictionaries.DataRateBandWidthMap.keySet()) {
			for (Float bwKey : L3GD20Dictionaries.DataRateBandWidthMap.get(drKey).keySet()) {
				if (L3GD20Dictionaries.DataRateBandWidthMap.get(drKey).get(bwKey) == current) {
					dr = drKey;
					bw = bwKey;
					return new Number[]{dr, bw};
				}
			}
		}
		return new Number[]{dr, bw};
	}

	public void setFifoThresholdValue(int value) throws Exception {
		this.writeToRegister(L3GD20_REG_RW_FIFO_CTRL_REG, L3GD20_MASK_FIFO_CTRL_REG_WTM, value);
	}

	public int getFifoThresholdValue() throws Exception {
		return this.readFromRegister(L3GD20_REG_RW_FIFO_CTRL_REG, L3GD20_MASK_FIFO_CTRL_REG_WTM);
	}

	public int getFifoStoredDataLevelValue() throws Exception {
		return this.readFromRegister(L3GD20_REG_R_FIFO_SRC_REG, L3GD20_MASK_FIFO_SRC_REG_FSS);
	}

	public boolean isFifoEmpty() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_FIFO_SRC_REG,
						L3GD20_MASK_FIFO_SRC_REG_EMPTY,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean isFifoFull() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_FIFO_SRC_REG,
						L3GD20_MASK_FIFO_SRC_REG_OVRN,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean isFifoGreaterOrEqualThanWatermark() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_FIFO_SRC_REG,
						L3GD20_MASK_FIFO_SRC_REG_WTM,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1CombinationValue(String value) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ANDOR,
						value,
						L3GD20Dictionaries.AndOrMap,
						"AndOrMap");
	}

	public String getInt1CombinationValue() throws Exception {
		return this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ANDOR,
						L3GD20Dictionaries.AndOrMap);
	}

	public void setInt1LatchRequestEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_LIR,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1LatchRequestEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_LIR,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnZHighEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ZHIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnZHighEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ZHIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnZLowEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ZLIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnZLowEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_ZLIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnYHighEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_YHIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnYHighEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_YHIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnYLowEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_YLIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnYLowEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_YLIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnXHighEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_XHIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnXHighEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_XHIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1GenerationOnXLowEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_XLIE,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1GenerationOnXLowEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_CFG_REG,
						L3GD20_MASK_INT1_CFG_XLIE,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean isInt1Active() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_IA,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasZHighEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_ZH,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasZLowEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_ZL,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasYHighEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_YH,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasYLowEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_YL,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasXHighEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_XH,
						L3GD20Dictionaries.EnabledMap));
	}

	public boolean hasXLowEventOccured() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_R_INT1_SRC_REG,
						L3GD20_MASK_INT1_SRC_XL,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1ThresholdXValue(int value) throws Exception {
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_XH, L3GD20_MASK_INT1_THS_H, (value & 0x7f00) >> 8);
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_XL, L3GD20_MASK_INT1_THS_L, value & 0x00ff);
	}

	public void setInt1ThresholdYValue(int value) throws Exception {
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_YH, L3GD20_MASK_INT1_THS_H, (value & 0x7f00) >> 8);
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_YL, L3GD20_MASK_INT1_THS_L, value & 0x00ff);
	}

	public void setInt1ThresholdZValue(int value) throws Exception {
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_ZH, L3GD20_MASK_INT1_THS_H, (value & 0x7f00) >> 8);
		this.writeToRegister(L3GD20_REG_RW_INT1_THS_ZL, L3GD20_MASK_INT1_THS_L, value & 0x00ff);
	}

	public int[] getInt1Threshold_Values() throws Exception {
		int xh = this.readFromRegister(L3GD20_REG_RW_INT1_THS_XH, L3GD20_MASK_INT1_THS_H);
		int xl = this.readFromRegister(L3GD20_REG_RW_INT1_THS_XL, L3GD20_MASK_INT1_THS_L);
		int yh = this.readFromRegister(L3GD20_REG_RW_INT1_THS_YH, L3GD20_MASK_INT1_THS_H);
		int yl = this.readFromRegister(L3GD20_REG_RW_INT1_THS_YL, L3GD20_MASK_INT1_THS_L);
		int zh = this.readFromRegister(L3GD20_REG_RW_INT1_THS_ZH, L3GD20_MASK_INT1_THS_H);
		int zl = this.readFromRegister(L3GD20_REG_RW_INT1_THS_ZL, L3GD20_MASK_INT1_THS_L);
		return new int[]{xh * 256 + xl, yh * 256 + yl, zh * 256 + zl};
	}

	public void setInt1DurationWaitEnabled(boolean enabled) throws Exception {
		this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_INT1_DURATION,
						L3GD20_MASK_INT1_DURATION_WAIT,
						enabled ? L3GD20Dictionaries.TRUE : L3GD20Dictionaries.FALSE,
						L3GD20Dictionaries.EnabledMap,
						"EnabledMap");
	}

	public boolean isInt1DurationWaitEnabled() throws Exception {
		return L3GD20Dictionaries.TRUE.equals(this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_INT1_DURATION,
						L3GD20_MASK_INT1_DURATION_WAIT,
						L3GD20Dictionaries.EnabledMap));
	}

	public void setInt1DurationValue(int value) throws Exception {
		this.writeToRegister(L3GD20_REG_RW_INT1_DURATION, L3GD20_MASK_INT1_DURATION_D, value);
	}

	public int getInt1DurationValue() throws Exception {
		return this.readFromRegister(L3GD20_REG_RW_INT1_DURATION, L3GD20_MASK_INT1_DURATION_D);
	}

	/*
	 * Read an unsigned byte from the I2C device
	 */
	private int readU8(int reg) throws Exception {
		int result = 0;
		try {
			result = this.l3dg20.read(reg);
			if (verbose)
				System.out.println("(U8) I2C: Device " + toHex(L3GD20ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(reg));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	private static String toHex(int i) {
		String s = Integer.toString(i, 16).toUpperCase();
		while (s.length() % 2 != 0) {
			s = "0" + s;
		}
		return "0x" + s;
	}
}
