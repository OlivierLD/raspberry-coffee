package joystick;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import servo.StandardServo;

import java.net.URI;

import static utils.TimeUtil.delay;

/*
 * Driven by WesbSocket server. Listen to messages, and drive 2 servos accordingly.
 * See in node/server.js
 *
 * 2 Servos (UP/LR)
 *
 * Web interface available, see in node/tilt.pan.app/servo.pilot.html
 *
 * Start the WebSocket node server,
 * Start the script named pantilt.ws
 */
public class PanTiltWebSocket {
	private static StandardServo ssUD = null, ssLR = null;

	private static WebSocketClient webSocketClient = null;

	public static void main(String... args) throws Exception {
		ssUD = new StandardServo(14); // 14 : Address on the board (1..15)
		ssLR = new StandardServo(15); // 15 : Address on the board (1..15)

		// Init/Reset
		ssUD.stop();
		ssLR.stop();
		ssUD.setAngle(0f);
		ssLR.setAngle(0f);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> close(), "Shutdown Hook"));

		delay(2_000);

		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);

	}

	private static void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS On Open");
				}

				@Override
				public void onMessage(String string) {
					if ("true".equals(System.getProperty("verbose", "false"))) {
						System.out.println("WS On Message:" + string);
					}
					JSONObject message = new JSONObject(string);
					JSONObject motion = new JSONObject(message.getJSONObject("data").getString("text"));
					float roll = 0f; // (float)motion.getDouble("roll");
					float pitch = 0f; // (float)motion.getDouble("pitch");
					float yaw = 0f; // (float)motion.getDouble("yaw");
					boolean withYaw = true, withRoll = true;
					try {
						roll = (float) motion.getDouble("roll");
					} catch (Exception ex) {
						withRoll = false;
					}
					try {
						pitch = (float) motion.getDouble("pitch");
					} catch (Exception ex) {
					}
					try {
						yaw = (float) motion.getDouble("yaw");
					} catch (Exception ex) {
						withYaw = false;
					}
					System.out.println("Roll:" + roll + ", pitch:" + pitch + ", yaw:" + yaw);
					if (withYaw) {
						ssLR.setAngle(yaw);
					}
					if (withRoll) {
						ssUD.setAngle(-roll); // Actually pitch...
					}
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS On Close");
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
			webSocketClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void close() {
		System.out.println("\nExiting...");
		webSocketClient.close();
		// Reset to 0,0 before shutting down.
		ssUD.setAngle(0f);
		ssLR.setAngle(0f);
		delay(2_000);
		ssUD.stop();
		ssLR.stop();
		System.out.println("Bye");
	}
}
