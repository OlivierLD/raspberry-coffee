package navrest;

import astrorest.AstroRequestManager;
import gribprocessing.GRIBRequestManager;
import http.HTTPServer;
import imageprocessing.ImgRequestManager;
import nmea.api.Multiplexer;
import nmea.mux.GenericNMEAMultiplexer;
import orientation.SunFlower;
import orientation.SunFlowerRequestManager;
import tiderest.TideRequestManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Entry point. Very Simple.
 * The complexity is somewhere else.
 *
 * Gathers other REST Services, all in one place.
 * See RESTTideEngine, RESTNauticalAlmanac, etc.
 */
public class NavServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	private Multiplexer multiplexer;

	public NavServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.println(String.format("From %s, running on port %d", this.getClass().getName(), httpPort));
		this.httpServer = startHttpServer(httpPort, new NavRequestManager(this));
		// Add astronomical features...
		System.out.println(String.format("\t>> %s - adding AstroRequestManager", NumberFormat.getInstance().format(System.currentTimeMillis())));
		this.httpServer.addRequestManager(new AstroRequestManager());
		// Add tide features...
		System.out.println(String.format("\t>> %s - adding TideRequestManager", NumberFormat.getInstance().format(System.currentTimeMillis())));
		this.httpServer.addRequestManager(new TideRequestManager());
		// Add Nav features: Dead Reckoning, logging, re-broadcasting, from the NMEA Multiplexer
		Properties definitions = GenericNMEAMultiplexer.getDefinitions();
		multiplexer = new GenericNMEAMultiplexer(definitions);
		System.out.println(String.format("\t>> %s - adding GenericNMEAMultiplexer", NumberFormat.getInstance().format(System.currentTimeMillis())));
		this.httpServer.addRequestManager((GenericNMEAMultiplexer)multiplexer); // refers to nmea.mux.properties, unless -Dmux.properties is set
		// Add image processing service...
		System.out.println(String.format("\t>> %s - adding ImgRequestManager", NumberFormat.getInstance().format(System.currentTimeMillis())));
		this.httpServer.addRequestManager(new ImgRequestManager());
		// Add GRIB features
		System.out.println(String.format("\t>> %s - adding GRIBRequestManager", NumberFormat.getInstance().format(System.currentTimeMillis())));
		this.httpServer.addRequestManager(new GRIBRequestManager());
		// Add SunFlower, for sun data, if needed
		if ("true".equals(System.getProperty("with.sun.flower", "false"))) {
			System.out.println(String.format("\t>> %s - adding SunFlowerRequestManager", NumberFormat.getInstance().format(System.currentTimeMillis())));
			this.httpServer.addRequestManager(new SunFlowerRequestManager());
		}
		System.out.println(String.format("\t>> %s - End of NavServer constructor", NumberFormat.getInstance().format(System.currentTimeMillis())));
	}

	public Multiplexer getMultiplexer() {
		return this.multiplexer;
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
			newHttpServer.startServer();
			System.out.println(String.format("\t>> %s - Starting HTTP server", NumberFormat.getInstance().format(System.currentTimeMillis())));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
