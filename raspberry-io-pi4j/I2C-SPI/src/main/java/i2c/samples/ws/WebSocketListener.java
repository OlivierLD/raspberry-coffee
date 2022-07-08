package i2c.samples.ws;

import i2c.servo.PCA9685;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class WebSocketListener {
	private final static boolean DEBUG = false;

	private boolean keepWorking = true;
	private WebSocketClient webSocketClient = null;

	PCA9685 servoBoard = null;
	private final int freq = 60;
	// For the TowerPro SG-5010
	private final static int servoMin = 150;   // -90 deg
	private final static int servoMax = 600;   // +90 deg

	private final static int STANDARD_SERVO_CHANNEL = 15;

	private int servo = STANDARD_SERVO_CHANNEL;

	public WebSocketListener() throws Exception {
		try {
			servoBoard = new PCA9685();
			servoBoard.setPWMFreq(freq); // Set frequency in Hz
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("You're not on the Pi, are you?");
		}

		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");

		initWebSocketConnection(wsUri);
	}

	private void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI), (Draft) null) {
				@Override
				public void onMessage(String mess) {
					//  System.out.println("    . Text message :[" + mess + "]");
					JSONObject json = new JSONObject(mess);
					String valueContent = ((JSONObject) json.get("data")).get("text").toString().replace("&quot;", "\"");
					JSONObject valueObj = new JSONObject(valueContent);
					//  System.out.println("    . Mess content:[" + ((JSONObject)json.get("data")).get("text") + "]");
					int servoValue = valueObj.getInt("value");
					System.out.println("Servo Value:" + servoValue);
					// TODO Drive the servo here
					if (servoBoard != null) {
						System.out.println("Setting the servo to " + servoValue);
						if (servoValue < -90 || servoValue > 90) {
							System.err.println("Between -90 and 90 only");
						} else {
							int on = 0;
							int off = (int) (servoMin + (((double) (servoValue + 90) / 180d) * (servoMax - servoMin)));
							System.out.println("setPWM(" + servo + ", " + on + ", " + off + ");");
							servoBoard.setPWM(servo, on, off);
							System.out.println("-------------------");
						}
					}
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
				}

				@Override
				public void onError(Exception ex) {
				}
			};
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) throws Exception {
		System.out.println("System variable ws.uri can be used if the URL is not ws://localhost:9876/");
		new WebSocketListener();
	}
}

