package tcp.greetings;

import org.junit.Test;
import utils.samples.tcp.clients.TCPGreetingsClient;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class TCPTests {

	@Test
	public void givenGreetingClient_whenServerRespondsWhenStarted_thenCorrect() {
		TCPGreetingsClient client = new TCPGreetingsClient();
		try {
			client.startConnection("127.0.0.1", 6666);
			String response = client.sendMessage("hello server");
			assertEquals("hello client", response);
		} catch (Exception ex) {
			// Ooch!
			ex.printStackTrace();
			fail();
		}
	}

}
