package http;

public interface HTTPServerInterface {
	HTTPServer.Response onRequest(HTTPServer.Request request);
}
