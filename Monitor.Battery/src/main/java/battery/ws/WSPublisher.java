package battery.ws;

import adc.ADCObserver;
import adc.sample.BatteryMonitor;

import java.nio.channels.NotYetConnectedException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class WSPublisher {

	private static boolean verbose = false;
	private static long betweenLoops = 1_000L;

	private WebSocketClient webSocketClient = null;

	private BatteryMonitor batteryMonitor = null;
	private final static NumberFormat VOLT_FMT = new DecimalFormat("##0.00");

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
		keepGoing = false;
		webSocketClient.close();
	}

	public void consumer(BatteryMonitor.ADCData adcData) {
		this.voltage = adcData.getVoltage();
		// We could publish from here. That would generate some traffic...
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

	private void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// TODO Implement this method
					System.out.println("Connected");
				}

				@Override
				public void onMessage(String string) {
					// TODO Implement this method
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					// TODO Implement this method
				}

				@Override
				public void onError(Exception exception) {
					// TODO Implement this method
				}
			};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public WSPublisher() {
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);
		webSocketClient.connect();
	}

	private void start() {
		this.keepGoing = true;
		while (keepGoing()) {
			try {
				float voltage = getVoltage();
				publish(voltage);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
			try {
				Thread.sleep(betweenLoops);
			} catch (InterruptedException ie) {
			}
		}
	}

	private void publish(float voltage) {
		try {
			webSocketClient.send(VOLT_FMT.format(voltage));
			if (verbose) {
				System.out.println(String.format("Message sent:%f, %s", voltage, VOLT_FMT.format(voltage)));
			}
		} catch (/* NotYetConnected */ Exception e) {
			System.err.println("Ooops:" + e.toString());
//			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		System.out.println("Battery Monitoring!");
		verbose = "true".equals(System.getProperty("verbose", "false"));

		final WSPublisher publisher = new WSPublisher();
		Thread batteryThread = new Thread(() -> {
			try {
				if (verbose) {
					System.out.println("Creating BatteryMonitor...");
				}
				BatteryMonitor batteryMonitor = new BatteryMonitor(ADCObserver.MCP3008_input_channels.CH0.ch(), publisher::consumer);
				publisher.setBatteryMonitor(batteryMonitor);
				if (verbose) {
					System.out.println("Creating BatteryMonitor: done");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}, "Battery Thread");
		batteryThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			publisher.stop();
			System.out.println("Bye...");
		}, "Shutdown Hook"));
		publisher.start();
	}
}
