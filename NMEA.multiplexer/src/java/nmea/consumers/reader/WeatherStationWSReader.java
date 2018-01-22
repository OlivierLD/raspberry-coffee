package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;
import java.util.Properties;

/**
 * WebSocket reader
 */
public class WeatherStationWSReader extends NMEAReader {
	private WebSocketClient wsClient = null;
	private WeatherStationWSReader instance = this;
	private String wsUri;

	public WeatherStationWSReader(List<NMEAListener> al) {
		this(al, (Properties)null);
	}
	public WeatherStationWSReader(List<NMEAListener> al, Properties props) {
		this(al, props.getProperty("ws.uri"));
	}
	public WeatherStationWSReader(List<NMEAListener> al, String wsUri) {
		super(al);
		this.wsUri = wsUri;
		try {
			this.wsClient = new WebSocketClient(new URI(wsUri)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS On Open");
				}

				@Override
				public void onMessage(String mess) {
//        System.out.println("WS On Message");
					// TODO Transform into NMEA
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
					String s = mess + NMEAParser.NMEA_SENTENCE_SEPARATOR; // Blah blah blah
					NMEAEvent n = new NMEAEvent(this, s);
					instance.fireDataRead(n);
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS On Close");
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
