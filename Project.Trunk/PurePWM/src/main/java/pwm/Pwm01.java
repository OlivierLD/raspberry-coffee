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
		PinUtil.print(new String[] { String.valueOf(PinUtil.findByPin(servoPin).pinNumber()) + ":" + "Servo" });

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

//	GpioPinPwmOutput pin = gpio.provisionSoftPwmOutputPin(servoPin, "Standard-Servo");
		GpioPinPwmOutput pin = gpio.provisionPwmOutputPin(servoPin, "Standard-Servo");

		pin.setMode(PinMode.PWM_OUTPUT);
		pin.setPwmRange(200);
		delay(1_000L);

		int[] servoValues = new int[] {
				20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160
		};

		for (int val : servoValues) {
			System.out.println(String.format("Setting PWM to %d", val));
			pin.setPwm(val);
			delay(1_000L);
		}
		if (false) {
			System.out.println("Setting PWM to 100");
			pin.setPwm(100);
			delay(1_000L);
			System.out.println("Setting PWM to 50");
			pin.setPwm(50);
			delay(1_000L);
			System.out.println("Setting PWM to 0");
			pin.setPwm(0);
		}
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		gpio.shutdown();
		System.out.println("Bye");
	}
}
