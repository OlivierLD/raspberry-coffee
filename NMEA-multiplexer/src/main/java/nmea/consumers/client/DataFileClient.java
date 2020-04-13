package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.DataFileReader;

/**
 * Read a file containing logged data for replay
 */
public class DataFileClient extends NMEAClient {
	private boolean loop = true;

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

	public boolean getLoop() {
		return this.loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println(">> DataFileClient >> Received from File:" + e.getContent());
		}
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
		private boolean loop = true;

		public DataFileBean(DataFileClient instance) {
			cls = instance.getClass().getName();
			file = ((DataFileReader) instance.getReader()).getFileNme();
			pause = ((DataFileReader) instance.getReader()).getBetweenRecord();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
			loop = instance.getLoop();
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
		public boolean getLoop() { return loop; }

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
		for (String s : args) {
			System.out.println("DataFileClient prm:" + s);
		}

		System.setProperty("file.data.verbose", "true");

		String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
		if (args.length > 0) {
			dataFile = args[0];
		}

		nmeaClient = new DataFileClient(null, new String[] { "RMC", "GLL" }, null);
		nmeaClient.setVerbose("true".equals(System.getProperty("file.data.verbose", "false")));

		Runtime.getRuntime().addShutdownHook(new Thread("DataFileClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new DataFileReader("DataFileReader", nmeaClient.getListeners(), dataFile, 10L)); // 10 overrides the default (500)
		nmeaClient.getReader().setVerbose("true".equals(System.getProperty("file.data.verbose", "false")));
		((DataFileReader)nmeaClient.getReader()).setLoop(false);
		nmeaClient.startWorking();
	}
}
