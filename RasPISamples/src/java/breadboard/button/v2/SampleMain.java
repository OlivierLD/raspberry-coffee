package breadboard.button.v2;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

public class SampleMain {

	public static void main(String... args) {

		Runnable onClick = () -> {
			System.out.println(">> Single click");
		};
		Runnable onDoubleClick = () -> {
			System.out.println(">> Double click");
		};
		Runnable onLongClick = () -> {
			System.out.println(">> Long click");
		};

		Pin pin = RaspiPin.GPIO_01;

		String[] map = new String[2];
		map[0] = String.valueOf(PinUtil.findByPin(pin).pinNumber()) + ":" + "BUTTON Hot Wire";
		map[1] = String.valueOf(PinUtil.GPIOPin.GRND_1.pinNumber()) + ":" + "BUTTON GND";

		utils.PinUtil.print(map);

		PushButtonMaster pbm = new PushButtonMaster("ForTest", onClick, onDoubleClick, onLongClick);
		pbm.initCtx(pin);

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

		pbm.freeResources();
		System.out.println("\nBye!");
	}
}
