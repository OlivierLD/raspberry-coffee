package nmea.forwarders;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * For dynamic loading
 */
public class MQTTPublisher implements Forwarder {
	private MqttClient mqttClient = null;
	private boolean isConnected = false;
	private String brokerURL;

	private Properties props;

	private final static NumberFormat TEMP_FMT = new DecimalFormat("##0.00");
	public static final String TOPIC_TEMPERATURE = "nmea/temperature";


	public MQTTPublisher() throws Exception {
	}

	/*
	 * brokerURL like tcp://hostname:port
	 */
	private void init() throws Exception {
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
//		options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false); // LWT: Last Will and Testament
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
				init();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			String mess = new String(message);
			if (!mess.isEmpty() && isConnected) {
				if (true) {
					publishTemperature(12.34F);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void publishTemperature(float temp) throws MqttException {
		final MqttTopic temperatureTopic = mqttClient.getTopic(TOPIC_TEMPERATURE);
		final String temperature = String.format("%s°C", TEMP_FMT.format(temp));
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
