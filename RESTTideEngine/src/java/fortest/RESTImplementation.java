package fortest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import tideengine.TideStation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
									"List of all available operations."),
					new Operation(
									"GET",
									"/tide-stations",
									this::getStationsList,
									"Get Tide Stations list. Returns an array of Strings containing the Station full names"),
					new Operation(
							"GET",
							"/tide-stations/{st-regex}",
							this::getStations,
							"Get Tide Stations matching the regex. Returns all data of the matching stations"));

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
			List<String> stationNames = this.one.getStationList().
					stream()
					.map(ts -> ts.getFullName())
					.collect(Collectors.toList());
			String content = new Gson().toJson(stationNames);
//		System.out.println(String.format("Length:%d,\n%s", content.length(), content));
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private Response getStations(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		final Pattern pattern;
		if (prmValues.size() == 1) {
			String nameRegex = prmValues.get(0);
			try {
				pattern = Pattern.compile(String.format(".*%s.*", URLDecoder.decode(nameRegex, "UTF-8"))); // decode/unescape
			} catch (UnsupportedEncodingException uee) {
				throw new RuntimeException(uee);
			}
		} else {
			throw new RuntimeException("Need one path parameter {regex}.");
		}
		try {
			List<TideStation> ts = this.one.getStationList().
					stream()
					.filter(station -> pattern.matcher(station.getFullName()).matches()) // TODO IgnoreCase?
					.collect(Collectors.toList());
			String content = new Gson().toJson(ts);
//		System.out.println(String.format("Length:%d,\n%s", content.length(), content));
			RESTProcessorUtil.generateHappyResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
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
