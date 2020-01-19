package i2c.sensor.utils;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * Suitable for an LED, not for a servo (cycle width...)
 */
public class PWMPin extends GPIOPinAdapter {
	// 30 seems to be the maximum value.
	// You can really see the led blinking beyond that.
	private final static int CYCLE_WIDTH = 30; // ms

	private final Thread mainThread;
	private final boolean debug = "true".equals(System.getProperty("debug", "false"));

	public PWMPin(Pin p, String name, PinState originalState) {
		super(p, name, originalState);
		mainThread = Thread.currentThread();
	}

	private boolean emittingPWM = false;
	private int pwmVolume = 0; // [0..CYCLE_WIDTH], percent / (100 / CYCLE_WIDTH);

	public void emitPWM(final int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		if (debug) {
			System.out.println("Volume:" + percentToVolume(percent) + "/" + CYCLE_WIDTH);
		}
		Thread pwmThread = new Thread(() -> {
			emittingPWM = true;
			pwmVolume = percentToVolume(percent);
			while (emittingPWM) {
				if (pwmVolume > 0) {
					pin.pulse(pwmVolume, true); // set second argument to 'true' makes a blocking call
				}
				pin.low();
				waitFor(CYCLE_WIDTH - pwmVolume);  // Wait for the rest of the cycle
			}
			System.out.println("Stopping PWM");
			// Notify the ones waiting for this thread to end
			synchronized (mainThread) {
				mainThread.notify();
			}
		});
		pwmThread.start();
	}

	/**
	 * return a number in [0..CYCLE_WIDTH]
	 *
	 * @param percent in [0..100]
	 * @return a number in [0..CYCLE_WIDTH]
	 */
	private int percentToVolume(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		return percent / (100 / CYCLE_WIDTH);
	}

	public void adjustPWMVolume(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		pwmVolume = percentToVolume(percent);
	}

	public boolean isPWMing() {
		return emittingPWM;
	}

	public void stopPWM() {
		emittingPWM = false;
		synchronized (mainThread) {
			try {
				mainThread.wait();
			} catch (InterruptedException ie) {
				System.out.println(ie.toString());
			}
		}
		pin.low();
	}

	private void waitFor(long ms) {
		if (ms <= 0) {
			return;
		}
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}
