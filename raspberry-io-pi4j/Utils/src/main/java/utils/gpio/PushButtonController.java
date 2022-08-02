package utils.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements the nuts and bolts of the push button interactions.
 * <p>
 * Button:
 * - One pin on 3V3
 * - One pin on GPIO XX
 * <p>
 * No need to worry about that in the main class.
 * From the main:
 * - Start with invoking the initCtx method
 * - Finish with invoking the freeResources method
 * <p>
 * Need to manage
 * - Click
 * - Double Click
 * - Long Click
 * - Two-button click (or more..., Shift, Ctrl, etc. You implement it)
 * <p>
 * Note: System.currentTimeMillis returns values like
 * 1,536,096,764,842
 * |   |   |
 * |   |   milliseconds
 * |   seconds
 * seconds * 1000
 * <p>
 * System property(ies):
 * - button.verbose, default false
 * <p>
 * Also works in simulator mode, the simulator calls the manageButtonState method.
 */
public class PushButtonController {

    private final SimpleDateFormat DURATION_FMT = new java.text.SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS Z");

    private GpioController gpio = null;
    private GpioPinDigitalInput button = null;    // Will remain null in simulation mode

    private boolean isHighForSimulation = false;  // Will be used in simulation mode

    private String buttonName = "Button";
    private final boolean verbose = "true".equals(System.getProperty("button.verbose"));

    private Runnable onClick = () -> {};       // Empty, NoOp
    private Runnable onDoubleClick = () -> {}; // Empty, NoOp
    private Runnable onLongClick = () -> {};   // Empty, NoOp

