package arduino;

import gnu.io.NoSuchPortException;
import java.io.IOException;

/**
 * Connect an Arduino Uno with its USB cable.
 * Serial port (/dev/ttyUSB0 below) may vary.
 *
 * Interactive demo version.
 * Shows how to interact with ArduinoLoRaClient.
 * Enter a String from the command line,
 * Send it to the Arduino,
 *
 * See system properties:
 * "serial.port", default "/dev/ttyUSB0"
 * "baud.rate", default "9600"
 */
public class ArduinoLoRaInteractiveClient  {

	// This is the callback when LoRa returned some data.
	private static void onData(String str) {
		System.out.println(String.format("Received [%s]", str.trim()));
		// Manage potential errors.
		try {
			LoRaMessageManager.throwIfError(str);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {

		String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
		String baudRateStr = System.getProperty("baud.rate", "9600");
		try {
			int br = Integer.parseInt(baudRateStr);
			final ArduinoLoRaClient bridge = new ArduinoLoRaClient(serialPortName, br, ArduinoLoRaInteractiveClient::onData);

			System.out.println("Enter 'Q' at the prompt to quit.");
			try {
				boolean keepWorking = true;
				while (keepWorking) {
					String str = utils.StaticUtil.userInput("?> ");
					if ("Q".equalsIgnoreCase(str.trim())) {
						keepWorking = false;
					} else {
						bridge.sendToLora(str + "\n");
					}
				}
				System.out.println("Exiting.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				bridge.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} catch (NoSuchPortException nspe) {
			System.err.println(serialPortName + ": No Such Port");
			nspe.printStackTrace();
			if ("/dev/ttyACM0".equals(serialPortName)) {
				System.err.println("Note: There is some bug in libRxTx-java regarding the access to /dev/ttyACM0");
				System.err.println("If this is your case, try creating a symbolic link on the port, and access it through its link:");
				System.err.println(" $ sudo ln -s /dev/ttyACM0 /dev/ttyS80");
				System.err.println("Then try reading or writing on /dev/ttyS80");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Done.");
	}
}
