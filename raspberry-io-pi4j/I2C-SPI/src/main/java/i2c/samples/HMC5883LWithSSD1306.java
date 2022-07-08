package i2c.samples;

import i2c.sensor.HMC5883L;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;

import java.util.Map;

public class HMC5883LWithSSD1306 {

	// Screen dimensions
	private final static int WIDTH = 128;
	private final static int HEIGHT = 32;

	private final static boolean verbose = "true".equals(System.getProperty("ssd1306.compass.verbose", "false"));

	public static void main(String... args) throws Exception {

		HMC5883L sensor;
		SSD1306 oled;

//		 try {
			try {
				sensor = new HMC5883L(); // Magnetometer
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}
			try {
				oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS); // OLED Screen, I2C interface
			} catch (Exception ex) {
				ex.printStackTrace();
				throw ex;
			}

			oled.begin();
			oled.clear();

			ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
			sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
			sb.text("Starting ...", 2, 10);
			oled.setBuffer(sb.getScreenBuffer());
			oled.display();

			// Listener
			sensor.subscribe(new HMC5883L.HMC5883LEventListener() {
				@Override
				public void onNewData(Map<HMC5883L.MagValues, Double> magData) {
					// Write on oled screen
					try {
						sb.clear();
						int line = 0;
						String display = String.format("Heading: %06.02f  ", magData.get(HMC5883L.MagValues.HEADING));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
						line += 1;
						display =        String.format("Pitch  :  %05.02f  ", magData.get(HMC5883L.MagValues.PITCH));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);
						line += 1;
						display =        String.format("Roll   :  %05.02f  ", magData.get(HMC5883L.MagValues.ROLL));
						sb.text(display, 2, 10 + (line * 10), ScreenBuffer.Mode.WHITE_ON_BLACK);

						// Circle, needle pointing north. If heading is 30, north is -30.
						// Center is (128 - 16, 16).
						double heading = magData.get(HMC5883L.MagValues.HEADING);
						int centerX = WIDTH - (HEIGHT / 2);
						int centerY = (HEIGHT / 2);
						int radius = (HEIGHT / 2) - 1;
						sb.circle(centerX, centerY, radius);

						// Needle points coordinates
						// Needle front (N)
						int needleTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
						int needleTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
						// Needle back (S)
						int needleBackTipX = centerX - (int)Math.round(Math.sin(Math.toRadians(- heading)) * radius);
						int needleBackTipY = centerY + (int)Math.round(Math.cos(Math.toRadians(- heading)) * radius);
						// Needle right side
						int needleRightTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading + 90)) * 3);
						int needleRightTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading + 90)) * 3);
						// Needle left side
						int needleLeftTipX = centerX + (int)Math.round(Math.sin(Math.toRadians(- heading - 90)) * 3);
						int needleLeftTipY = centerY - (int)Math.round(Math.cos(Math.toRadians(- heading - 90)) * 3);

						// Draw needle
						sb.line(centerX, centerY, needleTipX, needleTipY);                         // Center to N
						sb.line(needleBackTipX, needleBackTipY, needleRightTipX, needleRightTipY); // S to right
						sb.line(needleRightTipX, needleRightTipY, needleTipX, needleTipY);         // Right to N
						sb.line(needleTipX, needleTipY, needleLeftTipX, needleLeftTipY);           // N to left
						sb.line(needleLeftTipX, needleLeftTipY, needleBackTipX, needleBackTipY);   // left to S

						oled.setBuffer(sb.getScreenBuffer());
						oled.display();

						if (verbose) {
							System.out.println(String.format("Heading: %06.02f, Pitch: %05.02f, Roll: %05.02f",
									magData.get(HMC5883L.MagValues.HEADING),
									magData.get(HMC5883L.MagValues.PITCH),
									magData.get(HMC5883L.MagValues.ROLL)));
						}
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
					System.out.println("Magnetometer stopped.");
				}
				try {
					sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
					oled.setBuffer(sb.getScreenBuffer());
					oled.display();
					oled.clear();
					oled.shutdown();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				System.out.println("Oled screen down");
			}, "Shutdown Hook"));
			sensor.startReading();
//		} catch (Throwable throwable) {
//			throwable.printStackTrace();
//		}
	}
}
