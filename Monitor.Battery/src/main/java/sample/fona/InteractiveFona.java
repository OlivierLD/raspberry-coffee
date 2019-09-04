package sample.fona;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialPortException;
import fona.pi4jmanager.FONAClient;
import fona.pi4jmanager.FONAManager;
import fona.pi4jmanager.FONAManager.NetworkStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

import static utils.StaticUtil.userInput;

public class InteractiveFona implements FONAClient {
	private static void displayMenu() {
		System.out.println("> Commands are case-sensitive.");
		System.out.println("Verbose is " + (FONAManager.getVerbose() ? "on" : "off"));
		System.out.println("-- Q to Quit, V for Verbose --");
		System.out.println("[?] Print this menu");
		System.out.println("[D] Turn DEBUG on");
		System.out.println("[M] Module name and revision");
		System.out.println("[b] Read the battery V");
		System.out.println("[C] Read the SIM CCID");
		System.out.println("[I] Network Status");
		System.out.println("[i] Read RSSI (signal strength)");
		System.out.println("[n] Get network name");
		System.out.println("[N] Number of SMSs");
		System.out.println("[r] Read SMS #");
		System.out.println("[R] Read All SMSs");
		System.out.println("[d] Delete SMS #");
		System.out.println("[s] Send SMS");
		System.out.println("------------------------------");
	}

	private static int messToRead = -1;
	private static FONAManager fona;

	private final static void reprompt() {
		System.out.print("FONA> ");
	}

	public static void main(String... args)
					throws InterruptedException, NumberFormatException, IOException {
		// Display current Classpath
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}

		InteractiveFona sf = new InteractiveFona();
		fona = new FONAManager(sf);

