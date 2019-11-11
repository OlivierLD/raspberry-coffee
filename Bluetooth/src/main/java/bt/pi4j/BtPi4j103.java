package bt.pi4j;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import com.pi4j.util.CommandArgumentParser;

import java.io.IOException;

public class BtPi4j103 {
	/**
	 * This example program supports the following optional command arguments/options:
	 * "--device (device-path)"                   [DEFAULT: /dev/ttyAMA0]
	 * "--baud (baud-rate)"                       [DEFAULT: 9600]
	 * "--data-bits (5|6|7|8)"                    [DEFAULT: 8]
	 * "--parity (none|odd|even)"                 [DEFAULT: none]
	 * "--stop-bits (1|2)"                        [DEFAULT: 1]
	 * "--flow-control (none|hardware|software)"  [DEFAULT: none]
	 *
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String... args) throws InterruptedException, IOException {

		// !! ATTENTION !!
		// By default, the serial port is configured as a console port
		// for interacting with the Linux OS shell.  If you want to use
		// the serial port in a software program, you must disable the
		// OS from using this port.
		//
		// Please see this blog article for instructions on how to disable
		// the OS console for this port:
		// https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(event -> {

			// NOTE! - It is extremely important to read the data received from the
			// serial port.  If it does not get read from the receive buffer, the
			// buffer will continue to grow and consume memory.

			// print out the data received to the console
			try {
				System.out.println("[HEX DATA]   " + event.getHexByteString());
				System.out.println("[ASCII DATA] " + event.getAsciiString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		try {
			// create serial config object
			SerialConfig config = new SerialConfig();

			// set default serial settings (device, baud rate, flow control, etc)
			//
			// by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
			// NOTE: this utility method will determine the default serial port for the
			//       detected platform and board/model.  For all Raspberry Pi models
			//       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
			//       model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
			//       environment configuration.
			config.device(SerialPort.getDefaultPort())
					.baud(Baud._9600)
					.dataBits(DataBits._8)
					.parity(Parity.NONE)
					.stopBits(StopBits._1)
					.flowControl(FlowControl.NONE);

			// parse optional command argument options to override the default serial settings.
			if (args.length > 0) {
				config = CommandArgumentParser.getSerialConfig(config, args);
			}

			// display connection details
			System.out.println("Let's get started");

			// open the default serial device/port with the configuration settings
			serial.open(config);

			boolean onOff = true;
			// continuous loop to keep the program running until the user terminates the program
			while (true) {
				try {
					// write a formatted string to the serial transmit buffer
					serial.write(onOff ? "0" : "1");
					onOff = !onOff;
				} catch (IllegalStateException ex) {
					ex.printStackTrace();
				}
				// wait 1 second before continuing
				Thread.sleep(1_000);
			}

		} catch (IOException ex) {
			System.out.println(" => Argh! : " + ex.getMessage());
			return;
		}
	}
}
