package main;

import sensors.sth10.STH10Driver;
import utils.WeatherUtil;

public class STH10 {

	private static boolean go = true;

	public static void main(String... args) {

		STH10Driver probe = new STH10Driver();


		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			System.out.println("Exiting");
			try { Thread.sleep(1_500L); } catch (InterruptedException ie) {}
		}));

		while (go) {
			double t = probe.readTemperature();
			double h = probe.readHumidity(t);

			System.out.println(String.format("Temp: %.02f C, Hum: %.02f%%, dew pt Temp: %.02f", t, h, WeatherUtil.dewPointTemperature(h, t)));

			try { Thread.sleep(1_000L); } catch (Exception ex) {}
		}

		probe.shutdownGPIO();
		System.out.println("Bye!");
	}
}
