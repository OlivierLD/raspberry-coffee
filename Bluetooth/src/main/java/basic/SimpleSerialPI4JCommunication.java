package basic;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import utils.DumpUtil;
import utils.StaticUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static utils.TimeUtil.delay;

public class SimpleSerialPI4JCommunication {

	private final static String NEW_LINE = "\r\n"; // \n = 0xA, \r = 0xD
	private static boolean responseReceived = false;
	private static StringBuffer response = new StringBuffer();

	private static boolean verbose = "true".equals(System.getProperty("bt.verbose"));

	private static String waitForResponse() { // TODO Return String or byte[] ?

		synchronized (Thread.currentThread()) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		String resp = "";
		// Get the response
		if (responseReceived) {
			resp = response.toString();
			response.delete(0, response.length()); // Reset
			responseReceived = false;
		} else {
			System.out.println("Bizarre...");
		}
		return resp;
	}

	/**
	 * From scratch, not from the PI4J samples.
	 * Uses the Serial library from PI4J.
	 *
	 * This example program supports the following optional System variables:
	 * "port.name"                   [DEFAULT: /dev/rfcomm0]
	 * "baud.rate"                   [DEFAULT: 9600]
	 *
	 * A skeleton for further implementation.
	 * Use it for example with an Arduino and an HC-05 module, running `bluetooth.102.ino`
	 *
	 * @param args
	 */
	public static void main(String... args) {

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(event -> {
			// print out the data received to the console
			try {
				response.append(event.getAsciiString());
				if (verbose) {
					//	System.out.println(String.format("Current Buffer > [%s]", response.toString()));
					DumpUtil.displayDualDump(response.toString());
				}
				if (response.toString().endsWith(NEW_LINE)) {
					responseReceived = true;
					synchronized (Thread.currentThread()) {
						Thread.currentThread().notify();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		try {
			// create serial config object
			SerialConfig config = new SerialConfig(); // May display Error, but does not throw it.

			// set default serial settings (device, baud rate, flow control, etc)
			String portName = System.getProperty("port.name", "/dev/rfcomm0");
			String brStr =  System.getProperty("baud.rate", "9600");

			Baud br;
			try {
				br = Baud.getInstance(Integer.parseInt(brStr));
			} catch (NumberFormatException nfe) {
				System.err.println(String.format("Bad value for baud rate: %s, try again.", brStr));
				nfe.printStackTrace();
				return;
			}
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
						String reply = waitForResponse();
						if (verbose) {
							System.out.println(String.format(">> Received [%s]", reply));
						}
						DumpUtil.displayDualDump(reply);
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
}
