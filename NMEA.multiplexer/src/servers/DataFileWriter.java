package servers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataFileWriter implements Forwarder
{
	private BufferedWriter dataFile;

	public DataFileWriter(String fName) throws Exception
	{
		try
		{
			this.dataFile = new BufferedWriter(new FileWriter(fName));
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	@Override
	public void write(byte[] message)
	{
		try {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				this.dataFile.write(mess + '\n');
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void close()
	{
		try {
			this.dataFile.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
