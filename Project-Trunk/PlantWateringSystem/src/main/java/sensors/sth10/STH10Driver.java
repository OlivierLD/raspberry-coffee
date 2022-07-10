package sensors.sth10;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import utils.PinUtil;
import utils.StringUtils;
import utils.TimeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * WARNING:
 *  Only for values: 3.5V, High resolution, no heater, otp_no_reload off
 *
 *  Adapted from / inspired by the python code at https://github.com/drohm/pi-sht1x
 *  Datasheet STH1x: https://cdn-shop.adafruit.com/datasheets/Sensirion_Humidity_SHT1x_Datasheet_V5.pdf
 */
public class STH10Driver {

	private static boolean DEBUG = "true".equals(System.getProperty("sth.debug", "false"));
	private static boolean DEBUG2 = false; // Freak... ;)

	private final static String TEMPERATURE_CMD = "Temperature";
	private final static String HUMIDITY_CMD = "Humidity";
	private final static String READ_STATUS_REGISTER_CMD = "ReadStatusRegister";
	private final static String WRITE_STATUS_REGISTER_CMD = "WriteStatusRegister";
	private final static String SOFT_RESET_CMD = "SoftReset";
	private final static String NO_OP_CMD = "NoOp";

	private final static Map<String, Byte> COMMANDS = new HashMap<>();
	static {
		COMMANDS.put(TEMPERATURE_CMD,           (byte)0b00000011);
		COMMANDS.put(HUMIDITY_CMD,              (byte)0b00000101);
		COMMANDS.put(READ_STATUS_REGISTER_CMD,  (byte)0b00000111);
		COMMANDS.put(WRITE_STATUS_REGISTER_CMD, (byte)0b00000110);
		COMMANDS.put(SOFT_RESET_CMD,            (byte)0b00011110);
		COMMANDS.put(NO_OP_CMD,                 (byte)0b00000000);
	}

	private final static double
			D2_SO_C = 0.01,
			D1_VDD_C = -39.7,
			C1_SO = -2.0468,
			C2_SO = 0.0367,
			C3_SO = -0.0000015955,
			T1_S0 = 0.01,
			T2_SO = 0.00008;

	private GpioController gpio = null;
	private boolean simulating = false;

	private static final Pin DEFAULT_DATA_PIN =  RaspiPin.GPIO_01; // BCM 18
	private static final Pin DEFAULT_CLOCK_PIN = RaspiPin.GPIO_04; // BCM 23

	private Pin dataPin;
	private Pin clockPin;

	private GpioPinDigitalMultipurpose data;
	private GpioPinDigitalMultipurpose clock;

	private byte statusRegister = 0x0;

	private Supplier<Double> temperatureSimulator = null;
	private Supplier<Double> humiditySimulator = null;

	public STH10Driver() {
		this(DEFAULT_DATA_PIN, DEFAULT_CLOCK_PIN);
	}

	public STH10Driver(Pin _dataPin, Pin _clockPin) {
		this.dataPin = _dataPin;
		this.clockPin = _clockPin;
		if ("true".equals(System.getProperty("gpio.verbose"))) {
			System.out.println(String.format("GPIO> Opening GPIO (%s)", this.getClass().getName()));
		}
		// Trap stderr output
		PrintStream console = System.err;
		try {
			PrintStream hidden = new PrintStream(new FileOutputStream(new File("hidden.txt")));
			System.setErr(hidden);
			try {
				this.gpio = GpioFactory.getInstance();
			} catch (UnsatisfiedLinkError ule) {
				System.out.println(ule.toString());
				// Simulating
				if ("true".equals(System.getProperty("gpio.verbose"))) {
					System.out.println(String.format("GPIO> Will simulate (for %s)", this.getClass().getName()));
				}
				this.simulating = true;
			}
			System.setErr(console);
		} catch (IOException ioe) {
			System.err.println(String.format("At %s :", new Date().toString()));
			ioe.printStackTrace();
		}

		if (this.gpio != null) {
			int nbTry = 0;
			boolean ok = false;
			Throwable lastError = null;
			while (!ok && nbTry < 5) {
				try {
					this.data = this.gpio.provisionDigitalMultipurposePin(this.dataPin, PinMode.DIGITAL_OUTPUT);
					this.clock = this.gpio.provisionDigitalMultipurposePin(this.clockPin, PinMode.DIGITAL_OUTPUT);
					this.data.setShutdownOptions(true, PinState.LOW);
					this.clock.setShutdownOptions(true, PinState.LOW);
					if ("true".equals(System.getProperty("gpio.verbose"))) {
						System.out.println(String.format("GPIO> Pins BCM #%d and #%d provisioned.",
								PinUtil.findByPin(this.dataPin).gpio(),
								PinUtil.findByPin(this.clockPin).gpio()));
					}
					ok = true;
				} catch (Exception ex) {
					lastError = ex;
					nbTry++;
					TimeUtil.delay(1_000L);
				}
			}
			if (!ok) { // Could not initialize :( Barf.
				System.err.println(String.format("At %s :", new Date().toString()));
				throw new RuntimeException("Could not initialize after 5 attempts.", lastError);
			}
		}
		//
		this.init();
	}

