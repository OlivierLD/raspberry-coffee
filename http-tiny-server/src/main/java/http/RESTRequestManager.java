package http;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * To be added to an HTTP Server ({@link HTTPServer }) to manage REST requests
 */
public interface RESTRequestManager {
	HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException;
	List<HTTPServer.Operation> getRESTOperationList();

	default Logger getLogger() {
		final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		LOGGER.setLevel(Level.INFO);
		return LOGGER;
	}

	default boolean containsOp(String verb, String path) {
		return this.getRESTOperationList()
				.stream()
				.anyMatch(operation -> operation.getVerb().equals(verb) && RESTProcessorUtil.pathMatches(operation.getPath(), path));
	}
}
