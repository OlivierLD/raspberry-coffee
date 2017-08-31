package fortest;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import tideengine.TideStation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

	private One one;

	public RESTImplementation(One sf) {

		this.one = sf;

		// Check duplicates in operation list. Barfs if duplicate is found.
		for (int i = 0; i < operations.size(); i++) {
			for (int j = i + 1; j < operations.size(); j++) {
				if (operations.get(i).getVerb().equals(operations.get(j).getVerb()) &&
								RESTProcessorUtil.pathsAreIndentical(operations.get(i).getPath(), operations.get(j).getPath())) {
					throw new RuntimeException(String.format("Duplicate entry in operations list %s %s", operations.get(i).getVerb(), operations.get(i).getPath()));
				}
			}
		}
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
									"List of all available operations."),
					new Operation(
									"GET",
									"/tide-stations",
									this::getStationsList,
									"Get Tide Stations list."));

	protected List<Operation> getOperations() {
		return  this.operations;
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
		Operation[] channelArray = operations.stream()
						.collect(Collectors.toList())
						.toArray(new Operation[operations.size()]);
		String content = new Gson().toJson(channelArray);
		RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private Response getStationsList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			List<TideStation> ts = this.one.getStationList();
			String content = new Gson().toJson(ts);
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 * @param request
	 * @return
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		return response;
	}
}
