package i2c.sensor.utils;

import java.util.HashMap;
import java.util.Map;

public class L3GD20Dictionaries {
	public final static String POWER_DOWN = "Power-down";
	public final static String SLEEP = "Sleep";
	public final static String NORMAL = "Normal";
	public final static Map<String, Byte> PowerModeMap = new HashMap<>();

	static {
		PowerModeMap.put(POWER_DOWN, (byte) 0);
		PowerModeMap.put(SLEEP, (byte) 1);
		PowerModeMap.put(NORMAL, (byte) 2);
	}

	public final static String FALSE = "false";
	public final static String TRUE = "true";
	public final static Map<String, Byte> EnabledMap = new HashMap<String, Byte>();

	static {
		EnabledMap.put(FALSE, (byte) 0);
		EnabledMap.put(TRUE, (byte) 1);
	}

	public final static String HIGH = "High";
	public final static String LOW = "Low";
	public final static Map<String, Byte> LevelMap = new HashMap<String, Byte>();

	static {
		LevelMap.put(HIGH, (byte) 0);
		LevelMap.put(LOW, (byte) 1);
	}

	public final static String PUSH_PULL = "Push-pull";
	public final static String OPEN_DRAIN = "Open drain";
	public final static Map<String, Byte> OutputMap = new HashMap<String, Byte>();

	static {
		OutputMap.put(PUSH_PULL, (byte) 0);
		OutputMap.put(OPEN_DRAIN, (byte) 1);
	}

	public final static String _4_WIRE = "4-wire";
	public final static String _3_WIRE = "3-wire";
	public final static Map<String, Byte> SimModeMap = new HashMap<String, Byte>();

	static {
		SimModeMap.put(_4_WIRE, (byte) 0);
		SimModeMap.put(_3_WIRE, (byte) 1);
	}

	public final static String BIG_ENDIAN = "Big endian";
	public final static String LITTLE_ENDIAN = "Little endian";
	public final static Map<String, Byte> BigLittleEndianMap = new HashMap<String, Byte>();

	static {
		BigLittleEndianMap.put(BIG_ENDIAN, (byte) 0);
		BigLittleEndianMap.put(LITTLE_ENDIAN, (byte) 1);
	}

	public final static String _250_DPS = "250dps";
	public final static String _500_DPS = "500dps";
	public final static String _2000_DPS = "2000dps";
	public final static Map<String, Byte> FullScaleMap = new HashMap<String, Byte>();

	static {
		FullScaleMap.put(_250_DPS, (byte) 0);
		FullScaleMap.put(_500_DPS, (byte) 1);
		FullScaleMap.put(_2000_DPS, (byte) 2);
	}

	public final static String CONTINUOUS_UPDATE = "Continous update";
	public final static String NOT_UPDATED_UNTIL_READING = "Output registers not updated until reading";
	public final static Map<String, Byte> BlockDataUpdateMap = new HashMap<String, Byte>();

	static {
		BlockDataUpdateMap.put(CONTINUOUS_UPDATE, (byte) 0);
		BlockDataUpdateMap.put(NOT_UPDATED_UNTIL_READING, (byte) 1);
	}

	public final static String LPF1 = "LPF1";
	public final static String HPF = "HPF";
	public final static String LPF2 = "LPF2";
	public final static Map<String, Byte> OutSelMap = new HashMap<String, Byte>();

	static {
		OutSelMap.put(LPF1, (byte) 0);
		OutSelMap.put(HPF, (byte) 1);
		OutSelMap.put(LPF2, (byte) 2);
	}

	public final static Map<String, Byte> IntSelMap = new HashMap<String, Byte>();

	static {
		IntSelMap.put(LPF1, (byte) 0);
		IntSelMap.put(HPF, (byte) 1);
		IntSelMap.put(LPF2, (byte) 2);
	}

	//public final static String NORMAL = "Normal";
	public final static String REBOOT_MEMORY_CONTENT = "Reboot memory content";
	public final static Map<String, Byte> BootModeMap = new HashMap<String, Byte>();

	static {
		BootModeMap.put(NORMAL, (byte) 0);
		BootModeMap.put(REBOOT_MEMORY_CONTENT, (byte) 1);
	}

	public final static String BYPASS = "Bypass";
	public final static String FIFO = "FIFO";
	public final static String STREAM = "Stream";
	public final static String STREAM_TO_FIFO = "Stream-to-Fifo";
	public final static String BYPASS_TO_STREAM = "Bypass-to-Stream";
	public final static Map<String, Byte> FifoModeMap = new HashMap<String, Byte>();

