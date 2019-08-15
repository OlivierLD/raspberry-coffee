package analogdigitalconverter;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

import static utils.StringUtils.lpad;

/**
 * Read an MCP3008, Analog to Digital Converter
 */
public class ADCReader {
	private final static boolean DISPLAY_DIGIT = false;
	private final static boolean DEBUG = "true".equals(System.getProperty("adc.verbose"));
	// Note: "Mismatch" 23-24. The wiring says DOUT->#23, DIN->#24
	// 23: DOUT on the ADC is IN on the GPIO. ADC:Slave, GPIO:Master
	// 24: DIN on the ADC, OUT on the GPIO. Same reason as above.
	// SPI: Serial Peripheral Interface
	private final static Pin DEFAULT_CLK  = RaspiPin.GPIO_14;
	private final static Pin DEFAULT_MISO = RaspiPin.GPIO_13;
	private final static Pin DEFAULT_MOSI = RaspiPin.GPIO_12;
	private final static Pin DEFAULT_CS   = RaspiPin.GPIO_10;

	private Pin spiClk  = null;
	private Pin spiMiso = null;
	private Pin spiMosi = null;
	private Pin spiCs   = null;

	public enum MCP3008_input_channels {
		CH0(0),
		CH1(1),
		CH2(2),
		CH3(3),
		CH4(4),
		CH5(5),
		CH6(6),
		CH7(7);

		private int ch;

		MCP3008_input_channels(int chNum) {
			this.ch = chNum;
		}

		public int ch() {
			return this.ch;
		}
	}

	private GpioPinDigitalInput misoInput = null;
	private GpioPinDigitalOutput mosiOutput = null;
	private GpioPinDigitalOutput clockOutput = null;
	private GpioPinDigitalOutput chipSelectOutput = null;

	GpioController gpio = null;

	public ADCReader() {
		this(DEFAULT_MISO, DEFAULT_MOSI, DEFAULT_CLK, DEFAULT_CS);
	}

	public ADCReader(Pin miso, Pin mosi, Pin clock, Pin cs) {
		this.spiMiso = miso;
		this.spiMosi = mosi;
		this.spiClk = clock;
		this.spiCs = cs;

		this.gpio = GpioFactory.getInstance();
		this.mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		this.clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		this.chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);

		this.misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");

		if (DEBUG) {
			String[] map = new String[4];
			map[0] = String.valueOf(PinUtil.findByPin(spiMosi).pinNumber()) + ":" + "MOSI";
			map[1] = String.valueOf(PinUtil.findByPin(spiMiso).pinNumber()) + ":" + "MISO";
			map[2] = String.valueOf(PinUtil.findByPin(spiClk).pinNumber()) + ":" + "CLK";
			map[3] = String.valueOf(PinUtil.findByPin(spiCs).pinNumber()) + ":" + "CS";
			PinUtil.print(map);
		}
	}

	public int readAdc(int channel) {
		this.chipSelectOutput.high();

		this.clockOutput.low();
		this.chipSelectOutput.low();

		int adccommand = channel;
		adccommand |= 0x18; // 0x18: 00011000
		adccommand <<= 3;
		// Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
		for (int i = 0; i < 5; i++) {
			if ((adccommand & 0x80) != 0x0) { // 0x80 = 0&10000000
				this.mosiOutput.high();
			} else {
				this.mosiOutput.low();
			}
			adccommand <<= 1;
			this.clockOutput.high();
			this.clockOutput.low();
		}

		int adcOut = 0;
		for (int i = 0; i < 12; i++) { // Read in one empty bit, one null bit and 10 ADC bits
			this.clockOutput.high();
			this.clockOutput.low();
			adcOut <<= 1;

			if (this.misoInput.isHigh()) {
//      System.out.println("    " + misoInput.getName() + " is high (i:" + i + ")");
				// Shift one bit on the adcOut
				adcOut |= 0x1;
			}
			if (DISPLAY_DIGIT) {
				System.out.println("ADCOUT: 0x" + Integer.toString(adcOut, 16).toUpperCase() +
						", 0&" + Integer.toString(adcOut, 2).toUpperCase());
			}
		}
		this.chipSelectOutput.high();

		adcOut >>= 1; // Drop first bit
		return adcOut;
	}

	public void closeReader() {
		if (this.gpio != null) {
			this.gpio.shutdown();
		}
	}

	// Example
	private static boolean go = true;
	public static void main(String... args) throws Exception {

		int channel = MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008
		if (args.length == 1) {
			channel = Integer.parseInt(args[0]);
			if (channel < 0 || channel > 7) {
				throw new IllegalArgumentException("Channel must be in [0..7]");
			}
		}
		ADCReader mcp3008 = new ADCReader();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down.");
			go = false;
		}, "Shutdown Hook"));
		int lastRead = 0;
		int tolerance = 1; // This is used for damping...
		System.out.println("Reading.");
		while (go) {
			int adc = mcp3008.readAdc(channel); // [0..1023]
			int postAdjust = Math.abs(adc - lastRead);
			if (DEBUG || postAdjust > tolerance) {
				int volume = (int) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if (DEBUG) {
					System.out.println("readAdc:" + Integer.toString(adc) +
							" (0x" + lpad(Integer.toString(adc, 16).toUpperCase(), 2, "0") +
							", 0&" + lpad(Integer.toString(adc, 2), 8, "0") + ")");
				}
				System.out.println("Volume:" + volume + "% (" + adc + ")");
				lastRead = adc;
			}
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println("Bye...");
		mcp3008.closeReader();
	}

}
