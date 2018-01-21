package sample.ws;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static utils.StaticUtil.userInput;

public class WSPublisher {

	private static boolean verbose = false;

	private WebSocketClient webSocketClient = null;
	private final static NumberFormat VOLT_FMT = new DecimalFormat("##0.00");

	private void initWebSocketConnection(String serverURI) {
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// TODO Implement this method
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

	public void stop() {
		webSocketClient.close();
	}

	public WSPublisher() {
		String wsUri = System.getProperty("ws.uri", "ws://localhost:9876/");
		initWebSocketConnection(wsUri);
		webSocketClient.connect();
	}

	private void publish(float voltage) {
		webSocketClient.send(VOLT_FMT.format(voltage));
	}

	public static void main(String... args) {
		verbose = "true".equals(System.getProperty("verbose", "false"));
		final WSPublisher publisher = new WSPublisher();
		boolean go = true;
		System.out.println("Enter the Voltage (like 12.34) and hit [Return], Q to Quit.");
		while (go) {
			String str = userInput("Voltage> ");
			if ("Q".equalsIgnoreCase(str)) {
				go = false;
			} else {
				try {
					float f = Float.parseFloat(str);
					publisher.publish(f);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		publisher.stop();
		System.out.println("Bye!");
	}
}
