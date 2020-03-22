package nmea.forwarders;

import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import nmea.parser.HDG;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import utils.PinUtil;

import java.awt.Color;
import java.util.List;
import java.util.Properties;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * Its features are VERY basic and simple:
 * It display the Heading, Pitch and roll from a magnetometer (like HMC5883L or LSM303)
 * on a small OLED Screen (SSD1306).
 *
 * <br>
 * To be used with other apps.
 * This transformer displays the HDM on an OLED display (SSD1306), in its I2C version
 * <br>
 * See http://raspberrypi.lediouris.net/SSD1306/readme.html
 *
 * <br>
 * This is JUST an example. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 *
 */
public class SSD1306_HDMDisplay implements Forwarder {
	private double declination = 0d;

	private double heading = 0;
	private double pitch = 0d;
	private double roll = 0d;

	public enum SCREEN_SIZE {
		_128x32(128, 32),
		_128x64(128, 64);

		private final int w;
		private final int h;

		SCREEN_SIZE(int w, int h) {
			this.w = w;
			this.h = h;
		}

		public int w() { return this.w; }
		public int h() { return this.h; }
	}

	private SCREEN_SIZE screenDimension = SCREEN_SIZE._128x32;
	private int screenWidth = screenDimension.w();
	private int screenHeight = screenDimension.h();

	// Default SSD1306 pins for SPI (not the default here):
	/*              | function                     | Wiring/PI4J    |Cobbler | Name      |GPIO/BCM
	 * -------------+------------------------------+----------------+--------=-----------+----
	 * @param clock | Clock Pin.        Default is |RaspiPin.GPIO_14|Pin #23 |SPI0_SCLK  | 11    Clock
	 * @param mosi, | MOSI / Data Pin.  Default is |RaspiPin.GPIO_12|Pin #19 |SPI0_MOSI  | 10    Master Out Slave In
	 * @param cs,   | CS Pin.           Default is |RaspiPin.GPIO_10|Pin #24 |SPI0_CE0_N |  8    Chip Select
	 * @param rst,  | RST Pin.          Default is |RaspiPin.GPIO_05|Pin #18 |GPIO_24    | 24    Reset
	 * @param dc,   | DC Pin.           Default is |RaspiPin.GPIO_04|Pin #16 |GPIO_23    | 23    Data Control (?)
   */
	// Use WiringPi numbers.
	int ssd1306CLK  = 14;  // Physical #23
	int ssd1306MOSI = 12;  // Physical #19
	int ssd1306CS   = 10;  // Physical #24
	int ssd1306RST  = 5;   // Physical #18
	int ssd1306DC   = 4;   // Physical #16

	private static SSD1306_HDMDisplay instance = null;

	private enum OLED_INTERFACE {
		SPI, I2C
	}
	private OLED_INTERFACE oledInterface = OLED_INTERFACE.I2C; // Default

	private SSD1306 oled;
	private ScreenBuffer sb;
	private SwingLedPanel substitute;

	private boolean mirror = false; // Screen is to be seen in a mirror. (left-right mirror, not up-down, for now)
	private boolean verbose = false;

	public static SSD1306_HDMDisplay getInstance() {
		return instance;
	}

	public SSD1306_HDMDisplay() throws Exception {
		instance = this;
	}

