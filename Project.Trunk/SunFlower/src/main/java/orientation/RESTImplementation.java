package orientation;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

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

	private SunFlower sunFlower;

	private final static String SUN_FLOWER_PREFIX = "/sun-flower";

	public RESTImplementation() {
		this(null);
	}
	public RESTImplementation(SunFlower sf) {

		if (sf != null) {
			this.sunFlower = sf;
		} else { // Most probably no servos, started for Sun data
			SunFlower sunFlower = new SunFlower(null, null);
			String strLat = System.getProperty("default.sf.latitude");
			if (strLat != null) {
				try {
					sunFlower.setLatitude(Double.parseDouble(strLat));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.exit(1);
				}
			}
			String strLong = System.getProperty("default.sf.longitude");
			if (strLong != null) {
				try {
					sunFlower.setLongitude(Double.parseDouble(strLong));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					System.exit(1);
				}
			}
			SunFlower.setWithAdc(false);
			this.sunFlower = sunFlower;
			this.sunFlower.startWorking();
		}

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
					SUN_FLOWER_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations."),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/position",
					this::getPosition,
					"Get device position on Earth."),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/device-heading",
					this::getDeviceHeading,
					"Get device heading."),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/servo-values",
					this::getServoValues,
					"Get servos values"),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/dates",
					this::getDates,
					"Get the dates (System, UTC, Solar"),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/sun-data",
					this::getSunData,
					"Get the computed Sun data"),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/battery-data",
					this::getBatteryData,
					"Get the LiPo battery data (voltage)"),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/photo-cell-data",
					this::getPhotoCellData,
					"Get the photo-cell data (adc [0..1023])"),
			new Operation(
					"GET",
					SUN_FLOWER_PREFIX + "/all",
					this::getAll,
					"Get everything!"));

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

	private Response getPosition(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		SunFlower.GeographicPosition pos = sunFlower.getPosition();
		String content = new Gson().toJson(pos);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getDeviceHeading(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		double heading = sunFlower.getDeviceHeading();
		String content = new Gson().toJson(heading);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getServoValues(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		SunFlower.ServoValues servoValues = sunFlower.getServoValues();
		String content = new Gson().toJson(servoValues);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getDates(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		SunFlower.Dates dates = sunFlower.getDates();
		String content = new Gson().toJson(dates);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getSunData(Request request)  {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		SunFlower.SunData sunData = sunFlower.getSunData();

		String content = new Gson().toJson(sunData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getAll(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		SunFlower.AllData allData = sunFlower.getAllData();

		String content = new Gson().toJson(allData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getBatteryData(Request request)  {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		SunFlower.BatteryData batteryData = sunFlower.getBatteryData();

		String content = new Gson().toJson(batteryData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getPhotoCellData(Request request)  {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		SunFlower.PhotocellData photoCellData = sunFlower.getPhotocellData();

		String content = new Gson().toJson(photoCellData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Operation[] channelArray = operations.stream()
						.collect(Collectors.toList())
						.toArray(new Operation[operations.size()]);
		String content = new Gson().toJson(channelArray);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 * @param request
	 * @return
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);

		return response;
	}
}
