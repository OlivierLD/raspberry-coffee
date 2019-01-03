package alamode101;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.*;
import utils.DumpUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.StaticUtil.userInput;
import static utils.TimeUtil.delay;

/*
 * See the Arduino sketch named AlaModeTest.ino
 */
public class AlaModeTest {
	public final static int ALAMODE_ADDRESS = 0x2A; // See AlaModeTest.ino
	private static boolean verbose = false;

	private I2CBus bus;
	private I2CDevice alamode;

	final private Serial serial = SerialFactory.createInstance();

	public AlaModeTest() throws I2CFactory.UnsupportedBusNumberException {
		this(ALAMODE_ADDRESS);
	}

	public AlaModeTest(int address) throws I2CFactory.UnsupportedBusNumberException {
		try {
			// Get i2c bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPi version
			if (verbose) {
				System.out.println("Connected to bus. OK.");
			}
			// Get device itself
			alamode = bus.getDevice(address);
			if (verbose) {
				System.out.println("Connected to device. OK.");
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		// Serial part
		try {
			this.openSerialInput();
			this.startListening();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void startListening()
			throws NumberFormatException {
		// create and register the serial data listener
		serial.addListener(new SerialDataEventListener() {
			private StringBuffer fullMessage = new StringBuffer();
			private final String ACK = "\r\n";

			@Override
			public void dataReceived(SerialDataEvent event) {
				// print out the data received to the console
				String payload;
				try {
					payload = event.getAsciiString();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
				if (verbose) {
					System.out.println("\t<<< [Serial] Partial receive :[" + payload + "]");
					try {
						String[] sa = DumpUtil.dualDump(payload);
						if (sa != null) {
							System.out.println("\t<<< [Serial] Received...");
							for (String s : sa) {
								System.out.println("\t\t" + s);
							}
						}
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				}
				fullMessage.append(payload);
				if (fullMessage.toString().endsWith(ACK)) {
					//        System.out.println("Flushing...");
					String mess = fullMessage.toString(); // Send the full message. Parsed later.
					// Manage data here. Check in the enum ArduinoMessagePrefix
					try {
						mess = mess.trim();
						while (mess.endsWith("\n") || mess.endsWith("\r")) {
							mess = mess.substring(mess.length() - 1);
						}
						takeAction(mess);
					} catch (Exception e) {
						e.printStackTrace();
					}
					fullMessage = new StringBuffer();
				}
			}
		});
	}

	private void takeAction(String mess) throws Exception {
		System.out.println("\tFrom Serial <<< Command [" + mess + "]");
	}

	public void openSerialInput() throws IOException {
		String port = System.getProperty("serial.port", "/dev/ttyS0");
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));

		System.out.println("Serial Communication.");
//  System.out.println(" ... connect using settings: " + Integer.toString(br) +  ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
		serial.open(port, br);
		System.out.println("Port is opened.");
	}

	public void closeChannel() throws IOException {
		if (serial.isOpen())
			serial.close();
	}

	public void close() {
		try {
			this.bus.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		try {
			this.closeChannel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int readAlaMode()
			throws Exception {
		int r = alamode.read();
		return r;
	}

	public void writeAlaMode(byte b)
			throws Exception {
		alamode.write(b);
		delay(1L);
	}

	private static void displayMenu() {
		System.out.println("Verbose is " + (verbose ? "on" : "off"));
//  System.out.println("\nCommands are case-sensitive.");
		System.out.println("[?] Display menu");
		System.out.println("[Q] to quit");
		System.out.println("[V] to toggle verbose");
		System.out.println("[S] Send Serial Message");
		System.out.println("[T] Test SD Card");
		System.out.println("[R] Test I2C Read");
		System.out.println("[W] Test I2C Write");
	}

	private void sendSerial(String payload) throws IOException {
		if (serial.isOpen()) {
			if (verbose) {
				System.out.println("\t>>> Writing [" + payload + "] to the serial port...");
			}
			try {
				serial.write(payload); // + "\n");
//      serial.flush();
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("Not open yet...");
		}
	}
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		verbose = "true".equals(System.getProperty("alamode.debug", "false"));

		final NumberFormat NF = new DecimalFormat("##00.00");
		final AlaModeTest sensor = new AlaModeTest();

		try {
			System.out.println("Hit 'Q' to quit.");
			//    System.out.println("Hit [return] when ready to start.");
			//    userInput("");

			final Thread me = Thread.currentThread();
			Thread userInputThread = new Thread() {
				public void run() {
					displayMenu();
					boolean loop = true;
					while (loop) {
						String userInput = "";
						userInput = userInput("So? > ");
						if ("Q".equalsIgnoreCase(userInput)) {
							loop = false;
						} else if ("V".equalsIgnoreCase(userInput)) {
							verbose = !verbose;
						} else {
							//  channel.sendSerial(userInput); // Private
							if ("?".equals(userInput)) {
								displayMenu();
							} else if ("S".equalsIgnoreCase(userInput)) {
								if (false) {
									String mess = userInput("Enter a number > ");
									try {
										int n = Integer.parseInt(mess);
										sensor.sendSerial(mess);
									} catch (NumberFormatException nfe) {
										System.out.println("\nAn integer please...");
									} catch (IOException ioe) {
										System.out.println("Sending to Serial port:" + ioe.toString());
									}
								} else {
									String mess = userInput("Enter a message > ");
									try {
										sensor.sendSerial(mess);
									} catch (IOException ioe) {
										ioe.printStackTrace();
									}
								}
							} else if ("T".equalsIgnoreCase(userInput)) {
								String mess = "SD";
								try {
									sensor.sendSerial(mess);
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							} else if ("R".equalsIgnoreCase(userInput)) {
								int read = 0;
								try {
									read = sensor.readAlaMode();
								} catch (Exception ex) {
									System.err.println(ex.getMessage());
									ex.printStackTrace();
								}
								System.out.println("\tRead: " + NF.format(read));
							} else if ("W".equalsIgnoreCase(userInput)) {
								try {
									byte b = (byte) 'l'; // Lowercase L
									sensor.writeAlaMode(b);
									System.out.println("Wrote to AlaMode : 0x" + Integer.toHexString(b));
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							} else {
								System.out.println("Duh?");
							}
						}
					}
					synchronized (me) {
						me.notify();
					}
				}
			};
			userInputThread.start();

			synchronized (me) {
				try {
					me.wait();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			sensor.closeChannel();
		} catch (SerialPortException ex) {
			System.out.println(" ==>> Serial Setup failed : " + ex.getMessage());
			return;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		sensor.close();
		System.out.println("Bye!");
		System.exit(0);
	}
}
