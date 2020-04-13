package nmea.tcp.waveshare;

import calc.GeomUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import http.HTTPServer;
import http.client.HTTPClient;
import spi.lcd.waveshare.LCD1in3;
import spi.lcd.waveshare.fonts.Font;
import spi.lcd.waveshare.fonts.Font24;
import utils.TimeUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static spi.lcd.waveshare.LCD1in3.DrawFill;

/**
 * TCP/REST Display Client for an NMEA Logger/REST Server
 *
 * For the small waveshare color display (1.3")
 * available at https://www.waveshare.com/product/modules/oleds-lcds/raspberry-pi-lcd/1.3inch-lcd-hat.htm
 */
public class TCPWatch {

	private static String BASE_URL = System.getProperty("base.url", "http://192.168.50.10:9999");
	private static boolean VERBOSE = "true".equals(System.getProperty("verbose", "false"));

	private final static SimpleDateFormat SDF_1 = new SimpleDateFormat("E dd MMM yyyy");
	private final static SimpleDateFormat SDF_2 = new SimpleDateFormat("HH:mm:ss Z");
	private final static SimpleDateFormat SDF_3 = new SimpleDateFormat("HH:mm:ss z");

	private final static SimpleDateFormat SDF_HH = new SimpleDateFormat("HH");
	private final static SimpleDateFormat SDF_MM = new SimpleDateFormat("mm");
	private final static SimpleDateFormat SDF_SS = new SimpleDateFormat("ss");

	private final static NumberFormat SOG_FMT = new DecimalFormat("#0.00");
	private final static NumberFormat COG_FMT = new DecimalFormat("000");

	private static int currentIndex = 0; // Screen index, incremented/decremented with the buttons K1 (up) & K3 (down)

	private static boolean k1 = false, k2 = false, k3 = false, jUp = false, jDown = false, jRight = false, jLeft = false, jPressed = false;
	private static Consumer<GpioPinDigitalStateChangeEvent> key1Consumer = (event) -> {
		k1 = event.getState().isLow();
		if (VERBOSE) {
			System.out.println(String.format("K1 was %s", k1 ? "pushed" : "released"));
		}
		if (k1) { // K1 is pushed down
			currentIndex++;
		}
	};
	private static Consumer<GpioPinDigitalStateChangeEvent> key2Consumer = (event) -> k2 = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> key3Consumer = (event) -> {
		k3 = event.getState().isLow();
		if (VERBOSE) {
			System.out.println(String.format("K3 was %s", k3 ? "pushed" : "released"));
		}
		if (k3) { // K3 is pushed down
			currentIndex--;
		}
	};

	private static Consumer<GpioPinDigitalStateChangeEvent> jUpConsumer = (event) -> jUp = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jDownConsumer = (event) -> jDown = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jLeftConsumer = (event) -> jLeft = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jRightConsumer = (event) -> jRight = event.getState().isLow();
	private static Consumer<GpioPinDigitalStateChangeEvent> jPressedConsumer = (event) -> jPressed = event.getState().isLow();

	private static boolean keepLooping = true;