	@Override
	public void init() {

		System.out.println("------ I N I T -------");

		try {
			// I2C Config
			if (oledInterface == OLED_INTERFACE.I2C) {
				oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS, screenWidth, screenHeight);
			} else { // SPI
				oled = new SSD1306(
						PinUtil.getPinByWiringPiNumber(ssd1306CLK),
						PinUtil.getPinByWiringPiNumber(ssd1306MOSI),
						PinUtil.getPinByWiringPiNumber(ssd1306CS),
						PinUtil.getPinByWiringPiNumber(ssd1306RST),
						PinUtil.getPinByWiringPiNumber(ssd1306DC),
						screenWidth,
						screenHeight); // See Default pins in SSD1306.
			}
			oled.begin();
			oled.clear();
		} catch (Throwable error) {
			// Not on a RPi? Try JPanel.
			oled = null;
			System.out.println("Displaying substitute Swing Led Panel");
			SwingLedPanel.ScreenDefinition screenDef = screenDimension == SCREEN_SIZE._128x64 ? SwingLedPanel.ScreenDefinition.SSD1306_128x64 : SwingLedPanel.ScreenDefinition.SSD1306_128x32;
			substitute = new SwingLedPanel(screenDef);
			substitute.setVisible(true);
		}
		sb = new ScreenBuffer(screenWidth, screenHeight);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		sb.text("Starting Forwarder...", 2, 10);
		oled.setBuffer(sb.getScreenBuffer());
		try {
			oled.display();
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
//		String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			boolean doIt = false;
			switch (sentenceId) {

				case "HDG":
					HDG hdg = StringParsers.parseHDG(str);
					this.heading = hdg.getHeading();
					if (hdg.getVariation() != -Double.MAX_VALUE && hdg.getVariation() != 0d) {
						this.declination = hdg.getVariation();
					}
					this.heading -= this.declination;
					while (this.heading < 0) {
						this.heading += 360;
					}
					if (verbose) {
						System.out.println("Something smart");
					}
					doIt = true;
					break;
				case "HDM":
					this.heading = StringParsers.parseHDM(str);
					this.heading -= this.declination; // h is true heading
					while (this.heading < 0) {
						this.heading += 360;
					}
					if (verbose) {
						System.out.println("Something smart");
					}
					doIt = true;
					break;
				case "XDR":
					// Pitch & Roll
					List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(str);
					for (StringGenerator.XDRElement xdr : xdrElements) {
						switch (xdr.getTransducerName()) {
							case "PTCH":
								this.pitch = xdr.getValue();
								doIt = true;
								break;
							case "ROLL":
								this.roll = xdr.getValue();
								doIt = true;
								break;
							default:
								break;
						}
					}
					if (verbose && doIt) {
						System.out.println("Something smart");
					}
					break;
				default:
					break;
			}
			if (doIt) {
				refreshDisplay();
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			sb.clear();
			if (oled != null) {
				oled.clear(); // Blank screen
				oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), screenWidth, screenHeight) : sb.getScreenBuffer());
				oled.display(); // Display blank screen
				oled.shutdown();
			} else {
				substitute.setVisible(false);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void refreshDisplay() {
		try {
			sb.clear();
			int line = 0;
			String display = String.format("Heading: %06.02f  ", this.heading);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
			line += 1;
			display =        String.format("Pitch  :  %05.02f  ", this.pitch);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
			line += 1;
			display =        String.format("Roll   :  %05.02f  ", this.roll);
			sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);

			// Circle, needle pointing north. If heading is 30, north is -30.
			// Center is (128 - 16, 16).
			double heading = this.heading;
			int centerX = this.screenWidth - (this.screenHeight / 2);
			int centerY = (this.screenHeight / 2);
			int radius = (this.screenHeight / 2) - 1;
			sb.circle(centerX, centerY, radius);

			// Needle points coordinates
			// Needle front (N)
			int needleTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
			int needleTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
			// Needle back (S)
			int needleBackTipX = centerX - (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
			int needleBackTipY = centerY + (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
			// Needle right side
			int needleRightTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading + 90)) * 3);
			int needleRightTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading + 90)) * 3);
			// Needle left side
			int needleLeftTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading - 90)) * 3);
			int needleLeftTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading - 90)) * 3);

			// Draw compass needle
			sb.line(centerX, centerY, needleTipX, needleTipY);                         // Center to N
			sb.line(needleBackTipX, needleBackTipY, needleRightTipX, needleRightTipY); // S to right
			sb.line(needleRightTipX, needleRightTipY, needleTipX, needleTipY);         // Right to N
			sb.line(needleTipX, needleTipY, needleLeftTipX, needleLeftTipY);           // N to left
			sb.line(needleLeftTipX, needleLeftTipY, needleBackTipX, needleBackTipY);   // left to S

			oled.setBuffer(sb.getScreenBuffer());
			oled.display();

			if (verbose) {
				System.out.println(String.format("Heading: %06.02f, Pitch: %05.02f, Roll: %05.02f",
						this.heading,
						this.pitch,
						this.roll));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void display() throws Exception {
		if (oled != null) {
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), screenWidth, screenHeight) : sb.getScreenBuffer());
			oled.display();
		} else {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), screenWidth, screenHeight) : sb.getScreenBuffer());
			substitute.display();
		}
	}

	public boolean isSimulating() {
		return (oled == null && substitute != null);
	}

	public void setSimutatorLedColor(Color c) {
		if (!isSimulating()) {
			throw new RuntimeException("Not in simulator mode");
		}
		substitute.setLedColor(c);
	}

	public static class OLEDI2CBean {
		private String cls;
		private String type = "oled-i2c";

		public OLEDI2CBean(SSD1306_HDMDisplay instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new OLEDI2CBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		// OLED flavor: SPI or I2C
		String preferredInterface = props.getProperty("screen.interface", oledInterface.toString());
		switch (preferredInterface) {
			case "SPI":
				oledInterface = OLED_INTERFACE.SPI;
				break;
			case "I2C":
			default:
				oledInterface = OLED_INTERFACE.I2C;
				break;
		}
		// SPI pins?
		// Override the default pin:  Clock              MOSI                CS               RST                DC
//  oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
		ssd1306CLK  = Integer.parseInt(props.getProperty("ssd1306.clk", String.valueOf(ssd1306CLK)));
		ssd1306MOSI = Integer.parseInt(props.getProperty("ssd1306.mosi", String.valueOf(ssd1306MOSI)));
		ssd1306CS   = Integer.parseInt(props.getProperty("ssd1306.cs", String.valueOf(ssd1306CS)));
		ssd1306RST  = Integer.parseInt(props.getProperty("ssd1306.rst", String.valueOf(ssd1306RST)));
		ssd1306DC   = Integer.parseInt(props.getProperty("ssd1306.dc", String.valueOf(ssd1306DC)));

		// Display size, mirror, verbose?
		String screenSize = props.getProperty("screen.size", "128x32");
		switch (screenSize) {
			case "128x64":
				screenDimension = SCREEN_SIZE._128x64;
				screenWidth = screenDimension.w();
				screenHeight = screenDimension.h();
				break;
			case "128x32": // Default
			default:
				break;
		}
		this.mirror = "true".equals(props.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror. (left-right mirror, not up-down, for now)
		this.verbose = "true".equals(props.getProperty("screen.verbose", "false"));
	}
}
