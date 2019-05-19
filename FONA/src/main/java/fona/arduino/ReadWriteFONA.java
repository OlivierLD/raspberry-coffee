package fona.arduino;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Write data, from the Raspberry to the Arduino/FONA, through the serial port.
 * Receives a response from the Arduino/FONA.
 *
 *  Commands (sent to FONA/Arduino) are:

  a   read the ADC 2.8V max (FONA800 & 808)
  b   read the Battery V and % charged
  C   read the SIM CCID
  i   read RSSI
  n   read network status

  N   Number of SMSs
  r|x Read SMS # x
  d|x Delete SMS # x
  s|(dest number)|(mess payload) Send SMS payload (mess payload) to (dest number)

  Message received: +CMTI: "SM",3 <- where 3 is the number of the message just received.
 */
public class ReadWriteFONA
{
  private FONAClient caller;

  private static Method GENERIC_FAILURE_PARSER;
  private static Method GENERIC_SUCCESS_PARSER;
  private static Method INCOMING_MESSAGE_MANAGER;
  private static Method READY;
  private static Method BATTERY_PARSER;
  private static Method ADC_PARSER;
  private static Method CCID_PARSER;
  private static Method RSSI_PARSER;
  private static Method NETWORK_PARSER;
  private static Method NUM_MESS_PARSER;
  private static Method MESS_PARSER;
  private static Method ON_SENT_OK;
  // All methods have the same signature: void function(String). <= Consumer<String>
  static
  {
    try { GENERIC_FAILURE_PARSER   = ReadWriteFONA.class.getMethod("genericFailureParser",   String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { GENERIC_SUCCESS_PARSER   = ReadWriteFONA.class.getMethod("genericSuccessParser",   String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { INCOMING_MESSAGE_MANAGER = ReadWriteFONA.class.getMethod("incomingMessageManager", String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { READY                    = ReadWriteFONA.class.getMethod("ready",                  String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { BATTERY_PARSER           = ReadWriteFONA.class.getMethod("batteryParser",          String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { ADC_PARSER               = ReadWriteFONA.class.getMethod("adcParser",              String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { CCID_PARSER              = ReadWriteFONA.class.getMethod("ccidParser",             String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { RSSI_PARSER              = ReadWriteFONA.class.getMethod("rssiParser",             String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { NETWORK_PARSER           = ReadWriteFONA.class.getMethod("networkParser",          String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { NUM_MESS_PARSER          = ReadWriteFONA.class.getMethod("numberOfMessagesParser", String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { MESS_PARSER              = ReadWriteFONA.class.getMethod("readMessageParser",      String.class); } catch (Exception ex) { ex.printStackTrace(); }
    try { ON_SENT_OK               = ReadWriteFONA.class.getMethod("onSendOK",               String.class); } catch (Exception ex) { ex.printStackTrace(); }
  }

  // TODO Rewrite this one with Consumer<String> instead of the Method
  // Like this::onSendOK in place of ON_SENT_OK
  public enum ArduinoMessagePrefix {
    FONA_OK       (">> FONA READY",          "Good to go",                     READY),
    INCOMING_MESS ("+CMTI:",                 "Incoming message",               INCOMING_MESSAGE_MANAGER),
    ADC_OK        (">> ADC:",                "Read ADC",                       ADC_PARSER),
    ADC_FAILED    (">> ADC FAILED",          "Read ADC failed",                GENERIC_FAILURE_PARSER),
    BAT_OK        (">> BAT:",                "Read Battery",                   BATTERY_PARSER),
    BAT_FAILED    (">> BATTERY READ FAILED", "Read Battery failed",            GENERIC_FAILURE_PARSER),
    CCID_OK       (">> CCID:",               "Read SIM Card #",                CCID_PARSER),
    RSSI_OK       (">> RSSI:",               "Read RSSI",                      RSSI_PARSER),
    NETW_OK       (">> NETW:",               "Read Network status",            NETWORK_PARSER),
    MESS_N_FAILED (">> NUMMESS FAILED",      "Read Number of messages failed", GENERIC_FAILURE_PARSER),
    MESS_N_OK     (">> MESS:",               "Read Number of messages OK",     NUM_MESS_PARSER),
    READ_FAILED   (">> READ MESS FAILED",    "Read Message failed",            GENERIC_FAILURE_PARSER),
    READ_OK       (">> MESSNUM:",            "Read Message OK",                MESS_PARSER),
    DEL_OK        (">> DEL OK",              "Delete message OK",              GENERIC_SUCCESS_PARSER),
    DEL_FAILED    (">> DEL FAILED",          "Delete Message failed",          GENERIC_FAILURE_PARSER),
    SEND_OK       (">> SEND OK",             "Send Message OK",                ON_SENT_OK),
    SEND_FAILED   (">> SEND FAILED",         "Send Message Failed",            GENERIC_FAILURE_PARSER);

    private final String prefix;
    private final String meaning;
    private final transient Method parser;
    ArduinoMessagePrefix(String prefix, String meaning, Method parser) {
      this.prefix  = prefix;
      this.meaning = meaning;
      this.parser  = parser;
    }

    public String prefix()  { return this.prefix; }
    public String meaning() { return this.meaning; }
    public Method parser()  { return this.parser; }
  }

  final private Serial serial = SerialFactory.createInstance();

  public ReadWriteFONA(FONAClient from)
  {
    this.caller = from;
  }

  public void startListening()
    throws NumberFormatException {
    // create and register the serial data listener
    serial.addListener(new SerialDataEventListener() {
      private StringBuffer fullMessage = new StringBuffer();
      private final String ACK = "\n"; // "\r\n";

      @Override
      public void dataReceived(SerialDataEvent event) {
        // print out the data received to the console
        String payload = null;
        try {
          payload = event.getAsciiString();
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }

        fullMessage.append(payload);
        if (fullMessage.toString().endsWith(ACK)) {
       // System.out.println("Flushing...");
          String mess = fullMessage.toString(); // Send the full message. Parsed later.
	        // Manage data here. Check in the enum ArduinoMessagePrefix
	        try {
	          mess = mess.trim();
	          while (mess.endsWith("\n") ||
	                 mess.endsWith("\r")) {
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

  public void openSerialInput() throws IOException {
    String port = System.getProperty("serial.port", Serial.DEFAULT_COM_PORT);
    int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));

    System.out.println("Serial Communication.");
    System.out.println(" ... connect using settings: " + Integer.toString(br) +  ", N, 8, 1.");
    System.out.println(" ... data received on serial port should be displayed below.");

    System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
    serial.open(port, br);
    System.out.println("Port is opened.");
  }

  public void closeChannel() throws IOException {
    serial.close();
  }

  public void requestBatteryState() throws IOException {
    String mess = "b";
    this.sendSerial(mess);
  }

  public void requestADC() throws IOException {
    String mess = "a";
    this.sendSerial(mess);
  }

  public void requestSIMCardNumber() throws IOException {
    String mess = "C";
    this.sendSerial(mess);
  }

  public void requestRSSI() throws IOException {
    String mess = "i";
    this.sendSerial(mess);
  }

  public void requestNetworkStatus() throws IOException {
    String mess = "n";
    this.sendSerial(mess);
  }

  public void requestNumberOfMessage() throws IOException {
    String mess = "N";
    this.sendSerial(mess);
  }

  public void readMessNum(int smsn) throws IOException {
    String mess = "r|" + Integer.toString(smsn);
    this.sendSerial(mess);
  }

  public void deleteMessNum(int smsn) throws IOException {
    String mess = "d|" + Integer.toString(smsn);
    this.sendSerial(mess);
  }

  public void sendMess(String to, String payload) throws IOException {
    if (payload.length() > 140) {
      System.out.println("Truncating payload to 140 characters");
      payload = payload.substring(0, 140);
    }
    String mess = "s|" + to + "|" + payload;
    this.sendSerial(mess);
  }

  private void sendSerial(String payload) throws IOException {
    if (serial.isOpen()) {
      if ("true".equals(System.getProperty("fona.verbose", "false"))) {
	      System.out.println("\tWriting [" + payload + "] to the serial port...");
      }
      try {
        serial.write(payload + "\n");
      } catch (IllegalStateException ex) {
        ex.printStackTrace();
      }
    } else {
      System.out.println("Not open yet...");
    }
  }

  private void takeAction(String mess) throws Exception {
    ArduinoMessagePrefix amp = findCommand(mess);
    if (amp != null) {
      if ("true".equals(System.getProperty("fona.verbose", "false"))) {
	      System.out.println(amp.meaning());
      }
      Method parser = amp.parser();
      if (parser != null) {
        parser.invoke(this, mess);
      }
    } else {
	    System.out.println("Command [" + mess + "] unknown.");
    }
  }

  private ArduinoMessagePrefix findCommand(String message) {
    ArduinoMessagePrefix ret = null;
    for (ArduinoMessagePrefix amp : ArduinoMessagePrefix.values()) {
      if (message.startsWith(amp.prefix())) {
        ret = amp;
        break;
      }
    }
    return ret;
  }

  public void onSendOK(String message) {
    caller.sendSuccess(message);
  }

  public void genericSuccessParser(String message) {
    caller.genericSuccess(message);
  }

  public void genericFailureParser(String message) {
    caller.genericFailure(message);
  }

  public void incomingMessageManager(String message) throws IOException {
    // +CMTI: "SM",3
    System.out.println("Incoming message:" + message);
    String[] sa = message.split(",");
    if (sa.length == 2) {
      this.readMessNum(Integer.parseInt(sa[1].trim()));
    }
  }

  public void adcParser(String message) {
    String s = message.substring(">> ADC:".length());
    String mess = ("ADC: " + s + " mV");
    caller.adcState(mess);
  }

  public void ready(@SuppressWarnings("unused") String s) {
    caller.ready();
  }

  public void batteryParser(String message) {
    String[] sa = message.substring(">> BAT:".length()).split(",");
    String mess = ("Battery: " + sa[0] + " mV, " + sa[1] + "%");
    caller.batteryState(mess);
  }

  public void ccidParser(String message) {
    String s = message.substring(">> CCID:".length());
    String mess = ("SIM Card#:" + s);
    caller.ccidState(mess);
  }

  public void rssiParser(String message) {
    String[] sa = message.substring(">> RSSI:".length()).split(",");
    String mess = ("RSSI: level " + sa[0] + ", " + sa[1] + "dBm");
    caller.rssiState(mess);
  }

  public void networkParser(String message) {
    String[] sa = message.substring(">> NETW:".length()).split(",");
    String mess = ("Network: level " + sa[0] + ": " + sa[1]);
    caller.networkState(mess);
  }

  public void numberOfMessagesParser(String message) {
    int nb = Integer.parseInt(message.substring(">> MESS:".length()));
    caller.numberOfMessages(nb);
  }

  public void readMessageParser(String message) {
    String[] sa = message.substring(">> MESSNUM:".length()).split("\\|");
    try {
      caller.message(new SMS(Integer.parseInt(sa[0]),
                             sa[1].substring("FROM:".length()),
                             Integer.parseInt(sa[2]),
                             sa[3]));
    } catch (Exception ex) {
      System.err.println("Message [" + message + "]");
      System.err.println("==================");
      for (String s : sa) {
	      System.err.println(s);
      }
      System.err.println("==================");
      ex.printStackTrace();
    }
  }

  public static class SMS {
    private int num;
    private String from;
    private int len;
    private String content;

    public SMS(int num, String from, int len, String content) {
      this.num     = num;
      this.from    = from;
      this.len     = len;
      this.content = content;
    }

    public int getNum() { return this.num; }
    public String getFrom() { return this.from; }
    public int getLen() { return this.len; }
    public String getContent() { return this.content; }
  }
}
