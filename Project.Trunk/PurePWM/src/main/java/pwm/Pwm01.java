package pwm;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

import static utils.TimeUtil.delay;

/**
 * uses -Dservo.pin, physical number of the servo pin, default is 13
 */
public class Pwm01 {
	public static void main(String... args)
					throws InterruptedException {

		Pin servoPin = RaspiPin.GPIO_02; // GPIO_02 => Physical #13, BCM 27

		String servoPinSysVar = System.getProperty("servo.pin"); // Physical number
		if (servoPinSysVar != null) {
			try {
				int servoPinValue = Integer.parseInt(servoPinSysVar);
				servoPin = PinUtil.getPinByPhysicalNumber(servoPinValue);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		System.out.println(String.format("PWM Control - pin %s ... started.", PinUtil.findByPin(servoPin).pinName()));

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinPwmOutput pin = gpio.provisionSoftPwmOutputPin(servoPin, "Standard-Servo");

		pin.setMode(PinMode.PWM_OUTPUT);
		pin.setPwmRange(100);
		delay(1_000L);
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
