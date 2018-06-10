package sensors.sth10;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

public class STH10Driver {

	final GpioController gpio = GpioFactory.getInstance();
	// TODO Use PinUtils
	private static final Pin DEFAULT_DATA_PIN = RaspiPin.GPIO_01;  // TODO Tweak that
	private static final Pin DEFAULT_CLOCK_PIN = RaspiPin.GPIO_02; // TODO Tweak that

	private Pin dataPin;
	private Pin clockPin;
	private GpioPinDigitalInput pirInput;

	private GpioPin data;
	private GpioPin clock;

	public STH10Driver() {
		this.dataPin = DEFAULT_DATA_PIN;
		this.clockPin = DEFAULT_CLOCK_PIN;

		this.data = gpio.getProvisionedPin(this.dataPin);
		this.clock = gpio.getProvisionedPin(this.clockPin);
	}

	private void sendCommandSHT(int command) {

		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		gpio.high((GpioPinDigitalOutput)this.clock);

		gpio.low((GpioPinDigitalOutput)this.data);
		gpio.low((GpioPinDigitalOutput)this.clock);

		gpio.high((GpioPinDigitalOutput)this.clock);
		gpio.high((GpioPinDigitalOutput)this.data);

		gpio.low((GpioPinDigitalOutput)this.clock);

		// TODO Shift out here

		// Verify we get the correct ack
		gpio.high((GpioPinDigitalOutput)this.clock);
		gpio.setMode(PinMode.DIGITAL_INPUT, this.data);
		while (!((GpioPinDigitalInput)this.data).isLow()) {
			// just wait
		}
		gpio.low((GpioPinDigitalOutput)this.clock);
		while (!((GpioPinDigitalInput)this.data).isHigh()) {
			// just wait
		}
		// Done...
	}

}
