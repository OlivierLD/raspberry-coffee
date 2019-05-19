package nmea.mux;

import http.RESTRequestManager;
import nmea.computers.Computer;
import context.ApplicationContext;
import http.HTTPServer;
import utils.DumpUtil;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAParser;
import nmea.forwarders.Forwarder;
import nmea.mux.context.Context;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <b>NMEA Multiplexer.</b><br>
 * Also contains the definition of the REST operations for admin purpose.<br>
 * See {@link RESTRequestManager} and {@link HTTPServer}.<br>
 * Also see below the definition of <code>List&lt;Operation&gt; operations</code>.
 */
public class GenericNMEAMultiplexer  implements RESTRequestManager, Multiplexer  {
	private HTTPServer adminServer = null;

	private List<NMEAClient> nmeaDataClients = new ArrayList<>();
	private List<Forwarder> nmeaDataForwarders = new ArrayList<>();
	private List<Computer> nmeaDataComputers = new ArrayList<>();

	private RESTImplementation restImplementation;

	/**
	 * Implements the management of the REST requests (see {@link RESTImplementation})
	 * Dedicated Admin Server.
	 * This method is called by the HTTPServer through the current RESTRequestManager
	 *
	 * @param request the parsed request.
	 * @return the response, along with its HTTP status code.
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
//	HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_IMPLEMENTED);
		HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
		if (this.verbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	@Override
	public List<HTTPServer.Operation> getRESTOperationList() {
		return restImplementation.getOperations();
	}

	@Override
	public synchronized void onData(String mess) {
		// To measure the flow (in bytes per time)
    Context.getInstance().addManagedBytes(mess.length());

		// Last sentence (inbound)
		Context.getInstance().setLastDataSentence(mess);

		if (this.verbose) {
			System.out.println("==== From MUX: " + mess);
			DumpUtil.displayDualDump(mess);
			System.out.println("==== End Mux =============");
		}
		// Cache, if initialized
		if (ApplicationContext.getInstance().getDataCache() != null) {
			ApplicationContext.getInstance().getDataCache().parseAndFeed(mess);
		}
		if (this.process) {
			// Computers. Must go first, as a computer may re-feed the present onData method.
			synchronized (nmeaDataComputers) {
			 	nmeaDataComputers.stream()
								.forEach(computer -> {
									computer.write(mess.getBytes());
								});
							}

			// Forwarders
			synchronized (nmeaDataForwarders) {
				nmeaDataForwarders.stream()
								.forEach(fwd -> {
									try {
										fwd.write((mess.trim() + NMEAParser.STANDARD_NMEA_EOS).getBytes());
									} catch (Exception e) {
										e.printStackTrace();
									}
			 					});
							}
		}
	}

	@Override
	public void setVerbose(boolean b) {
		this.verbose = b;
	}

	@Override
	public void setEnableProcess(boolean b) {
		this.process = b;
	}
	@Override
	public boolean getEnableProcess() {
		return this.process;
	}
	@Override
	public void stopAll() {
		// Send Ctrl+C
		softStop = true;
		terminateMux();
//	System.exit(0);
//	try {  Thread.sleep(2_500L); } catch (InterruptedException ie) {}
		System.out.println("Soft Exit");
		Runtime.getRuntime().exit(0); // Ctrl-C for the HTTP Server
	}

	private boolean verbose = "true".equals(System.getProperty("mux.data.verbose"));
	private boolean process = true; // onData, forward to computers and forwarders

	private boolean softStop = false;

	public void terminateMux() {
		System.out.println("Shutting down multiplexer nicely.");
    if (adminServer != null && softStop) {
			// Delay for the REST response
	//	System.out.println("Waiting a bit (for REST terminate request to complete)...");
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
				// Absorb
			}
//		System.out.println("Done waiting");
		}
		nmeaDataClients.stream()
						.forEach(NMEAClient::stopDataRead);
		nmeaDataForwarders.stream()
						.forEach(Forwarder::close);
		nmeaDataComputers.stream()
						.forEach(Computer::close);
		if (adminServer != null) {
			synchronized (adminServer) {
				System.out.println("Mux Stopping Admin server");
				adminServer.stopRunning();
			}
		}
	}
	/**
	 * Constructor.
	 * @param muxProps Initial config. See {@link #main(String...)} method.
	 */
	public GenericNMEAMultiplexer(Properties muxProps) {

		Context.getInstance().setStartTime(System.currentTimeMillis());

		// Read initial config from the properties file. See the main method.
		verbose = "true".equals(System.getProperty("mux.data.verbose", "false")); // Initial verbose.
		restImplementation = new RESTImplementation(nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this);
		MuxInitializer.setup(muxProps, nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (!softStop) {
				terminateMux();
			}
		}, "Multiplexer shutdown hook"));

		nmeaDataClients.stream()
						.forEach(client -> {
							try {
								client.startWorking();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
	}

	public void startAdminServer(int port) {
		try {
			this.adminServer = new HTTPServer(port, this);
			this.adminServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Properties getDefinitions() {
		String propertiesFile = System.getProperty("mux.properties", "nmea.mux.properties");

		Properties definitions = new Properties();
		File propFile = new File(propertiesFile);
		if (!propFile.exists()) {
			throw new RuntimeException(String.format("File [%s] not found", propertiesFile));
		} else {
			try {
				definitions.load(new FileReader(propFile));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return definitions;
	}

	/**
	 * Start the Multiplexer from here.
	 *
	 * @param args unused.
	 */
	public static void main(String... args) {
		Properties definitions = GenericNMEAMultiplexer.getDefinitions();

		boolean startProcessingOnStart = "true".equals(System.getProperty("process.on.start", "true"));
		GenericNMEAMultiplexer mux = new GenericNMEAMultiplexer(definitions);
		mux.setEnableProcess(startProcessingOnStart);
		// with.http.server=yes
		// http.port=9999
		if ("yes".equals(definitions.getProperty("with.http.server", "no"))) {
			mux.startAdminServer(Integer.parseInt(definitions.getProperty("http.port", "9999")));
		}
	}
}
