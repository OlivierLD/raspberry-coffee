package basic;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import utils.DumpUtil;
import utils.StaticUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static utils.TimeUtil.delay;

/**
 * From scratch, not from the PI4J samples.
 * Uses the Serial library from PI4J.
 * <p>
 * This example program supports the following optional System variables:
 * "port.name"                   [DEFAULT: /dev/rfcomm0]
 * "baud.rate"                   [DEFAULT: 9600]
 * <p>
 * A skeleton for further implementation.
 * Use it for example with an Arduino and an HC-05 module, running `bluetooth.102.ino` or `bluetooth.spy.ino`
 */
public class SimpleSerialPI4JCommunication {

	private final static String NEW_LINE = "\r\n"; // \n = 0xA, \r = 0xD
	private boolean responseReceived = false;
	private StringBuffer response = new StringBuffer();
	private final Thread waiter;

	private static boolean verbose = "true".equals(System.getProperty("bt.verbose"));

	public SimpleSerialPI4JCommunication() {
		this.waiter = Thread.currentThread();
	}

	private String waitForResponse() { // TODO Return String or byte[] ?

		synchronized (waiter) {
			try {
				waiter.wait();
				if (verbose) {
					System.out.println("\tWaiter thread released");
				}
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
		if (verbose) {
			System.out.println(String.format("Dispatching response [%s]", resp));
		}
		return resp;
	}

	private void eventManager(SerialDataEvent event) {
		// print out the data received to the console
		try {
			this.response.append(event.getAsciiString());
			if (verbose) {
				//	System.out.println(String.format("Current Buffer > [%s]", response.toString()));
				DumpUtil.displayDualDump(this.response.toString());
			}
			if (this.response.toString().endsWith(NEW_LINE)) {
				if (verbose) {
					System.out.println("\tNew line detected");
				}
				this.responseReceived = true;
				synchronized (waiter) {
					waiter.notify();
					if (verbose) {
						System.out.println("\tWaiter thread notified");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initComm(String portName, int baudRate) {

		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(this::eventManager);

		try {
			// create serial config object
			SerialConfig config = new SerialConfig(); // May display Error, but does not throw it.

			// set default serial settings (device, baud rate, flow control, etc)
			Baud br;
			br = Baud.getInstance(baudRate);
			config.device(portName)
					.baud(br)
					.dataBits(DataBits._8)
					.parity(Parity.NONE)
					.stopBits(StopBits._1)
					.flowControl(FlowControl.NONE);

			System.out.println("Let's get started");

			// open the default serial device/port with the configuration settings
			serial.open(config);

			// continuous loop to keep the program running until the user terminates the program
			final AtomicBoolean keepLooping = new AtomicBoolean(true);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				keepLooping.set(false);
				System.out.println("\nExiting the loop");
				delay(1_000);
			}));

			while (keepLooping.get()) {
				try {
					String userInput = StaticUtil.userInput("(Q to quit) > "); // Blocking input
					if ("Q".equalsIgnoreCase(userInput)) {
						System.out.println("\tExiting...");
						keepLooping.set(false);
					} else {
						String dataToWrite = userInput + "\r\n";
						if (verbose) {
							DumpUtil.displayDualDump(dataToWrite);
						} else {
							System.out.println(String.format("Writing: %s", userInput));
						}
						serial.write(dataToWrite); // <-- write to device
						// Wait for reply
						if (verbose) {
							System.out.println("Waiting for reply...");
						}
						// ... Move on!
						String reply = waitForResponse();
						if (reply.indexOf(NEW_LINE) > -1) {
							reply = reply.substring(0, reply.length() - NEW_LINE.length());
						}
						if (verbose) {
							DumpUtil.displayDualDump(reply);
						}
						System.out.println(String.format(">> Received [%s]", reply));
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			System.out.println("Out of the loop");
			serial.close();
			System.out.println("Serial is closed, bye!");
			System.exit(0);
		} catch (IOException ex) {
			System.out.println(" => Argh! : " + ex.getMessage());
			return;
		} catch (Throwable t) {
			if (t instanceof UnsatisfiedLinkError) {
				System.err.println("Not on a Pi maybe?");
			}
			t.printStackTrace();
			return;
		} finally {
			System.out.println("Bam.");
		}
	}

	public static void main(String... args) {
		SimpleSerialPI4JCommunication comm = new SimpleSerialPI4JCommunication();
		String portName = System.getProperty("port.name", "/dev/rfcomm0");
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
		comm.initComm(portName, br);
	}

}
