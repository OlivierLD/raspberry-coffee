package sample;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Connect an Arduino Uno with its USB cable.
 * Serial port (/dev/ttyUSB0 below) may vary.
 *
 * Interactive version.
 * Enter a String from the command line,
 * Send it to the Arduino,
 * It comes back inverted.
 *
 * The Arduino must have the ArduinoSerialEvent.ino sketch running on it.
 *
 * See system properties:
 * "serial.port", default "/dev/ttyUSB0"
 * "baud.rate", default "9600"
 */
public class ArduinoCLIClient implements SerialIOCallbacks {
	@Override
	public void connected(boolean b) {
		System.out.println("Arduino connected: " + b);
	}

	private int lenToRead = 0;
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
			lenToRead = 0;
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	public void arduinoOutput(byte[] mess) {
		if (true) { // verbose...
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
	}

	public static void main(String... args) {
		final ArduinoCLIClient caller = new ArduinoCLIClient();
		final SerialCommunicator sc = new SerialCommunicator(caller);
		sc.setVerbose(false);

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

		String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
		String baudRateStr = System.getProperty("baud.rate", "9600");
		System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));

		CommPortIdentifier arduinoPort = pm.get(serialPortName);
		if (arduinoPort == null) {
			System.out.println(String.format("Port %s not found in the list", serialPortName));
//		System.exit(1);
		}

		// Try this if not found in list...
		if (arduinoPort == null) {
			System.out.println("Trying plan B...");
			try {
				arduinoPort = CommPortIdentifier.getPortIdentifier(serialPortName);
			} catch (NoSuchPortException nspe) {
				System.err.println(serialPortName + ": No Such Port");
				nspe.printStackTrace();
				if ("/dev/ttyACM0".equals(serialPortName)) {
					System.err.println("Note: There is some bug in libRxTx-java regarding the access to /dev/ttyACM0");
					System.err.println("If this is your case, try creating a symbolic link on the port, and access it through its link:");
					System.err.println(" $ sudo ln -s /dev/ttyACM0 /dev/ttyS80");
					System.err.println("Then try reading or writing on /dev/ttyS80");
				}
				System.exit(1);
			}
		}

		System.out.println("Enter 'Q' at the prompt to quit.");
		try {
			/*
			 * Serial connection here
			 */
			sc.connect(arduinoPort, "Arduino", Integer.parseInt(baudRateStr));
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			Thread.sleep(500L);
			// Wake up!
			for (int i = 0; i < 5; i++) {
				sc.writeData("\n");
			}

			boolean keepWorking = true;
			while (keepWorking) {
				String str = utils.StaticUtil.userInput("?> ");
				if ("Q".equalsIgnoreCase(str.trim())) {
					keepWorking = false;
				} else {
					sc.writeData(str + "\n");
				}
			}
			System.out.println("Exiting.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			sc.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done.");
	}
}
