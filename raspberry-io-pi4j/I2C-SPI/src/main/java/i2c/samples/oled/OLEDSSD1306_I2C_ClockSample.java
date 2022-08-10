package i2c.samples.oled;

import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import utils.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses the I2C interface
 * Ctrl-C to stop.
 */
public class OLEDSSD1306_I2C_ClockSample {

	private final static SimpleDateFormat SDF_DATE = new SimpleDateFormat("dd-MM-yyyy");
	private final static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");

	public static void main(String... args) throws Exception {
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Starting...");
		}

		SSD1306 oled;
		SwingLedPanel substitute = null;

		int WIDTH = 128;
		int HEIGHT = 32;

		try {
			oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // I2C interface
			oled.begin();
			oled.clear();
			//  oled.display();
		} catch (Throwable error) {
			// Not on a RPi? Try JPanel.
			oled = null;
			System.out.println("Displaying substitute Swing Led Panel");
			SwingLedPanel.ScreenDefinition screenDef = SwingLedPanel.ScreenDefinition.SSD1306_128x32;
			substitute = new SwingLedPanel(screenDef, true);

			substitute.setLedColor(Color.RED);
			substitute.setVisible(true);
		}
		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("ScreenBuffer ready...");
		}

		AtomicBoolean keepGoing = new AtomicBoolean(true);
		final Thread currentThread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Ah!");
			keepGoing.set(false);
			synchronized (currentThread) {
				try {
					TimeUtil.delay(2f);
					currentThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "I'm the killer"));

		System.out.println("\tLook at your screen, not here...");

		while (keepGoing.get()) {
			Date now = new Date();
			String date = SDF_DATE.format(now);
			String time = SDF_TIME.format(now);

			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			int fontFactor = 2;
			sb.text(date, 2, 1 + (fontFactor * 3) + (0 * (fontFactor * 8)), fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text(time, 2, 1 + (fontFactor * 3) + (1 * (fontFactor * 8)), fontFactor, ScreenBuffer.Mode.WHITE_ON_BLACK);

			if (oled != null) {
				oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				oled.display();
			} else if (substitute != null) {
				substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				substitute.display();
			}

			TimeUtil.delay(1_000L);
		}

		sb.clear();
		if (oled != null) {
			oled.clear(); // Blank screen
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

			oled.shutdown();
		} else if (substitute != null) {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			substitute.display();
			substitute.dispose();
		}
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Done.");
		}
	}
}
