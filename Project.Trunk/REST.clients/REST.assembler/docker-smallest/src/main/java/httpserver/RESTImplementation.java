package httpserver;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import sensors.ADCChannel;

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

	private static boolean verbose = "true".equals(System.getProperty("server.verbose", "false"));
	private final static String SERVER_PREFIX = "/server";
	private final static String LIGHT_PREFIX =  "/light";

	private HttpRequestManager httpRequestManager;
	private ADCChannel physicalADCChannel = null;

	public RESTImplementation(HttpRequestManager restRequestManager) {

		this.httpRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	public void setADCChannel(ADCChannel adcChannel) {
		this.physicalADCChannel = adcChannel;
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
					SERVER_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the Relay service."),
			new Operation(
					"GET",
					LIGHT_PREFIX + "/ambient",
					this::getAmbientLight,
					"Get the ambient light in %")
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

		List<Operation> opList = this.getOperations();
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * @param request
	 * @return
	 */
	private Response getAmbientLight(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		AmbientLight al = new AmbientLight();
		// Get status here
		try {
			float ambient = this.physicalADCChannel.readChannelVolume();
			al.percent = ambient;
			String content = new Gson().toJson(al);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("LIGHT-0001")
							.errorMessage(ex1.toString()));
			return response;
		}
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

	public static class AmbientLight {
		float percent;
	}
}
