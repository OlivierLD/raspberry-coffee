package diozerotests;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.BMP180;
import com.diozero.util.SleepUtil;
import org.tinylog.Logger;

public class I2C_BMP180 {

    private static final int ITERATIONS = 20;

    public static void main(String... args) {
        int controller = 1;
        if (args.length > 0) {
            controller = Integer.parseInt(args[0]);
        }

        try (BMP180 bmp180 = new BMP180(controller, BMP180.BMPMode.STANDARD)) {
            bmp180.readCalibrationData();
            Logger.debug("Opened device");

            for (int i = 0; i < ITERATIONS; i++) {
                Logger.info("Temperature: {0.##} C. Pressure: {0.##} hPa", Float.valueOf(bmp180.getTemperature()),
                        Float.valueOf(bmp180.getPressure()));
                SleepUtil.sleepSeconds(0.5);
            }
        } catch (RuntimeIOException ioe) {
            Logger.error(ioe, "Error: {}", ioe);
        }
    }
}
