package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAReader;
import nmea.consumers.reader.ZDAReader;

/**
 * Generates ZDA numbers, in a valid NMEA Sentence.
 */
public class ZDAClient extends NMEAClient {
	public ZDAClient() {
		this(null, null, null);
	}

	public ZDAClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public ZDAClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public ZDAClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("zda.data.verbose", "false"));
	}

	public String getSpecificDevicePrefix() {
		String dp = "";
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof ZDAReader) {
			dp = ((ZDAReader)reader).getDevicePrefix();
		}
		return dp;
	}

	public void setSpecificDevicePrefix(String dp) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof ZDAReader) {
			((ZDAReader)reader).setDevicePrefix(dp);
		}
	}


	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Generated from ZDA:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static ZDAClient nmeaClient = null;

	public static class ZDABean implements ClientBean {
		private String cls;
		private String type = "zda";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private String devicePrefix;
		private boolean verbose;

		public ZDABean(ZDAClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
			devicePrefix = instance.getSpecificDevicePrefix();
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

		public String getDevicePrefix() { return this.devicePrefix; }
	}

	@Override
	public Object getBean() {
		return new ZDABean(this);
	}

	public static void main(String... args) {
		System.out.println("ZDAClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("ZDAClient prm:" + s);
		}
		nmeaClient = new ZDAClient();

		Runtime.getRuntime().addShutdownHook(new Thread("ZDAClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new ZDAReader("ZDAReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
