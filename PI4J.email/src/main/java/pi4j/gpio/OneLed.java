package pi4j.gpio;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class OneLed {
	private GpioPinDigitalOutput led = null;
	private String name;

	public OneLed(GpioController gpio, Pin pin, String name) {
		this.name = name;
		led = gpio.provisionDigitalOutputPin(pin, "Led", PinState.LOW);
	}

	public void on() {
		if ("true".equals(System.getProperty("verbose", "false")))
			System.out.println(this.name + " is on.");
		led.high();
	}

	public void off() {
		if ("true".equals(System.getProperty("verbose", "false")))
			System.out.println(this.name + " is off.");
		led.low();
	}
}
