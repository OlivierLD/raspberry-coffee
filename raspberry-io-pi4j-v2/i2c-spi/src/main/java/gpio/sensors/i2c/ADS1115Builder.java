package gpio.sensors.i2c;

import com.pi4j.context.Context;
import gpio.sensors.i2c.impl.ADS1115Impl;

public class ADS1115Builder {

    private Context pi4j;
    private int address = ADS1115Impl.ADDRESS;
    private ADS1115Impl.GAIN gain = ADS1115Impl.GAIN.GAIN_4_096V;
    private int i2cBus = 1;

    public ADS1115Builder context(Context pi4j) {
        this.pi4j = pi4j;
        return this;
    }

    public ADS1115Builder address(int address) {
        this.address = address;
        return this;
    }

    public ADS1115Builder gain(ADS1115Impl.GAIN gain) {
        this.gain = gain;
        return this;
    }

    public ADS1115Builder i2cBus(int i2cBus) {
        this.i2cBus = i2cBus;
        return this;
    }

    public ADS1115 build() {
        return new ADS1115Impl(pi4j, address, gain, i2cBus);
    }

    public static ADS1115Builder get() {
        return new ADS1115Builder();
    }

}
