package sample;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read serial port (/dev/ttyUSB0 below).
 */
public class SerialReaderSample implements SerialIOCallbacks {
	private boolean verbose = "true".equals(System.getProperty("serial.verbose", "false"));

	private List<String> filters = null;
	private void setSentenceFilter(List<String> filters) {
		this.filters = filters;
	}

	@Override
	public void connected(boolean b) {
		System.out.println("Serial Port connected: " + b);
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
			serialOutput(mess);
			// Reset
			lenToRead = 0;
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	/**
	 * Formatting the data read by {@link #onSerialData(byte)}
	 * @param mess
	 */
	public void serialOutput(byte[] mess) {
		if (verbose) { // verbose...
			try {
				String[] sa = DumpUtil.dualDump(mess);
				if (sa != null) {
					System.out.println("\t>>> [From Serial Port] Received:");
					for (String s : sa) {
						System.out.println("\t\t" + s);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			String sentence = new String(mess);
			String id = sentence.substring(3, 6);
			if (filters == null || filters.contains(id)) {
				System.out.print(sentence); // Regular output
			}
		}
	}

	public static void main(String... args) {
		final SerialReaderSample gpsReader = new SerialReaderSample();
		String filters = System.getProperty("filters");
		if (filters != null) {
			gpsReader.setSentenceFilter(Arrays.asList(filters.split(",")));
		}
		final SerialCommunicator sc = new SerialCommunicator(gpsReader);
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
		String baudRateStr = System.getProperty("baud.rate", "4800");
		System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));
		CommPortIdentifier serialPort = pm.get(serialPortName);
		if (serialPort == null) {
			System.out.println(String.format("Port %s not found, aborting", serialPortName));
			System.exit(1);
		}
		final Thread thread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				synchronized (thread) {
					thread.notify();
					Thread.sleep(1_000L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}));
		try {
			sc.connect(serialPort, "Serial", Integer.parseInt(baudRateStr));
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			synchronized (thread) {
				try {
					thread.wait();
					System.out.println("\nNotified.");
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
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
