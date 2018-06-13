package main;

import com.pi4j.io.gpio.PinState;
import relay.RelayDriver;
import sensors.sth10.STH10Driver;
import utils.WeatherUtil;

/**
 * Example / Prototype...
 */
public class STH10 {

	private static boolean go = true;

	public static void main(String... args) {

		STH10Driver probe = new STH10Driver();
		RelayDriver relay = new RelayDriver();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			System.out.println("Exiting");
			try { Thread.sleep(1_500L); } catch (InterruptedException ie) {}
		}));

		while (go) {
			double t = probe.readTemperature();
			double h = probe.readHumidity(t);

			System.out.println(String.format("Temp: %.02f C, Hum: %.02f%%, dew pt Temp: %.02f", t, h, WeatherUtil.dewPointTemperature(h, t)));

			/*
			 * TODO Here, test the sensor's values, and make the decision about the valve.
			 */

			try { Thread.sleep(1_000L); } catch (Exception ex) {}
		}

		if (relay.getState() == PinState.HIGH) {
			relay.down();
		}

		probe.shutdownGPIO();
		relay.shutdownGPIO();

		System.out.println("Bye!");
	}
}
