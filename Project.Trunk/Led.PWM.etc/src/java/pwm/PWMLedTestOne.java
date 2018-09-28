package pwm;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import static utils.TimeUtil.delay;

public class PWMLedTestOne {
	public static void main(String... args)
					throws InterruptedException {

		System.out.println("GPIO Control - pin 01 ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "OneLED", PinState.HIGH);
		System.out.println("--> GPIO state should be: ON");

		Thread.sleep(1_000);

		// turn off gpio pin #01
		pin.low();
		System.out.println("--> GPIO state should be: OFF");

		if (false) {
			Thread.sleep(1_000);

			// toggle the current state of gpio pin #01 (should turn on)
			pin.toggle();
			System.out.println("--> GPIO state should be: ON");

			Thread.sleep(1_000);

			// toggle the current state of gpio pin #01  (should turn off)
			pin.toggle();
			System.out.println("--> GPIO state should be: OFF");

			Thread.sleep(2_000);

			// turn on gpio pin #01 for 1 second and then off
			System.out.println("--> GPIO state should be: ON for only 1 second");
			pin.pulse(1_000, true); // set second argument to 'true' use a blocking call

			pin.low();
			long before = System.currentTimeMillis();
			for (int i = 0; i < 10_000; i++) {
				pin.high();
				pin.low();
			}
			long after = System.currentTimeMillis();
			System.out.println("10,000 switches took " + Long.toString(after - before) + " ms.");
		}

		System.out.println("PWM!!!");
		// PWM
		int threshold = 25;
		int nbLoop = 5;
		for (int pwmValueOn = 1; pwmValueOn < threshold; pwmValueOn++) {
			System.out.println("PWM " + pwmValueOn);
			for (int i = 0; i < nbLoop; i++) {
				pin.pulse(pwmValueOn, true); // set second argument to 'true' use a blocking call
				//    waitFor(pwmValueOn);
				pin.low();
				delay(threshold - pwmValueOn);
			}
//    Thread.sleep(500);
		}
		for (int pwmValueOn = threshold; pwmValueOn > 0; pwmValueOn--) {
			System.out.println("PWM " + pwmValueOn);
			for (int i = 0; i < nbLoop; i++) {
				pin.pulse(pwmValueOn, true); // set second argument to 'true' use a blocking call
				//    waitFor(pwmValueOn);
				pin.low();
				delay(threshold - pwmValueOn);
			}
			//    Thread.sleep(500);
		}

		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
	}
}
