package arduino.raspberrypi;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import java.io.IOException;

/**
 * Reads the Arduino through its serial port.
 * Reads the data returned by the level_detector.ino.
 * Payload is a NMEA-like sentence, like $OSLVL,LEVEL,5,1022*0B
 */
public class SerialLevelReader {
	private static int previousLevel = -1;
	private static StringBuffer stream = null;

	public synchronized static void setPreviousLevel(int pl) {
		previousLevel = pl;
	}

	public synchronized static int getPreviousLevel() {
		return previousLevel;
	}

	public synchronized static void appendToStream(String str) {
		if (stream == null)
			stream = new StringBuffer();
		stream.append(str);
	}

	public synchronized static void processStream() {
		String str = stream.toString();
		String[] elem = str.split("\n");
		StringBuffer newStr = new StringBuffer();
		for (int i = 0; i < elem.length; i++) {
			String s = elem[i];
			if (validCheckSum(s, false)) {
				int level = parseMessage(s);
				if ("true".equals(System.getProperty("verbose", "false")))
					System.out.println("\tLevel is " + level);
				if (level != getPreviousLevel()) {
					System.out.println("Arduino said level is :" + level);
					setPreviousLevel(level);
				}
			} else {
				if (i == elem.length - 1)
					newStr.append(s);
				else
					System.err.println("\t>>> Oops! Invalid String [" + s + "]");
			}
		}
		// Reset
		stream = newStr;
	}

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
			if (starIndex < 0)
				return false;
			String csKey = sentence.substring(starIndex + 1);
			int csk = Integer.parseInt(csKey, 16);
			String str2validate = sentence.substring(1, starIndex); // sentence.indexOf("*"));
			int calcCheckSum = calculateCheckSum(str2validate);
			b = (calcCheckSum == csk);
		} catch (Exception ex) {
			if (verb) System.err.println("Oops:" + ex.getMessage());
		}
		return b;
	}

	/**
	 * Sample payload:
	 * $OSLVL,LEVEL,4,1021*09
	 * $OSLVL,LEVEL,0,0*3F
	 * $OSLVL,LEVEL,5,1023*0A
	 * $OSLVL,LEVEL,0,0*3F
	 * $OSLVL,LEVEL,5,1022*0B
	 * $OSLVL,LEVEL,3,1020*0F
	 * $OSLVL,LEVEL,4,1018*03
	 * $OSLVL,LEVEL,3,1019*05
	 * $OSLVL,LEVEL,2,1019*04
	 * $OSLVL,LEVEL,1,1020*0D
	 * $OSLVL,LEVEL,5,1021*08
	 * $OSLVL,LEVEL,1,1021*0C
	 * $OSLVL,LEVEL,0,0*3F
	 * $OSLVL,LEVEL,5,1023*0A
	 * $OSLVL,LEVEL,0,0*3F
	 * $OSLVL,LEVEL,4,1020*08
	 * $OSLVL,LEVEL,0,0*3F
	 * <p>
	 * The message is assumed to be valid
	 */
	private static int parseMessage(String message) {
		int level = -1;
		String[] data = message.split(",");
		try {
			level = Integer.parseInt(data[2]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return level;
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
		System.out.println(" ... connect on " + port + " using settings: " + Integer.toString(br) + ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					System.out.println("\n------------------------------------");
					System.out.println("Shutting down.");
					System.out.println("Closing Serial port.");
					serial.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					System.out.println("Bye.");
				}
			}, "Shutdown Hook"));

		// create and register the serial data listener
		serial.addListener(event -> {
			// print out the data received to the console
			String payload;
			try {
				payload = event.getAsciiString();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			if ("true".equals(System.getProperty("verbose", "false")))
				System.out.println("Payload [" + payload + "]");

			appendToStream(payload);
			processStream();
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
			System.out.println("------------------------------------");

			Thread me = Thread.currentThread();
			synchronized (me) {
				me.wait();
			}
		} catch (SerialPortException ex) {
			System.out.println(" ==>> Serial Setup Failed : " + ex.getMessage());
			ex.printStackTrace();
			return;
		}
	}
}
