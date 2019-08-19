package restserver;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

public class PoloServer {
	private HTTPServer httpServer = null;
	private int httpPort = 2345;

	public PoloServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format("Running on port %d", httpPort));
		this.httpServer = startHttpServer(httpPort, new PoloRESTRequestManager(this));
		// Add other features here if needed...
//	this.httpServer.addRequestManager(new OtherRequestManager());
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new PoloServer();
	}

	public HTTPServer startHttpServer(int port, http.RESTRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
