package tests;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import pwm.PWMPin;

public class PWM3ColorLed {
	public static void main(String... args)
			throws InterruptedException {
		final GpioController gpio = GpioFactory.getInstance();

		final PWMPin pin00 = new PWMPin(RaspiPin.GPIO_00, "Blue", PinState.HIGH);
		final PWMPin pin01 = new PWMPin(RaspiPin.GPIO_01, "Green", PinState.HIGH);
		final PWMPin pin02 = new PWMPin(RaspiPin.GPIO_02, "Red", PinState.HIGH);

		System.out.println("Ready...");

		Runtime.getRuntime().addShutdownHook(new Thread("Hook") {
			public void run() {
				try {
					System.out.println("\nQuitting");
					pin00.stopPWM();
					pin01.stopPWM();
					pin02.stopPWM();

					Thread.sleep(1_000);
					// Last blink
					System.out.println("Bye-bye");
					pin00.low();
					Thread.sleep(500);
					pin00.high();
					Thread.sleep(500);
					pin00.low();

					gpio.shutdown();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		Thread.sleep(1_000);

		pin00.emitPWM(0);
		pin01.emitPWM(0);
		pin02.emitPWM(0);

		for (int vol = 0; vol < 100; vol++) {
			pin00.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}
		for (int vol = 100; vol >= 0; vol--) {
			pin00.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}

		for (int vol = 0; vol < 100; vol++) {
			pin01.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}
		for (int vol = 100; vol >= 0; vol--) {
			pin01.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}

		for (int vol = 0; vol < 100; vol++) {
			pin02.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}
		for (int vol = 100; vol >= 0; vol--) {
			pin02.adjustPWMVolume(vol);
			try {
				Thread.sleep(10);
			} catch (Exception ex) {
			}
		}

		Thread one = new Thread(() -> {
			while (true) {
				final int sleep = (int) (20 * Math.random());
				for (int vol = 0; vol < 100; vol++) {
					pin00.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
				for (int vol = 100; vol >= 0; vol--) {
					pin00.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
			}
		});
		Thread two = new Thread(() -> {
			while (true) {
				final int sleep = (int) (20 * Math.random());
				for (int vol = 0; vol < 100; vol++) {
					pin01.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
				for (int vol = 100; vol >= 0; vol--) {
					pin01.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
			}
		});
		Thread three = new Thread(() -> {
			while (true) {
				final int sleep = (int) (20 * Math.random());
				for (int vol = 0; vol < 100; vol++) {
					pin02.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
				for (int vol = 100; vol >= 0; vol--) {
					pin02.adjustPWMVolume(vol);
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
					}
				}
			}
		});

		one.start();
		two.start();
		three.start();

		Thread me = Thread.currentThread();
		synchronized (me) {
			try {
				me.wait();
			} catch (InterruptedException ie) {
			}
		}
		System.out.println("Tcho!");
	}
}
