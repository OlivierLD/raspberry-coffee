package sensors.sth10;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.HashMap;
import java.util.Map;

public class STH10Driver {

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

	private GpioPin data;
	private GpioPin clock;

	private byte statusRegister = 0x0;

	public STH10Driver() {
		this(DEFAULT_DATA_PIN, DEFAULT_CLOCK_PIN);
	}

	public STH10Driver(Pin _dataPin, Pin _clockPin) {
		this.dataPin = _dataPin;
		this.clockPin = _clockPin;

		this.data = gpio.getProvisionedPin(this.dataPin);
		this.clock = gpio.getProvisionedPin(this.clockPin);
	}

	private void resetConnection() {
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		for (int i=0; i<10; i++) {
			gpio.high((GpioPinDigitalOutput)this.clock);
			gpio.low((GpioPinDigitalOutput)this.clock);
		}
	}

	private void writeStatusRegister(byte mask) {
		byte cmd = COMMANDS.get(WRITE_STATUS_REGISTER_CMD);
		this.sendCommandSHT(cmd, false);
		this.sendByte(mask);
		this.getAck(WRITE_STATUS_REGISTER_CMD);
		this.statusRegister = mask;
	}

	private void resetStatusRegister() {
		this.writeStatusRegister(COMMANDS.get(NO_OP_CMD));
	}

	private void startTx() {
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		gpio.high((GpioPinDigitalOutput)this.clock);

		gpio.low((GpioPinDigitalOutput)this.data);
		gpio.low((GpioPinDigitalOutput)this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		gpio.high((GpioPinDigitalOutput)this.clock);

		gpio.low((GpioPinDigitalOutput)this.clock);
	}

	private void endTx() {
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		gpio.high((GpioPinDigitalOutput)this.clock);

		gpio.low((GpioPinDigitalOutput)this.clock);
	}

	private void sendByte(byte data) {
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		for (int i=0; i<8; i++) {
			if ((data & (1 << (7 -i))) == 0) {
				gpio.low((GpioPinDigitalOutput)this.data);
			} else {
				gpio.high((GpioPinDigitalOutput)this.data);
			}
			gpio.high((GpioPinDigitalOutput)this.clock);
			gpio.low((GpioPinDigitalOutput)this.clock);
		}
	}

	private byte getByte() {
		byte b = 0x0;

		gpio.setMode(PinMode.DIGITAL_INPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		for (int i=0; i<8; i++) {
			gpio.high((GpioPinDigitalOutput)this.clock);
			PinState state = gpio.getState((GpioPinDigital) this.data);
			if (state == PinState.HIGH) {
				b |= (1 << (7 - 1));
			}
			gpio.low((GpioPinDigitalOutput)this.clock);
		}
		return b;
	}

	private void getAck(String commandName) {
		gpio.setMode(PinMode.DIGITAL_INPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.clock);
		PinState state = gpio.getState((GpioPinDigital) this.data);
		if (state == PinState.HIGH) {
			throw new RuntimeException(String.format("SHTx failed to properly receive command [%s, %8s]", commandName, Integer.toBinaryString(COMMANDS.get(commandName)).replace(' ', '0')));
		}
		gpio.low((GpioPinDigitalOutput)this.clock);
	}

	private void sendAck() {
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.data);
		gpio.setMode(PinMode.DIGITAL_OUTPUT, this.clock);

		gpio.high((GpioPinDigitalOutput)this.data);
		gpio.low((GpioPinDigitalOutput)this.data);
		gpio.high((GpioPinDigitalOutput)this.clock);
		gpio.low((GpioPinDigitalOutput)this.clock);
	}

	public void waitForResult() {
		gpio.setMode(PinMode.DIGITAL_INPUT, this.data);
		PinState state = PinState.HIGH;
		for (int t=0; t<35; t++) {
			try { Thread.sleep(100L); } catch (InterruptedException ie) { /* Abosrb */ }
			state = gpio.getState((GpioPinDigital) this.data);
			if (state == PinState.LOW) {
				// Completed
				break;
			}
		}
		if (state == PinState.HIGH) {
			throw new RuntimeException("Sensor has not completed measurement within allocated time.");
		}
	}

	public void init() {
		this.resetConnection();
		byte mask = 0x0;
		// Other options go here

		this.writeStatusRegister(mask);
	}

	public double readTemperature() {
		byte cmd = COMMANDS.get(TEMPERATURE_CMD);
		this.sendCommandSHT(cmd);
		int value = readMeasurement();

		return value;
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
// TODO Some mgic goes here
		return value;
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
		if (!COMMANDS.containsValue(command)) {
			throw new RuntimeException(String.format("Command %8s not found.", Integer.toBinaryString(command).replace(' ', '0')));
		}
		String commandName = COMMANDS.entrySet()
				.stream()
				.filter(entry -> command == COMMANDS.get(entry))
				.map(entry -> entry.getKey())
				.findFirst()
				.get();
		this.startTx();
		this.sendByte((byte) command);
		this.getAck(commandName);

		if (measurement) {
			PinState state = gpio.getState((GpioPinDigital) this.data);
			// SHT1x is taking measurement.
			if (state == PinState.LOW) {
				throw new RuntimeException("SHT1x is not in the proper measurement state. DATA line is LOW.");
			}
			this.waitForResult();
		}
	}

}
