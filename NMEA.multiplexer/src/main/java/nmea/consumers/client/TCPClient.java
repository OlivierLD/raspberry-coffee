package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.TCPReader;

/**
 * Read NMEA Data from a TCP server
 */
public class TCPClient extends NMEAClient {
	public TCPClient() {
		this(null, null, null);
	}

	public TCPClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public TCPClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public TCPClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("tcp.data.verbose", "false"));
	}

		@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from TCP :" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static TCPClient nmeaClient = null;

	public static class TCPBean implements ClientBean {
		private String cls;
		private String type = "tcp";
		private int port;
		private String hostname;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public TCPBean(TCPClient instance) {
			cls = instance.getClass().getName();
			port = ((TCPReader) instance.getReader()).getPort();
			hostname = ((TCPReader) instance.getReader()).getHostname();
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
		return new TCPBean(this);
	}

	public static void main(String... args) {
		System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("CustomTCPClient prm:" + s);

		String serverName = "192.168.42.2";

		nmeaClient = new TCPClient();

		Runtime.getRuntime().addShutdownHook(new Thread("TCPClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new TCPReader("TCPReader", nmeaClient.getListeners(), serverName, 7001));
		nmeaClient.startWorking();
	}
}
