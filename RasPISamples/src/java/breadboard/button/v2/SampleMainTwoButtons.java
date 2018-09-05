package breadboard.button.v2;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

public class SampleMainTwoButtons {

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

		Runnable noOp = () -> {
			System.out.println("Duh.");
		};

		Pin appPin   = RaspiPin.GPIO_01; // The hot pin for this button. The other is GND.
		Pin shiftPin = RaspiPin.GPIO_02; // The hot pin for this button. The other is GND.

		String[] map = new String[3];
		map[0] = String.valueOf(PinUtil.findByPin(appPin).pinNumber()) + ":" + "BUTTON Hot Wire";
		map[1] = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber()) + ":" + "3v3";
		map[2] = String.valueOf(PinUtil.findByPin(shiftPin).pinNumber()) + ":" + "Shft";

		PinUtil.print(map);

		pbmOne.update(
				"App-Button",
				appPin,
				onClick,
				onDoubleClick,
				onLongClick);

		pbmShift.update(
				"Shift-Button",
				shiftPin,
				noOp,
				noOp,
				noOp);

		final Thread me = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (me) {
				me.notify();
			}
		}));

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
