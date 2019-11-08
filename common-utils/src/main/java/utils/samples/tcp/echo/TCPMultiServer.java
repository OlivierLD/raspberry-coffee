package utils.samples.tcp.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPMultiServer {
	private ServerSocket serverSocket;

	public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		while (true) {
			new EchoClientHandler(serverSocket.accept()).start(); // All in once!
		}
	}

	public void stop() throws Exception {
		serverSocket.close();
	}

	private static class EchoClientHandler extends Thread {
		private Socket clientSocket;
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
				while ((inputLine = in.readLine()) != null) {
					System.out.println(String.format("Read >> %s", inputLine));
					if (".".equals(inputLine)) {
						out.println("bye");
						break;
					}
					out.println(inputLine);
				}
				System.out.println("- End of thread");

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
		server.start(5555);
	}

}
