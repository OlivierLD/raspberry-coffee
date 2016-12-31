package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.BME280Reader;

/**
 * Reads a BME280 sensor, and produces a valid NMEA sentence.
 */
public class BME280Client extends NMEAClient {
	public BME280Client() {
		this(null, null, null);
	}

	public BME280Client(Multiplexer mux) {
		this(null, null, mux);
	}

	public BME280Client(String s, String[] sa) {
		this(s, sa, null);
	}

	public BME280Client(String s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = ("true".equals(System.getProperty("bme280.data.verbose", "false")));
	}

		@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from BME280:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static BME280Client nmeaClient = null;

	public static class BME280Bean implements ClientBean {
		private String cls;
		private String type = "bme280";
		private boolean verbose;

		public BME280Bean(BME280Client instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
		}

		@Override
		public String getType() {
			return this.type;
		}

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}
	}

	@Override
	public Object getBean() {
		return new BME280Bean(this);
	}

	public static void main(String[] args) {
		System.out.println("BME280Client invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("BME280Client prm:" + s);

		nmeaClient = new BME280Client();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new BME280Reader(nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}