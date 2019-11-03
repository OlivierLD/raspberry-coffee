package tcp.echo;

import org.junit.Test;
import utils.samples.tcp.clients.TCPEchoClient;

import static junit.framework.TestCase.assertEquals;

public class TCPMultiTests {
	@Test
	public void givenClient1_whenServerResponds_thenCorrect() {
		TCPEchoClient client1 = new TCPEchoClient();
		try {
			client1.startConnection("127.0.0.1", 5555);
			String msg1 = client1.sendMessage("hello");
			String msg2 = client1.sendMessage("world");
			String terminate = client1.sendMessage(".");

			assertEquals(msg1, "hello");
			assertEquals(msg2, "world");
			assertEquals(terminate, "bye");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void givenClient2_whenServerResponds_thenCorrect() {
		TCPEchoClient client2 = new TCPEchoClient();
		try {
			client2.startConnection("127.0.0.1", 5555);
			String msg1 = client2.sendMessage("hello");
			String msg2 = client2.sendMessage("world");
			String terminate = client2.sendMessage(".");

			assertEquals(msg1, "hello");
			assertEquals(msg2, "world");
			assertEquals(terminate, "bye");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
