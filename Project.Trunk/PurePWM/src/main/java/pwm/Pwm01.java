package pwm;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;

import static utils.TimeUtil.delay;

/**
 * See: http://wiringpi.com/reference/software-pwm-library/
 * Suitable pins for PWM are GPIO_01, GPIO_23, GPIO_24, GPIO_26.
 * See {@link RaspiPin} source.
 *
 */
public class Pwm01 {
	public static void main(String... args)
					throws InterruptedException {

		System.out.println("PWM Control - pin 01 ... started.");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinPwmOutput pin = gpio.provisionSoftPwmOutputPin(RaspiPin.GPIO_02); // , "Standard-Servo");
//	GpioPinPwmOutput pin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_01); // , "Standard-Servo");

//	pin.setMode(PinMode.PWM_OUTPUT);
		pin.setPwmRange(100);
		System.out.println("Setting PWM to 100");
		pin.setPwm(100);
		delay(1_000L);
		System.out.println("Setting PWM to 50");
		pin.setPwm(50);
		delay(1_000L);
		System.out.println("Setting PWM to 0");
		pin.setPwm(0);

		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
		System.out.println("Bye");
	}
}
