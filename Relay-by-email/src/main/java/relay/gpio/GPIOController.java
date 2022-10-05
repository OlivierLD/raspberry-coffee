package relay.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class GPIOController {
    private GpioController gpio = null;
    private OneRelay relay = null;

    public GPIOController() {
        this.gpio = GpioFactory.getInstance();
        this.relay = new OneRelay(this.gpio, RaspiPin.GPIO_00, "Relay01");
    }

    public void shutdown() {
        this.gpio.shutdown();
    }

    public void switchRelay(boolean on) {
        if (on)
            relay.on();
        else
            relay.off();
    }
}
