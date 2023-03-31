package sunflower.main;

import http.HTTPServer;
import sunflower.httpserver.FeatureRequestManager;
import utils.SystemUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Drives the stepper motors to orient the solar panel.
 * Provides a REST interface.
 *
 * @see sunflower.httpserver.FeatureRequestManager to see the available features
 * @see sunflower.httpserver.RESTImplementation to see how features are exposed through REST
 */
public class SunFlowerServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	public SunFlowerServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format(">>> Running on port %d", httpPort));
		try {
			// The FeatureRequestManager starts the SunFlowerDriver
			this.httpServer = startHttpServer(httpPort, new FeatureRequestManager(this));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		String ipAddress = "localhost";
		try {
			ipAddress = SystemUtils.getIPAddress();
		} catch (Exception ex) {
			// Duh
			System.err.println("Not on a Rpi?");
			if ("true".equals("http.verbose")) {
				ex.printStackTrace();
			}
		}
		System.out.println(String.format("Try REST request : GET http://%s:%d/sf/oplist", ipAddress, httpPort));
	}

	public List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public HTTPServer startHttpServer(int port, FeatureRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
	//		newHttpServer.stopRunning();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}

	public static void main(String... args) {
		new SunFlowerServer();
	}
}
