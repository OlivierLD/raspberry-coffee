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
				String[] sa = DumpUtil.dualDump(event.getBytes());
				if (sa != null) {
					System.out.println("\t>>> [From Bluetooth] Received:");
					for (String s : sa) {
						System.out.println("\t\t" + s);
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
			boolean verbose = "true".equals(System.getProperty("bt.verbose"));

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
				System.out.println("Exiting the loop");
				delay(1_000);
			}));
			while (keepLooping.get()) {

				try {
					String userInput = StaticUtil.userInput("(Q to quit) > "); // Blocking input
					if ("Q".equalsIgnoreCase(userInput)) {
						System.out.println("Exiting...");
						keepLooping.set(false);
					} else {
						String dataToWrite = userInput + "\r\n";
						if (verbose) {
							DumpUtil.displayDualDump(dataToWrite);
						} else {
							System.out.println(String.format("Writing: %s", userInput));
						}
						serial.write(dataToWrite); // <--
						// Wait for reply
						if (verbose) {
							System.out.println("Waiting for reply...");
						}
						if (false) { // TODO Implement a waitForResponse in the listener
							String reply = ""; // waitForResponse();
							if (verbose) {
								System.out.println(String.format(">> Received [%s]", reply));
								DumpUtil.displayDualDump(reply);
							}
						}
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			System.out.println("Out of the loop");
			serial.close();
			System.out.println("Serial is closed, bye!");

		} catch (IOException ex) {
			System.out.println(" => Argh! : " + ex.getMessage());
			return;
		} catch (Throwable t) {
			if (t instanceof UnsatisfiedLinkError) {
				System.err.println("Not on a Pi maybe?");
			}
			t.printStackTrace();
			return;
		}
	}
}
