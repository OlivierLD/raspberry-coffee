package tests;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import pwm.PWMPin;
import utils.PinUtil;

import static utils.StaticUtil.userInput;

/**
 * No breakout board required.
 * Pure Soft PWM from the GPIO header.
 * <br/>
 * <b><i>Theoretically</i></b>, servos follow those rules:
 * <table border='1'>
 * <tr><th>Pulse</th><th>Standard</th><th>Continuous</th></tr>
 * <tr><td>1.5 ms</td><td align='right'>0 &deg;</td><td>Stop</td></tr>
 * <tr><td>2.0 ms</td><td align='right'>90 &deg;</td><td>FullSpeed forward</td></tr>
 * <tr><td>1.0 ms</td><td align='right'>-90 &deg;</td><td>FullSpeed backward</td></tr>
 * </table>
 * That happens not to be always true, some servos (like <a href="https://www.adafruit.com/product/169">https://www.adafruit.com/product/169</a> or <a href="https://www.adafruit.com/product/155">https://www.adafruit.com/product/155</a>)
 * have values going between `0.5 ms` and `2.5 ms`.
 */
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
		PinUtil.print(
				String.format("%d:Servo", PinUtil.findByPin(servoPin).pinNumber()), // Yellow
				"6:brown", // Can also be black
				"2:red"    // Orange-ish
		);
		GpioController gpio = null;
		try {
			gpio = GpioFactory.getInstance();
		} catch (Throwable t) {
			System.err.println("This work only on a Raspberry Pi...");
			System.exit(1);
		}

		float cycleWidth = (1_000f / 60f); // 16.6666f; // In ms. 50 Hertz, 1000 / 20. 60 Hz: 16.6666 ms.
		PWMPin pin = new PWMPin(servoPin, "OneServo", PinState.LOW, cycleWidth);
		// pin.low(); // Useless

		System.out.println("PWM, by pulse length");
		pin.emitPWM(1.5f); // PWM, center servo.
//  Thread.sleep(1_000);
		System.out.println(String.format("Enter \"S\" or \"quit\" to stop, or a pulse in ms [0..%.02f]", cycleWidth));
		boolean go = true;
		while (go) {
			String userInput = userInput("Pulse in ms > ");
			if ("S".equalsIgnoreCase(userInput) ||
					"quit".equalsIgnoreCase(userInput)) {
				go = false;
			} else {
				try {
					float pulse = Float.parseFloat(userInput);
					pin.emitPWM(pulse);
				} catch (NumberFormatException nfe) {
					System.out.println(nfe.toString());
				} catch (Throwable t) {
					System.out.println(t.toString());
				}
			}
		}
		System.out.println("Exiting loop");
//		if (pin.isPWMing()) {
//			pin.stopPWM();
//		}
		pin.stopPWM();

		Thread.sleep(1_000);
		// Last blink
		System.out.println("Bye-bye");
		pin.low();
		Thread.sleep(500);

		gpio.shutdown();
	}
}
