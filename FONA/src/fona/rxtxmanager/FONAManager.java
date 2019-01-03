package fona.rxtxmanager;

import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

/**
 * Important: On the Raspberry Pi, make sure you've run
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
 *
 * IMPORTANT:
 * ==========
 * This version uses Lib-RxTx for Java, and <b>not</b> the com.pi4j.io.serial package of PI4J.
 * As such, it could possibly run on other machines than the Raspberry Pi.
 *
 */
public class FONAManager implements SerialIOCallbacks {

	public enum NetworkStatus {
		NOT_REGISTERED          (0, "Not Registered"),
		REGISTERED_HOME         (1, "Registered (Home)"),
		NOT_REGISTERED_SEARCHING(2, "Not Registered (searching)"),
		DENIED                  (3, "Denied"),
		UNKNOWN                 (4, "Unknown"),
		REGISTERED_ROAMING      (5, "Registered (roaming)");

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

	public final static String CRLF                      = "\r\n";
//private final static String CRCRLF                   = "\r\r\n";

	public final static String CONNECTION_OK             = "AT" + CRLF + "OK" + CRLF;
	public final static String ATI_RESPONSE              = "ATI" + CRLF;
	public final static String DEBUG_ON_RESPONSE         = "AT+CMEE=2" + CRLF;
	public final static String BATTERY_RESPONSE          = "AT+CBC" + CRLF;
	public final static String SIGNAL_RESPONSE           = "AT+CSQ" + CRLF;
	public final static String SIM_CARD_RESPONSE         = "AT+CCID" + CRLF;
	public final static String NETWORK_NAME_RESPONSE     = "AT+COPS?" + CRLF;
	public final static String NETWORK_STATE_RESPONSE    = "AT+CREG?" + CRLF + "+CREG:";
	public final static String SET_TO_TEXT               = "AT+CMGF=1";
	public final static String SET_TO_TEXT_RESPONSE      = SET_TO_TEXT + CRLF;
	public final static String NUM_SMS_RESPONSE          = "AT+CPMS?" + CRLF;
	public final static String MESSAGE_PROMPT            = "AT+CMGS=\"";
	public final static String READ_SMS_RESPONSE         = "AT+CSDH=1" + CRLF;
	public final static String READ_SMS_CONTENT_RESPONSE = "AT+CMGR="; // + CRLF;
	public final static String SMS_DELETED_RESPONSE      = "AT+CMGD=";

	public final static String RECEIVED_SMS              = CRLF + "+CMTI:";
	public final static String SOMEONE_CALLING           = CRLF + "RING" + CRLF;

	private static SerialCommunicator serialCommunicator = null;

	private static String expectedNotification = "";
	private static Thread expectingNotification = null;

	public final static String CLTR_Z                    = "\032";              // 26, 0x1A, 032
	public final static String ACK                       = CRLF + "OK" + CRLF;  // QUESTION specially when entering a message.
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
		serialCommunicator.disconnect();
	}

	private static boolean connectionEstablished = false;

	private FONAClient parent;

	private boolean simulateSerial = false;

