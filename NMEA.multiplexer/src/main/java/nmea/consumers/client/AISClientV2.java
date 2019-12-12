package nmea.consumers.client;

import nmea.ais.AISParser;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.TCPReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Not a Consumer, just a sample.
 * Any regular one is AIS-aware (Serial, File, TCP, etc).
 *
 * See System variable -Dno.ais
 */
public class AISClientV2 {

	private static TCPClient nmeaClient = null;
	/*
	 * Set proxy at runtime if needed -Dhttp.proxyHost, -Dhttp.proxyPort
	 */
	public static void main(String... args) {
		try {
			Map<Integer, AISParser.AISRecord> map = new HashMap<>();

//			String aisUrl = "http://ais.exploratorium.edu:80";

			System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
			for (String s : args)
				System.out.println("CustomTCPClient prm:" + s);

			String serverName = "ais.exploratorium.edu";
			int serverPort = 80;

			nmeaClient = new TCPClient() {
				@Override
				public void dataDetectedEvent(NMEAEvent e) {
					String aisMessage = e.getContent();
					System.out.println(">> " + aisMessage);
					if (!aisMessage.startsWith("#")) {
						try {
							AISParser.AISRecord rec = AISParser.parseAIS(aisMessage);
							if (rec != null) {
								map.put(rec.getMMSI(), rec);
								System.out.println(String.format("(%d boat%s in sight): %s", map.size(), map.size() > 1 ? "s" : "", rec.toString()));
							}
						} catch (Exception ex) {
							System.err.println(ex.toString());
						}
					}
				}
			};

			Runtime.getRuntime().addShutdownHook(new Thread("TCPClient shutdown hook") {
				public void run() {
					System.out.println("Shutting down nicely.");
					nmeaClient.stopDataRead();
				}
			});
			nmeaClient.initClient();
			nmeaClient.setReader(new TCPReader("TCPReader", nmeaClient.getListeners(), serverName, serverPort));
			nmeaClient.startWorking();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
