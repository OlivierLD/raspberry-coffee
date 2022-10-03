package relay.gpio;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class OneRelay {
    private GpioPinDigitalOutput led = null;
    private String name;

    public OneRelay(GpioController gpio, Pin pin, String name) {
        this.name = name;
        led = gpio.provisionDigitalOutputPin(pin, name, PinState.HIGH); // Begin HIGH for a relay
    }

    public void on() {
        if ("true".equals(System.getProperty("verbose", "false")))
            System.out.println(this.name + " is on.");
        led.low();
    }

    public void off() {
        if ("true".equals(System.getProperty("verbose", "false")))
            System.out.println(this.name + " is off.");
        led.high();
    }
}
