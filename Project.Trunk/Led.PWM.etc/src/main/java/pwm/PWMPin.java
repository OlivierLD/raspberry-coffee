package pwm;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import java.util.concurrent.TimeUnit;

import static utils.TimeUtil.delay;

/**
 * Pulse Width Modulation implementation. WIP.
 */
public class PWMPin extends GPIOPinAdapter {
	// 30 seems to be the maximum value for a led. You can really see the led blinking beyond that.
	private final static int DEFAULT_CYCLE_WIDTH = 30;
	private float pulseCycleWidth = DEFAULT_CYCLE_WIDTH;

	private final Thread mainThread;
	private final boolean debug = "true".equals(System.getProperty("pwm.debug", "false"));

	public PWMPin(Pin p, String name, PinState originalState) {
		this(p, name, originalState, DEFAULT_CYCLE_WIDTH);
	}
	public PWMPin(Pin p, String name, PinState originalState, float cycleWidth) {
		super(p, name, originalState);
		mainThread = Thread.currentThread();
		this.pulseCycleWidth = cycleWidth;
	}

	private boolean emittingPWM = false;
	private int pwmVolume = 0; // [0..CYCLE_WIDTH], percent / (100 / CYCLE_WIDTH);

	public void emitPWM(final int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		if (debug) {
			System.out.println("Starting thread with Volume:" + percentToVolume(percent) + "/" + this.pulseCycleWidth);
			System.out.println(String.format("pwmVolume: %d", pwmVolume));
		}
		Thread pwmThread = new Thread(() -> {
			emittingPWM = true;
			pwmVolume = percentToVolume(percent);
			while (emittingPWM) {
				if (pwmVolume > 0) { // On
					pin.pulse(pwmVolume, true); // 'pin' is defined in the superclass GPIOPinAdapter, set second argument to 'true' makes a blocking call
				}
				pin.low();           // Off. Should be already off after the pulse
				delay(Math.max((int)this.pulseCycleWidth - pwmVolume, 0));  // Wait for the rest of the cycle
			}
			System.out.println("Stopping PWM");
			// Notify the ones waiting for this thread to end
			synchronized (mainThread) {
				mainThread.notify();
			}
		}, "percent-thread");
		if (this.isPWMing()) {
			this.stopPWM();
		}
		pwmThread.start();
	}

	/**
	 *
	 * @param pulseLength in ms
	 */
	public void emitPWM(float pulseLength) {
		if (pulseLength <= 0) {
			throw new IllegalArgumentException(String.format("Pulse length must be greater than 0, not %f", pulseLength));
		}
		if (pulseLength > this.pulseCycleWidth) {
			throw new IllegalArgumentException(String.format("Pulse length (%f) cannot be greater than cycle width (%f)", pulseLength, this.pulseCycleWidth));
		}
		Thread pwmThread = new Thread(() -> {
			emittingPWM = true;
			long widthInMicroSec = Math.round(pulseLength * 1_000L);
			System.out.println(String.format("Starting PWM (widthInMicroSec %d \u03bcs)", widthInMicroSec));
			while (emittingPWM) {
				pin.pulse(widthInMicroSec, true, TimeUnit.MICROSECONDS); // 'pin' is defined in the superclass GPIOPinAdapter, set second argument to 'true' makes a blocking call
				pin.low();           // Off. Should be already off after the pulse
				float remainderInSeconds = ((this.pulseCycleWidth * 1_000L) - widthInMicroSec) / 1_000_000;
				delay(remainderInSeconds);  // Wait for the rest of the cycle
			}
			System.out.println("Stopping PWM");
			// Notify the ones waiting for this thread to end
			synchronized (mainThread) {
				mainThread.notify();
			}
		}, "pulse-length");
		if (this.isPWMing()) {
			this.stopPWM();
		}
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
		int volume = (int)Math.round((double)percent / (100d / (double) this.pulseCycleWidth));
		if (debug) {
			System.out.println(String.format("percentToVolume: percent: %d => volume: %d (width: %f)", percent, volume, this.pulseCycleWidth));
		}
		return volume;
	}

	public void adjustPWMVolume(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		pwmVolume = percentToVolume(percent);
		if (debug) {
			System.out.println(String.format("\tpercent to volume: %d => %d", percent, pwmVolume));
		}
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
