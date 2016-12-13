package nmea.providers.client.mux;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.providers.client.DataFileClient;
import nmea.providers.client.SerialClient;
import nmea.providers.client.TCPClient;
import nmea.providers.reader.DataFileReader;
import nmea.providers.reader.SerialReader;
import nmea.providers.reader.TCPReader;

/**
 * Just a sample, for validation of the concept.
 * Use {@link GenericNMEAMultiplexer} instead (for production).
 */
public class NMEAMultiplexer implements Multiplexer {
	@Override
	public synchronized void onData(String mess) {
		System.out.println(">> From MUX:" + mess);
	}

	private NMEAClient tcpClient;
	private NMEAClient fileClient;
	private NMEAClient serialClient;

	private static String tcpServerName = "192.168.1.1";
	private static int tcpPort = 7001;
	private static String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
	// like "/dev/tty.usbserial"on Mac, "COMx" on Windows, "/dev/ttyUSB0" on Linux
	private static String serialPort = "/dev/tty.usbserial";
	private static int serialBaudRate = 4800;

	public NMEAMultiplexer() {
		tcpClient = new TCPClient(this);
		fileClient = new DataFileClient(this);
		serialClient = new SerialClient(this);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down multiplexer nicely.");
				tcpClient.stopDataRead();
				fileClient.stopDataRead();
				serialClient.stopDataRead();
			}
		});
//	tcpClient.setEOS("\n");
		tcpClient.initClient();
		tcpClient.setReader(new TCPReader(tcpClient.getListeners(), tcpServerName, tcpPort));

//	fileClient.setEOS("\n");
		fileClient.initClient();
		fileClient.setReader(new DataFileReader(fileClient.getListeners(), dataFile));

//	serialClient.setEOS("\n");
		serialClient.initClient();
		serialClient.setReader(new SerialReader(serialClient.getListeners(), serialPort, serialBaudRate));

		tcpClient.startWorking();
		fileClient.startWorking();
		serialClient.startWorking();
	}

	public static void main(String... args) {
		new NMEAMultiplexer();
	}
}
