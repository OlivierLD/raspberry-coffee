package utils.samples.tcp.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Accepts multiple clients.
 * Sends back to the client what the client told the server.
 */
public class TCPMultiServer {
	private ServerSocket serverSocket;

	/**
	 * Start the server
	 * @param port TCP Port
	 * @throws Exception
	 */
	public void start(int port) throws Exception {
		System.out.printf("(%s) - Starting server on port %d\n", this.getClass().getName(), port);
		serverSocket = new ServerSocket(port);
		while (true) {
			new EchoClientHandler(serverSocket.accept()).start(); // All in once!
		}
	}

	/**
	 * Stop the server
	 * @throws Exception
	 */
	public void stop() throws Exception {
		System.out.printf("(%s) Stopping TCP Server\n", this.getClass().getName());
		serverSocket.close();
	}

	/**
	 * Listener to ONE TCP client
	 * Stops when a "." is received.
	 * Features are implemented in the "run" method, in its while loop.
	 */
	private static class EchoClientHandler extends Thread {
		private final Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		public EchoClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				String inputLine;
				// Loops until a "." is received.
				while ((inputLine = in.readLine()) != null) {
					System.out.printf("(%s) Read >> %s\n", this.getClass().getName(), inputLine);
					if (".".equals(inputLine)) {
						out.println("bye");
						break;
					}
					// The actual server's job.
					out.println(new StringBuilder(inputLine).reverse().toString()); // Return what's received, backwards.
				}

				System.out.printf("(%s) - End of thread\n", this.getClass().getName());

				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String... args) throws Exception {
		TCPMultiServer server = new TCPMultiServer();
		server.start(5_555);
	}

}
