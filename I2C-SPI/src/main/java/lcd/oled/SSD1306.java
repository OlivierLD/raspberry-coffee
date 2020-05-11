package lcd.oled;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.Spi;
import utils.PinUtil;

import java.io.IOException;

import static utils.TimeUtil.delay;

/**
 * SSD1306, small OLED screen. SPI and I2C. 128x32, 128x64 (2 versions)
 *
 * This code is common to both I2C or SPI interface. See the different constructors.
 * Adapted from the Arduino code from Adafruit.
 *
 * Good and tested for:
 * https://www.adafruit.com/product/938,
 * https://www.adafruit.com/product/3527
 * https://www.adafruit.com/product/326
 * https://www.adafruit.com/product/931
 * https://www.adafruit.com/product/661
 */
public class  SSD1306 {
	public final static int SSD1306_I2C_ADDRESS                          = 0x3C; // 011110+SA0+RW - 0x3C or 0x3D
	public final static int SSD1306_SETCONTRAST                          = 0x81;
	public final static int SSD1306_DISPLAYALLON_RESUME                  = 0xA4;
	public final static int SSD1306_DISPLAYALLON                         = 0xA5;
	public final static int SSD1306_NORMALDISPLAY                        = 0xA6;
	public final static int SSD1306_INVERTDISPLAY                        = 0xA7;
	public final static int SSD1306_DISPLAYOFF                           = 0xAE;
	public final static int SSD1306_DISPLAYON                            = 0xAF;
	public final static int SSD1306_SETDISPLAYOFFSET                     = 0xD3;
	public final static int SSD1306_SETCOMPINS                           = 0xDA;
	public final static int SSD1306_SETVCOMDETECT                        = 0xDB;
	public final static int SSD1306_SETDISPLAYCLOCKDIV                   = 0xD5;
	public final static int SSD1306_SETPRECHARGE                         = 0xD9;
	public final static int SSD1306_SETMULTIPLEX                         = 0xA8;
	public final static int SSD1306_SETLOWCOLUMN                         = 0x00;
	public final static int SSD1306_SETHIGHCOLUMN                        = 0x10;
	public final static int SSD1306_SETSTARTLINE                         = 0x40;
	public final static int SSD1306_MEMORYMODE                           = 0x20;
	public final static int SSD1306_COLUMNADDR                           = 0x21;
	public final static int SSD1306_PAGEADDR                             = 0x22;
	public final static int SSD1306_COMSCANINC                           = 0xC0;
	public final static int SSD1306_COMSCANDEC                           = 0xC8;
	public final static int SSD1306_SEGREMAP                             = 0xA0;
	public final static int SSD1306_CHARGEPUMP                           = 0x8D;
	public final static int SSD1306_EXTERNALVCC                          = 0x01;
	public final static int SSD1306_SWITCHCAPVCC                         = 0x02;

	// Scrolling constants
	public final static int SSD1306_ACTIVATE_SCROLL                      = 0x2F;
	public final static int SSD1306_DEACTIVATE_SCROLL                    = 0x2E;
	public final static int SSD1306_SET_VERTICAL_SCROLL_AREA             = 0xA3;
	public final static int SSD1306_RIGHT_HORIZONTAL_SCROLL              = 0x26;
	public final static int SSD1306_LEFT_HORIZONTAL_SCROLL               = 0x27;
	public final static int SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;
	public final static int SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL  = 0x2A;

	//private final static int SPI_PORT =  0;
	private final static int SPI_DEVICE = Spi.CHANNEL_0; // 0
  private final static int DEFAULT_WIDTH = 128,
		                       DEFAULT_HEIGHT = 32;

	private int width = DEFAULT_WIDTH,
							height = DEFAULT_HEIGHT;
	private int clockHertz = 8_000_000; // 8 MHz
	private int vccstate = 0; // or SSD1306_EXTERNALVCC
	private int pages = 0;
	private int[] buffer = null;

	//private SpiDevice spiDevice = null; // Only available in PI4J Jan-2015

	// SPI: Serial Peripheral Interface. Default pin values.
	private static Pin spiClk = RaspiPin.GPIO_14; // Pin #23, SCLK, GPIO_11
	private static Pin spiMosi = RaspiPin.GPIO_12; // Pin #19, SPI0_MOSI
	private static Pin spiCs = RaspiPin.GPIO_10; // Pin #24, SPI0_CE0_N
	private static Pin spiRst = RaspiPin.GPIO_05; // Pin #18, GPIO_24
	private static Pin spiDc = RaspiPin.GPIO_04; // Pin #16, GPIO_23

