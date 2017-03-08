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

}
