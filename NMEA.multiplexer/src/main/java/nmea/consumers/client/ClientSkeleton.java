package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.ReaderSkeleton;

/**
 * Skeleton for your own dynamically loaded Consumer (aka Channel)
 */
public class ClientSkeleton extends NMEAClient {
	public ClientSkeleton() {
		this(null, null, null);
	}

	public ClientSkeleton(Multiplexer mux) {
		this(null, null, mux);
	}

	public ClientSkeleton(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public ClientSkeleton(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("skeleton.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from Skeleton:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static ClientSkeleton nmeaClient = null;

	public static class SkeletonBean implements ClientBean {
		private String cls;
		private String type = "skeleton";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;

		public SkeletonBean(ClientSkeleton instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
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
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
	}

	@Override
	public Object getBean() {
		return new SkeletonBean(this);
	}

	public static void main(String... args) {
		System.out.println("ClientSkeleton invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("ClientSkeleton prm:" + s);

		nmeaClient = new ClientSkeleton();

		Runtime.getRuntime().addShutdownHook(new Thread("ClientSkeleton shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new ReaderSkeleton(nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
