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

	private final static int HUMIDITY_THRESHOLD = 35; // 35 %
	private final static long WATERING_DURATION = 10_000L; // 10 seconds
	private final static long RESUME_SENSOR_WATCH_AFTER = 120_000L; // 2 minutes

	public static void main(String... args) {

		// TODO Override values with system variables

		STH10Driver probe = new STH10Driver();
		RelayDriver relay = new RelayDriver();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			if (relay.getState() == PinState.HIGH) {
				relay.down();
			}
			System.out.println("Exiting");
			try { Thread.sleep(1_500L); } catch (InterruptedException ie) {}
		}));

		while (go) {
			double t = probe.readTemperature();
			double h = probe.readHumidity(t);

			// TODO A screen (Like the SSD1306) ?
			System.out.println(String.format("Temp: %.02f C, Hum: %.02f%%, dew pt Temp: %.02f", t, h, WeatherUtil.dewPointTemperature(h, t)));

			/*
			 * Here, test the sensor's values, and make the decision about the valve.
			 */
			if (h < HUMIDITY_THRESHOLD) { // Ah! Need some water
				// Open the valve
				relay.up();
				// Watering time
				try {
					Thread.sleep(WATERING_DURATION);
				} catch (Exception ex) {
				}
				// Shut the valve
				relay.down();
				// Wait before resuming sensor watching
				try {
					Thread.sleep(RESUME_SENSOR_WATCH_AFTER);
				} catch (Exception ex) {
				}
			} else {
				try {
					Thread.sleep(1_000L);
				} catch (Exception ex) {
				}
			}
		}

		probe.shutdownGPIO();
		relay.shutdownGPIO();

		System.out.println("Bye!");
	}
}
