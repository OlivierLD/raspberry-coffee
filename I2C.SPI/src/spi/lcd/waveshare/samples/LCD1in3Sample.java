package spi.lcd.waveshare.samples;

import spi.lcd.waveshare.LCD1in3;
import utils.TimeUtil;

public class LCD1in3Sample {

	public static void main(String... args) {
		LCD1in3 lcd = new LCD1in3();
		TimeUtil.delay(2_000L);
		lcd.shutdown();
	}

}
