package gpio.sensors.i2c.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import gpio.sensors.i2c.ADS1115;
import gpio.sensors.i2c.ADS1115Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADS1115Test {

    private static final Logger LOG = LoggerFactory.getLogger(ADS1115Test.class);

    private ADS1115Test() {
    }

    public static void test(Context context) throws Exception {
        LOG.info("ADS1115Test started ...");
        try (ADS1115 ads1115 = ADS1115Builder.get().context(context).build()) {
            for (int i = 0; i < 10; i++) {
                double aIn0 = ads1115.getAIn0();
                double aIn1 = ads1115.getAIn1();
                double aIn2 = ads1115.getAIn2();
                double aIn3 = ads1115.getAIn3();
                LOG.info("[{}] Voltages: a0={} V, a1={} V, a2={} V, a3={} V",
                        i, String.format("%.3f", aIn0), String.format("%.3f", aIn1), String.format("%.3f", aIn2), String.format("%.3f", aIn3));
                Thread.sleep(500);
            }
        }
        LOG.info("ADS1115Test done.");
    }

    public static void main(String... args) {
        org.tinylog.Logger.info("Off we go ...");
        Context context = Pi4J.newAutoContext();

        context.providers().getAll().forEach((k, v) -> System.out.printf("Providers: Key: %s, Value: %s\n", k, v.getType()));
        System.out.printf("Description: %s\n", context.describe().description());

        try {
            ADS1115Test.test(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
