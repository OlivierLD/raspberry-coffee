package http;

import java.util.List;

/**
 * To be added to an HTTP Server ({@link HTTPServer }) to manage REST requests
 */
public interface RESTRequestManager {
	HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException;
	List<HTTPServer.Operation> getRESTOperationList();
}
