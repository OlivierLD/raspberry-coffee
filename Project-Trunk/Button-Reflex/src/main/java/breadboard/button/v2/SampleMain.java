package breadboard.button.v2;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

public class SampleMain {

	// TODO Button pins as parameters
	public static void main(String... args) {

		// Action to take depending on the type of click.
		Runnable onClick = () -> {
			System.out.println(">> Single click");
		};
		Runnable onDoubleClick = () -> {
			System.out.println(">> Double click");
		};
		Runnable onLongClick = () -> {
			System.out.println(">> Long click");
		};

		Pin pin = RaspiPin.GPIO_01; // The hot pin for this button. The other is 3v3.

		String[] map = new String[2];
		map[0] = PinUtil.findByPin(pin).pinNumber() + ":" + "BUTTON Hot Wire";
		map[1] = PinUtil.GPIOPin.PWR_1.pinNumber() + ":" + "3v3";

		PinUtil.print(map);

		PushButtonMaster pbm = new PushButtonMaster(
				"ForTest",
				pin,
				onClick,
				onDoubleClick,
				onLongClick);

		final Thread me = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (me) {
				me.notify();
			}
		}, "Shutdown Hook"));

		synchronized(me) {
			try {
				me.wait();
			} catch (InterruptedException ie) {
				me.interrupt();
			}
		}
		// Cleanup
		pbm.freeResources();
		System.out.println("\nBye!");
	}
}
