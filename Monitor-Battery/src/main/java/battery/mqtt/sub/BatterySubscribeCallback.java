package battery.mqtt.sub;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BatterySubscribeCallback implements MqttCallback {

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println(">> Argh!..");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		System.out.println("Message arrived. Topic: " + topic + " Message: " + message.toString());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println(">> Delivered Ok");
	}
}