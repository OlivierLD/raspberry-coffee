package cam;

import http.HTTPServer;

public class CamServer {

	//private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private CamRequestManager requestManager;

	public CamServer() {
		String port = System.getProperty("http.port", "9999");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.println(String.format("Running on port %d", httpPort));
		requestManager = new CamRequestManager();
		this.httpServer = startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new CamServer();
	}

	public HTTPServer startHttpServer(int port) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager) {
				public void onExit() {
					// Reset the servos to zero before closing.
					System.out.println(">> Reseting servos before shutting down.");
					requestManager.stopServos();
					try {
						Thread.sleep(2_000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			};
			newHttpServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