	public FONAManager(FONAClient parent) {
		this.parent = parent;

		simulateSerial = "true".equals(System.getProperty("simulate.serial"));

		/**
		 * Reads what FONA emits
		 * Identifies the full messages, and send them to {@link #manageFonaOutput(String)}
		 */
		Thread serialReader = new Thread(() -> {
			while (keepReading()) {
				try {
					while (/*true && */bufferIdx > 0) {

						if (getVerbose()) {
							String[] sa0 = DumpUtil.dualDump(fullMessage.toString());
							if (sa0 != null) {
								System.out.println("\t<<< [FONA] Received (top)...");
								for (String s : sa0) {
									System.out.println("\t\t" + s);
								}
							}
						} // verbose

						if (fullMessage.toString().endsWith(FONAManager.ACK) || fullMessage.toString().startsWith(FONAManager.MESSAGE_PROMPT)) {
							String mess = fullMessage.toString(); // Send the full message. Parsed later.
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
							manageFonaOutput(mess);
				//    delay(0.5f);
							resetSerialBuffer();
						} else if (fullMessage.toString().endsWith(FONAManager.ACK)) { // CRLF)) {
							String mess = fullMessage.toString(); // Send the full message. Parsed later.
							//  mess = mess.substring(0, mess.length() - ACK.length() - 1);
							if (getVerbose()) {
								System.out.println("   >> A Full message.");
							}
							manageFonaOutput(mess);
							resetSerialBuffer();
						} else if ((fullMessage.toString().startsWith(FONAManager.RECEIVED_SMS) || fullMessage.toString().startsWith(FONAManager.SOMEONE_CALLING)) &&
								fullMessage.toString().endsWith(FONAManager.CRLF)) {
							String mess = fullMessage.toString(); // Send the full message. Parsed later.
							//  mess = mess.substring(0, mess.length() - ACK.length() - 1);
							if (getVerbose()) {
								System.out.println("   >> Received.");
							}
							manageFonaOutput(mess);
							resetSerialBuffer();
						} else {
							if (getVerbose()) {
								System.out.println("No ACK, no PROMPT:");
								System.out.println("\t--> Available bytes:" + bufferIdx);
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
						//  delay(0.02f);
						if (bufferIdx == 0) {
							delay(0.5f);
						}
					}
					if (true || simulateSerial) {
						delay(0.5f); // Make sure this is not necessary when not simulating...
					}

				} catch (IllegalStateException ise) {
					// Not opened yet
				} catch (Exception ex) {
					System.err.println(ex.toString());
				}
			}
			System.out.println(">> Stop Reading FONA");
		});
		serialReader.start();
	}

	@Override
	public void connected(boolean b) {
		System.out.println("Connected: " + b);
	}

	private int bufferIdx = 0;
	private byte[] serialBuffer = new byte[1024]; // TODO See if this is necessary (seems it is not)

	private StringBuffer fullMessage = new StringBuffer();

	private void resetSerialBuffer() {
		bufferIdx = 0;
		synchronized (fullMessage) {
			fullMessage = new StringBuffer();
		}
	}

	@Override
	public void onSerialData(byte b) {

//	System.out.println(String.format("Receiving one byte >> %d 0x%s (bufferIdx=%d)", b, StringUtils.lpad(Integer.toHexString(b).toUpperCase(), 2, "0"), bufferIdx));

		serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
		fullMessage.append((char) (b & 0xFF));

		if (verbose) {
			String payload = fullMessage.substring(0, bufferIdx);
			try {
				String[] sa = DumpUtil.dualDump(payload);
				if (sa != null) {
					System.out.println("\t>>> [From FONA, onSerialData] Received:");
					for (String s : sa)
						System.out.println("\t\t" + s);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onSerialData(byte[] ba, int len) {
	}

	public boolean isConnected() {
		return connectionEstablished;
	}

	public void openSerial(String port, int br) {
		if (!simulateSerial) {
			serialCommunicator = new SerialCommunicator(this);
		} else {
			serialCommunicator = new SerialCommunicator(this, System.in, System.out);
		}
		serialCommunicator.setVerbose(false);

		Map<String, CommPortIdentifier> pm = serialCommunicator.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String serialport : ports) {
			System.out.println("-> " + serialport);
		}
		System.out.println("======================");

		// String serialPortName = port; // System.getProperty("serial.port", "/dev/ttyUSB0");
		System.out.println(String.format("Opening port %s:%d%s", port, br, (simulateSerial? " (Simulation)" : "")));

		CommPortIdentifier serialPort = null;
		if (!simulateSerial) {
			serialPort =	pm.get(port);
			if (serialPort == null) {
				String mess = String.format("Port %s not found, aborting", port);
				throw new RuntimeException(mess);
			}
		}
		try {
			serialCommunicator.connect(serialPort, "FONA", br); // Other values are defaulted
			boolean b = serialCommunicator.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			serialCommunicator.initListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isSerialOpen() {
		return serialCommunicator.isConnected();
	}

	public void dumpToSerial(String str) throws IOException {
		serialCommunicator.writeData(str);
		serialCommunicator.flushSerial();
	}

	public void readMessNum(int messNum) throws IOException {
		expectingNotification = Thread.currentThread();
		expectedNotification = SET_TO_TEXT;
		sendToFona(SET_TO_TEXT);
		synchronized (expectingNotification) {
			try {
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		expectingNotification = Thread.currentThread();
		//   delay(1);
		expectedNotification = "AT+CSDH=1";
		sendToFona("AT+CSDH=1");
		synchronized (expectingNotification) {
			try {
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose())
					System.out.println("Moving on!");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose()) {
					System.out.println("Moving on!");
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose()) {
					System.out.println("Moving on!");
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose()) {
					System.out.println("Moving on, enter message");
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
				if (getVerbose()) {
					System.out.println("     ... Waiting");
				}
				expectingNotification.wait(); // TODO Timeout?
				if (getVerbose()) {
					System.out.println("Moving on!");
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
		if (serialCommunicator.isConnected()) {
			try {
				if (getVerbose()) {
					System.out.println("Writing to FONA (" + payload.length() + " ch): [" + payload + "]");
					try {
						String[] sa = DumpUtil.dualDump(payload);
						if (sa != null) {
							System.out.println("\t>>> [FONA] Sent...");
							for (String s : sa) {
								System.out.println("\t\t" + s);
							}
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
					serialCommunicator.writeData((byte)payload.charAt(i));
					delay(BETWEEN_SENT_CHAR);                     // << The MOST important trick here
				}
				if (withCR) {
					serialCommunicator.writeData((byte)'\n');
					delay(BETWEEN_SENT_CHAR);
				}
				serialCommunicator.flushSerial();
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
	 * Aware of the FONA Protocol.
	 */
	public void manageFonaOutput(String mess) {
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
//      System.out.println("Signal:" + parsed[0] + " dB. Must be higher than 5, the higher the better.");
				this.parent.signalResponse(parsed[0]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (mess.startsWith(SIM_CARD_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
//      System.out.println("SIM Card # " + content);
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
				if (getVerbose()) {
					System.out.println("Releasing the waiter");
				}
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				if (getVerbose()) {
					System.out.println("Weird: no one is waiting...");
				}
			}
		} else if (mess.startsWith(NUM_SMS_RESPONSE)) {
			try {
				int start = mess.indexOf(CRLF) + CRLF.length();
				int end = mess.indexOf(CRLF, start + CRLF.length() + 1);
				String content = mess.substring(start, end);
				String[] parsed = content.substring("+CPMS: ".length()).split(",");
	//    System.out.println("Number of SMS :" + parsed[1]);
				this.parent.numberSMSResponse(Integer.parseInt(parsed[1]));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else if (mess.startsWith(READ_SMS_RESPONSE)) {
			if (expectingNotification != null) {
				if (getVerbose()) {
					System.out.println("Releasing the waiter");
				}
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				System.out.println("Weird: no one is waiting...");
			}
//    System.out.println("Enter number of message to read here:...");
		} else if (mess.startsWith(MESSAGE_PROMPT)) {
			if (getVerbose()) {
				System.out.println("Enter message prompt, received:" + mess);
			}
			// Release the waiting thread
			if (expectingNotification != null) {
				if (getVerbose()) {
					System.out.println("Releasing the waiter");
				}
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				System.out.println("Weird: no one is waiting...");
			}
		} else if (mess.startsWith(READ_SMS_CONTENT_RESPONSE)) {
			try {
				String payload = mess.substring(mess.indexOf(CRLF) + CRLF.length());
				if (getVerbose()) {
					System.out.println("Message Content:" + payload);
				}
				ReceivedSMS sms = new ReceivedSMS(payload);
	//    System.out.println("From " + sms.getFrom() + ", " + sms.getMessLen() + " char : " + sms.getContent());
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
				if (mn != -1) {
					this.parent.smsDeletedResponse(mn, success);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mess.startsWith(RECEIVED_SMS)) {
			try {
				String messNumStr = mess.substring(mess.lastIndexOf(",") + 1, mess.lastIndexOf(CRLF));
				if (true || getVerbose()) {
					System.out.println("Received message #" + messNumStr);
				}
//				delay(2f);
				int messToRead = Integer.parseInt(messNumStr.trim());
				this.parent.receivedSMS(messToRead);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (mess.startsWith(SOMEONE_CALLING)) {
	//  System.out.println("Someone is calling!..");
			this.parent.someoneCalling();
		} else { // The rest...
			if (expectingNotification != null) {
				if (getVerbose()) {
					System.out.println("Releasing the waiter");
				}
				synchronized (expectingNotification) {
					expectingNotification.notify();
				}
			} else {
				if (getVerbose()) {
					System.out.println("... No one is waiting on a lock.");
				}
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

	/**
	 *
	 * @param delay in seconds.
	 */
	public final static void delay(float delay) {
		try {
			Thread.sleep(Math.round(delay * 1_000L));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
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
