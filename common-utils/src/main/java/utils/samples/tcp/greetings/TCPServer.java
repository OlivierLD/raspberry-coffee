package utils.samples.tcp.greetings;

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
		System.out.println("Starting server");
		serverSocket = new ServerSocket(port);
		System.out.println("Server waiting for connection");
		clientSocket = serverSocket.accept();
		System.out.println("Server accepted connection");
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String greeting = in.readLine();
		if ("hello server".equals(greeting)) {
			out.println("hello client");
		} else {
			out.println("unrecognized greeting");
		}
		System.out.println("Server is done");
	}

	public void stop() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String... args) throws Exception {
		TCPServer server = new TCPServer();
		server.start(6666);
	}
}
