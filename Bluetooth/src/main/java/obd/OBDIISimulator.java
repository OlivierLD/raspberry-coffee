package obd;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.TimeUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/*
 * On your Mac, choose Apple menu > System Preferences, then click Bluetooth. Your Mac is now discoverable.
 * We use here Bluetooth over a Serial connection
 *
 * Adapted from the code at https://github.com/dplanella/arduino-odb2sim.git
 */

public class OBDIISimulator implements SerialIOCallbacks {

// ODBII scan tool simulator. Transmits ODBII PIDs with vehicle sensor
// information upon request, using the ELM327 protocol.
//
// The client will generally be a smart phone running an OBDII app,
// such as Torque.
//
// This program implements the acquisition and calculation of sensor values,
// and transmission of those to the client using OBDII PID structures over the
// ELM327 protocol.
//
//         +-----+---+     +--------+        Xx     +----+
//         |     |   |     |  +--+  |     Xx  XX    |----|
//         |    +++  |     | -+  +- |  Xx  XX  XX   ||  ||
//         | -> | |  +---> | -+  +- |   XX  X   X   ||  ||
//         |    +++  |     |  +--+  |  Xx  XX  XX   ||  ||
//         |     |   |     |        |     Xx  XX    |----|
//         +-----+---+     +--------+        Xx     +----+
//
//           Sensor      MCU + Bluetooth          Smartphone
//                      (OBDII scan tool           or tablet
//                         simulator)
//
// https://en.wikipedia.org/wiki/OBD-II_PIDs

	private static boolean verbose = "true".equals(System.getProperty("obd.verbose"));

	private static SerialCommunicator serialCommunicator = null;

	// RPM calculation variables
	private volatile byte rpm_pulse_count;
	private long rpm;
	private long time_old;

	// Variables to track Mode 1 PIDs
	int eng_rpm = 5_400;
	int ambient_air_temp;
	int eng_oil_temp;
	float eng_coolant_temp;
	float vehicle_speed;
	float in_manifld_press_abs;
	float fuel_percentage;

	// Variables to track Mode 21 (custom) PIDs
	float vehicle_speed_2;
	float analog_in_5;
	int eng_oil_press;

	// Responses
//
// Generally, ELM327 clones return the following as firmware version:
// "ELM327/ELM-USB v1.0 (c) SECONS Ltd."
// Valid ELM327 firmware versions are: 1.0, 1.3a, 1.4b, 2.1 and 2.2
// We will be using our own firmware version and device description
	private final static String DEVICE_DESCRIPTION = "OBDII Simulator";
	private final static String VERSION = DEVICE_DESCRIPTION + " " + "v1.0";
	private final static String OK = "OK";
	private final static String PROMPT = ">";

	// Control characters, whitespace
	private final static char CR = '\r';
	private final static char LF = '\n';
	private final static char SPACE = ' ';

	// Request prefixes
	private final static String AT_REQ = "AT";      // AT command request: handshake/control
	private final static String MODE_01_REQ = "01"; // OBDII Mode 1 request: data (PID values)
	private final static String MODE_21_REQ = "21"; // OBDII Mode 21 request: custom PID values

	// Response prefixes
	private final static String MODE_01_RSP = "41";
	private final static String MODE_21_RSP = "61";

	// OBDII Mode 1 PIDs
	private final static String MODE_1_SUPPORTED_PIDS = "00";
	private final static String MODE_1_ENG_COOLANT_TEMP = "05";
	private final static String MODE_1_IN_MANIFLD_PRESS_ABS = "0B";
	private final static String MODE_1_ENG_RPM = "0C";
	private final static String MODE_1_VEHICLE_SPEED = "0D";
	private final static String MODE_1_FUEL_TANK_LVL_IN = "2F";
	private final static String MODE_1_AMBIENT_AIR_TEMP = "46";
	private final static String MODE_1_ENG_OIL_TEMP = "5C";

	// Serial debug switch. Set it to true to echo the serial communication to an
// external serial monitor.
	private final static boolean SERIAL_DEBUG = true;

