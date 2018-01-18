package analogdigitalconverter;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import static utils.StringUtils.lpad;

/**
 * Read an MCP3008, Analog to Digital Converter
 */
public class ADCReader {
	private final static boolean DISPLAY_DIGIT = false;
	private final static boolean DEBUG = false;
	// Note: "Mismatch" 23-24. The wiring says DOUT->#23, DIN->#24
	// 23: DOUT on the ADC is IN on the GPIO. ADC:Slave, GPIO:Master
	// 24: DIN on the ADC, OUT on the GPIO. Same reason as above.
	// SPI: Serial Peripheral Interface
	private static Pin spiClk = RaspiPin.GPIO_01; // Pin #18, clock
	private static Pin spiMiso = RaspiPin.GPIO_04; // Pin #23, data in.  MISO: Master In Slave Out
	private static Pin spiMosi = RaspiPin.GPIO_05; // Pin #24, data out. MOSI: Master Out Slave In
	private static Pin spiCs = RaspiPin.GPIO_06; // Pin #25, Chip Select

	private enum MCP3008_input_channels {
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

	private static int ADC_CHANNEL = MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static GpioPinDigitalInput misoInput = null;
	private static GpioPinDigitalOutput mosiOutput = null;
	private static GpioPinDigitalOutput clockOutput = null;
	private static GpioPinDigitalOutput chipSelectOutput = null;

	private static boolean go = true;

	public static void main(String... args) {
		GpioController gpio = GpioFactory.getInstance();
		mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.LOW);

		misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down.");
			go = false;
		}));
		int lastRead = 0;
		int tolerance = 5;
		while (go) {
			boolean trimPotChanged = false;
			int adc = readAdc();
			int postAdjust = Math.abs(adc - lastRead);
			if (postAdjust > tolerance) {
				trimPotChanged = true;
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
		gpio.shutdown();
	}

	private static int readAdc() {
		chipSelectOutput.high();

		clockOutput.low();
		chipSelectOutput.low();

		int adccommand = ADC_CHANNEL;
		adccommand |= 0x18; // 0x18: 00011000
		adccommand <<= 3;
		// Send 5 bits: 8 - 3. 8 input channels on the MCP3008.
		for (int i = 0; i < 5; i++) //
		{
			if ((adccommand & 0x80) != 0x0) { // 0x80 = 0&10000000
				mosiOutput.high();
			} else {
				mosiOutput.low();
			}
			adccommand <<= 1;
			clockOutput.high();
			clockOutput.low();
		}

		int adcOut = 0;
		for (int i = 0; i < 12; i++) // Read in one empty bit, one null bit and 10 ADC bits
		{
			clockOutput.high();
			clockOutput.low();
			adcOut <<= 1;

			if (misoInput.isHigh()) {
//      System.out.println("    " + misoInput.getName() + " is high (i:" + i + ")");
				// Shift one bit on the adcOut
				adcOut |= 0x1;
			}
			if (DISPLAY_DIGIT) {
				System.out.println("ADCOUT: 0x" + Integer.toString(adcOut, 16).toUpperCase() +
						", 0&" + Integer.toString(adcOut, 2).toUpperCase());
			}
		}
		chipSelectOutput.high();

		adcOut >>= 1; // Drop first bit
		return adcOut;
	}
}