	static {
		FifoModeMap.put(BYPASS, (byte) 0);
		FifoModeMap.put(FIFO, (byte) 1);
		FifoModeMap.put(STREAM, (byte) 2);
		FifoModeMap.put(STREAM_TO_FIFO, (byte) 3);
		FifoModeMap.put(BYPASS_TO_STREAM, (byte) 4);
	}

	public final static String AND = "And";
	public final static String OR = "Or";
	public final static Map<String, Byte> AndOrMap = new HashMap<String, Byte>();

	static {
		AndOrMap.put(AND, (byte) 0);
		AndOrMap.put(OR, (byte) 1);
	}

	public final static String NORMAL_WITH_RESET = "Normal with reset.";
	public final static String REFERENCE_SIGNAL_FOR_FILTERING = "Reference signal for filtering.";
	//public final static String NORMAL                          = "Normal";
	public final static String AUTORESET_ON_INTERRUPT = "Autoreset on interrupt.";
	public final static Map<String, Byte> HighPassFilterMap = new HashMap<String, Byte>();

	static {
		HighPassFilterMap.put(NORMAL_WITH_RESET, (byte) 0);
		HighPassFilterMap.put(REFERENCE_SIGNAL_FOR_FILTERING, (byte) 1);
		HighPassFilterMap.put(NORMAL, (byte) 2);
		HighPassFilterMap.put(AUTORESET_ON_INTERRUPT, (byte) 3);
	}

	private final static int[] DATA_RATE_VALUES = {95, 190, 380, 760};
	private final static float[] BANDWIDTH_VALUES = {12.5f, 20, 25, 30, 35, 50, 70, 100};
	private final static float[] HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES = {51.4f, 27, 13.5f, 7.2f, 3.5f, 1.8f, 0.9f, 0.45f, 0.18f, 0.09f, 0.045f, 0.018f, 0.009f};

	// __DRBW
	public final static Map<Integer, Map<Float, Byte>> DataRateBandWidthMap = new HashMap<Integer, Map<Float, Byte>>();

	static {
		// DataRateValues[0] : { BandWidthValues[0]:0x00, BandWidthValues[2]:0x01},
		Map<Float, Byte> map0 = new HashMap<Float, Byte>();
		map0.put(BANDWIDTH_VALUES[0], (byte) 0);
		map0.put(BANDWIDTH_VALUES[2], (byte) 1);
		DataRateBandWidthMap.put(DATA_RATE_VALUES[0], map0);
		// DataRateValues[1] : { BandWidthValues[0]:0x04, BandWidthValues[2]:0x05, BandWidthValues[5]:0x06, BandWidthValues[6]:0x07},
		Map<Float, Byte> map1 = new HashMap<Float, Byte>();
		map1.put(BANDWIDTH_VALUES[0], (byte) 0x4);
		map1.put(BANDWIDTH_VALUES[2], (byte) 0x5);
		map1.put(BANDWIDTH_VALUES[5], (byte) 0x6);
		map1.put(BANDWIDTH_VALUES[6], (byte) 0x7);
		DataRateBandWidthMap.put(DATA_RATE_VALUES[1], map1);
		// DataRateValues[2] : { BandWidthValues[1]:0x08, BandWidthValues[2]:0x09, BandWidthValues[5]:0x0a, BandWidthValues[7]:0x0b},
		Map<Float, Byte> map2 = new HashMap<Float, Byte>();
		map2.put(BANDWIDTH_VALUES[1], (byte) 0x8);
		map2.put(BANDWIDTH_VALUES[2], (byte) 0x9);
		map2.put(BANDWIDTH_VALUES[5], (byte) 0xa);
		map2.put(BANDWIDTH_VALUES[7], (byte) 0xb);
		DataRateBandWidthMap.put(DATA_RATE_VALUES[2], map2);
		// DataRateValues[3] : { BandWidthValues[3]:0x0c, BandWidthValues[4]:0x0d, BandWidthValues[5]:0x0e, BandWidthValues[7]:0x0f}
		Map<Float, Byte> map3 = new HashMap<Float, Byte>();
		map3.put(BANDWIDTH_VALUES[3], (byte) 0xc);
		map3.put(BANDWIDTH_VALUES[4], (byte) 0xd);
		map3.put(BANDWIDTH_VALUES[5], (byte) 0xe);
		map3.put(BANDWIDTH_VALUES[7], (byte) 0xf);
		DataRateBandWidthMap.put(DATA_RATE_VALUES[3], map3);
	}

	// __HPCF
	public final static Map<Float, Map<Integer, Byte>> HighPassCutOffMap = new HashMap<Float, Map<Integer, Byte>>();

