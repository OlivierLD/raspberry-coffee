package fona.pi4jmanager;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.pi4j.io.serial.StopBits;
import utils.DumpUtil;

/**
 * Important: Make sure you've run
 * Prompt> rpi-serial-console disable (or its equivalent)
 * and re-booted.
 * =====================================
 * Wiring:
 * -------
 * FONA --- RPi
 * ---------------
 * Vio --- 3V3 #1, or 5V #2
 * <p>
 * Rx  --- TX  #8
 * Tx  --- RX  #10
 * <p>
 * Key -+
 *      |
 * GND -+- GND #6
 * ----------------
 * <p>
 * See https://learn.adafruit.com/adafruit-fona-mini-gsm-gprs-cellular-phone-module?view=all
 *
 * IMPORTANT NOTE:
 * ===============
 * In case you do not want - or cannot - use the UART port (/dev/ttyAMA0) it is easy to
 * use another port - like a USB slot. You just need a USB cable like the
 * one at https://www.adafruit.com/products/954.
 *
 * Hook-up the green wire of the USB cable on the FONA Rx, and
 * hook-up the white wire of the USB cable on the FONA Tx.
 *
 * You can as well use the VIn and the GND of the USB cable.
 * This would be another project, a FONA on its own board, with a USB Cable attached to it ;)
 */
public class FONAManager {
	public enum SerialOption {
		SERIAL_LISTENER_OPTION,
		SERIAL_READER_OPTION
	}

	public final static SerialOption SERIAL_OPTION = SerialOption.SERIAL_LISTENER_OPTION;

	public enum NetworkStatus {
		NOT_REGISTERED(0, "Not Registered"),
		REGISTERED_HOME(1, "Registered (Home)"),
		NOT_REGISTERED_SEARCHING(2, "Not Registered (searching)"),
		DENIED(3, "Denied"),
		UNKNOWN(4, "Unknown"),
		REGISTERED_ROAMING(5, "Registered (roaming)");

		private final int val;
		private final String label;

		NetworkStatus(int val, String label) {
			this.val = val;
			this.label = label;
		}

		public int val() {
			return this.val;
		}

		public String label() {
			return this.label;
		}
	}

	public final static String CRLF = "\r\n";
//private final static String CRCRLF                   = "\r\r\n";

	public final static String CONNECTION_OK = "AT" + CRLF + "OK" + CRLF;
	public final static String ATI_RESPONSE = "ATI" + CRLF;
	public final static String DEBUG_ON_RESPONSE = "AT+CMEE=2" + CRLF;
	public final static String BATTERY_RESPONSE = "AT+CBC" + CRLF;
	public final static String SIGNAL_RESPONSE = "AT+CSQ" + CRLF;
	public final static String SIM_CARD_RESPONSE = "AT+CCID" + CRLF;
	public final static String NETWORK_NAME_RESPONSE = "AT+COPS?" + CRLF;
	public final static String NETWORK_STATE_RESPONSE = "AT+CREG?" + CRLF + "+CREG:";
	public final static String SET_TO_TEXT = "AT+CMGF=1";
	public final static String SET_TO_TEXT_RESPONSE = SET_TO_TEXT + CRLF;
	public final static String NUM_SMS_RESPONSE = "AT+CPMS?" + CRLF;
	public final static String MESSAGE_PROMPT = "AT+CMGS=\"";
	public final static String READ_SMS_RESPONSE = "AT+CSDH=1" + CRLF;
	public final static String READ_SMS_CONTENT_RESPONSE = "AT+CMGR="; // + CRLF;
	public final static String SMS_DELETED_RESPONSE = "AT+CMGD=";

	public final static String RECEIVED_SMS = CRLF + "+CMTI:";
	public final static String SOMEONE_CALLING = CRLF + "RING" + CRLF;

	private final static Serial serial = SerialFactory.createInstance();

	private static String expectedNotification = "";
	private static Thread expectingNotification = null;

	public final static String CLTR_Z = "\032";           // 26, 0x1A, 032
	public final static String ACK = CRLF + "OK" + CRLF;  // QUESTION specially when entering a message.
	private final static float BETWEEN_SENT_CHAR = 0.001F;

