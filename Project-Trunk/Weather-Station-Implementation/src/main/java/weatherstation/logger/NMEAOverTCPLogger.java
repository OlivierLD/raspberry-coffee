package weatherstation.logger;

import nmea.api.NMEAParser;
import org.json.JSONObject;
import nmea.parser.StringGenerator;
import weatherstation.logger.servers.TCPServer;

/**
 * Compatible with OpenCPN, NodeRED, NMEA.multiplexer, etc...
 *
 * Expect -Dtcp.port to override default 7001.
 * See also station.lat, station.lng
 *
 *
 *
 * Strictly speaking, not a loader, but a forwarder/broadcaster
 */

public class NMEAOverTCPLogger implements LoggerInterface {

	private int tcpPort = 7001;
	private boolean verbose = "true".equals(System.getProperty("tcp.verbose", "false"));
	private TCPServer tcpServer = null;

	private Double stationLatitude  = null;
	private Double stationLongitude = null;

	private final String DEVICE_PREFIX = "WS";

	public NMEAOverTCPLogger() {
		try {
			tcpPort = Integer.parseInt(System.getProperty("tcp.port", String.valueOf(tcpPort)));
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}

		String strLat = System.getProperty("station.lat");
		String strLng = System.getProperty("station.lng");

		if (strLat != null && strLng != null) {
			try {
				stationLatitude = Double.parseDouble(strLat);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			try {
				stationLongitude = Double.parseDouble(strLng);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		// Open TCP port here
		try {
			tcpServer = new TCPServer(this.tcpPort);
		} catch (Exception ex) {
			// Oops
			ex.printStackTrace();
		}
	}

	@Override
	public void pushMessage(JSONObject json)
			throws Exception {
		if (verbose) {
			System.out.println(">>> Pushing (NMEA-TCP) :\n" + json.toString(2));
		}
		convertAndPush(json);
	}

	/**
	 * @param json
	 */
	private void convertAndPush(JSONObject json) {

		double hum    = json.getDouble("hum");
		double volts  = json.getDouble("volts");
		double dir    = json.getDouble("dir");
		double avgdir = json.getDouble("avgdir");
		double speed  = json.getDouble("speed");
		double gust   = json.getDouble("gust");
		double rain   = json.getDouble("rain");
		double press  = json.getDouble("press");
		double temp   = json.getDouble("temp");
		double dew    = json.getDouble("dew");

		int deviceIdx = 0; // Instead of "BME280" or so...
		String nmeaXDR = StringGenerator.generateXDR(DEVICE_PREFIX,
				new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
						hum,
						String.valueOf(deviceIdx++)), // %, Humidity
				new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
						temp,
						String.valueOf(deviceIdx++)), // Celcius, Temperature
				new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
						temp,
						String.valueOf(deviceIdx++)), // mm/h, Rain
				new StringGenerator.XDRElement(StringGenerator.XDRTypes.GENERIC, // In lack of a better category...
						rain,
						String.valueOf(deviceIdx++))); // Pascal, pressure
		nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;

		if (verbose) {
			System.out.println(String.format(">>> Generated for TCP [%s] with rain %f ", nmeaXDR.trim(), rain));
		}

		tcpServer.write(nmeaXDR.getBytes());

		String nmeaMDA = StringGenerator.generateMDA(DEVICE_PREFIX,
				press / 100,
				temp,
				-Double.MAX_VALUE,  // Water Temp
				hum,
				-Double.MAX_VALUE,  // Abs hum
				dew,  // dew point
				avgdir,  // TWD
				-Double.MAX_VALUE,  // TWD (mag)
				speed); // TWS
		nmeaMDA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

		if (verbose) {
			System.out.println(String.format(">>> Generated for TCP [%s]", nmeaMDA.trim()));
		}

		tcpServer.write(nmeaMDA.getBytes());

		String nmeaMTA = StringGenerator.generateMTA(DEVICE_PREFIX, temp);
		nmeaMTA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

		if (verbose) {
			System.out.println(String.format(">>> Generated for TCP [%s]", nmeaMTA.trim()));
		}

		tcpServer.write(nmeaMTA.getBytes());

		String nmeaMMB = StringGenerator.generateMMB(DEVICE_PREFIX, press / 100);
		nmeaMMB += NMEAParser.NMEA_SENTENCE_SEPARATOR;

		if (verbose) {
			System.out.println(String.format(">>> Generated for TCP [%s]", nmeaMMB.trim()));
		}

		tcpServer.write(nmeaMMB.getBytes());

		String nmeaMWD = StringGenerator.generateMWD(DEVICE_PREFIX, avgdir, speed, 0D);
		nmeaMWD += NMEAParser.NMEA_SENTENCE_SEPARATOR;

		if (verbose) {
			System.out.println(String.format(">>> Generated for TCP [%s]", nmeaMWD.trim()));
		}

		tcpServer.write(nmeaMWD.getBytes());

		if (stationLatitude != null && stationLongitude != null) {
			String nmeaGLL = StringGenerator.generateGLL(DEVICE_PREFIX, stationLatitude, stationLongitude, System.currentTimeMillis());
			nmeaGLL += NMEAParser.NMEA_SENTENCE_SEPARATOR;

			if (verbose) {
				System.out.println(String.format(">>> Generated for TCP [%s]", nmeaGLL.trim()));
			}

			tcpServer.write(nmeaGLL.getBytes());
		}
	}
	@Override
	public void close() {
		System.out.println("(TCP Logger) Bye!");
		if (tcpServer != null) {
			tcpServer.close();
		}
	}
}
