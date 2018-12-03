package spi.lcd.nokia.samples;

import lcd.ScreenBuffer;
import spi.lcd.nokia.Nokia5110;
import utils.PinUtil;

public class Nokia5110Sample {
	public static void main(String... args) {

		System.out.println("Nokia pins - from the TOP (seeing the screen), bottom pins (P1):");
		System.out.println("|                           |");
		System.out.println("+------o-o-o-o-o-o-o-o------+");
		System.out.println("       | | | | | | | |");
		System.out.println("       | | | | | | | GND");
		System.out.println("       | | | | | | VCC");
		System.out.println("       | | | | | CLK");
		System.out.println("       | | | | DIN");
		System.out.println("       | | | D/C");
		System.out.println("       | | CS");
		System.out.println("       | RST");
		System.out.println("       LED");
		System.out.println();

		String[] map = new String[7];
		map[0] = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber()) + ":" + "VCC";
		map[1] = String.valueOf(PinUtil.GPIOPin.GRND_1.pinNumber()) + ":" + "GND";
		map[2] = String.valueOf(PinUtil.GPIOPin.GPIO_4.pinNumber()) + ":" + "D/C";
		map[3] = String.valueOf(PinUtil.GPIOPin.GPIO_5.pinNumber()) + ":" + "RST";
		map[4] = String.valueOf(PinUtil.GPIOPin.GPIO_10.pinNumber()) + ":" + "CS";
		map[5] = String.valueOf(PinUtil.GPIOPin.GPIO_14.pinNumber()) + ":" + "CLK";
		map[6] = String.valueOf(PinUtil.GPIOPin.GPIO_12.pinNumber()) + ":" + "DIN";
//	map[7] = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber()) + ":" + "LED and VCC";

		PinUtil.print(map);
		System.out.println("VCC and LED are connected. This is also where a pot would go.");
		System.out.println();

		System.out.println("Starting");
		Nokia5110 lcd = new Nokia5110();
		lcd.begin();

		lcd.clear();
		lcd.display();
		System.out.println("Ready");

		lcd.setScreenBuffer(Nokia5110.ADAFRUIT_LOGO);
//  lcd.data(Nokia5110.ADAFRUIT_LOGO);
		System.out.println("Displaying...");
		lcd.display();
		System.out.println("Displayed");
		try {
			Thread.sleep(5_000L);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		ScreenBuffer sb = new ScreenBuffer(84, 48);
		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("Hello Nokia!", 5, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 5, 30, ScreenBuffer.Mode.BLACK_ON_WHITE);
		lcd.setScreenBuffer(sb.getScreenBuffer());
		lcd.display();
		try {
			Thread.sleep(2_000);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("Hello Nokia!", 5, 20, ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("I speak Java!", 5, 30, ScreenBuffer.Mode.WHITE_ON_BLACK);
		lcd.setScreenBuffer(sb.getScreenBuffer());
		lcd.display();
		try {
			Thread.sleep(2_000);
		} catch (Exception ex) {
		}

		sb.clear();
		for (int i = 0; i < 8; i++) {
			sb.rectangle(1 + (i * 2), 1 + (i * 2), 83 - (i * 2), 47 - (i * 2));
//    lcd.setScreenBuffer(sb.getScreenBuffer());
//    lcd.display();
			//  try { Thread.sleep(100); } catch (Exception ex) {}
		}
		lcd.setScreenBuffer(sb.getScreenBuffer());
		lcd.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("Pi=", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("3.1415926", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);
		lcd.setScreenBuffer(sb.getScreenBuffer());
		lcd.display();
//  sb.dumpScreen();
		try {
			Thread.sleep(5_000);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("Pi=", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK, true);
		sb.text("3.1415926", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK, true);
		lcd.setScreenBuffer(sb.getScreenBuffer());
		lcd.display();
		//  sb.dumpScreen();
		try {
			Thread.sleep(5_000);
		} catch (Exception ex) {
		}

		lcd.clear();
		lcd.display();
		lcd.shutdown();
		System.out.println("Done");
	}
}
