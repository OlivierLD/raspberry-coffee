package sunflower.httpserver;

import http.HTTPServer;
import utils.StaticUtil;
import utils.TCPUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
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
			this.httpServer = startHttpServer(httpPort, new FeatureRequestManager(this));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		String ipAddress = "localhost";
		try {
			ipAddress = TCPUtils.getIPAddress();
		} catch (Exception ex) {
			// Duh
			ex.printStackTrace();
		}
		System.out.println(String.format("Try REST request : GET http://%s:%d/sf/oplist", ipAddress, httpPort));
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new SunFlowerServer();
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
}
