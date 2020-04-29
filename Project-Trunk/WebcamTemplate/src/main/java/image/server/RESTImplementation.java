package image.server;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	private boolean verbose = "true".equals(System.getProperty("math.rest.verbose"));

	private SnapRequestManager snapRequestManager;
	private final static String SNAP_PREFIX = "/snap";

	public RESTImplementation(SnapRequestManager restRequestManager) {

		this.snapRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * Frame path parameters with curly braces.
	 * <p>
	 * See {@link #processRequest(Request)}
	 * See {@link HTTPServer}
	 */
	private List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					"/oplist",
					this::getOperationList,
					"List of all available operations, on all request managers."),
			/*
			 * Specific to this RequestManager
			 */
			new Operation(
					"GET",
					SNAP_PREFIX + "/last-snapshot", // TODO Prm form OpenCV
					this::getLastSnapshot,
					"Return the last snapshot.")
	);

	protected List<Operation> getOperations() {
		return this.operations;
	}

	/**
	 * This is the method to invoke to have a REST request processed as defined above.
	 *
	 * @param request as it comes from the client
	 * @return the actual result.
	 */
	public Response processRequest(Request request) throws UnsupportedOperationException {
		Optional<Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.snapRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	public static class SnapPayload {
		String status;
		String snapUrl;
		String fullPath;

		public SnapPayload status(String status) {
			this.status = status;
			return this;
		}
		public SnapPayload snapUrl(String snapUrl) {
			this.snapUrl = snapUrl;
			return this;
		}
		public SnapPayload fullPath(String fullPath) {
			this.fullPath = fullPath;
			return this;
		}
	}

	/**
	 * Verb is GET
	 *
	 * TODO Flesh it
	 *
	 * @param request
	 * @return
	 */
	private Response getLastSnapshot(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		// TODO Get header parameters for the OpenCV transformations, and apply transformation

		SnapPayload payload = new SnapPayload()
				.status("Ok")
				.fullPath(SnaphotServer.SNAP_NAME)
				.snapUrl(String.format("%s", SnaphotServer.SNAP_NAME));

		String content = new Gson().toJson(payload);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request
	 * @return
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
