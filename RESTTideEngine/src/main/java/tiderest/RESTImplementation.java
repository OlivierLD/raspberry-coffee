package tiderest;

import calc.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import tideengine.*;
import tideengine.publisher.TidePublisher;

import javax.annotation.Nonnull;
import java.io.StringReader;
import java.net.URLDecoder;
import java.text.NumberFormat;
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
 * Those operation mostly retrieve the state of the TideServer class, and device.
 * <br>
 * The TideServer will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private TideRequestManager tideRequestManager;

	private final static String TIDE_PREFIX = "/tide";

	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final static SimpleDateFormat TZ_ABR = new SimpleDateFormat("z");
	private final static SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");
	private final static SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");

	public RESTImplementation(@Nonnull TideRequestManager restRequestManager) {

		this.tideRequestManager = restRequestManager;
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
					TIDE_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations, on tide request manager."),
			new Operation(
					"GET",
					TIDE_PREFIX + "/tide-stations",
					this::getStationsList,
					"Get Tide Stations list. Returns an array of Strings containing the Station full names. Paginable, supports 'filter', 'limit' and 'offset' optional query string parameters. Default offset is 0, default limit is 500."),
			new Operation(
					"GET",
					TIDE_PREFIX + "/coeff-definitions",
					this::getCoefficients,
					"Get all the coefficient names and definitions"),
			new Operation(
					"GET",
					TIDE_PREFIX + "/coeff-definitions/{coeff-name}",
					this::getCoefficient,
					"Get one coefficient name and definition. {coeff-name} is the name. Returns 'unknown' if not found in the map."),
			new Operation(
					"GET",
					TIDE_PREFIX + "/tide-stations/{st-regex}",
					this::getStations,
					"Get Tide Stations matching the regex. Returns all data of the matching stations. Regex might need encoding/escaping."),
			new Operation(
					"POST",
					TIDE_PREFIX + "/tide-stations/{station-name}/wh",
					this::getWaterHeight,
					"Creates a Water Height request for the {station}. Requires 2 query params: from, and to, in Duration format. Station Name might need encoding/escaping. Can also take a json body payload."),
			new Operation(
					"POST",
					TIDE_PREFIX + "/publish/{station-name}",
					this::publishTideTable,
					"Generates tide table (or agenda, if query parameter 'agenda' is set to 'y') document (pdf)"),
			new Operation(
					"POST",
					TIDE_PREFIX + "/publish/{station-name}/moon-cal",
					this::publishMoonCalendar,
					"Generates moon calendar document (pdf), for one year"),
			new Operation(
					"POST",
					TIDE_PREFIX + "/tide-stations/{station-name}/wh/details",
					this::getWaterHeightPlus,
					"Creates a Water Height request for the {station}, with harmonic curves. Requires 2 query params: from, and to, in Duration format. Station Name might need encoding/escaping. Can also take a json body payload."));

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

		List<Operation> opList = this.getOperations();
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * Returns the coefficient names and definitions
	 *
	 * @param request
	 * @return
	 */
	private Response getCoefficients(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String content = new Gson().toJson(this.tideRequestManager.getCoeffDefinitions());
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

	/**
	 * Returns the coefficient name and definition, just for one.
	 *
	 * @param request
	 * @return
	 */
	private Response getCoefficient(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<String> prmValues = request.getPathParameters(); // RESTProcessorUtil.getPathPrmValues(request.getRequestPattern(), request.getPath());
		String coeffName = "";
		if (prmValues.size() == 1) {
			String param = prmValues.get(0);
			coeffName = param;
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0001")
							.errorMessage("Need tideRequestManager path parameter {coeff-name}."));
			return response;
		}
		try {
			String definition = this.tideRequestManager.getCoeffDefinitions().get(coeffName);
			String content = new Gson().toJson(new NameValuePair<String>().name(coeffName).value(definition == null ? "unknown" : definition));
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

	/**
	 * Accepts limit and offset Query String parameters. Optional.
	 *
	 * @param request
	 * @return Encoded list (UTF-8)
	 */
	private Response getStationsList(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		long offset = 0;
		long limit = 500;
		final Pattern pattern;
		Map<String, String> qsPrms = request.getQueryStringParameters();
		if (qsPrms != null && qsPrms.containsKey("offset")) {
			try {
				offset = Long.parseLong(qsPrms.get("offset"));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if (qsPrms != null && qsPrms.containsKey("limit")) {
			try {
				limit = Long.parseLong(qsPrms.get("limit"));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		if (qsPrms != null && qsPrms.containsKey("filter")) {
			String filter = qsPrms.get("filter");
			pattern = Pattern.compile(String.format(".*%s.*", filter)); // decode/unescape
		} else {
			pattern = null;
		}
		try {
			List<String> stationNames = this.tideRequestManager.getStationList()
					.stream()
					.filter(ts -> (pattern == null ? true : pattern.matcher(ts.getFullName()).matches()))
					.map(ts -> ts.getFullName())
					.skip(offset)
					.limit(limit)
					.collect(Collectors.toList());
			String content = new Gson().toJson(stationNames);
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

	/**
	 * Supports a payload in the body, in json format:
	 * <pre>
	 * {
	 *   "timezone": "Etc/UTC",
	 *   "step": 5,
	 *   "unit": "meters"|"feet"
	 * }
	 * </pre>
	 * <ul>
	 * <li>Default timezone is the timezone of the station</li>
	 * <li>Default step (in minutes) is 5</li>
	 * <li>Default unit is the unit of the station</li>
	 * </ul>
	 *
	 * @param request Requires two query string parameters <b>from</b> and <b>to</b>, in Duration format (yyyy-MM-ddThh:mm:ss)
	 * @return the expect response. Could contain an error, see the "TIDE-XXXX" messages.
	 */
	private Response getWaterHeight(@Nonnull Request request) {
		return getWaterHeightWithDetails(request, false);
	}

	private Response getWaterHeightPlus(@Nonnull Request request) {
		return getWaterHeightWithDetails(request, true);
	}

	/**
	 * Supports a payload in the body, in json format:
	 * <pre>
	 * {
	 *   "timezone": "Etc/UTC",
	 *   "step": 5,
	 *   "unit": "meters"|"feet"
	 * }
	 * </pre>
	 * <ul>
	 * <li>Default timezone is the timezone of the station</li>
	 * <li>Default step (in minutes) is 5</li>
	 * <li>Default unit is the unit of the station</li>
	 * </ul>
	 *
	 * @param request Requires two query string parameters <b>from</b> and <b>to</b>, in Duration format (yyyy-MM-ddThh:mm:ss)
	 * @param withDetails if true, also returns the curves for all harmonic coeffs.
	 * @return the expect response. Could contain an error, see the "TIDE-XXXX" messages.
	 */
	private Response getWaterHeightWithDetails(@Nonnull Request request, boolean withDetails) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK); // Happy response
		List<String> prmValues = request.getPathParameters(); // RESTProcessorUtil.getPathPrmValues(request.getRequestPattern(), request.getPath());
		String stationFullName = "";
		Calendar calFrom = null, calTo = null;
		String fromPrm = null, toPrm = null;
		boolean proceed = true;
		if (prmValues.size() == 1) {
			String param = prmValues.get(0);
			stationFullName = param;
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0002")
							.errorMessage("Need tideRequestManager path parameter {station-name}."));
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
				fromPrm = prms.get("from");
				toPrm = prms.get("to");
			}
			if (proceed) {
				final String stationName = stationFullName;
				int step = 5;
				String timeZoneToUse = null;
				unit unitToUse = null;
				// Payload in the body?
				if (request.getContent() != null && request.getContent().length > 0) {
					String payload = new String(request.getContent());
					if (!"null".equals(payload)) {
						Gson gson = new GsonBuilder().create();
						StringReader stringReader = new StringReader(payload);
						try {
							WaterHeightOptions options = gson.fromJson(stringReader, WaterHeightOptions.class);
							if (options.step == 0 &&
									options.timezone == null &&
									options.unit == null) {
								response = HTTPServer.buildErrorResponse(response,
										Response.BAD_REQUEST,
										new HTTPServer.ErrorPayload()
												.errorCode("TIDE-0011")
												.errorMessage(String.format("Invalid payload: %s", payload)));
								proceed = false;
							} else {
								if (options.step < 0) {
									response = HTTPServer.buildErrorResponse(response,
											Response.BAD_REQUEST,
											new HTTPServer.ErrorPayload()
													.errorCode("TIDE-0012")
													.errorMessage(String.format("Step MUST be positive: %d", options.step)));
									proceed = false;
								}
								if (proceed && options.timezone != null) {
									if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(options.timezone)) {
										response = HTTPServer.buildErrorResponse(response,
												Response.BAD_REQUEST,
												new HTTPServer.ErrorPayload()
														.errorCode("TIDE-0013")
														.errorMessage(String.format("Invalid TimeZone: %s", options.timezone)));
										proceed = false;
									}
								}
								if (proceed) {
									// Set overriden parameter values
									if (options.timezone != null) {
										timeZoneToUse = options.timezone;
									}
									if (options.unit != null) {
										unitToUse = options.unit;
									}
									if (options.step != 0) {
										step = options.step;
									}
								}
							}
						} catch (Exception ex) {
							response = HTTPServer.buildErrorResponse(response,
									Response.BAD_REQUEST,
									new HTTPServer.ErrorPayload()
											.errorCode("TIDE-0010")
											.errorMessage(ex.toString()));
							proceed = false;
						}
					}
				}
				if (proceed) {
					// Parameters OK, now performing the real calculation
					try {
						TideStation ts = null;
						Optional<TideStation> optTs = this.tideRequestManager.getStationList()
								.stream()
								.filter(station -> station.getFullName().equals(stationName))
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
							tideTable.baseHeight = ts.getBaseHeight() * unitSwitcher(ts, unitToUse);
							tideTable.unit = (unitToUse != null ? unitToUse.toString() : ts.getDisplayUnit());
							tideTable.timeZone = (timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone());
							tideTable.position = new GeoPoint(ts.getLatitude(), ts.getLongitude());
							tideTable.fromPrm = fromPrm;
							tideTable.toPrm = toPrm;
							Map<String, WhDate> map = new LinkedHashMap<>();

							DURATION_FMT.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
							try {
								Date fromDate = DURATION_FMT.parse(fromPrm);
								Date toDate = DURATION_FMT.parse(toPrm);
								calFrom = Calendar.getInstance();
								calFrom.setTime(fromDate);
								calTo = Calendar.getInstance();
								calTo.setTime(toDate);
								if (calTo.before(calFrom)) {
									response = HTTPServer.buildErrorResponse(response,
											Response.BAD_REQUEST,
											new HTTPServer.ErrorPayload()
													.errorCode("TIDE-0014")
													.errorMessage(String.format("Bad date chronology. %s is after %s", fromPrm, toPrm)));
									proceed = false;
								}
							} catch (ParseException pe) {
								response = HTTPServer.buildErrorResponse(response,
										Response.BAD_REQUEST,
										new HTTPServer.ErrorPayload()
												.errorCode("TIDE-0004")
												.errorMessage(pe.toString()));
								proceed = false;
							}

							if (proceed) {
								Calendar now = (Calendar)calFrom.clone();
								Calendar upTo = (Calendar)calTo.clone();

								List<TideUtilities.TimedValue> table = TideUtilities.getTideTableForOneDay(
										ts,
										this.tideRequestManager.getConstSpeed(),
										now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
										timeZoneToUse);
								tideTable.table = table;

								if ("true".equals(System.getProperty("tide.verbose", "false"))) {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z z");
									sdf.setTimeZone(TimeZone.getTimeZone((timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone())));
									try {
										System.out.println(String.format("Calculating tide in %s, from %s (%s) to %s (%s)",
												URLDecoder.decode(ts.getFullName(), "UTF-8"),
												sdf.format(now.getTime()),
												NumberFormat.getInstance().format(now.getTimeInMillis()),
												sdf.format(upTo.getTime()),
												NumberFormat.getInstance().format(upTo.getTimeInMillis())));
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}

								ts = BackEndTideComputer.findTideStation(stationFullName, now.get(Calendar.YEAR));
								if (ts != null) {
									TZ_ABR.setTimeZone(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone()));
									DATE_FMT.setTimeZone(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone()));
									TIME_FMT.setTimeZone(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone()));
									now.setTimeZone(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone()));


									while (now.before(upTo)) {
										double wh = TideUtilities.getWaterHeight(ts, this.tideRequestManager.getConstSpeed(), now);
										TimeZone.setDefault(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone())); // for TS Timezone display
	//							  System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + stationName + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
	//								map.put(now.getTime().toString(), wh * unitSwitcher(ts, unitToUse));
										Date d = now.getTime();
										map.put(String.valueOf(now.getTimeInMillis()),
												new WhDate()
														.wh(wh * unitSwitcher(ts, unitToUse))
														.tz(TZ_ABR.format(d))
														.date(DATE_FMT.format(d))
														.time(TIME_FMT.format(d)));
										now.add(Calendar.MINUTE, step);
									}
								} else {
									System.out.println("No Tide station, Wow!"); // I know...
								}
								tideTable.heights = map;
								if (withDetails) { // With harmonic curves
									final TideStation fts = ts;
									final Calendar _reference = (Calendar) calFrom.clone();
									final Calendar _calTo = (Calendar) calTo.clone();
									final String tztu = timeZoneToUse;
									final unit unit2use = unitToUse;
									final int _step = step;
									final List<Coefficient> constSpeed = this.tideRequestManager.getConstSpeed();
									_reference.setTimeZone(TimeZone.getTimeZone(timeZoneToUse != null ? timeZoneToUse : ts.getTimeZone()));
									List<Harmonic> harmonics = ts.getHarmonics()
											.stream()
											.filter(harmonic -> (harmonic.getAmplitude() != 0d && harmonic.getEpoch() != 0d))
											.collect(Collectors.toList());
									Hashtable<String, List<DataPoint>> harmonicCurves = new Hashtable<>();
									harmonics.stream()
											.forEach(harmonicCoeff -> {
												List<DataPoint> oneCurve = new ArrayList<>();
												Calendar _now = (Calendar) _reference.clone();
												_now.setTimeZone(TimeZone.getTimeZone(tztu != null ? tztu : fts.getTimeZone()));
												while (_now.before(_calTo)) {
													int year = _now.get(Calendar.YEAR);
													// Calc Jan 1st of the current year
													Date jan1st = new GregorianCalendar(year, 0, 1).getTime();
													//      double value = Utils.convert(TideUtilities.getHarmonicValue(cal.getTime(), jan1st, ts, constSpeed, i), ts.getDisplayUnit(), currentUnit);
													double value = TideUtilities.getHarmonicValue(
															_now.getTime(),
															jan1st,
															fts,
															constSpeed,
															harmonicCoeff.getName()) * unitSwitcher(fts, unit2use);

													double x = _now.getTimeInMillis(); // (h + (double) (m / 60D));
													double y = (value); // - _bottomValue);
													oneCurve.add(new DataPoint(x, y));

													_now.add(Calendar.MINUTE, _step);
												}
												harmonicCurves.put(harmonicCoeff.getName(), oneCurve);
											});
									tideTable.harmonicCurves = harmonicCurves;
								}
								/*
								 * Happy End
								 */
								String content = new Gson().toJson(tideTable);
								RESTProcessorUtil.generateResponseHeaders(response, content.length());
								response.setPayload(content.getBytes());
								return response;
							}
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
		}
		return response; // If we reach here, something went wrong, it's a BAD_REQUEST or so.
	}

	private Response getStations(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<String> prmValues = request.getPathParameters(); // RESTProcessorUtil.getPathPrmValues(request.getRequestPattern(), request.getPath());
		final Pattern pattern;

		if (prmValues.size() == 1) {
			String nameRegex = prmValues.get(0);
			pattern = Pattern.compile(String.format("(?i).*%s.*", nameRegex)); // decode/unescape, ignore-case
//			System.out.println("Pattern:" + pattern.toString());
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0008")
							.errorMessage("Need tideRequestManager path parameter {regex}."));
			return response;
		}
		try {
			List<TideStation> ts = this.tideRequestManager.getStationList()
					.stream()
					.filter(station -> pattern.matcher(station.getFullName()).matches())
					.collect(Collectors.toList());
			String content = new Gson().toJson(ts);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0009")
							.errorMessage(ex.toString()));
			return response;
		}
	}

	/**
	 * Publish (generate pdf) for a station.
	 * Supports a payload in the body, in json format:
	 * <pre>
	 * {
	 *   "startMonth": 0,             // Optional, default 0
	 *   "startYear": 2017,
	 *   "nb": 1,                     // Optional, default 1
	 *   "quantity": "YEAR" | "MONTH" // Optional, default "YEAR"
	 * }
	 * </pre>
	 * @param request
	 * @return
	 */
	private Response publishMoonCalendar(@Nonnull Request request) {
		return publishTideDocument(request, TidePublisher.MOON_CALENDAR);
	}

	/**
	 * Publish (generate pdf) for a station.
	 * Supports a payload in the body, in json format:
	 * <pre>
	 * {
	 *   "startMonth": 0,
	 *   "startYear": 2017,
	 *   "nb": 1,
	 *   "quantity": "YEAR" | "MONTH"
	 * }
	 * </pre>
	 * @param request
	 * @return
	 */
	private Response publishTideTable(@Nonnull Request request) {
		Map<String, String> queryParams = request.getQueryStringParameters();
		boolean agenda = false;
		if (queryParams != null) {
			if (queryParams.containsKey("agenda") && "y".equals(queryParams.get("agenda"))) {
				agenda = true;
			}
		}
		return publishTideDocument(request, (agenda ? TidePublisher.AGENDA_TABLE : TidePublisher.TIDE_TABLE));
	}

	private Response publishTideDocument(@Nonnull Request request, String script) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<String> prmValues = request.getPathParameters(); // RESTProcessorUtil.getPathPrmValues(request.getRequestPattern(), request.getPath());
		String stationFullName = "";
		if (prmValues.size() == 1) {
			String param = prmValues.get(0);
			stationFullName = param;
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0100")
							.errorMessage("Need tideRequestManager path parameter {station-name}."));
			return response;
		}
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				String errMess = "";
				PublishingOptions options;
				try {
					options = gson.fromJson(stringReader, PublishingOptions.class);
					if (options.startMonth > 11 || options.startMonth < 0) {
						errMess += ((errMess.length() > 0 ? "\n" : "") + "Invalid month, must be in [0..11].");
					}
					if (options.nb < 1) {
						errMess += ((errMess.length() > 0 ? "\n" : "") + "Invalid number, must be at least 1 ");
					}
					if (options.quantity == null) {
						errMess += ((errMess.length() > 0 ? "\n" : "") + "Quantity must be YEAR or MONTH.");
					}
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("TIDE-0102")
									.errorMessage(errMess));
					return response;
				}
				try {
//				String unescaped = URLDecoder.decode(stationFullName, "UTF-8");
					String generatedFileName = TidePublisher.publish(
							stationFullName,
							options.startMonth,
							options.startYear,
							options.nb,
							(options.quantity.equals(Quantity.MONTH) ? Calendar.MONTH : Calendar.YEAR),
							script);
					response.setPayload(generatedFileName.getBytes());
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("TIDE-0103")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("TIDE-0101")
							.errorMessage("Required payload not found."));
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

	private static class TideTable {
		String stationName;
		double baseHeight;
		String unit;
		String timeZone;
		GeoPoint position;
		String fromPrm;
		String toPrm;
		List<TideUtilities.TimedValue> table;
		Map<String, WhDate> heights;
		Hashtable<String, List<DataPoint>> harmonicCurves;
	}

	private enum unit {
		meters, feet
	}

	private static double unitSwitcher(@Nonnull TideStation ts, unit overridden) {
		double factor = 1d;
		if (overridden != null) {
			if (!ts.getDisplayUnit().equals(overridden.toString())) {
				switch (ts.getDisplayUnit()) {
					case "feet": // feet to meters
						factor = 0.3048;
						break;
					case "meters": // meters to feet
						factor = 3.28084;
						break;
					default:
						break;
				}
			}
		}
		return factor;
	}

	private static class WaterHeightOptions {
		String timezone; // If not the Station timezone
		int step; // In minutes
		unit unit; // If not the station unit
	}

	private enum Quantity {
	  MONTH, YEAR
	};

	private static class PublishingOptions {
		int startMonth;
		int startYear;
		int nb;
		Quantity quantity;
	}

	private static class NameValuePair<T> {
		String name;
		T value;

		public NameValuePair<T> name(String name) {
			this.name = name;
			return this;
		}

		public NameValuePair<T> value(T value) {
			this.value = value;
			return this;
		}
	}

	public static class WhDate {
		double wh;
		String date;
		String time;
		String tz;

		public WhDate wh(double wh) {
			this.wh = wh;
			return this;
		}
		public WhDate date(String date) {
			this.date = date;
			return this;
		}
		public WhDate time(String time) {
			this.time = time;
			return this;
		}
		public WhDate tz(String tz) {
			this.tz = tz;
			return this;
		}

	}

	public class DataPoint {
		private double x, y;

		public DataPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}
}
