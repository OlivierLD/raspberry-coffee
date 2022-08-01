package utils.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Just test, to be run separately, for debugging...
 * 2 Buttons:
 * - One and Two pin on 3V3
 * - One pin on GPIO 28 (BCM 20, physical #38)
 * - Two pin on GPIO 29 (BCM 21, physical #40)
 */
public class TwoPushButtonControllerSample {

	private Pin buttonOnePin;
	private Pin buttonTwoPin;

	final static PushButtonController buttonOne = new PushButtonController();
	final static PushButtonController buttonTwo = new PushButtonController();

	private final Runnable sayHelloOne = () -> {
		try {
			System.out.println("OnClick [One]: Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello:");
			ex.printStackTrace();
		}
	};
	private final Runnable sayHelloHelloOne = () -> {
		try {
			System.out.println("OnDoubleClick [One]: Hello Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello Hello:");
			ex.printStackTrace();
		}
	};
	private final Runnable sayHelloooOne = () -> {
		try {
			System.out.println("OnLongClick [One]: Hellooo!");
		} catch (Exception ex) {
			System.err.println("Say Hellooo:");
			ex.printStackTrace();
		}
	};
	private final Runnable sayHelloTwo = () -> {
		try {
			System.out.println("OnClick [Two]: Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello:");
			ex.printStackTrace();
		}
	};
	private final Runnable sayHelloHelloTwo = () -> {
		try {
			System.out.println("OnDoubleClick [Two]: Hello Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello Hello:");
			ex.printStackTrace();
		}
	};
	private final Runnable sayHelloooTwo = () -> {
		try {
			System.out.println("OnLongClick [Two]: Hellooo!");
		} catch (Exception ex) {
			System.err.println("Say Hellooo:");
			ex.printStackTrace();
		}
	};

	public TwoPushButtonControllerSample() {
		try {
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_28; // wiPi 28, BCM 20, Physical #38.
			buttonTwoPin = RaspiPin.GPIO_29; // wiPi 29, BCM 21, Physical #40.

			// Button-1 provisioning, with its name and operations
			buttonOne.update(
					"Button-One",
					buttonOnePin,
					sayHelloOne,
					sayHelloHelloOne,
					sayHelloooOne);
			// Button-1 provisioning, with its name and operations
			buttonTwo.update(
					"Button-Two",
					buttonTwoPin,
					sayHelloTwo,
					sayHelloHelloTwo,
					sayHelloooTwo);

		} catch (Throwable error) {
			error.printStackTrace();
		}
		System.out.println(">> Button 28 (physical #38) provisioned.");
		System.out.println(">> Button 29 (physical #40) provisioned.");
		System.out.println("\tTry click, double-click, long-click.");

	}

	public static void freeResources() {
		// Cleanup
		buttonOne.freeResources();
	}

	public static void main(String... args) {

		final Thread me = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (me) {
				freeResources();
				me.notify();
				try {
					me.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "Shutdown Hook"));

		// System.setProperty("button.verbose", "true");

		new TwoPushButtonControllerSample();

		// Now wait for the user to stop the program
		System.out.println("Ctrl-C to stop.");
		try {
			synchronized (me) {
				me.wait();
				System.out.println("\nOoch!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Bye!");
	}

}
