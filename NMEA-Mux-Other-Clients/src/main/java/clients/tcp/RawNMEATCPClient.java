package clients.tcp;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

/**
 * JUST a simple TCP Client, expecting to receive NMEA Data.
 * Requires an NMEA-multiplexer to be running on TCP:7001 (See system variables verbose, tcp.host and tcp.port)
 */
public class RawNMEATCPClient {

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");

	public static void main(String... args) {

		System.out.println("Hit [Ctrl + C] to stop.");
		try {
			final NMEATCPClient tcpClient = new NMEATCPClient();
			// tcpClient.setConsumer(System.out::println); // Raw output
			Consumer<String> nmeaConsumer = nmea -> System.out.printf("Received: [%s]\n", nmea);
			tcpClient.setConsumer(nmeaConsumer);

			try {
				tcpClient.startConnection(
						System.getProperty("tcp.host", "localhost"),
						Integer.parseInt(System.getProperty("tcp.port", String.valueOf(7001)))
				);
			} catch (Exception ex) {
				// Ooch!
				ex.printStackTrace();
				System.exit(1);
			}
			System.out.println(new Date() + ": New " + tcpClient.getClass().getName() + " created.");

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (tcpClient != null) {
					System.out.printf("\n\t>> (Ctrl-C intercepted) Stop reading requested, at %s\n.", SDF.format(new Date()));
					try {
						tcpClient.stopConnection();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				System.out.println("Now exiting hook.");
				System.out.println("Bye (from the hook)!");
//				System.exit(0);
			}, "Hook"));

			try {
				// Initiate. Request data
				tcpClient.read();
			} catch (Exception ex) {
				System.err.println("TCP Reader:" + ex.getMessage());
				ex.printStackTrace();
			}
			System.out.println("Done with the main");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Bye (main).");
		// System.exit(0);
	}
}
