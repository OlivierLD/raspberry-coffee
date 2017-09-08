package astrorest;

import calc.GeoPoint;
import calculation.AstroComputer;
import calculation.SightReductionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
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

	private AstroRequestManager astroRequestManager;
	private static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


	public RESTImplementation(AstroRequestManager astroRequestManager) {
		this.astroRequestManager = astroRequestManager;
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
//			new Operation(
//					"GET",
//					"/oplist",
//					this::getOperationList,
//					"List of all available operations."),

			new Operation( // Payload like { latitude: 37.76661945, longitude: -122.5166988 } , Ocean Beach
					"POST",
					"/sun-now",
					this::getSunDataNow,
					"Create a request for Sun data now. Requires body payload (GeoPoint)"),
			new Operation( // Payload like { latitude: 37.76661945, longitude: -122.5166988 } , Ocean Beach. POST /sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
					"POST",
					"/sun-between-dates",
					this::getSunDataBetween,
					"Create a request for Sun data between 2 dates. Requires body payload (GeoPoint), and 3 queryString prm : from and to, in DURATION Format, and tz, the timezone name."),
			new Operation( // Example: GET /utc?tz=America%2FNome,America%2FNew_York,Europe%2FParis,Pacific%2FMarquesas
					"GET",
					"/utc",
					this::getCurrentTime,
					"Get current UTC Date. Will return UTC time, system time, and optionally, the time(s) at the time zone(s) passed in QS prm 'tz', UTF-8 encoded, comma separated.")
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

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z z");

	private Response getCurrentTime(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		long now = System.currentTimeMillis();
		Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		calNow.setTimeInMillis(now);
		SDF.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		String utc = SDF.format(calNow.getTime());
		SDF.setTimeZone(TimeZone.getDefault());
		String sys = SDF.format(calNow.getTime());
		List<String> others = null;

		Map<String, String> queryStringParameters = request.getQueryStringParameters();
		if (queryStringParameters != null && queryStringParameters.get("tz") != null) {
			List<String> tzs = Arrays.asList(queryStringParameters.get("tz").split(","))
					.stream()
					.map(tz -> {
						try {
							return URLDecoder.decode(tz, "UTF-8");
						} catch (UnsupportedEncodingException uee) {
							return tz;
						}
					})
					.collect(Collectors.toList());
			List<String> errors = new ArrayList<>();
			tzs.stream() // Validation
					.forEach(tz -> {
						if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(tz)) {
							errors.add(tz);
						}
					});
			if (errors.size() > 0) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0001")
								.errorMessage(String.format("Invalid time zone(s): [%s]", errors.stream().collect(Collectors.joining(", ")))));
				return response;
			} else {
				final List<String> extras = new ArrayList<>();
				tzs.forEach(tz -> {
					SDF.setTimeZone(TimeZone.getTimeZone(tz));
					extras.add(SDF.format(calNow.getTime()));
				});
				SDF.setTimeZone(TimeZone.getDefault());
				others = extras;
			}
		}

		DateHolder dateHolder = new DateHolder()
				.epoch(now)
				.utcStr(utc)
				.sysStr(sys);
		if (others != null) {
			dateHolder.others(others);
		}
		String content = new Gson().toJson(dateHolder);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 *
	 * @param request MUST contain a GeoPoint payload (observer's position)
	 * @return
	 */
	private Response getSunDataNow(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		GeoPoint pos = null;

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					pos = gson.fromJson(stringReader, GeoPoint.class);
					System.out.println();
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0004")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		}
		BodyDataForPos sunData = getSunData(pos.getL(), pos.getG());
		String content = new Gson().toJson(sunData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 *
	 * @param request MUST contain a GeoPoint payload (observer's position), and query String prms from (duration format), to (duration format), and tz (timezone name).
	 * @return
	 */
	private Response getSunDataBetween(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		GeoPoint pos = null;

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					pos = gson.fromJson(stringReader, GeoPoint.class);
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0002")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		}
		String fromPrm = null, toPrm = null;
		String tzName = null;
		Map<String, String> prms = request.getQueryStringParameters();
		if (prms == null || prms.get("from") == null || prms.get("to") == null || prms.get("tz") == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0003")
							.errorMessage("Query parameters 'tz', 'from' and 'to' are required."));
			return response;
		} else {
			fromPrm = prms.get("from");
			toPrm = prms.get("to");
			tzName = prms.get("tz");
			try {
				tzName = URLDecoder.decode(tzName, "UTF-8");
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0006")
								.errorMessage(ex.toString()));
				return response;
			}
			if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(tzName)) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0005")
								.errorMessage(String.format("Invalid TimeZone: %s", tzName)));
				return response;
			}
		}

		Map<Long, BodyDataForPos> map = new LinkedHashMap<>();
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone(tzName));
		try {
			Date from = DURATION_FMT.parse(fromPrm);
			Date to = DURATION_FMT.parse(toPrm);
			Calendar toCal = Calendar.getInstance(TimeZone.getTimeZone(tzName));
			toCal.setTime(to);
			Calendar current = Calendar.getInstance(TimeZone.getTimeZone(tzName));
			current.setTime(from);
			do {
				BodyDataForPos data = getSunDataForDate(pos.getL(), pos.getG(), current);
				map.put(current.getTimeInMillis(), data);
				current.add(Calendar.DATE , 1);
			} while (current.before(toCal));
			String content = new Gson().toJson(map);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0006")
							.errorMessage(ex.toString()));
			return response;
		}
	}

	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		return response;
	}

	public static class DateHolder {
		long epoch;
		String utcStr;
		String sysStr;
		List<String> others;

		public DateHolder epoch(long epoch) {
			this.epoch = epoch;
			return this;
		}
		public DateHolder utcStr(String utcStr) {
			this.utcStr = utcStr;
			return this;
		}
		public DateHolder sysStr(String sysStr) {
			this.sysStr = sysStr;
			return this;
		}
		public DateHolder others(List<String> others) {
			this.others = others;
			return this;
		}
	}

	public static class BodyDataForPos {
		long epoch;
		double lat;
		double lng;
		String body;
		double decl;
		double gha;
		double altitude;
		double z;
		double eot;
		double riseTime;
		double setTime;
		double riseZ;
		double setZ;

		public BodyDataForPos(long epoch, double lat, double lng, String body) {
			this.epoch = epoch;
			this.lat = lat;
			this.lng = lng;
			this.body = body;
		}

		public BodyDataForPos altitude(double alititude) {
			this.altitude = alititude;
			return this;
		}
		public BodyDataForPos decl(double decl) {
			this.decl = decl;
			return this;
		}
		public BodyDataForPos gha(double gha) {
			this.gha = gha;
			return this;
		}
		public BodyDataForPos z(double z) {
			this.z = z;
			return this;
		}
		public BodyDataForPos eot(double eot) {
			this.eot = eot;
			return this;
		}
		public BodyDataForPos riseTime(double riseTime) {
			this.riseTime = riseTime;
			return this;
		}
		public BodyDataForPos setTime(double setTime) {
			this.setTime = setTime;
			return this;
		}
		public BodyDataForPos riseZ(double riseZ) {
			this.riseZ = riseZ;
			return this;
		}
		public BodyDataForPos setZ(double setZ) {
			this.setZ = setZ;
			return this;
		}
	}

	private BodyDataForPos getSunData(double lat, double lng) {
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		return getSunDataForDate(lat, lng, current);
	}

	private BodyDataForPos getSunDataForDate(double lat, double lng, Calendar current) {
		AstroComputer.setDateTime(current.get(Calendar.YEAR),
				current.get(Calendar.MONTH) + 1,
				current.get(Calendar.DAY_OF_MONTH),
				current.get(Calendar.HOUR_OF_DAY),
				current.get(Calendar.MINUTE),
				current.get(Calendar.SECOND));
		AstroComputer.calculate();
		SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
				AstroComputer.getSunDecl(),
				lat,
				lng);
		sru.calculate();
		double he = sru.getHe().doubleValue();
		double z = sru.getZ().doubleValue();
		double sunDecl = AstroComputer.getSunDecl();
		double sunGHA = AstroComputer.getSunGHA();

		double[] sunRiseAndSet = AstroComputer.sunRiseAndSet(lat, lng);
		// Get Equation of time, used to calculate solar time.
		double eot = AstroComputer.getSunMeridianPassageTime(lat, lng); // in decimal hours

		return new BodyDataForPos(current.getTimeInMillis(), lat, lng, "Sun")
				.decl(sunDecl)
				.gha(sunGHA)
				.altitude(he)
				.z(z)
				.eot(eot)
				.riseTime(sunRiseAndSet[AstroComputer.UTC_RISE_IDX])
				.setTime(sunRiseAndSet[AstroComputer.UTC_SET_IDX])
				.riseZ(sunRiseAndSet[AstroComputer.RISE_Z_IDX])
				.setZ(sunRiseAndSet[AstroComputer.SET_Z_IDX]);
	}
}