	private final static int BAUDRATE = 115_200; // 9_600;
	private final static int DELAY = 600;

	private boolean simulateSerial = false;

	public void openSerial(String port, int br) {
		if (!simulateSerial) {
			serialCommunicator = new SerialCommunicator(this);
		} else {
			serialCommunicator = new SerialCommunicator(this, System.in, System.out);
		}
		serialCommunicator.setVerbose(false);

		Map<String, CommPortIdentifier> pm = serialCommunicator.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String serialport : ports) {
			System.out.println("-> " + serialport);
		}
		System.out.println("======================");

		// String serialPortName = port; // System.getProperty("serial.port", "/dev/ttyUSB0");
		System.out.println(String.format("Opening port %s:%d%s", port, br, (simulateSerial ? " (Simulation)" : "")));

		CommPortIdentifier serialPort = null;
		if (!simulateSerial) {
			serialPort = pm.get(port);
			if (serialPort == null) {
				String mess = String.format("Port %s not found, aborting", port);
				throw new RuntimeException(mess);
			}
		}
		try {
			serialCommunicator.connect(serialPort, "OBD", br); // Other values are defaulted
			boolean b = serialCommunicator.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			serialCommunicator.initListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setup() {
		initSensors();

		// Set up the hardware and software serial objects
		TimeUtil.delay(DELAY);
		String serialPort = System.getProperty("serial.port", "/dev/tty.Bluetooth-Incoming-Port");
		openSerial(serialPort, BAUDRATE);
	}

	private String OBDRequest = "";
	void loop() {

		// Buffer to hold the OBDII request from the serial port and further process it
		updateSensorValues();
	}

// Process the requests (commands) sent from the client app
// The requests can either be:
// - AT commands to set up the simulator and perform handshaking
// - ODBII PID requests to transmit sensor information
	void processRequest(String request) {

		String pid;
		byte reply_bytes = 0x0;
		String ATCommand;
		double value;
		// Value after having applied the calculation formula defined by the ODBII
		// PID structure definition:
		// https://en.wikipedia.org/wiki/OBD-II_PIDs#Mode_01
		double after_formula = 0D;
		byte TEMP_OFFSET = 40;
		String mode_id_rsp = MODE_01_RSP;


		if (SERIAL_DEBUG) {
			System.out.println(String.format("processRequest [%s]", request));
		}

		if (request.startsWith(MODE_01_REQ)) {
			// Mode 1 request: show current data

			pid = request.substring(2); // Get the PID request

			if (SERIAL_DEBUG) {
				System.out.println(MODE_01_REQ + pid);
			}

			if (MODE_1_SUPPORTED_PIDS.equals(pid)) {
				// List of PIDs supported (range 01 to 32)
				// Bit encoded [A7..D0] == [PID 0x01..PID 0x20]
				// https://en.wikipedia.org/wiki/OBD-II_PIDs#Mode_1_PID_00
				reply_bytes = 4;
				value = 142_249_984; // Hex: "087A9000"
				after_formula = value;

			} else if (MODE_1_ENG_COOLANT_TEMP.equals(pid)) {
				// Temperature of the engine coolant in °C
				reply_bytes = 1;
				value = eng_coolant_temp;
				after_formula = value + TEMP_OFFSET;

			} else if (MODE_1_ENG_RPM.equals(pid)) {
				// Engine speed in rpm
				reply_bytes = 2;
				value = eng_rpm;
				after_formula = value * 4;

			} else if (MODE_1_VEHICLE_SPEED.equals(pid)) {
				// Vehicle speed in km/h
				reply_bytes = 1;
				value = vehicle_speed;
				after_formula = value;

			} else if (MODE_1_IN_MANIFLD_PRESS_ABS.equals(pid)) {
				// Intake manifold absolute pressure in kPa
				reply_bytes = 1;
				value = in_manifld_press_abs;
				after_formula = value;

			} else if (MODE_1_FUEL_TANK_LVL_IN.equals(pid)) {
				// Fuel level in %
				reply_bytes = 1;
				value = fuel_percentage;
				after_formula = (int) (((float) value / 100.0) * 255.0);

			} else if (MODE_1_AMBIENT_AIR_TEMP.equals(pid)) {
				// Evaporation purge in %
				reply_bytes = 1;
				value = ambient_air_temp;
				after_formula = value + TEMP_OFFSET;

			} else if (MODE_1_ENG_OIL_TEMP.equals(pid)) {
				// Engine oil temperature in °C
				reply_bytes = 1;
				value = eng_oil_temp;
				after_formula = value + TEMP_OFFSET;
			} else {
				// Unhandled PID
				reply_bytes = 1;
				value = 0;
				after_formula = value;
			}

			mode_id_rsp = MODE_01_RSP;
			replyOBD2(mode_id_rsp, pid, Math.round(after_formula), reply_bytes);

		} else if (request.startsWith(MODE_21_REQ)) {
			// Mode 21 request: used to display custom values

			pid = request.substring(2);

			if (SERIAL_DEBUG) {
				System.out.println(MODE_21_REQ + pid);
			}

			// These are just example custom PIDs. You can define your own instead
			if ("13".equals(pid)) {
				reply_bytes = 2;
				value = vehicle_speed_2;
				after_formula = value;

			} else if ("14".equals(pid)) {
				reply_bytes = 1;
				value = eng_oil_press;
				after_formula = value;

			} else if ("15".equals(pid)) {
				reply_bytes = 2;
				value = analog_in_5;
				after_formula = value;
			}

			mode_id_rsp = MODE_21_RSP;
			replyOBD2(mode_id_rsp, pid, Math.round(after_formula), reply_bytes);

		} else if (request.startsWith(AT_REQ)) {
			// Process AT requests. They are used to set up the connection
			// between the microcontroller and the client app

			ATCommand = request.substring(2);

			if (SERIAL_DEBUG) {
				System.out.println(AT_REQ + ATCommand);
			}

			if ("Z".equals(ATCommand)) {
				// Reset all
				initSensors();
				replyVersion();
				replyOK();

			} else if ("E0".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("M0".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("L0".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("ST62".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("S0".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("H0".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("H1".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("AT1".equals(ATCommand)) {
				replyNotImplemented();

			} else if ("@1".equals(ATCommand)) {
				// Device description (adapter manufacturer)
				replyDescription();

			} else if ("I".equals(ATCommand)) {
				// Adapter firmware version
				replyVersion();

			} else if ("SP0".equals(ATCommand)) {
				// Set protocol to auto
				replyNotImplemented();

			} else if ("DPN".equals(ATCommand)) {
				// Device Protocol Number
				replyValue("1"); //just say it is number 1.

			} else if ("RV".equals(ATCommand)) {
				// Read Voltage
				replyValue("12.5");

			} else if ("PC".equals(ATCommand)) {
				// Protocol Close: terminates current diagnostic session
				replyOK();
			}
		}

		replyPrompt();

		request = "";  // Clear the request buffer once processed
	}

	// Sends a reply to an OBD2 request
	void replyOBD2(String mode_response, String pid, long value, byte reply_bytes) {
		String modeReply = mode_response + pid + toHexReply(value, reply_bytes);

		try {
			serialCommunicator.writeData(modeReply);
			if (SERIAL_DEBUG) {
				System.out.println(modeReply);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Sends an "OK" reply to an AT command request to acknowledge reception
	void replyOK() {
		replyValue(OK);
	}

	// Sends an "OK" reply to an AT command request for an unimplemented command.
// The client app will then generally ignore the response, but it does expect
// an acknowledgement of reception
	void replyNotImplemented() {
		replyOK();
	}

	// Sends the firmware version number upon AT command request
	void replyVersion() {
		replyValue(VERSION);
	}

	// Sends the device description upon AT command request
	void replyDescription() {
		replyValue(DEVICE_DESCRIPTION);
	}

	// Sends the prompt character to indicate that the simulator is idle and
// awaiting a command
	void replyPrompt() {
		replyValue(PROMPT);
	}

	// Sends a value as a request to either an AT or OBD2 command
	void replyValue(String value) {
		try {
			serialCommunicator.writeData(value);
			if (SERIAL_DEBUG) {
				System.out.println(value);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Sends a response to an OBD2 command
	void replyOBDResponse(String response) {
		if (response.length() > 0) {
			replyValue(response);
		}
	}

	public long micros() {
		return System.nanoTime() / 1_000;
	}
	private final static int RPM_SAMPLE_COUNT = 30;
	// Updates the sensor values
	void updateSensorValues() {
		// Update RPM every n sample counts:
		// - increase n for better RPM resolution,
		// - decrease for faster update

		// Sensor data sent as mode 1 PIDs
		if (rpm_pulse_count >= 30) {
			rpm = (30 * 1000 * 1000 / (micros() - time_old)) * rpm_pulse_count;
			time_old = micros();

			rpm_pulse_count = 0;
			eng_rpm = (int)rpm;
		}

		eng_coolant_temp = 93;
		vehicle_speed = 155;
		in_manifld_press_abs = 200;
		ambient_air_temp = 22; // Outside temperature 22 °C
		eng_oil_temp = 109; // Oil temp. 109 °C
		fuel_percentage = 89.0f;

		// Sensor data sent as custom mode 21 PIDs
		eng_oil_press = 65;
		analog_in_5 = 5f; // analogRead(5); // Read sensor
		vehicle_speed_2 = 312;
	}

	// Initialize the sensor definitions and pin states and function
	void initSensors() {

		rpm_pulse_count = 0;
		rpm = 0;
		time_old = 0;

//	attachInterrupt(digitalPinToInterrupt(rpmPin), onRpm, RISING);

	}

	// Increase pulse count upon an interrupt on the RPM monitoring pin
	void onRpm() {
		rpm_pulse_count++;
	}

	// Return a hex string with padded zeroes up to the specified width
	String toHexReply(long value, int width) {

		// Generally the sprintf() function would be used, but its Arduino
		// implementation lacks most of the advanced functionality from its
		// standard C++ counterpart. Thus we use a sort of a brute force
		// approach by prepending zeroes until the given width is achieved
		String value_hex = Long.toHexString(value);
		int value_hex_len = value_hex.length();
		int MAX_REPLY_LEN = 4; // in bytes

		if (width <= MAX_REPLY_LEN) {
			// Width was specified in bytes:
			// 1 byte = 2 hex characters
			width *= 2;
			while ((width - value_hex_len) > 0) {
				value_hex = "0" + value_hex;
				value_hex_len++;
			}
		}

		return value_hex;
	}

	@Override
	public void connected(boolean b) {

	}

	@Override
	public void onSerialData(byte b) {
		if (verbose) {
			System.out.println(String.format("onSerialData-1 %s", String.valueOf((char)b)));
		}
		// Read one received character at a time
		char c = (char)b;

		if ((c == LF || c == CR) && OBDRequest.length() > 0) {
			// Once a full command is received, process it and clear the command
			// buffer afterwards to start receiving new requests
			OBDRequest.toUpperCase();
			processRequest(OBDRequest);
			OBDRequest = "";
		} else if (c != SPACE && c != LF && c != CR) {
			// If the full command is not yet there, read a new character
			// Ignore whitespace and control characters
			OBDRequest += c;
		}
	}

	@Override
	public void onSerialData(byte[] ba, int len) {
		System.out.println(String.format("onSerialData-2 %s", new String(ba)));
	}

	private static boolean go = true;
	public static void main(String... args) {
		OBDIISimulator simulator = new OBDIISimulator();
		simulator.setup();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nCtrl+C intercepted");
			go = false;
		}));
		while (go) {
			simulator.loop();
		}
		System.out.println("Done");
		try {
			serialCommunicator.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
