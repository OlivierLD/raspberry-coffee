package readserialport;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import java.util.Date;

public class SerialReader {
	public static void main(String... args)
			throws InterruptedException, NumberFormatException {
		String port = System.getProperty("serial.port", Serial.DEFAULT_COM_PORT);
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
		if (args.length > 0) {
			try {
				br = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}

		System.out.println("Serial Communication.");
		System.out.println(" ... connect using settings: " + Integer.toString(br) + ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				// print out the data received to the console
				System.out.print(/*"Read:\n" + */ event.getData());
			}
		});

		try {
			// open the default serial port provided on the GPIO header
			System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
			serial.open(port, br);
			System.out.println("Port is opened.");

			// continuous loop to keep the program running until the user terminates the program
			while (true) {
				if (serial.isOpen()) {
					System.out.println("Writing to the serial port...");
					try {
						// write a formatted string to the serial transmit buffer
						serial.write("CURRENT TIME: %s", new Date().toString());

						// write a individual bytes to the serial transmit buffer
						serial.write((byte) 13);
						serial.write((byte) 10);

						// write a simple string to the serial transmit buffer
						serial.write("Second Line");

						// write a individual characters to the serial transmit buffer
						serial.write('\r');
						serial.write('\n');

						// write a string terminating with CR+LF to the serial transmit buffer
						serial.writeln("Third Line");
					} catch (IllegalStateException ex) {
						ex.printStackTrace();
					}
				} else {
					System.out.println("Not open yet...");
				}
				// wait 1 second before continuing
				Thread.sleep(1_000);
			}
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}