    public PushButtonController() {
    }

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
        if (verbose) {
            System.out.printf("\t >>>> Initial setup for %s, %s%n", buttonName, pin);
        }
        if (buttonName != null) {
            this.buttonName = buttonName;
        }
        if (onClick != null) { // Will keep the default (empty) otherwise.
            this.onClick = onClick;
        }
        if (onDoubleClick != null) { // Will keep the default (empty) otherwise.
            this.onDoubleClick = onDoubleClick;
        }
        if (onLongClick != null) { // Will keep the default (empty) otherwise.
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
    private final static long LONG_CLICK_DELAY = 500L; // Long click: more than half a second

    /*
     * This boolean is here not to take the first click of a double click as a single click.
     * When a click (not a long click) happens, maybeDoubleClick is set to true.
     * Then the thread waits for DOUBLE_CLICK_DELAY ms.
     * If after that, maybeDoubleClick is still true, it was NOT a double click.
     * If maybeDoubleClick is now false, it means it has been reset by a double click. In which case the single-click event is not fired.
     */
    private boolean maybeDoubleClick = false; // To be read as it may be the first click of a double click'.
    private Thread clickManager;

    public enum ButtonStatus {
        NONE,
        HIGH,
        LOW
    }

    public synchronized void manageButtonState(ButtonStatus status) {
        if (status == ButtonStatus.HIGH) { // Button pressed
            // Following one for simulation only
            this.isHighForSimulation = true;

            this.pushedTime = System.currentTimeMillis();
            this.betweenClicks = this.pushedTime - this.releaseTime;
            if (verbose) {
                System.out.printf("\tStatus: HIGH (aka pushed). Since last release of [%s]: %s ms.\n",
                        this.buttonName,
                        NumberFormat.getInstance().format(this.betweenClicks));
            }
        } else if (status == ButtonStatus.LOW) { // Button released
            // Following one for simulation only
            this.isHighForSimulation = false;

            this.previousReleaseTime = this.releaseTime;
            this.releaseTime = System.currentTimeMillis();
            if (verbose) {
                System.out.printf("\tStatus: LOW (aka released). Button [%s] was down for %s ms.\n",
                        this.buttonName,
                        NumberFormat.getInstance().format(this.releaseTime - this.pushedTime));
            }
        }
        // Test the click type here, and take action. Event callbacks on button release only
        if (status == ButtonStatus.LOW) { // Released. Was before: (this.button.isLow()) {
            if (verbose) {
                System.out.printf("\tLOW/Released: Button [%s]:\n\t\tbetweenClicks: %s ms,\n\t\tpushedTime: %s ms (%s),\n\t\treleaseTime: %s (%s),\n\t\tpreviousReleaseTime: %s (%s)\n",
                        this.buttonName,
                        NumberFormat.getInstance().format(this.betweenClicks),
                        NumberFormat.getInstance().format(this.pushedTime),
                        DURATION_FMT.format(new Date(this.pushedTime)),
                        NumberFormat.getInstance().format(this.releaseTime),
                        DURATION_FMT.format(new Date(this.releaseTime)),
                        NumberFormat.getInstance().format(this.previousReleaseTime),
                        DURATION_FMT.format(new Date(this.previousReleaseTime))
                );
            }
            if (verbose) {
                System.out.println("\t>> Before starting clickManager thread:");
                System.out.printf("\t   Thread is %s%n", clickManager == null ? "null" : String.format("not null, and %s.", clickManager.isAlive() ? "alive" : "not alive."));
            }
            // final Thread currentThread = Thread.currentThread();
            final Object lock = new Object();
            if (clickManager != null && clickManager.isAlive()) {
                if (verbose) {
                    System.out.printf("\t>> Killing previous clickManager thread (%s).%n", this.buttonName);
                }
                clickManager.interrupt();
                // Wait for the other click manager to die
                synchronized (lock) {
                    try {
                        System.out.println("\t\tCurrentThread waiting");
                        if (false && clickManager.isAlive()) {
                            lock.wait();
                        }
                        System.out.println("\t\tCurrentThread released");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            clickManager = new Thread(() -> {
                boolean wasInterrupted = false;
                // Double, long or single click?
                if (this.maybeDoubleClick && this.betweenClicks > 0 && this.betweenClicks < DOUBLE_CLICK_DELAY) {
                    this.maybeDoubleClick = false; // Done with 2nd click of a double click.
                    if (verbose) {
                        System.out.printf("\t>>> Detected Double-click. In DoubleClick branch (%s), Setting maybeDoubleClick back to false%n", this.buttonName);
                    }
                    // Execute double-click operation
                    this.onDoubleClick.run();
                } else if ((this.releaseTime - this.pushedTime) > LONG_CLICK_DELAY) {
                    this.maybeDoubleClick = false; // That is not the first of a double-click
                    // Execute long-click operation
                    this.onLongClick.run();
                } else { // Single click
                    this.maybeDoubleClick = true;
                }
                // If single click... It may be the first of a double click
                if (this.maybeDoubleClick) {
                    try {
                        synchronized (this) {
                            this.wait(DOUBLE_CLICK_DELAY); // !! Cannot work in simulation mode if not in a Thread !!
                            if (this.maybeDoubleClick) { // Can have been set to false by a double click
                                if (verbose) {
                                    System.out.printf("\t++++ maybeDoubleClick still true (%s), it was NOT a double-click%n", this.buttonName);
                                }
                                this.maybeDoubleClick = false; // Reset
                                // Execute single-click operation
                                this.onClick.run();
                            } else {
                                if (verbose) {
                                    System.out.printf("\t++++ maybeDoubleClick found false (%s), it WAS a double click (managed before)%n", this.buttonName);
                                }
                            }
                        }
                    } catch (InterruptedException ie) {
                        wasInterrupted = true;
                        if (verbose) {
                            System.out.printf("\t--- Double-click waiter (%s) interrupted (InterruptedException)%n", this.buttonName);
                        }
                        // ie.printStackTrace();
                        // Unlock waiter
                        synchronized (lock) {
                            if (verbose) {
                                System.out.println("\t\tInterrupted thread notifying main thread.");
                            }
                            lock.notify();
                        }
                    }
                }
                if (verbose && !wasInterrupted) {
                    System.out.printf("\tEnd of thread clickManager (%s)%n", this.buttonName);
                }
            });
            // Thread started, on button release.
            if (verbose) {
                System.out.printf("\t>> Starting the clickManager thread (%s)%n", this.buttonName);
            }
            clickManager.start();
        }
    }

    private synchronized void initCtx(Pin buttonPin) {
        if (this.gpio != null) {
            if (verbose) {
                System.out.printf("\t>> InitCtx on %s, %s, provisioning and adding listener.\n", this.buttonName, buttonPin);
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
                if (verbose) {
                    System.out.println("\t+-------------------------------------");
                    System.out.printf("\t| ... In button listener (%s, %s), status is %s (%d listener(s))%n",
                            this.buttonName,
                            buttonPin,
                            buttonStatus,
                            this.button.getListeners().size());
                    System.out.println("\t+-------------------------------------");
                }
                manageButtonState(buttonStatus);
            });
        } else {
            if (verbose) {
                System.out.printf("\tNo GPIO InitCtx on %s, %s\n", this.buttonName, buttonPin);
            }
        }
    }

    // Use for shift-like operations
    public synchronized boolean isPushed() {
        if (this.button != null) {
            return this.button.isHigh();
        } else { // Simulation!
            System.out.printf("  >> Simulating isPushed on %s: %s\n", this.buttonName, this.isHighForSimulation ? "true" : "false");
            return this.isHighForSimulation;
        }
    }

    public synchronized void freeResources() {
        if (this.gpio != null) {
            if (verbose) {
                System.out.printf("\tFreeing resources for [%s]\n", buttonName);
            }
            this.gpio.shutdown();
        }
    }
}
