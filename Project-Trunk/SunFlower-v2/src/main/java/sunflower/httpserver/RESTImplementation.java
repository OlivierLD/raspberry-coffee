package sunflower.httpserver;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import sunflower.SunFlowerDriver;
// import utils.StaticUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	// That one below allows the system variable (sun.flower.verbose) to be modified during the program execution
	// TODO Do this everywhere
	private final static Supplier<Boolean> verbose = () -> "true".equals(System.getProperty("sun.flower.verbose", "false"));
	private final static String SF_PREFIX = "/sf";

	private final FeatureRequestManager featureRequestManager; // Will hold the data cache
	private SunFlowerDriver featureManager = null;

	private static class ValueHolder {
		private double value;

		public ValueHolder() {
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public ValueHolder value(double value) {
			this.value = value;
			return this;
		}
	}

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
	private final List<Operation> operations = Arrays.asList(
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
					"Get the device (only) status (with azimuth and elevation offsets)"),
			new Operation(
					"POST",
					SF_PREFIX + "/azimuth-offset",
					this::setAzimuthOffset,
					"Set the azimuth offset ('value' as QueryString parameter)"),
			new Operation(
					"POST",
					SF_PREFIX + "/elevation-offset",
					this::setElevationOffset,
					"Set the elevation offset ('value' as QueryString parameter)"),
			new Operation(
					"POST",
					SF_PREFIX + "/device-heading",
					this::setDeviceHeading,
					"Set the device heading ('value' as QueryString parameter)"),
			new Operation(
					"GET",
					SF_PREFIX + "/test-oled",
					this::testOledScreen,
					"Display the QS 'value' prm on the OLED (if available)"),
			new Operation(
					"POST",
					SF_PREFIX + "/set-system-prop",
					this::setSystemProperty,
					"Set a system property. Warning: works only if explicitly read each time.")
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
		if (verbose.get()) {
			System.out.printf("ProcessRequest (%s): From: %s, looking for %s %s%n",
					this.getClass().getName(),
					(request.getHeaders() != null ? request.getHeaders().get("User-Agent") : "-?-"),
					request.getVerb(),
					request.getPath());
		}
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
			System.out.printf(">> OP not found: %s %s %n", request.getVerb(), request.getPath());
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

	private Response getDeviceStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters(); // If needed...

		try {
			Map<String, Object> serviceData = this.featureRequestManager.getDataCache(); // TODO Tweak this...
			// Clone to avoid concurrentAccessException
			@SuppressWarnings("unchecked")
			final Map<String, Object> _serviceData = (Map<String, Object>)((HashMap<String, Object>)serviceData).clone(); // shallow copy
//			@SuppressWarnings("unchecked")
//			final Map<String, Object> _serviceData = (Map<String, Object>)StaticUtil.deepCopy(serviceData);
			String content = "";
			try {
				content = new Gson().toJson(_serviceData);
			} catch (IllegalArgumentException iae) {
				System.out.printf("Device status failed (but moving on), serviceData: %s%n", _serviceData);
				content = "Device not initialized yet.";
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			System.err.println(">> Managed Exception SUN_FLOWER-0001:");
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
			System.err.println(">> Managed Exception SUN_FLOWER-0002:");
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
			System.err.println(">> Managed Exception SUN_FLOWER-0003:");
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0003")
							.errorMessage(ex1.toString()));
			return response;
		}
	}

	private Response setAzimuthOffset(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		List<String> pathParameters = request.getPathParameters(); // Not needed...
		// Azimuth in the query, as 'value'
		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		if (queryStringParameters == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0005")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		}
		Optional<String> value = queryStringParameters
				.keySet()
				.stream()
				.filter("value"::equals)
				.map(key -> queryStringParameters.get(key))
				.findFirst();
		if (!value.isPresent()) { // .isEmpty not available in JDK 8
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0005")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		} else {
			String val = value.get();
			double offsetValue = 0;
			try {
				offsetValue = Double.parseDouble(val);
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0006")
								.errorMessage(nfe.toString()));
				return response;
			}
			try {
				this.featureManager.setAzimuthOffset(offsetValue);
				ValueHolder valueHolder = new ValueHolder().value(offsetValue);
				String content = new Gson().toJson(valueHolder);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			} catch (Exception ex1) {
				System.err.println(">> Managed Exception SUN_FLOWER-0004:");
				ex1.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0004")
								.errorMessage(ex1.toString()));
				return response;
			}
		}
	}

	private Response setElevationOffset(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		List<String> pathParameters = request.getPathParameters(); // Not needed...
		// Elevation in the query, as 'value'
		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		if (queryStringParameters == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0008")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		}
		Optional<String> value = queryStringParameters
				.keySet()
				.stream()
				.filter(key -> "value".equals(key))
				.map(key -> queryStringParameters.get(key))
				.findFirst();
		if (!value.isPresent()) { // .isEmpty not available in JDK 8
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0008")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		} else {
			String val = value.get();
			double offsetValue = 0;
			try {
				offsetValue = Double.parseDouble(val);
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0009")
								.errorMessage(nfe.toString()));
				return response;
			}
			try {
				this.featureManager.setElevationOffset(offsetValue);
				ValueHolder valueHolder = new ValueHolder().value(offsetValue);
				String content = new Gson().toJson(valueHolder);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			} catch (Exception ex1) {
				System.err.println(">> Managed Exception SUN_FLOWER-0007:");
				ex1.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0007")
								.errorMessage(ex1.toString()));
				return response;
			}
		}
	}

	private Response setDeviceHeading(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		List<String> pathParameters = request.getPathParameters(); // Not needed...
		// heading in the query, as 'value'
		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		if (queryStringParameters == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0011")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		}
		Optional<String> value = queryStringParameters
				.keySet()
				.stream()
				.filter(key -> "value".equals(key))
				.map(key -> queryStringParameters.get(key))
				.findFirst();
		if (!value.isPresent()) { // .isEmpty not available in JDK 8
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SUN_FLOWER-0011")
							.errorMessage("Query String prm 'value' is missing"));
			return response;
		} else {
			String val = value.get();
			double heading = 0;
			try {
				heading = Double.parseDouble(val);
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0012")
								.errorMessage(nfe.toString()));
				return response;
			}
			try {
				this.featureManager.setDeviceHeading(heading);
				ValueHolder valueHolder = new ValueHolder().value(heading);
				String content = new Gson().toJson(valueHolder);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			} catch (Exception ex1) {
				System.err.println(">> Managed Exception SUN_FLOWER-0010:");
				ex1.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SUN_FLOWER-0010")
								.errorMessage(ex1.toString()));
				return response;
			}
		}
	}

	private Response testOledScreen(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		String testString = "TEST";
		if (queryStringParameters != null) {
			String value = queryStringParameters.get("value");
			if (value != null) {
				testString = value;
			}
		}
		// Display
		try {
			System.out.printf("In %s, testing oled with %s %n", this.getClass().getName(), testString);
			this.featureManager.testOled(testString);
			response.setPayload(testString.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(Response.BAD_REQUEST);
			response.setPayload(ex.getMessage().getBytes());
		}
		return response;
	}

	private Response setSystemProperty(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		if (queryStringParameters != null) {
			String key = queryStringParameters.get("key");
			String value = queryStringParameters.get("value");
			if (key != null && value != null) {
				System.setProperty(key, value);  // Warning: will work if value is explicitly read each time (not at the beginning, as a final...).
				response.setPayload(String.format("{ key: '%s', value: '%s' }", key, value).getBytes());
			} else {
				response.setStatus(Response.BAD_REQUEST);
				response.setPayload("requires 'key' and 'value' QS parameters".getBytes());
			}
		} else {
			response.setStatus(Response.BAD_REQUEST);
			response.setPayload("requires 'key' and 'value' QS parameters".getBytes());
		}
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request Request
	 * @return Response
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
