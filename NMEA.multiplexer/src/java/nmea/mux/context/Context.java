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
}
