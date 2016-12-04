package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.SerialReader;

/**
 * Read NMEA Data from a Serial port
 */
public class SerialClient extends NMEAClient {
	public SerialClient() {
		super();
	}

	public SerialClient(Multiplexer mux) {
		super(mux);
	}

	public SerialClient(String s, String[] sa) {
		super(s, sa);
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if ("true".equals(System.getProperty("serial.data.verbose", "false")))
			System.out.println("Received from Serial:" + e.getContent());
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static SerialClient nmeaClient = null;

	public static class SerialBean implements ClientBean {
		String cls;
		String type = "serial";
		String port;
		int br;

		public SerialBean(SerialClient instance) {
			cls = instance.getClass().getName();
			port = ((SerialReader) instance.getReader()).getPort();
			br = ((SerialReader) instance.getReader()).getBr();
		}

		@Override
		public String getType() { return this.type; }

		public String getPort() {
			return port;
		}

		public int getBr() {
			return br;
		}
	}

	@Override
	public Object getBean() {
		return new SerialBean(this);
	}

	public static void main(String[] args) {
		System.out.println("CustomSerialClient invoked with " + args.length + " Parameter(s).");
		for (String s : args)
			System.out.println("CustomSerialClient prm:" + s);

//  String commPort = "/dev/ttyUSB0"; // "COM1";
		String commPort = "/dev/tty.usbserial"; // Mac
		if (args.length > 0)
			commPort = args[0];

		nmeaClient = new SerialClient();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
//  nmeaClient.setEOS("\n"); // TASK Sure?
		nmeaClient.initClient();
		nmeaClient.setReader(new SerialReader(nmeaClient.getListeners(), commPort, 4800));
		nmeaClient.startWorking();
	}
}