package main;

import sensors.sth10.STH10Driver;
import utils.WeatherUtil;

public class STH10 {
	public static void main(String... args) {

		STH10Driver probe = new STH10Driver();

		while (true) {
			double t = probe.readTemperature();
			double h = probe.readHumidity(t);

			System.out.println(String.format("Temp: %.02f C, Hum: %.02f%%, dew pt Temp: %.02f", t, h, WeatherUtil.dewPointTemperature(h, t)));

			try { Thread.sleep(1000); } catch (Exception ex) {}
		}
	}
}
