package astrorest;

import http.HTTPServer;

public class AstroServer {

	// See http://maia.usno.navy.mil/ser7/deltat.data
	private double deltaT = Double.parseDouble(System.getProperty("deltaT", Double.toString(68.8033))); // June 2017

	//private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private AstroRequestManager requestManager;

	public AstroServer() {
		System.out.println(String.format("Using Delta-T:%f", deltaT));
		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.println(String.format("Running on port %d", port));
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
