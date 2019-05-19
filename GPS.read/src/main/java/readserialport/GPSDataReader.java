package readserialport;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import java.io.IOException;
import utils.DumpUtil;

/**
 * Just reads the GPS data, with PI4J (no need for libRxTx here).
 * No parsing, just raw data.
 * <p>
 * Uses the Serial communication packages from PI4J
 */
public class GPSDataReader {

	private static Baud getBaudRate(int br) {
		Baud baud = null;
		for (Baud b : Baud.values()) {
			if (b.getValue() == br) {
				baud = b;
				break;
			}
		}
		return baud;
	}

	public static void openSerial(Serial serial, String port, int br) throws IOException {
		SerialConfig config = new SerialConfig();
		config.device(port)
						.baud(getBaudRate(br))
						.dataBits(DataBits._8)
						.parity(Parity.NONE)
						.stopBits(StopBits._1)
						.flowControl(FlowControl.NONE);
		serial.open(config);
	}

	public static void main(String... args)
					throws InterruptedException, NumberFormatException {
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
		String port = System.getProperty("port.name", Serial.DEFAULT_COM_PORT);
		boolean verbose = "true".equals(System.getProperty("verbose", "false"));

		System.out.println("Serial Communication.");
		System.out.println(" ... connect using settings: " + Integer.toString(br) + ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(event -> {
			try {
				// print out the data received to the console
				String data = event.getAsciiString();
				if (verbose) {
					System.out.println("Got Data (" + data.length() + " byte(s))");
					System.out.println(data);
				}
				String[] sa = DumpUtil.dualDump(data);
				for (String str : sa)
					System.out.println(str);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});

		final Thread t = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("\nShutting down...");
				try {
					if (serial.isOpen()) {
						serial.close();
						System.out.println("Serial port closed");
					}
					synchronized (t) {
						t.notify();
						System.out.println("Thread notified");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		try {
			System.out.println("Opening port [" + port + "]");
			boolean open = false;
			while (!open) {
				//	serial.open(port, br);
				openSerial(serial, port, br);
				open = serial.isOpen();
				System.out.println("Port is " + (open ? "" : "NOT ") + "opened.");
				if (!open)
					try {
						Thread.sleep(500L);
					} catch (Exception ex) {
					}
			}
			synchronized (t) {
				t.wait();
			}
			System.out.println("Bye...");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}

