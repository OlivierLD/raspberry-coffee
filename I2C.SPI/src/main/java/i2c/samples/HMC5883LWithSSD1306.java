package i2c.samples;

import i2c.sensor.HMC5883L;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;

import java.util.Map;

public class HMC5883LWithSSD1306 {

	public static void main(String... args) {
		try {
			HMC5883L sensor = new HMC5883L();
			SSD1306 oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // I2C interface

			oled.begin();
			oled.clear();

			int WIDTH = 128;
			int HEIGHT = 32;

			ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
			sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
			if ("true".equals(System.getProperty("ssd1306.verbose", "false"))) {
				System.out.println("Screenbuffer ready...");
			}

			// Listener
			sensor.subscribe(new HMC5883L.HMC5883LEventListener() {
				@Override
				public void onNewData(Map<HMC5883L.MagValues, Double> magData) {
					// Write on oled screen
					try {
						int line = 0;
						String display = String.format("Heading: %.02f", magData.get(HMC5883L.MagValues.HEADING));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
						line += 1;
						display =        String.format("Pitch  : %.02f", magData.get(HMC5883L.MagValues.PITCH));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
						line += 1;
						display =        String.format("Roll   : %.02f", magData.get(HMC5883L.MagValues.ROLL));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);

						oled.setBuffer(sb.getScreenBuffer());
						oled.display();
						/*
						System.out.println(String.format("Heading: %.02f, Pitch: %.02f, Roll: %.02f",
								magData.get(HMC5883L.MagValues.HEADING),
								magData.get(HMC5883L.MagValues.PITCH),
								magData.get(HMC5883L.MagValues.ROLL)));
						 */
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});

			sensor.setWait(250);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				synchronized (sensor) {
					sensor.stopReading();
					try {
						Thread.sleep(sensor.getWait());
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
				oled.clear();
				oled.shutdown();
			}, "Shutdown Hook"));
			sensor.startReading();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
}
