package nmea.api;

import java.util.List;


/**
 * A Controller.
 * This class is final, and can be used as it is.
 *
 * @author Olivier Le Diouris
 * @version 1.0
 * @see nmea.api.NMEAReader
 * @see nmea.api.NMEAClient
 * @see nmea.api.NMEAEvent
 * @see nmea.api.NMEAException
 */
public final class NMEAParser extends Thread {
	protected String nmeaPrefix = "";
	private String[] nmeaSentence = null;

	private String nmeaStream = "";
	private final static long MAX_STREAM_SIZE = 2048;
	public final static String STANDARD_NMEA_EOS = new String(new char[]{0x0A, 0x0D}); // "\r\n";

	private final static String SPLIT_NMEA_SENTENCE_AT = "\n";

	private List<NMEAListener> NMEAListeners = null; // new ArrayList(2);

	NMEAParser instance = null;

	/**
	 * @param al The ArrayList of the Listeners instanciated by the NMEAClient
	 */

	public NMEAParser(List<NMEAListener> al) {
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println(this.getClass().getName() + ":Creating parser");
		instance = this;
		NMEAListeners = al;
		this.addNMEAListener(new NMEAListener() {
			public void dataRead(NMEAEvent e) {
//        System.out.println("Receieved Data:" + e.getContent());
				nmeaStream += e.getContent();
				// TODO Broadcast that ?

				// Send to parser
				String s = "";
				try {
					while (s != null) {
						s = instance.detectSentence();
						if (s != null)
							instance.fireDataDetected(new NMEAEvent(this, s));
					}
				} catch (NMEAException ne) {
					ne.printStackTrace();
				}
			}
		});
	}

	public String getNmeaPrefix() {
		return this.nmeaPrefix;
	}

	public void setNmeaPrefix(String s) {
		this.nmeaPrefix = s;
	}

//	public void setEOS(String str) {
//		SPLIT_NMEA_SENTENCE_AT = str;
//	}

//	public static String getEOS() {
//		return SPLIT_NMEA_SENTENCE_AT;
//	}

	public String[] getNmeaSentence() {
		return this.nmeaSentence;
	}

	public void setNmeaSentence(String[] sa) {
		this.nmeaSentence = sa;
	}

	public String getNmeaStream() {
		return this.nmeaStream;
	}

	public void setNmeaStream(String s) {
		this.nmeaStream = s;
	}

	public String detectSentence() throws NMEAException {
		String ret = null;
		try {
			if (interesting()) {
				int end = nmeaStream.indexOf(SPLIT_NMEA_SENTENCE_AT);
				ret = nmeaStream.substring(0, end);
				nmeaStream = nmeaStream.substring(end + SPLIT_NMEA_SENTENCE_AT.length());
			} else {
				if (nmeaStream.length() > MAX_STREAM_SIZE)
					nmeaStream = ""; // Reset to avoid OutOfMemoryException
				return null; // Not enough info
			}
		} catch (NMEAException e) {
			throw e;
		}
		return ret;
	}

	private boolean interesting()
					throws NMEAException {
//    if (nmeaPrefix == null || nmeaPrefix.length() == 0)
//      throw new NMEAException("NMEA Prefix is not set");

//  int beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
		int beginIdx = nmeaStream.indexOf("$");
		int endIdx = nmeaStream.indexOf(SPLIT_NMEA_SENTENCE_AT);

		if (beginIdx == -1 && endIdx == -1)
			return false; // No beginning, no end !

		if (endIdx > -1 && endIdx < beginIdx) // Seek the beginning of a sentence
		{
			nmeaStream = nmeaStream.substring(endIdx + SPLIT_NMEA_SENTENCE_AT.length());
//    beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
			beginIdx = nmeaStream.indexOf("$");
		}

		if (beginIdx == -1)
			return false;
		else {
			while (true) {
				try {
					// The stream should here begin with $XX
					if (nmeaStream.length() > 6) // "$" + prefix + XXX
					{
						endIdx = nmeaStream.indexOf(SPLIT_NMEA_SENTENCE_AT);
						if (endIdx > -1) {
							if (nmeaSentence != null) {
								for (int i = 0; i < this.nmeaSentence.length; i++) {
									//  System.out.println("Checking [" + nmeaSentence[i] + "] against [" + nmeaStream + "]");
									// Fully qualified sentence
									if (nmeaSentence[i].length() == 5 && nmeaStream.startsWith("$" + nmeaSentence[i])) {
										return true;
									}
									// Specific prefix
									else if (!("*".equals(nmeaPrefix.trim())) && nmeaStream.startsWith("$" + nmeaPrefix + nmeaSentence[i])) {
										return true;
									}
									// Any prefix
									else if ("*".equals(nmeaPrefix.trim()) && nmeaStream.startsWith("$") && nmeaStream.substring(3).startsWith(nmeaSentence[i])) {
										return true;
									}
								}
							} else {
//              System.out.println("Taking everything!");
								return true; // Take all
							}
							nmeaStream = nmeaStream.substring(endIdx + SPLIT_NMEA_SENTENCE_AT.length());
						} else
							return false; // unfinished sentence
					} else
						return false; // Not long enough - Not even sentence ID
				} catch (Exception e) {
					System.err.println("Oooch!");
					e.printStackTrace();
					System.err.println("nmeaStream.length = " + nmeaStream.length() + ", Stream:[" + nmeaStream + "]");
				}
			} // End of infinite loop
		}
	}

	protected void fireDataDetected(NMEAEvent e) {
		for (int i = 0; i < NMEAListeners.size(); i++) {
			NMEAListener l = /*(NMEAListener)*/NMEAListeners.get(i);
			l.dataDetected(e);
		}
	}

	public synchronized void addNMEAListener(NMEAListener l) {
		if (!NMEAListeners.contains(l)) {
			NMEAListeners.add(l);
		}
	}

	public synchronized void removeNMEAListener(NMEAListener l) {
		NMEAListeners.remove(l);
	}

	public void run() {
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println(this.getClass().getName() + ":Parser Running");
	}
}
