package gpio.sensors.i2c.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import gpio.sensors.i2c.BME280;
import gpio.sensors.i2c.BME280Builder;
import gpio.sensors.i2c.impl.BME280Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BME280Test {

    private static final Logger LOG = LoggerFactory.getLogger(BME280Test.class);

    private BME280Test() {
    }

    public static void test(Context context) throws Exception {
        LOG.info("BME280Test started ...");
        try (BME280 bme280 = BME280Builder.get().context(context).build()) {
            int id = bme280.getId();
            LOG.info("BME280 CHIP ID={}", id);
            int status = bme280.getStatus();
            LOG.info("BME280 status={}", status);
            for (int i = 0; i < 10; i++) {
                BME280Impl.Data data = bme280.getSensorValues();
                float pres = data.getPressure() / 1000;
                LOG.info("[{}] Temperature: {} C, Pressure: {} kPa, Relative humidity: {} %", i,
                        String.format("%.3f", data.getTemperature()), String.format("%.3f", pres), String.format("%.3f", data.getRelativeHumidity()));
                Thread.sleep(500);
            }
        }
        LOG.info("BME280Test done.");
    }

    public static void main(String... args) {
        org.tinylog.Logger.info("Off we go ...");
        Context context = Pi4J.newAutoContext();

        context.providers().getAll().forEach((k, v) -> System.out.printf("Providers: Key: %s, Value: %s\n", k, v.getType()));
        System.out.printf("Description: %s\n", context.describe().description());

        try {
            BME280Test.test(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
