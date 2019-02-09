package spi.lcd.waveshare.samples;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import spi.lcd.waveshare.LCD1in3;
import spi.lcd.waveshare.fonts.Font16;
import spi.lcd.waveshare.fonts.Font20;
import spi.lcd.waveshare.fonts.Font24;
import utils.StaticUtil;
import utils.TimeUtil;

import java.util.function.Consumer;

import static spi.lcd.waveshare.LCD1in3.DrawFill;

public class LCD1in3ClockSample {

	public static void main(String... args) {

		LCD1in3 lcd = new LCD1in3(LCD1in3.HORIZONTAL, LCD1in3.BLUE);

		lcd.GUINewImage(LCD1in3.IMAGE_RGB, LCD1in3.LCD_WIDTH, LCD1in3.LCD_HEIGHT, LCD1in3.IMAGE_ROTATE_0, LCD1in3.IMAGE_COLOR_POSITIVE);
		lcd.GUIClear(LCD1in3.WHITE);

//	lcd.setKey1Consumer((event) -> System.out.println(String.format(">> FROM CUSTOM CONSUMER, Key 1 from main: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString())));

		TimeUtil.delay(500L);

		System.out.println("...Dots");

		/* 2.Drawing on the image */
		lcd.GUIDrawPoint(5, 10, LCD1in3.RED, LCD1in3.DotPixel.DOT_PIXEL_1X1, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 25, LCD1in3.RED, LCD1in3.DotPixel.DOT_PIXEL_2X2, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 40, LCD1in3.BLACK, LCD1in3.DotPixel.DOT_PIXEL_3X3, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 55, LCD1in3.BLACK, LCD1in3.DotPixel.DOT_PIXEL_4X4, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 70, LCD1in3.GREEN, LCD1in3.DotPixel.DOT_PIXEL_5X5, LCD1in3.DOT_STYLE_DFT);
		lcd.GUIDrawPoint(5, 85, LCD1in3.GREEN, LCD1in3.DotPixel.DOT_PIXEL_6X6, LCD1in3.DOT_STYLE_DFT);

		System.out.println("...Lines");

		lcd.GUIDrawLine(10, 10, 10, 200, LCD1in3.BROWN, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_2X2);
		lcd.GUIDrawLine(10, 10, 230, 230, LCD1in3.BROWN, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_2X2);

		lcd.GUIDrawLine(20, 10, 70, 60, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(70, 10, 20, 60, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_SOLID, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(170, 15, 170, 55, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_DOTTED, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawLine(150, 35, 190, 35, LCD1in3.RED, LCD1in3.LineStyle.LINE_STYLE_DOTTED, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Rectangles");

		lcd.GUIDrawRectangle(20, 10, 70, 60, LCD1in3.BLUE, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawRectangle(85, 10, 130, 60, LCD1in3.BLUE, DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Circles");

		lcd.GUIDrawCircle(170, 35, 20, LCD1in3.GREEN, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawCircle(170, 85, 20, LCD1in3.GREEN, DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Strings");

		lcd.GUIDrawString(5, 70, "hello world", Font16.getInstance(), LCD1in3.WHITE, LCD1in3.BLACK);
		lcd.GUIDrawString(5, 90, "OlivSoft rocks!", Font20.getInstance(), LCD1in3.RED, LCD1in3.CYAN);
		lcd.GUIDrawString(5, 120, "WaveShare", Font24.getInstance(), LCD1in3.BLUE, lcd.IMAGE_BACKGROUND);

		System.out.println("Displaying...");
		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
		}

		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.LCDDisplay();
//		lcd.shutdown();
		}

		StaticUtil.userInput("Hit Return to finish.");

		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			lcd.GUIDrawString(30, 70, "Bye!", Font24.getInstance(), LCD1in3.BLACK, LCD1in3.WHITE);
			lcd.LCDDisplay();
			TimeUtil.delay(1_000);

			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			lcd.LCDDisplay();

			TimeUtil.delay(1_000);
			lcd.shutdown();
		}
		System.out.println("End of Sample");
		System.out.println("Bye.");
	}
}
