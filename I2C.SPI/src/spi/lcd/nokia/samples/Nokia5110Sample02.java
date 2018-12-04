package spi.lcd.nokia.samples;

import lcd.ScreenBuffer;
import spi.lcd.nokia.Nokia5110;
import utils.TimeUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Nokia5110Sample02 {
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
		}));

		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		while (go) {
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text("BSP", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
			double bsp = Math.random() * 10.0;
			String speed = NF.format(bsp);
			sb.text(speed, 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);
			lcd.setScreenBuffer(sb.getScreenBuffer());
			lcd.display();
			TimeUtil.delay(500);
		}
	}
}
