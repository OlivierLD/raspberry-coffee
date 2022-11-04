package utils.samples.tcp.echo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void start(int port) throws Exception {
		serverSocket = new ServerSocket(port);
		clientSocket = serverSocket.accept();
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			if (".".equals(inputLine)) {
				out.println("good bye"); // Response
				break;
			}
			out.println(inputLine);
		}
	}

	public void stop() throws Exception {
		System.out.printf("(%s) Stopping server\n", this.getClass().getName());
		if (in != null) {
			in.close();
		} else {
			System.out.printf("(%s) InputStream is null\n", this.getClass().getName());
		}
		if (out != null) {
			out.close();
		} else {
			System.out.printf("(%s) OutputStream is null\n", this.getClass().getName());
		}
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String... args) throws Exception {
		TCPServer server = new TCPServer();
		server.start(4_444);
	}
}
