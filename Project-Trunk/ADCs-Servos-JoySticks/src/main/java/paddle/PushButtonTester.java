package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.exception.UnsupportedPinPullResistanceException;
import paddle.buttons.PushButtonInstance;

/**
 * Adafruit JoyBonnet for the Raspberry Pi
 * https://www.adafruit.com/product/3464
 * <p>
 * Use this class to see what button is connected, and where.
 */
public class PushButtonTester {
	final static GpioController gpio = GpioFactory.getInstance();


	public PushButtonTester() {
	}

	public void shutdown() {
		gpio.shutdown();
	}

	public static void main(String... args) {

		Pin[] pins = {
						RaspiPin.GPIO_00,
						RaspiPin.GPIO_01,
						RaspiPin.GPIO_02,
						RaspiPin.GPIO_03,
						RaspiPin.GPIO_04,
						RaspiPin.GPIO_05,
						RaspiPin.GPIO_06,
						RaspiPin.GPIO_07,
						RaspiPin.GPIO_08,
						RaspiPin.GPIO_09,
						RaspiPin.GPIO_10,
						RaspiPin.GPIO_11,
						RaspiPin.GPIO_12,
						RaspiPin.GPIO_13,
						RaspiPin.GPIO_14,
						RaspiPin.GPIO_15,
						RaspiPin.GPIO_16,
						RaspiPin.GPIO_17,
						RaspiPin.GPIO_18,
						RaspiPin.GPIO_19,
						RaspiPin.GPIO_20,
						RaspiPin.GPIO_21,
						RaspiPin.GPIO_22,
						RaspiPin.GPIO_23,
						RaspiPin.GPIO_24,
						RaspiPin.GPIO_25,
						RaspiPin.GPIO_26,
						RaspiPin.GPIO_27,
						RaspiPin.GPIO_28,
						RaspiPin.GPIO_29,
						RaspiPin.GPIO_30,
						RaspiPin.GPIO_31
		};
		for (int i=0; i<pins.length; i++) {
			try {
				final int idx = i;
				new PushButtonInstance(gpio, pins[i], String.format("Button %d", i), (event) -> System.out.println(String.format(">>>>>>>>>>>>>>  Received button event (%d) %s", idx, event.getPayload())));
			} catch (UnsupportedPinPullResistanceException uppre) {
				System.err.println("Un-appropriate pin:" + pins[i]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		Thread waiter = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (waiter) {
				waiter.notify();
				try {
					Thread.sleep(20);
				} catch (Exception ex) {
				}
				System.out.println("Bye");
			}
		}, "Shutdown Hook"));
		System.out.println("Ready...");
		synchronized (waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nDone reading buttons.");
	}
}
