package tests;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import pwm.PWMPin;
import utils.PinUtil;

import static utils.StaticUtil.userInput;

public class RealPWMServo {
	public static void main(String... args)
			throws InterruptedException {
		Pin servoPin = RaspiPin.GPIO_01; // Default: GPIO_01 => Physical #12, BCM 18

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
		PinUtil.print(new String[] { String.format("%d:Servo", PinUtil.findByPin(servoPin).pinNumber()) });
		GpioController gpio = null;
		try {
			gpio = GpioFactory.getInstance();
		} catch (Throwable t) {
			System.err.println("This work only on a Raspberry Pi...");
			System.exit(1);
		}

		PWMPin pin = new PWMPin(servoPin, "OneServo", PinState.LOW);
		// pin.low(); // Useless

		System.out.println("PWM, up and down in [0..100]");
		// PWM
		pin.emitPWM(0);
		Thread.sleep(1_000);
		for (int vol = 0; vol < 100; vol++) {
			pin.adjustPWMVolume(vol);
			Thread.sleep(10);
		}
		for (int vol = 100; vol >= 0; vol--) {
			pin.adjustPWMVolume(vol);
			Thread.sleep(10);
		}

		System.out.println("Enter \"S\" or \"quit\" to stop, or a volume [0..100]");
		boolean go = true;
		while (go) {
			String userInput = userInput("Volume > ");
			if ("S".equalsIgnoreCase(userInput) ||
					"quit".equalsIgnoreCase(userInput)) {
				go = false;
			} else {
				try {
					int vol = Integer.parseInt(userInput);
					pin.adjustPWMVolume(vol);
				} catch (NumberFormatException nfe) {
					System.out.println(nfe.toString());
				}
			}
		}
		pin.stopPWM();

		Thread.sleep(1_000);
		// Last blink
		System.out.println("Bye-bye");
		pin.low();
		Thread.sleep(500);
		pin.high();
		Thread.sleep(500);
		pin.low();

		gpio.shutdown();
	}
}
