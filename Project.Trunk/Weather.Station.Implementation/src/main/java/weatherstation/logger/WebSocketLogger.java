package weatherstation.logger;

import org.json.JSONObject;
import weatherstation.logger.servers.WebSocketFeeder;

import java.nio.channels.NotYetConnectedException;

/**
 * Uses -Dws.uri
 */

public class WebSocketLogger implements LoggerInterface {

	private WebSocketFeeder wsf = null;

	public WebSocketLogger() {
		try {

			wsf = new WebSocketFeeder(); // Uses -Dws.uri
			try {
				wsf.initWebSocketConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (NotYetConnectedException nyce) {
			nyce.printStackTrace();
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	@Override
	public void pushMessage(JSONObject json)
			throws Exception {
		if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
			System.out.println("-> Sending message (wsf)");
		}
		wsf.pushMessage(json.toString());
	}

	@Override
	public void close() {
		System.out.println("(WebSocket logger) Bye!");
		if (wsf != null) {
			wsf.shutdown();
		}
	}
}
