package gpio.sensors.i2c.tests;

import com.pi4j.context.Context;
import gpio.sensors.i2c.HTU21DF;
import gpio.sensors.i2c.HTU21DFBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTU21DTest {

	private static final Logger LOG = LoggerFactory.getLogger(HTU21DTest.class);

	private HTU21DTest() {
	}

	public static void test(Context context) throws Exception {
		try (HTU21DF htu21df = HTU21DFBuilder.get().context(context).build()) {
			LOG.info("HTU21DTest started ...");
			for (int i = 0; i < 10; i++) {
				float temp = htu21df.getTemperature();
				float hum = htu21df.getHumidity();
				LOG.info("[{}] Temperature: {} C, Rel. Humidity: {} %", i, String.format("%.3f", temp), String.format("%.3f", hum));
				Thread.sleep(500);
			}
			LOG.info("HTU21DTest done.");
		}
	}
		
}