	/**
	 * This is the REST request
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

	// The data to display
	private static double latitude = 0;
	private static double longitude = 0;
	private static double sog = 0;
	private static double cog = 0;
	// TODO More data, more pages (like a track)

	public static void main(String... args) {

		LCD1in3 lcd = new LCD1in3(LCD1in3.HORIZONTAL, LCD1in3.BLACK);
		// Key listeners
		lcd.setKey1Consumer(key1Consumer);
		lcd.setKey2Consumer(key2Consumer);
		lcd.setKey3Consumer(key3Consumer);
		lcd.setJUpConsumer(jUpConsumer);
		lcd.setJDownConsumer(jDownConsumer);
		lcd.setJLeftConsumer(jLeftConsumer);
		lcd.setJRightConsumer(jRightConsumer);
		lcd.setJPressedConsumer(jPressedConsumer);

		lcd.GUINewImage(LCD1in3.IMAGE_RGB, LCD1in3.LCD_WIDTH, LCD1in3.LCD_HEIGHT, LCD1in3.IMAGE_ROTATE_0, LCD1in3.IMAGE_COLOR_POSITIVE);

		// Display data, character mode
		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
		}

		// Double frame
		lcd.GUIDrawRectangle(2, 2, 238, 238, LCD1in3.YELLOW, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);
		lcd.GUIDrawRectangle(4, 4, 236, 236, LCD1in3.YELLOW, DrawFill.DRAW_FILL_EMPTY, LCD1in3.DotPixel.DOT_PIXEL_1X1);

		final int fontSize = 24;
		Font font = LCD1in3.findFontBySize(fontSize);
		int y = 8; // Top of the line

		int titlePos = 0, line1 = 0, line2 = 0, line3 = 0, line4 = 0, line5 = 0; // For refresh

		String title = "Screen #1";
		int len = font.strlen(title);
		int lineStart = (LCD1in3.LCD_WIDTH / 2) - (len / 2); // Centered
		titlePos = y;
		lcd.GUIDrawString(lineStart, y, title, font, LCD1in3.BLACK, LCD1in3.YELLOW);
		y += fontSize;
		String latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
		String lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
		lcd.GUIDrawString(8, y, String.format("L:%11s", latStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
		line1 = y;
		y += fontSize;
		lcd.GUIDrawString(8, y, String.format("G:%11s", lngStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
		line2 = y;
		y += fontSize;
		Date date = new Date();
		lcd.GUIDrawString(8, y, SDF_1.format(date), font, LCD1in3.BLACK, LCD1in3.YELLOW);
		line3 = y;
		y += fontSize;
		lcd.GUIDrawString(8, y, SDF_3.format(date), font, LCD1in3.BLACK, LCD1in3.YELLOW);
		line4 = y;
		y += fontSize;
		lcd.GUIDrawString(8, y, String.format("Index: %d", currentIndex), font, LCD1in3.BLACK, LCD1in3.YELLOW);
		line5 = y;
		// y += fontSize;

		if (!lcd.isSimulating()) {
			lcd.LCDDisplay();
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


				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String prettyJson = gson.toJson(response);

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
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					cog = response.get("COG").getAsJsonObject().get("angle").getAsDouble();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

//			System.out.println(">> Data:" + prettyJson);
			}
		}, "dataFetcher");
		dataFetcher.start();


		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nCtrl+C !");
			keepLooping = false;
			TimeUtil.delay(15_000);// Wait for the screen to shut off
		}, "Shutdown Hook"));

		// Display Data loop
		while (keepLooping) {
			TimeUtil.delay(10);

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
					title = "Screen #1";
					len = font.strlen(title);
					lineStart = (LCD1in3.LCD_WIDTH / 2) - (len / 2); // Centered
					lcd.GUIDrawString(lineStart, titlePos, title, font, LCD1in3.BLACK, LCD1in3.YELLOW);
					// Update position
					latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
					lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
					lcd.GUIDrawString(8, line1, String.format("L:%11s", latStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
					lcd.GUIDrawString(8, line2, String.format("G:%11s", lngStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
					Date now = new Date();
					lcd.GUIDrawString(8, line3, SDF_1.format(now), font, LCD1in3.BLACK, LCD1in3.RED);
					lcd.GUIDrawString(8, line4, SDF_3.format(now), font, LCD1in3.BLACK, LCD1in3.RED);
					lcd.GUIDrawString(8, line5, String.format("Index: %d  ", currentIndex), font, LCD1in3.BLACK, LCD1in3.GREEN);

					lcd.LCDDisplayWindows(8, titlePos, 235, titlePos + (6 * fontSize));
					break;

				case 1:
					if (VERBOSE) {
						System.out.println("Displaying Screen #2");
					}
					title = "Screen #2";
					len = font.strlen(title);
					lineStart = (LCD1in3.LCD_WIDTH / 2) - (len / 2); // Centered
					lcd.GUIDrawString(lineStart, titlePos, title, font, LCD1in3.BLACK, LCD1in3.YELLOW);
					// Update position
					latStr = GeomUtil.decToSex(latitude, GeomUtil.NO_DEG, GeomUtil.NS);
					lngStr = GeomUtil.decToSex(longitude, GeomUtil.NO_DEG, GeomUtil.EW);
					lcd.GUIDrawString(8, line1, String.format("L:%11s", latStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
					lcd.GUIDrawString(8, line2, String.format("G:%11s", lngStr), font, LCD1in3.BLACK, LCD1in3.YELLOW);
					lcd.GUIDrawString(8, line3, String.format("SOG: %s kts", SOG_FMT.format(sog)), font, LCD1in3.BLACK, LCD1in3.RED);
					lcd.GUIDrawString(8, line4, String.format("COG: %s    ", COG_FMT.format(cog)), font, LCD1in3.BLACK, LCD1in3.RED);
					lcd.GUIDrawString(8, line5, String.format("Index: %d  ", currentIndex), font, LCD1in3.BLACK, LCD1in3.GREEN);

					lcd.LCDDisplayWindows(8, titlePos, 235, titlePos + (6 * fontSize));
					break;

				default:
					if (VERBOSE) {
						System.out.println("Displaying no Screen...");
					}
					break;
			}
		}
		System.out.println("End of loop");

		if (!lcd.isSimulating()) {
			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			String bye = "Bye!";
			Font f24 = Font24.getInstance();
			int strlen = f24.getWidth() * bye.length();
			int strX = 120 - (strlen / 2);
			int strY = 120 - (f24.getHeight() / 2);

			int bg = LCD1in3.rgb(200, 200, 200);
			lcd.GUIDrawCircle(120, 120, (int)((strlen * 1.5) / 2), bg, DrawFill.DRAW_FILL_FULL, LCD1in3.DotPixel.DOT_PIXEL_1X1);
			lcd.GUIDrawString(strX, strY, bye, f24, bg, LCD1in3.BLACK);
			lcd.LCDDisplay();
			TimeUtil.delay(1_000);

			lcd.LCDClear(LCD1in3.BLACK);
			lcd.GUIClear(LCD1in3.BLACK);
			lcd.LCDDisplay();

//		TimeUtil.delay(1_000);
			lcd.shutdown();
		}
		System.out.println("End of Sample");
		System.out.println("Bye.");
	}
}
