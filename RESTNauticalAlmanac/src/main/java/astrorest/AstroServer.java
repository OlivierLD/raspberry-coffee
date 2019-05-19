package astrorest;

import http.HTTPServer;

public class AstroServer {

	//private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private AstroRequestManager requestManager;

	public AstroServer() {
		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.println(String.format("Running on port %d", httpPort));
		requestManager = new AstroRequestManager();
		this.httpServer = startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new AstroServer();
	}


	public HTTPServer startHttpServer(int port) {
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
