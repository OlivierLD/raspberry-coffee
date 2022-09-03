package spi.lcd.nokia;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;

import static utils.TimeUtil.delay;

/**
 * see https://learn.adafruit.com/nokia-5110-3310-monochrome-lcd?view=all
 * see https://learn.adafruit.com/nokia-5110-3310-lcd-python-library?view=all for the wiring
 *
 * Run Nokia5110Sample to see the wiring...
 *
 */
public class Nokia5110 {
	public final static int LCDWIDTH = 84;
	public final static int LCDHEIGHT = 48;
	public final static int ROWPIXELS = LCDHEIGHT / 6;
	public final static int PCD8544_POWERDOWN = 0x04;
	public final static int PCD8544_ENTRYMODE = 0x02;
	public final static int PCD8544_EXTENDEDINSTRUCTION = 0x01;
	public final static int PCD8544_DISPLAYBLANK = 0x00;
	public final static int PCD8544_DISPLAYNORMAL = 0x04;
	public final static int PCD8544_DISPLAYALLON = 0x01;
	public final static int PCD8544_DISPLAYINVERTED = 0x05;
	public final static int PCD8544_FUNCTIONSET = 0x20;
	public final static int PCD8544_DISPLAYCONTROL = 0x08;
	public final static int PCD8544_SETYADDR = 0x40;
	public final static int PCD8544_SETXADDR = 0x80;
	public final static int PCD8544_SETTEMP = 0x04;
	public final static int PCD8544_SETBIAS = 0x10;
	public final static int PCD8544_SETVOP = 0x80;

	private int[] buffer = new int[LCDWIDTH * ROWPIXELS];

	// SPI: Serial Peripheral Interface. Default pin values.
	private static Pin spiDc = RaspiPin.GPIO_04;   // Pin #16, GPIO_23
	private static Pin spiRst = RaspiPin.GPIO_05;  // Pin #18, GPIO_24
	private static Pin spiCs = RaspiPin.GPIO_10;   // Pin #24, SPI0_CE0_N
	private static Pin spiClk = RaspiPin.GPIO_14;  // Pin #23, SCLK, GPIO_11
	private static Pin spiMosi = RaspiPin.GPIO_12; // Pin #19, SPI0_MOSI

	private final static int SPI_DEVICE = Spi.CHANNEL_0; // 0

	private static GpioController gpio;
	private int clockHertz = 4_000_000; // 4 MHz

	private static GpioPinDigitalOutput mosiOutput = null;
	private static GpioPinDigitalOutput clockOutput = null;
	private static GpioPinDigitalOutput chipSelectOutput = null;
	private static GpioPinDigitalOutput resetOutput = null;
	private static GpioPinDigitalOutput dcOutput = null;

	private final boolean verbose = "true".equals(System.getProperty("verbose", "false"));

	public Nokia5110() {
		init();
	}

	public void init() {
		int fd = Spi.wiringPiSPISetup(SPI_DEVICE, clockHertz);
		if (fd < 0) {
			System.err.println("SPI Setup failed");
			System.exit(1);
		} else {
			if (verbose) {
				System.out.println("DEBUG: SPI Setup OK");
			}
		}

		gpio = GpioFactory.getInstance();
		// Spi BitBang?

		mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
		clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
		chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.HIGH);
		resetOutput = gpio.provisionDigitalOutputPin(spiRst, "RST", PinState.LOW);
		dcOutput = gpio.provisionDigitalOutputPin(spiDc, "DC", PinState.LOW);
	}

	private final int MASK = 0x80; // MSBFIRST, 0x80 = 0&10000000
//private final int MASK = 0x01; // LSBFIRST

	private void write(int[] data) {
		// Fail if MOSI is not specified.
		if (mosiOutput == null) {
			throw new RuntimeException("Write attempted with no MOSI pin specified.");
		}
		if (chipSelectOutput != null) {
			chipSelectOutput.low();
		}
		for (int i = 0; i < data.length; i++) {
			byte b = (byte) data[i];
			for (int j = 0; j < 8; j++) {
				byte bit = (byte) ((b << j) & MASK);
				// Write bit to MOSI.
				if (bit != 0) {
					mosiOutput.high();
				} else {
					mosiOutput.low();
				}
				// Flip clock off base. // TODO Check the value of the base (LOW Here)
				clockOutput.high();
				// Return clock to base.
				clockOutput.low();
			}
		}
		if (chipSelectOutput != null) {
			chipSelectOutput.high();
		}
	}

	private void command(int c) {
		dcOutput.low();
		this.write(new int[]{(byte) c});
	}

	private void extendedCommand(int c) {
		// Set extended command mode
		this.command(PCD8544_FUNCTIONSET | PCD8544_EXTENDEDINSTRUCTION);
		this.command(c);
		// Set normal display mode.
		this.command(PCD8544_FUNCTIONSET);
		this.command(PCD8544_DISPLAYCONTROL | PCD8544_DISPLAYNORMAL);
	}

	private void reset() {
		// Set reset low for 0.1 sec.
		resetOutput.low();
		delay(100);
		// Set reset high again.
		resetOutput.high();
	}

	public void data(byte c) {
		dcOutput.high();
		this.write(new int[]{c});
	}

	public void data(int[] c) {
		dcOutput.high();
		this.write(c);
	}

	/**
	 * Initialize display
	 */
	public void begin() {
		begin(40, 4);
	}

	public void begin(int contrast, int bias) {
		// Reset and initialize display.
		this.reset();
		this.setBias(bias);
		this.setContrast(contrast);
		if (verbose) {
			System.out.println("DEBUG: begin OK");
		}
	}

	public void setScreenBuffer(int[] sb) {
		this.buffer = sb;
	}

	public int[] getScreenBuffer() {
		return this.buffer;
	}

	public void clear() {
		for (int i = 0; this.buffer != null && i < this.buffer.length; i++) {
			this.buffer[i] = 0x0;
		}
	}

	public void setContrast(int contrast)
			throws IllegalArgumentException {
		if (contrast < 0 || contrast > 127) {
			throw new IllegalArgumentException("Contrast must be a value in [0, 127]");
		}
		int _contrast = Math.max(0, Math.min(contrast, 0x7F));
		this.extendedCommand(PCD8544_SETVOP | _contrast);
		if (verbose) {
			System.out.println("DEBUG: setContrast OK");
		}
	}

	public void setBias(int bias) {
		this.extendedCommand(PCD8544_SETBIAS | bias);
		if (verbose) {
			System.out.println("DEBUG: setBias OK");
		}
	}

	/**
	 * Write display buffer to physical display.
	 */
	public void display() {
		this.command(PCD8544_SETYADDR);
		this.command(PCD8544_SETXADDR);
		// Write buffer data.
		//   Set DC high for data.
		dcOutput.high();
		this.write(this.buffer);
		if (verbose) {
			System.out.println("DEBUG: display OK");
		}
	}

	public void shutdown() {
		gpio.shutdown();
	}
}
