package lis3mdl.http;

import http.client.HTTPClient;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Read raw mag data from an HTTP Server, using REST.
 * That one happens to be in Python, polling an LIS3MDL breakout board
 *
 * Notice that there is here NO code related to the device to be read.
 *
 * TODO Introduce calibration
 *
 * Warning: uses some Java 10 syntax (var)
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
     * -Drest.url defaulted to "http://192.168.42.9:8080/lis3mdl/cache"
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

        while (keepLooping.get()) {
            try {
                String str = HTTPClient.doGet( System.getProperty("rest.url", "http://192.168.42.9:8080/lis3mdl/cache"), null);
                if ("true".equals(System.getProperty("verbose"))) {
                    System.out.println(str);
                }
                JSONObject magData = new JSONObject(str);
                double magX = magData.getDouble("x");
                double magY = magData.getDouble("y");
                double magZ = magData.getDouble("z");
//                var data = MagnetometerReader.calculate(magX, magY, magZ); // Java 10+
                MagData data = MagnetometerReader.calculate(magX, magY, magZ); // Java 10-
                System.out.println(String.format("Heading: %f Pitch: %f, Roll: %f", data.heading, data.pitch, data.roll));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
