package navserver.button;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.text.NumberFormat;

/**
 * Implements the nuts and bolts of the push button interaction.
 * No need to worry about that in the main class.
 * From the main:
 * Invoke the initCtx method
 * Invoke the freeResources method
 *
 * Need to manage
 * - Click
 * - Double Click
 * - Long Click
 * - Two-button click (or more..., Shft, Ctrl, etc)
 *
 * Note: System.currentTimeMillis returns values like
 *   1,536,096,764,842
 *               |
 *               seconds
 *
 * System properties:
 * - button.verbose, default false
 */
public class PushButtonController {
	private GpioController gpio = null;
	private GpioPinDigitalInput button = null;

	private boolean isHighForSimulation = false;

	private String buttonName = "Button";
	private boolean verbose = "true".equals(System.getProperty("button.verbose"));

	private Runnable onClick = () -> {};       // Empty, NoOp
	private Runnable onDoubleClick = () -> {}; // Empty, NoOp
	private Runnable onLongClick = () -> {};   // Empty, NoOp

	public PushButtonController() {}

	public PushButtonController(Pin pin) {
		this(null, pin);
	}

	public PushButtonController(String buttonName,
	                            Pin pin) {
		this(buttonName, pin, null, null, null);
	}

	public PushButtonController(Pin pin,
	                            Runnable onClick,
	                            Runnable onDoubleClick,
	                            Runnable onLongClick) {
		this(null, pin, onClick, onDoubleClick, onLongClick);
	}
	public PushButtonController(String buttonName,
	                            Pin pin,
	                            Runnable onClick,
	                            Runnable onDoubleClick,
	                            Runnable onLongClick) {
		this.update(buttonName, pin, onClick, onDoubleClick, onLongClick);
	}

	public void update(Pin pin) {
		this.update(null, pin);
	}
	public void update(Pin pin,
	                   Runnable onClick,
	                   Runnable onDoubleClick,
	                   Runnable onLongClick) {
		this.update(null, pin, onClick, onDoubleClick, onLongClick);
	}
	public void update(String buttonName,
	                   Pin pin) {
		this.update(buttonName, pin, null, null, null);
	}
	public void update(String buttonName,
	                   Pin pin,
	                   Runnable onClick,
	                   Runnable onDoubleClick,
	                   Runnable onLongClick) {
		if (buttonName != null) {
			this.buttonName = buttonName;
		}
		if (onClick != null) {
			this.onClick = onClick;
		}
		if (onDoubleClick != null) {
			this.onDoubleClick = onDoubleClick;
		}
		if (onLongClick != null) {
			this.onLongClick = onLongClick;
		}

		try {
			this.gpio = GpioFactory.getInstance();
		} catch (UnsatisfiedLinkError ule) {
			// Absorb. You're not on a Pi.
			System.err.println("Not on a PI? Moving on.");
		}
		initCtx(pin);
	}

	private long pushedTime = 0L;
	private long previousReleaseTime = 0L;
	private long releaseTime = 0L;
	private long betweenClicks = 0L;

	private final static long DOUBLE_CLICK_DELAY = 200L; // Less than 2 10th of sec between clicks
	private final static long LONG_CLICK_DELAY   = 500L; // Long click: more than half a second

	/*
	 * This boolean is here not to take the first click of a double click as a single click.
	 * When a click (not a long click) happens, maybeDoubleClick is set to true.
	 * Then the thread waits for DOUBLE_CLICK_DELAY ms.
	 * If after that, maybeDoubleClick is still true, it was NOT a double click.
	 * If maybeDoubleClick is now false, it means it has been reset by a double-click. In which case the single-click event is not fired.
	 */
	private boolean maybeDoubleClick = false; // To be read as 'may be the first click of a double-click'.

	public enum ButtonStatus {
		NONE,
		HIGH,
		LOW
	};

