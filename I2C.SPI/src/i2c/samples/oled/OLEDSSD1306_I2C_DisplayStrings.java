package i2c.samples.oled;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.utils.img.ImgInterface;
import lcd.utils.img.Java32x32;

import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;

/**
 * Uses the I2C interface
 */
public class OLEDSSD1306_I2C_DisplayStrings {

	public static void main(String... args) throws UnsupportedBusNumberException, IOException {

		SSD1306 oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // I2C interface
		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		oled.begin();
		oled.clear();
//  oled.display();

		int WIDTH = 128;
		int HEIGHT = 32;

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

		for (int i=0; i<args.length; i++) {
			sb.text(args[i], 2, 10 + (i * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
		}
		oled.setBuffer(mirror ? SSD1306.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();

		oled.shutdown();
	}
}
