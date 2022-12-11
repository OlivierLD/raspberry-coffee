package gpio.sensors.i2c;

import com.pi4j.context.Context;
import gpio.sensors.i2c.impl.BME280Impl;

public class BME280Builder {

    private Context pi4j;
    private int address = BME280Impl.ADDRESS;
    private int i2cBus = 1;

    public BME280Builder context(Context pi4j) {
        this.pi4j = pi4j;
        return this;
    }

    public BME280Builder address(int address) {
        this.address = address;
        return this;
    }

    public BME280Builder i2cBus(int i2cBus) {
        this.i2cBus = i2cBus;
        return this;
    }

    public BME280 build() {
        return new BME280Impl(pi4j, address, i2cBus);
    }

    public static BME280Builder get() {
        return new BME280Builder();
    }

}
