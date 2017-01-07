package nmea.api;

import http.utils.DumpUtil;
import nmea.parser.StringParsers;

import java.util.Arrays;
import java.util.List;


/**
 * A Controller.
 * This class is final, and can be used as it is.
 *
 * Its job is to detect potential sentences in the NMEA stream of characters.
 * When a sentence (to be validated) is detected, it broadcasts an NMEAEvent
 * to all the registered NMEAListeners, see the {@link #fireDataDetected(NMEAEvent)} method.
 *
 * The NMEAListeners to register are sent to the constructor. They can also be added later on.
 *
 * @author Olivier Le Diouris
 * @version 1.0
 * @see nmea.api.NMEAReader
 * @see nmea.api.NMEAClient
 * @see nmea.api.NMEAEvent
 * @see nmea.api.NMEAException
 */
public final class NMEAParser extends Thread {
	protected String[] nmeaPrefix = null;
	private String[] nmeaSentence = null;

	private String nmeaStream = "";
	private final static long MAX_STREAM_SIZE = 2048;
	public final static String STANDARD_NMEA_EOS = new String(new char[]{0x0D, 0x0A}); // "\r\n";

	public final static String NMEA_SENTENCE_SEPARATOR = "\n";

	private List<NMEAListener> NMEAListeners = null; // new ArrayList(2);

	NMEAParser instance = null;

	/**
	 * @param al The ArrayList of the Listeners instantiated by the NMEAClient
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
				// Send to parser
				String s = "";
				try {
					while (s != null) {
						s = instance.detectSentence();
						if (s != null && s.length() > 6 && s.startsWith("$")) { // Potentially valid
							// TODO ? RegExp on the full sentence. Maybe not too user friendly...
							boolean broadcast = true;
							if (nmeaPrefix != null) {
								for (String device : nmeaPrefix) {
									if (device.trim().length() > 0 &&
													( (!device.startsWith("~") && !device.equals(StringParsers.getDeviceID(s))) ||
																	device.startsWith("~") && device.substring(1).equals(StringParsers.getDeviceID(s)))) {
										broadcast = false;
										break;
									}
								}
							}
							// Negative filters
							if (broadcast && nmeaSentence != null) {
								String thisId = StringParsers.getSentenceID(s);
								for (String prefix : nmeaSentence) {
									if (prefix.startsWith("~") && thisId.equals(prefix.substring(1))) {
										broadcast = false;
										break;
									}
								}
								// Positive filters
								if (broadcast && Arrays.stream(nmeaSentence).filter(id -> !id.startsWith("~")).count() > 0) {
									broadcast = false;
									for (String prefix : nmeaSentence) {
										if (!prefix.startsWith("~") && thisId.equals(prefix)) {
											broadcast = true;
											break;
										}
									}
								}
							}
							if (broadcast) {
								instance.fireDataDetected(new NMEAEvent(this, s));
							}
						}
					}
				} catch (NMEAException ne) {
					ne.printStackTrace();
				}
			}
		});
	}

	public String[] getDeviceFilters() {
		return this.nmeaPrefix;
	}

	public void setDeviceFilters(String[] s) {
		this.nmeaPrefix = s;
	}

	public String[] getSentenceFilters() {
		return this.nmeaSentence;
	}

	public void setSentenceFilters(String[] sa) {
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
				// DEBUG
//			System.out.println("=== NMEAParser ===");
//			DumpUtil.displayDualDump(nmeaStream);
				int end = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR);
				ret = nmeaStream.substring(0, end);
				nmeaStream = nmeaStream.substring(end + NMEA_SENTENCE_SEPARATOR.length());
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

	/**
	 * Detects a potentially valid NMEA Sentence
	 * @return tgrue if a potential sentence is detected.
	 * @throws NMEAException
	 */
	private boolean interesting()
					throws NMEAException {
//    if (nmeaPrefix == null || nmeaPrefix.length() == 0)
//      throw new NMEAException("NMEA Prefix is not set");

//  int beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
		int beginIdx = nmeaStream.indexOf("$");
		int endIdx = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR);

		if (beginIdx == -1 && endIdx == -1)
			return false; // No beginning, no end !

		if (endIdx > -1 && endIdx < beginIdx) { // Seek the beginning of a sentence
			nmeaStream = nmeaStream.substring(endIdx + NMEA_SENTENCE_SEPARATOR.length());
//    beginIdx = nmeaStream.indexOf("$" + this.nmeaPrefix);
			beginIdx = nmeaStream.indexOf("$");
		}

		if (beginIdx == -1)
			return false;
		else {
			while (true) {
				try {
					// The stream should here begin with $XX
					if (nmeaStream.length() > 6) { // "$" + prefix + XXX
						endIdx = nmeaStream.indexOf(NMEA_SENTENCE_SEPARATOR);
						if (endIdx > -1) {
//							if (nmeaSentence != null) {
//								for (int i = 0; i < this.nmeaSentence.length; i++) {
//									//  System.out.println("Checking [" + nmeaSentence[i] + "] against [" + nmeaStream + "]");
//									// Fully qualified sentence
//									if (nmeaSentence[i].length() == 5 && nmeaStream.startsWith("$" + nmeaSentence[i])) {
//										return true;
//									}
//									// Specific prefix
//									else if (!("*".equals(nmeaPrefix.trim())) && nmeaStream.startsWith("$" + nmeaPrefix + nmeaSentence[i])) {
//										return true;
//									}
//									// Any prefix
//									else if ("*".equals(nmeaPrefix.trim()) && nmeaStream.startsWith("$") && nmeaStream.substring(3).startsWith(nmeaSentence[i])) {
//										return true;
//									}
//									nmeaStream = nmeaStream.substring(endIdx + NMEA_SENTENCE_SEPARATOR.length());
//								}
//							} else {
//							System.out.println("Taking everything!");
								return true; // Take all
//							}
//					  nmeaStream = nmeaStream.substring(endIdx + NMEA_SENTENCE_SEPARATOR.length());
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
		this.NMEAListeners.stream().forEach(listener -> listener.dataDetected(e));
	}

	public synchronized void addNMEAListener(NMEAListener l) {
		if (!this.NMEAListeners.contains(l)) {
			this.NMEAListeners.add(l);
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
