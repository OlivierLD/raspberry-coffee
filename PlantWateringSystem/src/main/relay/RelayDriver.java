package relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RelayDriver {

	final GpioController gpio = GpioFactory.getInstance();

	private static final Pin DEFAULT_SIGNAL_PIN =  RaspiPin.GPIO_00; // BCM 17

	private Pin signalPin;
	private GpioPinDigitalOutput signal = null;

	public RelayDriver() {
		this(DEFAULT_SIGNAL_PIN);
	}

	public RelayDriver(Pin _signalPin) {
		this.signalPin = _signalPin;
		this.signal = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Relay", PinState.LOW);
	}

	public void up() {
		this.signal.high();
	}

	public void down() {
		this.signal.low();
	}

	public PinState getState() {
		return this.signal.getState();
	}

	public void shutdownGPIO() {
		if (!gpio.isShutdown()) {
			gpio.shutdown();
		}
	}
}
