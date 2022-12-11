package gpio.sensors.i2c;

/**
 * HTU21DF Temperature Humidity Sensor.
 * https://www.adafruit.com/product/3515
 */
public interface HTU21DF extends I2CDevice {

    /**
     * Get ambient temperature reading [Celsius].
     * @return - temperature reading [Celsius].
     */
    float getTemperature();

    /**
     * Get ambient relative humidity reading [%].
     * @return - relative humidity reading [%].
     */
    float getHumidity();

}
