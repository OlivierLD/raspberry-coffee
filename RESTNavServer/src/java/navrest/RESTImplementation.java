package navrest;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * Those operation mostly retrieve the state of the SunFlower class, and device.
 * <br>
 * The SunFlower will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private NavRequestManager navRequestManager;

	public RESTImplementation(@Nonnull NavRequestManager restRequestManager) {

		this.navRequestManager = restRequestManager;
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
			 * This resource involves both the Routing (for the GRIB) and the ImageProcessing (for the faxes) services.
			 * This is why it is here. It may go somewhere else in the future...
			 */
			new Operation(
					"GET",
					"/ww/composite-hierarchy", // QS Prm: filter
					this::getCompositeHierarchy,
					"Retrieve the list of the composites already available on the file system")
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
	public Response processRequest(@Nonnull Request request) throws UnsupportedOperationException {
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

	private Response getOperationList(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.navRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private Response getCompositeHierarchy(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		String filter = (qs == null ? null : qs.get("filter")); // Filter on the COMPOSITE name.
		try {
			// compositeHierarchy is still ordered.
			Map<String, Object> compositeHierarchy = new CompositeCrawler().getCompositeHierarchy(filter);
			String content = new Gson().toJson(compositeHierarchy);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("COMP-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;

		}
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request
	 * @return
	 */
	private Response emptyOperation(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
