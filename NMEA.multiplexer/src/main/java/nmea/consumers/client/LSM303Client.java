package nmea.consumers.client;

import i2c.sensor.LSM303;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAReader;
import nmea.consumers.reader.LSM303Reader;

/**
 * Reads a LSM303 sensor, and produces valid NMEA XDR sentences.
 * Pitch & Roll.
 * Also available: HDM (heading).
 */
public class LSM303Client extends NMEAClient {
	public LSM303Client() {
		this(null, null, null);
	}

	public LSM303Client(Multiplexer mux) {
		this(null, null, mux);
	}

	public LSM303Client(String s[], String[] sa) {
		this(s, sa, null);
	}

	public LSM303Client(String s[], String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = ("true".equals(System.getProperty("lsm303.data.verbose", "false")));
	}

	public String getSpecificDevicePrefix() {
		String dp = "";
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			dp = ((LSM303Reader)reader).getDevicePrefix();
		}
		return dp;
	}

	public void setSpecificDevicePrefix(String dp) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setDevicePrefix(dp);
		}
	}

	public void setDeviceFeature(String feature) {
		NMEAReader reader = this.getReader();
		LSM303.EnabledFeature found = null;
		for(LSM303.EnabledFeature type : LSM303.EnabledFeature.values()) {
			if (type.toString().equals(feature)) {
				found = type;
				break;
			}
		}
		if (found == null) {
			throw new RuntimeException(String.format("%s is not a valid LSM303.EnabledFeature", feature));
		}
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setDeviceFeature(found);
		}
	}

	public int getHeadingOffset() {
		int headingOffset = 0;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			headingOffset = ((LSM303Reader)reader).getHeadingOffset();
		}
		return headingOffset;
	}

	public void setHeadingOffset(int headingOffset) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setHeadingOffset(headingOffset);
		}
	}

	public long getReadFrequency() {
		Long readFrequency = null;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			readFrequency = ((LSM303Reader)reader).getReadFrequency();
		}
		return readFrequency;
	}

	public void setReadFrequency(Long readFrequency) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setReadFrequency(readFrequency);
		}
	}

	public int getDampingSize() {
		Integer dampingSize = null;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			dampingSize = ((LSM303Reader)reader).getDampingSize();
		}
		return dampingSize;
	}

	public void setDampingSize(Integer dampingSize) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setDampingSize(dampingSize);
		}
	}

	public LSM303.EnabledFeature getLSM303Feature() {
		LSM303.EnabledFeature feature = null;
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			feature = ((LSM303Reader)reader).getDeviceFeature();
		}
		return feature;
	}

	public void setLSM303Feature(LSM303.EnabledFeature feature) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof LSM303Reader) {
			((LSM303Reader)reader).setDeviceFeature(feature);
		}
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println(">> Received from LSM303:" + e.getContent());
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static LSM303Client nmeaClient = null;

	public static class LSM303Bean implements ClientBean {
		private String cls;
		private String type = "lsm303";
		private boolean verbose;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private String devicePrefix;
		private int headingOffset;
		private Long readFrequency;
		private Integer dampingSize;
		private LSM303.EnabledFeature feature = LSM303.EnabledFeature.BOTH;

		public LSM303Bean(LSM303Client instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
			devicePrefix = instance.getSpecificDevicePrefix();
			headingOffset = instance.getHeadingOffset();
			readFrequency = instance.getReadFrequency();
			dampingSize = instance.getDampingSize();
			feature = instance.getLSM303Feature();
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

		public LSM303.EnabledFeature getLSM303Feature() { return this.feature; }
	}

	@Override
	public Object getBean() {
		return new LSM303Bean(this);
	}

	public static void main(String... args) {
		System.out.println("LSM303Client invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("LSM303Client prm:" + s);
		}

		nmeaClient = new LSM303Client();

		Runtime.getRuntime().addShutdownHook(new Thread("LSM303Client shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new LSM303Reader("LSM303Reader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
