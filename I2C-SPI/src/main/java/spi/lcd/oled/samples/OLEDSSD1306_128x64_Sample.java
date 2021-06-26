package spi.lcd.oled.samples;

import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.utils.img.ImgInterface;
import lcd.utils.img.Java32x32;
import utils.StaticUtil;

import java.awt.*;

/**
 * Uses SPI interface
 * Displays text, images, figures, on the screen,
 * and on the console, if onUserReturn == true. (-Dreturn.to.move.on=true)
 */
public class OLEDSSD1306_128x64_Sample {

	public static void main(String... args) throws Exception {
		int WIDTH = 128;
		int HEIGHT = 64;

		boolean onUserReturn = "true".equals(System.getProperty("return.to.move.on"));

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Starting...");
		}
		SSD1306 oled = new SSD1306(WIDTH, HEIGHT); // Default pins (look in the SSD1306 code)
		// Override the default pins        Clock             MOSI              CS                RST               DC
//  oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Object created, default pins...");
//    System.out.println("Object created, Clock GPIO_12, MOSI GPIO_13, CS GPIO_14, RST GPIO_15, DC GPIO_16");
		}

		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		oled.begin();
		oled.clear();
//  oled.display();

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Screenbuffer ready...");
		}

		if (true) {
			sb.text("1 - ScreenBuffer", 2, 8);
			sb.text("2 - 128 x 64 for OLED", 2, 16);
			sb.text("3 - ScreenBuffer, 64 lines", 2, 24);
			sb.text("4 - 128 x 64 for OLED!", 2, 32);
			sb.text("5 - ScreenBuffer Deg: \272", 2, 40);
			sb.text("6 - 128 x 64 for OLED!", 2, 48);
			sb.text("7 - 128 x 64 for OLED!", 2, 56);
			sb.text("8 - This is the end", 2, 64);

			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();

			if (onUserReturn) {
				sb.dumpScreen();
				StaticUtil.userInput("Hit Return");
			} else {
				try {
					Thread.sleep(1_000);
				} catch (Exception ex) {
				}
			}
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		}

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Let's go...");
		}
		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		ImgInterface img = new Java32x32();
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);

		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();

		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(2_000);
			} catch (Exception ex) {
			}
		}
		// Blinking
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Blinking...");
		}
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.image(img, 0, 0, ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.WHITE_ON_BLACK);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}
		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.image(img, 0, 0, ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.WHITE_ON_BLACK);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		// End blinking
		sb.clear();
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		// Marquee
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Marquee shifting left...");
		}
		for (int i = 0; i < 128; i++) {
			oled.clear();
			sb.image(img, 0 - i, 0);
			sb.text("I speak Java!.......", 36 - i, 20);

			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
//    try { Thread.sleep(250); } catch (Exception ex) {}
		}

		// Circles
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Geometric shapes...");
		}
		sb.clear();

		sb.circle(64, 32, 15);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		sb.circle(74, 32, 10);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		sb.circle(80, 32, 5);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(500);
			} catch (Exception ex) {
			}
		}

		// Nested Rectangles
		sb.clear();
		for (int i = 0; i < 16; i++) {
			sb.rectangle(1 + (i * 2), 1 + (i * 2), 127 - (i * 2), 63 - (i * 2));
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
	//  try { Thread.sleep(100); } catch (Exception ex) {}
		}
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}

		// Shape
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("More shapes...");
		}
		sb.clear();
		// Star
		int[] x = new int[]{64, 73, 50, 78, 55};
		int[] y = new int[]{1, 30, 12, 12, 30};
		Polygon p = new Polygon(x, y, 5);
		sb.shape(p, true);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}

		// Centered text
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("More text...");
		}
		sb.clear();
		String txt = "Centered";
		int len = sb.strlen(txt);
		sb.text(txt, 64 - (len / 2), 16);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}
		// sb.clear();
		txt = "A much longer string.";
		len = sb.strlen(txt);
		sb.text(txt, 64 - (len / 2), 26);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}

		// Vertical marquee
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Vertical marquee...");
		}
		String[] txtA = new String[]{
						"Centered",
						"This is line one",
						"More text goes here",
						"Some crap follows",
						"We're reaching the end.",
				    "Long ago, in a galaxy far,",
				    "far away... Starwars effect",
						"  * The End *  "
		};
		len = 0;
		sb.clear();
		for (int t = 0; t < 80; t++) {
//    sb.clear();
			for (int i = 0; i < txtA.length; i++) {
				len = sb.strlen(txtA[i]);
				sb.text(txtA[i], 64 - (len / 2), (10 * (i + 1)) - t);
				oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				oled.display();
			}
//    try { Thread.sleep(100); } catch (Exception ex) {}
		}

		// Text Snake...
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Text snake...");
		}
		String snake = "This text is displayed like a snake, waving across the screen...";
		char[] ca = snake.toCharArray();
		int strlen = sb.strlen(snake);
		// int i = 0;
		for (int i = 0; i < strlen + 2; i++) {
			sb.clear();
			for (int c = 0; c < ca.length; c++) {
				int strOffset = 0;
				if (c > 0) {
					String tmp = new String(ca, 0, c);
		//    System.out.println(tmp);
					strOffset = sb.strlen(tmp) + 2;
				}
				double virtualAngle = Math.PI * (((c - i) % 64) / 64d);
				int xpos = strOffset - i,
						ypos = 58 + (int) (32 * Math.sin(virtualAngle));
//      System.out.println("Displaying " + ca[c] + " at " + x + ", " + y + ", i=" + i + ", strOffset=" + strOffset);
				sb.text(new String(new char[]{ca[c]}), xpos, ypos);
			}
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
		}

		// A curve
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Curve...");
		}
		sb.clear();
		// Axis
		sb.line(0, 32, 128, 32);
		sb.line(2, 0, 2, 64);

		Point prev = null;
		for (int _x = 0; _x < 130; _x++) {
			double amplitude = 12 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 32 - (int) (amplitude * Math.cos(Math.toRadians(360 * _x / 32d)));
			sb.plot(_x + 2, _y);
			if (prev != null) {
				sb.line(prev.x, prev.y, _x + 2, _y);
			}
			prev = new Point(_x + 2, _y);
		}
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}
		// A curve (progressing)
		sb.clear();
		// Axis
		sb.line(0, 32, 128, 32);
		sb.line(2, 0, 2, 64);

		prev = null;
		for (int _x = 0; _x < 130; _x++) {
			double amplitude = 12 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 32 - (int) (amplitude * Math.cos(Math.toRadians(360 * _x / 32d)));
			sb.plot(_x + 2, _y);
			if (prev != null) {
				sb.line(prev.x, prev.y, _x + 2, _y);
			}
			prev = new Point(_x + 2, _y);
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
		}
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}
		// Bouncing
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Bouncing...");
		}
		for (int _x = 0; _x < 130; _x++) {
			sb.clear();
			double amplitude = 12 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 64 - (int) (amplitude * Math.abs(Math.cos(Math.toRadians(180 * _x / 10d))));
			sb.plot(_x, _y);
			sb.plot(_x + 1, _y);
			sb.plot(_x + 1, _y + 1);
			sb.plot(_x, _y + 1);

			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
//    try { Thread.sleep(75); } catch (Exception ex) {}
		}
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		if (onUserReturn) {
			sb.dumpScreen();
			StaticUtil.userInput("Hit Return");
		} else {
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}

		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Closing...");
		}
		sb.clear();
		oled.clear();
		sb.text("Bye-bye!", 36, 20);

		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();

		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
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