	private static GpioController gpio;

	private static GpioPinDigitalOutput mosiOutput = null;
	private static GpioPinDigitalOutput clockOutput = null;
	private static GpioPinDigitalOutput chipSelectOutput = null;
	private static GpioPinDigitalOutput resetOutput = null;
	private static GpioPinDigitalOutput dcOutput = null;

	private I2CBus bus;
	private I2CDevice ssd1306;

	private boolean verbose = "true".equals(System.getProperty("ssd1306.verbose", "false"));

	public SSD1306() {
		initSSD1306(this.width, this.height);
	}

	/**
	 * SPI Interface
	 *
	 * @param w Buffer width (pixels).  Default is 128
	 * @param h Buffer height (pixels). Default is  32
	 */
	public SSD1306(int w, int h) {
		initSSD1306(w, h);
	}

	/**
	 * SPI Interface
	 *
	 *              | function                     | Wiring/PI4J    |Cobbler | Name      |GPIO/BCM
	 * -------------+------------------------------+----------------+--------=-----------+----
	 * @param clock | Clock Pin.        Default is |RaspiPin.GPIO_14|Pin #23 |SPI0_SCLK  | 11    Clock
	 * @param mosi, | MOSI / Data Pin.  Default is |RaspiPin.GPIO_12|Pin #19 |SPI0_MOSI  | 10    Master Out Slave In
	 * @param cs,   | CS Pin.           Default is |RaspiPin.GPIO_10|Pin #24 |SPI0_CE0_N |  8    Chip Select
	 * @param rst,  | RST Pin.          Default is |RaspiPin.GPIO_05|Pin #18 |GPIO_24    | 24    Reset
	 * @param dc,   | DC Pin.           Default is |RaspiPin.GPIO_04|Pin #16 |GPIO_23    | 23    Data Control (?)
	 * -------------+------------------------------+----------------+--------=-----------+----
	 */
	public SSD1306(Pin clock, Pin mosi, Pin cs, Pin rst, Pin dc) {
		spiClk = clock;
		spiMosi = mosi;
		spiCs = cs;
		spiRst = rst;
		spiDc = dc;
		initSSD1306(this.width, this.height);
	}

	/**
	 * SPI Interface
	 *
	 *              | function                     | Wiring/PI4J    |Cobbler | Name      |GPIO/BCM
	 * -------------+------------------------------+----------------+--------=-----------+----
	 * @param clock | Clock Pin.        Default is |RaspiPin.GPIO_14|Pin #23 |SPI0_SCLK  | 11    Clock
	 * @param mosi, | MOSI / Data Pin.  Default is |RaspiPin.GPIO_12|Pin #19 |SPI0_MOSI  | 10    Master Out Slave In
	 * @param cs,   | CS Pin.           Default is |RaspiPin.GPIO_10|Pin #24 |SPI0_CE0_N |  8    Chip Select
	 * @param rst,  | RST Pin.          Default is |RaspiPin.GPIO_05|Pin #18 |GPIO_24    | 24    Reset
	 * @param dc,   | DC Pin.           Default is |RaspiPin.GPIO_04|Pin #16 |GPIO_23    | 23    Data Control (?)
	 * -------------+------------------------------+----------------+--------=-----------+----
	 * @param w     Buffer width (pixels).  Default is 128
	 * @param h     Buffer height (pixels). Default is 32
	 */
	public SSD1306(Pin clock, Pin mosi, Pin cs, Pin rst, Pin dc, int w, int h) {
		spiClk = clock;
		spiMosi = mosi;
		spiCs = cs;
		spiRst = rst;
		spiDc = dc;
		initSSD1306(w, h);
	}

