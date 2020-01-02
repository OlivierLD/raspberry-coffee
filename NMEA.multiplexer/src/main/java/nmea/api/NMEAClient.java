package nmea.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A View. Must be extended to be used from the client.
 * The typical sequence (standalone instance) would look like this:
 * <pre>
 * public class CustomSerialClient extends NMEAClient {
 *   public CustomSerialClient(String s, String[] sa) {
 *     super(s, sa);
 *   }
 *
 *   public static void main(String... args) {
 *     String[] prefix = { "II" };
 *     String[] array = {"HDM", "GLL", "XTE", "MWV", "VHW"};
 *     CustomSerialClient customClient = new CustomSerialClient(prefix, array);
 *     customClient.initClient();
 *      // CustomXXReader extends NMEAReader
 *     customClient.setReader(new CustomXXReader(customClient.getListeners()));
 *     customClient.startWorking();
 *   }
 * }
 * </pre>
 *
 * <dl>
 *   <dt>Note:</dt>
 *   <dd>
 *     Prefixes and Sentence IDs can be negated with <b>~</b>.
 *     <br>
 *     Positive filters are linked with and <b>or</b>, Negative ones with an <b>and</b>.
 *     <br>
 *     A filter like "HDM", "GLL", "~RMC", "~XDR" would mean
 *     ( HDM or GLL) and (not RMC and not XDR).
 *     <br>
 *     It is the user's responsibility not to have contradiction in the filters, like [ "GLL", "~GLL" ],
 *     no verification is done in this area.
 *   </dd>
 * </dl>
 * <br>
 * This is the job of the NMEAClient to create NMEAListeners, and NMEAReader (to which you provide the NMEAListeners) as in the snippet above.
 * The NMEAClient receives the NMEA Sentences he's interested in through its abstract {@link #dataDetectedEvent(NMEAEvent)} method.
 * <br>
 * This <i>abstract</i> class takes care of instantiating its {@link NMEAParser}. This way, the actual client
 * extending this abstract class does not need to do it.
 * <br>
 * In the overwhelming majority of the cases, an NMEAClient will be created along with its NMEAReader companion.
 */
public abstract class NMEAClient {
	private List<NMEAListener> NMEAListeners = new ArrayList<>(2);
	private NMEAParser parser;
	private NMEAReader reader;
	private String[] devicePrefix = null;
	private String[] sentenceArray = null;

	protected Properties props = null;

	protected boolean verbose = false;

	public NMEAClient() {
		this(null, null, null);
	}

	/**
	 * Create the client
	 *
	 * @param prefix   the Device Identifier. Can be null. '~' negates the condition, like below.
	 * @param sentence the String Array containing the NMEA sentence identifiers to read (or not). If the identifier begins with '~', then the =sentence is dropped if the identifier matches.
	 */
	public NMEAClient(String[] prefix,
	                  String[] sentence) {
		this(prefix, sentence, null);
	}

	public NMEAClient(Multiplexer multiplexer) {
		this(null, null, multiplexer);
	}

	public NMEAClient(String[] prefix,
	                  String[] sentence,
	                  Multiplexer multiplexer) {
		this.setDevicePrefix(prefix);
		this.setSentenceArray(sentence);
		this.setMultiplexer(multiplexer);
	}

	protected Multiplexer multiplexer;

	public void setMultiplexer(Multiplexer multiplexer) {
		this.multiplexer = multiplexer;
	}

	public Multiplexer getMutiplexer() {
		return this.multiplexer;
	}

	public void initClient() {
		this.addNMEAListener(new NMEAListener() {
			@Override
			public void dataDetected(NMEAEvent e) {
				dataDetectedEvent(e);
			}
		});
		parser = new NMEAParser(NMEAListeners);
		parser.setDeviceFilters(this.getDevicePrefix());
		parser.setSentenceFilters(this.getSentenceArray());
	}

	public void setProperties(Properties props) {
		this.props = props;
	}

	public void setDevicePrefix(String[] s) {
		this.devicePrefix = s;
	}

	public String[] getDevicePrefix() {
		return this.devicePrefix;
	}

	public void setSentenceArray(String[] sa) {
		this.sentenceArray = sa;
	}

	public String[] getSentenceArray() {
		return this.sentenceArray;
	}

	public void setParser(NMEAParser p) {
		this.parser = p;
	}

	public NMEAParser getParser() {
		return this.parser;
	}

	public void setReader(NMEAReader r) {
		this.reader = r;
	}

	public NMEAReader getReader() {
		return this.reader;
	}

	public List<NMEAListener> getListeners() {
		return this.NMEAListeners;
	}

	public void startWorking() {
		synchronized (this) {
			synchronized (this.reader) {
				this.reader.start();
			}
			synchronized (this.parser) {
				this.parser.start();
			}
		}
	}

	public void stopDataRead() {
		this.getListeners().forEach(listener -> listener.stopReading(new NMEAEvent(this)));
		// Remove listeners
		removeAllListeners();

		try {
			this.getReader().closeReader();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * This one must be overwritten to customize the behavior of the client,
	 * like the destination of the data.
	 */
	public abstract void dataDetectedEvent(NMEAEvent e);

	public synchronized void addNMEAListener(NMEAListener l) {
		if (!NMEAListeners.contains(l)) {
			NMEAListeners.add(l);
		}
	}

	public synchronized void removeNMEAListener(NMEAListener l) {
		NMEAListeners.remove(l);
	}

	public synchronized void removeAllListeners() {
		while (NMEAListeners.size() > 0) {
			NMEAListeners.remove(0);
		}
	}

	public abstract Object getBean();

	public boolean isVerbose() {
		return this.verbose;
	}

	public void setVerbose(boolean b) {
		this.verbose = b;
		if (this.getReader() != null) {
			this.getReader().setVerbose(b);
		}
	}
}
