package bt.pi4j.demo;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;
import com.pi4j.util.CommandArgumentParser;
import utils.DumpUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static utils.TimeUtil.delay;

public class BtPi4j {
	/**
	 * This example program supports the following optional System variables:
	 * "port.name"                   [DEFAULT: /dev/ttyS0]
	 * "baud.rate"                   [DEFAULT: 9600]
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
			String portName = System.getProperty("port.name", "/dev/ttyS0");
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

			// display connection details
			System.out.println("Let's get started");

			// open the default serial device/port with the configuration settings
			serial.open(config);

			boolean onOff = true;
			// continuous loop to keep the program running until the user terminates the program
			final AtomicBoolean keepLooping = new AtomicBoolean(true);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				keepLooping.set(false);
				System.out.println("Exiting the loop");
				delay(1_000);
			}));
			while (keepLooping.get()) {
				try {
					// write a formatted string to the serial transmit buffer
					serial.write(onOff ? "0" : "1");
					onOff = !onOff;
				} catch (IllegalStateException ex) {
					ex.printStackTrace();
				}
				// wait 1 second before looping again
				Thread.sleep(1_000);
			}
			serial.close();

		} catch (IOException ex) {
			System.out.println(" => Argh! : " + ex.getMessage());
			return;
		} catch (Throwable t) {
			if (t instanceof java.lang.UnsatisfiedLinkError) {
				System.err.println("Not on a Pi maybe?");
			}
			t.printStackTrace();
			return;
		}
	}
}