	public SSD1306(int i2cAddr) throws I2CFactory.UnsupportedBusNumberException, IOException {
		this(i2cAddr, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	/**
	 * I2C Interface
	 *
	 * @param i2cAddr
	 * @throws I2CFactory.UnsupportedBusNumberException
	 * @throws IOException
	 */
	public SSD1306(int i2cAddr, int w, int h) throws I2CFactory.UnsupportedBusNumberException, IOException {
		this.width = w;
		this.height = h;
		if (verbose) {
			String[] map = new String[]{
					String.valueOf(PinUtil.findEnumName("SDA1").pinNumber()) + ":" + "SDA",
					String.valueOf(PinUtil.findEnumName("SCL1").pinNumber()) + ":" + "SCL"
			};
			PinUtil.print(map);
		}
		// Get i2c bus
		bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
		if (verbose) {
			System.out.println("Connected to bus. OK.");
		}

		// Get device itself
		ssd1306 = bus.getDevice(i2cAddr);

		if (verbose) {
			System.out.println("Connected to devices. OK.");
		}

		initSSD1306(this.width, this.height); // 128x32, hard coded for now.
	}

	private void initSSD1306(int w, int h) {
		if (w != 128) {
			throw new IllegalArgumentException("Width cannot be anything but 128");
		}
		if (h != 32 && h != 64) {
			throw new IllegalArgumentException("Height must be 32 or 64");
		}

		this.width = w;
		this.height = h;
		this.pages = this.height / 8; // Number of lines
		this.buffer = new int[this.width * this.pages];
		clear();

		if (bus == null) { // => SPI
			int fd = Spi.wiringPiSPISetup(SPI_DEVICE, clockHertz);
			if (fd < 0) {
				System.err.println("SPI Setup failed");
				System.exit(1);
			}

			gpio = GpioFactory.getInstance();

			mosiOutput = gpio.provisionDigitalOutputPin(spiMosi, "MOSI", PinState.LOW);
			clockOutput = gpio.provisionDigitalOutputPin(spiClk, "CLK", PinState.LOW);
			chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS", PinState.HIGH);
			resetOutput = gpio.provisionDigitalOutputPin(spiRst, "RST", PinState.LOW);
			dcOutput = gpio.provisionDigitalOutputPin(spiDc, "DC", PinState.LOW);

			if (verbose) {
				String[] map = new String[]{
						String.valueOf(PinUtil.findByPin(spiMosi).pinNumber()) + ":" + "MOSI",
						String.valueOf(PinUtil.findByPin(spiClk).pinNumber()) + ":" + "CLK",
						String.valueOf(PinUtil.findByPin(spiCs).pinNumber()) + ":" + "CS",
						String.valueOf(PinUtil.findByPin(spiRst).pinNumber()) + ":" + "RST",
						String.valueOf(PinUtil.findByPin(spiDc).pinNumber()) + ":" + "DC"
				};
				PinUtil.print(map);
			}
		}
	}

	public void shutdown() {
		if (bus == null) {
			gpio.shutdown();
		} else {
			try {
				bus.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setBuffer(int[] buffer) {
		this.buffer = buffer;
	}

	public int[] getBuffer() {
		return buffer;
	}

	/**
	 * Half-duplex SPI write.  If assertSs is True, the SS line will be
	 * asserted low, the specified bytes will be clocked out the MOSI line, and
	 * if deassertSs is True the SS line be put back high.
	 */
	private void write(int[] data) {
		if (bus == null) {
			write(data, true, true);
		} else {
			byte[] bb = new byte[data.length];
			for (int i=0; i<bb.length; i++) {
				bb[i] = (byte)data[i];
			}
			try {
				ssd1306.write(0x0, bb);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private final int MASK = 0x80; // MSBFIRST, 0x80 = 0&10000000
//private final int MASK = 0x01; // LSBFIRST

	private void write(int[] data, boolean assert_ss, boolean deassert_ss) {
		// Fail if MOSI is not specified.
		if (mosiOutput == null) {
			throw new RuntimeException("Write attempted with no MOSI pin specified.");
		}
		if (assert_ss && chipSelectOutput != null) {
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
		if (deassert_ss && chipSelectOutput != null) {
			chipSelectOutput.high();
		}
	}

	private void command(int c) throws Exception {
		if (dcOutput != null) {
			dcOutput.low();
//    try { spiDevice.write((byte)c); }
//    catch (IOException ioe) { ioe.printStackTrace(); }
			this.write(new int[]{c});
		} else {
			try {
				this.ssd1306.write(SSD1306_SETLOWCOLUMN, (byte) c);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
		}
	}

	private void reset() {
		if (resetOutput != null) {
			resetOutput.high();
			delay(1);
			// Set reset low for 10 milliseconds.
			resetOutput.low();
			delay(10);
			// Set reset high again.
			resetOutput.high();
		}
	}

	public void data(int c) {
		if (dcOutput != null) {
			// SPI write.
			dcOutput.high();
//    try { spiDevice.write((byte)c); }
//    catch (IOException ioe) { ioe.printStackTrace(); }
			this.write(new int[]{c});
		} else {
			try {
				this.ssd1306.write(SSD1306_SETSTARTLINE, (byte) c);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Initialize display
	 */
	public void begin() throws Exception {
		begin(SSD1306_SWITCHCAPVCC);
	}

	public void begin(int vcc) throws Exception {
		// Save vcc state.
		this.vccstate = vcc;
		// Reset and initialize display.
		this.reset();
		this.initialize();
		// Turn on the display.
		this.command(SSD1306_DISPLAYON);
	}

	private void initialize() throws Exception { // SPI, 128x32 or 128x64
		// 128x(32/64) pixel specific initialization.
		this.command(SSD1306_DISPLAYOFF);          // 0xAE
		this.command(SSD1306_SETDISPLAYCLOCKDIV);  // 0xD5
		this.command(0x80);                     // the suggested ratio 0x80

		this.command(SSD1306_SETMULTIPLEX);        // 0xA8
		this.command(height == 32 ? 0x1F : 0x3F); // Height - 1 : 1F = 31 = 32 - 1, 3F = 63 = 64 - 1
		if (verbose) {
			System.out.println(String.format(">>> Initialize: screen height: %d", height));
		}

		this.command(SSD1306_SETDISPLAYOFFSET);    // 0xD3
		this.command(0x0);                         // no offset
		this.command(SSD1306_SETSTARTLINE | 0x0);  // line //0
		this.command(SSD1306_CHARGEPUMP);          // 0x8D
		if (this.vccstate == SSD1306_EXTERNALVCC) {
			this.command(0x10);
		} else {
			this.command(0x14);
		}
		this.command(SSD1306_MEMORYMODE);          // 0x20
		this.command(0x00);                     // 0x0 act like ks0108
		this.command(SSD1306_SEGREMAP | 0x1);
		this.command(SSD1306_COMSCANDEC);

		this.command(SSD1306_SETCOMPINS);          // 0xDA
		this.command((height == 32) ? 0x02 : 0x12);
		this.command(SSD1306_SETCONTRAST);         // 0x81
		if (height == 32) {
			this.command(0x8F);
		} else { // 64
			if (this.vccstate == SSD1306_EXTERNALVCC) {
				this.command(0x9F);
			} else {
				this.command(0xCF);
			}
		}
		this.command(SSD1306_SETPRECHARGE);        // 0xd9
		if (this.vccstate == SSD1306_EXTERNALVCC) {
			this.command(0x22);
		} else {
			this.command(0xF1);
		}
		this.command(SSD1306_SETVCOMDETECT);       // 0xDB
		this.command(0x40);
		this.command(SSD1306_DISPLAYALLON_RESUME); // 0xA4
		this.command(SSD1306_NORMALDISPLAY);       // 0xA6

//	this.command(SSD1306_DEACTIVATE_SCROLL); // ?
	}

	public void clear() {
		for (int i = 0; this.buffer != null && i < this.buffer.length; i++) {
			this.buffer[i] = 0;
		}
	}

	public void setContrast(int contrast)
					throws IllegalArgumentException, Exception {
		if (contrast < 0 || contrast > 255) {
			throw new IllegalArgumentException("Contrast must be a value in [0, 255]");
		}
		this.command(SSD1306_SETCONTRAST);
		this.command(contrast);
	}

	/**
	 * Write display buffer to physical display.
	 */
	public void display() throws Exception {
		this.command(SSD1306_COLUMNADDR);
		this.command(0); // Column start address. (0 = reset)
		this.command(this.width - 1); // Column end address.
		this.command(SSD1306_PAGEADDR);
		this.command(0); // Page start address. (0 = reset)
		this.command(this.pages - 1); // Page end address.

		if (dcOutput != null) {
			// Write buffer data.
			//   Set DC high for data.
			dcOutput.high();
			this.write(this.buffer);
		} else {
			try {
				byte[] bb = new byte[this.buffer.length];
				for (int i=0; i<bb.length; i++) {
					bb[i] = (byte)this.buffer[i];
				}
				this.ssd1306.write(SSD1306_SETSTARTLINE, bb);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Adjusts contrast to dim the display if dim is True, otherwise sets the
	 * contrast to normal brightness if dim is False.
	 */
	public void dim(boolean dim) { // ???? WTF ?????
		// Assume dim display.
		int contrast = 0;
		// Adjust contrast based on VCC if not dimming.
		if (!dim) {
			if (this.vccstate == SSD1306_EXTERNALVCC) {
				contrast = 0x9F;
			} else {
				contrast = 0xCF;
			}
		}
	}
}
