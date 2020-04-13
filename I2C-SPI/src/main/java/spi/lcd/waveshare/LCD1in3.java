package spi.lcd.waveshare;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Spi;
import spi.lcd.waveshare.fonts.Font;
import spi.lcd.waveshare.fonts.Font12;
import spi.lcd.waveshare.fonts.Font16;
import spi.lcd.waveshare.fonts.Font20;
import spi.lcd.waveshare.fonts.Font24;
import spi.lcd.waveshare.fonts.Font8;
import utils.PinUtil;
import utils.TimeUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Java interface for https://www.waveshare.com/product/modules/oleds-lcds/raspberry-pi-lcd/1.3inch-lcd-hat.htm
 * As the header is attached to the screen, there is no choice in the pins to use for the SPI interface.
 * <p>
 * Color LCD Screen 240x240
 * 3 Buttons
 * Joystick
 */
public class LCD1in3 {

	/**
	 * Wiring:
	 * <p>
	 * function          | Wiring/PI4J | Physical | Name        | GPIO/BCM
	 * ------------------+-------------+----------+-------------+----
	 * Power             |             |          | 3v3         |
	 * GND               |             |          | GND         |
	 * ------------------+-------------+----------+-------------+----
	 * Clock Pin.        |          14 |     #23  | SPI0_SCLK   | 11    Clock
	 * MOSI / Data Pin.  |          12 |     #19  | SPI0_MOSI   | 10    Master Out Slave In
	 * CS Pin.           |          10 |     #24  | SPI0_CE0_N  |  8    Chip Select
	 * RST Pin.          |          02 |     #13  | GPIO_02     | 27    Reset
	 * DC Pin.           |          06 |     #22  | GPIO_06     | 25    Data Control (?)
	 * ------------------+-------------+----------+-------------+----
	 * Back Light        |          05 |     #18  | GPIO_5      | 24
	 * Key 1             |          29 |     #40  | PCM_DOUT    | 21
	 * Key 2             |          28 |     #38  | PCM_DIN     | 20
	 * Key 3             |          36 |     #36  | GPIO_27     | 16
	 * Joystick up       |          22 |     #31  | GPCLK2      |  6
	 * Joystick down     |          24 |     #35  | PCM_FS/PWM1 | 19
	 * Joystick left     |          21 |     #29  | GPCLK1      |  5
	 * Joystick right    |          25 |     #37  | GPIO_25     | 26
	 * Joystick pressed  |          23 |     #33  | PWM1        | 13
	 * ------------------+-------------+----------+-------------+----
	 */

	private final static boolean VERBOSE = "true".equals(System.getProperty("waveshare.1in3.verbose", "false"));
	private boolean simulate = false;

	private static GpioController gpio;

	// SPI pins, output (no MISO).
	private static GpioPinDigitalOutput mosiPin = null;
	private static GpioPinDigitalOutput clockPin = null;
	private static GpioPinDigitalOutput chipSelectPin = null;
	private static GpioPinDigitalOutput resetPin = null;
	private static GpioPinDigitalOutput dcPin = null;

	// 9 Other pins, input.
	private static GpioPinDigitalOutput backLightPin = null;

	private static GpioPinDigitalInput key1Pin = null;
	private static GpioPinDigitalInput key2Pin = null;
	private static GpioPinDigitalInput key3Pin = null;

	private static GpioPinDigitalInput joystickUpPin = null;
	private static GpioPinDigitalInput joystickDownPin = null;
	private static GpioPinDigitalInput joystickLeftPin = null;
	private static GpioPinDigitalInput joystickRightPin = null;
	private static GpioPinDigitalInput joystickPressedPin = null;


	private final static int SPI_DEVICE = Spi.CHANNEL_0;
	private int clockHertz = 8_000_000; // 8 MHz TODO Check this??

	public final static int LCD_HEIGHT = 240;
	public final static int LCD_WIDTH  = 240;

	public final static int HORIZONTAL = 0;
	public final static int VERTICAL   = 1;

	private int lcdWidth  = LCD_WIDTH;
	private int lcdHeight = LCD_HEIGHT;

	public final static int WHITE   = 0xffff;
	public final static int BLACK   = 0x0000;
	public final static int BLUE    = 0x001f;
	public final static int BRED    = 0xf81f;
	public final static int GRED    = 0xffe0;
	public final static int GBLUE   = 0x07ff;
	public final static int RED     = 0xf800;
	public final static int MAGENTA = 0xf81f;
	public final static int GREEN   = 0x07e0;
	public final static int CYAN    = 0x7fff;
	public final static int YELLOW  = 0xffe0;
	public final static int BROWN   = 0xbc40;
	public final static int BRRED   = 0xfc07;
	public final static int GRAY    = 0x8430;

	public int IMAGE_BACKGROUND = WHITE;

	public int FONT_FOREGROUND = BLACK;
	public int FONT_BACKGROUND = WHITE;


