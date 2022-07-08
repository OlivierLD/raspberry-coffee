package bt;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Start an Arduino Uno with its HC-05 on.<br/>
 * Serial port (rfcomm0 below) may vary.<br/>
 * <br/>
 * See system properties:
 * <ul>
 * <li><code>"serial.port"</code>, default <code>"/dev/rfcomm0"</code></li>
 * <li><code>"baud.rate"</code>, default <code>"9600"</code></li>
 * </ul>
 * <p>
 * Read & write to the Bluetooth/Serial Port
 */
public class BT101 implements SerialIOCallbacks {
	@Override
	public void connected(boolean b) {
		System.out.println("Bluetooth connected: " + b);
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	/**
	 * Receiver
	 *
	 * @param b one byte at a time
	 */
	@Override
	public void onSerialData(byte b) {
//  System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		if (b == 0xA) { // \n, NL => Message completed
			byte[] mess = new byte[bufferIdx];
			for (int i = 0; i < bufferIdx; i++) {
				mess[i] = serialBuffer[i];
			}
			bluetoothOutput(mess); // See below
			// Reset
			lenToRead = 0;
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	/**
	 * Invoked by {@link #onSerialData(byte)}
	 *
	 * @param mess
	 */
	public void bluetoothOutput(byte[] mess) {
		if (true) { // verbose...
			try {
				String[] sa = DumpUtil.dualDump(mess);
				if (sa != null) {
					System.out.println("\t>>> [From Bluetooth] Received:");
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
		final BT101 mwc = new BT101();
		final SerialCommunicator sc = new SerialCommunicator(mwc);
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

		String serialPortName = System.getProperty("serial.port", "/dev/rfcomm0");
		String baudRateStr = System.getProperty("baud.rate", "9600");
		System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));
		CommPortIdentifier bluetoothPort = pm.get(serialPortName);
		if (bluetoothPort == null) {
			System.out.println(String.format("Port %s not found, aborting", serialPortName));
			System.exit(1);
		}
		System.out.println("---------------------");
		System.out.println("Flipping the switch  ");
		System.out.println("---------------------");
		System.out.println("Ctrl + C to stop");
		final AtomicBoolean keepLooping = new AtomicBoolean(true);
		boolean on = false;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			keepLooping.set(false);
			System.out.println("\nExiting...");
		}));
		try {
			sc.connect(bluetoothPort, "Bluetooth", Integer.parseInt(baudRateStr));
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			Thread.sleep(500L);
			// Looping
			while (keepLooping.get()) {
				System.out.println("Writing to the serial port.");
				sc.writeData(on ? "0" : "1");
				on = !on;
				Thread.sleep(3_000L);
				System.out.println("Data written to the serial port.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			Thread.sleep(10_000L);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		try {
			sc.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done.");
	}

}
