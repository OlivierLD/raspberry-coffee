package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import i2c.sensor.listener.SensorLSM303Context;
import java.io.IOException;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

/**
 * Reads the LSM303 sensor, and feeds a WebSocket server with heading, pitch and roll.
 * A WegGL UI listens to the WS event, and displays a cube rotated accordingly.
 * <br>
 * The Web UI is in the <code>node</code> directory, named <code>heel.pitch.html</code>.
 * <br>
 * Start it from the script named <code>heelpitch</code>.
 */
public class LSM303HeelPitchWebGL {

	private static boolean verbose =  false;
	private static WebSocketClient webSocketClient = null;

	private static void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// TODO Implement this method when needed
					System.out.println("Connected");
				}

				@Override
				public void onMessage(String string) {
					// TODO Implement this method when needed
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					// TODO Implement this method when needed
				}

				@Override
				public void onError(Exception exception) {
					// TODO Implement this method when needed
				}
			};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException, IOException {

		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);
		webSocketClient.connect();

		verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
		System.out.println("Verbose: " + verbose);
		LSM303 sensor = new LSM303(); // TODO See with LSM303.EnabledFeature.MAGNETOMETER);
		LSM303Listener dataListener = new LSM303Listener() {
			@Override
			public void dataDetected(double accX, double accY, double accZ, double magX, double magY, double magZ, double heading, double pitch, double roll) {
				try {
					JSONObject json = new JSONObject();
					json.put("heading", heading);
					json.put("pitch", pitch);
					json.put("roll", roll);
					webSocketClient.send(json.toString());
					if (verbose) {
						System.out.println(String.format("Pitch:%f, Roll:%f", pitch, roll));
					}
				} catch (/* NotYetConnected */ Exception e) {
					System.err.println("Ooops:" + e.toString());
//			  e.printStackTrace();
				}
			}
		};

		SensorLSM303Context.getInstance().addReaderListener(dataListener);
		sensor.setDataListener(dataListener);
		sensor.setWait(250L);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nBye.");
			webSocketClient.close();
			synchronized (sensor) {
				sensor.setKeepReading(false);
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ie) {
					System.err.println(ie.getMessage());
				}
			}
		}, "Shutdown Hook"));
		System.out.println("Starting listening...");
		sensor.startReading();
	}
}
