package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.RESTReader;

/**
* Read NMEA Data from a REST server
*/
public class RESTClient extends NMEAClient {
	public RESTClient() {
		this(null, null, null);
	}

	public RESTClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public RESTClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public RESTClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("rest.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from REST :" + e.getContent());
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static RESTClient nmeaClient = null;

	public static class RESTBean implements ClientBean {
		private String cls;
		private String type = "rest";  // TODO Other parameters !!
		private String protocol = "http";
		private String hostname;
		private int port;
		private String queryPath = "/";
		private String queryString = "";
		private String verb = "GET"; // TODO See if more verbs are needed.
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public RESTBean(RESTClient instance) {
			cls = instance.getClass().getName();
			protocol = ((RESTReader) instance.getReader()).getProtocol();
			hostname = ((RESTReader) instance.getReader()).getHostname();
			port = ((RESTReader) instance.getReader()).getPort();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public int getPort() {
			return port;
		}

		public String getHostname() {
			return this.hostname;
		}

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
	}

	@Override
	public Object getBean() {
		return new RESTBean(this);
	}

	/**
	 * For standalone tests
	 * @param args Unused
	 */
	public static void main(String... args) {
		System.out.println("CustomRESTClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("CustomRESTClient prm:" + s);
		}
		String serverNameOrIP = "192.168.1.102";

		nmeaClient = new RESTClient();

		Runtime.getRuntime().addShutdownHook(new Thread("RESTClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				// nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new RESTReader("RESTReader", nmeaClient.getListeners(),
				"http",
				serverNameOrIP,
				8_080,
				"/oplist/",
				"",
				null));
		nmeaClient.startWorking();
	}
}
