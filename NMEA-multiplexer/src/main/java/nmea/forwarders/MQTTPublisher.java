package nmea.forwarders;

import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * <b>For dynamic loading</b> (non standard, as an example)
 * --------------------------
 * Requires a file like mqtt.properties to provide the broker url.
 */
public class MQTTPublisher implements Forwarder {
	private MqttClient mqttClient = null;
	private String brokerURL;

	private Properties props;

	private final static NumberFormat TEMP_FMT = new DecimalFormat("##0.00");

	// Topic list. To be extended as needed.
	public static final String TOPIC_AIR_TEMPERATURE = "nmea/airTemperature";
	public static final String TOPIC_WATER_TEMPERATURE = "nmea/waterTemperature";
	public static final String TOPIC_POSITION = "nmea/position";

	public MQTTPublisher() throws Exception {
		super();
	}

	/*
	 * brokerURL like tcp://hostname:port
	 */
	private void initMqtt() throws Exception {
		if (props == null) {
			throw new RuntimeException("Need props!");
		}
		String brokerURL = props.getProperty("broker.url");
		if (brokerURL == null) {
			throw new RuntimeException("No broker.url found in the props...");
		}
		this.brokerURL = brokerURL;

		try {

			mqttClient = new MqttClient(this.brokerURL, "nmea-pub");

		} catch (MqttException e) {
			throw e;
		}

		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setWill(mqttClient.getTopic("nmea/LWT"), "I'm gone.".getBytes(), 0, false); // LWT: Last Will and Testament
			mqttClient.connect(options);
		} catch (Exception e) {
			throw e;
		}
	}

	public String getBrokerURL() {
		return this.brokerURL;
	}

	@Override
	public void write(byte[] message) {

		if (mqttClient == null) {
			try {
				initMqtt();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				String sentenceId = StringParsers.getSentenceID(mess);
				switch (sentenceId) {
					case "MTA":
						double airTemp = StringParsers.parseMTA(mess);
						publishAirTemperature(airTemp);
						break;
					case "MTW":
						double waterTemp = StringParsers.parseMTW(mess);
						publishWaterTemperature(waterTemp);
						break;
					case "RMC":
						RMC rmc = StringParsers.parseRMC(mess);
						publishPosition(rmc.getGp());
						break;
					default: // TODO: etc...
						break;
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void publishAirTemperature(double temp) throws MqttException {
		final MqttTopic temperatureTopic = mqttClient.getTopic(TOPIC_AIR_TEMPERATURE);
		final String temperature = String.format("%s\272C", TEMP_FMT.format(temp));
		temperatureTopic.publish(new MqttMessage(temperature.getBytes()));
		System.out.println("Published data. Topic: " + temperatureTopic.getName() + "  Message: " + temperature);
	}

	private void publishWaterTemperature(double temp) throws MqttException {
		final MqttTopic temperatureTopic = mqttClient.getTopic(TOPIC_WATER_TEMPERATURE);
		final String temperature = String.format("%s\272C", TEMP_FMT.format(temp));
		temperatureTopic.publish(new MqttMessage(temperature.getBytes()));
		System.out.println("Published data. Topic: " + temperatureTopic.getName() + "  Message: " + temperature);
	}

	private void publishPosition(GeoPos pos) throws MqttException {
		final MqttTopic temperatureTopic = mqttClient.getTopic(TOPIC_POSITION);
		final String temperature = String.format("%s", pos.toString());
		temperatureTopic.publish(new MqttMessage(temperature.getBytes()));
		System.out.println("Published data. Topic: " + temperatureTopic.getName() + "  Message: " + temperature);
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.mqttClient.disconnect();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class MQTTBean {
		private String cls;
		private String brokerURL;
		private String type = "mqtt";

		public MQTTBean(MQTTPublisher instance) {
			cls = instance.getClass().getName();
			brokerURL = instance.brokerURL;
		}

		public String getBrokerURL() {
			return brokerURL;
		}
	}

	@Override
	public Object getBean() {
		return new MQTTBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
