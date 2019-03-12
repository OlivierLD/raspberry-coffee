package nmea.tcp.ssd1306_128x64;

import calc.GeomUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import http.HTTPServer;
import http.client.HTTPClient;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import utils.PinUtil;
import utils.TCPUtils;
import utils.TimeUtil;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * Uses SPI interface for the OLED Screen
 */
public class TCPWatch {

	private static SwingLedPanel substitute;

	private static final int WIDTH = 128;
	private static final int HEIGHT = 64;

	private static class GPSDate {
		int year;
		int month;
		int day;
		int hours;
		int minutes;
		int seconds;
		GPSDate seconds(int seconds) {
			this.seconds = seconds;
			return this;
		}
		GPSDate minutes(int minutes) {
			this.minutes = minutes;
			return this;
		}
		GPSDate hours(int hours) {
			this.hours = hours;
			return this;
		}
		GPSDate day(int day) {
			this.day = day;
			return this;
		}
		GPSDate month(int month) {
			this.month = month;
			return this;
		}
		GPSDate year(int year) {
			this.year = year;
			return this;
		}

		GPSDate date(Date date) {
			Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("etc/UTC"));
			calendar.setTime(date);

			this.year = calendar.get(Calendar.YEAR);
			this.month = calendar.get(Calendar.MONTH) + 1;
			this.day = calendar.get(Calendar.DATE);
			this.hours = calendar.get(Calendar.HOUR_OF_DAY);
			this.minutes = calendar.get(Calendar.MINUTE);
			this.seconds = calendar.get(Calendar.SECOND);

			return this;
		}

