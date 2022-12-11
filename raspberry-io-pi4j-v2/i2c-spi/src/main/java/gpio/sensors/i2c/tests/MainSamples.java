package gpio.sensors.i2c.tests;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

// TODO Forget it...

public class MainSamples {

    private static final Logger LOG = LoggerFactory.getLogger(MainSamples.class);

    private static void dumpContext(Context context) {
        context.providers().getAll().forEach((k, v) -> System.out.printf("Providers: Key: %s, Value: %s\n", k, v.getType()));
        System.out.printf("Description: %s\n", context.describe().description());
    }

    public static void main(String... args) throws Exception {
        Set<String> arguments = toSet(args);
        Context context = null;

        if (arguments.contains("ALL") || arguments.contains("HTU21D")) {
            context = lazyConfigInit(context);
            dumpContext(context);
            HTU21DTest.test(context);
        }

        if (arguments.contains("ALL") || arguments.contains("BMP180")) {
            context = lazyConfigInit(context);
            dumpContext(context);
            BMP180Test.test(context);
        }

        // . . .

        if (context == null) {
            LOG.info("No tests triggered !");
            LOG.info("Use arguments: ALL ADS1115 BME280 BMP180 HTU21D");
        }
    }

    private static Context lazyConfigInit(Context context) {
        if (context == null) {
            context = Pi4J.newAutoContext();
        }
        return context;
    }

    private static Set<String> toSet(String... args) {
        return /*Set<String> result =*/ Arrays.stream(args).collect(Collectors.toSet());
//        return result;
    }
}
