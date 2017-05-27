package nmea.mux.context;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a singleton
 */
public class Context {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // HTTPServer.class.getName());
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private long startTime = 0L;
	private long managedBytes = 0L;

	private String lastDataSentence = "";
	private long lastSentenceTimestamp = 0L;

	private static Context instance;

	public synchronized static Context getInstance() {
		if (instance == null) {
			instance = new Context();
		}
		return instance;
	}

	public Logger getLogger() {
		return this.LOGGER;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getManagedBytes() {
		return managedBytes;
	}

	public void addManagedBytes(long managedBytes) {
		this.managedBytes += managedBytes;
	}

	public void setLastDataSentence(String sentence) {
		this.lastDataSentence = sentence;
		this.lastSentenceTimestamp = System.currentTimeMillis();
	}
	public StringAndTimeStamp getLastDataSentence() {
		return new StringAndTimeStamp(this.lastDataSentence, this.lastSentenceTimestamp);
	}

	public static class StringAndTimeStamp {
		String str;
		long timestamp;
		public StringAndTimeStamp(String str, long ts) {
			this.str = str;
			this.timestamp = ts;
		}
		public String getString() { return this.str; }
		public long getTimestamp() { return this.timestamp; }
	}
}
