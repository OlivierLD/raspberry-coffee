package tcp.greetings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.samples.tcp.clients.TCPGreetingsClient;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TCPTests {

	TCPGreetingsClient client;

	@Before
	public void setup() {
		client = new TCPGreetingsClient();
		try {
			client.startConnection("127.0.0.1", 6666);
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
	public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() {
		try {
			String response = client.sendMessage("hello server");
			assertEquals("hello client", response);
		} catch (Exception ex) {
			// Ooch!
			ex.printStackTrace();
			fail();
		}
	}

}
