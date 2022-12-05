package oliv.tests;

// Copied from package one.microproject.rpi.hardware.gpio.sensors.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import one.microproject.rpi.hardware.gpio.sensors.BMP180;
import one.microproject.rpi.hardware.gpio.sensors.BMP180Builder;
import one.microproject.rpi.hardware.gpio.sensors.impl.BMP180Impl;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.tinylog.Logger;

public class BMP180Test {

//    private static final Logger LOG = LoggerFactory.getLogger(BMP180Test.class);

    private BMP180Test() {
    }

    public static void test(Context context) throws Exception {
        try (BMP180 bmp180 = BMP180Builder.get().context(context).build()) {
            Logger.info("BMP180Test started ...");
            int id = bmp180.getId();
            Logger.info("BME180 CHIP ID={}", id);
            for (int i = 0; i < 10; i++) {
                BMP180Impl.Data data = bmp180.getSensorValues();
                float pressure = data.getPressure() / 1000;
                Logger.info("[{}] Temperature: {} C, Pressure: {} kPa", i, String.format("%.3f", data.getTemperature()), String.format("%.3f", pressure));
                Thread.sleep(500);
            }
            Logger.info("BMP180Test done.");
        }
    }

    public static void main(String... args) {
        Logger.info("Off we go ...");
        // Context context = null;
        // context = lazyConfigInit(context);
        Context context = Pi4J.newAutoContext();
        try {
            BMP180Test.test(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}