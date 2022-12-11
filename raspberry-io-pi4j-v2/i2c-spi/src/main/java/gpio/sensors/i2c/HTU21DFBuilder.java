package gpio.sensors.i2c;

import com.pi4j.context.Context;
import gpio.sensors.i2c.impl.HTU21DFImpl;

public class HTU21DFBuilder {

    private Context pi4j;
    private int address = HTU21DFImpl.HTU21DF_ADDRESS;
    private int i2cBus = 1;

    public HTU21DFBuilder context(Context pi4j) {
        this.pi4j = pi4j;
        return this;
    }

    public HTU21DFBuilder address(int address) {
        this.address = address;
        return this;
    }

    public HTU21DFBuilder i2cBus(int i2cBus) {
        this.i2cBus = i2cBus;
        return this;
    }

    public HTU21DF build() {
        return new HTU21DFImpl(pi4j, address, i2cBus);
    }

    public static HTU21DFBuilder get() {
        return new HTU21DFBuilder();
    }

}
