package adc.levelreader.main;

import adc.levelreader.manager.AirWaterInterface;
import adc.levelreader.manager.PushButtonObserver;
import adc.levelreader.manager.SevenADCChannelsManager;
import adc.levelreader.manager.SurfaceDistanceManager;
import adc.utils.EscapeSeq;
import adc.utils.LowPassFilter;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import fona.arduino.FONAClient;
import fona.arduino.ReadWriteFONA;
import org.fusesource.jansi.AnsiConsole;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import relay.RelayManager;
import utils.StringUtils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Relies on props.properties
 * <p>
 * Synopsis:
 * =========
 * 1. There is water in the bilge, with oil on top.
 * 2. The bild pump starts.
 * 3. When the oil is about to reach the pump, the power of the pump is shut off, and a message (SMS)
 * is sent on the phone of the captain, saying "You have X inches of oil in the bilge, you have 24 hours to clean it,
 * reply 'CLEAN' to this message from your phone to reset the process and restart your bilge pump".
 * Pushing the button (physically) will restore tye power.
 * 4. A 'CLEAN' message is received (from captain, owner, or authorities).
 * 5. If the bilge is clean, the process is reset (bilge pump power turned back on).
 * 6. If not, a new message is sent to the captain, the process is NOT reset.
 * <p>
 * 7. If the bilge has not been cleaned within a given amount of time (24h by default),
 * then another message is sent to the boat owner.
 * 8. After the same amount of time, if the bilge is still no clean, then a message is sent
 * to the authorities (Harbor Master, Coast Guards)
 * <p>
 * Interfaced with:
 * - a bilge probe (ADC)
 * - a FONA (SMS shield)
 * - a WebSocket server (node.js)
 * also provides a web interface
 */
public class LelandPrototype implements AirWaterInterface, FONAClient, PushButtonObserver {
	private final static int NB_CHANNELS = 7;

	private static boolean ansiConsole = "true".equals(System.getProperty("ansi.console", "false"));

	private final static String LOG_FILE = "log.log";
	public final static Format CHANNEL_NF = new DecimalFormat("00");
	public final static String CHANNEL_PREFIX = "channel_";
	public final static String CHANNEL_SUFFIX = ".csv";

	private static BufferedWriter fileLogger = null;
	private static BufferedWriter[] channelLogger = null;

	private static Properties props = null;

	private static long cleaningDelay = 0L;
	private static int nbSeenInARow = 40;

	private static double rangeSensorHeight = 0D;
	private final static double SENSOR_SPACING = 1D; // In centimeters

	private static LevelMaterial<Float, SevenADCChannelsManager.Material>[] data = null;
	private static int windowWidth = 10;
	private static double distanceToSurface = Double.MAX_VALUE;
	private static List<Double> oilThicknessValues = null;
	private static double alfa = 0.5;


	private static SevenADCChannelsManager.Material[] previousMaterial = new SevenADCChannelsManager.Material[NB_CHANNELS];
	private static int[] nbSameMaterialInARow = new int[]{0, 0, 0, 0, 0, 0, 0};

	private final static NumberFormat DF31 = new DecimalFormat("000.0");
	private final static NumberFormat DF4 = new DecimalFormat("###0");
	private final static NumberFormat DF23 = new DecimalFormat("##0.000");
	private final static SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
	private static WebSocketClient webSocketClient = null;
	private static ReadWriteFONA smsProvider = null;
	private static RelayManager rm = null;
	private static Pin RESET_PI = RaspiPin.GPIO_10; // GPIO_10, CE0, #24

	private static final GpioController gpio = GpioFactory.getInstance();
	;

	private static String wsUri = "";
	private static String phoneNumber_1 = "",
			phoneNumber_2 = "",
			phoneNumber_3 = "";
	private static String boatName = "";

	private static boolean fonaReady = false;

