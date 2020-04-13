package nmea.forwarders;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.util.Properties;

/**
 * Example: How to override a Forwarder
 *
 * This one does not log any data until a valid RMC has been received,
 * and spits out the positions, in a non NMEA format, for RMC strings only.
 */
public class ExtendedTransformerDataFileWriter extends DataFileWriter {
	private boolean ok2log = true; // Possibly overridden by property 'wait.for.active.RMC', see below.
	private boolean verbose = false;

	public ExtendedTransformerDataFileWriter(String fName) throws Exception {
		super(fName);
	}
	public ExtendedTransformerDataFileWriter(String fName, Boolean append) throws Exception {
		super(fName, append);
	}

	private int nbRecProcessed = 0;

	@Override
	public void write(byte[] message) {
		String mess = new String(message).trim(); // trim removes \r\n
		String newFormat = null;
		if (mess.substring(3).startsWith("RMC,")) {
			RMC rmc = StringParsers.parseRMC(mess);
			if (!this.ok2log) { // Wait for Active RMC (Valid). WARNING: Once the sentence is valid, it's on. No way back here.
				if (rmc.isValid()) {
					this.ok2log = true;
				}
			}
			if (this.ok2log) {
				newFormat = String.format("{ %f, %f },", rmc.getGp().lat, rmc.getGp().lng);
			}
			if (this.ok2log) {
				nbRecProcessed += 1;
				super.write(newFormat.getBytes());
				if (this.verbose) {
					System.out.println(String.format("Processed %d record(s)", nbRecProcessed));
				}
			}
		}
	}

	/*
	 * getBean() will return (contains) the name of the extended class, if it is.
	 */

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);

		if ("true".equals(props.getProperty("wait.for.active.RMC"))) { // true | false Added Aug-20, 2018. Other props may be managed in a SubClass
			ok2log = false;
		}
		if ("true".equals(props.getProperty("verbose"))) {
			verbose = true;
		}
	}
}
