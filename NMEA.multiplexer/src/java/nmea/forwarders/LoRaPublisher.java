package nmea.forwarders;

import java.io.IOException;
import java.util.Properties;

import arduino.LoRaMessages;
import nmea.parser.StringParsers;
import arduino.ArduinoLoRaClient;

/**
 * This is a {@link Forwarder}, forward everything to a LoRa sensor.
 * <br>
 * It can be loaded dynamically. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.LoRaPublisher
 *   forward.XX.properties=loraRF95.properties

 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 */
public class LoRaPublisher implements Forwarder {
	private double previousTemp = -Double.MAX_VALUE;

	private String portName;
	private int baudRate;

	private ArduinoLoRaClient bridge = null;

	/*
	 * @throws Exception
	 */
	public LoRaPublisher() throws Exception {
	}

	private void onDataFromArduino(String str) {
		// Callback from Arduino
		System.out.println(String.format("Received [%s]", str.trim()));
		// Manage potential errors.
		try {
			LoRaMessages.throwIfError(str);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ArduinoLoRaClient init() throws Exception {
		return new ArduinoLoRaClient(this.portName, this.baudRate, this::onDataFromArduino);
	}

	@Override
	public void write(byte[] message) {
		if (bridge == null) {
			try {
				bridge = init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
			// Forward to LoRa
			try {
				bridge.sendToLora(str + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			bridge.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class LoRaBean {
		private String cls;
		private String type = "LoRa-forwarder";
		private String port;
		private int br;

		public LoRaBean(LoRaPublisher instance) {
			cls = instance.getClass().getName();
			port = instance.portName;
			br = instance.baudRate;
		}
	}

	@Override
	public Object getBean() {
		return new LoRaBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.portName = props.getProperty("serial.port", "/dev/ttyUSB0");
		String baudRateStr = props.getProperty("baud.rate", "9600");
		try {
			this.baudRate = Integer.parseInt(baudRateStr);
		} catch (NumberFormatException nfe) {
			this.baudRate = 9600;
		}
	}
}
