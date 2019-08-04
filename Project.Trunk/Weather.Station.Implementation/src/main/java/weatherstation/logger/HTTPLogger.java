package weatherstation.logger;

import org.json.JSONObject;
import weatherstation.logger.servers.HTTPServer;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Takes a snapshot through a REST service
 *
 * Uses:
 * -Dhttp.port, default 8080
 * -Dweather.station.verbose, default false
 * -Dsnap.verbose, default false
 */

public class HTTPLogger implements LoggerInterface {

	private HTTPServer httpServer = null;
	// The listener below can be used to trap given HTTP requests (like "take a snapshot", see below).
	private Consumer<HTTPServer.Request> listener = (HTTPServer.Request req) -> manageRequest(req);

	public HTTPLogger() {
		try {
			httpServer = new HTTPServer(); // Created and started
			httpServer.addListener(listener);
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

	/**
	 * Takes a snapshot when request is http://machine:port/snap
	 * @param req
	 */
	private void manageRequest(HTTPServer.Request req) {
//	System.out.print(">>> HTTP Request Intercepted >>> " + req.toString());
		if ("/snap".equals(req.getPath())) {
			// Take a snapshot
			System.out.println("Snapshot required");
			try {
				Map<String, String> queryStringParameters = req.getQueryStringParameters();
				if ("true".equals(System.getProperty("snap.verbose", "false"))) {
					queryStringParameters.keySet().stream().forEach(k -> {
						System.out.println(String.format("prm:%s => %s", k, queryStringParameters.get(k)));
					});
				}
				int rot = 0, width = 640, height = 480;
				if (queryStringParameters.containsKey("rot")) { // For example ?rot=180
					rot = Integer.parseInt(queryStringParameters.get("rot"));
					if ("true".equals(System.getProperty("snap.verbose", "false"))) {
						System.out.println(String.format(">> rot overridden [%d]", rot));
					}
				}
				if (queryStringParameters.containsKey("width")) {
					width = Integer.parseInt(queryStringParameters.get("width"));
					if ("true".equals(System.getProperty("snap.verbose", "false"))) {
						System.out.println(String.format(">> width overridden [%d]", width));
					}
				}
				if (queryStringParameters.containsKey("height")) {
					height = Integer.parseInt(queryStringParameters.get("height"));
					if ("true".equals(System.getProperty("snap.verbose", "false"))) {
						System.out.println(String.format(">> height overridden [%d]", height));
					}
				}
				String snapName = "snap-test";
				if (queryStringParameters.containsKey("name")) {
					snapName = queryStringParameters.get("name");
					if ("true".equals(System.getProperty("snap.verbose", "false"))) {
						System.out.println(String.format(">> name overridden [%s]", snapName));
					}
				}
				snap(snapName, rot, width, height);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// The --timeout seem to degrade the quality of the picture, specially outside...
	private final static String SNAPSHOT_COMMAND_1 = "raspistill -rot %d --width %d --height %d --output %s --nopreview"; // --timeout 1

	// For a webcam
	// Requires sudo apt-get install fswebcam
	// See http://www.raspberrypi.org/documentation/usage/webcams/ for some doc.
	private final static String SNAPSHOT_COMMAND_2 = "fswebcam snap%s.jpg";

	// Slow motion:
	private final static String SNAPSHOT_COMMAND_3 = "raspivid -w 640 -h 480 -fps 90 -t 30000 -o vid.h264";

	public static String snap(String name, int rot, int width, int height)
			throws Exception {
		Runtime rt = Runtime.getRuntime();
		// NOTE: The web directory has to exist on the Raspberry Pi
		String snapshotName = String.format("web/%s.jpg", name);
		try {
			String command = String.format(SNAPSHOT_COMMAND_1, rot, width, height, snapshotName);
			if ("true".equals(System.getProperty("snap.verbose", "false"))) {
				System.out.println("Snapshot name will be " + snapshotName);
				System.out.println(String.format("HTTPLogger Executing [%s]", command));
			}
			long before = System.currentTimeMillis();
			Process snap = rt.exec(command);
			snap.waitFor(); // Sync
			long after = System.currentTimeMillis();
		} catch (InterruptedException ie) {
			throw ie;
		} catch (IOException ioe) {
			throw ioe;
		}
		return snapshotName;
	}

}
