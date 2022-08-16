package tcp.greetings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TimeUtil;
import utils.samples.tcp.clients.TCPGreetingsClient;
import utils.samples.tcp.greetings.TCPServer;

import java.net.ConnectException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

/**
 * Requires a greetings.TCPServer to be running. Hence the @Before.
 */
public class TCPTests {

	TCPGreetingsClient client;
	int tcpPort = 6_666;

	@Before
	public void setup() {
		Thread serverThread = new Thread(() -> {
			TCPServer server = new TCPServer();
			try {
				server.start(tcpPort);
			} catch (Exception ex) {
				ex.printStackTrace();
				fail();
			}
			System.out.println("Server started, ready for clients");
		}, "server-thread");
		serverThread.start();
		// Wait for the server to start (there might be a better way ;) )
		TimeUtil.delay(2_000L);
		System.out.println("Ready for tests, let's go.");

		client = new TCPGreetingsClient();
		try {
			client.startConnection("127.0.0.1", tcpPort);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
			client.stopConnection(); // Bam!
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void givenGreetingClient_whenServerRespondsWhenStarted_thenOK() {
		try {
			String response = client.sendMessage("hello server");
			assertEquals("hello client", response);
		} catch (ConnectException ce) {
			System.out.println(ce.toString());
			System.out.println("Check your server...");
		} catch (Exception ex) {
			// Ooch!
			ex.printStackTrace();
			fail();
		}
	}

}
