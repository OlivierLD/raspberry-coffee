package lora;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This one has an SPI interface
 *
 * Something to checkout too: https://github.com/tftelkamp/single_chan_pkt_fwd
 * https://www.hackster.io/ChrisSamuelson/lora-raspberry-pi-single-channel-gateway-cheap-d57d36
 *
 */
public class RFM95W {
	private final static boolean verbose = false;
	private static GpioController gpio;

	private static GpioPinDigitalInput misoInput = null;
	private static GpioPinDigitalOutput mosiOutput = null;
	private static GpioPinDigitalOutput clockOutput = null;
	private static GpioPinDigitalOutput chipSelectOutput = null;

	// This is the maximum number of interrupts the driver can support
// Most Arduinos can handle 2, Megas can handle more
	private final static int RH_RF95_NUM_INTERRUPTS = 3;

	// Max number of octets the LORA Rx/Tx FIFO can hold
	private final static int RH_RF95_FIFO_SIZE = 255;

	// This is the maximum number of bytes that can be carried by the LORA.
// We use some for headers, keeping fewer for RadioHead messages
	private final static int RH_RF95_MAX_PAYLOAD_LEN = RH_RF95_FIFO_SIZE;

	// The length of the headers we add.
// The headers are inside the LORA's payload
	private final static int RH_RF95_HEADER_LEN = 4;

	// This is the maximum message length that can be supported by this driver.
// Can be pre-defined to a smaller size (to save SRAM) prior to including this header
// Here we allow for 1 byte message length, 4 bytes headers, user data and 2 bytes of FCS
//        #ifndef RH_RF95_MAX_MESSAGE_LEN
	private final static int RH_RF95_MAX_MESSAGE_LEN = (RH_RF95_MAX_PAYLOAD_LEN - RH_RF95_HEADER_LEN);
// #endif

	// The crystal oscillator frequency of the module
	private final static int RH_RF95_FXOSC = 32_000_000;

	// The Frequency Synthesizer step = RH_RF95_FXOSC / 2^^19
	private final static int RH_RF95_FSTEP = (RH_RF95_FXOSC / 524_288);


	// Register names (LoRa Mode, from table 85)
	private final static int RH_RF95_REG_00_FIFO = 0x00;
	private final static int RH_RF95_REG_01_OP_MODE = 0x01;
	private final static int RH_RF95_REG_02_RESERVED = 0x02;
	private final static int RH_RF95_REG_03_RESERVED = 0x03;
	private final static int RH_RF95_REG_04_RESERVED = 0x04;
	private final static int RH_RF95_REG_05_RESERVED = 0x05;
	private final static int RH_RF95_REG_06_FRF_MSB = 0x06;
	private final static int RH_RF95_REG_07_FRF_MID = 0x07;
	private final static int RH_RF95_REG_08_FRF_LSB = 0x08;
	private final static int RH_RF95_REG_09_PA_CONFIG = 0x09;
	private final static int RH_RF95_REG_0A_PA_RAMP = 0x0a;
	private final static int RH_RF95_REG_0B_OCP = 0x0b;
	private final static int RH_RF95_REG_0C_LNA = 0x0c;
	private final static int RH_RF95_REG_0D_FIFO_ADDR_PTR = 0x0d;
	private final static int RH_RF95_REG_0E_FIFO_TX_BASE_ADDR = 0x0e;
	private final static int RH_RF95_REG_0F_FIFO_RX_BASE_ADDR = 0x0f;
	private final static int RH_RF95_REG_10_FIFO_RX_CURRENT_ADDR = 0x10;
	private final static int RH_RF95_REG_11_IRQ_FLAGS_MASK = 0x11;
	private final static int RH_RF95_REG_12_IRQ_FLAGS = 0x12;
	private final static int RH_RF95_REG_13_RX_NB_BYTES = 0x13;
	private final static int RH_RF95_REG_14_RX_HEADER_CNT_VALUE_MSB = 0x14;
	private final static int RH_RF95_REG_15_RX_HEADER_CNT_VALUE_LSB = 0x15;
	private final static int RH_RF95_REG_16_RX_PACKET_CNT_VALUE_MSB = 0x16;
	private final static int RH_RF95_REG_17_RX_PACKET_CNT_VALUE_LSB = 0x17;
	private final static int RH_RF95_REG_18_MODEM_STAT = 0x18;
	private final static int RH_RF95_REG_19_PKT_SNR_VALUE = 0x19;
	private final static int RH_RF95_REG_1A_PKT_RSSI_VALUE = 0x1a;
	private final static int RH_RF95_REG_1B_RSSI_VALUE = 0x1b;
	private final static int RH_RF95_REG_1C_HOP_CHANNEL = 0x1c;
	private final static int RH_RF95_REG_1D_MODEM_CONFIG1 = 0x1d;
	private final static int RH_RF95_REG_1E_MODEM_CONFIG2 = 0x1e;
	private final static int RH_RF95_REG_1F_SYMB_TIMEOUT_LSB = 0x1f;
	private final static int RH_RF95_REG_20_PREAMBLE_MSB = 0x20;
	private final static int RH_RF95_REG_21_PREAMBLE_LSB = 0x21;
	private final static int RH_RF95_REG_22_PAYLOAD_LENGTH = 0x22;
	private final static int RH_RF95_REG_23_MAX_PAYLOAD_LENGTH = 0x23;
	private final static int RH_RF95_REG_24_HOP_PERIOD = 0x24;
	private final static int RH_RF95_REG_25_FIFO_RX_BYTE_ADDR = 0x25;
	private final static int RH_RF95_REG_26_MODEM_CONFIG3 = 0x26;

