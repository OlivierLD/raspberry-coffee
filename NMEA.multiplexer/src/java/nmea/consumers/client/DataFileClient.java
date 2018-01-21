package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.DataFileReader;

/**
 * Read a file containing logged data
 */
public class DataFileClient extends NMEAClient {
	public DataFileClient() {
		this(null, null, null);
	}

	public DataFileClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public DataFileClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public DataFileClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("file.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println(">> DataFileClient >> Received from File:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static DataFileClient nmeaClient = null;

	public static class DataFileBean implements ClientBean {
		private String cls;
		private String file;
		private long pause;
		private String type = "file";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public DataFileBean(DataFileClient instance) {
			cls = instance.getClass().getName();
			file = ((DataFileReader) instance.getReader()).getFileNme();
			pause = ((DataFileReader) instance.getReader()).getBetweenRecord();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public String getFile() {
			return file;
		}
		public long getPause() {
			return pause;
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
		return new DataFileBean(this);
	}

	public static void main(String... args) {
		System.out.println("DataFileClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("DataFileClient prm:" + s);

		String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
		if (args.length > 0)
			dataFile = args[0];

		nmeaClient = new DataFileClient(null, new String[] { "RMC", "GLL" }, null);

		Runtime.getRuntime().addShutdownHook(new Thread("DataFileClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new DataFileReader(nmeaClient.getListeners(), dataFile));
		nmeaClient.startWorking();
	}
}
