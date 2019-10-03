package i2c.samples.oled;

import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;

import java.awt.Color;
import java.security.InvalidParameterException;

/**
 * Uses the I2C interface.
 * Usage:
 * $ java ... i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "Blah"
 *
 * To display on several lines, use "|" to separate them:
 * $ java ... i2c.samples.oled.OLEDSSD1306_I2C_DisplayStrings "First|Second line|Finally, third one."
 *
 */
public class OLEDSSD1306_I2C_DisplayStrings {

	private static SwingLedPanel substitute;

	private enum SCREEN_SIZE {
		_128x32,
		_128x64
	}

	private enum LED_COLOR {
		RED("red", Color.RED),
		GREEN("green", Color.GREEN),
		WHITE("white", Color.WHITE);

		private final Color ledColor;
		private final String colorName;

		LED_COLOR(String colorName, Color ledColor) {
			this.ledColor = ledColor;
			this.colorName = colorName;
		}
		public String colorName() { return this.colorName; }
		public Color ledColor() { return this.ledColor; }
	}

	public static void main(String... args) throws Exception {

		String displayText;
		if (args.length == 0) {
			displayText = "Lorem ipsum dolor sit|amet, tellus tempus|vitae tempor|pellentesque. Lobortis|condimentum tortor|volutpat ipsum augue,...";
		} else {
			displayText = args[0];
		}
		int width = 128;
		int height = 32;

		LED_COLOR ledColor = LED_COLOR.GREEN;

		height = Integer.parseInt(System.getProperty("oled.height", String.valueOf(height)));
		OLEDSSD1306_I2C_DisplayStrings.SCREEN_SIZE screenDimension;
		if (height != 32 && height != 64) {
			throw new InvalidParameterException(String.format("oled.height must be 32 or 64, not %d", height));
		} else {
			screenDimension = (height == 32) ? SCREEN_SIZE._128x32 : SCREEN_SIZE._128x64;
		}

		String[] buffer = displayText.split("\\|");
		SSD1306 oled;
		try {
			oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS, width, height); // I2C interface
			oled.begin();
			oled.clear();
  //  oled.display();
		} catch (Throwable error) {
			// Not on a RPi? Try JPanel.
			oled = null;
			System.out.println("Displaying substitute Swing Led Panel");
			SwingLedPanel.ScreenDefinition screenDef = screenDimension == SCREEN_SIZE._128x64 ? SwingLedPanel.ScreenDefinition.SSD1306_128x64 : SwingLedPanel.ScreenDefinition.SSD1306_128x32;
			substitute = new SwingLedPanel(screenDef);
			String ledColorName = System.getProperty("led.color", ledColor.colorName());
			for (LED_COLOR color : LED_COLOR.values()) {
				if (color.colorName().equals(ledColorName)) {
					ledColor = color;
					break;
				}
			}
			substitute.setLedColor(ledColor.ledColor());
			substitute.setVisible(true);
		}
		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		ScreenBuffer sb = new ScreenBuffer(width, height);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		for (int i=0; i<buffer.length; i++) {
			sb.text(buffer[i], 2, 10 + (i * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
		}
		if (oled != null) {
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), width, height) : sb.getScreenBuffer());
			oled.display();
			oled.shutdown();
		} else {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), width, height) : sb.getScreenBuffer());
			substitute.display();
		}
	}
}