	static {
		Map<Integer, Byte> map0 = new HashMap<Integer, Byte>();
		map0.put(DATA_RATE_VALUES[3], (byte) 0);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[0], map0);

		Map<Integer, Byte> map1 = new HashMap<Integer, Byte>();
		map1.put(DATA_RATE_VALUES[2], (byte) 0x0);
		map1.put(DATA_RATE_VALUES[3], (byte) 0x1);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[1], map1);

		Map<Integer, Byte> map2 = new HashMap<Integer, Byte>();
		map2.put(DATA_RATE_VALUES[1], (byte) 0x0);
		map2.put(DATA_RATE_VALUES[2], (byte) 0x1);
		map2.put(DATA_RATE_VALUES[3], (byte) 0x2);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[2], map2);

		Map<Integer, Byte> map3 = new HashMap<Integer, Byte>();
		map3.put(DATA_RATE_VALUES[0], (byte) 0x0);
		map3.put(DATA_RATE_VALUES[1], (byte) 0x1);
		map3.put(DATA_RATE_VALUES[2], (byte) 0x2);
		map3.put(DATA_RATE_VALUES[3], (byte) 0x3);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[3], map3);

		Map<Integer, Byte> map4 = new HashMap<Integer, Byte>();
		map4.put(DATA_RATE_VALUES[0], (byte) 0x1);
		map4.put(DATA_RATE_VALUES[1], (byte) 0x2);
		map4.put(DATA_RATE_VALUES[2], (byte) 0x3);
		map4.put(DATA_RATE_VALUES[3], (byte) 0x4);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[4], map4);

		Map<Integer, Byte> map5 = new HashMap<Integer, Byte>();
		map5.put(DATA_RATE_VALUES[0], (byte) 0x2);
		map5.put(DATA_RATE_VALUES[1], (byte) 0x3);
		map5.put(DATA_RATE_VALUES[2], (byte) 0x4);
		map5.put(DATA_RATE_VALUES[3], (byte) 0x5);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[5], map5);

		Map<Integer, Byte> map6 = new HashMap<Integer, Byte>();
		map6.put(DATA_RATE_VALUES[0], (byte) 0x3);
		map6.put(DATA_RATE_VALUES[1], (byte) 0x4);
		map6.put(DATA_RATE_VALUES[2], (byte) 0x5);
		map6.put(DATA_RATE_VALUES[3], (byte) 0x6);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[6], map6);

		Map<Integer, Byte> map7 = new HashMap<Integer, Byte>();
		map7.put(DATA_RATE_VALUES[0], (byte) 0x4);
		map7.put(DATA_RATE_VALUES[1], (byte) 0x5);
		map7.put(DATA_RATE_VALUES[2], (byte) 0x6);
		map7.put(DATA_RATE_VALUES[3], (byte) 0x7);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[7], map7);

		Map<Integer, Byte> map8 = new HashMap<Integer, Byte>();
		map8.put(DATA_RATE_VALUES[0], (byte) 0x5);
		map8.put(DATA_RATE_VALUES[1], (byte) 0x6);
		map8.put(DATA_RATE_VALUES[2], (byte) 0x7);
		map8.put(DATA_RATE_VALUES[3], (byte) 0x8);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[8], map8);

		Map<Integer, Byte> map9 = new HashMap<Integer, Byte>();
		map9.put(DATA_RATE_VALUES[0], (byte) 0x6);
		map9.put(DATA_RATE_VALUES[1], (byte) 0x7);
		map9.put(DATA_RATE_VALUES[2], (byte) 0x8);
		map9.put(DATA_RATE_VALUES[3], (byte) 0x9);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[9], map9);

		Map<Integer, Byte> map10 = new HashMap<Integer, Byte>();
		map10.put(DATA_RATE_VALUES[0], (byte) 0x7);
		map10.put(DATA_RATE_VALUES[1], (byte) 0x8);
		map10.put(DATA_RATE_VALUES[2], (byte) 0x9);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[10], map10);

		Map<Integer, Byte> map11 = new HashMap<Integer, Byte>();
		map11.put(DATA_RATE_VALUES[0], (byte) 0x8);
		map11.put(DATA_RATE_VALUES[1], (byte) 0x9);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[11], map11);

		Map<Integer, Byte> map12 = new HashMap<Integer, Byte>();
		map12.put(DATA_RATE_VALUES[0], (byte) 0x9);
		HighPassCutOffMap.put(HIGHPASS_FILTER_CUTOFF_FREQUENCY_VALUES[12], map12);
	}
}
