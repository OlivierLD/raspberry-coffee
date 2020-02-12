package sunflower.httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import sunflower.SunFlowerDriver;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	private static boolean verbose = "true".equals(System.getProperty("sun.flower.verbose", "false"));
	private final static String SF_PREFIX = "/sf";

	private FeatureRequestManager featureRequestManager; // Will hold the data cache
	private SunFlowerDriver featureManager = null;

	public RESTImplementation(FeatureRequestManager restRequestManager) {

		this.featureRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	public void setFeatureManager(SunFlowerDriver sfManager) {
		this.featureManager = sfManager;
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
					SF_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the SunFlower service."),
			new Operation(
					"GET",
					SF_PREFIX + "/status",
					this::getDeviceStatus,
					"Get the device status"),
			new Operation(
					"GET",
					SF_PREFIX + "/sun-data",
					this::getSunData,
					"Get the Sun data"),
			new Operation(
					"GET",
					SF_PREFIX + "/device-data",
					this::getDeviceData,
					"Get the device (only) status")
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
	 * WiP
	 * @param request
	 * @return
	 */
	private Response getDeviceStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters(); // If needed...

		try {
			Map<String, Object> serviceData = this.featureRequestManager.getDataCache(); // TODO Tweak this
			String content = new Gson().toJson(serviceData);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0001")
							.errorMessage(ex1.toString()));
			return response;
		}
	}

	private Response getDeviceData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters(); // If needed...

		try {
			Object serviceData = this.featureRequestManager.getDataCache().get(SunFlowerDriver.EventType.DEVICE_DATA.toString());

			String content = new Gson().toJson(serviceData);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0002")
							.errorMessage(ex1.toString()));
			return response;
		}
	}

	private Response getSunData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters(); // If needed...

		try {
			Object serviceData = this.featureRequestManager.getDataCache().get(SunFlowerDriver.EventType.CELESTIAL_DATA.toString());
			String content = new Gson().toJson(serviceData);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0003")
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
}
