package httpserver;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Server Main Class
 */
public class HttpRequestServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9_999;

	public HttpRequestServer() {

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
			this.httpServer = startHttpServer(httpPort, new HttpRequestManager(this));
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
		new HttpRequestServer();
	}

	public HTTPServer startHttpServer(int port, HttpRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager) {
				@Override
				public void onExit() {
					requestManager.onExit();
				}
			};
			newHttpServer.startServer();
//		newHttpServer.stopRunning();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
