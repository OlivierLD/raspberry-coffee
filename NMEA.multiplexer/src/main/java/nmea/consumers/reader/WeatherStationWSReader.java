package nmea.consumers.reader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * WebSocket reader
 */
public class WeatherStationWSReader extends NMEAReader {
	private WebSocketClient wsClient = null;
	private WeatherStationWSReader instance = this;
	private String wsUri;
	private boolean verbose = false;

	private final static String DEVICE_PREFIX = "WS"; // Weather Station

	public WeatherStationWSReader(List<NMEAListener> al) {
		this(al, (Properties)null);
	}
	public WeatherStationWSReader(List<NMEAListener> al, Properties props) {
		this(al, props.getProperty("ws.uri"));
		verbose = "true".equals(props.getProperty("ws.verbose"));
		if (verbose) {
			WebSocketImpl.DEBUG = true;
		}
	}
	public WeatherStationWSReader(List<NMEAListener> al, String wsUri) {
		super(al);
		this.wsUri = wsUri;
		try {
			this.wsClient = this.createWebSocketClient();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private WebSocketClient createWebSocketClient() throws URISyntaxException {
		return new WebSocketClient(new URI(wsUri)) {

			@Override
			public void onOpen(ServerHandshake serverHandshake) {
				System.out.println("WS On Open");
			}

			@Override
			public void onMessage(String mess) {
//        System.out.println("WS On Message");
				// Transform into NMEA
					/*
					   Received message is like
					   {  "dir": 350.0,
						    "avgdir": 345.67,
						    "volts": 3.4567,
						    "speed": 12.345,
						    "gust": 13.456,
						    "rain": 0.1,
						    "press": 101300.00,
						    "temp": 18.34,
						    "hum": 58.5,
						    "cputemp": 34.56 }
					 */

				if (verbose) {
					System.out.println(String.format(">> From WeatherStation: %s", mess));
				}

				JsonParser jsonParser = new JsonParser();
				JsonObject json = (JsonObject)jsonParser.parse(mess);

				double hum    = json.get("hum").getAsDouble();
				double volts  = json.get("volts").getAsDouble();
				double dir    = json.get("dir").getAsDouble();
				double avgdir = json.get("avgdir").getAsDouble();
				double speed  = json.get("speed").getAsDouble();
				double gust   = json.get("gust").getAsDouble();
				double rain   = json.get("rain").getAsDouble();
				double press  = json.get("press").getAsDouble();
				double temp   = json.get("temp").getAsDouble();

				int deviceIdx = 0; // Instead of "BME280" or so...
				String nmeaXDR = StringGenerator.generateXDR(DEVICE_PREFIX,
						new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
								hum,
								String.valueOf(deviceIdx++)), // %, Humidity
						new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
								temp,
								String.valueOf(deviceIdx++)), // Celcius, Temperature
						new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
								temp,
								String.valueOf(deviceIdx++)), // mm/h, Rain
						new StringGenerator.XDRElement(StringGenerator.XDRTypes.GENERIC,
								rain,
								String.valueOf(deviceIdx++))); // Pascal, pressure
				nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;

				if (verbose) {
					System.out.println(String.format(">>> Generated [%s]", nmeaXDR.trim()));
				}

				fireDataRead(new NMEAEvent(this, nmeaXDR));

				String nmeaMDA = StringGenerator.generateMDA(DEVICE_PREFIX,
						press / 100,
						temp,
						-Double.MAX_VALUE,  // Water Temp
						hum,
						-Double.MAX_VALUE,  // Abs hum
						-Double.MAX_VALUE,  // dew point
						avgdir,  // TWD
						-Double.MAX_VALUE,  // TWD (mag)
						speed); // TWS
				nmeaMDA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

				if (verbose) {
					System.out.println(String.format(">>> Generated [%s]", nmeaMDA.trim()));
				}

				instance.fireDataRead(new NMEAEvent(this, nmeaMDA));

				String nmeaMTA = StringGenerator.generateMTA(DEVICE_PREFIX, temp);
				nmeaMTA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

				if (verbose) {
					System.out.println(String.format(">>> Generated [%s]", nmeaMTA.trim()));
				}

				instance.fireDataRead(new NMEAEvent(this, nmeaMTA));

				String nmeaMMB = StringGenerator.generateMMB(DEVICE_PREFIX, press / 100);
				nmeaMMB += NMEAParser.NMEA_SENTENCE_SEPARATOR;

				if (verbose) {
					System.out.println(String.format(">>> Generated [%s]", nmeaMMB.trim()));
				}

				instance.fireDataRead(new NMEAEvent(this, nmeaMMB));
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				System.out.println(String.format("WS On Close - Code: %d, Reason: %s, remote: %s", code, reason, String.valueOf(remote)));
				if (code == CloseFrame.ABNORMAL_CLOSE || code == CloseFrame.NEVER_CONNECTED) {
					long delay = 5_000L;
					System.out.println(String.format("Abnormal close, retrying in %d milliseconds.", delay));
					Thread connector = new Thread(() -> {
						try {
							Thread.sleep(delay);
							instance.wsClient = instance.createWebSocketClient();
							instance.wsClient.connect();
							System.out.println(">> Reconnected <<");
						} catch (URISyntaxException | InterruptedException e) {
							e.printStackTrace();
						}
					});
					connector.start();
				}
			}

			@Override
			public void onError(Exception exception) {
				System.err.println("==== WS On Error ====");
				exception.printStackTrace();
				System.err.println("=====================");
			}
		};
	}

	@Override
	public void startReader() {
		super.enableReading();
		this.wsClient.connect();
	}

	public String getWsUri() {
		return this.wsUri;
	}

	@Override
	public void closeReader() throws Exception {
		if (this.wsClient != null) {
			this.wsClient.close();
		}
	}
}
