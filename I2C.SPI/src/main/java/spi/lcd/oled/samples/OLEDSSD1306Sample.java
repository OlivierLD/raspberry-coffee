package spi.lcd.oled.samples;

import lcd.oled.SSD1306;
import lcd.utils.img.ImgInterface;
import lcd.utils.img.Java32x32;

import java.awt.Point;
import java.awt.Polygon;

import lcd.ScreenBuffer;

/**
 * Uses SPI interface
 */
public class OLEDSSD1306Sample {

	public static void main(String... args) throws Exception {
		if ("true".equals(System.getProperty("verbose", "false")))
			System.out.println("Starting...");
		SSD1306 oled = new SSD1306(); // Default pins (look in the SSD1306 code)
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

		int WIDTH = 128;
		int HEIGHT = 32;

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Screenbuffer ready...");
		}

		if (false) {
			sb.text("ScreenBuffer", 2, 8);
			sb.text("128 x 32 for OLED", 2, 17);
			sb.line(0, 19, 131, 19);
			sb.line(0, 32, 125, 19);
		}

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Let's go...");
		}
		ImgInterface img = new Java32x32();
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);

		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();

		try {
			Thread.sleep(2_000);
		} catch (Exception ex) {
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
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.image(img, 0, 0, ScreenBuffer.Mode.WHITE_ON_BLACK);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.WHITE_ON_BLACK);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
		sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		// End blinking
		sb.clear();
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		// Marquee
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Marquee...");
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


		sb.circle(64, 16, 15);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		sb.circle(74, 16, 10);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		sb.circle(80, 16, 5);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(500);
		} catch (Exception ex) {
		}

		// Lines
		sb.clear();
		sb.line(1, 1, 126, 30);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.line(126, 1, 1, 30);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.line(1, 25, 120, 10);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.line(10, 5, 10, 30);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.line(1, 5, 120, 5);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		// Rectangles
		sb.clear();
		sb.rectangle(5, 10, 100, 25);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.rectangle(15, 3, 50, 30);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		sb.clear();
		for (int i = 0; i < 8; i++) {
			sb.rectangle(1 + (i * 2), 1 + (i * 2), 127 - (i * 2), 31 - (i * 2));
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
			//  try { Thread.sleep(100); } catch (Exception ex) {}
		}
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		// Arc & plot
		sb.clear();
		sb.arc(64, 16, 10, 20, 90);
		sb.plot(64, 16);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
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
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
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
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}
		// sb.clear();
		txt = "A much longer string.";
		len = sb.strlen(txt);
		sb.text(txt, 64 - (len / 2), 26);
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
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
						"We're reaching the end",
						"* The End *"
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
				double virtualAngle = Math.PI * (((c - i) % 32) / 32d);
				int xpos = strOffset - i,
						ypos = 26 + (int) (16 * Math.sin(virtualAngle));
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
		sb.line(0, 16, 128, 16);
		sb.line(2, 0, 2, 32);

		Point prev = null;
		for (int _x = 0; _x < 130; _x++) {
			double amplitude = 6 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 16 - (int) (amplitude * Math.cos(Math.toRadians(360 * _x / 16d)));
			sb.plot(_x + 2, _y);
			if (prev != null) {
				sb.line(prev.x, prev.y, _x + 2, _y);
			}
			prev = new Point(_x + 2, _y);
		}
		oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
		oled.display();
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		// A curve (progressing)
		sb.clear();
		// Axis
		sb.line(0, 16, 128, 16);
		sb.line(2, 0, 2, 32);

		prev = null;
		for (int _x = 0; _x < 130; _x++) {
			double amplitude = 6 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 16 - (int) (amplitude * Math.cos(Math.toRadians(360 * _x / 16d)));
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
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

		// Bouncing
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Bouncing...");
		}
		for (int _x = 0; _x < 130; _x++) {
			sb.clear();
			double amplitude = 6 * Math.exp((double) (130 - _x) / (13d * 7.5d));
			//  System.out.println("X:" + x + ", ampl: " + (amplitude));
			int _y = 32 - (int) (amplitude * Math.abs(Math.cos(Math.toRadians(180 * _x / 10d))));
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
		try {
			Thread.sleep(1_000);
		} catch (Exception ex) {
		}

//  sb.dumpScreen();

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
