package astrorest;

import http.HTTPServer;
import http.HTTPServerInterface;

import java.util.List;

public class AstroServer implements HTTPServerInterface {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private RESTImplementation restImplementation;

	public AstroServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		restImplementation = new RESTImplementation(this);
		startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new AstroServer();
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return as defined in the {@link RESTImplementation}
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
		if (this.httpVerbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	@Override
	public List<HTTPServer.Operation> getRESTOperationList() {
		return restImplementation.getOperations();
	}

	public void startHttpServer(int port) {
		try {
			this.httpServer = new HTTPServer(port, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
