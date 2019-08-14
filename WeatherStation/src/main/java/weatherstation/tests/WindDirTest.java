
package weatherstation.tests;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import weatherstation.SDLWeather80422;
import weatherstation.SDLWeather80422.AdcMode;
import weatherstation.SDLWeather80422.SdlMode;

import java.text.DecimalFormat;
import java.text.Format;

public class WindDirTest {
	private final static Format VOLTS_FMT = new DecimalFormat("##0.000");
	private final static Format DIR_FMT = new DecimalFormat("##0.0");

	// Sample main, for tests
	private static boolean go = true;

	public static void main(String... args) {
		final Thread coreThread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nUser interrupted.");
			go = false;
			synchronized (coreThread) {
				coreThread.notify();
			}
		}, "Shutdown Hook"));

		final Pin ANEMOMETER_PIN = RaspiPin.GPIO_16; // <- WiringPi number. aka GPIO 15, #10
		final Pin RAIN_PIN = RaspiPin.GPIO_01; // <- WiringPi number. aka GPIO 18, #12
		SDLWeather80422 weatherStation = new SDLWeather80422(ANEMOMETER_PIN, RAIN_PIN, AdcMode.SDL_MODE_I2C_ADS1015);
		weatherStation.setWindMode(SdlMode.SAMPLE, 5);

		while (go) {
			double volts = weatherStation.getCurrentWindDirectionVoltage();
			float wd = weatherStation.getCurrentWindDirection();
			System.out.println("Wind Only: Dir=" + DIR_FMT.format(wd) + "\272, (" + VOLTS_FMT.format(volts) + " V)");
			try {
				synchronized (coreThread) {
					coreThread.wait(1_000L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		weatherStation.shutdown();
		System.out.println("Done.");
	}
}
