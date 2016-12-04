package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.HTU21DFReader;

/**
 * Read a file containing logged data
 */
public class HTU21DFClient extends NMEAClient {
	public HTU21DFClient() {
		super(null, null, null);
	}

	public HTU21DFClient(Multiplexer mux) {
		super(mux);
	}

	public HTU21DFClient(String s, String[] sa) {
		super(s, sa, null);
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if ("true".equals(System.getProperty("htu21df.data.verbose", "false")))
			System.out.println("Received from HTU21DF:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static HTU21DFClient nmeaClient = null;

	private static class HTU21DFBean {
		String cls;
		String type = "hut21df";

		public HTU21DFBean(HTU21DFClient instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new HTU21DFBean(this);
	}

	public static void main(String[] args) {
		System.out.println("HTU21DFClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("HTU21DFClient prm:" + s);

		nmeaClient = new HTU21DFClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new HTU21DFReader(nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}