	public final static int IMAGE_ROTATE_0   = 0;
	public final static int IMAGE_ROTATE_90  = 1;
	public final static int IMAGE_ROTATE_180 = 2;
	public final static int IMAGE_ROTATE_270 = 3;

	/**
	 * image color level
	 */
	public final static int IMAGE_COLOR_POSITIVE = 0x01;
	public final static int IMAGE_COLOR_INVERTED = 0x02;

	public boolean isSimulating() {
		return simulate;
	}

	private Consumer<GpioPinDigitalStateChangeEvent> key1Consumer = (event) -> System.out.println(String.format(">> Default event consumer, Key 1: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setKey1Consumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.key1Consumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> key2Consumer = (event) -> System.out.println(String.format(">> Default event consumer, Key 2: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setKey2Consumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.key2Consumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> key3Consumer = (event) -> System.out.println(String.format(">> Default event consumer, Key 3: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setKey3Consumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.key3Consumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> jUpConsumer = (event) -> System.out.println(String.format(">> Default event consumer, J-Up: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setJUpConsumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.jUpConsumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> jDownConsumer = (event) -> System.out.println(String.format(">> Default event consumer, J-Down: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setJDownConsumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.jDownConsumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> jLeftConsumer = (event) -> System.out.println(String.format(">> Default event consumer, J-Left: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setJLeftConsumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.jLeftConsumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> jRightConsumer = (event) -> System.out.println(String.format(">> Default event consumer, J-Right: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setJRightConsumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.jRightConsumer = consumer;
	}
	private Consumer<GpioPinDigitalStateChangeEvent> jPressedConsumer = (event) -> System.out.println(String.format(">> Default event consumer, J-Pressed: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString()));
	public void setJPressedConsumer(Consumer<GpioPinDigitalStateChangeEvent> consumer) {
		this.jPressedConsumer = consumer;
	}

	/**
	 * image number
	 */
	public final static int IMAGE_RGB = 0;

	private static class GUIImage {
		int imageName; // max = 128K / (imageWidth/8 * imageHeight) TODO imageName? Name, really?
		int imageOffset;
		int imageWidth;
		int imageHeight;
		int imageRotate;
		int imageColor;
		int memoryWidth;
		int memoryHeight;
	}

	private int[] imageBuff = new int[LCD_HEIGHT * LCD_WIDTH];

	public enum DotPixel {

		DOT_PIXEL_1X1(1),    // 1 x 1
		DOT_PIXEL_2X2(2),    // 2 X 2
		DOT_PIXEL_3X3(3),    // 3 X 3
		DOT_PIXEL_4X4(4),    // 4 X 4
		DOT_PIXEL_5X5(5),    // 5 X 5
		DOT_PIXEL_6X6(6),    // 6 X 6
		DOT_PIXEL_7X7(7),    // 7 X 7
		DOT_PIXEL_8X8(8);    // 8 X 8

		int size = 1;

		DotPixel(int size) {
			this.size = size;
		}

		public int size() {
			return this.size;
		}
	}

	public enum DotStyle {
		DOT_FILL_AROUND,    // dot pixel 1 x 1
		DOT_FILL_RIGHTUP    // dot pixel 2 X 2
	}

	public static DotStyle DOT_STYLE_DFT = DotStyle.DOT_FILL_AROUND; // Default dot pixel

	public enum LineStyle {
		LINE_STYLE_SOLID,
		LINE_STYLE_DOTTED,
	}

	public enum DrawFill {
		DRAW_FILL_EMPTY,
		DRAW_FILL_FULL,
	}

	public static DotPixel DOT_PIXEL_DFT = DotPixel.DOT_PIXEL_1X1;  // Default dot pixel

	private GUIImage guiImage = new GUIImage();

	public LCD1in3() {
		this(HORIZONTAL, WHITE);
	}

	public LCD1in3(int direction, int color) {

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
		try {
			init(direction, color);
		} catch (UnsatisfiedLinkError usle) {
			if (VERBOSE) {
				System.out.println("Simulating...");
			}
			simulate = true;
		}
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

		// See pin names at http://pi4j.com/pins/model-3b-rev1.html
		mosiPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MOSI", PinState.LOW);
		clockPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, "CLK", PinState.LOW);
		chipSelectPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_10, "CS", PinState.HIGH);
		resetPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "RST", PinState.LOW);
		dcPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "DC", PinState.LOW);
		// Other pins
		backLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "BL", PinState.LOW);

		// PULL_UP, means released, PULL_DOWN means pushed down.
		key1Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "K-1", PinPullResistance.PULL_UP);
		key1Pin.setShutdownOptions(true);
		key2Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "K-2", PinPullResistance.PULL_UP);
		key2Pin.setShutdownOptions(true);
		key3Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "K-3", PinPullResistance.PULL_UP);
		key3Pin.setShutdownOptions(true);

		joystickUpPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, "J-UP", PinPullResistance.PULL_UP);
		joystickUpPin.setShutdownOptions(true);
		joystickDownPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_24, "J-DWN", PinPullResistance.PULL_UP);
		joystickDownPin.setShutdownOptions(true);
		joystickLeftPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_21, "J-LFT", PinPullResistance.PULL_UP);
		joystickLeftPin.setShutdownOptions(true);
		joystickRightPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25, "J-RGT", PinPullResistance.PULL_UP);
		joystickRightPin.setShutdownOptions(true);
		joystickPressedPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, "J-PR", PinPullResistance.PULL_UP);
		joystickPressedPin.setShutdownOptions(true);

		// See http://pi4j.com/example/listener.html
		key1Pin.addListener((GpioPinListenerDigital) event -> {
			if (key1Consumer != null) {
				key1Consumer.accept(event);
			}
		});
		key2Pin.addListener((GpioPinListenerDigital) event -> {
			if (key2Consumer != null) {
				key2Consumer.accept(event);
			}
		});
		key3Pin.addListener((GpioPinListenerDigital) event -> {
			if (key3Consumer != null) {
				key3Consumer.accept(event);
			}
		});
		joystickUpPin.addListener((GpioPinListenerDigital) event -> {
			if (jUpConsumer != null) {
				jUpConsumer.accept(event);
			}
		});
		joystickDownPin.addListener((GpioPinListenerDigital) event -> {
			if (jDownConsumer != null) {
				jDownConsumer.accept(event);
			}
		});
		joystickRightPin.addListener((GpioPinListenerDigital) event -> {
			if (jRightConsumer != null) {
				jRightConsumer.accept(event);
			}
		});
		joystickLeftPin.addListener((GpioPinListenerDigital) event -> {
			if (jLeftConsumer != null) {
				jLeftConsumer.accept(event);
			}
		});
		joystickPressedPin.addListener((GpioPinListenerDigital) event -> {
			if (jPressedConsumer != null) {
				jPressedConsumer.accept(event);
			}
		});
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

	public void LCDClear(int color) {
		LCDSetWindows(0, 0, LCD_WIDTH, LCD_HEIGHT);
		for (int j = 0; j < LCD_HEIGHT; j++) {
			for (int i = 0; i < LCD_WIDTH; i++) {
				LCDSendData16Bit(color);
			}
		}
	}

	private void LCDSetWindows(int xFrom, int yFrom, int xTo, int yTo) {
		// set the X coordinates
		LCDSendCommand((byte) 0x2A);
		LCDSendData8Bit((byte) ((xFrom >> 8) & 0xFF));
		LCDSendData8Bit((byte) (xFrom & 0xFF));
		LCDSendData8Bit((byte) (((xTo - 1) >> 8) & 0xFF));
		LCDSendData8Bit((byte) ((xTo - 1) & 0xFF));

		// set the Y coordinates
		LCDSendCommand((byte) 0x2B);
		LCDSendData8Bit((byte) ((yFrom >> 8) & 0xFF));
		LCDSendData8Bit((byte) (yFrom & 0xFF));
		LCDSendData8Bit((byte) (((yTo - 1) >> 8) & 0xFF));
		LCDSendData8Bit((byte) ((yTo - 1) & 0xFF));

		LCDSendCommand((byte) 0X2C);
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
			lcdHeight = LCD_HEIGHT;
			lcdWidth = LCD_WIDTH;
			memoryAccessReg = 0X70;
		} else {
			lcdHeight = LCD_WIDTH;
			lcdWidth = LCD_HEIGHT;
			memoryAccessReg = 0X00;
		}

		// Set the read / write scan direction of the frame memory
		LCDSendCommand((byte) 0x36); // MX, MY, RGB mode
		LCDSendData8Bit(memoryAccessReg);  // 0x08 set RGB
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

	private void LCDSendData16Bit(int data) {
		dcPin.high();
		// LCD_CS_0;
		this.write((data >> 8) & 0xFF);
		this.write(data & 0xFF);
		// LCD_CS_1;
	}

	private void LCDInitReg() {
		LCDSendCommand((byte) 0x3A);
		LCDSendData8Bit((byte) 0x05);

		LCDSendCommand((byte) 0xB2);
		LCDSendData8Bit((byte) 0x0C);
		LCDSendData8Bit((byte) 0x0C);
		LCDSendData8Bit((byte) 0x00);
		LCDSendData8Bit((byte) 0x33);
		LCDSendData8Bit((byte) 0x33);

		LCDSendCommand((byte) 0xB7);  // Gate Control
		LCDSendData8Bit((byte) 0x35);

		LCDSendCommand((byte) 0xBB);  // VCOM Setting
		LCDSendData8Bit((byte) 0x19);

		LCDSendCommand((byte) 0xC0); // LCM Control
		LCDSendData8Bit((byte) 0x2C);

		LCDSendCommand((byte) 0xC2);  // VDV and VRH Command Enable
		LCDSendData8Bit((byte) 0x01);
		LCDSendCommand((byte) 0xC3);  // VRH Set
		LCDSendData8Bit((byte) 0x12);
		LCDSendCommand((byte) 0xC4);  // VDV Set
		LCDSendData8Bit((byte) 0x20);

		LCDSendCommand((byte) 0xC6);  // Frame Rate Control in Normal Mode
		LCDSendData8Bit((byte) 0x0F);

		LCDSendCommand((byte) 0xD0);  // Power Control 1
		LCDSendData8Bit((byte) 0xA4);
		LCDSendData8Bit((byte) 0xA1);

		LCDSendCommand((byte) 0xE0);  // Positive Voltage Gamma Control
		LCDSendData8Bit((byte) 0xD0);
		LCDSendData8Bit((byte) 0x04);
		LCDSendData8Bit((byte) 0x0D);
		LCDSendData8Bit((byte) 0x11);
		LCDSendData8Bit((byte) 0x13);
		LCDSendData8Bit((byte) 0x2B);
		LCDSendData8Bit((byte) 0x3F);
		LCDSendData8Bit((byte) 0x54);
		LCDSendData8Bit((byte) 0x4C);
		LCDSendData8Bit((byte) 0x18);
		LCDSendData8Bit((byte) 0x0D);
		LCDSendData8Bit((byte) 0x0B);
		LCDSendData8Bit((byte) 0x1F);
		LCDSendData8Bit((byte) 0x23);

		LCDSendCommand((byte) 0xE1);  // Negative Voltage Gamma Control
		LCDSendData8Bit((byte) 0xD0);
		LCDSendData8Bit((byte) 0x04);
		LCDSendData8Bit((byte) 0x0C);
		LCDSendData8Bit((byte) 0x11);
		LCDSendData8Bit((byte) 0x13);
		LCDSendData8Bit((byte) 0x2C);
		LCDSendData8Bit((byte) 0x3F);
		LCDSendData8Bit((byte) 0x44);
		LCDSendData8Bit((byte) 0x51);
		LCDSendData8Bit((byte) 0x2F);
		LCDSendData8Bit((byte) 0x1F);
		LCDSendData8Bit((byte) 0x1F);
		LCDSendData8Bit((byte) 0x20);
		LCDSendData8Bit((byte) 0x23);

		LCDSendCommand((byte) 0x21);  // Display Inversion On

		LCDSendCommand((byte) 0x11);  // Sleep Out

		LCDSendCommand((byte) 0x29);  // Display On
	}

	public void GUINewImage(int imageName, int width, int height, int rotate, int color) {
		if (rotate == IMAGE_ROTATE_0 || rotate == IMAGE_ROTATE_180) {
			guiImage.imageWidth = width;
			guiImage.imageHeight = height;
		} else {
			guiImage.imageWidth = height;
			guiImage.imageHeight = width;
		}
		guiImage.imageName = imageName;// At least one picture
		guiImage.imageRotate = rotate;
		guiImage.memoryWidth = width;
		guiImage.memoryHeight = height;
		guiImage.imageColor = color;

		int byteHeight = guiImage.memoryHeight;
		int byteWidth = guiImage.memoryWidth;
		guiImage.imageOffset = guiImage.imageName * (byteHeight * byteWidth);
	}

	public void GUIClear(int color) {
		int height = guiImage.memoryHeight;
		int width = guiImage.memoryWidth;
		int offset = guiImage.imageOffset;

		if (guiImage.imageColor == IMAGE_COLOR_INVERTED) {
			color = ~color;
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int addr = x + y * width + offset;
				imageBuff[addr] = color;
			}
		}
	}

	public void GUIDrawPoint(int xPoint, int yPoint, int color, DotPixel dotPixel, DotStyle dotStyle) {
		if (xPoint > guiImage.imageWidth || yPoint > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUIDrawPoint Input exceeds the normal display range");
			}
			return;
//		} else {
//			if (VERBOSE) {
//				System.out.println("GUIDrawPoint all good.");
//			}
		}

		if (dotStyle == DOT_STYLE_DFT) {
			for (int XDir_Num = 0; XDir_Num < 2 * dotPixel.size() - 1; XDir_Num++) {
				for (int YDir_Num = 0; YDir_Num < 2 * dotPixel.size() - 1; YDir_Num++) {
					if (xPoint + XDir_Num - dotPixel.size() == -1 || yPoint + XDir_Num - dotPixel.size() == -1) {
						if (VERBOSE) {
							System.out.println("error");
						}
						break;
					}
					GUISetPixel(xPoint + XDir_Num - dotPixel.size(), yPoint + YDir_Num - dotPixel.size(), color);
				}
			}
		} else {
			for (int XDir_Num = 0; XDir_Num < dotPixel.size(); XDir_Num++) {
				for (int YDir_Num = 0; YDir_Num < dotPixel.size(); YDir_Num++) {
					GUISetPixel(xPoint + XDir_Num - 1, yPoint + YDir_Num - 1, color);
				}
			}
		}
	}

	public void GUIDrawPixel(int xPoint, int yPoint, int color) {
		int width = guiImage.memoryWidth;
		int offset = guiImage.imageOffset;

		int addr = xPoint + yPoint * width + offset;

		if (guiImage.imageColor == IMAGE_COLOR_POSITIVE) {
			imageBuff[addr] = color;
		} else {
			imageBuff[addr] = ~color;
		}
	}

	public void GUISetPixel(int xPoint, int yPoint, int color) {
		int x, y;
		switch (guiImage.imageRotate) {
			case IMAGE_ROTATE_0:
				x = xPoint;
				y = yPoint;
				GUIDrawPixel(x, y, color);
				break;
			case IMAGE_ROTATE_90:
				x = yPoint;
				y = guiImage.imageWidth - xPoint - 1;
				GUIDrawPixel(x, y, color);
				break;
			case IMAGE_ROTATE_180:
				x = guiImage.imageWidth - xPoint - 1;
				y = guiImage.imageHeight - yPoint - 1;
				GUIDrawPixel(x, y, color);
				break;
			case IMAGE_ROTATE_270:
				x = guiImage.imageHeight - yPoint - 1;
				y = xPoint;
				GUIDrawPixel(x, y, color);
				break;
		}
	}

	public void GUIDrawLine(int xFrom, int yFrom, int xTo, int yTo, int color, LineStyle lineStyle, DotPixel dotPixel) {
		if (xFrom > guiImage.imageWidth || yFrom > guiImage.imageHeight || xTo > guiImage.imageWidth || yTo > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUIDrawLine Input exceeds the normal display range");
			}
			return;
		}

