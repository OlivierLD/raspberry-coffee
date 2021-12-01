package tcp.echo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.samples.tcp.clients.TCPEchoClient;
import utils.samples.tcp.echo.TCPServer;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TCPMultiTests {

	private TCPServer server;
	private final int PORT = 5555;

	@Before
	public void setup() {
		System.out.println(">>> Test Setup");
		Thread tcpServer = new Thread(() -> {
			try {
				server = new TCPServer();
				server.start(PORT);
				System.out.println("TCP Server is started on port " + PORT);
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
	}

	@After
	public void tearDown() {
		try {
			System.out.println("Tearing down...");
//			client.sendMessage("/exit");
			server.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	@Test
	public void givenClient1_whenServerResponds_thenCorrect() {
		TCPEchoClient client1 = new TCPEchoClient();
		try {
			client1.startConnection("127.0.0.1", PORT);
			String msg1 = client1.sendMessage("hello");
			String msg2 = client1.sendMessage("world");
			String terminate = client1.sendMessage(".");

			System.out.println("msg1:" + msg1);
			System.out.println("msg2:" + msg2);
			System.out.println("terminate:" + terminate);

			assertEquals(msg1, "hello");
			assertEquals(msg2, "world");
			assertEquals(terminate, "good bye");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		} finally {
			try {
				client1.stopConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Test
	public void givenClient2_whenServerResponds_thenCorrect() {
		TCPEchoClient client2 = new TCPEchoClient();
		try {
			client2.startConnection("127.0.0.1", PORT);
			String msg1 = client2.sendMessage("hello");
			String msg2 = client2.sendMessage("world");
			String terminate = client2.sendMessage(".");

			System.out.println("msg1:" + msg1);
			System.out.println("msg2:" + msg2);
			System.out.println("terminate:" + terminate);

			assertEquals(msg1, "hello");
			assertEquals(msg2, "world");
			assertEquals(terminate, "good bye");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		} finally {
			try {
				client2.stopConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
