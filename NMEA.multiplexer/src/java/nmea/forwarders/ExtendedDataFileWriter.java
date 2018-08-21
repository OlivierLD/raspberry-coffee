package nmea.forwarders;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.util.Properties;

/**
 * Example: How to override a Forwarder
 */
public class ExtendedDataFileWriter extends DataFileWriter {
	private boolean ok2log = true; // Possibly overridden by property 'wait.for.active.RMC', see below.

	public ExtendedDataFileWriter(String fName) throws Exception {
		super(fName);
	}
	public ExtendedDataFileWriter(String fName, Boolean append) throws Exception {
		super(fName, append);
	}

	@Override
	public void write(byte[] message) {
		String mess = new String(message).trim(); // trim removes \r\n

		if (!this.ok2log) { // Wait for Active RMC (Valid). WARNING: Once the sentence is valid, it's on. No way back here.
			if (mess.substring(3).startsWith("RMC,")) {
				RMC rmc = StringParsers.parseRMC(mess);
				if (rmc.isValid()) {
					this.ok2log = true;
				}
			}
		}
		if (this.ok2log) {
			super.write(message);
		}
	}


//	public String getLog() {
//		return this.log;
//	}

//	public static class DataFileBean {
//		private String cls;
//		private String log;
//		private boolean append;
//		private String type = "file";
//
//		public DataFileBean(ExtendedDataFileWriter instance) {
//			cls = instance.getClass().getName();
//			log = instance.log;
//			append = instance.append;
//		}
//
//		public String getLog() {
//			return log;
//		}
//		public boolean append() { return append; }
//	}

//	@Override
//	public Object getBean() {
//		return new DataFileBean(this);
//	}

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);

		if ("true".equals(props.getProperty("wait.for.active.RMC"))) { // true | false Added Aug-20, 2018. Other props may be managed in a SubClass
			ok2log = false;
		}
	}
}
