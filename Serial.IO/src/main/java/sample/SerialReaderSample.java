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

	public SerialReaderSample() {
		monitor.start();
	}

	@Override
	public void connected(boolean b) {
		System.out.println("Serial Port connected: " + b);
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	private long lastReceiveTime = System.currentTimeMillis();

	private static boolean keepMonitoring = true;
	private Thread monitor = new Thread(() -> {
		boolean interrupted = false;
		while (keepMonitoring) {
			try {
				synchronized (this) {
					long before = System.currentTimeMillis();
					this.wait(1_000L);
					if (System.currentTimeMillis() - before < 1_000) {
						// Was notified!
						interrupted = true;
						keepMonitoring = false;
					}
				}
			} catch (InterruptedException ie) {
				System.err.println("Ooops");
				interrupted = true;
				keepMonitoring = false;
			}
			if (!interrupted) {
				long now = System.currentTimeMillis();
				if (now - lastReceiveTime > 1_000) {
					if (bufferIdx > 0) {
						completeAndSend();
//					} else {
//						if (verbose) {
//							System.out.println("Nothing to flush");
//						}
					}
					lastReceiveTime = now;
				}
			}
		}
		System.out.println("Exiting monitor");
	});

	private void completeAndSend() {
		byte[] mess = new byte[bufferIdx];
		for (int i = 0; i < bufferIdx; i++) {
			mess[i] = serialBuffer[i];
		}
		serialOutput(mess); // spit it out
		// Reset
		lenToRead = 0;
		bufferIdx = 0;
	}

	@Override
	public void onSerialData(byte b) {
//  System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
		synchronized (serialBuffer) {
			serialBuffer[bufferIdx++] = (byte) (b & 0xFF); // Adding to the buffer
			if (b == 0xA) { // \n
				// Message completed
				completeAndSend();
			}
			lastReceiveTime = System.currentTimeMillis();
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
		final SerialReaderSample serialReader = new SerialReaderSample();
		String filters = System.getProperty("filters");
		if (filters != null) {
			serialReader.setSentenceFilter(Arrays.asList(filters.split(",")));
		}
		final SerialCommunicator sc = new SerialCommunicator(serialReader);
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
			keepMonitoring = false;
			try {
				synchronized (serialReader.monitor) {
					serialReader.monitor.notify();
//					serialReader.monitor.interrupt();
				}
				synchronized (thread) {
					thread.notify();
				}
				// Thread.sleep(1_000L);
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
					System.out.println("\nNotified (Main).");
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			System.out.println("Disconnecting.");
			sc.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done.");
	}
}
