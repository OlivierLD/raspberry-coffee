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
	private final static long WATERING_DURATION = 10L; // 10 seconds
	private final static long RESUME_SENSOR_WATCH_AFTER = 120L; // 2 minutes

	private final static String HUMIDITY_PREFIX = "--water-below:";             // %
	private final static String WATERING_DURATION_PREFIX = "--water-during:";   // seconds
	private final static String RESUME_AFTER_PREFIX = "--resume-after:";        // seconds

	// TODO Override default pins for sensor and relay

	public static void main(String... args) {

		int humidityThreshold = HUMIDITY_THRESHOLD;
		long wateringDuration = WATERING_DURATION;
		long resumeSensorWatchAfter = RESUME_SENSOR_WATCH_AFTER;
		// Override values with system variables
		for (String arg : args) {
			if (arg.startsWith(HUMIDITY_PREFIX)) {
				String val = arg.substring(HUMIDITY_PREFIX.length());
				try {
					humidityThreshold = Integer.parseInt(val);
					if (humidityThreshold < 0 || humidityThreshold > 100) {
						humidityThreshold = HUMIDITY_THRESHOLD;
						System.err.println(String.format(">> Humidity Threshold must be in [0..100]. Reseting to %d ", HUMIDITY_THRESHOLD));
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(WATERING_DURATION_PREFIX)) {
				String val = arg.substring(WATERING_DURATION_PREFIX.length());
				try {
					wateringDuration = Long.parseLong(val);
					if (wateringDuration < 0) {
						wateringDuration = WATERING_DURATION;
						System.err.println(">> Watering duration must be positive");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(RESUME_AFTER_PREFIX)) {
				String val = arg.substring(RESUME_AFTER_PREFIX.length());
				try {
					resumeSensorWatchAfter = Long.parseLong(val);
					if (resumeSensorWatchAfter < 0) {
						resumeSensorWatchAfter = RESUME_SENSOR_WATCH_AFTER;
						System.err.println(">> Resume Watch After must be positive");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		// Print summary
		System.out.println("----- P L A N T   W A T E R I N G   S Y S T E M --------");
		System.out.println(String.format("Start watering under %d%% of humidity.", humidityThreshold));
		System.out.println(String.format("Water during %d seconds.", wateringDuration));
		System.out.println(String.format("Resume sensor watch %d seconds after watering.", resumeSensorWatchAfter));
		System.out.println("--------------------------------------------------------");

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
			if (h < humidityThreshold) { // Ah! Need some water
				// Open the valve
				relay.up();
				// Watering time
				try {
					Thread.sleep(wateringDuration * 1_000L);
				} catch (Exception ex) {
				}
				// Shut the valve
				relay.down();
				// Wait before resuming sensor watching
				try {
					Thread.sleep(resumeSensorWatchAfter * 1_000L);
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
