package mqtt.sub;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import relay.RelayManager;

public class OnOffSubscribeCallback implements MqttCallback {


	private RelayManager relayManager;
	public OnOffSubscribeCallback(RelayManager relayManager) {
		this.relayManager = relayManager;
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println(">> Argh!..");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		System.out.println("Message arrived. Topic: " + topic + " Message: " + message.toString());
		this.relayManager.set(1, "ON".equals(message.toString()) ? "on" : "off");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println(">> Ok");
	}
}
