package samples;

import com.pi4j.io.gpio.RaspiPin;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import phonekeyboard3x4.KeyboardController;

/*
 * A phone keypad, and a 128x32 oled screen
 */
public class OLEDScreenAndKeypad {
	private KeyboardController kbc;
	private SSD1306 oled;
	private ScreenBuffer sb;

	// This one overrides the default pins for the OLED
	public OLEDScreenAndKeypad() throws Exception {
		kbc = new KeyboardController();
		// Override the default pins     Clock             MOSI (data)       CS                RST               DC
		oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
		oled.begin();
		oled.clear();

		sb = new ScreenBuffer(128, 32);

		reset();
	}

	public void display(String txt) throws Exception {
		sb.text(txt, 2, 17);
		oled.setBuffer(sb.getScreenBuffer());
		oled.display();
	}

	public void userInput() throws Exception {
		StringBuffer charBuff = new StringBuffer();
		boolean go = true;
		while (go) {
			char c = kbc.getKey();
//    System.out.println("At " + System.currentTimeMillis() + ", Char: " + c);
			if (c == '#') {
				go = false;
			} else if (c == '*') {
				charBuff = new StringBuffer();
				reset();
			} else {
				charBuff.append(c);
			}
			display(charBuff.toString());
			try {
				Thread.sleep(200L);
			} catch (Exception ex) {
			}
		}
		reset();
		display("Bye-bye");
		System.out.println("Bye");
		kbc.shutdown();
		try {
			Thread.sleep(1_000L);
		} catch (Exception ex) {
		}
		clear();
		oled.shutdown();
	}

	public void reset() throws Exception {
		sb.clear();
		oled.clear();
		sb.text("* = Reset, # = Exit.", 2, 8);
		oled.setBuffer(sb.getScreenBuffer());
		oled.display();
	}

	public void clear()throws Exception  {
		sb.clear();
		oled.clear();
		oled.setBuffer(sb.getScreenBuffer());
		oled.display();
	}

	public static void main(String... args) throws Exception {
		System.out.println("Hit * to reset");
		System.out.println("Hit # to exit");
		OLEDScreenAndKeypad ui = new OLEDScreenAndKeypad();
		ui.userInput();
	}
}
