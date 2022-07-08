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
public class Relay01 {
	public static void main(String... args) throws InterruptedException {

		System.out.println("GPIO Control - pin 00/#17 and 01/#18 ... started.");
		System.out.println("(Labelled #17 and #18 on the cobbler.)");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// For a relay it seems that HIGH means NC (Normally Closed)...
		final GpioPinDigitalOutput pin17 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Relay1", PinState.HIGH);
		final GpioPinDigitalOutput pin18 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Relay2", PinState.HIGH);
		System.out.println("--> GPIO state should be: OFF, and it is ");

		Thread.sleep(1_000);

		pin17.low();
		System.out.println("--> pin 17 should be: ON, and it is " + (pin17.isHigh() ? "OFF" : "ON"));

		Thread.sleep(1_000);

		pin17.high();
		System.out.println("--> pin 17 should be: OFF");

		Thread.sleep(1_000);

		pin18.low();
		System.out.println("--> pin 18 should be: ON");

		Thread.sleep(1_000);

		pin18.high();
		System.out.println("--> pin 18 should be: OFF");

		gpio.shutdown();
	}
}
