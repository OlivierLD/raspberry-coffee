package nmea.forwarders;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class DataFileWriter implements Forwarder {
	private BufferedWriter dataFile;
	private String log;
	private boolean append = false;
	private Properties props = null;

	private boolean ok2log = true;

	public DataFileWriter(String fName) throws Exception {
		this(fName, false);
	}
	public DataFileWriter(String fName, boolean append) throws Exception {
		this.log = fName;
		this.append = append;
		try {
			this.dataFile = new BufferedWriter(new FileWriter(fName, append));
		} catch (Exception ex) {
			System.err.println(String.format("When creating [%s]", fName));
			throw ex;
		}
	}

	@Override
	public void write(byte[] message) {
		try {
			String mess = new String(message).trim(); // trim removes \r\n

			if (!ok2log) {
				if (mess.substring(3).startsWith("RMC,")) { // Wait for Active RMC (Valid)
					RMC rmc = StringParsers.parseRMC(mess);
					if (rmc.isValid()) {
						ok2log = true;
					}
				}
			}
			if (ok2log) {
				if (!mess.isEmpty()) {
					this.dataFile.write(mess + '\n');
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.dataFile.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getLog() {
		return this.log;
	}
	public static class DataFileBean {
		private String cls;
		private String log;
		private boolean append;
		private String type = "file";

		public DataFileBean(DataFileWriter instance) {
			cls = instance.getClass().getName();
			log = instance.log;
			append = instance.append;
		}

		public String getLog() {
			return log;
		}
		public boolean append() { return append; }
	}

	@Override
	public Object getBean() {
		return new DataFileBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;

		if ("true".equals(props.getProperty("wait.for.active.RMC"))) { // true | false
			ok2log = false;
		}
	}
}
