package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

/**
 * WebSocket reader
 */
public class WebSocketReader extends NMEAReader {
	private WebSocketClient wsClient = null;
	private WebSocketReader instance = this;
	private String wsUri;

	public WebSocketReader(List<NMEAListener> al, String wsUri) {
		this(null, al, wsUri);
	}
	public WebSocketReader(String threadName, List<NMEAListener> al, String wsUri) {
		super(threadName, al);
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
					String s = mess + NMEAParser.NMEA_SENTENCE_SEPARATOR;
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