		String verbose = System.getProperty("verbose");
		if (verbose != null && "true".equals(verbose)) {
			fona.setVerbose(true);
		}

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
		System.out.println(" ... connect using port " + port + ":" + Integer.toString(br)); // +  ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		// final Serial serial = SerialFactory.createInstance();
		try {
			System.out.println("Hit 'Q' to quit.");
			System.out.println("Hit 'V' to toggle verbose on/off.");
			userInput("Hit [return] when ready to start."); // Usefull when connecting a remote debugger.

			System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
			fona.openSerial(port, br);

			System.out.println("Port is opened.");

			final Thread me = Thread.currentThread();
			Thread userInputThread = new Thread() {
				public void run() {
					System.out.println("Establishing connection (can take up to 3 seconds).");
					while (!fona.isConnected()) {
//            System.out.println(">>>> Trying to connect...");
						try {
							fona.tryToConnect();
						} catch (IOException ioe) {
							throw new RuntimeException(ioe);
						}
						if (!fona.isConnected()) {
							FONAManager.delay(1);
						}
					}
					System.out.println("Connection established.");
					displayMenu();
					boolean loop = true;
					while (loop) {
						String userInput = "";
						if (messToRead == -1)
							userInput = userInput("FONA> ");
						else {
							FONAManager.delay(1);
							userInput = "r"; // FONA received a message
							System.out.println("\t\t>>> Automated read");
						}
						if ("Q".equalsIgnoreCase(userInput))
							loop = false;
						else if ("V".equalsIgnoreCase(userInput)) {
							FONAManager.setVerbose(!FONAManager.getVerbose());
							System.out.println("Verbose is now " + (FONAManager.getVerbose() ? "on" : "off") + ".");
						} else if ("?".equalsIgnoreCase(userInput) || "".equalsIgnoreCase(userInput))
							displayMenu();
						else if (!userInput.trim().isEmpty()) {
							if (fona.isSerialOpen()) {
								String cmd = "";
								try {
									if ("M".equals(userInput)) // Module Name and Revision
										fona.requestModuleNameAndRevision();
									else if ("D".equals(userInput)) // Debug
										fona.requestDebug();
									else if ("C".equals(userInput)) // Read SIM CCID
										fona.requestSimCCID();
									else if ("b".equals(userInput)) // Battery
										fona.requestBatteryLevel();
									else if ("n".equals(userInput)) // Network Name
										fona.requestNetworkName();
									else if ("I".equals(userInput)) // Network Status
										fona.requestNetworkStatus();
									else if ("i".equals(userInput)) // RSSI Signal strength
										fona.requestRSSI();
									else if ("N".equals(userInput)) // Number of SMS
										fona.requestNumberOfSMS();
									else if ("r".equals(userInput)) // Read mess num x
									{
										if (messToRead == -1) {
											String str = userInput("  Mess Num? (return to cancel) > ");
											try {
												messToRead = Integer.parseInt(str.trim());
											} catch (NumberFormatException nfe) {
												System.out.println("...Cancelling.");
											}
										}
										if (messToRead != -1) {
											fona.readMessNum(messToRead);
										}
										messToRead = -1;
									} else if ("s".equals(userInput)) // Send SMS
									{
										System.out.println("> Note: Enter [Return] at the prompt to cancel the 'send SMS' operation <");
										String sendTo = userInput("  Send messsage to (like 14153505547) ?> ");
										if (!sendTo.trim().isEmpty()) {
											if (FONAManager.getVerbose())
												System.out.println("Sending message to " + sendTo);
											String messagePayload = userInput("  Mess Content (140 char max)?         > ");
											if (!messagePayload.trim().isEmpty()) {
												fona.sendSMS(sendTo, messagePayload);
												System.out.println("Sent.");
											} else {
												System.out.println("... Canceled.");
											}
										} else {
											System.out.println("... Canceled.");
										}
									} else if ("R".equals(userInput))  // Read all messages
										System.out.println("Operation ot available yet...");
									else if ("d".equals(userInput))  // Delete message #x
									{
										String num = userInput("  Delete messsage # (return to cancel) ?> ");
										int messNum = -1;
										try {
											messNum = Integer.parseInt(num);
											fona.deleteSMS(messNum);
										} catch (NumberFormatException nfe) {
											System.out.println("...Cancelling.");
										}
									} else { // Whatever is not implemented... out of sync, whatever.
										cmd = userInput;
										if (FONAManager.getVerbose())
											System.out.println("\tWriting [" + cmd + "] to the serial port...");
										try {
											fona.dumpToSerial(cmd + "\n");
										} catch (IllegalStateException ex) {
											ex.printStackTrace();
										}
									}
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							} else {
								System.out.println("Not open yet...");
							}
						}
					}
					synchronized (me) {
						me.notify();
					}
				}
			};
			userInputThread.start();

			// Debug thread... simulates fona output
			if (false) {
				Thread simulator = new Thread() {
					public void run() {
						while (true) {
							FONAManager.delay(10F);
							fona.fonaOutput(FONAManager.CRLF + "+CMTI: \"ME\",77" + FONAManager.CRLF);
						}
					}
				};
				simulator.start();
			}

			synchronized (me) {
				me.wait();
			}
			System.out.println("Bye!");
			fona.stopReading();
			fona.closeSerial();
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
		System.exit(0);
	}

	@Override
	public void networkStatusResponse(NetworkStatus ns) {
		System.out.println(ns.label());
		reprompt();
	}

	@Override
	public void smsDeletedResponse(int sms, boolean ok) {
		System.out.println("Message #" + sms + " deleted:" + (ok ? "OK" : "Failed"));
		reprompt();
	}

	@Override
	public void receivedSMS(final int sms) {
		if (false) {
			messToRead = sms;
			System.out.println(">>> Message received, hit [Return] to read it.");
		} else {
			Thread readit = new Thread() {
				public void run() {
					try {
						fona.readMessNum(sms);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			};
			readit.start();

			Thread deleteit = new Thread() {
				public void run() {
					FONAManager.delay(10f);
					System.out.println("\t\t>>>> Deleting mess #" + sms);
					try {
						fona.deleteSMS(sms);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			};
			deleteit.start();
		}
	}

	@Override
	public void fonaConnected() {
		System.out.println("FONA Connected!");

	}

	@Override
	public void moduleNameAndRevision(String str) {
		System.out.println("Module:" + str);
		reprompt();
	}

	@Override
	public void debugOn() {
		System.out.println("Debug ON");
		reprompt();
	}

	@Override
	public void batteryResponse(String percent, String mv) {
		System.out.println("Load:" + percent + "%, " + mv + " mV");
		reprompt();
	}

	@Override
	public void signalResponse(String s) {
		System.out.println("Signal:" + s + " dB. Must be higher than 5, the higher the better.");
		reprompt();
	}

	@Override
	public void simCardResponse(String s) {
		System.out.println("SIM Card # " + s);
		reprompt();
	}

	@Override
	public void networkNameResponse(String s) {
		System.out.println("Network:" + s);
		reprompt();
	}

	@Override
	public void numberSMSResponse(int n) {
		System.out.println("Number of SMS :" + n);
		reprompt();
	}

	@Override
	public void readSMS(final FONAManager.ReceivedSMS sms) {
		System.out.println("From " + sms.getFrom() + ", " + sms.getMessLen() + " char : " + sms.getContent());
		reprompt();
	}

	@Override
	public void someoneCalling() {
		System.out.println("Dring dring!");
		reprompt();
	}
}
