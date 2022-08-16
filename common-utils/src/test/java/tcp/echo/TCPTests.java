package tcp.echo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.samples.tcp.clients.TCPEchoClient;
import utils.samples.tcp.echo.TCPServer;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TCPTests {

	TCPEchoClient client;
	TCPServer server;
	private final int port = 4_444;

	@Before
	public void setup() {
		System.out.println(">>> Test Setup");
		Thread tcpServer = new Thread(() -> {
			try {
				server = new TCPServer();
				server.start(port);
				System.out.println("TCP Server is started on port " + port);
			} catch (Exception ex) {
				fail(ex.toString());
			}
		});
		tcpServer.start(); // A Thread
		// Wait?
		try {
			System.out.println(">>> Waiting for the server to come up.");
			Thread.sleep(2_000L);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		client = new TCPEchoClient();
		try {
			client.startConnection("127.0.0.1", port); // Requires a server to be running
		} catch (Exception ex) {
			throw new RuntimeException(ex);
//			ex.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
//			client.sendMessage("/exit");
			client.stopConnection();
			server.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void givenClient_whenServerEchosMessage_thenCorrect() {
		try {
			String resp1 = client.sendMessage("hello");
			String resp2 = client.sendMessage("world");
			String resp3 = client.sendMessage("!");
			String resp4 = client.sendMessage(".");

			System.out.println("Returned 1: " + resp1);
			System.out.println("Returned 2: " + resp2);
			System.out.println("Returned 3: " + resp3);
			System.out.println("Returned 4: " + resp4);

			assertEquals("hello", resp1);
			assertEquals("world", resp2);
			assertEquals("!", resp3);
			assertEquals("good bye", resp4);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
