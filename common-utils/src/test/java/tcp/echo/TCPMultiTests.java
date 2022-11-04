package tcp.echo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.samples.tcp.clients.TCPEchoClient;
import utils.samples.tcp.echo.TCPMultiServer;
import utils.samples.tcp.echo.TCPServer;

import java.net.SocketException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TCPMultiTests {

	private TCPMultiServer server;
	private final int PORT = 5_555;

	@Before
	public void setup() {
		System.out.printf(">>> (%s) Test Setup\n", this.getClass().getName());
		Thread tcpServer = new Thread(() -> {
			try {
				server = new TCPMultiServer();
				server.start(PORT);
				System.out.printf("(%s) TCP Server is started on port %d\n", this.getClass().getName(), PORT);
			} catch (SocketException se) {
				if (se.getMessage().startsWith("Socket closed")) {
					System.out.printf("(managed exception) Socket closed...\n");
				} else {
					se.printStackTrace();
					fail(se.toString());
				}
			} catch (Exception ex) {
				fail(ex.toString());
			}
		});
		tcpServer.start(); // A Thread
		// Wait?
		try {
			System.out.printf(">>> (%s) Waiting for the server to come up.\n", this.getClass().getName());
			Thread.sleep(2_000L);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
			System.out.printf("(%s) Tearing down...\n", this.getClass().getName());
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
			String msg1 = client1.sendMessage("HELLO");
			String msg2 = client1.sendMessage("WORLD");
			String terminate = client1.sendMessage(".");

			System.out.printf("(%s) msg1: %s\n", this.getClass().getName(), msg1);
			System.out.printf("(%s) msg2: %s\n", this.getClass().getName(), msg2);
			System.out.printf("(%s) terminate: %s\n", this.getClass().getName(), terminate);

			assertEquals(msg1, "OLLEH");
			assertEquals(msg2, "DLROW");
			assertEquals(terminate, "bye");
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

			System.out.printf("(%s) msg1: %s\n", this.getClass().getName(), msg1);
			System.out.printf("(%s) msg2: %s\n", this.getClass().getName(), msg2);
			System.out.printf("(%s) terminate: %s\n", this.getClass().getName(), terminate);

			assertEquals(msg1, "olleh");
			assertEquals(msg2, "dlrow");
			assertEquals(terminate, "bye");
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
