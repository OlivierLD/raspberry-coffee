package breadboard.button.v2;

import com.pi4j.io.gpio.RaspiPin;

import java.util.function.Consumer;

public class SampleMain {

	public static void main(String... args) {

		utils.PinUtil.print();

		Runnable onClick = () -> {
			System.out.println(">> Single click");
		};
		Runnable onDoubleClick = () -> {
			System.out.println(">> Double click");
		};
		Runnable onLongClick = () -> {
			System.out.println(">> Long click");
		};

		Runnable vroom = () -> {
			System.out.println("Pouet!");
		};

		PushButtonMaster pbm = new PushButtonMaster("ForTest", onClick, onDoubleClick, onLongClick);
		pbm.initCtx(RaspiPin.GPIO_01);

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
