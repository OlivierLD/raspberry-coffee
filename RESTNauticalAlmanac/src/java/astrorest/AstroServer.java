package astrorest;

import http.HTTPServer;

public class AstroServer {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
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
		requestManager = new AstroRequestManager();
		startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new AstroServer();
	}


	public void startHttpServer(int port) {
		try {
			this.httpServer = new HTTPServer(port, requestManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
