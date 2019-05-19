package pi4j.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GPIOController {
	private GpioController gpio = null;
	private OneLed yellowLed = null;
	private OneLed greenLed = null;
	private GpioPinDigitalInput button = null;
	private RaspberryPIEventListener caller = null;

	public GPIOController(RaspberryPIEventListener listener) {
		this.caller = listener;
		this.gpio = GpioFactory.getInstance();
		this.yellowLed = new OneLed(this.gpio, RaspiPin.GPIO_01, "yellow");
		this.greenLed = new OneLed(this.gpio, RaspiPin.GPIO_04, "green");
		this.button = this.gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
		this.button.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				caller.manageEvent(event);
			}
		});
	}

	public void shutdown() {
		this.gpio.shutdown();
	}

	public void switchYellow(boolean on) {
		if (on)
			yellowLed.on();
		else
			yellowLed.off();
	}

	public void switchGreen(boolean on) {
		if (on)
			greenLed.on();
		else
			greenLed.off();
	}
}
