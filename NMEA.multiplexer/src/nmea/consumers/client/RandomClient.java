package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.RandomReader;

/**
 * Read a file containing logged data
 */
public class RandomClient extends NMEAClient {
	public RandomClient() {
		this(null, null, null);
	}

	public RandomClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public RandomClient(String s, String[] sa) {
		this(s, sa, null);
	}

	public RandomClient(String s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("rnd.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from RND:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static RandomClient nmeaClient = null;

	public static class RandomBean implements ClientBean {
		private String cls;
		private String type = "rnd";
		private boolean verbose;

		public RandomBean(RandomClient instance) {
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
		return new RandomBean(this);
	}

	public static void main(String[] args) {
		System.out.println("RandomClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("RandomClient prm:" + s);

		nmeaClient = new RandomClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new RandomReader(nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}