//		if (VERBOSE && xFrom == xTo) {
//			System.out.println("-- Vertical line! --");
//		}

//		if (xFrom > xTo) { // Swap
//			int temp;
//			temp = xFrom;
//			xFrom = xTo;
//			xTo = temp;
//		}
//		if (yFrom > yTo) { // Swap
//			int temp;
//			temp = yFrom;
//			yFrom = yTo;
//			yTo = temp;
//		}

		int xPoint = xFrom;
		int yPoint = yFrom;
		int dx = Math.abs(xTo - xFrom);
		int dy = -Math.abs(yTo - yFrom);

		// Increment direction, 1 is positive, -1 is counter;
		int xDir = (xFrom < xTo) ? 1 : -1;
		int yDir = (yFrom < yTo) ? 1 : -1;

		// Cumulative error
		int esp = dx + dy;
		byte dottedLen = 0;
		while (true) {
			dottedLen++;
//			if (VERBOSE && xFrom == xTo) {
//				System.out.println(String.format("(2) Vertical line (x:%d y:%d) to (x:%d y:%d) current (x:%d y:%d), (dottedLen %d)", xFrom, yFrom, xTo, yTo, xPoint, yPoint, dottedLen));
//			}
			// Painted dotted line, 2 point is really virtual
			if (lineStyle == LineStyle.LINE_STYLE_DOTTED && dottedLen % 3 == 0) {
				GUIDrawPoint(xPoint, yPoint, IMAGE_BACKGROUND, dotPixel, DOT_STYLE_DFT);
				dottedLen = 0;
			} else {
				GUIDrawPoint(xPoint, yPoint, color, dotPixel, DOT_STYLE_DFT);
			}
			if (2 * esp >= dy) {
				if (xPoint == xTo) {
//					if (VERBOSE) {
//						System.out.println(String.format("(2) DrawLine, vertical, breaking. esp=%d, dy=%d", esp, dy));
//					}
					break;
				}
				esp += dy;
				xPoint += xDir;
			}
			if (2 * esp <= dx) {
				if (yPoint == yTo) {
//					if (VERBOSE) {
//						System.out.println(String.format("DrawLine, horizontal, breaking. esp=%d, dx=%d", esp, dx));
//					}
					break;
				}
				esp += dx;
				yPoint += yDir;
			}
		}
