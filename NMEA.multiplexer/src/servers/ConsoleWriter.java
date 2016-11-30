package servers;

public class ConsoleWriter implements Forwarder
{
	public ConsoleWriter() throws Exception
	{
	}

	@Override
	public void write(byte[] message)
	{
		String mess = new String(message);
		if (!mess.isEmpty()) {
			System.out.println(mess);
		}
	}

	@Override
	public void close()
	{
		System.out.println("Bye!");
	}
}
