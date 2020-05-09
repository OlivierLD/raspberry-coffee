package utils.samples.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import utils.gpio.PushButtonController;

public class PushButtonControllerSample {

	private Pin buttonOnePin;

	final static PushButtonController buttonOne = new PushButtonController();

	private Runnable sayHello = () -> {
		try {
			System.out.println("Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello:");
			ex.printStackTrace();
		}
	};
	private Runnable sayHellooo = () -> {
		try {
			System.out.println("Hellooo!");
		} catch (Exception ex) {
			System.err.println("Say Hellooo:");
			ex.printStackTrace();
		}
	};
	private Runnable sayHelloHello = () -> {
		try {
			System.out.println("Hello Hello!");
		} catch (Exception ex) {
			System.err.println("Say Hello Hello:");
			ex.printStackTrace();
		}
	};

	public PushButtonControllerSample() {
		try {
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_28; // Physical #38.

			// Button-1 provisioning, with its operations
			buttonOne.update(
					"The-Button",
					buttonOnePin,
					sayHello,
					sayHelloHello,
					sayHellooo);

		} catch (Throwable error) {
			error.printStackTrace();
		}
		System.out.println(">> Button 28 (physical #38) provisioned.");
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
			}
		}, "Shutdown Hook"));

		new PushButtonControllerSample();

		// Now wait for the user to stop the program
		try {
			synchronized (me) {
				me.wait();
				System.out.println("Ooch!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Bye!");
	}

}
