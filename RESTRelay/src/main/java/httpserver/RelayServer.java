package httpserver;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class RelayServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	public RelayServer() {

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
			this.httpServer = startHttpServer(httpPort, new RelayRequestManager(this));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new RelayServer();
	}

	public HTTPServer startHttpServer(int port, RelayRequestManager requestManager) {
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
