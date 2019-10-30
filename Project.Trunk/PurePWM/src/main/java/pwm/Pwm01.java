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
 * uses -Dservo.pin, physical number of the servo pin, default is 12
 *
 * GPIO_01, GPIO_23, GPIO_24, GPIO_26.
 *
 * Warning: requires a bug fix in GpioProviderBase, available after 13-Jul-2019
 */
public class Pwm01 {
	public static void main(String... args)
					throws InterruptedException {

		Pin servoPin = RaspiPin.GPIO_01; // GPIO_01 => Physical #12, BCM 18

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
		PinUtil.print(String.format("%d:Servo", PinUtil.findByPin(servoPin).pinNumber()));

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		GpioPinPwmOutput pin = gpio.provisionSoftPwmOutputPin(servoPin, "Standard-Servo");
//		GpioPinPwmOutput pin = gpio.provisionPwmOutputPin(servoPin, "Standard-Servo");

//		pin.setMode(PinMode.PWM_OUTPUT);
		pin.setPwmRange(2_000);
		delay(1_000L);

		int val = 150;
		System.out.println(String.format("Setting PWM to %d", val));
		pin.setPwm(val);
		delay(1_000L);

		for (int i=0; i<250; i++) {
			System.out.println(String.format("Setting PWM to %d", i));
			pin.setPwm(i);
			delay(50L);
		}

		val = 2_000;
		System.out.println(String.format("Setting PWM to %d", val));
		pin.setPwm(val);
		delay(1_000L);

		val = 200;
		System.out.println(String.format("Setting PWM to %d", val));
		pin.setPwm(val);
		delay(1_000L);

		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
		System.out.println("Bye");
	}
}
