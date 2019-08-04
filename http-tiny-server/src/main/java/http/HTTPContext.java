package http;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a singleton for the whole http server
 */
public class HTTPContext {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // HTTPServer.class.getName());
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private static HTTPContext instance;

	private HTTPContext() {
	}

	synchronized static HTTPContext getInstance() {
		if (instance == null) {
			instance = new HTTPContext();
		}
		return instance;
	}

	Logger getLogger() {
		return HTTPContext.LOGGER;
	}
}
