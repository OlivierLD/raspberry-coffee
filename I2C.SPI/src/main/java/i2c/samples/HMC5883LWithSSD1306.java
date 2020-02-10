package i2c.samples;

import i2c.sensor.HMC5883L;

import java.util.Map;

public class HMC5883LWithSSD1306 {

	public static void main(String... args) {
		try {
			HMC5883L sensor = new HMC5883L();

			// Listener
			sensor.subscribe(new HMC5883L.HMC5883LEventListener() {
				@Override
				public void onNewData(Map<HMC5883L.MagValues, Double> magData) {
					// TODO Write on oled screen
					System.out.println(String.format("Heading: %.02f, Pitch: %.02f, Roll: %.02f",
							magData.get(HMC5883L.MagValues.HEADING),
							magData.get(HMC5883L.MagValues.PITCH),
							magData.get(HMC5883L.MagValues.ROLL)));
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
			}, "Shutdown Hook"));
			sensor.startReading();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
}
