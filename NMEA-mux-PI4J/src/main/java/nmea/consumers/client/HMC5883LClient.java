package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAReader;
import nmea.consumers.reader.HMC5883LReader;

/**
 * Reads a HMC5883L sensor, and produces valid NMEA XDR sentences.
 * Pitch & Roll.
 * Also available: HDM (heading).
 */
public class HMC5883LClient extends NMEAClient {
	public HMC5883LClient() {
		this(null, null, null);
	}

	public HMC5883LClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public HMC5883LClient(String s[], String[] sa) {
		this(s, sa, null);
	}

	public HMC5883LClient(String s[], String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = ("true".equals(System.getProperty("hmc5883l.data.verbose", "false")));
	}

	public String getSpecificDevicePrefix() {
		String dp = "";
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			dp = ((HMC5883LReader)reader).getDevicePrefix();
		}
		return dp;
	}

	public void setSpecificDevicePrefix(String dp) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			((HMC5883LReader)reader).setDevicePrefix(dp);
		}
	}

	public int getHeadingOffset() {
		int headingOffset = 0;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			headingOffset = ((HMC5883LReader)reader).getHeadingOffset();
		}
		return headingOffset;
	}

	public void setHeadingOffset(int headingOffset) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			((HMC5883LReader)reader).setHeadingOffset(headingOffset);
		}
	}

	public long getReadFrequency() {
		Long readFrequency = null;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			readFrequency = ((HMC5883LReader)reader).getReadFrequency();
		}
		return readFrequency;
	}

	public void setReadFrequency(Long readFrequency) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			((HMC5883LReader)reader).setReadFrequency(readFrequency);
		}
	}

	public int getDampingSize() {
		Integer dampingSize = null;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			dampingSize = ((HMC5883LReader)reader).getDampingSize();
		}
		return dampingSize;
	}

	public void setDampingSize(Integer dampingSize) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof HMC5883LReader) {
			((HMC5883LReader)reader).setDampingSize(dampingSize);
		}
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println(">> Received from HMC5883L:" + e.getContent());
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static HMC5883LClient nmeaClient = null;

	public static class HMC5883LBean implements ClientBean {
		private String cls;
		private String type = "hmc5883l";
		private boolean verbose;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private String devicePrefix;
		private int headingOffset;
		private Long readFrequency;
		private Integer dampingSize;

		public HMC5883LBean(HMC5883LClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
			devicePrefix = instance.getSpecificDevicePrefix();
			headingOffset = instance.getHeadingOffset();
			readFrequency = instance.getReadFrequency();
			dampingSize = instance.getDampingSize();
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

		public int getHeadingOffset() {
			return this.headingOffset;
		}

		public Long getReadFrequency() {
			return this.readFrequency;
		}

		public Integer getDampingSize() {
			return this.dampingSize;
		}
	}

	@Override
	public Object getBean() {
		return new HMC5883LBean(this);
	}

	public static void main(String... args) {
		System.out.println("HMC5883LClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("HMC5883LClient prm:" + s);
		}

		nmeaClient = new HMC5883LClient();

		Runtime.getRuntime().addShutdownHook(new Thread("HMC5883LClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new HMC5883LReader("HMC5883LReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
