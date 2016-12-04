package nmeaproviders.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringParsers;

import java.util.List;

/**
 * Generates randowm crap.
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
				String customString = generateSentence("AA", "RND", Double.toString(Math.random())) + NMEAParser.getEOS();
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
		custom += ("*" + lpad(Integer.toString(cs, 16).toUpperCase(), "0", 2));
		return "$" + custom;
	}

	private static String lpad(String str, String with, int len) {
		String s = str;
		while (s.length() < len)
			s = with + s;
		return s;
	}

	@Override
	public void closeReader() throws Exception {
	}
}