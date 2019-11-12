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
public class SimpleODBReader implements SerialIOCallbacks {

	private final static int DEFAULT_BAUDRATE = 115_200;

	private final int MODE_01 = 0x0100;
//private final int PIDS_AVAIL = 0x00;
	private final int ENGINE_RPM = 0x0C;
	private final int VEHICLE_SPEED = 0x0D;
	private final int ACCEL_POS = 0x11;

	// Change this according to your vehicle!
	private final int  RESPONSE_PREFIX_OFFSET = 4;

	private final static String obdMacAddress = "1D,A5,68988B";
	private final static String cmdCheck = "AT";
	private final static String cmdInit = "+INIT";
	private final static String cmdPair = "+PAIR=";
	private final static String cmdPairTimeout = ",10";
	private final static String cmdPairCheck = "+FSAD=";
	private final static String cmdConnect = "+LINK=";
	private final static String cmdPids = "0100";
	private final static String cmdRpm = "010C";
	private final static String cmdSpeed = "010D";
	private final static String cmdThrottle = "0111";
	private final static String newLine = "\r\n";
	private final static String btResponseOk = "OK";
	private final static String btResponseError = "ERROR";
	private final static String btResponseFail = "FAIL";
	private final static String obdNoData = "NO DATA";

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
		if (response.toString().endsWith("\n\r")) {
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
			serialCommunicator.writeData(cmdCheck + newLine);
			// Wait for reply
			String reply = waitForResponse();
			if (verbose) {
				System.out.println(String.format(">> Received [%s]", reply));
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private String waitForResponse() {

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

		SimpleODBReader obdReader = new SimpleODBReader();
		obdReader.init("/dev/tty.Bluetooth-Incoming-Port"); // TODO A system variable
		obdReader.initBluetoothComm();

		obdReader.closeSerialConnection();
	}
}
