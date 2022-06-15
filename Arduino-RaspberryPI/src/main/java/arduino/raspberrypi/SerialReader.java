package arduino.raspberrypi;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import java.io.IOException;

public class SerialReader {
	// NMEA Style
	public static int calculateCheckSum(String str) {
		int cs = 0;
		char[] ca = str.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			cs ^= ca[i]; // XOR
//    System.out.println("\tCS[" + i + "] (" + ca[i] + "):" + Integer.toHexString(cs));
		}
		return cs;
	}

	// NMEA Style
	public static boolean validCheckSum(String data, boolean verb) {
		String sentence = data.trim();
		boolean b = false;
		try {
			int starIndex = sentence.indexOf("*");
			if (starIndex < 0) {
                return false;
            }
			String csKey = sentence.substring(starIndex + 1);
			int csk = Integer.parseInt(csKey, 16);
			String str2validate = sentence.substring(1, sentence.indexOf("*"));
			int calcCheckSum = calculateCheckSum(str2validate);
			b = (calcCheckSum == csk);
		} catch (Exception ex) {
			if (verb) {
                System.err.println("Oops:" + ex.getMessage());
            }
		}
		return b;
	}

	private final static String generateNMEAString(String payload, String prefix, String id) {
		if (prefix == null || prefix.length() != 2) {
            throw new IllegalArgumentException("Bad prefix [" + prefix + "], must be 2 character long.");
        }
		if (id == null || id.length() != 3) {
            throw new IllegalArgumentException("Bad ID [" + id + "], must be 3 character long.");
        }
		String nmea = prefix + id + "," + payload;
		int cs = calculateCheckSum(nmea);
		String cks = Integer.toString(cs, 16).toUpperCase();
		if (cks.length() < 2) {
            cks = "0" + cks;
        }
		nmea += ("*" + cks);
		return "$" + nmea;
	}

	public static void main_4tests(String... args) {
		String payload = "This is some bullshit.";
		String prefix = "OS";
		String id = "MSG";
		String sentence = generateNMEAString(payload, prefix, id);
		System.out.println(">>> Sentence [" + sentence + "] is " + (validCheckSum(sentence, false) ? "" : "NOT ") + "valid");

		sentence = generateNMEAString("3,Message from Arduino", prefix, id);
		System.out.println(sentence);
		sentence = "$OSMSG,3,Message from Arduino*61";
		System.out.println(">>> Sentence [" + sentence + "] is " + (validCheckSum(sentence, false) ? "" : "NOT ") + "valid");

		payload = "$OSMSG,LR,178*65";
		String content = payload.substring(7, payload.indexOf("*"));
		String[] sa = content.split(",");
		String strVal = sa[1];
		System.out.println("\tVal:" + strVal);
	}

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
		serial.addListener(event -> {
			// print out the data received to the console
			String payload;
			try {
				payload = event.getAsciiString();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			if (validCheckSum(payload, false)) {
                System.out.print("Arduino said:" + payload);
            } else {
                System.out.println("\tOops! Invalid String [" + payload + "]");
            }
		});

		try {
			// open the default serial port provided on the GPIO header
			System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
			try {
				serial.open(port, br);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			System.out.println("Port is opened.");

			Thread me = Thread.currentThread();
			synchronized (me) {
				me.wait();
			}
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}