		@Override
		public String toString() {
			return String.format("%04d-%02d-%02d %02d:%02d:%02d", this.year, this.month, this.day, this.hours, this.minutes, this.seconds);
		}
	}

	private static String[] MONTH = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	// The data to display
	private static double latitude = 0;
	private static double longitude = 0;
	private static double sog = 0;
	private static double cog = 0;
	private static GPSDate gpsDate = null;
	private static boolean connected = false;

	private static boolean VERBOSE = "true".equals(System.getProperty("verbose", "false"));
	private static boolean SCREEN_00_VERBOSE = "true".equals(System.getProperty("verbose.00", "false"));
	private static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));
	private static String BASE_URL = System.getProperty("base.url", "http://192.168.127.1:9999");

	private final static SimpleDateFormat SDF_1 = new SimpleDateFormat("E dd MMM yyyy");
	private final static SimpleDateFormat SDF_2 = new SimpleDateFormat("HH:mm:ss Z");
	private final static SimpleDateFormat SDF_3 = new SimpleDateFormat("HH:mm:ss z");

	private final static SimpleDateFormat SDF_HH = new SimpleDateFormat("HH");
	private final static SimpleDateFormat SDF_MM = new SimpleDateFormat("mm");
	private final static SimpleDateFormat SDF_SS = new SimpleDateFormat("ss");

	private final static NumberFormat SOG_FMT = new DecimalFormat("#0.00");
	private final static NumberFormat COG_FMT = new DecimalFormat("000");

	private static int currentIndex = 0; // Screen index, incremented/decremented with the buttons K1 (up) & K3 (down)

	private static GpioController gpio;
	private static GpioPinDigitalInput key1Pin = null;
	private static GpioPinDigitalInput key2Pin = null;

	private static boolean k1 = false, k2 = false;
	private static Consumer<GpioPinDigitalStateChangeEvent> key1Consumer = (event) -> {
		k1 = event.getState().isLow(); // low: down
		if (VERBOSE) {
			System.out.println(String.format("K1 was %s", k1 ? "pushed" : "released"));
		}
		if (k1) { // K1 is pushed down
			currentIndex++;
		}
	};
	private static Consumer<GpioPinDigitalStateChangeEvent> key2Consumer = (event) -> {
		k2 = event.getState().isLow(); // low: down
		if (VERBOSE) {
			System.out.println(String.format("K2 was %s", k2 ? "pushed" : "released"));
		}
		if (k2) { // K2 is pushed down
			currentIndex--;
		}
	};

	// Add your pages to the list below
	private static List<Consumer<ScreenBuffer>> pageManagers = Arrays.asList(
			TCPWatch::displayPage00,
			TCPWatch::displayPage01,
			TCPWatch::displayPage02,
			TCPWatch::displayPage03,
			TCPWatch::displayPage04,
			TCPWatch::displayPage05
	);

	/** This is the REST request
	 * @param baseUrl like "http://localhost:8080"
	 * @return
	 */
	public static JsonObject handleRequest(String baseUrl) {

		String url =  baseUrl + "/mux/cache";

		Map<String, String> headers = new HashMap<>(1);
		headers.put("Accept", "application/json"); //, text/javascript, */*; q=0.01");

		HTTPServer.Request request = new HTTPServer.Request("GET", url, "HTTP/1.1");
		request.setHeaders(headers);

		HTTPServer.Response response = null;
		try {
			response = HTTPClient.doRequest(request);
			connected = true;
		} catch (Exception ex) {
			connected = false;
			ex.printStackTrace();
		}

		String data = (response != null ? new String(response.getPayload()) : null);
		if (VERBOSE) {
			System.out.println(String.format("HTTP Response:\n%s", data));
		}
		if (data != null) {
			Gson gson = new Gson();
			JsonElement element = gson.fromJson(data, JsonElement.class);
			if (element != null) {
				JsonObject jsonObj = element.getAsJsonObject();
				return jsonObj;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private static boolean keepLooping = true;

	private static void displayPage00(ScreenBuffer sb) {
		sb.clear();
		String title = "- Status Screen -";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		String serverURL = BASE_URL;
		y += 10;
		sb.text("Server Base URL:", 2, y);
		y += 8;
		sb.text(serverURL.substring("http://".length()), 2, y);
		y += 8;
		sb.text(String.format("Connected: %s", connected ? "YES" : "NO"), 2, y);
		y += 8;
		sb.text("Network...", 2, y); // Potentially overridden
		y += 8;
		try {
			String command = "iwconfig"; // "iwconfig | grep wlan0 | awk '{ print $4 }'";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			if (SCREEN_00_VERBOSE) {
				System.out.println(String.format("Reading %s output", command));
			}
			y -= 8;
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					final String essid = "ESSID:";
					if (line.indexOf(essid) > -1) {
						sb.text(line.substring(line.indexOf(essid) + essid.length()), 2, y);
						y += 8;
						if (SCREEN_00_VERBOSE) {
							System.out.println(line);
						}
					}
				}
			}
			if (SCREEN_00_VERBOSE) {
				System.out.println(String.format("Done with %s", command));
			}
			reader.close();
		} catch (Exception ex) {
			if (SCREEN_00_VERBOSE) {
				ex.printStackTrace();
			}
		}
		try {
//			sb.text("IP Address:", 2, y);
			String line = "IP ";
//			y += 8;
//		List<String> addresses = TCPUtils.getIPAddresses("wlan0", true);
			List<String[]> addresses = TCPUtils.getIPAddresses(true);
			for (String[] addr : addresses) {
				line += (addr[1] + " ");
				if (SCREEN_00_VERBOSE) {
					System.out.println(addr);
				}
			}
			sb.text(line, 2, y);
			y += 8;
		} catch (Exception ex) {
			if (SCREEN_00_VERBOSE) {
				ex.printStackTrace();
			}
		}

//		y = HEIGHT - 1;
//		String index = String.format("Index: %d", currentIndex);
//		len = sb.strlen(index);
//		sb.text(index, (WIDTH / 2) - (len / 2), y);
	}

	private static void displayPage01(ScreenBuffer sb) {
		sb.clear();
		String title = "Screen #1";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		String latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
		String lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
		y += 12;
		sb.text(String.format("L:%11s", latStr), 2, y);
		y += 8;
		sb.text(String.format("G:%11s", lngStr), 2, y);
		y += 8;
		Date date = new Date();
		sb.text(SDF_1.format(date), 2, y);
		y += 8;
		sb.text(SDF_3.format(date), 2, y);
//		y += 8;

		y = HEIGHT - 1;
		String index = String.format("Index: %d", currentIndex);
		len = sb.strlen(index);
		sb.text(index, (WIDTH / 2) - (len / 2), y);
	}

	private static void displayPage02(ScreenBuffer sb) {
		sb.clear();
		String title = "Screen #2";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		String latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
		String lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
		y += 12;
		sb.text(String.format("L:%11s", latStr), 2, y);
		y += 8;
		sb.text(String.format("G:%11s", lngStr), 2, y);
		y += 8;
		sb.text(String.format("SOG: %s kts", SOG_FMT.format(sog)), 2, y);
		y += 8;
		sb.text(String.format("COG: %s", COG_FMT.format(cog)), 2, y);
//		y += 8;

		y = HEIGHT - 1;
		String index = String.format("Index: %d", currentIndex);
		len = sb.strlen(index);
		sb.text(index, (WIDTH / 2) - (len / 2), y);
	}

	private static void displayPage03(ScreenBuffer sb) {
		// A Watch for GPS Time
		sb.clear();
		String title = "Screen #3";
		int y = 8;
		int len = sb.strlen(title);
//		sb.text(title, (WIDTH / 2) - (len / 2), y);
		y += 12;
		if (gpsDate != null) {
			int centerX = HEIGHT / 2;
			int centerY = HEIGHT / 2;
			int radius = HEIGHT / 2;
			sb.circle(centerX, centerY, radius);
			for (int h=0; h<12; h+=1) { // Hours Ticks
				int extX = centerX + (int)Math.round(radius * Math.sin(Math.toRadians(h * 30)));
				int extY = centerY - (int)Math.round(radius * Math.cos(Math.toRadians(h * 30)));
				int intX = centerX + (int)Math.round(radius * 0.8 * Math.sin(Math.toRadians(h * 30)));
				int intY = centerY - (int)Math.round(radius * 0.8 * Math.cos(Math.toRadians(h * 30)));
				sb.line(intX, intY, extX, extY);
			}
			int secInDegrees = (gpsDate.seconds * 6);
			float minInDegrees = ((gpsDate.minutes * 6) + (gpsDate.seconds / 10));
			float hoursInDegrees = ((gpsDate.hours) * 30) + (((gpsDate.minutes * 6) + (gpsDate.seconds / 10)) / 12);

			// Hours
			int hoursX = centerX + (int)Math.round(radius * 0.5 * Math.sin(Math.toRadians(hoursInDegrees)));
			int hoursY = centerY - (int)Math.round(radius * 0.5 * Math.cos(Math.toRadians(hoursInDegrees)));
			sb.line(centerX, centerY, hoursX, hoursY);
			// Minutes
			int minutesX = centerX + (int)Math.round(radius * 0.8 * Math.sin(Math.toRadians(minInDegrees)));
			int minutesY = centerY - (int)Math.round(radius * 0.8 * Math.cos(Math.toRadians(minInDegrees)));
			sb.line(centerX, centerY, minutesX, minutesY);
			// Seconds
			int secondsX = centerX + (int)Math.round(radius * 0.9 * Math.sin(Math.toRadians(secInDegrees)));
			int secondsY = centerY - (int)Math.round(radius * 0.9 * Math.cos(Math.toRadians(secInDegrees)));
			sb.line(centerX, centerY, secondsX, secondsY);

			String line = String.format("%02d", gpsDate.day);
			len = sb.strlen(line);
			sb.text(line, (3 * WIDTH / 4) - (len / 2), y);
			y += 8;
			line = MONTH[gpsDate.month - 1];
			len = sb.strlen(line);
			sb.text(line, (3 * WIDTH / 4) - (len / 2), y);
			y += 8;
			line = String.format("%04d", gpsDate.year);
			len = sb.strlen(line);
			sb.text(line, (3 * WIDTH / 4) - (len / 2), y);
			y += 12;
			line = String.format("%02d:%02d:%02d", gpsDate.hours, gpsDate.minutes, gpsDate.seconds);
			len = sb.strlen(line);
			sb.text(line, (3 * WIDTH / 4) - (len / 2), y);
			y += 8;
			line = "UTC";
			len = sb.strlen(line);
			sb.text(line, (3 * WIDTH / 4) - (len / 2), y);
			y += 8;
		}
//		sb.text(String.format("Index: %d", currentIndex), 2, y);
	}

	private static void displayPage04(ScreenBuffer sb) {
		sb.clear();
		String title = "Screen #4";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		y += 12;
		String line = "SOG";
		len = sb.strlen(line);
		sb.text(line, (WIDTH / 2) - (len / 2), y);
		y += 16;
		int ff = 2;
		line = String.format("%s kts", SOG_FMT.format(sog));
		len = sb.strlen(line, ff);
		sb.text(line, (WIDTH / 2) - (len / 2), y, ff);
//		y += 16;

		y = HEIGHT - 1;
		String index = String.format("Index: %d", currentIndex);
		len = sb.strlen(index);
		sb.text(index, (WIDTH / 2) - (len / 2), y);
	}

	private static void displayPage05(ScreenBuffer sb) {
		sb.clear();
		String title = "Screen #5";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		y += 12;

		String line = "COG";
		len = sb.strlen(line);
		sb.text(line, (WIDTH / 2) - (len / 2), y);
		y += 16;
		int ff = 2;
		line = String.format("%s", COG_FMT.format(cog));
		len = sb.strlen(line, ff);
		sb.text(line, (WIDTH / 2) - (len / 2), y, ff);
//		y += 16;

		y = HEIGHT - 1;
		String index = String.format("Index: %d", currentIndex);
		len = sb.strlen(index);
		sb.text(index, (WIDTH / 2) - (len / 2), y);
	}

	public static void main(String... args) throws Exception {

		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		// Use WiringPi numbers.
		int defaultCLK  = 14;
		int defaultMOSI = 12;
		int defaultCS   = 10;
		int defaultRST  = 5;
		int defaultDC   = 4;

		int defaultK1   = 29;
		int defaultK2   = 28;

		int clkPin  = Integer.parseInt(System.getProperty("CLK", String.valueOf(defaultCLK)));
		int mosiPin = Integer.parseInt(System.getProperty("MOSI", String.valueOf(defaultMOSI)));
		int csPin   = Integer.parseInt(System.getProperty("CS", String.valueOf(defaultCS)));
		int rstPin  = Integer.parseInt(System.getProperty("RST", String.valueOf(defaultRST)));
		int dcPin   = Integer.parseInt(System.getProperty("DC", String.valueOf(defaultDC)));

		int k1Pin = Integer.parseInt(System.getProperty("K1", String.valueOf(defaultK1)));
		int k2Pin = Integer.parseInt(System.getProperty("K2", String.valueOf(defaultK2)));

		if (VERBOSE) {
			System.out.println("Starting...");
		}

		try {
			gpio = GpioFactory.getInstance();

			// User-parameters for those 2 pins, in case you want to invert them
			key1Pin = gpio.provisionDigitalInputPin(PinUtil.getPinByWiringPiNumber(k1Pin), "K-1", PinPullResistance.PULL_DOWN);
			key1Pin.setShutdownOptions(true);
			key2Pin = gpio.provisionDigitalInputPin(PinUtil.getPinByWiringPiNumber(k2Pin), "K-2", PinPullResistance.PULL_DOWN);
			key2Pin.setShutdownOptions(true);

			if (VERBOSE) {
				System.out.println("Initializing button listeners");
			}

			key1Pin.addListener((GpioPinListenerDigital) event -> {
				if (key1Consumer != null) {
					if (VERBOSE) {
						System.out.println("Consuming K-1");
					}
					key1Consumer.accept(event);
				} else {
					if (VERBOSE) {
						System.out.println("No consumer for K-1");
					}
				}
			});
			key2Pin.addListener((GpioPinListenerDigital) event -> {
				if (key2Consumer != null) {
					if (VERBOSE) {
						System.out.println("Consuming K-2");
					}
					key2Consumer.accept(event);
				} else {
					if (VERBOSE) {
						System.out.println("No consumer for K-2");
					}
				}
			});
		} catch (Throwable error) {
			error.printStackTrace();
		}

		// Start external data thread
		Thread dataFetcher = new Thread(() -> {
			while (keepLooping) {
				TimeUtil.delay(1_000);
//			System.out.println("\t\t... external data (like REST) Ping!");
				if (VERBOSE) {
					System.out.println(">> Fetching...");
				}
				JsonObject response = handleRequest(BASE_URL);
				/*
				 * We are interested in
				 * "Position": {
					    "lat": 38.063721666666666,
					    "lng": -122.94171999999998
					  },
					  "SOG": {
					    "speed": 1.9
					  },
					  "GPS Date \u0026 Time": {
					    "date": "Nov 24, 2018 11:23:08 AM",
					    "epoch": 1543087388000,
					    "fmtDate": {
					      "epoch": 1543087388000,
					      "year": 2018,
					      "month": 11,
					      "day": 24,
					      "hour": 19,
					      "min": 23,
					      "sec": 8
					    }
					  },
					  "COG": {
					    "angle": 212.7
					  }
				 */

				if (response != null) {
					// Dispatch the data
					try {
						latitude = response.get("Position").getAsJsonObject().get("lat").getAsDouble();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						longitude = response.get("Position").getAsJsonObject().get("lng").getAsDouble();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						JsonElement gpsJson = response.get("GPS Date & Time");
						if (gpsJson != null) {
							gpsDate = new GPSDate()
									.year(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("year").getAsInt())
									.month(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("month").getAsInt())
									.day(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("day").getAsInt())
									.hours(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("hour").getAsInt())
									.minutes(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("min").getAsInt())
									.seconds(gpsJson.getAsJsonObject().get("fmtDate").getAsJsonObject().get("sec").getAsInt());
						} else {
							Date substituteDate = new Date();
							gpsDate = new GPSDate()
									.date(substituteDate);
							if (DEBUG) {
								System.out.println(String.format("Generated substitute date %s", gpsDate.toString()));
							}
						}
					} catch (NullPointerException npe) {
						// No GPS Date
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						sog = response.get("SOG").getAsJsonObject().get("speed").getAsDouble();
					} catch (NullPointerException npe) {
						// No SOG
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						cog = response.get("COG").getAsJsonObject().get("angle").getAsDouble();
					} catch (NullPointerException npe) {
						// No COG
					} catch (Exception ex) {
						ex.printStackTrace();
					}
//				Gson gson = new GsonBuilder().setPrettyPrinting().create();
//				String prettyJson = gson.toJson(response);
//				System.out.println(">> Data:" + prettyJson);
				} else {
					if (DEBUG) {
						System.out.println("...No response");
					}
				}
			}
		}, "dataFetcher");
		dataFetcher.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nCtrl+C !");
			keepLooping = false;
			TimeUtil.delay(5_000);// Wait for the screen to shut off
		}));

		SSD1306 oled = null;
		try {
//		oled = new SSD1306(WIDTH, HEIGHT); // Default pins (look in the SSD1306 code)
			// If needed, override the default pins
			//                          Clock             MOSI              CS                RST               DC
//    oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16, WIDTH, HEIGHT);
			oled = new SSD1306(
					PinUtil.getPinByWiringPiNumber(clkPin),
					PinUtil.getPinByWiringPiNumber(mosiPin),
					PinUtil.getPinByWiringPiNumber(csPin),
					PinUtil.getPinByWiringPiNumber(rstPin),
					PinUtil.getPinByWiringPiNumber(dcPin),
					WIDTH, HEIGHT);
			oled.begin();
			oled.clear();
//    oled.display();
		} catch (Throwable error) {
			oled = null;
			System.out.println("Displaying substitute Swing Led Panel");
			substitute = new SwingLedPanel(SwingLedPanel.ScreenDefinition.SSD1306_128x64);
			substitute.setLedColor(Color.WHITE);
			substitute.setVisible(true);
		}

		if (VERBOSE) {
			System.out.println(String.format("Default pins:\n" +
					"CLK:  %02d\n" +
					"MOSI: %02d\n" +
					"CS:   %02d\n" +
					"RST:  %02d\n" +
					"DC:   %02d\n" +
					"K-1:  %02d\n" +
					"K-2:  %02d", defaultCLK, defaultMOSI, defaultCS, defaultRST, defaultDC, defaultK1, defaultK2));
			System.out.println("Object created");


			/* Defaults:
	private static Pin spiClk = RaspiPin.GPIO_14; // Pin #23, SCLK, GPIO_11
	private static Pin spiMosi = RaspiPin.GPIO_12; // Pin #19, SPI0_MOSI
	private static Pin spiCs = RaspiPin.GPIO_10; // Pin #24, SPI0_CE0_N
	private static Pin spiRst = RaspiPin.GPIO_05; // Pin #18, GPIO_24
	private static Pin spiDc = RaspiPin.GPIO_04; // Pin #16, GPIO_23

			key1Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "K-1", PinPullResistance.PULL_UP);
			key1Pin.setShutdownOptions(true);
			key2Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "K-2", PinPullResistance.PULL_UP);

			 */
			String[] map = new String[7];
			map[0] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(clkPin)).pinNumber()) + ":CLK";
			map[1] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(mosiPin)).pinNumber()) + ":MOSI";
			map[2] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(csPin)).pinNumber()) + ":CS";
			map[3] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(rstPin)).pinNumber()) + ":RST";
			map[4] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(dcPin)).pinNumber()) + ":DC";

			map[5] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(k1Pin)).pinNumber()) + ":K-1";
			map[6] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(k2Pin)).pinNumber()) + ":K-2";

			PinUtil.print(map);
		}

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Screenbuffer ready...");
		}

		displayPage00(sb); // Init Screen

		if (oled != null) {
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
		} else {
			substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			substitute.display();
		}

		if (oled == null) { // Simulate button clicks every 5 seconds
			Thread buttonSimulator = new Thread(() -> {
				while (keepLooping) {
					TimeUtil.delay(10_000);
					currentIndex += 1;
				}
			});
			buttonSimulator.start();
		}

		while (keepLooping) {
			TimeUtil.delay(200);

			// Display data based on currentIndex
			int screenIndex = Math.abs(currentIndex % pageManagers.size());
			if (VERBOSE) {
				System.out.println(String.format("Current Screen Index now %d (%d) on a total of %d", screenIndex, currentIndex, pageManagers.size()));
			}

			pageManagers.get(screenIndex).accept(sb);

			if (oled != null) {
				oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				oled.display();
			} else {
				substitute.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
				substitute.display();
			}
		}
		System.out.println("End of loop");

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Done.");
		}

		if (oled == null) {
			System.exit(0);
		} else {
			oled.clear();
			sb.clear();
			oled.setBuffer(mirror ? ScreenBuffer.mirror(sb.getScreenBuffer(), WIDTH, HEIGHT) : sb.getScreenBuffer());
			oled.display();
			oled.shutdown();
		}

		if (gpio != null) {
			gpio.shutdown();
		}
	}
}