	private final static int RH_RF95_REG_27_PPM_CORRECTION = 0x27;
	private final static int RH_RF95_REG_28_FEI_MSB = 0x28;
	private final static int RH_RF95_REG_29_FEI_MID = 0x29;
	private final static int RH_RF95_REG_2A_FEI_LSB = 0x2a;
	private final static int RH_RF95_REG_2C_RSSI_WIDEBAND = 0x2c;
	private final static int RH_RF95_REG_31_DETECT_OPTIMIZ = 0x31;
	private final static int RH_RF95_REG_33_INVERT_IQ = 0x33;
	private final static int RH_RF95_REG_37_DETECTION_THRESHOLD = 0x37;
	private final static int RH_RF95_REG_39_SYNC_WORD = 0x39;

	private final static int RH_RF95_REG_40_DIO_MAPPING1 = 0x40;
	private final static int RH_RF95_REG_41_DIO_MAPPING2 = 0x41;
	private final static int RH_RF95_REG_42_VERSION = 0x42;

	private final static int RH_RF95_REG_4B_TCXO = 0x4b;
	private final static int RH_RF95_REG_4D_PA_DAC = 0x4d;
	private final static int RH_RF95_REG_5B_FORMER_TEMP = 0x5b;
	private final static int RH_RF95_REG_61_AGC_REF = 0x61;
	private final static int RH_RF95_REG_62_AGC_THRESH1 = 0x62;
	private final static int RH_RF95_REG_63_AGC_THRESH2 = 0x63;
	private final static int RH_RF95_REG_64_AGC_THRESH3 = 0x64;

	// RH_RF95_REG_01_OP_MODE                             0x01
	private final static int RH_RF95_LONG_RANGE_MODE = 0x80;
	private final static int RH_RF95_ACCESS_SHARED_REG = 0x40;
	private final static int RH_RF95_LOW_FREQUENCY_MODE = 0x08;
	private final static int RH_RF95_MODE = 0x07;
	private final static int RH_RF95_MODE_SLEEP = 0x00;
	private final static int RH_RF95_MODE_STDBY = 0x01;
	private final static int RH_RF95_MODE_FSTX = 0x02;
	private final static int RH_RF95_MODE_TX = 0x03;
	private final static int RH_RF95_MODE_FSRX = 0x04;
	private final static int RH_RF95_MODE_RXCONTINUOUS = 0x05;
	private final static int RH_RF95_MODE_RXSINGLE = 0x06;
	private final static int RH_RF95_MODE_CAD = 0x07;

