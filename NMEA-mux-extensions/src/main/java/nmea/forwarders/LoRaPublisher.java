package nmea.forwarders;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import arduino.LoRaMessageManager;
import gnu.io.NoSuchPortException;
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

	private String portName;
	private int baudRate;
	private boolean verbose = false;
	private List<String> sentenceList = null;

	private boolean openTheSerialPort = true;

	private boolean available = false; // Released when ready, or Tx ACK'd

	private boolean isChannelAvailable() {
		return this.available;
	}

	private ArduinoLoRaClient bridge = null;

	/*
	 * @throws Exception
	 */
	public LoRaPublisher() throws Exception {
	}

	private void onDataFromArduino(String str) {
		// Callback from Arduino
		System.out.println(String.format("Received [%s]", str.trim()));

		if (str.startsWith(LoRaMessageManager.Messages.LORA_0008.id()) ||
				str.startsWith(LoRaMessageManager.Messages.LORA_0006.id())) { // ACK or Ready
			this.available = true;
		}
		// Manage potential errors.
		try {
			LoRaMessageManager.throwIfError(str);
		} catch (Exception ex) {
			ex.printStackTrace();
			this.available = true; // Release
		}
	}

	private ArduinoLoRaClient initLora() throws Exception {
		return new ArduinoLoRaClient(this.portName, this.baudRate, this::onDataFromArduino);
	}

	@Override
	public void write(byte[] message) {
		if (bridge == null && openTheSerialPort) {
			try {
				bridge = initLora();
			} catch (NoSuchPortException nspe) {
				System.out.println(String.format(" >> Serial port [%s] not available...", this.portName));
				openTheSerialPort = false; // Forget it.
				available = true; // Release
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
			String sentenceId = StringParsers.getSentenceID(str);
			if (("*".equals(sentenceList.get(0)) || sentenceList.contains(sentenceId)) && this.isChannelAvailable()) { // Filter sentences here
				// Forward to LoRa
				try {
					this.available = !openTheSerialPort || false; // Released when ACK is received
					if (bridge != null) {
						bridge.sendToLora(str); //  + "\n");
					}
					if (this.verbose) {
						System.out.println(String.format("Sending [%s] to Lora", str.trim()));
					}
				} catch (IOException e) {
					e.printStackTrace();
					this.available = true;
				}
			} else {
				if (this.verbose) {
					if (this.isChannelAvailable()) {
						System.out.println(
								String.format("Dropping %s%s",
										sentenceId,
										this.isChannelAvailable() ? "" : " (not available)"));
					}
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			if (bridge != null) {
				bridge.close();
			}
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
		String baudRateStr = props.getProperty("baudrate", "9600");
		this.verbose = "true".equals(props.getProperty("lora.verbose", "false"));
		try {
			this.baudRate = Integer.parseInt(baudRateStr);
		} catch (NumberFormatException nfe) {
			this.baudRate = 9_600;
		}
		String[] sentences = props.getProperty("nmea.filter", "RMC").split(",");
		this.sentenceList = Arrays.asList(sentences);
	}
}
