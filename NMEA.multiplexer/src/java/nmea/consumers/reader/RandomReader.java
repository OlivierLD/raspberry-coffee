package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringParsers;
import nmea.utils.NMEAUtils;

import java.util.List;

/**
 * Generates random crap.
 * For debugging.
 */
public class RandomReader extends NMEAReader {

	public RandomReader(List<NMEAListener> al) {
		super(al);
	}

	@Override
	public void read() {
		super.enableReading();
		while (this.canRead()) {
			// Read data every 1 second
			try {
				// Generate NMEA String
				String customString = generateSentence("AA", "RND", Double.toString(Math.random())) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000L); // TODO Make this a parameter
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	// Custom methods
	public static String generateSentence(String devicePrefix, String id, String value) {
		String custom = devicePrefix + id + "," + value;
		// Checksum
		int cs = StringParsers.calculateCheckSum(custom);
		custom += ("*" + NMEAUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
		return "$" + custom;
	}

	@Override
	public void closeReader() throws Exception {
	}
}