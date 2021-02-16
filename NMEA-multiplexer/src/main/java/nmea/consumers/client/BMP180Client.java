package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAReader;
import nmea.consumers.reader.BMP180Reader;

/**
 * Reads a BMP180 sensor, and produces a valid NMEA sentence.
 * Temperature, Pressure
 */
public class BMP180Client extends NMEAClient {
	public BMP180Client() {
		this(null, null, null);
	}

	public BMP180Client(Multiplexer mux) {
		this(null, null, mux);
	}

	public BMP180Client(String s[], String[] sa) {
		this(s, sa, null);
	}

	public BMP180Client(String s[], String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = ("true".equals(System.getProperty("bmp180.data.verbose", "false")));
	}

	public String getSpecificDevicePrefix() {
		String dp = "";
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof BMP180Reader) {
			dp = ((BMP180Reader)reader).getDevicePrefix();
		}
		return dp;
	}

	public void setSpecificDevicePrefix(String dp) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof BMP180Reader) {
			((BMP180Reader)reader).setDevicePrefix(dp);
		}
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println(">> Received from BMP180:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static BMP180Client nmeaClient = null;

	public static class BMP180Bean implements ClientBean {
		private String cls;
		private String type = "bmp180";
		private boolean verbose;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private String devicePrefix;

		public BMP180Bean(BMP180Client instance) {
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
		public String[] getDeviceFilters() { return this.deviceFilters; }

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; }

		public String getDevicePrefix() { return this.devicePrefix; }
	}

	@Override
	public Object getBean() {
		return new BMP180Bean(this);
	}

	/**
	 * For tests (TODO isolate?)
	 * @param args
	 */
	public static void main(String... args) {
		System.out.println("BMP180Client invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("BMP180Client prm:" + s);
		}
		nmeaClient = new BMP180Client();

		Runtime.getRuntime().addShutdownHook(new Thread("BMP180Client shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new BMP180Reader("BMP180Reader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