	private final static int _ALL_OK = -1;
	private final static int SENT_TO_CAPTAIN = 0;
	private final static int SENT_TO_OWNER = 1;
	private final static int SENT_TO_AUTHORITIES = 2;

	public enum ProcessStatus {
		ALL_OK(_ALL_OK),
		MESSAGE_SENT_TO_CAPTAIN(SENT_TO_CAPTAIN),
		MESSAGE_SENT_TO_OWNER(SENT_TO_OWNER),
		MESSAGE_SENT_TO_AUTHORITIES(SENT_TO_AUTHORITIES);

		private int level;

		private ProcessStatus(int level) {
			this.level = level;
		}

		public int level() {
			return this.level;
		}
	}

	private static ProcessStatus currentStatus = ProcessStatus.ALL_OK;

	//private static int currentWaterLevel   = 0;
	private static double currentOilThickness = -1D;
	private static boolean deviceStarted = false;

	private static FONAClient fonaClient = null;
	public final static String SIMULATOR = "Simulator";

	private static boolean calibration = false;

	public LelandPrototype() {
		fonaClient = this;
		data = new LevelMaterial[NB_CHANNELS];
		for (int i = 0; i < data.length; i++) {
			data[i] = new LevelMaterial<Float, SevenADCChannelsManager.Material>(0f, SevenADCChannelsManager.Material.UNKNOWN);
		}

		oilThicknessValues = new ArrayList<Double>(windowWidth);

		final GpioPinDigitalInput resetButton = gpio.provisionDigitalInputPin(RESET_PI, PinPullResistance.PULL_DOWN);
		resetButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState().isHigh()) {
					onButtonPressed();
				}
			}
		});
	}

	private static double smoothOilThickness() {
		double size = oilThicknessValues.size();
		double sigma = 0;
		List<Double> lpf = LowPassFilter.lowPass(oilThicknessValues, alfa);
		for (double v : lpf) {
			sigma += v;
		}
		return sigma / size;
	}

	@Override
	public void setStarted(boolean b) {
		log("                         >>>>>>>>>>>>>>>>>>>>>>  Starting:" + b);
		deviceStarted = b;
		if (b) {
			if ("true".equals(System.getProperty("monitor.fona", "false"))) {
				Thread monitor = new Thread() {
					public void run() {
						long nb = 0;
						while (true) {
							nb += 1;
							if (smsProvider != null && nb % 2 == 0) {
								try {
									smsProvider.requestBatteryState();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
							if (smsProvider != null && nb % 2 == 1) {
								try {
									smsProvider.requestNetworkStatus();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
							delay(10);
						}
					}
				};
				monitor.start();
			}
		}
	}

	@Override
	public void onButtonPressed() {
		log(">>> Reset button has been pressed.");
		RelayManager.RelayState status = rm.getStatus("00");
		// log("Relay is:" + status);
		if (RelayManager.RelayState.OFF.equals(status)) {
			log("Turning relay back on.");
			try {
				rm.set("00", RelayManager.RelayState.ON);
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
	}

	private static void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					log("WS On Open");
				}

				@Override
				public void onMessage(String string) {
					//        log("WS On Message");
					if (smsProvider == null) // Allow simulated CLEAN message
					{
						//  log("Received [" + string + "]");
						// "{"type":"message","data":{"time":1441877164577,"text":"CLEAN"}}"
						try {
							JSONObject json = new JSONObject(string);
							if ("message".equals(json.getString("type"))) {
								JSONObject data = json.getJSONObject("data");
								if (data != null) {
									if ("CLEAN".equals(data.getString("text"))) {
										fonaClient.message(new ReadWriteFONA.SMS(0, SIMULATOR, "CLEAN".length(), "CLEAN"));
									}
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					log("WS On Close");
				}

				@Override
				public void onError(Exception exception) {
					log("WS On Error");
					displayAppErr(exception);
					//      exception.printStackTrace();
				}
			};
			webSocketClient.connect();
		} catch (Exception ex) {
			displayAppErr(ex);
			//  ex.printStackTrace();
		}
	}

	private static String materialToString(SevenADCChannelsManager.Material material) {
		String s = "UNKNOWN";
		if (material == SevenADCChannelsManager.Material.AIR) {
			s = "Air";
		} else if (material == SevenADCChannelsManager.Material.WATER) {
			s = "Water";
		}
//  else if (material == SevenADCChannelsManager.Material.OIL)
//    s = "Oil";
		return s;
	}

	private static void sendSMS(final String to,
	                            final String[] content) {
		Thread bg = new Thread() {
			public void run() {
				for (String s : content) {
					sendSMS(to, s);
				}
			}
		};
		bg.start();
	}

	private static Thread sendMessWaiter = null;

	private static void sendSMS(String to,
	                            String content) {
		if (smsProvider != null) {
			String mess = content;
			if (mess.length() > 140) {
				mess = mess.substring(0, 140);
			}
			log(">>> Sending SMS :" + mess);
			try {
				smsProvider.sendMess(to, mess);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			sendMessWaiter = Thread.currentThread();
			synchronized (sendMessWaiter) {
				try {
					sendMessWaiter.wait(5_000L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				log("...Released!");
			}
			sendMessWaiter = null;
		} else
			log(">>> Simulating call to " + to + ", " + content);
	}


	// User Interface ... Sovietic! And business logic.
	private static void manageData() {
		int maxWaterLevel = -1;
//  int maxOilLevel   = -1;
		// Clear the screen, cursor on top left.
		String str = "";
		int y = 1;
		if (ansiConsole) {
			// AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
//    AnsiConsole.out.println(EscapeSeq.ansiLocate(0, y++));
			// Firts line to erase what was there before starting.
			AnsiConsole.out.println(EscapeSeq.ansiLocate(0, y++) +
					"WT:" + SevenADCChannelsManager.getWaterThreshold() +
					// ", OT:" + SevenADCChannelsManager.getOilThreshold() +
					", Sensor Height: " + DF23.format(rangeSensorHeight) + " cm, D2S:" + DF23.format(distanceToSurface * 100) + " cm                      ");
			str = EscapeSeq.ansiLocate(0, y++) + "+---+--------+---------+";
			AnsiConsole.out.println(str);
			str = EscapeSeq.ansiLocate(0, y++) + "| C |  Vol % |   Mat   |";
			AnsiConsole.out.println(str);

			str = EscapeSeq.ansiLocate(0, y++) + "+---+--------+---------+";
			AnsiConsole.out.println(str);
		}
		for (int chan = data.length - 1; chan >= 0; chan--) // Top to bottom
		{
			if (previousMaterial[chan] != null && previousMaterial[chan] == data[chan].getMaterial()) {
				nbSameMaterialInARow[chan] += 1;
			} else {
				nbSameMaterialInARow[chan] = 0;
			}
			String color = EscapeSeq.ANSI_BLACK; // ANSI_DEFAULT_BACKGROUND;

			if (nbSameMaterialInARow[chan] > nbSeenInARow) {
//        if (data[chan].getMaterial().equals(SevenADCChannelsManager.Material.OIL))
//          color = EscapeSeq.ANSI_RED;
//        else
				if (data[chan].getMaterial().equals(SevenADCChannelsManager.Material.WATER)) {
					color = EscapeSeq.ANSI_BLUE;
				}
			}

			String prefix = EscapeSeq.ansiLocate(0, y++) +
					EscapeSeq.ansiSetTextAndBackgroundColor(EscapeSeq.ANSI_WHITE, color) + EscapeSeq.ANSI_BOLD;
			String suffix = EscapeSeq.ANSI_NORMAL + EscapeSeq.ANSI_DEFAULT_BACKGROUND + EscapeSeq.ANSI_DEFAULT_TEXT;
			str = "| " + Integer.toString(chan + 1) + " | " +
					StringUtils.lpad(DF4.format(data[chan].getPercent()), 4) + " % | " +
					StringUtils.lpad(materialToString(data[chan].getMaterial()), 7) + " |";
			str += (" " + nbSameMaterialInARow[chan] + "   ");
			// if (maxOilLevel == -1 && data[chan].getMaterial().equals(SevenADCChannelsManager.Material.OIL))
			//   maxOilLevel = chan;
			if (maxWaterLevel == -1 && data[chan].getMaterial().equals(SevenADCChannelsManager.Material.WATER)) {
				maxWaterLevel = chan;
			}
			if (ansiConsole) {
				AnsiConsole.out.println(prefix + str + suffix);
			}
			previousMaterial[chan] = data[chan].getMaterial();
		}
		if (ansiConsole) {
			str = EscapeSeq.ansiLocate(0, y++) + "+---+--------+---------+";
			AnsiConsole.out.println(str);
		}
		double waterThickness = (maxWaterLevel + 1) * SENSOR_SPACING;
		double oilThickness = rangeSensorHeight - (waterThickness + (distanceToSurface * 100));
		if (ansiConsole) {
			str = EscapeSeq.ansiLocate(0, y++) + "Water:" + waterThickness + ", OT:" + oilThickness + " cm" + "                  ";
			AnsiConsole.out.println(str);
		}
		if (webSocketClient != null) {
			JSONObject json = new JSONObject();
			json.put("water", waterThickness);
			json.put("oil", oilThickness);
			try {
				webSocketClient.send(json.toString());
			} // [1..100]
			catch (Exception ex) {
				displayAppErr(ex);
				//  ex.printStackTrace();
			}
		}
//  log(">>> To BusinessLogic (" + maxWaterLevel + ", " + maxOilLevel + ")");
//  businessLogic(maxWaterLevel, maxOilLevel); // Before
		if (deviceStarted) {
			businessLogic(maxWaterLevel, oilThickness);
		}
	}

	/**
	 * -----+-oo-
	 * ^  | ^
	 * |  | | Dist To Surface
	 * |  | v
	 * | -+----------------
	 * RSH |  Oil Thickness
	 * | -+----------------
	 * |  | ^
	 * |  | | Water Thickness
	 * v  | v
	 * =====+================
	 * RSH: RangeSensor Height
	 */
	private static void businessLogic(double waterThickness, double oilThickness) {
//  log(">>> In BusinessLogic (" + waterLevel + ", " + oilLevel + ")");

//  currentWaterLevel   = Math.round(waterLevel);
		oilThicknessValues.add(oilThickness);

//  double smoothedOilValue = -1D;

//  System.out.println("Business Logic - Water:" + waterLevel + ", oil:" + oilLevel);
		if (oilThicknessValues.size() >= windowWidth) {
			while (oilThicknessValues.size() > windowWidth) {
				oilThicknessValues.remove(0);
			}
			currentOilThickness = smoothOilThickness();
		}
		if (oilThicknessValues.size() >= windowWidth && currentOilThickness > 0) {
//    log("Oil thick:" + oilThickness + ", Water:" + waterLevel);
			if (waterThickness <= 0.1 && currentOilThickness > 0.25) {
				log("       >>>>>>>>>>>>>>>>>>>>>>>>>> Shutting OFF !!!! <<<<<<<<<<<<<<<<<<<<< W:" + waterThickness + ", O:" + currentOilThickness);
				// Switch the relay off?
				RelayManager.RelayState status = rm.getStatus("00");
				// log("Relay is:" + status);
				if (RelayManager.RelayState.ON.equals(status)) {
					log("Turning relay off!");
					try {
						rm.set("00", RelayManager.RelayState.OFF);
					} catch (Exception ex) {
						System.err.println(ex.toString());
					}
				}
				if (currentStatus.equals(ProcessStatus.ALL_OK)) {
					log("Oil thick:" + currentOilThickness + ", Water:" + waterThickness + " ");
					// Make a call
					String[] mess = {"Oil in the bilge of " + boatName + ": " + DF23.format(currentOilThickness) + " cm.",
							"Please reply CLEAN to this message when done with it."};
					//  String mess = "First warning to " + boatName;

					displayAppMess(" >>>>>>>>>> CALLING " + phoneNumber_1); // + "Mess is a " + mess.getClass().getName() + "\n" + mess);
					sendSMS(phoneNumber_1, mess);
					currentStatus = ProcessStatus.MESSAGE_SENT_TO_CAPTAIN;
					WaitForCleanThread wfct = new WaitForCleanThread();
					wfct.start();
				}
			} else {
				System.out.println("                            ");
				System.out.println("                            ");
			}
		}
	}

	/*
	private static void businessLogic_v1(int waterLevel, int oilLevel)
	{
	//  log(">>> In BusinessLogic (" + waterLevel + ", " + oilLevel + ")");
		int oilThickness = Math.max(0, oilLevel - waterLevel);
		currentWaterLevel   = waterLevel;
		currentOilThickness = oilThickness;

	//  System.out.println("Business Logic - Water:" + waterLevel + ", oil:" + oilLevel);
		if (oilLevel > -1)
		{
	//    log("Oil thick:" + oilThickness + ", Water:" + waterLevel);
			if (waterLevel < 0 && oilThickness > 0)
			{
				// Switch the relay off?
				RelayManager.RelayState status = rm.getStatus("00");
		 // log("Relay is:" + status);
				if (RelayManager.RelayState.ON.equals(status))
				{
					log("Turning relay off!");
					try { rm.set("00", RelayManager.RelayState.OFF); }
					catch (Exception ex)
					{
						System.err.println(ex.toString());
					}
				}
				if (currentStatus.equals(ProcessStatus.ALL_OK))
				{
					log("Oil thick:" + oilThickness + ", Water:" + waterLevel + " (Oil Level:" + oilLevel + ")");
					// Make a call
					String[] mess = {"Oil the bilge of " + boatName + ": " + oilThickness + ".",
														"Please reply CLEAN to this message when done with it."};
			//  String mess = "First warning to " + boatName;

					displayAppMess(" >>>>>>>>>> CALLING " + phoneNumber_1); // + "Mess is a " + mess.getClass().getName() + "\n" + mess);
					sendSMS(phoneNumber_1, mess);
					currentStatus = ProcessStatus.MESSAGE_SENT_TO_CAPTAIN;
					WaitForCleanThread wfct = new WaitForCleanThread();
					wfct.start();
				}
			}
			else
			{
				System.out.println("                            ");
				System.out.println("                            ");
			}
		}
	}
	*/
	@Override
	public void setTypeOfChannel(int channel, SevenADCChannelsManager.Material material, float val) {
		data[channel] = new LevelMaterial(val, material);
		manageData();
		// Debug
		if (ansiConsole && "true".equals(System.getProperty("debug", "false"))) {
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 15 + channel));
			Date now = new Date();
			AnsiConsole.out.println(now.toString() + ": Channel " + channel + " >> (" + DF31.format(val) + ") " + materialToString(material) + "       ");
		}
	}

	@Override
	public void setSurfaceDistance(double dist) {
		distanceToSurface = dist;
		if (calibration) {
			System.out.println(TF.format(new Date()) + ": " + DF23.format(distanceToSurface * 100) + " cm");
		} else {
			manageData();
		}
	}

	@Override
	public void genericSuccess(String string) {
		log(string);
	}

	@Override
	public void sendSuccess(String string) {
		if (sendMessWaiter != null) {
			synchronized (sendMessWaiter) {
				sendMessWaiter.notify();
				log("Released waiter...");
			}
		}
	}

	@Override
	public void genericFailure(String string) {
		log(string);
	}

	@Override
	public void adcState(String string) {
		log(string);
	}

	@Override
	public void batteryState(String string) {
		if (ansiConsole) {
			String str = EscapeSeq.ansiLocate(0, 20) + ">>>> " + string;
			AnsiConsole.out.println(str);
		} else
			log(string);
	}

	@Override
	public void ccidState(String string) {
		log(string);
	}

	@Override
	public void rssiState(String string) {
		log(string);
	}

	@Override
	public void networkState(String string) {
		if (ansiConsole) {
			String str = EscapeSeq.ansiLocate(0, 21) + ">>>> " + string;
			AnsiConsole.out.println(str);
		} else
			log(string);
	}

	@Override
	public void numberOfMessages(int i) {
		log("Nb mess:" + i);
	}

	@Override
	public void message(ReadWriteFONA.SMS sms) {
		log("\nReceived messsage:");
		log("From:" + sms.getFrom());
		log(sms.getContent());
		if (sms.getContent().trim().equalsIgnoreCase("CLEAN") && (SIMULATOR.equals(sms.getFrom()) ||
				sms.getFrom().contains(phoneNumber_1) ||
				sms.getFrom().contains(phoneNumber_2) ||
				sms.getFrom().contains(phoneNumber_3))) {
			// Check, and Resume if OK
			if (currentOilThickness <= 0) {
				// Tell whoever has been warned so far
				boolean[] messLevel = {false, false, false};
				messLevel[SENT_TO_CAPTAIN] = (currentStatus == ProcessStatus.MESSAGE_SENT_TO_CAPTAIN ||
						currentStatus == ProcessStatus.MESSAGE_SENT_TO_OWNER ||
						currentStatus == ProcessStatus.MESSAGE_SENT_TO_AUTHORITIES);
				messLevel[SENT_TO_OWNER] = (currentStatus == ProcessStatus.MESSAGE_SENT_TO_OWNER ||
						currentStatus == ProcessStatus.MESSAGE_SENT_TO_AUTHORITIES);
				messLevel[SENT_TO_AUTHORITIES] = (currentStatus == ProcessStatus.MESSAGE_SENT_TO_AUTHORITIES);
				String[] mess = {"Oil in the bilge of " + boatName + " has been cleaned",
						"Bilge pump can be used as before."};
				//  String mess =  "Oil in the bilge of " + boatName + " has been cleaned.";
				if (messLevel[SENT_TO_AUTHORITIES])
					sendSMS(phoneNumber_3, mess);
				if (messLevel[SENT_TO_OWNER])
					sendSMS(phoneNumber_2, mess);
				if (messLevel[SENT_TO_CAPTAIN])
					sendSMS(phoneNumber_1, mess);

				currentStatus = ProcessStatus.ALL_OK;
				RelayManager.RelayState status = rm.getStatus("00");
				// log("Relay is:" + status);
				if (RelayManager.RelayState.OFF.equals(status)) {
					log("Turning relay back on as needed.");
					try {
						rm.set("00", RelayManager.RelayState.ON);
					} catch (Exception ex) {
						System.err.println(ex.toString());
					}
				}
			} else {
				// Reply, not clean enough.
				String[] mess = {"Sorry, the bilge is not clean enough.",
						"Try again to send a CLEAN message when this has been taken care of"};
				// String mess = "Not clean enough";
				sendSMS(phoneNumber_1, mess);
			}
		}
	}

	@Override
	public void ready() {
		log("FONA Ready!");
		fonaReady = true;
	}

	public final static void displayAppMess(String mess) {
		if (false && ansiConsole) {
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 50));
			AnsiConsole.out.println(StringUtils.rpad(mess, 80));
		} else {
			log("AppMess>> " + mess);
		}
	}

	public final static void displayAppErr(Exception ex) {
		if (ansiConsole) {
			AnsiConsole.out.println(EscapeSeq.ansiLocate(1, 60));
			AnsiConsole.out.println(StringUtils.rpad(ex.toString(), 80));
		} else {
			ex.printStackTrace();
		}
	}

	private abstract static class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			}
			if (other == this) {
				return true;
			}
			if (!(other instanceof Tuple)) {
				return false;
			}
			Tuple<X, Y> other_ = (Tuple<X, Y>) other;
			return other_.x == this.x && other_.y == this.y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
			return result;
		}
	}

	public static class LevelMaterial<X, Y> extends Tuple<X, Y> {
		public LevelMaterial(X x, Y y) {
			super(x, y);
		}

		public X getPercent() {
			return this.x;
		}

		public Y getMaterial() {
			return this.y;
		}
	}

	public static Properties getAppProperties() {
		return props;
	}

	public static BufferedWriter[] getChannelLoggers() {
		return channelLogger;
	}

	public static void log(String s) {
		if (fileLogger != null) {
			try {
				fileLogger.write(s + "\n");
				;
				fileLogger.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			System.out.println(s);
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		if (args.length > 0) {
			if (args[0].equals("-cal")) {
				calibration = true;
				ansiConsole = false;
			}
		}

		LelandPrototype lp = new LelandPrototype();

		if (!calibration) {
			props = new Properties();
			try {
				props.load(new FileInputStream("props.properties"));
			} catch (IOException ioe) {
				displayAppErr(ioe);
				//  ioe.printStackTrace();
			}

			try {
				windowWidth = Integer.parseInt(LelandPrototype.getAppProperties().getProperty("smooth.width", "10")); // For smoothing
				alfa = Double.parseDouble(LelandPrototype.getAppProperties().getProperty("low.pass.filter.alfa", "0.5"));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			try {
				cleaningDelay = Long.parseLong(props.getProperty("cleaning.delay", "86400")); // Default: one day
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			try {
				nbSeenInARow = Integer.parseInt(props.getProperty("seen.in.a.row", "40"));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			try {
				rangeSensorHeight = Double.parseDouble(props.getProperty("range.sensor.height", "10"));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			try {
				fileLogger = new BufferedWriter(new FileWriter(LOG_FILE));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (ansiConsole)
				AnsiConsole.systemInstall();

			final ReadWriteFONA fona;

			if ("true".equals(props.getProperty("with.fona", "false"))) {
				System.setProperty("baud.rate", props.getProperty("baud.rate"));
				System.setProperty("serial.port", props.getProperty("serial.port"));

				fona = new ReadWriteFONA(lp);
				fona.openSerialInput();
				fona.startListening();
				while (!fonaReady) {
					System.out.println("Waiting for the FONA device to come up...");
					try {
						Thread.sleep(1_000L);
					} catch (InterruptedException ie) {
					}
				}
				fona.requestBatteryState();
				fona.requestNetworkStatus();
				displayAppMess(">>> FONA Ready, moving on");
				smsProvider = fona;
			} else {
				System.out.println("Will simulate the phone calls.");
			}
			delay(1);

			wsUri = props.getProperty("ws.uri", "");
			phoneNumber_1 = props.getProperty("phone.number.1", "14153505547");
			phoneNumber_2 = props.getProperty("phone.number.2", "14153505547");
			phoneNumber_3 = props.getProperty("phone.number.3", "14153505547");
			boatName = props.getProperty("boat.name", "Never Again XXIII");

			try {
				rm = new RelayManager();
				rm.set("00", RelayManager.RelayState.ON);
			} catch (Exception ex) {
				System.err.println("You're not on the PI, hey?");
				ex.printStackTrace();
			}

			if (wsUri.trim().startsWith("ws://")) {
				log(">>> Connecting to the WebSocket server [" + wsUri + "]");
				initWebSocketConnection(wsUri);
			} else {
				log(">>> No WebSocket server");
				delay(1);
			}
		}

		final SevenADCChannelsManager sacm = (calibration ? null : new SevenADCChannelsManager(lp));
		final SurfaceDistanceManager sdm = new SurfaceDistanceManager(lp);
		sdm.startListening();

		final Thread me = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // Cleanup
      System.out.println();
      if (sacm != null) {
        sacm.quit();
      }
      if (smsProvider != null) {
        try {
          smsProvider.closeChannel();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      synchronized (me) {
        me.notify();
      }
      gpio.shutdown();
      if (channelLogger != null) {
        try {
          for (BufferedWriter bw : channelLogger) {
            bw.close();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      System.out.println("Program stopped by user's request.");
    }, "Shutdown Hook"));

		if (!calibration) {
			if ("true".equals(props.getProperty("log.channels", "false"))) {
				channelLogger = new BufferedWriter[SevenADCChannelsManager.getChannel().length];
				for (int i = 0; i < SevenADCChannelsManager.getChannel().length; i++) {
					channelLogger[i] = new BufferedWriter(new FileWriter(CHANNEL_PREFIX + CHANNEL_NF.format(i) + CHANNEL_SUFFIX));
				}
			}

			// CLS
			if (ansiConsole) {
				AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
			}
		}
		synchronized (me) {
			System.out.println("Main thread waiting...");
			me.wait();
		}
		System.out.println("Done.");
	}

	/**
	 * Wait/Delay/Sleep...
	 *
	 * @param sec delay in seconds.
	 */
	private static void delay(float sec) {
		try {
			Thread.sleep(Math.round(1_000 * sec));
		} catch (InterruptedException ie) {
		}
	}

	public static class WaitForCleanThread extends Thread {
		private boolean keepWaiting = true;
		private long started = 0L;

		public void stopWaiting() {
			this.keepWaiting = false;
		}

		public void run() {
			started = System.currentTimeMillis();
			while (keepWaiting && !currentStatus.equals(ProcessStatus.ALL_OK)) {
				delay(10); // in seconds
				if (!currentStatus.equals(ProcessStatus.ALL_OK) &&
						(System.currentTimeMillis() - started) > (cleaningDelay * 1_000)) { // Expired
					// Next status level.
					log("Your cleaning delay (" + cleaningDelay + ") has expired. Going to the next level");
					log(" >>>>>>>>>> Level is " + currentStatus + ", GOING TO THE NEXT LEVEL >>>>>> ");
					switch (currentStatus.level()) {
						case SENT_TO_CAPTAIN: {
							log(">>>>>>>>>>>>> SENDING MESSAGE TO OWNER >>>>>");
							started = System.currentTimeMillis(); // Re-initialize the loop
							currentStatus = ProcessStatus.MESSAGE_SENT_TO_OWNER;
							String[] mess = {"Your boat, " + boatName + ", has oil in its bilge",
									"The power supply of the bilge pump has been shut off",
									"This oil should be cleaned.",
									"Reply to this message by sending CLEAN when done"};
							//  String mess = "Your boat, " + boatName + ", has oil in its bilge.";
							sendSMS(phoneNumber_2, mess);
						}
						break;
						case SENT_TO_OWNER: {
							log(">>>>>>>>>>>>> SENDING MESSAGE TO AUTHORITIES >>>>>");
							started = System.currentTimeMillis(); // Re-initialize the loop
							currentStatus = ProcessStatus.MESSAGE_SENT_TO_AUTHORITIES;
							String[] mess = {"The vessel " + boatName + " has oil in its bilge",
									"The power supply of the bilge pump has been shut off",
									"This oil should be cleaned.",
									"Reply to this message by sending CLEAN when the bilge has been cleaned."};
							//  String mess = "The vessel " + boatName + " has oil in its bilge."};
							sendSMS(phoneNumber_3, mess);
						}
						break;
						default:
							log(">>>>>>>>>>>>> FULL RESET NEEDED >>>>>");
							keepWaiting = false; // Full reset needed.
							break;
					}
				}
			}
			log("  >>> " + this.getClass().getName() + " completed.");
		}
	}
}
