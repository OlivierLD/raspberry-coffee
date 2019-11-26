package util.tests;

import utils.DumpUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;

import java.text.SimpleDateFormat;

/**
 * Proto for GPSD...
 */
public class TCPServer {
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss 'UTC'");
	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss 'UTC'");

	public TCPServer() {
	}

	private boolean verbose = true;
	private int port = 0;

	public TCPServer(String strPort) {
		try {
			port = Integer.parseInt(strPort);
		} catch (NumberFormatException nfe) {
			throw nfe;
		}

		// Infinite loop, waiting for requests
		Thread tcpListenerThread = new Thread("TCPServer") {
			public void run() {
				boolean go = true;
				try {
					ServerSocket ss = new ServerSocket(port);

					while (go) {
						Socket client = ss.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						String clientRequest = in.readLine();
						System.out.println("Received: " + clientRequest);
						DumpUtil.displayDualDump(clientRequest);

						String requestResponse = clientRequest.toUpperCase() + '\n'; // Returns the incoming message, in upper case.
						if (clientRequest.startsWith("?WATCH={") && clientRequest.endsWith("};")) {
							requestResponse = "{\"class\":\"SKY\",\"device\":\"/dev/pts/1\",\"time\":\"2005-07-08T11:28:07.114Z\",\"xdop\":1.55,\"hdop\":1.24,\"pdop\":1.99,\"satellites\":[{\"PRN\":23,\"el\":6,\"az\":84,\"ss\":0,\"used\":false},{\"PRN\":28,\"el\":7,\"az\":160,\"ss\":0,\"used\":false},{\"PRN\":8,\"el\":66,\"az\":189,\"ss\":44,\"used\":true},{\"PRN\":29,\"el\":13,\"az\":273,\"ss\":0,\"used\":false},{\"PRN\":10,\"el\":51,\"az\":304,\"ss\":29,\"used\":true},{\"PRN\":4,\"el\":15,\"az\":199,\"ss\":36,\"used\":true},{\"PRN\":2,\"el\":34,\"az\":241,\"ss\":43,\"used\":true},{\"PRN\":27,\"el\":71,\"az\":76,\"ss\":43,\"used\":true}]}" + "\n";
						}
						out.writeBytes(requestResponse);
						if ("EXIT".equalsIgnoreCase(clientRequest)) {
							go = false;
						}

						client.close();
					}
					System.out.println("Closing server socket");
					ss.close();
				} catch (Exception e) {
					System.err.println("TCP Server:" + e.toString());
					e.printStackTrace();
				}
			}
		};
		tcpListenerThread.start();
	}

	public static void main(String... args) {
		new TCPServer("2947");
	}
}
