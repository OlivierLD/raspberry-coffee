package battery.mqtt.pub;

import adc.sample.BatteryMonitor;
import analogdigitalconverter.mcp.MCPReader;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Uses paho MQTT client to connect on Adafruit-IO
 * Paho doc at https://www.eclipse.org/paho/files/javadoc/index.html?org/eclipse/paho/client/mqttv3/
 */
public class AIOPublisher {

	private String userName = "";
	private String key = "";

	private static boolean verbose = false;
	private static long betweenLoops = 1_000L;

	public static final String BROKER_URL = "tcp://io.adafruit.com:1883";
	public static final String BATTERY_TOPIC = "/feeds/battery-pi"; // Concat with userName in front before using.

	private MqttClient client;

	private BatteryMonitor batteryMonitor = null;
	private final static NumberFormat VOLT_FMT = new DecimalFormat("#00.00");

	public BatteryMonitor getBatteryMonitor() {
		return batteryMonitor;
	}

	public void setBatteryMonitor(BatteryMonitor batteryMonitor) {
		this.batteryMonitor = batteryMonitor;
	}

	private float voltage = 0f;
	private boolean keepGoing = true;

	private boolean keepGoing() {
		return this.keepGoing;
	}
	private void stop() {
		this.keepGoing = false;
	}

	public void consumer(BatteryMonitor.ADCData adcData) {
		this.voltage = adcData.getVoltage();
		if (verbose) {
			System.out.println(
							String.format("From ADC Observer: volume %d, value %d, voltage %f",
											adcData.getVolume(),
											adcData.getNewValue(),
											adcData.getVoltage()));
		}
	}

	public float getVoltage() {
		return this.voltage;
	}

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
		this.keepGoing = true;
		try {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(this.userName);
			options.setPassword(this.key.toCharArray());

			client.connect(options);

			while (keepGoing()) {
				if (client.isConnected()) {
					try {
						float voltage = getVoltage();
						publish(voltage);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.out.println("... Not connected.");
				}
				try {
					Thread.sleep(betweenLoops);
				} catch (InterruptedException ie) {
				}
			}
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publish(float voltage) throws MqttException {
		final MqttTopic onOffTopic = client.getTopic(this.userName + BATTERY_TOPIC);
		final String val = String.valueOf(voltage);
		onOffTopic.publish(new MqttMessage(val.getBytes()));
		System.out.println("Published data. Topic: " + onOffTopic.getName() + "  Message: " + val);
	}

	public static void main(String... args) {

		verbose = "true".equals(System.getProperty("verbose", "false"));

		final AIOPublisher publisher = new AIOPublisher();
		Thread batteryThread = new Thread(() -> {
			try {
				if (verbose) {
					System.out.println("Creating BatteryMonitor...");
				}
				BatteryMonitor batteryMonitor = new BatteryMonitor(MCPReader.MCP3008InputChannels.CH0.ch(), publisher::consumer);
				publisher.setBatteryMonitor(batteryMonitor);
				if (verbose) {
					System.out.println("Creating BatteryMonitor: done");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		batteryThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			publisher.stop();
			try { // Wait for the loop in start() to stop.
				Thread.sleep(Math.round(betweenLoops * 1.25));
			} catch (InterruptedException ex) {
			}
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
