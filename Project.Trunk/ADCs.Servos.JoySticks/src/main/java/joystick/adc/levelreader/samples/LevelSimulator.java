package joystick.adc.levelreader.samples;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

/**
 * SIMULATES water level.
 * Can run off line, not on a RasPi
 * Sends WS Messages (see level.server.js)
 */
public class LevelSimulator implements LevelListenerInterface {
	private static WebSocketClient webSocketClient = null;
	private static String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
	private static String customerID = System.getProperty("customer.id", "Joe Shmow");

	@Override
	public void setLevel(int level) {
		if (level > 0) {
			JSONObject obj = new JSONObject();
			obj.put("customer-id", customerID);
			obj.put("water-level", level);
			if (webSocketClient != null) {
				System.out.println("Sending " + obj.toString());
				webSocketClient.send(obj.toString());
			} else
				System.out.println("No websocket client (for level)");
		}
	}

	private static void initWebSocketConnection(String serverURI) {
		System.out.println("Connecting on WS " + serverURI);
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS Connection opened");
				}

				@Override
				public void onMessage(String string) {
					try {
						JSONObject obj = new JSONObject(string);
						String ack = obj.getString("ack");
						String mess = obj.getString("mess");
						// TODO Display on the small oled screen
						System.out.println(ack + ":" + mess);
					} catch (Exception ex) {
						System.out.println("WS message:" + string);
					}
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS Connection closed");
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS Error:" + exception.toString());
				}
			};
			webSocketClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean simulating = true;

	private static boolean keepSumulating() {
		return simulating;
	}

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " parameter(s).");
		initWebSocketConnection(wsUri);
		final LevelSimulator ls = new LevelSimulator();

		final Thread me = Thread.currentThread();

		final Thread simulator = new Thread() {
			public void run() {
				while (keepSumulating()) {
					int level = (int) (Math.round(Math.random() * 7d));
					ls.setLevel(level);
					try {
						synchronized (this) {
							wait(2_000L);
						}
					} catch (InterruptedException ie) {
						System.out.println("Simulator interrupted");
					}
				}
				System.out.println("Done simulating");
			}
		};
		simulator.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println();
				simulating = false;
				if (webSocketClient != null)
					webSocketClient.close();
				else
					System.out.println("No websocket client to close");
				synchronized (simulator) {
					simulator.notify();
				}
				synchronized (me) {
					me.notify();
				}
				System.out.println("Program stopped by user's request.");
				try {
					Thread.sleep(1_000L);
				} catch (Exception ex) {
				} // Wait a bit for everything to shutdown cleanly...
			}
		});
		synchronized (me) {
			System.out.println("Main thread waiting...");
			me.wait();
		}
		System.out.println("Done.");
	}
}
