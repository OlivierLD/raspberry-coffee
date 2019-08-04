package relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * 5v are required to drive the relay
 * The GPIO pins deliver 3.3v
 * To drive a relay, a relay board is required.
 * (it may contain what's needed to drive the relay with 3.3v)
 */
public class OneRelay {
	public static void main(String... args) throws InterruptedException {

		System.out.println("GPIO Control - pin 02/#27 ... started.");
		System.out.println("(Labelled #17 on the cobbler.)");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// For a relay it seems that HIGH means NC (Normally Closed)...
		final GpioPinDigitalOutput pin17 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Relay1", PinState.HIGH);
		System.out.println(String.format("--> GPIO state should be: HIGH, and it is %s", pin17.getState()));

		Thread.sleep(1_000L);

		pin17.low();
		System.out.println("--> pin 17 should be: LOW, and it is " + (pin17.isHigh() ? "HIGH" : "LOW"));

		Thread.sleep(1_000L);

		pin17.high();
		System.out.println("--> pin 17 should be: HIGH");

		Thread.sleep(1_000L);

		gpio.shutdown();
	}
}
