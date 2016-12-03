package nmeaproviders.client.mux;

import gnu.io.CommPortIdentifier;
import http.HTTPServer;
import http.HTTPServerInterface;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmeaproviders.client.BME280Client;
import nmeaproviders.client.DataFileClient;
import nmeaproviders.client.HTU21DFClient;
import nmeaproviders.client.RandomClient;
import nmeaproviders.client.SerialClient;
import nmeaproviders.client.TCPClient;
import nmeaproviders.client.WebSocketClient;
import nmeaproviders.reader.BME280Reader;
import nmeaproviders.reader.FileReader;
import nmeaproviders.reader.HTU21DFReader;
import nmeaproviders.reader.RandomReader;
import nmeaproviders.reader.SerialReader;
import nmeaproviders.reader.TCPReader;
import nmeaproviders.reader.WebSocketReader;
import org.json.JSONArray;
import org.json.JSONObject;
import servers.ConsoleWriter;
import servers.DataFileWriter;
import servers.Forwarder;
import servers.TCPWriter;
import servers.WebSocketWriter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GenericNMEAMultiplexer implements Multiplexer, HTTPServerInterface
{
	private HTTPServer adminServer = null;

	private List<NMEAClient> nmeaDataProviders  = new ArrayList<>();
	private List<Forwarder>  nmeaDataForwarders = new ArrayList<>();

	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200); // Default

		if (request.getVerb().equals("GET")) {
			if (request.getPath().equals("/serial-port-list")) {
				response = new HTTPServer.Response(request.getProtocol(), 200);

				List<String> portList = getSerialPortList();
				String[] portArray = portList.toArray(new String[portList.size()]);
				JSONObject json = new JSONObject();
				JSONArray list = new JSONArray(portArray);
				json.put("serial-port-list", list);

				String content = json.toString();

				Map<String, String> responseHeaders = new HashMap<>();
				responseHeaders.put("Content-Type", "application/json");
				responseHeaders.put("Content-Length", String.valueOf(content.length()));
				responseHeaders.put("Access-Control-Allow-Origin", "*");
				response.setHeaders(responseHeaders);
				response.setPayload(content.getBytes());
			}
		}

		return response;
	}

	private static List<String> getSerialPortList() {
		List<String> portList = new ArrayList<>();
		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		while (enumeration.hasMoreElements())
		{
			CommPortIdentifier cpi = (CommPortIdentifier)enumeration.nextElement();
			portList.add(cpi.getName());
		}
		return portList;
	}

	@Override
	public synchronized void onData(String mess) {
		if ("true".equals(System.getProperty("mux.data.verbose", "false"))) {
			System.out.println(">> From MUX: " + mess);
		}
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
							wsClient.initClient();
							wsClient.setReader(new WebSocketReader(wsClient.getListeners(), wsUri));
							nmeaDataProviders.add(wsClient);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case "htu21df": // Humidity & Temperature sensor
						try {
							NMEAClient htu21dfClient = new HTU21DFClient(this);
							htu21dfClient.initClient();
							htu21dfClient.setReader(new HTU21DFReader(htu21dfClient.getListeners()));
							nmeaDataProviders.add(htu21dfClient);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "rnd": // Random generator, for debugging
						try {
							NMEAClient rndClient = new RandomClient(this);
							rndClient.initClient();
							rndClient.setReader(new RandomReader(rndClient.getListeners()));
							nmeaDataProviders.add(rndClient);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "bme280": // Humidity, Temperature, Pressure
						try {
							NMEAClient bme280Client = new BME280Client(this);
							bme280Client.initClient();
							bme280Client.setReader(new BME280Reader(bme280Client.getListeners()));
							nmeaDataProviders.add(bme280Client);
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Error err) {
							err.printStackTrace();
						}
						break;
					case "bmp180": // Temperature, Pressure
					case "lsm303": // 3D magnetometer
					case "batt":   // Battery Voltage, use XDR
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
					case "console":
						try {
							Forwarder consoleForwarder = new ConsoleWriter();
							nmeaDataForwarders.add(consoleForwarder);
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
				if (adminServer != null) {
					adminServer.stopRunning();
				}
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

	public void startAdminServer(int port) {
		try {
			this.adminServer = new HTTPServer(port, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String... args) {

		String propertiesFile = System.getProperty("mux.properties", "nmea.mux.properties");

		Properties definitions = new Properties();
		File propFile = new File(propertiesFile);
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

		GenericNMEAMultiplexer mux = new GenericNMEAMultiplexer(definitions);
		mux.startAdminServer(9999);
	}

}
