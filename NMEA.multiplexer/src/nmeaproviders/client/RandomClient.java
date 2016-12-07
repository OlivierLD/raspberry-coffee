package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.RandomReader;

/**
 * Read a file containing logged data
 */
public class RandomClient extends NMEAClient {
	public RandomClient() {
		super(null, null, null);
		this.verbose = "true".equals(System.getProperty("rnd.data.verbose", "false"));
	}

	public RandomClient(Multiplexer mux) {
		super(mux);
		this.verbose = "true".equals(System.getProperty("rnd.data.verbose", "false"));
	}

	public RandomClient(String s, String[] sa) {
		super(s, sa, null);
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

	private static class RandomBean implements ClientBean {
		String cls;
		String type = "rnd";

		public RandomBean(RandomClient instance) {
			cls = instance.getClass().getName();
		}

		@Override
		public String getType() {
			return this.type;
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


	@Override
	public boolean isVerbose() {
		return this.verbose;
	}

	@Override
	public void setVerbose(boolean b) {
		this.verbose = b;
	}
}