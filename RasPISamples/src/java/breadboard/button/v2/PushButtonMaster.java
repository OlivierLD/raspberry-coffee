package breadboard.button.v2;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.text.NumberFormat;
import java.util.function.Consumer;

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
 */
public class PushButtonMaster {
	private GpioController gpio = null;
	private GpioPinDigitalInput button = null;

	private String buttonName = "Default";
	private boolean verbose = "true".equals(System.getProperty("button.verbose"));

	private Consumer<Void> onClick;
	private Consumer<Void> onDoubleClick;
	private Consumer<Void> onLongClick;

	public PushButtonMaster(Consumer<Void> onClick,
	                        Consumer<Void> onDoubleClick,
	                        Consumer<Void> onLongClick) {
		this(null, onClick, onDoubleClick, onLongClick);
	}
	public PushButtonMaster(String buttonName,
	                        Consumer<Void> onClick,
	                        Consumer<Void> onDoubleClick,
	                        Consumer<Void> onLongClick) {
		if (buttonName != null) {
			this.buttonName = buttonName;
		}
		try {
			this.gpio = GpioFactory.getInstance();
		} catch (UnsatisfiedLinkError ule) {
			// Absorb. You're not on a Pi.
			System.err.println("Not on a PI? Moving on.");
		}
		this.onClick = onClick;
		this.onDoubleClick = onDoubleClick;
		this.onLongClick = onLongClick;
	}

	public void initCtx() {
		initCtx(RaspiPin.GPIO_01);
	}

	private long pushedTime = 0L;
	private long previousReleaseTime = 0L;
	private long releaseTime = 0L;
	private long betweenClicks = 0L;

	private final static long DOUBLE_CLICK_DELAY = 200L; // Less than 2 10th of sec between clicks
	private final static long LONG_CLICK_DELAY   = 500L; // Long: more than half a second

	public void initCtx(Pin buttonPin) {
		if (this.gpio != null) {
			// provision gpio pin as an output pin and turn it off
			this.button = gpio.provisionDigitalInputPin(buttonPin, PinPullResistance.PULL_DOWN);
			this.button.addListener((GpioPinListenerDigital) event -> {
				if (event.getState().isHigh()) { // Button pressed
					this.pushedTime = System.currentTimeMillis();
					this.betweenClicks = this.pushedTime - this.releaseTime;
					if (verbose) {
						System.out.println(String.format("Since last release of [%s]: %s ms.", this.buttonName, NumberFormat.getInstance().format(this.betweenClicks)));
					}
				} else if (event.getState().isLow()) { // Button released
					this.previousReleaseTime = this.releaseTime;
					this.releaseTime = System.currentTimeMillis();
					if (verbose) {
						System.out.println(String.format("Button [%s] was down for %s ms.", this.buttonName, NumberFormat.getInstance().format(this.releaseTime - this.pushedTime)));
					}
				}
				// Test the click type here, and take action
				if (this.button.isLow()) { // Event on release only
					long now = System.currentTimeMillis();
					if (verbose) {
						System.out.println(String.format("Button [%s]: betweenClicks: %s ms, pushedTime: %s ms, releaseTime: %s, (now - release): %s ",
								this.buttonName,
								NumberFormat.getInstance().format(this.betweenClicks),
								NumberFormat.getInstance().format(this.pushedTime),
								NumberFormat.getInstance().format(this.releaseTime),
								NumberFormat.getInstance().format(now - this.previousReleaseTime)));
					}
					if (this.betweenClicks > 0 && this.betweenClicks < DOUBLE_CLICK_DELAY) {
						this.onDoubleClick.accept(null);
					} else if ((this.releaseTime - this.pushedTime) > LONG_CLICK_DELAY) {
						this.onLongClick.accept(null);
					} else {
						if (now - this.previousReleaseTime > DOUBLE_CLICK_DELAY) { // Not to take the first click of a double click as a single click.
							this.onClick.accept(null);
						}
					}
				}
			});
		}
	}

	public boolean isPushed() {
		return this.button.isHigh();
	}

	public void freeResources() {
		if (this.gpio != null) {
			if (verbose) {
				System.out.println("Freeing resources");
			}
			this.gpio.shutdown();
		}
//	System.exit(0);
	}
}