	public boolean isSimulating() {
		return this.simulating;
	}

	public void setSimulators(Supplier<Double> tSimulator, Supplier<Double> hSimulator) {
		this.temperatureSimulator = tSimulator;
		this.humiditySimulator = hSimulator;
	}

	private String pinDisplay(GpioPinDigital pin) {
		return pinDisplay(pin, null);
	}
	private String pinDisplay(GpioPinDigital pin, String defaultDisplay) {
		if (pin != null) {
			return (String.format("%d [%s]", PinUtil.findByPin(pin.getPin()).gpio(), pin.equals(this.data) ? "DATA" : (pin.equals(this.clock) ? "CLOCK" : "!UNKNOWN!")));
		} else {
			return defaultDisplay;
		}
	}

	private void resetConnection() {
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_OUTPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			this.flipPin(this.data, PinState.HIGH);
			for (int i = 0; i < 10; i++) {
				this.flipPin(this.clock, PinState.HIGH);
				this.flipPin(this.clock, PinState.LOW);
			}
		}
	}

	public void softReset() {
		byte cmd = COMMANDS.get(SOFT_RESET_CMD);
		this.sendCommandSHT(cmd, false);
		TimeUtil.delay(15L, 0); // 15 ms
		this.statusRegister = 0x0;
	}

	private void writeStatusRegister(byte mask) {
		if (DEBUG) {
			System.out.println(String.format(">> writeStatusRegister, mask %d >>", mask));
		}
		byte cmd = COMMANDS.get(WRITE_STATUS_REGISTER_CMD);
		if (DEBUG) {
			System.out.println(String.format(">> writeStatusRegister, sendCommandSHT, cmd %d", cmd));
		}
		this.sendCommandSHT(cmd, false);
		this.sendByte(mask);
		if (DEBUG) {
			System.out.println(String.format(">> writeStatusRegister, getAck, cmd %d", cmd));
		}
		this.getAck(WRITE_STATUS_REGISTER_CMD);
		this.statusRegister = mask;
		if (DEBUG) {
			System.out.println(String.format("<< writeStatusRegister, mask %d <<", mask));
		}
	}

	private void resetStatusRegister() {
		this.writeStatusRegister(COMMANDS.get(NO_OP_CMD));
	}

	private void flipPin(GpioPinDigitalMultipurpose pin, PinState state) {
		if (DEBUG) {
			System.out.print(String.format(">> flipPin %s to %s", pinDisplay(pin, "[simulated]"), state.toString()));
		}
		if (!this.simulating) {
			if (state == PinState.HIGH) {
				pin.high();
			} else {
				pin.low();
			}
			if (pin.equals(this.clock)) {
				if (DEBUG) {
					System.out.print("   >> Flipping CLK, delaying");
				}
				TimeUtil.delay(0L, 100); // 0.1 * 1E-6 sec. 100 * 1E-9
			}
			if (DEBUG) {
				System.out.println(String.format("\tpin is now %s", pin.getState() == PinState.HIGH ? "HIGH" : "LOW"));
			}
		} else {
			if (DEBUG) {
				System.out.println();
			}
		}
	}

	private void startTx() {

		if (DEBUG) {
			System.out.println(String.format(">> startTx >>"));
		}
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_OUTPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			this.flipPin(this.data, PinState.HIGH);
			this.flipPin(this.clock, PinState.HIGH);

			this.flipPin(this.data, PinState.LOW);
			this.flipPin(this.clock, PinState.LOW);

			this.flipPin(this.clock, PinState.HIGH); // Clock first
			this.flipPin(this.data, PinState.HIGH);  // Data 2nd

			this.flipPin(this.clock, PinState.LOW);
		}
		if (DEBUG) {
			System.out.println(String.format("<< startTx <<"));
		}
	}

	private void endTx() {
		if (DEBUG) {
			System.out.println(String.format(">> endTx >>"));
		}
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_OUTPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			this.flipPin(this.data, PinState.HIGH);
			this.flipPin(this.clock, PinState.HIGH);

			this.flipPin(this.clock, PinState.LOW);
		}
		if (DEBUG) {
			System.out.println(String.format("<< endTx <<"));
		}
	}

	private void sendByte(byte data) {
		if (DEBUG) {
			System.out.println(String.format(">> sendByte %d [%s]", data, StringUtils.lpad(Integer.toBinaryString(data), 8,"0")));
		}
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_OUTPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);
		}

		for (int i=0; i<8; i++) {
			int bit = data & (1 << (7 - i));
			if (DEBUG) {
				System.out.println(String.format("\t\tBit #%d, %d, %s", (i + 1), bit, (bit == 0 ? "LOW" : "HIGH")));
			}
			this.flipPin(this.data, (bit == 0 ? PinState.LOW : PinState.HIGH));

			this.flipPin(this.clock, PinState.HIGH);
			this.flipPin(this.clock, PinState.LOW);
		}
		if (DEBUG) {
			System.out.println(String.format("<< sendByte << "));
		}
	}

	private byte getByte() {
		if (DEBUG) {
			System.out.println(String.format(">> getByte >>"));
		}
		byte b = 0x0;

		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_INPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			for (int i = 0; i < 8; i++) {
				this.flipPin(this.clock, PinState.HIGH);
				PinState state = this.data.getState();
				if (state == PinState.HIGH) {
					b |= (1 << (7 - i));
				}
				if (DEBUG || DEBUG2) {
					System.out.println(String.format("\tgetting byte %d, byte is %s", i, StringUtils.lpad(Integer.toBinaryString(b & 0x00FF), 8, "0")));
				}
				this.flipPin(this.clock, PinState.LOW);
			}
		}
		if (DEBUG || DEBUG2) {
			System.out.println(String.format("<< getByte %d 0b%s <<", (b & 0x00FF), StringUtils.lpad(Integer.toBinaryString(b & 0x00FF), 8, "0")));
		}
		return (byte)(b & 0x00FF);
	}

	private void getAck(String commandName) {
		if (DEBUG) {
			System.out.println(String.format(">> getAck, command %s >>", commandName));
			System.out.println(String.format(">> %s INPUT, %s OUTPUT", pinDisplay(this.data, "DATA"), pinDisplay(this.clock, "CLOCK")));
		}
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_INPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			if (DEBUG) {
				System.out.println(String.format(">> getAck, flipping %s to HIGH", pinDisplay(this.clock, "CLOCK")));
			}
			this.flipPin(this.clock, PinState.HIGH);
			if (DEBUG) {
				System.out.println(String.format("\t>> getAck, >>> getState %s = %s", pinDisplay(this.clock, "CLOCK"), this.clock != null ? this.clock.getState().toString() : "[simulated]"));
			}
			PinState state = this.data.getState();
			if (DEBUG) {
				System.out.println(String.format(">> getAck, getState %s = %s", pinDisplay(this.data, "DATA"), state.toString()));
			}
			if (state == PinState.HIGH) {
				throw new RuntimeException(String.format("SHTx failed to properly receive ack after command [%s, 0b%8s]", commandName, StringUtils.lpad(Integer.toBinaryString(COMMANDS.get(commandName)), 8, "0")));
			}
			if (DEBUG) {
				System.out.println(String.format(">> getAck, flipping %s to LOW", pinDisplay(this.clock, "CLOCK")));
			}
			this.flipPin(this.clock, PinState.LOW);
		}
		if (DEBUG) {
			System.out.println(String.format("<< getAck <<"));
		}
	}

	private void sendAck() {
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_OUTPUT);
			this.clock.setMode(PinMode.DIGITAL_OUTPUT);

			this.flipPin(this.data, PinState.HIGH);
			this.flipPin(this.data, PinState.LOW);
			this.flipPin(this.clock, PinState.HIGH);
			this.flipPin(this.clock, PinState.LOW);
		}
	}

	private final static int NB_TRIES = 35;

	public void waitForResult() {
		PinState state = PinState.HIGH;
		if (!this.simulating) {
			this.data.setMode(PinMode.DIGITAL_INPUT);
			for (int t = 0; t < NB_TRIES; t++) {
				TimeUtil.delay(10L, 0);
				state = this.data.getState();
				if (state.getValue() == PinState.LOW.getValue()) {
					if (DEBUG) {
						System.out.println(String.format(">> waitForResult completed iteration %d", t));
					}
					break;
				} else {
					if (DEBUG) {
						System.out.println(String.format(">> waitForResult still waiting - iteration %d", t));
					}
				}
			}
			if (state.getValue() == PinState.HIGH.getValue()) {
				throw new RuntimeException("Sensor has not completed measurement within allocated time.");
			}
		}
	}

	private void init() {
		if (DEBUG) {
			System.out.println(">> Init >>");
		}
		this.resetConnection();
		byte mask = 0x0;
		// Other options may go here

		if (DEBUG) {
			System.out.println(String.format(">> Init, writeStatusRegister, with mask %s >>", StringUtils.lpad(Integer.toBinaryString(mask), 8, "0")));
		}
		this.writeStatusRegister(mask);
		if (DEBUG) {
			System.out.println("<< Init <<");
		}
	}

	public double readTemperature() {
		byte cmd = COMMANDS.get(TEMPERATURE_CMD);
		this.sendCommandSHT(cmd);
		int value = 0;
		if (!this.simulating) {
			value = this.readMeasurement();
			if (DEBUG2 || DEBUG) {
				System.out.println(String.format(">> Read temperature raw value %d, 0x%s", value, StringUtils.lpad(Integer.toBinaryString(value), 16, "0")));
			}
			return (value * D2_SO_C) + (D1_VDD_C); // Celcius
		} else {
			return this.temperatureSimulator.get();
		}
	}

	public double readHumidity() {
		return readHumidity(null);
	}

	public double readHumidity(Double temp) {
		double t;
		if (temp == null) {
			t = this.readTemperature();
		} else {
			t = temp;
		}
		byte cmd = COMMANDS.get(HUMIDITY_CMD);
		this.sendCommandSHT(cmd);
		int value = 0;
		if (!this.simulating) {
			value = this.readMeasurement();
			if (DEBUG2 || DEBUG) {
				System.out.println(String.format(">> Read humidity raw value %d, 0x%s", value, StringUtils.lpad(Integer.toBinaryString(value), 16, "0")));
			}
			double linearHumidity = C1_SO + (C2_SO * value) + (C3_SO * Math.pow(value, 2));
			double humidity = ((t - 25) * (T1_S0 + (T2_SO * value)) + linearHumidity); // %
			return humidity;
		} else {
			return this.humiditySimulator.get();
		}
	}
	/**
	 *
	 * @return a 16 bit word.
	 */
	private int readMeasurement() {
		int value = 0;

		// MSB
		byte msb = this.getByte();
		value = (msb << 8);

		if (DEBUG || DEBUG2) {
			System.out.println(String.format("\t After MSB: %s", StringUtils.lpad(Integer.toBinaryString(value), 16, "0")));
		}
		this.sendAck();
		// LSB
		byte lsb = this.getByte();
		value |= (lsb & 0xFF);

		if (DEBUG || DEBUG2) {
			System.out.println(String.format("\t After LSB: %s", StringUtils.lpad(Integer.toBinaryString(value), 16, "0")));
		}
		this.endTx();

		return (value);
	}

	private void sendCommandSHT(byte command) {
		sendCommandSHT(command, true);
	}
	private void sendCommandSHT(byte command, boolean measurement) {
		if (DEBUG) {
			System.out.println(String.format(">> sendCommandSHT %d >>", command));
		}
		if (!COMMANDS.containsValue(command)) {
			throw new RuntimeException(String.format("Command 0b%8s not found.", StringUtils.lpad(Integer.toBinaryString(command), 8, "0")));
		}
		String commandName = COMMANDS.keySet()
				.stream()
				.filter(entry -> command == COMMANDS.get(entry))
				.findFirst()
				.get();

		this.startTx();
		this.sendByte(command);
		this.getAck(commandName);

		if (measurement) {
			if (DEBUG) {
				System.out.println(String.format(">> sendCommandSHT with measurement, %d", command));
			}
			PinState state = (!this.simulating ? this.data.getState() : PinState.HIGH); // TODO Simulate?
			// SHT1x is taking measurement.
			if (state.getValue() == PinState.LOW.getValue()) {
				throw new RuntimeException("SHT1x is not in the proper measurement state. DATA line is LOW.");
			}
			this.waitForResult();
		}
		if (DEBUG) {
			System.out.println("<< sendCommandSHT <<");
		}
	}

	public void shutdownGPIO() {
		if (this.gpio != null && !this.gpio.isShutdown()) {
			if ("true".equals(System.getProperty("gpio.verbose"))) {
				System.out.println(String.format("GPIO> Shutting down GPIO from %s", this.getClass().getName()));
			}
			this.gpio.shutdown();
		} else {
			if ("true".equals(System.getProperty("gpio.verbose"))) {
				System.out.println(String.format("GPIO> Shutting down GPIO from %s: was down already", this.getClass().getName()));
			}
		}
	}

}
