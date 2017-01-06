package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.HTU21DFReader;

/**
 * Reads a HTU21DF sensor, and produces a valid NMEA sentence.
 */
public class HTU21DFClient extends NMEAClient {
	public HTU21DFClient() {
		this(null, null, null);
	}

	public HTU21DFClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public HTU21DFClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public HTU21DFClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("htu21df.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from HTU21DF:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static HTU21DFClient nmeaClient = null;

	public static class HTU21DFBean implements ClientBean {
		private String cls;
		private String type = "hut21df";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public HTU21DFBean(HTU21DFClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		@Override
		public String getType() {
			return this.type;
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
		return new HTU21DFBean(this);
	}

	public static void main(String[] args) {
		System.out.println("HTU21DFClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("HTU21DFClient prm:" + s);

		nmeaClient = new HTU21DFClient();

		Runtime.getRuntime().addShutdownHook(new Thread("HTU21DFClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new HTU21DFReader(nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}