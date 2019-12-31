package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.SerialReader;

/**
 * Read NMEA Data from a Serial port
 */
public class SerialClient extends NMEAClient {
	private String clientName; // TODO Put this in the supertype?

	public SerialClient() {
		this(null, null, null);
	}

	public SerialClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public SerialClient(String s[], String[] sa) {
		this(s, sa, null);
	}

	public SerialClient(String s[], String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("serial.data.verbose", "false"));
		this.clientName = String.valueOf(System.currentTimeMillis()) ; // ((SerialReader) this.getReader()).getPort();
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			if (this.getReader() != null) {
				this.clientName = ((SerialReader) this.getReader()).getPort();
			}
			System.out.println(String.format("Received from Serial (%s): %s", this.clientName, e.getContent()));
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static SerialClient nmeaClient = null;

	public static class SerialBean implements ClientBean {
		private String cls;
		private String type = "serial";
		private String port;
		private int br;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public SerialBean(SerialClient instance) {
			cls = instance.getClass().getName();
			port = ((SerialReader) instance.getReader()).getPort();
			br = ((SerialReader) instance.getReader()).getBr();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public String getPort() {
			return port;
		}

		public int getBr() {
			return br;
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
		return new SerialBean(this);
	}

	/*
	 * For tests.
	 */
	public static void main(String... args) {
		System.out.println("SerialClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("SerialClient prm:" + s);

//  String commPort = "/dev/ttyUSB0"; // "COM1";
		String commPort = "/dev/tty.usbserial"; // Mac
		if (args.length > 0) {
			commPort = args[0];
		}

		nmeaClient = new SerialClient();

		Runtime.getRuntime().addShutdownHook(new Thread("SerialClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new SerialReader("SerialReader", nmeaClient.getListeners(), commPort, 4800));
		nmeaClient.startWorking();
	}
}
