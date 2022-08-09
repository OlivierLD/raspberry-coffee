package i2c.samples.oled;

import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses the I2C interface
 * Ctrl-C to stop.
 */
public class OLEDSSD1306_I2C_ClockSample {

	private final static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MMM-dd");
	private final static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");

	public static void main(String... args) throws Exception {
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Starting...");
		}
		SSD1306 oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // I2C interface
		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		oled.begin();
		oled.clear();
    //  oled.display();

		int WIDTH = 128;
		int HEIGHT = 32;

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
					currentThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "I'm the killer"));

		System.out.println("Look at your screen...");

		while (keepGoing.get()) {
			Date now = new Date();
			String date = SDF_DATE.format(now);
			String time = SDF_TIME.format(now);

			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);

			sb.text(date, 2, 8);
			sb.text(time, 2, 17);

			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

			TimeUtil.delay(1_000L);
		}

		sb.clear();
		oled.clear(); // Blank screen
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();

		oled.shutdown();
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Done.");
		}
	}
}
