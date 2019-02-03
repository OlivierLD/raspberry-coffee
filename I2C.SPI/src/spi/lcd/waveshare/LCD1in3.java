package spi.lcd.waveshare;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;
import utils.PinUtil;
import utils.TimeUtil;

/**
 * Java interface for https://www.waveshare.com/product/modules/oleds-lcds/raspberry-pi-lcd/1.3inch-lcd-hat.htm
 * As the header is attached to the screen, there is no choice in the pins to use for the SPI interface.
 *
 * Color LCD Screen 240x240
 * 3 Buttons
 * Joystick
 */
public class LCD1in3 {

	/**
	 * Wiring:
	 *
	 *  function          | Wiring/PI4J | Physical | Name        | GPIO/BCM
	 * -------------------+-------------+----------+-------------+----
	 *  Power             |             |          | 3v3         |
	 *  GND               |             |          | GND         |
	 * -------------------+-------------+----------+-------------+----
	 *  Clock Pin.        |          14 |     #23  | SPI0_SCLK   | 11    Clock
	 *  MOSI / Data Pin.  |          12 |     #19  | SPI0_MOSI   | 10    Master Out Slave In
	 *  CS Pin.           |          10 |     #24  | SPI0_CE0_N  |  8    Chip Select
	 *  RST Pin.          |          02 |     #13  | GPIO_02     | 27    Reset
	 *  DC Pin.           |          06 |     #22  | GPIO_06     | 25    Data Control (?)
	 * -------------------+-------------+----------+-------------+----
	 * Back Light         |          05 |     #18  | GPIO_5      | 24
	 * Key 1              |          29 |     #40  | PCM_DOUT    | 21
	 * Key 2              |          28 |     #38  | PCM_DIN     | 20
	 * Key 3              |          36 |     #36  | GPIO_27     | 16
	 * Joystick up        |          22 |     #31  | GPCLK2      |  6
	 * Joystick down      |          24 |     #35  | PCM_FS/PWM1 | 19
	 * Joystick left      |          21 |     #29  | GPCLK1      |  5
	 * Joystick right     |          25 |     #37  | GPIO_25     | 26
	 * Joystick pressed   |          23 |     #33  | PWM1        | 13
	 * -------------------+-------------+----------+-------------+----
	 */

	private final static boolean VERBOSE = "true".equals(System.getProperty("waveshare.1in3.verbose", "false"));

	private static GpioController gpio;

	private static GpioPinDigitalOutput mosiPin = null;
	private static GpioPinDigitalOutput clockPin = null;
	private static GpioPinDigitalOutput chipSelectPin = null;
	private static GpioPinDigitalOutput resetPin = null;
	private static GpioPinDigitalOutput dcPin = null;

	// TODO 9 Other pins
	private static GpioPinDigitalOutput backLightPin = null;

	private final static int SPI_DEVICE = Spi.CHANNEL_0;
	private int clockHertz = 8_000_000; // 8 MHz TODO Check this

	private final static int LCD_HEIGHT = 240;
	private final static int LCD_WIDTH  = 240;

	private final static int  HORIZONTAL = 0;
	private final static int  VERTICAL   = 1;

	private int lcdWidth = 0;
	private int lcdHeight = 0;

	private final static int WHITE = 0xFFFF;
	private final static int BLACK = 0x0000;
	private final static int BLUE = 0x001F;
	private final static int BRED = 0XF81F;
	private final static int GRED = 0XFFE0;
	private final static int GBLUE = 0X07FF;
	private final static int RED = 0xF800;
	private final static int MAGENTA = 0xF81F;
	private final static int GREEN = 0x07E;
	private final static int CYAN = 0x7FFF;
	private final static int YELLOW = 0xFFE0;
	private final static int BROWN = 0XBC40;
	private final static int BRRED = 0XFC07;
	private final static int GRAY = 0X8430;

	public LCD1in3() {

		if (VERBOSE) {
			String[] map = new String[14];
			map[0] = "23:CLK";
			map[1] = "19:MOSI";
			map[2] = "24:CS";
			map[3] = "13:RST";
			map[4] = "22:DC";
			map[5] = "18:BL";
			map[6] = "40:K-1";
			map[7] = "38:K-2";
			map[8] = "36:K-3";
			map[9] = "31:J-UP";
			map[10] = "35:J-DWN";
			map[11] = "29:J-LFT";
			map[12] = "37:J-RGT";
			map[13] = "33:J-PR";

			PinUtil.print(map);
		}
		init();
	}

