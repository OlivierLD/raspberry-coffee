package nmea.forwarders;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Properties;

public class WebSocketWriter implements Forwarder {
	private WebSocketClient wsClient = null;
	private boolean isConnected = false;
	private String wsUri;
	private Properties props = null;

	/**
	 * @param serverURL like ws://hostname:port/
	 * @throws Exception when it fails
	 */
	public WebSocketWriter(String serverURL) throws Exception {
		this.wsUri = serverURL;
		try {
			wsClient = new WebSocketClient(new URI(serverURL)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS On Open");
					isConnected = true;
				}

				@Override
				public void onMessage(String string) {
//                System.out.println("WS On Message");
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS On Close");
					isConnected = false;
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
			wsClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getWsUri() {
		return this.wsUri;
	}

	@Override
	public void write(byte[] message) {
		try {
			String mess = new String(message);
			if (!mess.isEmpty() && isConnected) {
				this.wsClient.send(mess);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.wsClient.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class WSBean {
		private String cls;
		private String wsUri;
		private String type = "ws";

		public WSBean(WebSocketWriter instance) {
			cls = instance.getClass().getName();
			wsUri = instance.wsUri;
		}

		public String getWsUri() {
			return wsUri;
		}
	}

	@Override
	public Object getBean() {
		return new WSBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
