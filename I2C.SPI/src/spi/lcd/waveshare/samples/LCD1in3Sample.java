package spi.lcd.waveshare.samples;

import spi.lcd.waveshare.LCD1in3;
import spi.lcd.waveshare.fonts.Font16;
import spi.lcd.waveshare.fonts.Font20;
import spi.lcd.waveshare.fonts.Font24;
import utils.StaticUtil;
import utils.TimeUtil;

import static spi.lcd.waveshare.LCD1in3.DrawFill.DRAW_FILL_EMPTY;

public class LCD1in3Sample {

	private static void drawKeyListenInit(LCD1in3 lcd) {
		lcd.GUIClear(lcd.WHITE);

		/* Press */
		lcd.GUIDrawCircle(90, 120, 25, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 112, "P", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Left */
		lcd.GUIDrawRectangle(15, 95, 65, 145, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(32, 112, "L", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Down */
		lcd.GUIDrawRectangle(65, 145, 115, 195, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 162, "D", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Right */
		lcd.GUIDrawRectangle(115, 95, 165, 145, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(132, 112, "R", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Up */
		lcd.GUIDrawRectangle(65, 45, 115, 95, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(82, 62, "U", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key1 */
		lcd.GUIDrawRectangle(185, 35, 235, 85, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 52, "K1", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key2	*/
		lcd.GUIDrawRectangle(185, 95, 235, 145, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 112, "K2", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		/* Key3 */
		lcd.GUIDrawRectangle(185, 155, 235, 205, lcd.RED, DRAW_FILL_EMPTY, LCD1in3.DOT_PIXEL_DFT);
		lcd.GUIDrawString(195, 172, "K3", Font24.getInstance(), lcd.IMAGE_BACKGROUND, LCD1in3.BLUE);

		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
		}
	}

	public static void main(String... args) {
		LCD1in3 lcd = new LCD1in3(LCD1in3.HORIZONTAL, LCD1in3.BLUE);
		lcd.GUINewImage(LCD1in3.IMAGE_RGB, LCD1in3.LCD_WIDTH, LCD1in3.LCD_HEIGHT, LCD1in3.IMAGE_ROTATE_0, LCD1in3.IMAGE_COLOR_POSITIVE);
		lcd.GUIClear(LCD1in3.WHITE);

		lcd.setKey1Consumer((event) -> System.out.println(String.format("Key 1 from main: Pin: %s, State: %s", event.getPin().toString(), event.getState().toString())));
		// TODO Others

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

		lcd.GUIDrawRectangle(20, 10, 70, 60, LCD1in3.BLUE, DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawRectangle(85, 10, 130, 60, LCD1in3.BLUE, LCD1in3.DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Circles");

		lcd.GUIDrawCircle(170, 35, 20, LCD1in3.GREEN, DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawCircle(170, 85, 20, LCD1in3.GREEN, LCD1in3.DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		System.out.println("...Strings");

		lcd.GUIDrawString(5, 70, "hello world", Font16.getInstance(), LCD1in3.WHITE, LCD1in3.BLACK);
		lcd.GUIDrawString(5, 90, "OlivSoft rocks!", Font20.getInstance(), LCD1in3.RED, LCD1in3.CYAN);
		lcd.GUIDrawString(5, 120, "WaveShare", Font24.getInstance(), LCD1in3.BLUE, lcd.IMAGE_BACKGROUND);

		System.out.println("Displaying...");
		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
		}

		// Wait for CR
		StaticUtil.userInput("Hit Return to move on...");

		drawKeyListenInit(lcd);

		// Wait for CR
		StaticUtil.userInput("Hit Return to finish");

		lcd.LCDClear(LCD1in3.BLACK);
		if (!lcd.isSimulating()) {
			lcd.shutdown();
		}
	}
}
