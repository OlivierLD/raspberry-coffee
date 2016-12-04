package servers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataFileWriter implements Forwarder {
	private BufferedWriter dataFile;
	private String log;

	public DataFileWriter(String fName) throws Exception {
		this.log = fName;
		try {
			this.dataFile = new BufferedWriter(new FileWriter(fName));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void write(byte[] message) {
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
	public void close() {
		try {
			this.dataFile.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static class DataFileBean {
		String cls;
		String log;

		public DataFileBean(DataFileWriter instance) {
			cls = instance.getClass().getName();
			log = instance.log;
		}
	}

	@Override
	public Object getBean() {
		return new DataFileBean(this);
	}
}
