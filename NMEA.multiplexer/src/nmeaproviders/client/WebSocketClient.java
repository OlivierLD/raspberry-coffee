package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.WebSocketReader;

/**
 * Read NMEA Data from a WebSocket server
 */
public class WebSocketClient extends NMEAClient {
	public WebSocketClient() {
		super();
	}

	public WebSocketClient(Multiplexer mux) {
		super(mux);
	}

	public WebSocketClient(String s, String[] sa) {
		super(s, sa);
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if ("true".equals(System.getProperty("ws.data.verbose", "false")))
			System.out.println("Received from WebSocket :" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static WebSocketClient nmeaClient = null;

	public static class WSBean implements ClientBean {
		String cls;
		String type = "ws";
		String wsUri;

		public WSBean(WebSocketClient instance) {
			cls = instance.getClass().getName();
			wsUri = ((WebSocketReader) instance.getReader()).getWsUri();
		}

		@Override
		public String getType() { return this.type; }

		public String getWsUri() {
			return wsUri;
		}
	}

	@Override
	public Object getBean() {
		return new WSBean(this);
	}

	public static void main(String[] args) {
		String serverUri = "ws://localhost:9876/";

		nmeaClient = new WebSocketClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new WebSocketReader(nmeaClient.getListeners(), serverUri));
		nmeaClient.startWorking();
	}
}