//		if (VERBOSE && xFrom == xTo) {
//			System.out.println("-- Vertical line (end) --");
//		}
	}

	public void LCDDisplay() {
		int addr = 0;
		int offset = guiImage.imageOffset;

		LCDSetWindows(0, 0, LCD_WIDTH, LCD_HEIGHT);
		for (int j = 0; j < LCD_HEIGHT; j++) {
			for (int i = 0; i < LCD_WIDTH; i++) {
				addr = i + j * LCD_WIDTH + offset;
				LCDSendData16Bit(imageBuff[addr]);
			}
		}
	}

	public void GUIDrawRectangle(int xFrom, int yFrom, int xTo, int yTo, int color, DrawFill filled, DotPixel dotPixel) {
		if (xFrom > guiImage.imageWidth || yFrom > guiImage.imageHeight || xTo > guiImage.imageWidth || yTo > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("Input exceeds the normal display range");
			}
			return;
		}

//		if (xFrom > xTo) { // Swap
//			int temp;
//			temp = xFrom;
//			xFrom = xTo;
//			xTo = temp;
//		}
//		if (yFrom > yTo) { // Swap
//			int temp;
//			temp = yFrom;
//			yFrom = yTo;
//			yTo = temp;
//		}

		if (filled.equals(DrawFill.DRAW_FILL_FULL)) {
			for (int yPoint = yFrom; yPoint < yTo; yPoint++) {
				GUIDrawLine(xFrom, yPoint, xTo, yPoint, color, LineStyle.LINE_STYLE_SOLID, dotPixel);
			}
		} else {
			GUIDrawLine(xFrom, yFrom, xTo, yFrom, color, LineStyle.LINE_STYLE_SOLID, dotPixel);
			GUIDrawLine(xFrom, yFrom, xFrom, yTo, color, LineStyle.LINE_STYLE_SOLID, dotPixel);
			GUIDrawLine(xTo, yTo, xTo, yFrom, color, LineStyle.LINE_STYLE_SOLID, dotPixel);
			GUIDrawLine(xTo, yTo, xFrom, yTo, color, LineStyle.LINE_STYLE_SOLID, dotPixel);
		}
	}

	public void GUIDrawCircle(int xCenter, int yCenter, int radius, int color, DrawFill drawFill, DotPixel dotPixel) {
		if (xCenter > guiImage.imageWidth || yCenter >= guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUI_DrawCircle Input exceeds the normal display range");
			}
			return;
		}

		// Draw a circle from(0, R) as a starting point
		int xCurrent = 0;
		int yCurrent = radius;

		// Cumulative error, judge the next point of the logo
		int esp = 3 - (radius << 1);

		if (drawFill == DrawFill.DRAW_FILL_FULL) {
			while (xCurrent <= yCurrent) { // Realistic circles
				for (int sCountY = xCurrent; sCountY <= yCurrent; sCountY++) {
					GUIDrawPoint(xCenter + xCurrent, yCenter + sCountY, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 1
					GUIDrawPoint(xCenter - xCurrent, yCenter + sCountY, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 2
					GUIDrawPoint(xCenter - sCountY, yCenter + xCurrent, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 3
					GUIDrawPoint(xCenter - sCountY, yCenter - xCurrent, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 4
					GUIDrawPoint(xCenter - xCurrent, yCenter - sCountY, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 5
					GUIDrawPoint(xCenter + xCurrent, yCenter - sCountY, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 6
					GUIDrawPoint(xCenter + sCountY, yCenter - xCurrent, color, DOT_PIXEL_DFT, DOT_STYLE_DFT); // 7
					GUIDrawPoint(xCenter + sCountY, yCenter + xCurrent, color, DOT_PIXEL_DFT, DOT_STYLE_DFT);
				}
				if (esp < 0) {
					esp += (4 * xCurrent + 6);
				} else {
					esp += (10 + 4 * (xCurrent - yCurrent));
					yCurrent--;
				}
				xCurrent++;
			}
		} else { // Draw a hollow circle
			while (xCurrent <= yCurrent) {
				GUIDrawPoint(xCenter + xCurrent, yCenter + yCurrent, color, dotPixel, DOT_STYLE_DFT); // 1
				GUIDrawPoint(xCenter - xCurrent, yCenter + yCurrent, color, dotPixel, DOT_STYLE_DFT); // 2
				GUIDrawPoint(xCenter - yCurrent, yCenter + xCurrent, color, dotPixel, DOT_STYLE_DFT); // 3
				GUIDrawPoint(xCenter - yCurrent, yCenter - xCurrent, color, dotPixel, DOT_STYLE_DFT); // 4
				GUIDrawPoint(xCenter - xCurrent, yCenter - yCurrent, color, dotPixel, DOT_STYLE_DFT); // 5
				GUIDrawPoint(xCenter + xCurrent, yCenter - yCurrent, color, dotPixel, DOT_STYLE_DFT); // 6
				GUIDrawPoint(xCenter + yCurrent, yCenter - xCurrent, color, dotPixel, DOT_STYLE_DFT); // 7
				GUIDrawPoint(xCenter + yCurrent, yCenter + xCurrent, color, dotPixel, DOT_STYLE_DFT); // 0

				if (esp < 0) {
					esp += (4 * xCurrent + 6);
				} else {
					esp += (10 + 4 * (xCurrent - yCurrent));
					yCurrent--;
				}
				xCurrent++;
			}
		}
	}

	public void GUIDrawChar(int xPoint, int yPoint, char asciiChar, Font font, int backgroundColor, int foregroundColor) {
		if (xPoint > guiImage.imageWidth || yPoint > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUIDrawChar Input exceeds the normal display range");
			}
			return;
		}

		int charOffset = (asciiChar - ' ') * font.getHeight() * (font.getWidth() / 8 + (font.getWidth() % 8 != 0 ? 1 : 0));
		char ptr = 0;
		try {
			ptr = (char) font.getCharacters()[charOffset];
		} catch (IndexOutOfBoundsException ioobe) { // Char not in the table.
			ptr = 0; // defaulted to ' '.
		}

		for (int page = 0; page < font.getHeight(); page++) {
			for (int column = 0; column < font.getWidth(); column++) {
				// To determine whether the font background color and screen background color is consistent
				if (FONT_BACKGROUND == backgroundColor) { // this process is to speed up the scan
					if ((ptr & (0x80 >> (column % 8))) != 0) {
						GUIDrawPoint(xPoint + column, yPoint + page, foregroundColor, DOT_PIXEL_DFT, DOT_STYLE_DFT);
					}
				} else {
					if ((ptr & (0x80 >> (column % 8))) != 0) {
						GUIDrawPoint(xPoint + column, yPoint + page, foregroundColor, DOT_PIXEL_DFT, DOT_STYLE_DFT);
					} else {
						GUIDrawPoint(xPoint + column, yPoint + page, backgroundColor, DOT_PIXEL_DFT, DOT_STYLE_DFT);
					}
				}
				// One pixel is 8 bits
				if (column % 8 == 7) {
					charOffset += 1;
					ptr = (char) font.getCharacters()[charOffset];
				}
			} // Write a line
			if (font.getWidth() % 8 != 0) {
				charOffset += 1;
				ptr = (char) font.getCharacters()[charOffset];
			}
		} // Write all
	}

	public void GUIDrawString(int xFrom, int yFrom, String str, Font font, int backgroundColor, int foregroundColor) {
		int xPoint = xFrom;
		int yPoint = yFrom;

		if (xFrom > guiImage.imageWidth || yFrom > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUIDrawString Input exceeds the normal display range");
			}
			return;
		}

		for (int i = 0; i < str.length(); i++) {
			// if X direction filled , reposition to(xFrom,yPoint),yPoint is Y direction plus the Height of the character
			if ((xPoint + font.getWidth()) > guiImage.imageWidth) {
				xPoint = xFrom;
				yPoint += font.getHeight();
			}

			// If the Y direction is full, reposition to(xFrom, yFrom)
			if ((yPoint + font.getHeight()) > guiImage.imageHeight) {
				xPoint = xFrom;
				yPoint = yFrom;
			}
			GUIDrawChar(xPoint, yPoint, str.charAt(i), font, backgroundColor, foregroundColor);

			// The next word of the abscissa increases the font of the broadband
			xPoint += font.getWidth();
		}
	}

	public void LCDDisplayWindows(int xFrom, int yFrom, int xTo, int yTo) {
		//Convert coordinates
		int x0 = 0, y0 = 0, x1 = 0, y1 = 0;
		switch (guiImage.imageRotate) {
			case IMAGE_ROTATE_0:
				x0 = xFrom;
				y0 = yFrom;
				x1 = xTo;
				y1 = yTo;
				break;
			case IMAGE_ROTATE_90:
				x0 = yFrom;
				y0 = guiImage.imageWidth - xTo;
				x1 = yTo;
				y1 = guiImage.imageWidth - xFrom;
				break;
			case IMAGE_ROTATE_180:
				x0 = guiImage.imageWidth - xTo;
				y0 = guiImage.imageHeight - yTo;
				x1 = guiImage.imageWidth - xFrom;
				y1 = guiImage.imageHeight - yFrom;
				break;
			case IMAGE_ROTATE_270:
				x0 = guiImage.imageWidth - xTo;
				y0 = xFrom;
				x1 = guiImage.imageHeight - yFrom;
				y1 = guiImage.imageHeight - yTo;
				break;
		}
		// display
		int addr = 0;
		int offset = guiImage.imageOffset;

		LCDSetWindows(x0 - 1, y0 - 1, x1 + 1, y1 + 1);
		for (int j = y0 - 1; j < y1 + 1; j++) {
			for (int i = x0 - 1; i < x1 + 1; i++) {
				addr = i + j * LCD_WIDTH + offset;
				try {
					LCDSendData16Bit(imageBuff[addr]);
				} catch (ArrayIndexOutOfBoundsException aioob) {
					System.err.println(String.format("x0:%d, y0:%d, x1:%d, y1:%d, addr:%d", xFrom, yFrom, xTo, yTo, addr));
					aioob.printStackTrace();
				}
			}
		}
	}

	public void GUIClearWindows(int xFrom, int yFrom, int xTo, int yTo, int color) {
		if (xFrom - 1 == -1 || yFrom - 1 == -1) {
			if (VERBOSE) {
				System.out.println("GUIClearWindows Input exceeds the normal display range");
			}
			return;
		}
		for (int y = yFrom - 1; y < yTo; y++) {
			for (int x = xFrom - 1; x < xTo; x++) {
//				System.out.println(String.format("xFrom %d yFrom %d xTo %d yTo %d X: %d, Y: %d", xFrom, yFrom, xTo, yTo, y, x));
				GUISetPixel(x, y, color);
			}
		}
	}

	public enum ImgJustification {
		CENTERED,
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	public void GUIDisplayImage(String imgPath) {
		GUIDisplayImage(imgPath, 0, 0);
	}

	public void GUIDisplayImage(String imgPath, ImgJustification justification) {
		try {
			File imgFile = new File(imgPath);
			BufferedImage image = ImageIO.read(imgFile);
			int x = 0, y = 0;
			if (justification == ImgJustification.CENTERED) {
				x = (lcdWidth / 2) - (image.getWidth() / 2);
				y = (lcdHeight / 2) - (image.getHeight() / 2);
			} else if (justification == ImgJustification.TOP_LEFT) {
				x = 0;
				y = 0;
			} else if (justification == ImgJustification.TOP_RIGHT) {
				x = lcdWidth - image.getWidth();
				y = 0;
			} else if (justification == ImgJustification.BOTTOM_LEFT) {
				x = 0;
				y = lcdHeight - image.getHeight();
			} else if (justification == ImgJustification.BOTTOM_RIGHT) {
				x = lcdWidth - image.getWidth();
				y = lcdHeight - image.getHeight();
			}

			GUIDisplayImage(image, x, y);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void GUIDisplayImage(String imgPath, int topLeftX, int topLeftY) {
		try {
			File imgFile = new File(imgPath);
			BufferedImage image = ImageIO.read(imgFile);
			GUIDisplayImage(image, topLeftX, topLeftY);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void GUIDisplayImage(BufferedImage image, int topLeftX, int topLeftY) {
		if ((image.getWidth() + topLeftX) > guiImage.imageWidth || (image.getHeight() + topLeftY) > guiImage.imageHeight) {
			if (VERBOSE) {
				System.out.println("GUIDrawString Input exceeds the normal display range");
			}
//		return;
		}

		Raster data = image.getData();
		int[] pixel = new int[4];       // RGBA
		for (int row = 0; row < image.getHeight(); row++) {
			for (int col = 0; col < image.getWidth(); col++) {
				data.getPixel(col, row, pixel);
				int rgb = rgb(pixel[0], pixel[1], pixel[2]);
//			System.out.println(String.format("x:%d y:%d, pix: %d %d %d %d => %04x", col, row, pixel[0], pixel[1], pixel[2], pixel[3], rgb));
				if (col + topLeftX < guiImage.imageWidth &&
						row + topLeftY < guiImage.imageHeight &&
						col + topLeftX >= 0 && row + topLeftY >= 0) { // Within the screen
					GUISetPixel(col + topLeftX, row + topLeftY, rgb);
				}
			}
//		if (VERBOSE) {
//			System.out.println(String.format("Row %d, completed", row));
//		}
		}
		if (VERBOSE) {
			System.out.println("Done with image");
		}
	}

	/*
	 * Top-left to bottom-right
	 * Color picker at https://helloacm.com/tools/rgb-color-picker/
	 * Can replace the GUIClear
	 */
	public void setBackgroundGradient(int topLeftR, int topLeftG, int topLeftB,
	                                  int bottomRightR, int bottomRightG, int bottomRightB) {
		float deltaR = bottomRightR - topLeftR;
		float deltaG = bottomRightG - topLeftG;
		float deltaB = bottomRightB - topLeftB;

		// Diagonal gradient, from top-left to bottom-right
		for (int row = 0; row < LCD1in3.LCD_HEIGHT; row++) {
			int colFromR = topLeftR + (Math.round((deltaR / 2f) * ((float) row / (float) LCD1in3.LCD_WIDTH)));
			int colFromG = topLeftG + (Math.round((deltaG / 2f) * ((float) row / (float) LCD1in3.LCD_WIDTH)));
			int colFromB = topLeftB + (Math.round((deltaB / 2f) * ((float) row / (float) LCD1in3.LCD_WIDTH)));
			for (int col = 0; col < LCD1in3.LCD_WIDTH; col++) {
				int r = colFromR + (Math.round((deltaR / 2f) * ((float) col / (float) LCD1in3.LCD_HEIGHT)));
				int g = colFromG + (Math.round((deltaG / 2f) * ((float) col / (float) LCD1in3.LCD_HEIGHT)));
				int b = colFromB + (Math.round((deltaB / 2f) * ((float) col / (float) LCD1in3.LCD_HEIGHT)));
//				System.out.println(String.format("At (%d, %d), color 0x%02x%02x%02x", col, row, (r & 0xff), (g & 0xff), (b & 0xff)));
				GUISetPixel(col, row, LCD1in3.rgb(r, g, b));
			}
		}
	}

	public static int rgb(int r, int g, int b) {
		return (((r >> 3) << 11) | ((g >> 2) << 5) | (b >> 3));
	}

	public static Font findFontBySize(int size) {
		Font font = null;
		switch (size) {
			case 8:
				font = Font8.getInstance();
				break;
			case 12:
				font = Font12.getInstance();
				break;
			case 16:
				font = Font16.getInstance();
				break;
			case 20:
				font = Font20.getInstance();
				break;
			case 24:
				font = Font24.getInstance();
				break;
			default:
				font = Font16.getInstance(); // if not found...
				break;
		}
		return font;
	}

	private final int MASK = 0x80; // MSBFIRST, 0x80 = 0&10000000
	// private final int MASK = 0x01; // LSBFIRST

	private void write(int data) {
		this.write(new int[]{data});
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

	public void shutdown() {
		if (VERBOSE) {
			System.out.println("Shutting down");
		}
		gpio.shutdown();
	}
}
