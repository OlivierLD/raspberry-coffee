package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.BME280Reader;
import nmeaproviders.reader.HTU21DFReader;

/**
 * Read a file containing logged data
 */
public class BME280Client extends NMEAClient {
	public BME280Client() {
		super(null, null, null);
		this.verbose = ("true".equals(System.getProperty("bme280.data.verbose", "false")));
	}

	public BME280Client(Multiplexer mux) {
		super(mux);
		this.verbose = ("true".equals(System.getProperty("bme280.data.verbose", "false")));
	}

	public BME280Client(String s, String[] sa) {
		super(s, sa, null);
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
		String cls;
		String type = "bme280";

		public BME280Bean(BME280Client instance) {
			cls = instance.getClass().getName();
		}

		@Override
		public String getType() {
			return this.type;
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

//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new BME280Reader(nmeaClient.getListeners()));
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