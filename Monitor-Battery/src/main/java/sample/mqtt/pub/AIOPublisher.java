package sample.mqtt.pub;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static utils.StaticUtil.userInput;

/**
 * Use paho MQTT client to connect on Adafruit-IO
 * Paho doc at https://www.eclipse.org/paho/files/javadoc/index.html?org/eclipse/paho/client/mqttv3/
 */
public class AIOPublisher {

	private String userName = "";
	private String key = "";

	public static final String BROKER_URL = "tcp://io.adafruit.com:1883";
	public static final String ON_OFF_TOPIC = "/feeds/onoff"; // Concat with userName in front before using.

	private MqttClient client;

	public AIOPublisher() {

		this.userName = System.getProperty("aio.user.name", "").trim();
		this.key = System.getProperty("aio.key", "").trim();

		if (this.userName.trim().isEmpty() || this.key.trim().isEmpty()) {
			throw new RuntimeException("Require both username and key as System variables (-Daio.user.name and -Daio.key)");
		}

		String clientId = userName + "-pub";
		try {
			client = new MqttClient(BROKER_URL, clientId);
			System.out.println(String.format("Connected to %s as %s.", BROKER_URL, clientId));
		} catch (MqttException e) {
			System.err.println("Constructor:");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void start() {

		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(this.userName);
			options.setPassword(this.key.toCharArray());

			client.connect(options);

			System.out.println("Hit return to toggle the switch, Q to exit.");
			boolean go = true;
			while (go) {
				String str = userInput("Hit [Return] ");
				if ("Q".equalsIgnoreCase(str)) {
					go = false;
					if (this.client != null && this.client.isConnected()) {
						try {
							this.client.disconnect();
							System.out.println("\nClient disconnected.");
						} catch (MqttException e) {
							e.printStackTrace();
						}
					}
					System.out.println("Bye.");

				} else {
					if (client.isConnected()) {
						publish();
					} else {
						System.out.println("... Not connected.");
					}
				}
			}
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean onOff = true;

	private void publish() throws MqttException {
		final MqttTopic onOffTopic = client.getTopic(this.userName + ON_OFF_TOPIC);
		final String pos = onOff ? "ON" : "OFF";
		onOffTopic.publish(new MqttMessage(pos.getBytes()));
		System.out.println("Published data. Topic: " + onOffTopic.getName() + "  Message: " + pos);
		onOff = !onOff;
	}

	public static void main(String... args) {
		final AIOPublisher publisher = new AIOPublisher();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (publisher.client != null && publisher.client.isConnected()) {
				try {
					publisher.client.disconnect();
					System.out.println("\nClient disconnected.");
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Bye...");
		}, "Shutdown Hook"));

		publisher.start();
	}
}
