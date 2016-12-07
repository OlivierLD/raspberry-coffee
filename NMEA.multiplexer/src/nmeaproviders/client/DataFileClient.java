package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.DataFileReader;

/**
 * Read a file containing logged data
 */
public class DataFileClient extends NMEAClient {
	public DataFileClient() {
		super(null, null, null);
		this.verbose = "true".equals(System.getProperty("file.data.verbose", "false"));
	}

	public DataFileClient(Multiplexer mux) {
		super(mux);
		this.verbose = "true".equals(System.getProperty("file.data.verbose", "false"));
	}

	public DataFileClient(String s, String[] sa) {
		super(s, sa, null);
		this.verbose = "true".equals(System.getProperty("file.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from File:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static DataFileClient nmeaClient = null;

	public static class DataFileBean implements ClientBean {
		String cls;
		String file;
		String type = "file";

		public DataFileBean(DataFileClient instance) {
			cls = instance.getClass().getName();
			file = ((DataFileReader) instance.getReader()).getFileNme();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public String getFile() {
			return file;
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

	@Override
	public boolean isVerbose() {
		return this.verbose;
	}

	@Override
	public void setVerbose(boolean b) {
		this.verbose = b;
	}
}