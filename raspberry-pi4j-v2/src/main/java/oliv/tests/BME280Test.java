package oliv.tests;

// Copied from package one.microproject.rpi.hardware.gpio.sensors.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import one.microproject.rpi.hardware.gpio.sensors.BME280;
import one.microproject.rpi.hardware.gpio.sensors.BME280Builder;
import one.microproject.rpi.hardware.gpio.sensors.impl.BME280Impl;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.tinylog.Logger;

/*
 * This one uses TinyLog (https://tinylog.org/v2/)
 */
public class BME280Test {

//    private static final Logger LOG = LoggerFactory.getLogger(BME280Test.class);

    private BME280Test() {
    }

    public static void test(Context context) throws Exception {
//        LOG.info("BME280Test started ...");
        Logger.info("BME280Test started ...");
        try (BME280 bme280 = BME280Builder.get().context(context).build()) {
            int id = bme280.getId();
            Logger.info("BME280 CHIP ID={}", id);
            int status = bme280.getStatus();
            Logger.info("BME280 status={}", status);
            for (int i = 0; i < 10; i++) {
                BME280Impl.Data data = bme280.getSensorValues();
                float pres = data.getPressure() / 1000;
                Logger.info("[{}] Temperature: {} C, Pressure: {} kPa, Relative humidity: {} %", i,
                        String.format("%.3f", data.getTemperature()), String.format("%.3f", pres), String.format("%.3f", data.getRelativeHumidity()));
                Thread.sleep(500);
            }
        }
        Logger.info("BME280Test done.");
    }

    public static void main(String... args) {
        Logger.info("Off we go ...");
        // Context context = null;
        // context = lazyConfigInit(context);
        Context context = Pi4J.newAutoContext();
        try {
            BME280Test.test(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
