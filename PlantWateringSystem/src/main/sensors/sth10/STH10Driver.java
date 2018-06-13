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

import java.util.HashMap;
import java.util.Map;

public class STH10Driver {

	private static boolean DEBUG = "true".equals(System.getProperty("sth.debug", "false"));

	private final static String TEMPERATURE_CMD = "Temperature";
	private final static String HUMIDITY_CMD = "Humidity";
	private final static String READ_STATUS_REGISTER_CMD = "ReadStatusRegister";
	private final static String WRITE_STATUS_REGISTER_CMD = "WriteStatusRegister";
	private final static String SOFT_RESET_CMD = "SoftReset";
	private final static String NO_OP_CMD = "NoOp";

	private final static Map<String, Byte> COMMANDS = new HashMap<>();
	static {
		COMMANDS.put(TEMPERATURE_CMD, (byte)0b00000011);
		COMMANDS.put(HUMIDITY_CMD, (byte)0b00000101);
		COMMANDS.put(READ_STATUS_REGISTER_CMD, (byte)0b00000111);
		COMMANDS.put(WRITE_STATUS_REGISTER_CMD, (byte)0b00000110);
		COMMANDS.put(SOFT_RESET_CMD, (byte)0b00011110);
		COMMANDS.put(NO_OP_CMD, (byte)0b00000000);
	}

	final GpioController gpio = GpioFactory.getInstance();

	private static final Pin DEFAULT_DATA_PIN =  RaspiPin.GPIO_01; // BCM 18
	private static final Pin DEFAULT_CLOCK_PIN = RaspiPin.GPIO_04; // BCM 23

	private Pin dataPin;
	private Pin clockPin;

	private GpioPinDigitalMultipurpose data;
	private GpioPinDigitalMultipurpose clock;

	private byte statusRegister = 0x0;

	public STH10Driver() {
		this(DEFAULT_DATA_PIN, DEFAULT_CLOCK_PIN);
	}

	public STH10Driver(Pin _dataPin, Pin _clockPin) {
		this.dataPin = _dataPin;
		this.clockPin = _clockPin;

		this.data = gpio.provisionDigitalMultipurposePin(this.dataPin, PinMode.DIGITAL_OUTPUT);
		this.clock = gpio.provisionDigitalMultipurposePin(this.clockPin, PinMode.DIGITAL_OUTPUT);
		//
		this.init();
	}

	private String pinDisplay(GpioPinDigital pin) {
		return (String.format("%d [%s]", PinUtil.findByPin(pin.getPin()).gpio(), pin.equals(this.data) ? "DATA" : (pin.equals(this.clock) ? "CLOCK" : "!UNKNOWN!")));
	}

	private void resetConnection() {
		this.data.setMode(PinMode.DIGITAL_OUTPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		this.flipPin(this.data, PinState.HIGH);
		for (int i=0; i<10; i++) {
			this.flipPin(this.clock, PinState.HIGH);
			this.flipPin(this.clock, PinState.LOW);
		}
	}

	public void softReset() {
		byte cmd = COMMANDS.get(SOFT_RESET_CMD);
		this.sendCommandSHT(cmd, false);
		delay(15L, 0);
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
		//
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
			System.out.print(String.format(">> flipPin %s to %s", pinDisplay(pin), state.toString()));
		}
		if (state == PinState.HIGH) {
			pin.high();
		} else {
			pin.low();
		}
		if (pin.equals(this.clock)) {
			if (DEBUG) {
				System.out.print("   >> Flipping CLK, delaying");
			}
			delay(0L, 100); // 0.1 * 1E-6 sec. 100 * 1E-9
		}
		if (DEBUG) {
			System.out.println(String.format("\tpin is now %s", (pin.getState() == PinState.HIGH ? "HIGH" : "LOW")));
		}
	}

