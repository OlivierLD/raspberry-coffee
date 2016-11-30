package servers;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class WebSocketWriter implements Forwarder
{
	private WebSocketClient wsClient = null;
	/**
	 *
	 * @param serverURL like ws://hostname:port/
	 * @throws Exception
	 */
	public WebSocketWriter(String serverURL) throws Exception
	{
		try
		{
			wsClient = new WebSocketClient(new URI(serverURL))
			{
				@Override
				public void onOpen(ServerHandshake serverHandshake)
				{
					System.out.println("WS On Open");
				}

				@Override
				public void onMessage(String string)
				{
//        System.out.println("WS On Message");
				}

				@Override
				public void onClose(int i, String string, boolean b)
				{
					System.out.println("WS On Close");
				}

				@Override
				public void onError(Exception exception)
				{
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
			wsClient.connect();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void write(byte[] message)
	{
		try {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				this.wsClient.send(mess);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close()
	{
		try {
			this.wsClient.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
