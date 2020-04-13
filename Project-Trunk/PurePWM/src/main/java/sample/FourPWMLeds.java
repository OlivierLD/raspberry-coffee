package sample;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import i2c.sensor.utils.PWMPin;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pure PWM, with 4 leds, glowing up and down.
 * No PCB, no I2C, pure GPIO.
 * OK for a led, not for a servo.
 */
public class FourPWMLeds {

	private static boolean go = true;
	private static int[] signs = { 1, 1, 1, 1 };

	private static PWMPin pin01 = null;
	private static PWMPin pin02 = null;
	private static PWMPin pin03 = null;
	private static PWMPin pin04 = null;

	private static int changeValue(int val, int index) {
		val += signs[index];
		if (val == 100 || val == 0) {
			signs[index] *= -1;
		}
		return val;
	}

	public static void main(String... args) throws Exception {

		GpioController gpio = null;
		final AtomicBoolean simulating = new AtomicBoolean(false);
		try {
			gpio = GpioFactory.getInstance();
		} catch (Error error) {
			if (error instanceof UnsatisfiedLinkError) {
				System.out.println("Will simulate");
				simulating.set(true);
			} else {
				error.printStackTrace();
				System.exit(1);
			}
		}
		final GpioController gpioClone = gpio;

		// Off
		if (!simulating.get()) {
			pin01 = new PWMPin(RaspiPin.GPIO_00, "one", PinState.LOW);
			pin02 = new PWMPin(RaspiPin.GPIO_01, "two", PinState.LOW);
			pin03 = new PWMPin(RaspiPin.GPIO_02, "three", PinState.LOW);
			pin04 = new PWMPin(RaspiPin.GPIO_03, "four", PinState.LOW);

			Thread.sleep(1_000);

			pin01.emitPWM(0);
			pin02.emitPWM(0);
			pin03.emitPWM(0);
			pin04.emitPWM(0);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			// Turn it off
			if (!simulating.get()) {
				pin01.emitPWM(0);
				pin02.emitPWM(0);
				pin03.emitPWM(0);
				pin04.emitPWM(0);

				gpioClone.shutdown();
			}
			System.out.println("\nBye");
		}, "Shutdown Hook"));
		// Main loop
		int valueOne = (int)Math.round(100 * Math.random());
		int valueTwo = (int)Math.round(100 * Math.random());
		int valueThree = (int)Math.round(100 * Math.random());
		int valueFour = (int)Math.round(100 * Math.random());
		while (go) {
			valueOne = changeValue(valueOne, 0);
			valueTwo = changeValue(valueTwo, 1);
			valueThree = changeValue(valueThree, 2);
			valueFour = changeValue(valueFour, 3);
			if (!simulating.get()) {
				pin01.emitPWM(valueOne);
				pin02.emitPWM(valueTwo);
				pin03.emitPWM(valueThree);
				pin04.emitPWM(valueFour);
			} else {
				System.out.println(String.format("Led Values - 1:%03d, 2:%03d, 3:%03d, 4:%03d", valueOne, valueTwo, valueThree, valueFour));
			}
 		}
	}
}
