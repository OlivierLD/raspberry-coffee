package cam;

import http.HTTPServer;

public class CamServer {

	//private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private CamRequestManager requestManager;

	public CamServer() {
		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.println(String.format("Running on port %d", port));
		requestManager = new CamRequestManager();
		this.httpServer = startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new CamServer();
	}


	public HTTPServer startHttpServer(int port) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
