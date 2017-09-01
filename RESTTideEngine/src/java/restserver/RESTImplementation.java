package restserver;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
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

	private TideServer tideServer;

	private static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public RESTImplementation(TideServer ts) {

		this.tideServer = ts;
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
					"Get Tide Stations matching the regex. Returns all data of the matching stations. Regex might need encoding/escaping."),
			new Operation(
					"GET",
					"/tide-stations/{station-name}/wh",
					this::getWaterHeight,
					"Get Water Height for the station. Requires 2 query params: from, and to, in Duration format. Station Name might need encoding/escaping."));

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
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private Response getStationsList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			List<String> stationNames = this.tideServer.getStationList().
					stream()
					.map(ts -> ts.getFullName())
					.collect(Collectors.toList());
			String content = new Gson().toJson(stationNames);
//		System.out.println(String.format("Length:%d,\n%s", content.length(), content));
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(Response.BAD_REQUEST);
			response.setPayload(ex.toString().getBytes());
			return response;
		}
	}

	private Response getWaterHeight(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<String> prmValues = RESTProcessorUtil.getPrmValues(request.getRequestPattern(), request.getPath());
		String stationFullName = "";
		Calendar calFrom = null, calTo = null;
		boolean proceed = true;
		if (prmValues.size() == 1) {
			String param = prmValues.get(0);
			try {
				stationFullName = URLDecoder.decode(param, "UTF-8"); // decode/unescape
			} catch (UnsupportedEncodingException uee) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("TIDE-0001")
								.errorMessage(uee.toString()));
				proceed = false;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0002")
							.errorMessage("Need tideServer path parameter {station-name}."));
			proceed = false;
		}
		if (proceed) {
			Map<String, String> prms = request.getQueryStringParameters();
			if (prms == null || prms.get("from") == null || prms.get("to") == null) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("TIDE-0003")
								.errorMessage("Query parameters 'from' and 'to' are required."));
				proceed = false;
			} else {
				String from = prms.get("from");
				String to = prms.get("to");
				try {
					Date fromDate = DURATION_FMT.parse(from);
					Date toDate = DURATION_FMT.parse(to);
					calFrom = Calendar.getInstance();
					calFrom.setTime(fromDate);
					calTo = Calendar.getInstance();
					calTo.setTime(toDate);
				} catch (ParseException pe) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("TIDE-0004")
									.errorMessage(pe.toString()));
					proceed = false;
				}
			}
			if (proceed) {
				final String stationName = stationFullName;
				try {
					TideStation ts = null;
					Optional<TideStation> optTs = this.tideServer.getStationList().
							stream()
							.filter(station -> stationName.equals(station.getFullName()))
							.findFirst();
					if (!optTs.isPresent()) {
						response = HTTPServer.buildErrorResponse(response,
								Response.NOT_FOUND,
								new HTTPServer.ErrorPayload()
										.errorCode("TIDE-0005")
										.errorMessage(String.format("Station [%s] not found", stationName)));
						proceed = false;
					} else {
						ts = optTs.get();
					}
					if (proceed) {
						// Calculate water height, from-to;
						TideTable tideTable = new TideTable();
						tideTable.stationName = stationName;
						tideTable.baseHeight = ts.getBaseHeight();
						tideTable.unit = ts.getDisplayUnit();
						Map<String, Double> map = new LinkedHashMap<>();

						Calendar now = calFrom;
						ts = BackEndTideComputer.findTideStation(stationName, now.get(Calendar.YEAR));
						if (ts != null) {
//            TimeZone tz = TimeZone.getDefault();
							now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
							while (now.before(calTo)) {
								double wh = TideUtilities.getWaterHeight(ts, this.tideServer.getConstSpeed(), now);
//							TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
								TimeZone.setDefault(TimeZone.getTimeZone(ts.getTimeZone())); // for TS Timezone display
//							System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + stationName + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
//              TimeZone.setDefault(tz);
								map.put(now.getTime().toString(), wh);
								now.add(Calendar.MINUTE, 5);
							}
						}
						tideTable.heights = map;

						String content = new Gson().toJson(tideTable);
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
						return response;
					}
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("TIDE-0006")
									.errorMessage(ex.toString()));
				}
			}
		}
		return response; // If we reach here, something went wrong, it's a BAD_REQUEST or so.
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
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("TIDE-0007")
								.errorMessage(uee.toString()));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0008")
							.errorMessage("Need tideServer path parameter {regex}."));
			return response;
		}
		try {
			List<TideStation> ts = this.tideServer.getStationList().
					stream()
					.filter(station -> pattern.matcher(station.getFullName()).matches()) // TODO IgnoreCase?
					.collect(Collectors.toList());
			String content = new Gson().toJson(ts);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0008")
							.errorMessage(ex.toString()));
			return response;
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

	private static class TideTable {
		String stationName;
		double baseHeight;
		String unit;
		Map<String, Double> heights;
	}
}
