package spi.lcd.nokia.samples;

import lcd.ScreenBuffer;
import spi.lcd.nokia.Nokia5110;
import utils.TimeUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Clock
 */
public class Nokia5110Sample03 {

	private final static SimpleDateFormat HMS = new SimpleDateFormat("HH:mm:ss");

	private static boolean go = true;

	public static void main(String... args) {
		NumberFormat NF = new DecimalFormat("00.00");
		final Nokia5110 lcd = new Nokia5110();

		ScreenBuffer sb = new ScreenBuffer(84, 48);
		lcd.begin();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Clear
			sb.clear();
			lcd.setScreenBuffer(sb.getScreenBuffer());
			lcd.display();
			// Bye
			lcd.shutdown();
			System.out.println("\nExiting");
			go = false;
		}, "Shutdown Hook"));

		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		while (go) {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text("Time:", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
			String speed = HMS.format(new Date());
			sb.text(speed, 4, 19, 1, ScreenBuffer.Mode.WHITE_ON_BLACK);
			lcd.setScreenBuffer(sb.getScreenBuffer());
			lcd.display();
			TimeUtil.delay(500);
		}
	}
}
