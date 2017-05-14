package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Adafruit JoyBonnet for the Raspberry PI
 * https://www.adafruit.com/product/3464
 *
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

		new PushButtonInstance(gpio, RaspiPin.GPIO_00, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (0) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_01, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (1) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_02, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (2) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_03, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (3) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_04, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (4) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_05, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (5) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_06, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (6) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_07, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (7) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_08, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (8) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_09, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (9) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_10, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (10) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_11, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (11) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_12, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (12) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_13, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (13) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_14, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (14) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_15, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (15) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_16, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (16) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_17, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (17) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_18, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (18) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_19, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (19) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_20, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (20) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_21, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (21) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_22, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (22) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_23, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (23) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_24, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (24) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_25, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (25) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_26, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (26) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_27, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (27) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_28, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (28) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_29, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (29) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_30, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (30) " + event.toString()));
		new PushButtonInstance(gpio, RaspiPin.GPIO_31, (event) -> System.out.println(">>>>>>>>>>>>>>  Received button event (31) " + event.toString()));

		Thread waiter = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized(waiter) {
				waiter.notify();
				try { Thread.sleep(20); } catch (Exception ex) {}
				System.out.println("Bye");
			}
		}));

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
