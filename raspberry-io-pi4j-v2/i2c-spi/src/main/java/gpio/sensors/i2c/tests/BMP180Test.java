package gpio.sensors.i2c.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import gpio.sensors.i2c.BMP180;
import gpio.sensors.i2c.BMP180Builder;
import gpio.sensors.i2c.impl.BMP180Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BMP180Test {

	private static final Logger LOG = LoggerFactory.getLogger(BMP180Test.class);

	private BMP180Test() {
	}

	public static void test(Context context) throws Exception {
		try (BMP180 bmp180 = BMP180Builder.get().context(context).build()) {
			LOG.info("BMP180Test started ...");
			int id = bmp180.getId();
			LOG.info("BME180 CHIP ID={}", id);
			for (int i = 0; i < 10; i++) {
				BMP180Impl.Data data = bmp180.getSensorValues();
				float pressure = data.getPressure() / 1_000;
				LOG.info("[{}] Temperature: {} C, Pressure: {} kPa", i, String.format("%.3f", data.getTemperature()), String.format("%.3f", pressure));
				Thread.sleep(500);
			}
			LOG.info("BMP180Test done.");
		}
	}

	public static void main(String... args) {
		org.tinylog.Logger.info("Off we go ...");
		Context context = Pi4J.newAutoContext();

		context.providers().getAll().forEach((k, v) -> System.out.printf("Providers: Key: %s, Value: %s\n", k, v.getType()));
		System.out.printf("Description: %s\n", context.describe().description());

		try {
			BMP180Test.test(context);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
