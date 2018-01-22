package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.WeatherStationWSReader;

import java.util.Properties;

/**
 * Read WeatherStation Data from its WebSocket server, and turns them into NMEA data.
 */
public class WeatherStationWSClient extends NMEAClient {
	public WeatherStationWSClient() {
		this(null, null, null);
	}

	public WeatherStationWSClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public WeatherStationWSClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public WeatherStationWSClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("weather.station.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from WebSocket :" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);
	}

	private static WeatherStationWSClient nmeaClient = null;

	public static class WeatherStationBean implements ClientBean {
		private String cls;
		private String type = "weather.station";
		private String wsUri;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public WeatherStationBean(WeatherStationWSClient instance) {
			cls = instance.getClass().getName();
			wsUri = ((WeatherStationWSReader) instance.getReader()).getWsUri();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		@Override
		public String getType() { return this.type; }

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}

		public String getWsUri() {
			return wsUri;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
	}

	@Override
	public Object getBean() {
		return new WeatherStationBean(this);
	}

	public static void main(String... args) {
		String serverUri = "ws://localhost:9876/";

		nmeaClient = new WeatherStationWSClient();

		Runtime.getRuntime().addShutdownHook(new Thread("WebSocketClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new WeatherStationWSReader(nmeaClient.getListeners(), serverUri));
		nmeaClient.startWorking();
	}
}
