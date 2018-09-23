package sample;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Connect an Arduino Uno with its USB cable.<br/>
 * Serial port (ttyUSB0 below) may vary.<br/>
 *<br/>
 * See system properties:
 * <ul>
 * <li><code>"serial.port"</code>, default <code>"/dev/ttyUSB0"</code></li>
 * <li><code>"baud.rate"</code>, default <code>"9600"</code></li>
 * </ul>
 *
 * Read & write to the Serial Port
 */
public class ArduinoEchoClient implements SerialIOCallbacks {
	@Override
	public void connected(boolean b) {
		System.out.println("Arduino connected: " + b);
	}

	private int lenToRead = 0;
	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[256];

	/**
	 * Receiver
	 * @param b one byte at a time
	 */
	@Override
	public void onSerialData(byte b) {
//  System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		if (b == 0xA) { // \n, NL => Message completed
			byte[] mess = new byte[bufferIdx];
			for (int i = 0; i < bufferIdx; i++) {
				mess[i] = serialBuffer[i];
			}
			arduinoOutput(mess); // See below
			// Reset
			lenToRead = 0;
			bufferIdx = 0;
		}
	}

	@Override
	public void onSerialData(byte[] b, int len) {
	}

	/**
	 * Invoked by {@link #onSerialData(byte)}
	 *
	 * @param mess
	 */
	public void arduinoOutput(byte[] mess) {
		if (true) { // verbose...
			try {
				String[] sa = DumpUtil.dualDump(mess);
				if (sa != null) {
					System.out.println("\t>>> [From Arduino] Received:");
					for (String s : sa) {
						System.out.println("\t\t" + s);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// Sample data
	private final static String[] LOREM_IPSUM = {
			"Lorem ipsum dolor sit amet, consectetuer adipiscing elit, ",
			"sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. ",
			"Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. ",
			"Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, ",
			"vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim ",
			"qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.",
			"Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat ",
			"facer possim assum. Typi non habent claritatem insitam; ",
			"est usus legentis in iis qui facit eorum claritatem. ",
			"Investigationes demonstraverunt lectores legere me lius quod ii legunt saepius. ",
			"Claritas est etiam processus dynamicus, qui sequitur mutationem consuetudium lectorum. ",
			"Mirum est notare quam littera gothica, quam nunc putamus parum claram, ",
			"anteposuerit litterarum formas humanitatis per seacula quarta decima et quinta decima. ",
			"Eodem modo typi, qui nunc nobis videntur parum clari, fiant sollemnes in futurum."
	};

	public static void main(String... args) {
		final ArduinoEchoClient mwc = new ArduinoEchoClient();
		final SerialCommunicator sc = new SerialCommunicator(mwc);
		sc.setVerbose(false);

		Map<String, CommPortIdentifier> pm = sc.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String port : ports) {
			System.out.println("-> " + port);
		}
		System.out.println("======================");

		String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
		String baudRateStr = System.getProperty("baud.rate", "9600");
		System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));
		CommPortIdentifier arduinoPort = pm.get(serialPortName);
		if (arduinoPort == null) {
			System.out.println(String.format("Port %s not found, aborting", serialPortName));
			System.exit(1);
		}
		System.out.println("---------------------");
		System.out.println("Lorem ipsum, inverted");
		System.out.println("---------------------");
		try {
			sc.connect(arduinoPort, "Arduino", Integer.parseInt(baudRateStr));
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();

			Thread.sleep(500L);
			// Wake up! 5 CR
			for (int i = 0; i < 5; i++) {
				sc.writeData("\n");
			}
			Thread.sleep(1_000L);

			System.out.println("Writing to the serial port.");
			for (String str : LOREM_IPSUM) {
				sc.writeData(str + "\n");
				Thread.sleep(500L);
			}
			System.out.println("Data written to the serial port.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			Thread.sleep(10_000L);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		try {
			sc.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Done.");
	}
}
