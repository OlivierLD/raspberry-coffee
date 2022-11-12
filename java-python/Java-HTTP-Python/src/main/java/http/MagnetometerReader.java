package http;

import com.fasterxml.jackson.databind.ObjectMapper;
import http.client.HTTPClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Read raw mag data from an HTTP Server, using REST.
 * That one happens to be in Python, polling an LIS3MDL breakout board
 *
 * Notice that there is here NO code related to the device to be read.
 *
 * TODO Introduce calibration
 *
 * Warning: may use some Java 10 syntax (var)
 */
public class MagnetometerReader {

    private static class MagData {
        double heading;
        double pitch;
        double roll;
    }

    public static MagData calculate(double magX, double magY, double magZ) {
//        var magData = new MagData(); // Java 10+
        MagData magData = new MagData(); // Java 10-
        magData.heading = Math.toDegrees(Math.atan2(magY, magX));
        while (magData.heading < 0) {
            magData.heading += 360f;
        }
        magData.pitch = Math.toDegrees(Math.atan2(magY, magZ));
        magData.roll = Math.toDegrees(Math.atan2(magX, magZ));
        return magData;
    }

    /**
     * System variables:
     * -Drest.url defaulted to "http://192.168.1.106:8080/lis3mdl/cache"
     * -Dverbose default false
     *
     * @param args Unused
     */
    public static void main(String... args) {

        AtomicBoolean keepLooping = new AtomicBoolean(true);
        System.out.println("Ctrl+C to stop");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping");
            keepLooping.set(false);
        }));

        ObjectMapper mapper = new ObjectMapper();

        while (keepLooping.get()) {
            try {
                String str = HTTPClient.doGet( System.getProperty("rest.url", "http://192.168.1.106:8080/lis3mdl/cache"), null);
                if ("true".equals(System.getProperty("verbose"))) {
                    System.out.println(str);
                }
                final Map<String, Double> magData = mapper.readValue(str, Map.class);
                double magX = magData.get("x");
                double magY = magData.get("y");
                double magZ = magData.get("z");
//                var data = MagnetometerReader.calculate(magX, magY, magZ); // Java 10+
                MagData data = MagnetometerReader.calculate(magX, magY, magZ); // Java 10-
                System.out.println(String.format("Heading: %f Pitch: %f, Roll: %f", data.heading, data.pitch, data.roll));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
