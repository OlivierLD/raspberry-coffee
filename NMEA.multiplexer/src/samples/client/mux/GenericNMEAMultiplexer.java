package samples.client.mux;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import samples.client.DataFileClient;
import samples.client.SerialClient;
import samples.client.TCPClient;
import samples.reader.FileReader;
import samples.reader.SerialReader;
import samples.reader.TCPReader;
import servers.Forwarder;
import servers.TCPWriter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GenericNMEAMultiplexer implements Multiplexer
{
	private List<NMEAClient> nmeaDataProviders  = new ArrayList<>();
	private List<Forwarder>  nmeaDataForwarders = new ArrayList<>();

	@Override
	public synchronized void onData(String mess) {
		System.out.println(">> From MUX: " + mess);
		nmeaDataForwarders.stream()
						.forEach(fwd -> fwd.write(mess.getBytes()));
	}

	private final static NumberFormat MUX_IDX_FMT = new DecimalFormat("00");

	public GenericNMEAMultiplexer(Properties muxProps)
	{
		int muxIdx = 1;
		boolean thereIsMore = true;
		while (thereIsMore)
		{
			String typeProp = String.format("mux.%s.type", MUX_IDX_FMT.format(muxIdx));
			String type = muxProps.getProperty(typeProp);
			if (type == null)
			{
				thereIsMore = false;
			}
			else
			{
				switch (type)
				{
					case "serial":
						String serialPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
						String br         = muxProps.getProperty(String.format("mux.%s.baudrate", MUX_IDX_FMT.format(muxIdx)));
						NMEAClient serialClient = new SerialClient(this);
						serialClient.setEOS("\n");
						serialClient.initClient();
						serialClient.setReader(new SerialReader(serialClient.getListeners(), serialPort, Integer.parseInt(br)));
						nmeaDataProviders.add(serialClient);
						break;
					case "tcp":
						String tcpPort   = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
						String tcpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
						NMEAClient tcpClient = new TCPClient(this);
						tcpClient.setEOS("\n");
						tcpClient.initClient();
						tcpClient.setReader(new TCPReader(tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort)));
						nmeaDataProviders.add(tcpClient);
						break;
					case "file":
						String filename = muxProps.getProperty(String.format("mux.%s.filename", MUX_IDX_FMT.format(muxIdx)));
						NMEAClient fileClient = new DataFileClient(this);
						fileClient.setEOS("\n");
						fileClient.initClient();
						fileClient.setReader(new FileReader(fileClient.getListeners(), filename));
						nmeaDataProviders.add(fileClient);
						break;
					default:
						System.out.println("??? " + type);
						break;
				}
			}
			muxIdx++;
		}
		thereIsMore = true;
		int fwdIdx = 1;
		while (thereIsMore) {
			String typeProp = String.format("forward.%s.type", MUX_IDX_FMT.format(fwdIdx));
			String type = muxProps.getProperty(typeProp);
			if (type == null) {
				thereIsMore = false;
			} else {
				switch (type) {
					case "tcp":
						String tcpPort   = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder tcpForwarder = new TCPWriter(Integer.parseInt(tcpPort));
							nmeaDataForwarders.add(tcpForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					default:
						System.out.println("Not supported yet");
						break;
				}
			}
			fwdIdx++;
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				System.out.println ("Shutting down multiplexer nicely.");
				nmeaDataProviders.stream()
								.forEach(client -> client.stopDataRead());
				nmeaDataForwarders.stream()
								.forEach(fwd -> fwd.close());
				}
			});

		nmeaDataProviders.stream()
						.forEach(client -> client.startWorking());
	}

	public static void main(String... args) {
		Properties definitions = new Properties();
		File propFile = new File("nmea.mux.properties");
		if (!propFile.exists())
		{
			throw new RuntimeException("File nmea.mux.properties not found");
		}
		else
		{
			try
			{
				definitions.load(new java.io.FileReader(propFile));
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

    new GenericNMEAMultiplexer(definitions);
	}
}
