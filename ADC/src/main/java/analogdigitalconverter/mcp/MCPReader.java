package analogdigitalconverter.mcp;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import static utils.StringUtils.lpad;

/**
 * Read an SPI Analog to Digital Converter.
 * Suitable for MCP3008 & MCP3002.
 */
public class MCPReader {

	private final static boolean DISPLAY_DIGIT = "true".equals(System.getProperty("display.digit", "false"));
	// Note: "Mismatch" 23-24. The wiring says DOUT->#23, DIN->#24
	// 23: DOUT on the ADC is IN on the GPIO. ADC:Slave, GPIO:Master
	// 24: DIN on the ADC, OUT on the GPIO. Same reason as above.
	// SPI: Serial Peripheral Interface
	private static Pin spiClk  = RaspiPin.GPIO_01; // Pin #18, clock
	private static Pin spiMiso = RaspiPin.GPIO_04; // Pin #23, data in.  MISO: Master In Slave Out
	private static Pin spiMosi = RaspiPin.GPIO_05; // Pin #24, data out. MOSI: Master Out Slave In
	private static Pin spiCs   = RaspiPin.GPIO_06; // Pin #25, Chip Select

	public enum MCPFlavor {
		MCP3008, MCP3002
	}

	private static MCPFlavor adcFlavor = MCPFlavor.MCP3008; // Default

	public enum MCP3008InputChannels {
		CH0(0),
		CH1(1),
		CH2(2),
		CH3(3),
		CH4(4),
		CH5(5),
		CH6(6),
		CH7(7);

		private int ch;

		MCP3008InputChannels(int chNum) {
			this.ch = chNum;
		}

		public int ch() {
			return this.ch;
		}
	}

	public enum MCP3002InputChannels {
		CH0(0),
		CH1(1);

		private int ch;

		MCP3002InputChannels(int chNum) {
			this.ch = chNum;
		}

		public int ch() {
			return this.ch;
		}
	}

	private static GpioController gpio;

	private static GpioPinDigitalInput  misoInput = null;        // In
	private static GpioPinDigitalOutput mosiOutput = null;       // Out
	private static GpioPinDigitalOutput clockOutput = null;      // Out
	private static GpioPinDigitalOutput chipSelectOutput = null; // Out

	public static void initMCP() {
		initMCP(MCPFlavor.MCP3008, spiMiso, spiMosi, spiClk, spiCs);
	}
	public static void initMCP(MCPFlavor adc) {
		initMCP(adc, spiMiso, spiMosi, spiClk, spiCs);
	}
	public static void initMCP(Pin miso, Pin mosi, Pin clk, Pin cs) {
		initMCP(MCPFlavor.MCP3008, miso, mosi, clk, cs);
	}
	public static void initMCP(MCPFlavor adc, Pin miso, Pin mosi, Pin clk, Pin cs) {
		adcFlavor = adc;
		spiMiso = miso;
		spiMosi = mosi;
		spiClk = clk;
		spiCs = cs;

		gpio = GpioFactory.getInstance();
		// Out
		mosiOutput       = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		clockOutput      = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);
		// In
		misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");
	}

	public static void shutdownMCP() {
		gpio.shutdown();
	}

	public static int readMCP(int channel) {

		boolean validChannel = false;
		if (adcFlavor.equals(MCPFlavor.MCP3008)) {
			for (MCPReader.MCP3008InputChannels adcChannel : MCPReader.MCP3008InputChannels.values()) {
				if (adcChannel.ch() == channel) {
					validChannel = true;
					break;
				}
			}
		} else if (adcFlavor.equals(MCPFlavor.MCP3002)) {
			for (MCPReader.MCP3002InputChannels adcChannel : MCPReader.MCP3002InputChannels.values()) {
				if (adcChannel.ch() == channel) {
					validChannel = true;
					break;
				}
			}
		}
		if (!validChannel) {
			throw new IllegalArgumentException(String.format("Non-suitable channel for %s: %d", adcFlavor, channel));
		}

		chipSelectOutput.high();

		clockOutput.low();
		chipSelectOutput.low();

		int adcCommand = channel;
		if (DISPLAY_DIGIT) {
			System.out.println("1 -       ADCCOMMAND: 0x" + lpad(Integer.toString(adcCommand, 16).toUpperCase(), 4, "0") +
					", 0&" + lpad(Integer.toString(adcCommand, 2).toUpperCase(), 16, "0"));
		}
		adcCommand |= 0x18; // 0x18: 00011000
		if (DISPLAY_DIGIT) {
			System.out.println("2 -       ADCCOMMAND: 0x" + lpad(Integer.toString(adcCommand, 16).toUpperCase(), 4, "0") +
					", 0&" + lpad(Integer.toString(adcCommand, 2).toUpperCase(), 16, "0"));
		}
		adcCommand <<= 3;
		if (DISPLAY_DIGIT) {
			System.out.println("3 -       ADCCOMMAND: 0x" + lpad(Integer.toString(adcCommand, 16).toUpperCase(), 4, "0") +
					", 0&" + lpad(Integer.toString(adcCommand, 2).toUpperCase(), 16, "0"));
		}
		// Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
		for (int i = 0; i < 5; i++) {
			if (DISPLAY_DIGIT) {
				System.out.println("4 - (i=" + i + ") ADCCOMMAND: 0x" + lpad(Integer.toString(adcCommand, 16).toUpperCase(), 4, "0") +
						", 0&" + lpad(Integer.toString(adcCommand, 2).toUpperCase(), 16, "0"));
			}
			if ((adcCommand & 0x80) != 0x0) { // 0x80 = 0&10000000
				mosiOutput.high();
			} else {
				mosiOutput.low();
			}
			adcCommand <<= 1;
			// Clock high and low
			tickOnPin(clockOutput);
		}

		int adcOut = 0;
		for (int i = 0; i < 12; i++) { // Read in one empty bit, one null bit and 10 ADC bits
			tickOnPin(clockOutput);
			adcOut <<= 1;

			if (misoInput.isHigh()) {
//      System.out.println("    " + misoInput.getName() + " is high (i:" + i + ")");
				// Shift one bit on the adcOut
				adcOut |= 0x1;
			}
			if (DISPLAY_DIGIT) {
				System.out.println("ADCOUT: 0x" + lpad(Integer.toString(adcOut, 16).toUpperCase(), 4, "0") +
						", 0&" + lpad(Integer.toString(adcOut, 2).toUpperCase(), 16, "0"));
			}
		}
		chipSelectOutput.high();

		adcOut >>= 1; // Drop first bit
		return adcOut;
	}

	private static void tickOnPin(GpioPinDigitalOutput pin) {
		pin.high();
		pin.low();
	}
}
