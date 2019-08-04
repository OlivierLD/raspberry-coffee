package rgbled;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import utils.StaticUtil;

public class RGBLed {

	public static void main(String... args)
			throws InterruptedException {
		System.out.println("GPIO Control - pin 00, 01 & 02 ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalOutput greenPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "green", PinState.LOW);
		final GpioPinDigitalOutput bluePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "blue", PinState.LOW);
		final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "red", PinState.LOW);

		/*
		 * yellow  = R+G
		 * cyan    = G+B
		 * magenta = R+B
		 * white   = R+G+B
		 */

		boolean go = true;
		while (go) {
			String s = StaticUtil.userInput("R, G, B, or QUIT > ");
			if ("R".equals(s.toUpperCase())) {
				redPin.toggle();
			} else if ("G".equals(s.toUpperCase())) {
				greenPin.toggle();
			} else if ("B".equals(s.toUpperCase())) {
				bluePin.toggle();
			} else if ("QUIT".equals(s.toUpperCase()) || "Q".equals(s.toUpperCase())) {
				go = false;
			} else {
				System.out.println("Unknown command [" + s + "]");
			}
		}
		// Switch them off
		redPin.low();
		greenPin.low();
		bluePin.low();
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
	}
}
