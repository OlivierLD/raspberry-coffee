package navrest;

import astrorest.AstroRequestManager;
import http.HTTPServer;
import nmea.mux.GenericNMEAMultiplexer;
import tiderest.TideRequestManager;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Gathers other REST Services, all in one place.
 * See RESTTideEngine, RESTNauticalAlmanac, etc.
 */
public class NavServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	public NavServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format("Running on port %d", httpPort));
		this.httpServer = startHttpServer(httpPort, new NavRequestManager(this));
		// Add astronomical features...
		this.httpServer.addRequestManager(new AstroRequestManager());
		// Add tide features...
		this.httpServer.addRequestManager(new TideRequestManager());
		// Add Dead Reckoning, logging, re-broadcasting, from the NMEA Multiplexer
		Properties definitions = GenericNMEAMultiplexer.getDefinitions();
		this.httpServer.addRequestManager(new GenericNMEAMultiplexer(definitions));
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new NavServer();
	}

	public HTTPServer startHttpServer(int port, NavRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
