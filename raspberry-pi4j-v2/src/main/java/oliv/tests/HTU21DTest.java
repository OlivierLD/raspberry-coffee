package oliv.tests;

// Copied from package one.microproject.rpi.hardware.gpio.sensors.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import one.microproject.rpi.hardware.gpio.sensors.HTU21DF;
import one.microproject.rpi.hardware.gpio.sensors.HTU21DFBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.tinylog.Logger;

/*
 * This one uses TinyLog (https://tinylog.org/v2/)
 */

public class HTU21DTest {

    // private static final Logger LOG = LoggerFactory.getLogger(HTU21DTest.class);

    private HTU21DTest() {
    }

    public static void test(Context context) throws Exception {
        try (HTU21DF htu21df = HTU21DFBuilder.get().context(context).build()) {
            Logger.info("HTU21DTest started ...");
            for (int i = 0; i < 10; i++) {
                float temp = htu21df.getTemperature();
                float hum = htu21df.getHumidity();
                Logger.info("[{}] Temperature: {} C, Rel. Humidity: {} %", i, String.format("%.3f", temp), String.format("%.3f", hum));
                Thread.sleep(500);
            }
            Logger.info("HTU21DTest done.");
        }
    }

    public static void main(String... args) {
        Logger.info("Off we go ...");
        // Context context = null;
        // context = lazyConfigInit(context);
        Context context = Pi4J.newAutoContext();
        System.out.printf("%d provider(s)\n:", context.providers().getAll().size());
        context.providers().getAll().forEach((k, v) -> System.out.printf("Providers: Key: %s, Value: %s (%s)\n", k, v.getType(), v.description()));
        System.out.printf("Description: %s\n", context.describe().description());
        try {
            HTU21DTest.test(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}