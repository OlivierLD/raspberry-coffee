package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.TCPReader;

/**
 * Read NMEA Data from a TCP server
 */
public class TCPClient extends NMEAClient {
	public TCPClient() {
		super();
		this.verbose = "true".equals(System.getProperty("tcp.data.verbose", "false"));
	}

	public TCPClient(Multiplexer mux) {
		super(mux);
		this.verbose = "true".equals(System.getProperty("tcp.data.verbose", "false"));
	}

	public TCPClient(String s, String[] sa) {
		super(s, sa);
		this.verbose = "true".equals(System.getProperty("tcp.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose)
			System.out.println("Received from TCP :" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static TCPClient nmeaClient = null;

	public static class TCPBean implements ClientBean {
		String cls;
		String type = "tcp";
		int port;
		String hostname;

		public TCPBean(TCPClient instance) {
			cls = instance.getClass().getName();
			port = ((TCPReader) instance.getReader()).getPort();
			hostname = ((TCPReader) instance.getReader()).getHostname();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public int getPort() {
			return port;
		}

		public String getHostname() {
			return this.hostname;
		}
	}

	@Override
	public Object getBean() {
		return new TCPBean(this);
	}

	public static void main(String[] args) {
		System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("CustomTCPClient prm:" + s);

		String serverName = "192.168.1.1";

		nmeaClient = new TCPClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new TCPReader(nmeaClient.getListeners(), serverName, 7001));
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