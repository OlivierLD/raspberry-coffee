package sample.mqtt.pub;

import sample.mqtt.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * Paho doc at https://www.eclipse.org/paho/files/javadoc/index.html?org/eclipse/paho/client/mqttv3/
 */
public class Publisher {

	public static final String BROKER_URL = "tcp://192.168.1.136:1883"; // tcp://broker.mqttdashboard.com:1883";

	public static final String BRIGHTNESS_TOPIC = "home/brightness";
	public static final String TEMPERATURE_TOPIC = "home/temperature";

	private MqttClient client;


	public Publisher() {

		//We have to generate a unique Client id.
		String clientId = Utils.getMacAddress() + "-pub";


		try {

			client = new MqttClient(BROKER_URL, clientId);

		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void start() {

		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setWill(client.getTopic("home/LWT"), "I'm gone :(".getBytes(), 0, false); // LWT: Last Will and Testament

			client.connect(options);

			//Publish data forever
			while (true) {

				publishBrightness();

				Thread.sleep(500);

				publishTemperature();

				Thread.sleep(500);
			}
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void publishTemperature() throws MqttException {
		final MqttTopic temperatureTopic = client.getTopic(TEMPERATURE_TOPIC);

		final int temperatureNumber = Utils.createRandomNumberBetween(20, 30);
		final String temperature = temperatureNumber + "°C";

		temperatureTopic.publish(new MqttMessage(temperature.getBytes()));

		System.out.println("Published data. Topic: " + temperatureTopic.getName() + "  Message: " + temperature);
	}

	private void publishBrightness() throws MqttException {
		final MqttTopic brightnessTopic = client.getTopic(BRIGHTNESS_TOPIC);

		final int brightnessNumber = Utils.createRandomNumberBetween(0, 100);
		final String brigthness = brightnessNumber + "%";

		brightnessTopic.publish(new MqttMessage(brigthness.getBytes()));

		System.out.println("Published data. Topic: " + brightnessTopic.getName() + "   Message: " + brigthness);
	}

	public static void main(String... args) {
		final Publisher publisher = new Publisher();
		publisher.start();
	}
}
