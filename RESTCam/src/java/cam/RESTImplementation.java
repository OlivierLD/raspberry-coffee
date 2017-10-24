package cam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import implementation.cam.CameraManager;

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

	private CamRequestManager camRequestManager;


	public RESTImplementation(CamRequestManager camRequestManager) {
		this.camRequestManager = camRequestManager;
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
					"/cam/oplist",
					this::getOperationList,
					"List of all available operations, on cam request manager."),
			new Operation( // QueryString contains date /sun-moon-gp?at=2017-09-01T00:00:00
					"POST",
					"/cam/snap",
					this::takeSnap,
					"Takes a snapshot.")
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

		List<Operation> opList = this.getOperations(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}


	/**
	 * Take a snap
	 * @param request QS prms: 'rot', 'height', 'width', 'name'
	 * @return
	 */
	private Response takeSnap(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> prms = request.getQueryStringParameters();
		// rot, width, height, name.
		int rot = 180;
		int width = 640;
		int height = 480;
		String name = "-snap";
		if (prms.get("rot") != null) {
			try {
				rot = Integer.parseInt(prms.get("rot"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0001")
								.errorMessage(String.format("Bad 'rot' parameter [%s], must be an integer", prms.get("rot"))));
				return response;
			}
		}
		if (prms.get("width") != null) {
			try {
				width = Integer.parseInt(prms.get("width"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0002")
								.errorMessage(String.format("Bad 'width' parameter [%s], must be an integer", prms.get("width"))));
				return response;
			}
		}
		if (prms.get("height") != null) {
			try {
				height = Integer.parseInt(prms.get("height"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0003")
								.errorMessage(String.format("Bad 'height' parameter [%s], must be an integer", prms.get("height"))));
				return response;
			}
		}
		if (prms.get("name") != null) {
			name = prms.get("name");
		}
		CameraManager.snap(name, rot, width, height);

		String content = "{ \"status\": \"Ok\" }"; // new Gson().toJson(data);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}


}
