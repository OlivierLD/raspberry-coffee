package pwm.simulator;

import static utils.TimeUtil.delay;

/**
 * Pulse Width Modulation soft implementation
 * http://raspberrypi.lediouris.net/PWM/readme.html
 */
public class PWMSoftPin {
	// 30 seems to be the maximum value. You can really see the led blinking beyond that.
	private int cycleWidth = 30; // in milliseconds

	private final Thread mainThread;
	private final boolean debug = "true".equals(System.getProperty("pwm.debug", "false"));

	private String name;
	private SoftPin pin;

	public PWMSoftPin(SoftPin pin, String name, int cycleWidth) {
		this.pin = pin;
		this.name = name;
		this.cycleWidth = cycleWidth;
		this.mainThread = Thread.currentThread();
	}

	private boolean emittingPWM = false;
	private int pwmVolume = 0; // [0..CYCLE_WIDTH], percent / (100 / CYCLE_WIDTH);

	public void emitPWM(final int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent MUST be in [0, 100], not [" + percent + "]");
		}
		if (debug) {
			System.out.println("Volume:" + percentToVolume(percent) + "/" + cycleWidth);
		}
		Thread pwmThread = new Thread(() -> {
			emittingPWM = true;
			pwmVolume = percentToVolume(percent);
			while (emittingPWM) {
				if (pwmVolume > 0) {
		//		System.out.println(String.format("%d%% -> Volume: %d", percent, pwmVolume));
		//		pin.pulse(pwmVolume, true);
					pin.high();
					delay(pwmVolume);  // Wait for the rest of the cycle
				}
				pin.low();
				delay(cycleWidth - pwmVolume);  // Wait for the rest of the cycle
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
		return percent / (100 / cycleWidth);
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

	public static void main(String... args) {
		SoftPin pin = new SoftPin() {
			public void high() {
				System.out.println("Going high");
			}
			public void low() {
				System.out.println("Going low");
			}
		};

		PWMSoftPin softPin = new PWMSoftPin(pin, "SoftLED", 30);

		softPin.emitPWM(50);
		delay(5_000L);
		softPin.stopPWM();

	}
}
