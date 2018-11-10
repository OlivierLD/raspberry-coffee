package gribprocessing;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class GRIBServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	public GRIBServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format(">>> Running on port %d", httpPort));
		this.httpServer = startHttpServer(httpPort, new GRIBRequestManager(this));
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new GRIBServer();
	}

	public HTTPServer startHttpServer(int port, GRIBRequestManager requestManager) {
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
