package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.WebSocketReader;

/**
 * Read NMEA Data from a WebSocket server
 */
public class WebSocketClient extends NMEAClient {
	public WebSocketClient() {
		this(null, null, null);
	}

	public WebSocketClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public WebSocketClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public WebSocketClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("ws.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from WebSocket :" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static WebSocketClient nmeaClient = null;

	public static class WSBean implements ClientBean {
		private String cls;
		private String type = "ws";
		private String wsUri;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public WSBean(WebSocketClient instance) {
			cls = instance.getClass().getName();
			wsUri = ((WebSocketReader) instance.getReader()).getWsUri();
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
		return new WSBean(this);
	}

	public static void main(String... args) {
		String serverUri = "ws://localhost:9876/";

		nmeaClient = new WebSocketClient();

		Runtime.getRuntime().addShutdownHook(new Thread("WebSocketClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new WebSocketReader(nmeaClient.getListeners(), serverUri));
		nmeaClient.startWorking();
	}
}
