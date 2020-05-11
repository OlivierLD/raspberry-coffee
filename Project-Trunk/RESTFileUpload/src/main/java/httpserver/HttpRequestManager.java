package httpserver;

import http.HTTPServer;
import http.RESTRequestManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private HttpRequestServer httpRequestServer = null;
	// Physical
	// ... if needed

	public HttpRequestManager() throws Exception {
		this(null);
	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public HttpRequestManager(HttpRequestServer parent) throws Exception {

		this.httpRequestServer = parent;
		restImplementation = new RESTImplementation(this);
//		restImplementation.setRelayManager(this.relayManager);
	}

	public void onExit() { // Cleanup
		if ("true".equals(System.getProperty("server.verbose", "false"))) {
			System.out.println("Cleaning up - HttpRequestManager");
		}
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return response as defined in the {@link RESTImplementation}
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

	/*
	 Specific operations
	 */

	protected List<HTTPServer.Operation> getAllOperationList() {
		return httpRequestServer.getAllOperationList();
	}

}