	private static boolean verbose = "true".equals(System.getProperty("fona.verbose", "false"));

	public static void setVerbose(boolean b) {
		verbose = b;
	}

	public static boolean getVerbose() {
		return verbose;
	}

	private static boolean readSerial = true;

	public boolean keepReading() {
		return readSerial;
	}

	public void stopReading() {
		readSerial = false;
	}

	public void closeSerial() throws IOException {
		serial.close();
	}

	private static boolean connectionEstablished = false;

	private FONAClient parent;

	public FONAManager(FONAClient parent) {
		this.parent = parent;
		if (SERIAL_OPTION.equals(SerialOption.SERIAL_READER_OPTION)) {
			// Option: serial.read() in its own thread...
			Thread serialReader = new Thread() {
				private StringBuffer fullMessage = new StringBuffer();

				public void run() {
					while (keepReading()) {
						try {
							while (true && serial.available() > 0) {
								CharBuffer cb = serial.read(Charset.defaultCharset());
								fullMessage.append(cb);

								String[] sa0 = DumpUtil.dualDump(fullMessage.toString());
								if (sa0 != null) {
									System.out.println("\t<<< [FONA] Received (top)...");
									for (String s : sa0) {
										System.out.println("\t\t" + s);
									}
								}

//                char c = (char)serial.read();
//                c &= 0xFF;
//
//                if ((c & 0xFF) != 0xFF)
//                  fullMessage.append(c);

								if (fullMessage.toString().endsWith(FONAManager.ACK) || fullMessage.toString().startsWith(FONAManager.MESSAGE_PROMPT)) {
									String mess = fullMessage.toString(); // Send the full message. Parsed later.
									//  mess = mess.substring(0, mess.length() - ACK.length() - 1);
									if (getVerbose()) {
										try {
											String[] sa = DumpUtil.dualDump(mess);
											if (sa != null) {
												System.out.println("\t<<< [FONA] Received...");
												for (String s : sa)
													System.out.println("\t\t" + s);
											}
										} catch (Exception ex) {
											System.out.println(ex.toString());
										}
									}
									fonaOutput(mess);
									//    delay(0.5f);
									fullMessage = new StringBuffer(); // Reset
								}
								//  delay(0.02f);
								if (serial.available() == 0)
									delay(0.5f);
							}
							//   delay(0.5f);
						} catch (IllegalStateException ise) {
							// Not opened yet
						} catch (Exception ex) {
							System.err.println(ex.toString());
						}
					}
				}
			};
			serialReader.start();
		}

		if (SERIAL_OPTION.equals(SerialOption.SERIAL_LISTENER_OPTION)) {
			// create and register the serial data listener
			final SerialDataEventListener sdl = new SerialDataEventListener() {
				private StringBuffer fullMessage = new StringBuffer();

				@Override
				public void dataReceived(SerialDataEvent event) {
					// print out the data received to the console
					String payload = null;
					try {
						payload = event.getAsciiString();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					if (getVerbose()) {
						try {
							String[] sa = DumpUtil.dualDump(payload);
							if (sa != null) {
								System.out.println("\t<<< [FONA] Received...");
								for (String s : sa)
									System.out.println("\t\t" + s);
							}
						} catch (Exception ex) {
							System.out.println(ex.toString());
						}
					}

					fullMessage.append(payload);
					if (fullMessage.toString().endsWith(FONAManager.ACK) || (fullMessage.toString().startsWith(FONAManager.MESSAGE_PROMPT))) // && fullMessage.toString().endsWith(CRLF)))
					{
						String mess = fullMessage.toString(); // Send the full message. Parsed later.
						if (getVerbose())
							System.out.println("   >> It is a " + (fullMessage.toString().endsWith(FONAManager.ACK) ? "ACK" : "Prompt"));
						fonaOutput(mess);
						// Reset
						fullMessage = new StringBuffer();
					} else if (fullMessage.toString().endsWith(FONAManager.CRLF)) {
						String mess = fullMessage.toString(); // Send the full message. Parsed later.
						//  mess = mess.substring(0, mess.length() - ACK.length() - 1);
						if (getVerbose()) {
							System.out.println("   >> A Full message.");
						}
						fonaOutput(mess);
						// Reset
						fullMessage = new StringBuffer();
					} else {
						if (getVerbose()) {
							System.out.println("No ACK, no PROMPT:");
							try {
								System.out.println("\t--> Available bytes:" + serial.available());
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
							try {
								String[] sa = DumpUtil.dualDump(fullMessage.toString());
								if (sa != null) {
									System.out.println("\t<<< [FONA] Received...");
									for (String s : sa)
										System.out.println("\t\t" + s);
								}
							} catch (Exception ex) {
								System.out.println(ex.toString());
							}
						}
						//   fonaOutput(fullMessage.toString());
					}
				}
			};
			serial.addListener(sdl);
		}
	}

	public boolean isConnected() {
		return connectionEstablished;
	}


	private static Baud getBaudRate(int br) {
		Baud baud = null;
		for (Baud b : Baud.values()) {
			if (b.getValue() == br) {
				baud = b;
				break;
			}
		}
		return baud;
	}

	public void openSerial(String port, int br) throws IOException {
		SerialConfig config = new SerialConfig();
		config.device(port)
						.baud(getBaudRate(br))
						.dataBits(DataBits._8)
						.parity(Parity.NONE)
						.stopBits(StopBits._1)
						.flowControl(FlowControl.NONE);
		serial.open(config);
	}

	public boolean isSerialOpen() {
		return serial.isOpen();
	}

	public void dumpToSerial(String str) throws IOException {
		serial.write(str);
		serial.flush();
	}

	public void readMessNum(int messNum) throws IOException {
		expectingNotification = Thread.currentThread();
		expectedNotification = SET_TO_TEXT;
		sendToFona(SET_TO_TEXT);
		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = Thread.currentThread();
		//   delay(1);
		expectedNotification = "AT+CSDH=1";
		sendToFona("AT+CSDH=1");
		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = null;

		delay(1f);
		String readCmd = "AT+CMGR=" + Integer.toString(messNum);
		sendToFona(readCmd);
	}

	public void requestModuleNameAndRevision() throws IOException {
		sendToFona("ATI");
	}

	public void requestDebug() throws IOException {
		sendToFona("AT+CMEE=2");
	}

	public void requestSimCCID() throws IOException {
		sendToFona("AT+CCID");
	}

	public void requestBatteryLevel() throws IOException {
		sendToFona("AT+CBC");
	}

	public void requestNetworkName() throws IOException {
		sendToFona("AT+COPS?");
	}

	public void requestRSSI() throws IOException {
		sendToFona("AT+CSQ");
	}

	public void requestNetworkStatus() throws IOException {
		sendToFona("AT+CREG?");
	}

	public void requestNumberOfSMS() throws IOException {
		// Wait (notification) then send AT+CPMS?
		expectingNotification = Thread.currentThread();
		expectedNotification = SET_TO_TEXT;
		sendToFona(SET_TO_TEXT);
		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = null;
		delay(1);
		sendToFona("AT+CPMS?");
	}

	public void sendSMS(String to, String content) throws IOException {
		// Wait (notification) then send AT+CPMS?
		expectingNotification = Thread.currentThread();
		expectedNotification = SET_TO_TEXT;
		sendToFona(SET_TO_TEXT);
		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = null;
		// Prompt for number here
		String sendTo = to;
		expectingNotification = Thread.currentThread();
//  expectedNotification = "AT+CMGS=\"";
		sendToFona("AT+CMGS=\"" + sendTo + "\""); // , false);
		// Here expect a "> " prompt... Unreceived so far.

		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on, enter message");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = null;
//  delay(1);
		String messagePayload = content; // userInput("  Mess Content?> ");
	  /*
     * TASK Compare to
    mySerial->println(smsmsg);
    mySerial->println();
    mySerial->write(0x1A);
     */
		sendToFona(messagePayload);
		delay(1);
		sendToFona(CLTR_Z, false);  // \032 = Ctrl^Z = 26 = 0x1A
	}

	private final static NumberFormat MN_FMT = new DecimalFormat("000");

	public void deleteSMS(int messNum) throws IOException {
		String first = "AT+CMGF=1";
		expectingNotification = Thread.currentThread();
		expectedNotification = first;
		sendToFona(first);
		synchronized (expectingNotification) {
			try {
				if (getVerbose())
					System.out.println("     ... Waiting");
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
			}
		}
		expectingNotification = null;
		delay(1);
		sendToFona("AT+CMGD=" + MN_FMT.format(messNum));
	}

	public void readAllSMS() {
		// TODO Implement
	}

	private static void sendToFona(String payload) throws IOException {
		sendToFona(payload, true);
	}

	private static void sendToFona(String payload, boolean withCR) throws IOException {
		if (serial.isOpen()) {
			try {
				if (getVerbose()) {
					System.out.println("Writing to FONA (" + payload.length() + " ch): [" + payload + "]");
					try {
						String[] sa = DumpUtil.dualDump(payload);
						if (sa != null) {
							System.out.println("\t>>> [FONA] Sent...");
							for (String s : sa)
								System.out.println("\t\t" + s);
						}
					} catch (Exception ex) {
						System.out.println(ex.toString());
					}
				}
        /* See below...
        if (withCR)
          serial.writeln(payload);
        else
          serial.write(payload);
        */
				for (int i = 0; i < payload.length(); i++) {
					serial.write(payload.charAt(i));
					delay(BETWEEN_SENT_CHAR);                     // << The MOST important trick here
				}
				if (withCR) {
					serial.write('\n');
					delay(BETWEEN_SENT_CHAR);
				}
				serial.flush();
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void tryToConnect() throws IOException {
		sendToFona("AT");
	}

	/*
	 * Data received from FONA
	 */
	public void fonaOutput(String mess) {
		if (mess.equals(CONNECTION_OK)) {
			connectionEstablished = true;
			this.parent.fonaConnected();
		} else if (mess.startsWith(ATI_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				this.parent.moduleNameAndRevision(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(DEBUG_ON_RESPONSE)) {
//    System.out.println("Debug is ON");
			this.parent.debugOn();
		} else if (mess.startsWith(BATTERY_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				String[] parsed = content.substring("+CBC: ".length()).split(",");
				//     System.out.println("Load:" + parsed[1] + "%, " + parsed[2] + " mV");
				this.parent.batteryResponse(parsed[1], parsed[2]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (mess.startsWith(SIGNAL_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				String[] parsed = content.substring("+CSQ: ".length()).split(",");
				//  System.out.println("Signal:" + parsed[0] + " dB. Must be higher than 5, the higher the better.");
				this.parent.signalResponse(parsed[0]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (mess.startsWith(SIM_CARD_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				//  System.out.println("SIM Card # " + content);
				this.parent.simCardResponse(content);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(NETWORK_NAME_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				String[] parsed = content.substring("+COPS: ".length()).split(",");
				if (parsed != null && parsed.length > 2) {
					//  System.out.println("Network:" + parsed[2]);
					this.parent.networkNameResponse(parsed[2]);
				} else
					System.out.println("Invalid Network Name response");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(SET_TO_TEXT_RESPONSE)) {
			// Release the waiting thread
			if (expectingNotification != null) {
				if (getVerbose())
					System.out.println("Releasing the waiter");
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				if (getVerbose())
					System.out.println("Weird: no one is waiting...");
			}
		} else if (mess.startsWith(NUM_SMS_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				String[] parsed = content.substring("+CPMS: ".length()).split(",");
				//  System.out.println("Number of SMS :" + parsed[1]);
				this.parent.numberSMSResponse(Integer.parseInt(parsed[1]));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else if (mess.startsWith(READ_SMS_RESPONSE)) {
			if (expectingNotification != null) {
				if (getVerbose())
					System.out.println("Releasing the waiter");
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				System.out.println("Weird: no one is waiting...");
			}
//    System.out.println("Enter number of message to read here:...");
		} else if (mess.startsWith(MESSAGE_PROMPT)) {
			if (getVerbose())
				System.out.println("Enter message prompt, received:" + mess);
			// Release the waiting thread
			if (expectingNotification != null) {
				if (getVerbose())
					System.out.println("Releasing the waiter");
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				System.out.println("Weird: no one is waiting...");
			}
		} else if (mess.startsWith(READ_SMS_CONTENT_RESPONSE)) {
			try {
				String payload = mess.substring(mess.indexOf(CRLF) + CRLF.length());
				if (getVerbose())
					System.out.println("Message Content:" + payload);
				ReceivedSMS sms = new ReceivedSMS(payload);
				//   System.out.println("From " + sms.getFrom() + ", " + sms.getMessLen() + " char : " + sms.getContent());
				this.parent.readSMS(sms);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(NETWORK_STATE_RESPONSE)) {
			try {
				String str = mess.substring(mess.indexOf(",") + 1, mess.indexOf(CRLF + CRLF)).trim();
				int ns = Integer.parseInt(str);
				NetworkStatus nStat = null;
				for (NetworkStatus netStat : NetworkStatus.values()) {
					if (netStat.val() == ns) {
						nStat = netStat;
						break;
					}
				}
				this.parent.networkStatusResponse(nStat);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else if (mess.startsWith(SMS_DELETED_RESPONSE)) {
			try {
				String num = mess.substring(SMS_DELETED_RESPONSE.length(), mess.indexOf((CRLF)));
				boolean success = mess.indexOf(CRLF + "OK" + CRLF) > 0;
				int mn = -1;
				try {
					mn = Integer.parseInt(num);
				} catch (NumberFormatException nfe) {
					System.err.println("Error:");
					System.err.println("Original mess [" + mess + "], num:[" + num + "]");
					nfe.printStackTrace();
				}
				if (mn != -1)
					this.parent.smsDeletedResponse(mn, success);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(RECEIVED_SMS)) {
			try {
				String messNumStr = mess.substring(mess.lastIndexOf(",") + 1, mess.lastIndexOf(CRLF));
				if (getVerbose())
					System.out.println("Received message #" + messNumStr);
				delay(2f);
				int messToRead = Integer.parseInt(messNumStr.trim());
				this.parent.receivedSMS(messToRead);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (mess.startsWith(SOMEONE_CALLING)) {
			//  System.out.println("Someone is calling!..");
			this.parent.someoneCalling();
		} else // The rest...
		{
			if (expectingNotification != null) {
				if (getVerbose())
					System.out.println("Releasing the waiter");
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				if (getVerbose())
					System.out.println("... No one is waiting on a lock.");
			}
			if (getVerbose()) {
				try {
					String[] usa = DumpUtil.dualDump(mess);
					if (usa != null) {
						System.out.println("\t<<< [FONA] Unknown format for received data...");
						for (String s : usa)
							System.out.println("\t\t" + s);
					}
				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
		}
	}

	public final static void delay(float delay) {
		try {
			Thread.sleep(Math.round(delay * 1_000L));
		} catch (InterruptedException ie) {
		}
	}

	public static class ReceivedSMS {
		/*
		 * Sample:
		 *    +CMGR: "REC READ","+4153505547","","15/11/14,08:57:42-32",145,4,0,0,"+12063130057",145,7\r\nRe-sent\r\n\r\nOK\r\n
		 *    0                1             2  3         5            5   6 7 8 9              10  11
		 */
		private String from = null;
		private String at = null;
		private int len = 0;
		private String content = null;

		public ReceivedSMS(String raw) {
			String[] sa = raw.split(",");
			if (sa != null && sa.length > 11) {
				this.from = stripQuotes(sa[1].trim());
				this.at = stripQuotes(sa[3].trim()) + "," + stripQuotes(sa[4].trim());
				String payload = sa[11].trim();
				String[] cp = payload.split(CRLF);
				this.content = cp[1];
				try {
					this.len = Integer.parseInt(cp[0]);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}

		public String getFrom() {
			return this.from;
		}

		public String getDate() {
			return this.at;
		}

		public int getMessLen() {
			return this.len;
		}

		public String getContent() {
			return this.content;
		}

		private String stripQuotes(String str) {
			String s = str;
			if (s.startsWith("\""))
				s = s.substring(1);
			if (s.endsWith("\""))
				s = s.substring(0, s.length() - 1);
			return s;
		}
	}
}
