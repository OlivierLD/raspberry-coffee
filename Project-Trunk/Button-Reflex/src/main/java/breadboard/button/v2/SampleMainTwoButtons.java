package breadboard.button.v2;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;
import utils.gpio.StringToGPIOPin;

import java.util.Arrays;

/**
 * Two buttons:
 * - One App button
 * - One Shift button
 *
 * Trap the following events on the App button, with or without [Shift]:
 * - Single click
 * - Double click
 * - Long click
 */
public class SampleMainTwoButtons {

	private final static String BUTTON_PREFIX = "--button:";
	private final static String SHIFT_PREFIX  = "--shift:";  // Acts like a [Shift] key

	// Default button pins
	private static Pin appPin   = RaspiPin.GPIO_01; // The hot pin for this button. The other is 3v3.
	private static Pin shiftPin = RaspiPin.GPIO_02; // The hot pin for this button. The other is 3v3.

	public static void main(String... args) {

		final PushButtonMaster pbmOne = new PushButtonMaster();
		final PushButtonMaster pbmShift = new PushButtonMaster();

		// Action to take depending on the type of click.
		Runnable onClick = () -> {
			System.out.println(String.format(">> %sSingle click", (pbmShift.isPushed() ? "[Shft] + " : "")));
		};
		Runnable onDoubleClick = () -> {
			System.out.println(String.format(">> %sDouble click", (pbmShift.isPushed() ? "[Shft] + " : "")));
		};
		Runnable onLongClick = () -> {
			System.out.println(String.format(">> %sLong click", (pbmShift.isPushed() ? "[Shft] + " : "")));
		};
		/**
		 *  For the Shift button, no operation needed. We only need if it is up or down.
		 *  See {@link PushButtonMaster#isPushed()}
 		 */

		// Pins as program argument. Physical pins [1..40]
		if (args.length > 0) {
			Arrays.stream(args).forEach(arg -> {
				if (arg.startsWith(BUTTON_PREFIX)) {
					String bStrPin = arg.substring(BUTTON_PREFIX.length());
					try {
						int bPin = Integer.parseInt(bStrPin);
						appPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(bPin));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else if (arg.startsWith(SHIFT_PREFIX)) {
					String shStrPin = arg.substring(SHIFT_PREFIX.length());
					try {
						int shPin = Integer.parseInt(shStrPin);
						shiftPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(shPin));
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			});
		}

		String[] map = new String[3];
		map[0] = String.valueOf(PinUtil.findByPin(appPin.getName()).pinNumber()) + ":" + "BUTTON Hot Wire";
		map[1] = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber()) + ":" + "3v3";
		map[2] = String.valueOf(PinUtil.findByPin(shiftPin.getName()).pinNumber()) + ":" + "Shift";

		PinUtil.print(map);

		pbmOne.update(
				"App-Button",
				appPin,
				onClick,
				onDoubleClick,
				onLongClick);

		pbmShift.update(
				"Shift-Button",
				shiftPin);

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
		pbmOne.freeResources();
		pbmShift.freeResources();
		System.out.println("\nBye!");
	}
}
