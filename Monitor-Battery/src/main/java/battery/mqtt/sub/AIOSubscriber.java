package battery.mqtt.sub;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class AIOSubscriber {

	private String userName = "";
	private String key = "";

	public static final String BROKER_URL = "tcp://io.adafruit.com:1883";
	public static final String BATTERY_TOPIC = "/feeds/battery-pi"; // Concat with userName in front before using.

	private MqttClient client;

	public AIOSubscriber() {

		this.userName = System.getProperty("aio.user.name", "").trim();
		this.key = System.getProperty("aio.key", "").trim();

		if (this.userName.trim().isEmpty() || this.key.trim().isEmpty()) {
			throw new RuntimeException("Require both username and key as System variables (-Daio.user.name and -Daio.key)");
		}

		String clientId = this.userName + "-sub";
		try {
			client = new MqttClient(BROKER_URL, clientId);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void start() {
		try {
			client.setCallback(new BatterySubscribeCallback());
			MqttConnectOptions options = new MqttConnectOptions();
//		options.setCleanSession(false);
			options.setUserName(this.userName);
			options.setPassword(this.key.toCharArray());
			client.connect(options);
			//Subscribe to all subtopics of home
			final String topic = this.userName + BATTERY_TOPIC;
			client.subscribe(topic);
			System.out.println("Subscriber is now listening to " + topic);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String... args) {
		final AIOSubscriber subscriber = new AIOSubscriber();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (subscriber.client != null && subscriber.client.isConnected()) {
				try {
					subscriber.client.disconnect();
					System.out.println("\nClient disconnected.");
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Bye.");
		}, "Shutdown Hook"));

		subscriber.start();
	}
}
