package basic;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;
import utils.StaticUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/*
 * A skeleton for further implementation.
 * Use it for example with an Arduino and an HC-05 module, running `bluetooth.102.ino`
 */
public class SimpleSerialCommunication implements SerialIOCallbacks {

	private final static int DEFAULT_BAUDRATE = 115_200;

	private final static String NEW_LINE = "\r\n"; // \n = 0xA, \r = 0xD
	private boolean responseReceived = false;
	private StringBuffer response = new StringBuffer();

	@Override
	public void connected(boolean b) {
		System.out.println("Connected!");
	}

	@Override
	public void onSerialData(byte b) {
		response.append((char)b);
		if (verbose) {
	//	System.out.println(String.format("Current Buffer > [%s]", response.toString()));
			DumpUtil.displayDualDump(response.toString());
		}
		if (response.toString().endsWith(NEW_LINE)) {
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

	private static boolean verbose = "true".equals(System.getProperty("bt.verbose"));

	private static SerialCommunicator serialCommunicator = null;
	private boolean simulateSerial = false;

	public void init(String port) {
		init(port, DEFAULT_BAUDRATE);
	}

	public void init(String port, int br) {
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
		this.serialDialog();
	}

	public void serialDialog() {
		boolean keepLooping = true;
		while (keepLooping) {
			try {
				String userInput = StaticUtil.userInput("(Q to quit) > ");
				if ("Q".equalsIgnoreCase(userInput)) {
					keepLooping = false;
				} else {
					String dataToWrite = userInput + NEW_LINE;
					if (verbose) {
						//	System.out.println(String.format("Current Buffer > [%s]", response.toString()));
						DumpUtil.displayDualDump(dataToWrite);
					} else {
						System.out.println(String.format("Writing: %s", userInput));
					}
					serialCommunicator.writeData(dataToWrite.getBytes());
					// Wait for reply
					if (verbose) {
						System.out.println("Waiting for reply...");
					}
					String reply = waitForResponse();
					if (verbose) {
						System.out.println(String.format(">> Received [%s]", reply));
						DumpUtil.displayDualDump(reply);
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
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

		SimpleSerialCommunication simpleSerialCommunication = new SimpleSerialCommunication();
		simpleSerialCommunication.simulateSerial = "true".equals(System.getProperty("serial.simulate"));

		String bluetoothSerialPort = System.getProperty("serial.port", "/dev/tty.Bluetooth-Incoming-Port");
		int bluetoothSerialPortBaudRate = Integer.parseInt(System.getProperty("bt.serial.baud.rate", String.valueOf(DEFAULT_BAUDRATE)));
		if (verbose) {
			System.out.println(String.format("Opening %s:%d", bluetoothSerialPort, bluetoothSerialPortBaudRate));
		}

		simpleSerialCommunication.init(bluetoothSerialPort, bluetoothSerialPortBaudRate);

		simpleSerialCommunication.initBluetoothComm();

		System.out.println("Closing connection.");
		simpleSerialCommunication.closeSerialConnection();
		System.out.println("Bye!");
		synchronized (Thread.currentThread()) {
			Thread.currentThread().notify();
		}
		System.exit(0);
	}
}
