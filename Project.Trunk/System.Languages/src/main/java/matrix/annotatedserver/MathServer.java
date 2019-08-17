package matrix.annotatedserver;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

public class MathServer {
	private HTTPServer httpServer = null;
	private int httpPort = 2345;

	public MathServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format("Running on port %d", httpPort));
		this.httpServer = startHttpServer(httpPort, new MathRequestManager(this));
		// Add other features here if needed...
//	this.httpServer.addRequestManager(new AstroRequestManager());
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new MathServer();
	}

	public HTTPServer startHttpServer(int port, MathRequestManager requestManager) {
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
