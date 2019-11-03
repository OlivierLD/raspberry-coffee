package tcp.echo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.samples.tcp.clients.TCPEchoClient;

import static junit.framework.TestCase.assertEquals;

public class TCPTests {

	TCPEchoClient client;

	@Before
	public void setup() {
		client = new TCPEchoClient();
		try {
			client.startConnection("127.0.0.1", 4444);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
			client.stopConnection();
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

			assertEquals("hello", resp1);
			assertEquals("world", resp2);
			assertEquals("!", resp3);
			assertEquals("good bye", resp4);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
