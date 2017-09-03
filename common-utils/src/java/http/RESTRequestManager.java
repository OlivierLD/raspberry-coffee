package http;

import java.util.List;

public interface RESTRequestManager {
	HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException;
	List<HTTPServer.Operation> getRESTOperationList();
}
