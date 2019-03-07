package nmea.tcp.ssd1306_128x64;

import calc.GeomUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import http.HTTPServer;
import http.client.HTTPClient;
import lcd.ScreenBuffer;
import lcd.oled.SSD1306;
import lcd.substitute.SwingLedPanel;
import lcd.utils.img.ImgInterface;
import lcd.utils.img.Java32x32;
import utils.PinUtil;
import utils.StaticUtil;
import utils.TimeUtil;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Uses SPI interface
 */
public class TCPWatch {

	private static SwingLedPanel substitute;

	private static final int WIDTH = 128;
	private static final int HEIGHT = 64;

	// The data to display
	private static double latitude = 0;
	private static double longitude = 0;
	private static double sog = 0;
	private static double cog = 0;
	// TODO More data, more pages (like a track)

	private static boolean VERBOSE = "true".equals(System.getProperty("verbose", "false"));
	private static String BASE_URL = System.getProperty("base.url", "http://192.168.127.1:8080");

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

	/** This is the REST request
	 * @param baseUrl
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String data = (response != null ? new String(response.getPayload()) : null);

		Gson gson = new Gson();
		JsonElement element = gson.fromJson (data, JsonElement.class);
		JsonObject jsonObj = element.getAsJsonObject();
		return jsonObj;
	}


	private static boolean keepLooping = true;

	private static void displayPageOne(ScreenBuffer sb) {
		String title = "Screen #1";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		String latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
		String lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
		y += 8;
		sb.text(String.format("L:%11s   ", latStr), 2, y);
		y += 8;
		sb.text(String.format("G:%11s   ", lngStr), 2, y);
		y += 8;
		Date date = new Date();
		sb.text(SDF_1.format(date), 2, y);
		y += 8;
		sb.text(SDF_3.format(date), 2, y);
		y += 8;
		sb.text(String.format("Index: %d", currentIndex), 2, y);
	}

	private static void displayPageTwo(ScreenBuffer sb) {
		String title = "Screen #2";
		int y = 8;
		int len = sb.strlen(title);
		sb.text(title, (WIDTH / 2) - (len / 2), y);
		String latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
		String lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
		y += 8;
		sb.text(String.format("L:%11s   ", latStr), 2, y);
		y += 8;
		sb.text(String.format("G:%11s   ", lngStr), 2, y);
		y += 8;
		sb.text(String.format("SOG: %s kts      ", SOG_FMT.format(sog)), 2, y);
		y += 8;
		sb.text(String.format("COG: %s          ", COG_FMT.format(cog)), 2, y);
		y += 8;

		sb.text(String.format("Index: %d", currentIndex), 2, y);
	}

	public static void main(String... args) throws Exception {

		boolean mirror = "true".equals(System.getProperty("mirror.screen", "false")); // Screen is to be seen in a mirror.

		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Starting...");
		}

		try {
			gpio = GpioFactory.getInstance();

			// TODO Parameters for those 2 pins
			key1Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "K-1", PinPullResistance.PULL_UP);
			key1Pin.setShutdownOptions(true);
			key2Pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "K-2", PinPullResistance.PULL_UP);
			key2Pin.setShutdownOptions(true);

			key1Pin.addListener((GpioPinListenerDigital) event -> {
				if (key1Consumer != null) {
					key1Consumer.accept(event);
				}
			});
			key2Pin.addListener((GpioPinListenerDigital) event -> {
				if (key2Consumer != null) {
					key2Consumer.accept(event);
				}
			});
		} catch (Throwable error) {

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
			oled = new SSD1306(WIDTH, HEIGHT); // Default pins (look in the SSD1306 code)
			// If needed, override the default pins
			//                          Clock             MOSI              CS                RST               DC
//    oled = new SSD1306(RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
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
			System.out.println("Object created, default pins...");
			/* Defaults:
	private static Pin spiClk = RaspiPin.GPIO_14; // Pin #23, SCLK, GPIO_11
	private static Pin spiMosi = RaspiPin.GPIO_12; // Pin #19, SPI0_MOSI
	private static Pin spiCs = RaspiPin.GPIO_10; // Pin #24, SPI0_CE0_N
	private static Pin spiRst = RaspiPin.GPIO_05; // Pin #18, GPIO_24
	private static Pin spiDc = RaspiPin.GPIO_04; // Pin #16, GPIO_23
			 */
			String[] map = new String[5];
			map[0] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(14)).pinNumber()) + ":CLK";
			map[1] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(12)).pinNumber()) + ":MOSI";
			map[2] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(10)).pinNumber()) + ":CS";
			map[3] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(05)).pinNumber()) + ":RST";
			map[4] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByWiringPiNumber(04)).pinNumber()) + ":DC";

			PinUtil.print(map);
//    System.out.println("Object created, Clock GPIO_14, MOSI GPIO_12, CS GPIO_10, RST GPIO_05, DC GPIO_04");
		}

		ScreenBuffer sb = new ScreenBuffer(WIDTH, HEIGHT);
		sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
		if ("true".equals(System.getProperty("verbose", "false"))) {
			System.out.println("Screenbuffer ready...");
		}

		displayPageOne(sb); // Init Screen

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
					TimeUtil.delay(5_000);
					currentIndex += 1;
				}
			});
			buttonSimulator.start();
		}

		while (keepLooping) {
			TimeUtil.delay(200);

			// Display data based on currentIndex
			int screenIndex = Math.abs(currentIndex % 2);
			if (VERBOSE) {
				System.out.println(String.format("Current Index now %d (%d)", screenIndex, currentIndex));
			}
			switch (screenIndex) {

				case 0:
					if (VERBOSE) {
						System.out.println("Displaying Screen #1");
					}
					displayPageOne(sb);
					break;

				case 1:
					if (VERBOSE) {
						System.out.println("Displaying Screen #2");
					}
					displayPageTwo(sb);
					break;

				default:
					if (VERBOSE) {
						System.out.println("Displaying no Screen...");
					}
					break;
			}
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
			oled.shutdown();
		}

		if (gpio != null) {
			gpio.shutdown();
		}
	}
}
