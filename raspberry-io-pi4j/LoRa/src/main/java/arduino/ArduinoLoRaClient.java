package arduino;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Connect an Arduino Uno with its USB cable.
 * Serial port (/dev/ttyUSB0 below) may vary.
 *
 * Talks to the Arduino where ArdiunoRF95_TX is running
 */
public class ArduinoLoRaClient implements SerialIOCallbacks {

	private SerialCommunicator sc = null;
	private boolean verbose = false;

	private Consumer<String> callback;

	public ArduinoLoRaClient(String portName, int baudRate, Consumer<String> onData)
			throws Exception {

		if (portName.isEmpty()) {
			throw new RuntimeException("Please provide a serial port name");
		}
		if (onData == null) {
			throw new RuntimeException("Please provide a callback function");
		}
		this.verbose = "true".equals(System.getProperty("lora.verbose", "false"));
		sc = new SerialCommunicator(this);
		sc.setVerbose("true".equals(System.getProperty("serial.verbose", "false")));

		this.callback = onData;

		Map<String, CommPortIdentifier> pm = sc.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String port : ports) {
			System.out.println("-> " + port);
		}
		System.out.println("======================");

		String serialPortName = portName;
		System.out.println(String.format("Opening port %s:%d", serialPortName, baudRate));

		CommPortIdentifier arduinoPort = pm.get(serialPortName);
		if (arduinoPort == null) {
			System.out.println(String.format("Port %s not found in the list", serialPortName));
//		System.exit(1);
		}

		// Try this if not found in list...
		if (arduinoPort == null) {
			if (this.verbose) System.out.println("Trying plan B...");
			try {
				arduinoPort = CommPortIdentifier.getPortIdentifier(serialPortName);
			} catch (NoSuchPortException nspe) {
				throw nspe;
			}
		}

		try {
			/*
			 * Serial connection here
			 */
			sc.connect(arduinoPort, "Arduino", baudRate);
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			Thread.sleep(500L);
			// Wake up! Boom Boom!
			for (int i = 0; i < 2; i++) {
				sc.writeData("\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void sendToLora(String str)
			throws IOException {
		this.sc.writeData(str);
	}

	public void close()
			throws IOException {
		this.sc.disconnect();
	}

	@Override
	public void connected(boolean b) {
		System.out.println("Arduino connected: " + b);
	}

	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	@Override
	public void onSerialData(byte b) {
//  System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		if (b == 0xA) { // \n
			// Message completed
			byte[] mess = new byte[bufferIdx];
			for (int i = 0; i < bufferIdx; i++) {
				mess[i] = serialBuffer[i];
			}
			arduinoOutput(mess);
			// Reset
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	private void arduinoOutput(byte[] mess) {
		if (this.verbose) {
			try {
				String[] sa = DumpUtil.dualDump(mess);
				if (sa != null) {
					System.out.println("\t>>> [From Arduino] Received:");
					for (String s : sa) {
						System.out.println("\t\t" + s);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		this.callback.accept(new String(mess));
	}
}
