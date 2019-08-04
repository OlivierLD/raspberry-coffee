package robot.ws;

// import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import com.pi4j.io.i2c.I2CFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

public class ServoHat {
	private WebSocketClient webSocketClient = null;
	private Robot robot = null;
	private static Thread me = null;

	public ServoHat() throws I2CFactory.UnsupportedBusNumberException {
		if (!"true".equals(System.getProperty("no.robot", "false"))) {
			robot = new Robot();
		}
		me = Thread.currentThread();
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);
	}

	public Robot getRobot() {
		return robot;
	}

	public void bye() {
		if (webSocketClient != null) {
			webSocketClient.close();
		}
	}

	private void initWebSocketConnection(String serverURI) {
		try {
			System.out.println("Connecting to " + serverURI);
			webSocketClient = new WebSocketClient(new URI(serverURI)) { //, (Draft)null)
				@Override
				public void onMessage(String mess) {
					//    System.out.println("onMessage");
					//    System.out.println("    . Text message :[" + mess + "]");
					JSONObject json = new JSONObject(mess);
					String command = ((JSONObject) json.get("data")).get("text").toString().replace("&quot;", "\"");
					String value = (new JSONObject(command)).getString("command");
					//  System.out.println("    . Mess content:[" + ((JSONObject)json.get("data")).get("text") + "]");
					System.out.println("Command Value:" + value);
					if ("close".equals(value)) {
						bye();
						if (robot != null) {
							robot.stop();
						}
					} else {
						// Drive the robot here
						if (robot != null) {
							try {
								if ("forward".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false"))) {
										System.out.println(String.format("%s, %d", value, speed));
									}
									robot.forward(speed);
								} else if ("backward".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false"))) {
										System.out.println(String.format("%s, %d", value, speed));
									}
									robot.backward(speed);
								} else if ("left".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false"))) {
										System.out.println(String.format("%s, %d", value, speed));
									}
									robot.left(speed);
								} else if ("right".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false"))) {
										System.out.println(String.format("%s, %d", value, speed));
									}
									robot.right(speed);
								} else if ("stop".equals(value)) {
									robot.stop();
								} else {
									System.out.println("Unsupported command [" + value + "]");
								}
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
						} else {
							System.out.println("No robot available.");
						}
					}
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					System.out.println("onOpen");
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.out.println("onClose");
					synchronized (me) {
						me.notify();
					}
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

	public static void help() {
		System.out.println("Requires a WebSocket server to run.");
		System.out.println("prompt> cd node; node robot.server.js & ");
		System.out.println();
		System.out.println("System variables: no.robot,  set to \"true\" to test the WebSocket part (no robot, not on the RPi), default is \"false\"");
		System.out.println("                  ws.uri,    default is ws://localhost:9876/");
		System.out.println("                  test.only, set to \"true\" to test the robot, default is \"false\"");
		System.out.println("                  robot.verbose, set to \"true\" to see console output, default is \"false\"");
	}

	public static void test(ServoHat proto) throws IOException {
	  /* Now move the robot around!
     * Each call below takes two parameters:
     *  - speed: The speed of the movement, a value from 0-255.  The higher the value
     *           the faster the movement.  You need to start with a value around 100
     *           to get enough torque to move the robot.
     *  - time (seconds):  Amount of time to perform the movement.  After moving for
     *                     this amount of seconds the robot will stop.  This parameter
     *                     is optional and if not specified the robot will start moving
     *                     forever.
     */
		System.out.println("Forward");
		proto.getRobot().forward(150, 1.0f);   // Move forward at speed 150 for 1 second.
		System.out.println("Left");
		proto.getRobot().left(200, 0.5f);      // Spin left at speed 200 for 0.5 seconds.
		System.out.println("Forward");
		proto.getRobot().forward(150, 1.0f);   // Repeat the same movement 3 times below...
		System.out.println("Left");
		proto.getRobot().left(200, 0.5f);
		System.out.println("Forward");
		proto.getRobot().forward(150, 1.0f);
		System.out.println("Left");
		proto.getRobot().left(200, 0.5f);
		System.out.println("Forward");
		proto.getRobot().forward(150, 1.0f);
		System.out.println("Right");
		proto.getRobot().right(200, 0.5f);

		// Spin in place slowly for a few seconds.
		System.out.println("Right...");
		proto.getRobot().right(100);  // No time is specified so the robot will start spinning forever.
		Robot.delay(2.0f); // Pause for a few seconds while the robot spins (you could do other processing here though!).
		proto.getRobot().stop();      // Stop the robot from moving.

		// Now move backwards and spin right a few times.
		System.out.println("Backward");
		proto.getRobot().backward(150, 1.0f);
		System.out.println("Right");
		proto.getRobot().right(200, 0.5f);
		System.out.println("Backward");
		proto.getRobot().backward(150, 1.0f);
		System.out.println("Right");
		proto.getRobot().right(200, 0.5f);
		System.out.println("Backward");
		proto.getRobot().backward(150, 1.0f);
		System.out.println("Right");
		proto.getRobot().right(200, 0.5f);
		System.out.println("Backward");
		proto.getRobot().backward(150, 1.0f);

		// Free run
		System.out.println("Free run");
		proto.getRobot().forward(200);

		try {
			Thread.sleep(2500);
		} catch (InterruptedException ie) {
		}
		proto.getRobot().stop();

		System.out.println("That's it!");
	}

	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		help();
		ServoHat proto = new ServoHat();
		if ("true".equals(System.getProperty("test.only", "false"))) {
			test(proto);
		} else {
			System.out.println("Driving the robot.");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				proto.bye();
				synchronized (me) {
					me.notify();
				}
			}));
			synchronized (me) {
				try {
					me.wait();
				} catch (InterruptedException ie) {
				}
			}
		}
	}
}