	// RH_RF95_REG_09_PA_CONFIG                           0x09
	private final static int RH_RF95_PA_SELECT = 0x80;
	private final static int RH_RF95_MAX_POWER = 0x70;
	private final static int RH_RF95_OUTPUT_POWER = 0x0f;

	// RH_RF95_REG_0A_PA_RAMP                             0x0a
	private final static int RH_RF95_LOW_PN_TX_PLL_OFF = 0x10;
	private final static int RH_RF95_PA_RAMP = 0x0f;
	private final static int RH_RF95_PA_RAMP_3_4MS = 0x00;
	private final static int RH_RF95_PA_RAMP_2MS = 0x01;
	private final static int RH_RF95_PA_RAMP_1MS = 0x02;
	private final static int RH_RF95_PA_RAMP_500US = 0x03;
	private final static int RH_RF95_PA_RAMP_250US = 0x0;
	private final static int RH_RF95_PA_RAMP_125US = 0x05;
	private final static int RH_RF95_PA_RAMP_100US = 0x06;
	private final static int RH_RF95_PA_RAMP_62US = 0x07;
	private final static int RH_RF95_PA_RAMP_50US = 0x08;
	private final static int RH_RF95_PA_RAMP_40US = 0x09;
	private final static int RH_RF95_PA_RAMP_31US = 0x0a;
	private final static int RH_RF95_PA_RAMP_25US = 0x0b;
	private final static int RH_RF95_PA_RAMP_20US = 0x0c;
	private final static int RH_RF95_PA_RAMP_15US = 0x0d;
	private final static int RH_RF95_PA_RAMP_12US = 0x0e;
	private final static int RH_RF95_PA_RAMP_10US = 0x0f;

	// RH_RF95_REG_0B_OCP                                 0x0b
	private final static int RH_RF95_OCP_ON = 0x20;
	private final static int RH_RF95_OCP_TRIM = 0x1f;

	// RH_RF95_REG_0C_LNA                                 0x0c
	private final static int RH_RF95_LNA_GAIN = 0xe0;
	private final static int RH_RF95_LNA_GAIN_G1 = 0x20;
	private final static int RH_RF95_LNA_GAIN_G2 = 0x40;
	private final static int RH_RF95_LNA_GAIN_G3 = 0x60;
	private final static int RH_RF95_LNA_GAIN_G4 = 0x80;
	private final static int RH_RF95_LNA_GAIN_G5 = 0xa0;
	private final static int RH_RF95_LNA_GAIN_G6 = 0xc0;
	private final static int RH_RF95_LNA_BOOST_LF = 0x18;
	private final static int RH_RF95_LNA_BOOST_LF_DEFAULT = 0x00;
	private final static int RH_RF95_LNA_BOOST_HF = 0x03;
	private final static int RH_RF95_LNA_BOOST_HF_DEFAULT = 0x00;
	private final static int RH_RF95_LNA_BOOST_HF_150PC = 0x11;

	// RH_RF95_REG_11_IRQ_FLAGS_MASK                      0x11
	private final static int RH_RF95_RX_TIMEOUT_MASK = 0x80;
	private final static int RH_RF95_RX_DONE_MASK = 0x40;
	private final static int RH_RF95_PAYLOAD_CRC_ERROR_MASK = 0x20;
	private final static int RH_RF95_VALID_HEADER_MASK = 0x10;
	private final static int RH_RF95_TX_DONE_MASK = 0x08;
	private final static int RH_RF95_CAD_DONE_MASK = 0x04;
	private final static int RH_RF95_FHSS_CHANGE_CHANNEL_MASK = 0x02;
	private final static int RH_RF95_CAD_DETECTED_MASK = 0x01;

