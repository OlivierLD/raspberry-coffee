package pwm;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import static utils.TimeUtil.delay;

/**
 * Pulse Width Modulation implementation
 */
public class PWMPin extends GPIOPinAdapter {
	// 30 seems to be the maximum value. You can really see the led blinking beyond that.
	private final static int DEFAULT_CYCLE_WIDTH = 30;
	private static int pulseCycleWidth = DEFAULT_CYCLE_WIDTH;

	private final Thread mainThread;
	private final boolean debug = "true".equals(System.getProperty("pwm.debug", "false"));

	public PWMPin(Pin p, String name, PinState originalState) {
		this(p, name, originalState, DEFAULT_CYCLE_WIDTH);
	}
	public PWMPin(Pin p, String name, PinState originalState, int cycleWidth) {
		super(p, name, originalState);
		mainThread = Thread.currentThread();
		pulseCycleWidth = cycleWidth;
	}

	private boolean emittingPWM = false;
	private int pwmVolume = 0; // [0..CYCLE_WIDTH], percent / (100 / CYCLE_WIDTH);

	public void emitPWM(final int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		if (debug) {
			System.out.println("Starting thread with Volume:" + percentToVolume(percent) + "/" + pulseCycleWidth);
			System.out.println(String.format("pwmVolume: %d", pwmVolume));
		}
		Thread pwmThread = new Thread(() -> {
			emittingPWM = true;
			pwmVolume = percentToVolume(percent);
			while (emittingPWM) {
				if (pwmVolume > 0) { // On
					pin.pulse(pwmVolume, true); // 'pin' is defined in the superclass GPIOPinAdapter, set second argument to 'true' makes a blocking call
				}
				pin.low();           // Off
				delay(Math.max(pulseCycleWidth - pwmVolume, 0));  // Wait for the rest of the cycle
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
	 * @return
	 */
	private int percentToVolume(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		int volume = (int)Math.round((double)percent / (100d / (double) pulseCycleWidth));
		if (debug) {
			System.out.println(String.format("percentToVolume: percent: %d => volume: %d (width: %d)", percent, volume, pulseCycleWidth));
		}
		return volume;
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
				Thread.currentThread().interrupt();
			}
		}
		pin.low();
	}
}
