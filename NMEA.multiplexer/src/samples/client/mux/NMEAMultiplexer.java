package samples.client.mux;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import samples.client.DataFileClient;
import samples.client.TCPClient;
import samples.reader.FileReader;
import samples.reader.TCPReader;

public class NMEAMultiplexer implements Multiplexer
{
	@Override
	public synchronized void onData(String mess) {
		System.out.println(">> From MUX:" + mess);
	}

	private NMEAClient tcpClient;
	private NMEAClient fileClient;

	private static String serverName = "192.168.1.1";
	private static int tcpPort = 7001;
	private static String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";

	public NMEAMultiplexer()
	{
		tcpClient = new TCPClient(null, null);
		tcpClient.setMultiplexer(this);

		fileClient = new DataFileClient(null, null);
		fileClient.setMultiplexer(this);

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				System.out.println ("Shutting down nicely.");
				tcpClient.stopDataRead();
				fileClient.stopDataRead();
			}
		});
		tcpClient.setEOS("\n");
		tcpClient.initClient();
		tcpClient.setReader(new TCPReader(tcpClient.getListeners(), serverName, tcpPort));

		fileClient.setEOS("\n");
		fileClient.initClient();
		fileClient.setReader(new FileReader(fileClient.getListeners(), dataFile));

		tcpClient.startWorking();
		fileClient.startWorking();
	}

	public static void main(String... args) {
    new NMEAMultiplexer();
	}
}
