package i2c.samples.oled;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import java.io.IOException;

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
	private static SSD1306 oled;

	public static enum SCREEN_SIZE {
		_128x32,
		_128x64
	};
	private static SCREEN_SIZE screenDimension = SCREEN_SIZE._128x32;

	public static void main(String... args) throws Exception {

		if (args.length == 0) {
			return;
		}

		String[] buffer = args[0].split("\\|");
		try {
			oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // I2C interface
			oled.begin();
			oled.clear();
  //  oled.display();
		} catch (Throwable error) {
			// Not on a RPi? Try JPanel.
			oled = null;
			System.out.println("Displaying substitute Swing Led Panel");
			SwingLedPanel.ScreenDefinition screenDef = screenDimension == SCREEN_SIZE._128x64 ? SwingLedPanel.ScreenDefinition.SSD1306_128x64 : SwingLedPanel.ScreenDefinition.SSD1306_128x32;
			substitute = new SwingLedPanel(screenDef);
			substitute.setVisible(true);
		}
		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		int WIDTH = 128;
		int HEIGHT = 32;

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		for (int i=0; i<buffer.length; i++) {
			sb.text(buffer[i], 2, 10 + (i * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
		}
		if (oled != null) {
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

			oled.shutdown();
		} else {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			substitute.display();
		}
	}
}
