package weatherstation.logger;

import org.json.JSONObject;
import weatherstation.logger.servers.HTTPServer;

/**
 * Uses -Dhttp.port, default 8080
 */

public class HTTPLogger implements LoggerInterface {

	private HTTPServer httpServer = null;

	public HTTPLogger() {
		try {
			httpServer = new HTTPServer(); // Created and started
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	@Override
	public void pushMessage(JSONObject json)
			throws Exception {
		if ("true".equals(System.getProperty("weather.station.verbose", "false"))) {
			System.out.println("-> Sending message (http)");
		}
		if (httpServer != null) {
			httpServer.setData(json.toString());
		}
	}

	@Override
	public void close() {
		System.out.println("(HTTP Logger) Bye!");
	}
}
