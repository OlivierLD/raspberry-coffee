package paddle.buttons;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import pushbutton.PushButtonObserver;

public class PushButtonMaster {
	private GpioPinDigitalInput button = null;

	private PushButtonObserver pbo = null;

	public PushButtonMaster(PushButtonObserver obs) {
		if (obs == null) {
			throw new IllegalArgumentException("Observer cannot be null");
		}
		this.pbo = obs;
	}

	public void initCtx(GpioController gpio) {
		initCtx(gpio, RaspiPin.GPIO_02);
	}

	public void initCtx(GpioController gpio, Pin buttonPin) {
		// provision gpio pin #01 as an output pin and turn it off
		button = gpio.provisionDigitalInputPin(buttonPin, PinPullResistance.PULL_DOWN);
		button.addListener((GpioPinListenerDigital) event -> {
			if (event.getState().isHigh()) {
				pbo.onButtonPressed();
			} else if (event.getState().isLow()) {
				pbo.onButtonReleased();
			}
		});
	}
}
