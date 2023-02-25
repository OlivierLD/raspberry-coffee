package nmea.forwarders.pushbutton;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Implements the nuts and bolts of the push button interaction.
 * No need to worry about that in the main class.
 * From the main:
 * Invoke the initCtx method
 * Invoke the freeResources method
 *
 * TODO Upgrade to ./Project.Trunk/Button-Reflex/src/main/java/breadboard/button/v2/PushButtonMaster.java
 */
public class PushButtonMaster {
	private final GpioController gpio = GpioFactory.getInstance();
	private GpioPinDigitalInput button = null;

	private PushButtonObserver pbo = null;

	public PushButtonMaster(PushButtonObserver obs) {
		if (obs == null) {
			throw new IllegalArgumentException("Observer cannot be null");
		}
		this.pbo = obs;
	}

	public void initCtx() {
		initCtx(RaspiPin.GPIO_01);
	}

	public void initCtx(Pin buttonPin) {
		// provision gpio pin #01 as an output pin and turn it off
		button = gpio.provisionDigitalInputPin(buttonPin, PinPullResistance.PULL_DOWN);
		button.addListener((GpioPinListenerDigital) event -> {
			if (event.getState().isHigh())
				pbo.onButtonPressed();
		});
	}

	public void freeResources() {
		gpio.shutdown();
//	System.exit(0);
	}
}
