package spi.sensor.main;

import spi.sensor.BMP183;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SampleBMP183Main {
	private final static NumberFormat T_FMT = new DecimalFormat("##0.0");
	private final static NumberFormat P_FMT = new DecimalFormat("###0.00");

	public static void main(String... args) throws Exception {
		BMP183 bmp183 = new BMP183();
		for (int i = 0; i < 10; i++) {
			double temp = bmp183.measureTemperature();
			double press = bmp183.measurePressure();
			System.out.println("Temperature: " + T_FMT.format(temp) + "\272C");
			System.out.println("Pressure   : " + P_FMT.format(press / 100.0) + " hPa");
			try {
				Thread.sleep(1_000);
			} catch (Exception ex) {
			}
		}
		BMP183.shutdownBMP183();
		System.out.println("Bye");
	}
}
