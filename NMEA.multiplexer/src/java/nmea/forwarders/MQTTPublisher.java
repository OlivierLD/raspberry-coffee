package nmea.forwarders;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

public class MQTTPublisher implements Forwarder {
	private MqttClient mqttClient = null;
	private boolean isConnected = false;
	private String brokerURL;

	private final static NumberFormat TEMP_FMT = new DecimalFormat("##0.00");
	public static final String TOPIC_TEMPERATURE = "nmea/temperature";


	/**
	 * @param brokerURL like tcp://hostname:port
	 * @throws Exception
	 */
	public MQTTPublisher(String brokerURL) throws Exception {
		this.brokerURL = brokerURL;
		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
//		options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false); // LWT: Last Will and Testament
			mqttClient.connect(options);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getBrokerURL() {
		return this.brokerURL;
	}

	@Override
	public void write(byte[] message) {
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
	}
}
