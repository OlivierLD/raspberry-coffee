package relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RelayDriver {

	private GpioController gpio = null;

	private static final Pin DEFAULT_SIGNAL_PIN =  RaspiPin.GPIO_00; // BCM 17

	private Pin signalPin = DEFAULT_SIGNAL_PIN;
	private GpioPinDigitalOutput signal = null;
	private boolean simulating = false;
	private Consumer<PinState> simulator = null;
	private Supplier<PinState> relayStatus = null;

	public RelayDriver() {
		this(DEFAULT_SIGNAL_PIN);
	}

	public RelayDriver(Pin _signalPin) {
		if ("true".equals(System.getProperty("gpio.verbose"))) {
			System.out.println(String.format("GPIO> Provisioning pin BCM #%d", PinUtil.findByPin(signalPin).gpio()));
		}
		// Trap stderr output
		PrintStream console = System.err;
		try {
			PrintStream hidden = new PrintStream(new FileOutputStream(new File("hidden.txt")));
			System.setErr(hidden);
			try {
				this.gpio = GpioFactory.getInstance();
				this.signalPin = _signalPin;
				this.signal = gpio.provisionDigitalOutputPin(signalPin, "Relay", PinState.HIGH); // HIGH is off
			} catch (UnsatisfiedLinkError ule) {
				if ("true".equals(System.getProperty("gpio.verbose"))) {
					System.out.println(String.format("GPIO> Will simulate pin BCM #%d (for %s)", PinUtil.findByPin(signalPin).gpio(), this.getClass().getName()));
				}
				this.simulating = true;
			}
			System.setErr(console);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public boolean isSimulating() {
		return this.simulating;
	}

	public void setSimulator(Consumer<PinState> simulator, Supplier<PinState> relayStatus) {
		this.simulator = simulator;
		this.relayStatus = relayStatus;
	}

	public void on() {
		if (!this.simulating) {
			this.signal.low();
		} else {
			this.simulator.accept(PinState.LOW);
		}
	}

	public void off() {
		if (!this.simulating) {
			this.signal.high();
		} else {
			this.simulator.accept(PinState.HIGH);
		}
	}

	public PinState getState() {
		PinState state = null;
		if (!this.simulating) {
			state = this.signal.getState();
			return state;
		} else {
			return relayStatus.get();
		}
	}

	public void shutdownGPIO() {
		if (this.gpio != null && !gpio.isShutdown()) {
			if ("true".equals(System.getProperty("gpio.verbose"))) {
				System.out.println(String.format("GPIO> Shutting down GPIO from %s", this.getClass().getName()));
			}
			gpio.shutdown();
		} else {
			if ("true".equals(System.getProperty("gpio.verbose"))) {
				System.out.println(String.format("GPIO> Shutting down GPIO from %s: was down already", this.getClass().getName()));
			}
		}
	}
}