	// RH_RF95_REG_12_IRQ_FLAGS                           0x12
	private final static int RH_RF95_RX_TIMEOUT = 0x80;
	private final static int RH_RF95_RX_DONE = 0x40;
	private final static int RH_RF95_PAYLOAD_CRC_ERROR = 0x20;
	private final static int RH_RF95_VALID_HEADER = 0x10;
	private final static int RH_RF95_TX_DONE = 0x08;
	private final static int RH_RF95_CAD_DONE = 0x04;
	private final static int RH_RF95_FHSS_CHANGE_CHANNEL = 0x02;
	private final static int RH_RF95_CAD_DETECTED = 0x01;

	// RH_RF95_REG_18_MODEM_STAT                          0x18
	private final static int RH_RF95_RX_CODING_RATE = 0xe0;
	private final static int RH_RF95_MODEM_STATUS_CLEAR = 0x10;
	private final static int RH_RF95_MODEM_STATUS_HEADER_INFO_VALID = 0x08;
	private final static int RH_RF95_MODEM_STATUS_RX_ONGOING = 0x04;
	private final static int RH_RF95_MODEM_STATUS_SIGNAL_SYNCHRONIZED = 0x02;
	private final static int RH_RF95_MODEM_STATUS_SIGNAL_DETECTED = 0x01;

	// RH_RF95_REG_1C_HOP_CHANNEL                         0x1c
	private final static int RH_RF95_PLL_TIMEOUT = 0x80;
	private final static int RH_RF95_RX_PAYLOAD_CRC_IS_ON = 0x40;
	private final static int RH_RF95_FHSS_PRESENT_CHANNEL = 0x3f;

	// RH_RF95_REG_1D_MODEM_CONFIG1                       0x1d
	private final static int RH_RF95_BW = 0xf0;

	private final static int RH_RF95_BW_7_8KHZ = 0x00;
	private final static int RH_RF95_BW_10_4KHZ = 0x10;
	private final static int RH_RF95_BW_15_6KHZ = 0x20;
	private final static int RH_RF95_BW_20_8KHZ = 0x30;
	private final static int RH_RF95_BW_31_25KHZ = 0x40;
	private final static int RH_RF95_BW_41_7KHZ = 0x50;
	private final static int RH_RF95_BW_62_5KHZ = 0x60;
	private final static int RH_RF95_BW_125KHZ = 0x70;
	private final static int RH_RF95_BW_250KHZ = 0x80;
	private final static int RH_RF95_BW_500KHZ = 0x90;
	private final static int RH_RF95_CODING_RATE = 0x0e;
	private final static int RH_RF95_CODING_RATE_4_5 = 0x02;
	private final static int RH_RF95_CODING_RATE_4_6 = 0x04;
	private final static int RH_RF95_CODING_RATE_4_7 = 0x06;
	private final static int RH_RF95_CODING_RATE_4_8 = 0x08;
	private final static int RH_RF95_IMPLICIT_HEADER_MODE_ON = 0x01;

	// RH_RF95_REG_1E_MODEM_CONFIG2                       0x1e
	private final static int RH_RF95_SPREADING_FACTOR = 0xf0;
	private final static int RH_RF95_SPREADING_FACTOR_64CPS = 0x60;
	private final static int RH_RF95_SPREADING_FACTOR_128CPS = 0x70;
	private final static int RH_RF95_SPREADING_FACTOR_256CPS = 0x80;
	private final static int RH_RF95_SPREADING_FACTOR_512CPS = 0x90;
	private final static int RH_RF95_SPREADING_FACTOR_1024CPS = 0xa0;
	private final static int RH_RF95_SPREADING_FACTOR_2048CPS = 0xb0;
	private final static int RH_RF95_SPREADING_FACTOR_4096CPS = 0xc0;
	private final static int RH_RF95_TX_CONTINUOUS_MOE = 0x08;

	private final static int RH_RF95_PAYLOAD_CRC_ON = 0x04;
	private final static int RH_RF95_SYM_TIMEOUT_MSB = 0x03;

