package weatherstation.tests;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import weatherstation.SDLWeather80422;
import weatherstation.SDLWeather80422.AdcMode;
import weatherstation.SDLWeather80422.SdlMode;

import java.text.DecimalFormat;
import java.text.Format;

public class BasicWindTest {
	private final static Format SPEED_FMT = new DecimalFormat("##0.00");
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

		float totalRain = 0f;

		while (go) {
			double ws = weatherStation.getCurrentWindSpeed();
			double wg = weatherStation.getWindGust();
			float wd = weatherStation.getCurrentWindDirection();
			double volts = weatherStation.getCurrentWindDirectionVoltage();
			totalRain += weatherStation.getCurrentRainTotal(); // in mm

			System.out.println("Wind : Dir=" + DIR_FMT.format(wd) + "\272, (" + VOLTS_FMT.format(volts) + " V) Speed:" +
					SPEED_FMT.format(SDLWeather80422.toKnots(ws)) + " kts, Gust:" +
					SPEED_FMT.format(SDLWeather80422.toKnots(wg)) + " kts");
			System.out.println("Rain Total:" + totalRain + " mm, " + SPEED_FMT.format(SDLWeather80422.toInches(totalRain)) + " in.");
			if (weatherStation.isBMP180Available()) {
				try {
					float temp = weatherStation.readTemperature();
					float press = weatherStation.readPressure();
					System.out.println("    Temperature:" + DIR_FMT.format(temp) + "\272C, Pressure:" + SPEED_FMT.format(press / 100) + " hPa");
				} catch (Exception ex) {
					System.err.println("Can't read BMP180:");
					ex.printStackTrace();
				}
			}
			if (weatherStation.isHTU21DFAvailable()) {
				try {
					float hum = weatherStation.readHumidity();
					System.out.println("    Humidity:" + DIR_FMT.format(hum) + " %");
				} catch (Exception e) {
					System.err.println("Can't read HTU21DF:");
					e.printStackTrace();
				}
			}

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
