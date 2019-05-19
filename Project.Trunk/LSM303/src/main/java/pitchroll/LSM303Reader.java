package pitchroll;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.LSM303;
import java.io.IOException;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

/**
 * Feeds a WebSocket server with pitch and roll data.
 * The WebSocket server can be started using nodejs.
 * $> node server.js
 *
 * The WebSocket data can be read from an html page, and rendered as WebGL.
 * See pitchroll.html in the `node` directory.
 */
public class LSM303Reader {

	private static boolean verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
	private LSM303 lsm303;

	private static final long BETWEEN_LOOPS = 250L; // TODO: Make it an external parameter?
	private static boolean read = true;

	private WebSocketClient webSocketClient = null;

	public LSM303Reader() {
		try {
			this.lsm303 = new LSM303(); // TODO Calibration parameters!

			String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
			initWebSocketConnection(wsUri);

		} catch (I2CFactory.UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void startReader() {
		this.lsm303.startReading();
		while (read) {
			// Read data continuously, as defined by BETWEEN_LOOPS
			try {
				double pitch   = lsm303.getPitch();
				double roll    = lsm303.getRoll();
				double heading = lsm303.getHeading();
				// Feed WS
				if (verbose) {
					System.out.println(String.format("Heading: %.02f, pitch: %.02f, roll: %.02f", heading, pitch, roll));
				}
				if (webSocketClient != null) {
					JSONObject json = new JSONObject();
					json.put("pitch", pitch);
					json.put("roll",  roll);
					json.put("yaw",   heading);
					webSocketClient.send(json.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(BETWEEN_LOOPS);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println(String.format(">>> %s done reading. Bye.", this.getClass().getName()));
		try {
			this.closeReader();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			Thread.sleep(2_000L);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	public void closeReader() throws Exception {
		this.lsm303.setKeepReading(false);
		if (webSocketClient != null) {
			webSocketClient.close();
		}
	}

	private void initWebSocketConnection(String serverURI) {
		try {
			System.out.println("Connecting to " + serverURI);
			webSocketClient = new WebSocketClient(new URI(serverURI)) //, (Draft)null)
			{
				@Override
				public void onMessage(String mess) {
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					System.out.println("onOpen");
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.out.println("onClose");
				}

				@Override
				public void onError(Exception ex) {
					System.err.println("onError");
					System.err.println(ex.toString());
				}
			};
			webSocketClient.connect(); // IMPORTANT! Do not forget that one...
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public static void main(String... args) {
		LSM303Reader reader = new LSM303Reader();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			read = false;
		}));
		reader.startReader();
	}
}