	// RH_RF95_REG_4B_TCXO                                0x4b
	private final static int RH_RF95_TCXO_TCXO_INPUT_ON = 0x10;

	// RH_RF95_REG_4D_PA_DAC                              0x4d
	private final static int RH_RF95_PA_DAC_DISABLE = 0x04;
	private final static int RH_RF95_PA_DAC_ENABLE = 0x07;

	public final static int READWRITE = 0x80;

	private static Pin spiClk = RaspiPin.GPIO_14; // clock (pin #23)
	private static Pin spiMiso = RaspiPin.GPIO_13; // data in.  MISO: Master In Slave Out (pin #21)
	private static Pin spiMosi = RaspiPin.GPIO_12; // data out. MOSI: Master Out Slave In (pin #19)
	private static Pin spiCs = RaspiPin.GPIO_10; // Chip Select (pin #24)

	public RFM95W(int slavePin, int interruptPin) throws Exception { // TODO Manage the parameters
		initRFM95W();

		writeByte(RH_RF95_REG_01_OP_MODE, RH_RF95_MODE_SLEEP | RH_RF95_LONG_RANGE_MODE);
		delay(10);
		// Check we are in sleep mode, with LORA set
		if (readByte(RH_RF95_REG_01_OP_MODE) != (RH_RF95_MODE_SLEEP | RH_RF95_LONG_RANGE_MODE)) {
			System.err.println("Ooops!");
			shutdownRFM95W();
		}
		// TODO Attach interrupt?

		// Setup FIFO
		writeByte(RH_RF95_REG_0E_FIFO_TX_BASE_ADDR, 0);
		writeByte(RH_RF95_REG_0F_FIFO_RX_BASE_ADDR, 0);

		if (verbose) {
			System.out.println("Communication established.");
		}
	}

	private static void initRFM95W() {
		gpio = GpioFactory.getInstance();
		mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);

		misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");
	}

	public static void shutdownRFM95W() {
		gpio.shutdown();
	}

	private static int mkInt16(int val) {
		int ret = val & 0x7FFF;
		if (val > 0x7FFF) {
			ret -= 0x8000;
		}
//    if (verbose) {
//      System.out.println(val + " becomes " + ret);
//	  }
		return ret;
	}

	private static int mkUInt16(int val) {
		int ret = val & 0xFFFF;
		return ret;
	}

	private final int MASK = 0x80; // MSBFIRST, 0x80 = 0&10000000
//private final int MASK = 0x01; // LSBFIRST

	private final static int WRITE = 0;
	private final static int READ = 1;

	/**
	 * @param addr   Register
	 * @param value  value to write
	 * @param rw     READ or WRITE
	 * @param length length in <b><u><i>bits</i></u></b>
	 * @return
	 */
	private int spiTransfer(int addr, int value, int rw, int length) {
		// Bit banging at address "addr", "rw" indicates READ (1) or WRITE (0) operation
		int retValue = 0;
		int spiAddr;
		if (rw == WRITE) {
			spiAddr = addr & (~READWRITE);
		} else {
			spiAddr = addr | READWRITE;
		}
		// System.out.println("SPI ADDR: 0x" + Integer.toHexString(spiAddr) + ", mode:" + rw);

		chipSelectOutput.low();
//  waitFor(DELAY);
		for (int i = 0; i < 8; i++) {
			int bit = spiAddr & (0x01 << (7 - i));
			if (bit != 0) {
				mosiOutput.high();
			} else {
				mosiOutput.low();
			}
			clockOutput.low();
//    delay(DELAY);
			clockOutput.high();
//    delay(DELAY);
		}
		if (rw == READ) {
			for (int i = 0; i < length; i++) {
				clockOutput.low();
//      delay(DELAY);
				int bit = misoInput.getState().getValue(); // TODO Check that
				clockOutput.high();
				retValue = (retValue << 1) | bit;
//      delay(DELAY);
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
//      delay(DELAY);
				clockOutput.high();
//      delay(DELAY);
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
