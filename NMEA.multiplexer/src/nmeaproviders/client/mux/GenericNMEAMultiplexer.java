package nmeaproviders.client.mux;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmeaproviders.client.DataFileClient;
import nmeaproviders.client.SerialClient;
import nmeaproviders.client.TCPClient;
import nmeaproviders.client.WebSocketClient;
import nmeaproviders.reader.FileReader;
import nmeaproviders.reader.SerialReader;
import nmeaproviders.reader.TCPReader;
import nmeaproviders.reader.WebSocketReader;
import servers.DataFileWriter;
import servers.Forwarder;
import servers.TCPWriter;
import servers.WebSocketWriter;

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
						.forEach(fwd -> {
							try {
								fwd.write(mess.getBytes());
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
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
						try {
							String serialPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
							String br         = muxProps.getProperty(String.format("mux.%s.baudrate", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient serialClient = new SerialClient(this);
//					  serialClient.setEOS("\n");
							serialClient.initClient();
							serialClient.setReader(new SerialReader(serialClient.getListeners(), serialPort, Integer.parseInt(br)));
							nmeaDataProviders.add(serialClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "tcp":
						try {
							String tcpPort   = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
							String tcpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient tcpClient = new TCPClient(this);
//					  tcpClient.setEOS("\n");
							tcpClient.initClient();
							tcpClient.setReader(new TCPReader(tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort)));
							nmeaDataProviders.add(tcpClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "file":
						try {
							String filename = muxProps.getProperty(String.format("mux.%s.filename", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient fileClient = new DataFileClient(this);
//					  fileClient.setEOS("\n");
							fileClient.initClient();
							fileClient.setReader(new FileReader(fileClient.getListeners(), filename));
							nmeaDataProviders.add(fileClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "ws":
						try {
							String wsUri = muxProps.getProperty(String.format("mux.%s.wsuri", MUX_IDX_FMT.format(muxIdx)));
							NMEAClient wsClient = new WebSocketClient(this);
//					  wsClient.setEOS("\n");
							wsClient.initClient();
							wsClient.setReader(new WebSocketReader(wsClient.getListeners(), wsUri));
							nmeaDataProviders.add(wsClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					default:
						throw new RuntimeException(String.format("mux type [%s] not supported yet.", type));
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
						String tcpPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder tcpForwarder = new TCPWriter(Integer.parseInt(tcpPort));
							nmeaDataForwarders.add(tcpForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "file":
						String fName = muxProps.getProperty(String.format("forward.%s.filename", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder fileForwarder = new DataFileWriter(fName);
							nmeaDataForwarders.add(fileForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					case "ws":
						String wsUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
						try {
							Forwarder wsForwarder = new WebSocketWriter(wsUri);
							nmeaDataForwarders.add(wsForwarder);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						break;
					default:
						throw new RuntimeException(String.format("forward type [%s] not supported yet.", type));
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
						.forEach(client -> {
							try {
								client.startWorking();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
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
