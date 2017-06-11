package pwm;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;

public class Pwm01 {
	public static void main(String[] args)
					throws InterruptedException
	{

		System.out.println("GPIO Control - pin 01 ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		final GpioPinPwmOutput pin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_01, "Standard-Servo");
		pin.setMode(PinMode.PWM_OUTPUT);
		pin.setPwmRange(2000);
		pin.setPwm(150);
		delay(1000);
		pin.setPwm(200);

		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
	}

	private static void delay(long ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ignore) {}
	}
}