	private void init() {
		init(HORIZONTAL, WHITE);
	}
	private void init(int direction, int color) {
		int fd = Spi.wiringPiSPISetup(SPI_DEVICE, clockHertz);
		if (fd < 0) {
			System.err.println("SPI Setup failed");
			System.exit(1);
		}

		gpio = GpioFactory.getInstance();

		mosiPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MOSI", PinState.LOW);
		clockPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, "CLK", PinState.LOW);
		chipSelectPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, "CS", PinState.HIGH);
		resetPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "RST", PinState.LOW);
		dcPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "DC", PinState.LOW);
		// TODO Other pins
		backLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "BL", PinState.LOW);

		LCDInit(direction);
		LCDClear(color);
	}

	private void LCDInit(int scanDirection) {
		// Turn on the back-light
		backLightPin.high();

		// Hardware reset
		LCDReset();

		// Set the resolution and scanning method of the screen
		LCDSetAttributes(scanDirection);

		// Set the initialization register
		LCDInitReg();
	}

	private void LCDClear(int color) {
		LCDSetWindows(0, 0, LCD_WIDTH, LCD_HEIGHT);
		for (int j = 0; j < LCD_HEIGHT; j++) {
			for (int i = 0; i < LCD_WIDTH; i++) {
				LCDSendData16Bit(color);
			}
		}
	}

	private void LCDSetWindows(int xFrom, int yFrom, int xTo, int yTo) {
		// set the X coordinates
		LCDSendCommand((byte)0x2A);
		LCDSendData8Bit((byte)((xFrom >> 8) & 0xFF));
		LCDSendData8Bit((byte)(xFrom & 0xFF));
		LCDSendData8Bit((byte)(((xTo  - 1) >> 8) & 0xFF));
		LCDSendData8Bit((byte)((xTo  - 1) & 0xFF));

		// set the Y coordinates
		LCDSendCommand((byte)0x2B);
		LCDSendData8Bit((byte)((yFrom >> 8) & 0xFF));
		LCDSendData8Bit((byte)(yFrom & 0xFF));
		LCDSendData8Bit((byte)(((yTo  - 1) >> 8) & 0xFF));
		LCDSendData8Bit((byte)((yTo  - 1) & 0xFF));

		LCDSendCommand((byte)0X2C);
	}

	private void LCDReset() {
		resetPin.high();
		TimeUtil.delay(100);
		resetPin.low();
		TimeUtil.delay(100);
		resetPin.high();
		TimeUtil.delay(100);
	}

	private void LCDSetAttributes(int scanDirection) {
		byte memoryAccessReg = 0x00;

		// Get GRAM and LCD width and height
		if (scanDirection == HORIZONTAL) {
			lcdHeight	= LCD_HEIGHT;
			lcdWidth   = LCD_WIDTH;
			memoryAccessReg = 0X70;
		} else {
			lcdHeight	= LCD_WIDTH;
			lcdWidth   = LCD_HEIGHT;
			memoryAccessReg = 0X00;
		}

		// Set the read / write scan direction of the frame memory
		LCDSendCommand((byte)0x36); //MX, MY, RGB mode
		LCDSendData8Bit(memoryAccessReg);	//0x08 set RGB

	}

	private void LCDSendCommand(byte reg) {
		dcPin.low();
		// LCD_CS_0;
		this.write(reg);
		// LCD_CS_1;
	}

	private void LCDSendData8Bit(byte data) {
		dcPin.high();
		// LCD_CS_0;
		this.write(data);
		// LCD_CS_1;
	}

	private void LCDSendData16Bit(int data)
	{
		dcPin.high();
		// LCD_CS_0;
		this.write((data >> 8) & 0xFF);
		this.write(data & 0xFF);
		// LCD_CS_1;
	}

	private void LCDInitReg() {
		LCDSendCommand((byte)0x3A);
		LCDSendData8Bit((byte)0x05);

		LCDSendCommand((byte)0xB2);
		LCDSendData8Bit((byte)0x0C);
		LCDSendData8Bit((byte)0x0C);
		LCDSendData8Bit((byte)0x00);
		LCDSendData8Bit((byte)0x33);
		LCDSendData8Bit((byte)0x33);

		LCDSendCommand((byte)0xB7);  //Gate Control
		LCDSendData8Bit((byte)0x35);

		LCDSendCommand((byte)0xBB);  //VCOM Setting
		LCDSendData8Bit((byte)0x19);

		LCDSendCommand((byte)0xC0); //LCM Control
		LCDSendData8Bit((byte)0x2C);

		LCDSendCommand((byte)0xC2);  //VDV and VRH Command Enable
		LCDSendData8Bit((byte)0x01);
		LCDSendCommand((byte)0xC3);  //VRH Set
		LCDSendData8Bit((byte)0x12);
		LCDSendCommand((byte)0xC4);  //VDV Set
		LCDSendData8Bit((byte)0x20);

		LCDSendCommand((byte)0xC6);  //Frame Rate Control in Normal Mode
		LCDSendData8Bit((byte)0x0F);

		LCDSendCommand((byte)0xD0);  // Power Control 1
		LCDSendData8Bit((byte)0xA4);
		LCDSendData8Bit((byte)0xA1);

		LCDSendCommand((byte)0xE0);  //Positive Voltage Gamma Control
		LCDSendData8Bit((byte)0xD0);
		LCDSendData8Bit((byte)0x04);
		LCDSendData8Bit((byte)0x0D);
		LCDSendData8Bit((byte)0x11);
		LCDSendData8Bit((byte)0x13);
		LCDSendData8Bit((byte)0x2B);
		LCDSendData8Bit((byte)0x3F);
		LCDSendData8Bit((byte)0x54);
		LCDSendData8Bit((byte)0x4C);
		LCDSendData8Bit((byte)0x18);
		LCDSendData8Bit((byte)0x0D);
		LCDSendData8Bit((byte)0x0B);
		LCDSendData8Bit((byte)0x1F);
		LCDSendData8Bit((byte)0x23);

		LCDSendCommand((byte)0xE1);  //Negative Voltage Gamma Control
		LCDSendData8Bit((byte)0xD0);
		LCDSendData8Bit((byte)0x04);
		LCDSendData8Bit((byte)0x0C);
		LCDSendData8Bit((byte)0x11);
		LCDSendData8Bit((byte)0x13);
		LCDSendData8Bit((byte)0x2C);
		LCDSendData8Bit((byte)0x3F);
		LCDSendData8Bit((byte)0x44);
		LCDSendData8Bit((byte)0x51);
		LCDSendData8Bit((byte)0x2F);
		LCDSendData8Bit((byte)0x1F);
		LCDSendData8Bit((byte)0x1F);
		LCDSendData8Bit((byte)0x20);
		LCDSendData8Bit((byte)0x23);

		LCDSendCommand((byte)0x21);  //Display Inversion On

		LCDSendCommand((byte)0x11);  //Sleep Out

		LCDSendCommand((byte)0x29);  //Display On
	}

	private final int MASK = 0x80; // MSBFIRST, 0x80 = 0&10000000
	//private final int MASK = 0x01; // LSBFIRST

	private void write(int data) {
		this.write(new int[] { data });
	}
	private void write(int[] data) {
		this.write(data, true, true);
	}
	private void write(int[] data, boolean assertSs, boolean deassertSs) {
		// Fail if MOSI is not specified.
		if (mosiPin == null) {
			throw new RuntimeException("Write attempted with no MOSI pin specified.");
		}
		if (assertSs && chipSelectPin != null) {
			chipSelectPin.low();
		}
		for (int i = 0; i < data.length; i++) {
			byte b = (byte) data[i];
			for (int j = 0; j < 8; j++) {
				byte bit = (byte) ((b << j) & MASK);
				// Write bit to MOSI.
				if (bit != 0) {
					mosiPin.high();
				} else {
					mosiPin.low();
				}
				// Flip clock off base. // TODO Check the value of the base (LOW Here)
				clockPin.high();
				// Return clock to base.
				clockPin.low();
			}
		}
		if (deassertSs && chipSelectPin != null) {
			chipSelectPin.high();
		}
	}

	private void command(int c) throws Exception {
			dcPin.low();
//    try { spiDevice.write((byte)c); }
//    catch (IOException ioe) { ioe.printStackTrace(); }
			this.write(new int[]{c});
	}

	public void shutdown() {
		gpio.shutdown();
	}

}
