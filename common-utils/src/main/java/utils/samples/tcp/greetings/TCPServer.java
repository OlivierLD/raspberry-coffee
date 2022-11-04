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
		System.out.printf("(%s) Starting server\n", this.getClass().getName());
		serverSocket = new ServerSocket(port);
		System.out.printf("(%s) Server waiting for connection\n", this.getClass().getName());
		clientSocket = serverSocket.accept();
		System.out.printf("(%s) Server accepted connection\n", this.getClass().getName());
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String greeting = in.readLine();
		if ("hello server".equals(greeting)) {
			out.println("hello client");
		} else {
			out.println("unrecognized greeting");
		}
		System.out.printf("(%s) One time greeting delivered, Server is done.\n", this.getClass().getName());
	}

	public void stop() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String... args) throws Exception {
		TCPServer server = new TCPServer();
		server.start(6_666);
	}
}