	private void startTx() {

		if (DEBUG) {
			System.out.println(String.format(">> startTx >>"));
		}
		this.data.setMode(PinMode.DIGITAL_OUTPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		this.flipPin(this.data, PinState.HIGH);
		this.flipPin(this.clock, PinState.HIGH);

		this.flipPin(this.data, PinState.LOW);
		this.flipPin(this.clock, PinState.LOW);

		this.flipPin(this.clock, PinState.HIGH); // Clock first
		this.flipPin(this.data, PinState.HIGH);  // Data 2nd

		this.flipPin(this.clock, PinState.LOW);
		if (DEBUG) {
			System.out.println(String.format("<< startTx <<"));
		}
	}

	private void endTx() {
		if (DEBUG) {
			System.out.println(String.format(">> endTx >>"));
		}
		this.data.setMode(PinMode.DIGITAL_OUTPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		this.flipPin(this.data, PinState.HIGH);
		this.flipPin(this.clock, PinState.HIGH);

		this.flipPin(this.clock, PinState.LOW);
		if (DEBUG) {
			System.out.println(String.format("<< endTx <<"));
		}
	}

	private void sendByte(byte data) {
		if (DEBUG) {
			System.out.println(String.format(">> sendByte %d [%s]", data, StringUtils.lpad(Integer.toBinaryString(data), 8,"0")));
		}
		this.data.setMode(PinMode.DIGITAL_OUTPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

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

		this.data.setMode(PinMode.DIGITAL_INPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		for (int i=0; i<8; i++) {
			this.flipPin(this.clock, PinState.HIGH);
			PinState state = this.data.getState();
			if (state == PinState.HIGH) {
				b |= (1 << (7 - i));
			}
			if (DEBUG) {
				System.out.println(String.format("\tgetting byte %d, byte is %s", i, StringUtils.lpad(Integer.toBinaryString(b), 8,"0")));
			}
			this.flipPin(this.clock, PinState.LOW);
		}
		if (DEBUG) {
			System.out.println(String.format("<< getByte %d <<", (b & 0xFF)));
		}
		return (byte)(b & 0xFF);
	}

	private void getAck(String commandName) {
		if (DEBUG) {
			System.out.println(String.format(">> getAck, command %s >>", commandName));
			System.out.println(String.format(">> %s INPUT %s OUTPUT", pinDisplay(this.data), pinDisplay(this.clock)));
		}
		this.data.setMode(PinMode.DIGITAL_INPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		if (DEBUG) {
			System.out.println(String.format(">> getAck, flipping %s to HIGH", pinDisplay(this.clock)));
		}
		this.flipPin(this.clock, PinState.HIGH);
		if (DEBUG) {
			System.out.println(String.format("\t>> getAck, >>> getState %s = %s", pinDisplay(this.clock), this.clock.getState().toString()));
		}
//	delay(100L, 0);
		PinState state = this.data.getState();
		if (DEBUG) {
			System.out.println(String.format(">> getAck, getState %s = %s", pinDisplay(this.data), state.toString()));
		}
		if (state == PinState.HIGH) {
			throw new RuntimeException(String.format("SHTx failed to properly receive ack after command [%s, 0b%8s]", commandName, StringUtils.lpad(Integer.toBinaryString(COMMANDS.get(commandName)), 8,"0")));
		}
		if (DEBUG) {
			System.out.println(String.format(">> getAck, flipping %s to LOW", pinDisplay(this.clock)));
		}
		this.flipPin(this.clock, PinState.LOW);
		if (DEBUG) {
			System.out.println(String.format("<< getAck <<"));
		}
	}

	private void sendAck() {
		this.data.setMode(PinMode.DIGITAL_OUTPUT);
		this.clock.setMode(PinMode.DIGITAL_OUTPUT);
//  this.clock.low(); // ??

		this.flipPin(this.data, PinState.HIGH);
		this.flipPin(this.data, PinState.LOW);
		this.flipPin(this.clock, PinState.HIGH);
		this.flipPin(this.clock, PinState.LOW);
	}

	private final static int NB_TRIES = 35;

	public void waitForResult() {
		this.data.setMode(PinMode.DIGITAL_INPUT);
		PinState state = PinState.HIGH;
		for (int t=0; t<NB_TRIES; t++) {
			delay(10L, 0);
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
		int value = readMeasurement();
		if (DEBUG) {
			System.out.println(String.format(">> Read temperature raw value %d", value));
		}
		return (value * 0.01) + (-39.7); // Celcius
	}

	public double readHumidity() {
		return readHumidity(null);
	}
	public double readHumidity(Double temp) {
		double t = temp;
		if (temp == null) {
			t = this.readTemperature();
		}
		byte cmd = COMMANDS.get(HUMIDITY_CMD);
		this.sendCommandSHT(cmd);
		int value = readMeasurement();
		if (DEBUG) {
			System.out.println(String.format(">> Read humidity raw value %d", value));
		}

		double linearHumidity = -2.0468 + (0.0367 * value) + (-0.0000015955 * Math.pow(value, 2));
		double humidity = ((t - 25) * (0.01 + (0.00008 * value)) + linearHumidity); // %
		return humidity;
	}
	/**
	 *
	 * @return a 16 bit word.
	 */
	private int readMeasurement() {
		int value = 0;

		// MSB
		value = this.getByte();
		value <<= 8;
		this.sendAck();
		// LSB
		value |= this.getByte();

		this.endTx();

		return value;
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
			PinState state = this.data.getState();
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

	private void delay(long ms, int nano) {
		try {
			Thread.sleep(ms, nano);
		} catch (InterruptedException ie) {
			// Absorb
		}
	}

}
