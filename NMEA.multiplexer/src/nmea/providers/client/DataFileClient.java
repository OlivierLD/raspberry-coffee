package nmea.providers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.providers.reader.DataFileReader;

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

	public DataFileClient(String s, String[] sa) {
		this(s, sa, null);
	}

	public DataFileClient(String s, String[] sa, Multiplexer mux) {
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
		String cls;
		String file;
		String type = "file";
		boolean verbose;

		public DataFileBean(DataFileClient instance) {
			cls = instance.getClass().getName();
			file = ((DataFileReader) instance.getReader()).getFileNme();
			verbose = instance.isVerbose();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public String getFile() {
			return file;
		}

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}
	}

	@Override
	public Object getBean() {
		return new DataFileBean(this);
	}

	public static void main(String[] args) {
		System.out.println("CustomDataFileClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("CustomDataFileClient prm:" + s);

		String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
		if (args.length > 0)
			dataFile = args[0];

		nmeaClient = new DataFileClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new DataFileReader(nmeaClient.getListeners(), dataFile));
		nmeaClient.startWorking();
	}
}