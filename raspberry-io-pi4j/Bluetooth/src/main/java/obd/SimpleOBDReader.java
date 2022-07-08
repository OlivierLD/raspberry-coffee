package obd;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/*
 * WIP - TODO:
 *   Add a GPS?
 *   Add logging
 */
public class SimpleOBDReader implements SerialIOCallbacks {

	private final static int DEFAULT_BAUDRATE = 115_200;

	private final int MODE_01 = 0x0100;
	private final int PIDS_AVAIL = 0x00;
	private final int ENGINE_RPM = 0x0C;
	private final int VEHICLE_SPEED = 0x0D;
	private final int ACCEL_POS = 0x11;

	// Change this according to your vehicle!
	private final int RESPONSE_PREFIX_OFFSET = 4;

	private final static String OBD_MAC_ADDRESS = "1D,A5,68988B";
	private final static String CMD_CHECK = "AT";
	private final static String CMD_INIT = "+INIT";
	private final static String CMD_PAIR = "+PAIR=";
	private final static String CMD_PAIR_TIMEOUT = ",10";
	private final static String CMD_PAIR_CHECK = "+FSAD=";
	private final static String CMD_CONNECT = "+LINK=";
	private final static String CMD_PIDS = "0100";  // MODE_01 | PIDS_AVAIL
	private final static String CMD_RPM = "010C";   // MODE_01 | ENGINE_RPM
	private final static String CMD_SPEED = "010D"; // MODE_01 | VEHICLE_SPEED
	private final static String CMD_THROTTLE = "0111"; // MODE_01 | ACCEL_POS
	private final static String NEW_LINE = "\r\n";
	private final static String BT_RESPONSE_OK = "OK";
	private final static String BT_RESPONSE_ERROR = "ERROR";
	private final static String BT_RESPONSE_FAIL = "FAIL";
	private final static String OBD_NO_DATA = "NO DATA";

	private int commandCount = 0;

	private boolean nextCommandReady = false;
	private boolean responseReceived = false;

	private StringBuffer response = new StringBuffer();

	@Override
	public void connected(boolean b) {

	}

	@Override
	public void onSerialData(byte b) {
		response.append((char)b);
		if (verbose) {
	//	System.out.println(String.format("Current Buffer > [%s]", response.toString()));
			DumpUtil.displayDualDump(response.toString());
		}
		if (response.toString().endsWith(NEW_LINE)) {  // Ends with CR LF, aka NEW_LINE
			this.responseReceived = true;
			synchronized (Thread.currentThread()) {
				Thread.currentThread().notify();
			}
		}
	}

	@Override
	public void onSerialData(byte[] ba, int len) {
		System.out.println(String.format("onSerialData-2 [%s]", new String(ba)));
	}

	public static class OBDHolder {
		short RPM;
		byte speed;
		byte throttle;
	}

	private OBDHolder currentObdReading = new OBDHolder();

	private static boolean verbose = "true".equals(System.getProperty("obd.verbose"));

	private static SerialCommunicator serialCommunicator = null;
	private boolean simulateSerial = false;

	public void init(String port) {
		init(port, DEFAULT_BAUDRATE);
	}

	public void init(String port, int br) {
		assert port != null;

		if (!simulateSerial) {
			serialCommunicator = new SerialCommunicator(this);
		} else {
			serialCommunicator = new SerialCommunicator(this, System.in, System.out);
		}
		serialCommunicator.setVerbose(verbose);

		Map<String, CommPortIdentifier> pm = serialCommunicator.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String serialPort : ports) {
			System.out.println("-> " + serialPort);
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

	public void closeSerialConnection() {
		if (serialCommunicator.isConnected()) {
			try {
				serialCommunicator.disconnect();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void setVerbose(boolean b) {
		this.verbose = b;
	}

	public void initBluetoothComm() {
		this.nextCommandReady = true;
		this.currentObdReading.RPM = -1;
		this.currentObdReading.speed = -1;
		this.currentObdReading.throttle = -1;

		this.connectToOBDDevice();
	}

	public void connectToOBDDevice() {
		try {
			String dataToWrite = CMD_CHECK + NEW_LINE;
			System.out.println(String.format("Writing: %s", dataToWrite));
			serialCommunicator.writeData(dataToWrite.getBytes());
			// Wait for reply
			String reply = waitForResponse();
			if (verbose) {
//				System.out.println(String.format(">> Received [%s]", reply));
				DumpUtil.displayDualDump(reply);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private String waitForResponse() { // TODO Return String or byte[] ?

		synchronized (Thread.currentThread()) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		String resp = "";
		// Get the response
		if (this.responseReceived) {
			resp = this.response.toString();
			this.response.delete(0, this.response.length()); // Reset
			this.responseReceived = false;
		} else {
			System.out.println("Bizarre...");
		}
		return resp;
	}

	public static void main(String... args) {

		SimpleOBDReader obdReader = new SimpleOBDReader();
		obdReader.simulateSerial = false;

		String bluetoothSerialPort = System.getProperty("bt.serial.port", "/dev/tty.Bluetooth-Incoming-Port");
		if (verbose) {
			System.out.println(String.format("Opening %s:%d", bluetoothSerialPort, bluetoothSerialPort));
		}

		obdReader.init(bluetoothSerialPort);
		obdReader.initBluetoothComm();
		System.out.println("Closing connection");
		obdReader.closeSerialConnection();
		System.out.println("Bye!");
	}
}
