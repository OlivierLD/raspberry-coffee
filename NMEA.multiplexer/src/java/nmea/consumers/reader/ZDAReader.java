package nmea.consumers.reader;

import java.util.List;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;

/**
 * Generates ZDA String (NMEA).
 */
public class ZDAReader extends NMEAReader {

	private static final String DEFAULT_DEVICE_PREFIX = "GP";
	private String devicePrefix = DEFAULT_DEVICE_PREFIX;

	public ZDAReader(List<NMEAListener> al) {
		super(al);
	}

	public String getDevicePrefix() {
		return this.devicePrefix;
	}

	public void setDevicePrefix(String devicePrefix) {
		this.devicePrefix = devicePrefix;
	}

	@Override
	public void startReader() {
		super.enableReading();
		while (this.canRead()) {
			// Read data every 1 second
			try {
				// Generate NMEA String
				String zdaString = StringGenerator.generateZDA("GP", System.currentTimeMillis());
				zdaString += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, zdaString));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1_000L); // TODO Make this a parameter
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public void closeReader() throws Exception {
	}
}