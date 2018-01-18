package paddle.ws;

import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import paddle.MeArmPilotImplementation;

/**
 * WebSocket client.
 * Listens to the events sent from teh WebGUI.
 * See mearm.server.js and mearm.pilot.html
 */
public class MeArmWebSocket {
	private WebSocketClient webSocketClient = null;
	private MeArmPilotImplementation robot = null;
	private static Thread me = null;

	public MeArmWebSocket() throws I2CFactory.UnsupportedBusNumberException {
		if (!"true".equals(System.getProperty("no.robot", "false")))
			robot = new MeArmPilotImplementation();
		me = Thread.currentThread();
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);
	}

	public MeArmPilotImplementation getRobot() {
		return robot;
	}

	public void bye() {
		if (webSocketClient != null) {
			webSocketClient.close();
		}
		MeArmPilotImplementation.shutdown();
	}

	private void initWebSocketConnection(String serverURI) {
		try {
			System.out.println("Connecting to " + serverURI);
			webSocketClient = new WebSocketClient(new URI(serverURI)) //, (Draft)null)
			{
				@Override
				public void onMessage(String mess) {
					//    System.out.println("onMessage");
					//    System.out.println("    . Text message :[" + mess + "]");
					JSONObject json = new JSONObject(mess);
					String command = ((JSONObject) json.get("data")).get("text").toString().replace("&quot;", "\"");
					String value = (new JSONObject(command)).getString("command");
					//  System.out.println("    . Mess content:[" + ((JSONObject)json.get("data")).get("text") + "]");
					System.out.println("Command Value:" + value);
					if ("stop".equals(value)) {
						bye();
					} else {
						// Drive the robot here
						if (robot != null) {
							try {
								if ("forward".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Forward %s, %d", value, speed));
									robot.moveForward(speed, speed);
								} else if ("backward".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Backward %s, %d", value, speed));
									robot.moveBackward(speed, speed);
								} else if ("left".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Left %s, %d", value, speed));
									robot.turnLeft(speed, speed);
								} else if ("right".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Right %s, %d", value, speed));
									robot.turnRight(speed, speed);
								} else if ("up".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Up %s, %d", value, speed));
									robot.moveUp(speed, speed);
								} else if ("down".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Down %s, %d", value, speed));
									robot.moveDown(speed, speed);
								} else if ("open".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Open %s, %d", value, speed));
									robot.openClaw(speed, speed);
								} else if ("close".equals(value)) {
									int speed = (new JSONObject(command)).getInt("speed");
									if ("true".equals(System.getProperty("robot.verbose", "false")))
										System.out.println(String.format("Close %s, %d", value, speed));
									robot.closeClaw(speed, speed);
								} else if ("stop".equals(value))
									MeArmPilotImplementation.shutdown();
								else
									System.out.println("Unsupported command [" + value + "]");
							} catch (Exception ioe) {
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
		System.out.println("prompt> cd node; node mearm.server.js & ");
		System.out.println();
		System.out.println("System variables: no.robot,  set to \"true\" to test the WebSocket part (no robot, not on the RPi), default is \"false\"");
		System.out.println("                  ws.uri,    default is ws://localhost:9876/");
		System.out.println("                  test.only, set to \"true\" to test the robot, default is \"false\"");
		System.out.println("                  robot.verbose, set to \"true\" to see console output, default is \"false\"");
	}

	public static void test(MeArmWebSocket proto) throws IOException {
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
		proto.getRobot().moveForward();   // Move forward at speed 150 for 1 second.
		System.out.println("Left");
		proto.getRobot().turnLeft();      // Spin left at speed 200 for 0.5 seconds.
		System.out.println("Forward");
		proto.getRobot().moveForward();   // Repeat the same movement 3 times below...
		System.out.println("Left");
		proto.getRobot().turnLeft();
		System.out.println("Forward");
		proto.getRobot().moveForward();
		System.out.println("Left");
		proto.getRobot().turnLeft();
		System.out.println("Forward");
		proto.getRobot().moveForward();
		System.out.println("Right");
		proto.getRobot().turnRight();

		// Spin in place slowly for a few seconds.
		System.out.println("Right...");
		proto.getRobot().turnRight();  // No time is specified so the robot will start spinning forever.

		// Now move backwards and spin right a few times.
		System.out.println("Backward");
		proto.getRobot().moveBackward();
		System.out.println("Right");
		proto.getRobot().turnRight();
		System.out.println("Backward");
		proto.getRobot().moveBackward();
		System.out.println("Right");
		proto.getRobot().turnRight();
		System.out.println("Backward");
		proto.getRobot().moveBackward();
		System.out.println("Right");
		proto.getRobot().turnRight();
		System.out.println("Backward");
		proto.getRobot().moveBackward();

		// Free run
		System.out.println("Free run");
		proto.getRobot().moveForward();

		try {
			Thread.sleep(2500);
		} catch (InterruptedException ie) {
		}
		System.out.println("That's it!");
	}

	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		help();
		MeArmWebSocket proto = new MeArmWebSocket();
		MeArmPilotImplementation.init();

		if ("true".equals(System.getProperty("test.only", "false"))) {
			test(proto);
		} else {
			System.out.println("Driving the robot.");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					proto.bye();
					synchronized (me) {
						me.notify();
					}
				}
			});
			synchronized (me) {
				try {
					me.wait();
				} catch (InterruptedException ie) {
				}
			}
		}
	}
}
