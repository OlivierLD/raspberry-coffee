package gpio.sensors.i2c;

import gpio.sensors.i2c.impl.ADS1115Impl;

/**
 * ADS1115 device, 4 Channel 16-bit analog to digital converter.
 * https://www.adafruit.com/product/1085
 */
public interface ADS1115 extends I2CDevice {

    /**
     * Get Gain settings.
     * @return - Gain settings.
     */
    ADS1115Impl.GAIN getGain();

    /**
     * Get AIN0 Value [Volts].
     * @return AIN0 Value [Volts].
     */
    double getAIn0();

    /**
     * Get AIN1 Value [Volts].
     * @return AIN1 Value [Volts].
     */
    double getAIn1();

    /**
     * Get AIN2 Value [Volts].
     * @return AIN2 Value [Volts].
     */
    double getAIn2();

    /**
     * Get AIN3 Value [Volts].
     * @return AIN3 Value [Volts].
     */
    double getAIn3();

}
