package http;

import java.util.List;

public interface HTTPServerInterface {
	HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException;
	List<HTTPServer.Operation> getRESTOperationList();
}
