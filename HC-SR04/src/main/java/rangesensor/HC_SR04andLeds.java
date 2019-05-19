package rangesensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * See https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
 * <p>
 * This version is multi-threaded.
 * This allows the management of a signal that does not come back.
 */
public class HC_SR04andLeds {
	private final static Format DF22 = new DecimalFormat("#0.00");
	private final static double SOUND_SPEED = 34_300;          // in cm/s, 343 m/s
	private final static double DIST_FACT = SOUND_SPEED / 2; // round trip
	private final static int MIN_DIST = 5;

	private final static long BETWEEN_LOOPS = 500L;
	private final static long MAX_WAIT = 500L;

	private final static boolean DEBUG = false;

	public static void main(String... args)
			throws InterruptedException {
		System.out.println("GPIO Control - Range Sensor HC-SR04.");
		System.out.println("Will stop is distance is smaller than " + MIN_DIST + " cm");

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalOutput trigPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Trig", PinState.LOW);
		final GpioPinDigitalInput echoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "Echo");

		final GpioPinDigitalOutput ledOne = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "One", PinState.LOW);
		final GpioPinDigitalOutput ledTwo = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Two", PinState.LOW);
		final GpioPinDigitalOutput ledThree = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Three", PinState.LOW);
		final GpioPinDigitalOutput ledFour = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Four", PinState.LOW);
		final GpioPinDigitalOutput ledFive = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Five", PinState.LOW);

		final GpioPinDigitalOutput[] ledArray = new GpioPinDigitalOutput[]{ ledOne, ledTwo, ledThree, ledFour, ledFive };

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Oops!");
			for (int i = 0; i < ledArray.length; i++) {
				ledArray[i].low();
			}
			gpio.shutdown();
			System.out.println("Exiting nicely.");
		}));

		System.out.println("Waiting for the sensor to be ready (2s)...");
		Thread.sleep(2_000L);
		Thread mainThread = Thread.currentThread();

		boolean go = true;
		System.out.println("Looping until the distance is less than " + MIN_DIST + " cm");
		while (go) {
			boolean ok = true;
			double start = 0d, end = 0d;
			if (DEBUG) System.out.println("Triggering module.");
			TriggerThread trigger = new TriggerThread(mainThread, trigPin, echoPin);
			trigger.start();
			try {
				synchronized (mainThread) {
					long before = System.currentTimeMillis();
					mainThread.wait(MAX_WAIT);
					long after = System.currentTimeMillis();
					long diff = after - before;
					if (DEBUG) {
						System.out.println("MainThread done waiting (" + Long.toString(diff) + " ms)");
					}
					if (diff >= MAX_WAIT) {
						ok = false;
						if (true || DEBUG) System.out.println("...Reseting.");
						if (trigger.isAlive()) {
							trigger.interrupt();
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				ok = false;
			}

			if (ok) {
				start = trigger.getStart();
				end = trigger.getEnd();
				if (DEBUG) {
					System.out.println("Measuring...");
				}
				if (end > 0 && start > 0) {
//        double pulseDuration = (end - start) / 1000000000d; // in seconds
					double pulseDuration = (end - start) / 1E9; // in seconds
					double distance = pulseDuration * DIST_FACT;
					if (distance < 1_000) { // Less than 10 meters
						System.out.println("Distance: " + DF22.format(distance) + " cm."); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
					}
					if (distance > 0 && distance < MIN_DIST) {
						go = false;
					} else {
						if (distance < 0) {
							System.out.println("Dist:" + distance + ", start:" + start + ", end:" + end);
						}
						for (int i = 0; i < ledArray.length; i++) {
							if (distance < ((i + 1) * 10)) {
								ledArray[i].high();
							} else {
								ledArray[i].low();
							}
						}
						try {
							Thread.sleep(BETWEEN_LOOPS);
						} catch (Exception ex) {
						}
					}
				} else {
					System.out.println("Hiccup!");
					//  try { Thread.sleep(2_000L); } catch (Exception ex) {}
				}
			}
		}
		System.out.println("Done.");
		for (int i = 0; i < ledArray.length; i++) {
			ledArray[i].low();
		}
		trigPin.low(); // Off

		gpio.shutdown();
		System.exit(0);
	}

	private static class TriggerThread extends Thread {
		private GpioPinDigitalOutput trigPin = null;
		private GpioPinDigitalInput echoPin = null;
		private Thread caller = null;

		private double start = 0D, end = 0D;

		public TriggerThread(Thread parent, GpioPinDigitalOutput trigger, GpioPinDigitalInput echo) {
			this.trigPin = trigger;
			this.echoPin = echo;
			this.caller = parent;
		}

		public void run() {
			trigPin.high();
			// 10 microsec (10000 ns) to trigger the module  (8 ultrasound bursts at 40 kHz)
			// https://www.dropbox.com/s/615w1321sg9epjj/hc-sr04-ultrasound-timing-diagram.png
			try {
				Thread.sleep(0, 10_000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			trigPin.low();

			// Wait for the signal to return
			while (echoPin.isLow()) {
				start = System.nanoTime();
			}
			// There it is
			while (echoPin.isHigh()) {
				end = System.nanoTime();
			}
			synchronized (caller) {
				caller.notify();
			}
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}
	}
}