	public void manageButtonState(ButtonStatus status) {
		if (status == ButtonStatus.HIGH) { // Button pressed
			// Following one for simulation only
			this.isHighForSimulation = true;

			this.pushedTime = System.currentTimeMillis();
			this.betweenClicks = this.pushedTime - this.releaseTime;
			if (verbose) {
				System.out.println(String.format("Since last release of [%s]: %s ms.", this.buttonName, NumberFormat.getInstance().format(this.betweenClicks)));
			}
		} else if (status == ButtonStatus.LOW) { // Button released
			// Following one for simulation only
			this.isHighForSimulation = false;

			this.previousReleaseTime = this.releaseTime;
			this.releaseTime = System.currentTimeMillis();
			if (verbose) {
				System.out.println(String.format("Button [%s] was down for %s ms.", this.buttonName, NumberFormat.getInstance().format(this.releaseTime - this.pushedTime)));
			}
		}
		// Test the click type here, and take action. Event callbacks on button release only
		// TODO Make sure this is right...
		if (status == ButtonStatus.LOW) { // Was before: (this.button.isLow()) {
			if (verbose) {
				System.out.println(
						String.format("Button [%s]: betweenClicks: %s ms, pushedTime: %s ms, releaseTime: %s, previousReleaseTime: %s ",
								this.buttonName,
								NumberFormat.getInstance().format(this.betweenClicks),
								NumberFormat.getInstance().format(this.pushedTime),
								NumberFormat.getInstance().format(this.releaseTime),
								NumberFormat.getInstance().format(this.previousReleaseTime)));
			}
			Thread clickManager = new Thread(() -> {
				// Double, long or single click?
				if (this.betweenClicks > 0 && this.betweenClicks < DOUBLE_CLICK_DELAY) {
					this.maybeDoubleClick = false; // Done with 2nd click of a double-click.
					if (verbose) {
						System.out.println("++++ Setting maybeDoubleClick to false");
					}
					this.onDoubleClick.run();
				} else if ((this.releaseTime - this.pushedTime) > LONG_CLICK_DELAY) {
					this.onLongClick.run();
				} else { // Single click
					this.maybeDoubleClick = true;
				}
				// If single-click... May be the first of a double-click
				if (this.maybeDoubleClick) {
					try {
						Thread.sleep(DOUBLE_CLICK_DELAY); // Cannot work in simulation mode if not in a Thread
						if (this.maybeDoubleClick) { // Can have been set to false by a double-click
							if (verbose) {
								System.out.println("++++ maybeDoubleClick still true");
							}
							this.maybeDoubleClick = false; // Reset
							this.onClick.run();
						} else {
							if (verbose) {
								System.out.println("++++ maybeDoubleClick found false, it WAS a double click");
							}
						}
					} catch (InterruptedException ie) {
						// Absorb
					}
				}
			});
			clickManager.start();
		}
	}

	private void initCtx(Pin buttonPin) {
		if (this.gpio != null) {
			if (verbose) {
				System.out.println(String.format(">> InitCtx on %s, %s", this.buttonName, buttonPin));
			}
			// provision gpio pin as an output pin and turn it off/pushed down
			this.button = gpio.provisionDigitalInputPin(buttonPin, PinPullResistance.PULL_DOWN);
			this.button.addListener((GpioPinListenerDigital) event -> {
				ButtonStatus buttonStatus = ButtonStatus.NONE;
				if (event.getState().isHigh()) {       // Pressed
					buttonStatus = ButtonStatus.HIGH;
				} else if (event.getState().isLow()) { // Released
					buttonStatus = ButtonStatus.LOW;
				}
				manageButtonState(buttonStatus);
			});
		} else {
			if (verbose) {
				System.out.println(String.format("No GPIO InitCtx on %s, %s", this.buttonName, buttonPin));
			}
		}
	}

	// Use for shift-like operations
	public boolean isPushed() {
		if (this.button != null) {
			return this.button.isHigh();
		} else { // Simulation!
			System.out.println(String.format("  >> Simulating isPushed on %s: %s", this.buttonName, this.isHighForSimulation ? "true":"false"));
			return this.isHighForSimulation;
		}
	}

	public void freeResources() {
		if (this.gpio != null) {
			if (verbose) {
				System.out.println("Freeing resources");
			}
			this.gpio.shutdown();
		}
	}
}
