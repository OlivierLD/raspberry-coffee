package gps;

import sunflower.gps.GPSReader;

public class GPSReaderTest {
    /**
     * Standalone test. Not a UnitTest
     * @param args CLI args
     */
    public static void main(String... args) {
        final GPSReader gpsReader = new GPSReader(date -> {
            System.out.println(GPSReader.SDF_DATETIME.format(date));
        }, "RMC");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gpsReader.stopReading();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "Shutdown Hook"));

        String defaultValue = "/dev/tty.usbmodem141101";
        // "/dev/ttyUSB0"
        gpsReader.startReading(defaultValue, 4_800);

        System.out.println("Done.");
    }
}
