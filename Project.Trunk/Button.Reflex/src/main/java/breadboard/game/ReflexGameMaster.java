package breadboard.game;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import pushbutton.PushButtonObserver;

import static utils.TimeUtil.delay;

/**
 * Implements the nuts and bolts of the reflex game.
 * No need to worry about that in the main class.
 * From the main:
 * Invoke the initCtx method
 * Invoke the go method
 * in the onButtonPressed method, invoke the release method
 * Invoke the freeResources method
 */
public class ReflexGameMaster {
	private static long startedAt = 0L;
	private static Thread waiter = null;
	private final static long MAX_WAIT_TIME = 10_000L; // 10 sec max.

	private final GpioController gpio = GpioFactory.getInstance();
	private GpioPinDigitalOutput led = null;
	private GpioPinDigitalInput button = null;

	private PushButtonObserver pbo = null;

	public ReflexGameMaster(PushButtonObserver obs) {
		if (obs == null) {
			throw new IllegalArgumentException("Observer cannot be null");
		}
		this.pbo = obs;
	}

	public void initCtx() {
		initCtx(RaspiPin.GPIO_01, RaspiPin.GPIO_02);
	}

	public void initCtx(Pin ledPin, Pin buttonPin) {
		// provision gpio pin #01 as an output pin and turn it off
		led = gpio.provisionDigitalOutputPin(ledPin, "TheLED", PinState.LOW);
		button = gpio.provisionDigitalInputPin(buttonPin, PinPullResistance.PULL_DOWN);
		button.addListener((GpioPinListenerDigital) event -> {
			if (event.getState().isHigh()) {
				pbo.onButtonPressed();
			}
		});
	}

	public void release() {
		Thread waiter = this.getWaiter();
		synchronized (waiter) {
			waiter.notify();
		}
	}

	public long getStartTime() {
		return startedAt;
	}

	public void go() {
		System.out.println("Get ready...");
		long rnd = (MAX_WAIT_TIME * Math.round(1 - Math.random())); // TASK Parameter this amount?
		delay(rnd);

		// turn on the led
		led.high();
		System.out.println("Hit the button NOW!!");
		startedAt = System.currentTimeMillis();

		waiter = Thread.currentThread();
		synchronized (waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("Good Job!");
		led.low();
	}

	public void freeResources() {
		gpio.shutdown();
		System.exit(0);
	}

	private Thread getWaiter() {
		return waiter;
	}
}
