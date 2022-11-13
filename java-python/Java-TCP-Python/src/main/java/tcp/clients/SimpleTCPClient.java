package tcp.clients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple interactive TCP client.
 * Assumes that the server is sending lines (ending with a LF)
 *
 * CLI args: --host:<IP or name> --port:5555
 */
public class SimpleTCPClient {
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void startConnection(String ip, int port) throws Exception {
		try {
			clientSocket = new Socket(ip, port);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (ConnectException ce) {
			throw new RuntimeException(String.format("ConnectException for %s, port %d", ip, port));
		}
	}

	public String sendMessage(String msg) throws Exception {
		out.println(msg);
		/*String resp =*/ return in.readLine();
		// return resp;
	}

	public void stopConnection() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
	}

	private final static String PORT_PREFIX = "--port:";
	private final static String HOST_PREFIX = "--host:";

	public static void main(String... args) {

		final AtomicInteger port = new AtomicInteger(5_555);
		final AtomicReference<String> host = new AtomicReference<>("127.0.0.1");

		// Parse CLI parameters
		Arrays.asList(args).forEach(arg -> {
			if (arg.startsWith(PORT_PREFIX)) {
				try {
					port.set(Integer.parseInt(arg.substring(PORT_PREFIX.length())));
					System.out.printf("(%s) Port now set to %d\n", SimpleTCPClient.class.getName(), port.get());
				} catch (NumberFormatException nfe) {
					System.out.printf("Invalid port in [%s], keeping default %d\n", arg, port.get());
					System.out.printf("(%s) Host is now %s\n", SimpleTCPClient.class.getName(), host);
				}
			} else if (arg.startsWith(HOST_PREFIX)) {
				host.set(arg.substring(HOST_PREFIX.length()));
			}
		});

		SimpleTCPClient client = new SimpleTCPClient();
		try {
			client.startConnection(host.get(), port.get());
			System.out.printf("(%s) Enter '.' at the prompt to stop\n", SimpleTCPClient.class.getName());

			boolean keepWorking = true;
			while (keepWorking) {
				String request = System.console().readLine("Request > ");
				if (request.trim().length() > 0) {
					String response = client.sendMessage(request);
					System.out.printf("Server responded %s\n", response);
					if (".".equals(request)) {
						keepWorking = false;
					}
				} else {
					System.out.println("... Enter something!");
				}
			}
			System.out.printf("(%s) Client exiting\n", SimpleTCPClient.class.getName());
			client.stopConnection();
		} catch (Exception ex) {
			if (ex instanceof SocketException) {
				if (ex.getMessage().contains("Connection reset")) {
					System.out.println("Server connection was reset.");
				} else {
					ex.printStackTrace();
				}
			} else {
				// Ooch!
				ex.printStackTrace();
			}
		}
	}
}
