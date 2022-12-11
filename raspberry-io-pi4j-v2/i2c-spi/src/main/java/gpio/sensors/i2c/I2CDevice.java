package gpio.sensors.i2c;

import com.pi4j.context.Context;

/**
 * Generic I2C device.
 */
public interface I2CDevice extends AutoCloseable {

    /**
     * Get I2C address of this device.
     * @return - I2C address.
     */
    int getAddress();

    /**
     * Get pi4j context.
     * @return - pi4j context.
     */
    Context getContext();

    /**
     * Get I2C bus ID.
     * @return - I2C bus ID.
     */
    int getI2CBus();

    /**
     * Get device ID.
     * @return - device ID.
     */
    String getDeviceId();

}
