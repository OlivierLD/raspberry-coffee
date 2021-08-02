package astrorest;

import calc.*;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;
import calc.calculation.nauticalalmanac.Star;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import implementation.almanac.AlmanacComputer;
import implementation.perpetualalmanac.Publisher;
import utils.TimeUtil;

import java.io.*;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
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

	private final AstroRequestManager astroRequestManager;
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final static DecimalFormat DF22 = new DecimalFormat("#0.00"); // ("##0'�'00'\''");

	private final static String UTC_TZ = "etc/UTC";
	private final static SimpleDateFormat UTC_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static {
		UTC_FMT.setTimeZone(TimeZone.getTimeZone(UTC_TZ));
	}

	private final static String ASTRO_PREFIX = "/astro";

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
	private final List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					ASTRO_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations, on astro request manager."),
			new Operation( // QueryString contains date /positions-in-the-sky?at=2017-09-01T00:00:00
					"GET",
					ASTRO_PREFIX + "/positions-in-the-sky",
					this::getPositionsInTheSky,
					"Get the Sun's and Moon's position (D & GHA) for an UTC date passed as QS prm named 'at', in DURATION Format. Optional: 'fromL' and 'fromG', 'wandering' (true|[false])."),
			new Operation( // Payload like { latitude: 37.76661945, longitude: -122.5166988 }
					"POST",
					ASTRO_PREFIX + "/sun-now",
					this::getSunDataNow,
					"Create a request for Sun data now. Requires body payload (GeoPoint)"),
			new Operation( // Payload like { position: { latitude: 37.76661945, longitude: -122.5166988 }, step: 10 } . POST /astro/sun-path-today
					"POST",
					ASTRO_PREFIX + "/sun-path-today",
					this::getSunPathInTheSky,
					"Create a request for Sun path today. Requires body payload (GeoPoint & step)"),
			new Operation( // See payload in the method definition
					"POST",
					ASTRO_PREFIX + "/declination",
					this::getBodyDeclination,
					"Get declination of one or more bodies between two UTC dates"),
			new Operation(  // Payload like { latitude: 37.76661945, longitude: -122.5166988 }. POST /astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
					"POST",
					ASTRO_PREFIX + "/sun-between-dates",
					this::getSunDataBetween,
					"Create a request for Sun data between 2 dates. Requires body payload (GeoPoint), and 3 queryString prm : from and to, in DURATION Format, and tz, the timezone name."),
			new Operation( // Payload like { latitude: 37.76661945, longitude: -122.5166988 }. POST /astro/sun-moon-dec-alt?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
					"POST",
					ASTRO_PREFIX + "/sun-moon-dec-alt",
					this::getSunMoonDecAlt,
					"Create a request for Sun data between 2 dates. Requires body payload (GeoPoint), and 2 to 3 queryString prm : from and to, in DURATION Format, and optional tz, the timezone name."),
			new Operation( // Example: GET /astro/utc?tz=America%2FNome,America%2FNew_York,Europe%2FParis,Pacific%2FMarquesas
					"GET",
					ASTRO_PREFIX + "/utc",
					this::getCurrentTime,
					"Get current UTC Date. Will return UTC time, system time, and optionally, the time(s) at the time zone(s) passed in QS prm 'tz', UTF-8 encoded, comma separated."),
			new Operation(
					"POST",
					ASTRO_PREFIX + "/publish/almanac",
					this::publishAlmanac,
					"Generates nautical almanac document (pdf)"),
			new Operation(
					"POST",
					ASTRO_PREFIX + "/publish/lunar",
					this::publishLunar,
					"Generates lunar distances document (pdf)"),
			new Operation(
					"POST",
					ASTRO_PREFIX + "/publish/perpetual",
					this::publishPerpetual,
					"Generates perpetual nautical almanac document (pdf)"),

			new Operation(
					"GET",
					ASTRO_PREFIX + "/sight-reduction",
					this::getSightReductionUserData,
					"Sight reduction user data sample (for development, to get the shape of the returned object)"),
			new Operation(
					"POST",
					ASTRO_PREFIX + "/sight-reduction",
					this::sightReduction,
					"Sight reduction"),
			new Operation(
					"POST",
					ASTRO_PREFIX + "/reverse-sight",
					this::reverseSightReduction,
					"Reverse Sight reduction")

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
	 * @param request MUST contain a GeoPoint payload (observer's position). If not present, will try default.mux.latitude & longitude,
	 *                an error will be returned if the position is not there, and if no default position is available.
	 * @return
	 */
	private Response getSunDataNow(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		GeoPoint pos = null;
		boolean tryDefaultPos = false;

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
									.errorCode("ASTRO-0004")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				tryDefaultPos = true;
			}
		} else {
			tryDefaultPos = true;
		}
		if (pos == null && tryDefaultPos) {
			String strLat = System.getProperty("default.mux.latitude");
			String strLng = System.getProperty("default.mux.longitude");
			if (strLat != null && strLng != null) {
				try {
					double l = Double.parseDouble(strLat);
					double g = Double.parseDouble(strLng);
					pos = new GeoPoint(l, g);
					if ("true".equals(System.getProperty("astro.verbose", "false"))) {
						System.out.println("getSunDataNow: Default position OK:" + pos.toString());
					}
				} catch (NumberFormatException nfe) {
					System.err.println("Moving on...");
					nfe.printStackTrace();
				}
			}
		}
		if (pos == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0006")
							.errorMessage("getSunDataNow: No position provided, no default position found"));
			return response;
		}

		BodyDataForPos sunData = getSunData(pos.getL(), pos.getG());
		String content = new Gson().toJson(sunData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private static class PosAndStep {
		GeoPoint position;
		Integer step;
	}

	private Response getSunPathInTheSky(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		PosAndStep pas = null;
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					pas = gson.fromJson(stringReader, PosAndStep.class);
					if (pas.position == null) {
						String strLat = System.getProperty("default.mux.latitude");
						String strLng = System.getProperty("default.mux.longitude");
						if (strLat != null && strLng != null) {
							try {
								double l = Double.parseDouble(strLat);
								double g = Double.parseDouble(strLng);
								GeoPoint defaultGp = new GeoPoint(l, g);
								pas.position = defaultGp;
								if ("true".equals(System.getProperty("astro.verbose", "false"))) {
									System.out.println("getSunPathInTheSky: Default position OK:" + defaultGp.toString());
								}
							} catch (NumberFormatException nfe) {
								System.err.println("Moving on...");
								nfe.printStackTrace();
							}
						}
					}
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
		if (pas.position == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0005")
							.errorMessage("getSunPathInTheSky: No position provided, no default position found."));
			return response;
		}
		List<BodyAt> sunPath = getSunDataForAllDay(pas.position.getL(), pas.position.getG(), pas.step);
		String content = new Gson().toJson(sunPath);
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
			if ("true".equals(System.getProperty("astro.verbose", "false"))) {
				System.out.println("Starting SunData calculation at " + current.getTime() + " (" + fromPrm + ")");
			}
			do {
				BodyDataForPos data = getSunDataForDate(pos.getL(), pos.getG(), current);
				map.put(current.getTimeInMillis(), data);
				current.add(Calendar.DATE , 1); // Add one day
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

	private static Date getSolarDate(Date utc, double eot) {
		long ms = utc.getTime();
		Date solar = new Date(ms + Math.round((12 - eot) * 3_600_000));
		return solar;
	}

	private final static SimpleDateFormat SDF_SOLAR = new SimpleDateFormat("yyyy;MM;dd;HH;mm;ss");
	static {
		SDF_SOLAR.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}

	/**
	 * Computes sun's and moon's Declination and GHA at a given (UTC) time.
	 * Also, optionally:
	 * - wandering bodies
	 * - stars
	 * @param request require query string parameters at (duration fmt), fromL, fromG, optional wandering=true|false, stars=true|false
	 * @return
	 */
	private Response getPositionsInTheSky(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		String atPrm = null;
		boolean wandering = false;  // Wandering bodies (visible ones)
		boolean stars = false;      // Selected stars (selected by me)
		Map<String, String> prms = request.getQueryStringParameters();
		if (prms == null || prms.get("at") == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0400")
							.errorMessage("Query parameters 'at' is required."));
			return response;
		} else {
			atPrm = prms.get("at");
			// Check if prms fromG and fromL are available
			if (prms.get("fromL") != null || prms.get("fromG") != null) {
				if (prms.get("fromL") == null || prms.get("fromG") == null) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0402")
									.errorMessage("Query parameters 'fromL' and 'fromG' must both be here, or none. Just one of them does not work."));
					return response;
				}
			}
			if ("true".equals(prms.get("wandering"))) {
				/*
				 * Then will also get data for
				 * - Aries (-> ecliptic)
				 * - Venus
				 * - Mars
				 * - Jupiter
				 * - Saturn
				 */
				wandering = true;
			}
			if ("true".equals(prms.get("stars"))) {
				stars = true;
			}

			DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
			PositionsInTheSky data = null;
			try {
				Date at = DURATION_FMT.parse(atPrm);
				Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
				date.setTime(at);
				if ("true".equals(System.getProperty("astro.verbose", "false"))) {
					System.out.println("Starting Sun and Moon data calculation at " + date.getTime());
				}
				// TODO Make it non-static, and synchronized
				AstroComputerV2 acv2 = new AstroComputerV2();
				acv2.calculate(
						date.get(Calendar.YEAR),
						date.get(Calendar.MONTH) + 1,
						date.get(Calendar.DAY_OF_MONTH),
						date.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						date.get(Calendar.MINUTE),
						date.get(Calendar.SECOND));

				data = new PositionsInTheSky()
						.epoch(date.getTimeInMillis())
						.deltaT(acv2.getDeltaT())
						.ghaAries(acv2.getAriesGHA())
						.sun(new AstroComputerV2.GP().gha(acv2.getSunGHA())
								.decl(acv2.getSunDecl()))
						.moon(new AstroComputerV2.GP().gha(acv2.getMoonGHA())
								.decl(acv2.getMoonDecl()))
						.moonPhase(acv2.getMoonPhase());

				double lat = 0d, lng = 0d;
				if (prms.get("fromL") != null && prms.get("fromG") != null) {
					try {
						lat = Double.parseDouble(prms.get("fromL"));
						lng = Double.parseDouble(prms.get("fromG"));
					} catch (NumberFormatException nfe) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0403")
										.errorMessage(String.format("Invalid Query parameters 'fromL' and 'fromG' [%s], [%s]", prms.get("fromL"), prms.get("fromG"))));
						return response;
					}
					data = data.from(new AstroComputerV2.Pos()
							.latitude(lat)
							.longitude(lng));
					double tPass = acv2.getSunMeridianPassageTime(lat, lng); // In decimal hours
//					long tt = AstroComputer.getSunTransitTime(lat, lng);
					TimeUtil.DMS dms = TimeUtil.decimalToDMS(tPass);

//					int hTPass = (int)Math.floor(tPass);
//					int mTPass = (int)Math.floor((tPass - hTPass) * 60);
//					int sTPass = (int)Math.round((((tPass - hTPass) * 60) - mTPass) * 60);

					int hTPass = dms.getHours();
					int mTPass = dms.getMinutes();
					int sTPass = (int)Math.floor(dms.getSeconds());

					data = data.tPass(new FmtDate()
							.hour(hTPass)
							.min(mTPass)
							.sec(sTPass)
							.tz("UTC"));

					Date solar = getSolarDate(at, tPass);
//				System.out.println(SDF_SOLAR.format(solar));
					String[] sol = SDF_SOLAR.format(solar).split(";");

					data = data.solar(new FmtDate()
							.epoch(solar.getTime())
							.year(Integer.parseInt(sol[0]))
							.month(Integer.parseInt(sol[1]))
							.day(Integer.parseInt(sol[2]))
							.hour(Integer.parseInt(sol[3]))
							.min(Integer.parseInt(sol[4]))
							.sec(Integer.parseInt(sol[5])));

					SightReductionUtil sru = new SightReductionUtil();
					sru.calculate(lat, lng, acv2.getSunGHA(), acv2.getSunDecl());
					data = data.sunObs(new AstroComputerV2.OBS()
							.alt(sru.getHe())
							.z(sru.getZ()));
					sru.calculate(lat, lng, acv2.getMoonGHA(), acv2.getMoonDecl());
					data = data.moonObs(new AstroComputerV2.OBS()
							.alt(sru.getHe())
							.z(sru.getZ()));

					sru.calculate(lat, lng, acv2.getAriesGHA(), 0d);
					data = data.ariesObs(new AstroComputerV2.OBS()
							.alt(sru.getHe())
							.z(sru.getZ()));

					// Sky route from Moon to Sun (will help calculate Moon's tilt)
					double moonLongitude = acv2.ghaToLongitude(acv2.getMoonGHA());
					double sunLongitude = acv2.ghaToLongitude(acv2.getSunGHA());
					GreatCircle gc = new GreatCircle();
					gc.setStartInDegrees(new GreatCirclePoint(new GeoPoint(acv2.getMoonDecl(), moonLongitude)));
					gc.setArrivalInDegrees(new GreatCirclePoint(new GeoPoint(acv2.getSunDecl(), sunLongitude)));
					gc.calculateGreatCircle(20);
					double finalLat = lat;
					double finalLng = lng;
					// TODO All in one operation
					Vector<GreatCircleWayPoint> greatCircleWayPoints = GreatCircle.inDegrees(gc.getRoute()); // In Degrees
					List<AstroComputerV2.GreatCircleWayPointWithBodyFromPos> route = greatCircleWayPoints.stream()
							.map(rwp -> {
								AstroComputerV2.GreatCircleWayPointWithBodyFromPos gcwpwbfp = new AstroComputerV2.GreatCircleWayPointWithBodyFromPos(rwp.getPoint(), rwp.getZ());
									if (rwp.getPoint() != null) {
										sru.calculate(finalLat, finalLng, acv2.longitudeToGHA(rwp.getPoint().getG()), rwp.getPoint().getL());
										gcwpwbfp.setWpFromPos(new AstroComputerV2.BodyFromPos()
												.observer(new AstroComputerV2.Pos()
														.latitude(finalLat)
														.longitude(finalLng))
												.observed(new AstroComputerV2.OBS()
														.alt(sru.getHe())
														.z(sru.getZ())));
									}
									return gcwpwbfp;
								}).collect(Collectors.toList());
					data = data.moonToSunSkyRoute(route);
					// Add Moon Tilt
//					double moonTilt = AstroComputer.getMoonTiltV2(finalLat, finalLng); // Experimental
					double moonTilt = acv2.getMoonTilt(finalLat, finalLng);
					if ("true".equals(System.getProperty("astro.verbose", "false"))) {
						System.out.println(String.format(">> From %s / %s, moon tilt= %.03f\272",
								GeomUtil.decToSex(finalLat, GeomUtil.SWING, GeomUtil.NS),
								GeomUtil.decToSex(finalLng,  GeomUtil.SWING, GeomUtil.EW),
								moonTilt));
					}
					data = data.moonTilt(moonTilt);
				}
				// Wandering bodies
				if (wandering) {
					List<AstroComputerV2.GP> wanderingBodies = new ArrayList<>();
					wanderingBodies.add(new AstroComputerV2.GP()
							.name("aries")
							.gha(acv2.getAriesGHA()));
					SightReductionUtil sru = new SightReductionUtil();
					// Calculate Venus observed prms
					sru.calculate(lat, lng, acv2.getVenusGHA(), acv2.getVenusDecl());
					wanderingBodies.add(new AstroComputerV2.GP()
						.name("venus")
						.decl(acv2.getVenusDecl())
						.gha(acv2.getVenusGHA())
					  .bodyFromPos(new AstroComputerV2.BodyFromPos()
							  .observer(new AstroComputerV2.Pos()
									  .latitude(lat)
									  .longitude(lng))
							  .observed(new AstroComputerV2.OBS()
									  .alt(sru.getHe())
									  .z(sru.getZ()))));
					// Calculate Mars observed prms
					sru.calculate(lat, lng, acv2.getMarsGHA(), acv2.getMarsDecl());
					wanderingBodies.add(new AstroComputerV2.GP()
							.name("mars")
							.decl(acv2.getMarsDecl())
							.gha(acv2.getMarsGHA()).bodyFromPos(new AstroComputerV2.BodyFromPos()
									.observer(new AstroComputerV2.Pos()
											.latitude(lat)
											.longitude(lng))
									.observed(new AstroComputerV2.OBS()
											.alt(sru.getHe())
											.z(sru.getZ()))));
					// Calculate Jupiter observed prms
					sru.calculate(lat, lng, acv2.getJupiterGHA(), acv2.getJupiterDecl());
					wanderingBodies.add(new AstroComputerV2.GP()
							.name("jupiter")
							.decl(acv2.getJupiterDecl())
							.gha(acv2.getJupiterGHA()).bodyFromPos(new AstroComputerV2.BodyFromPos()
									.observer(new AstroComputerV2.Pos()
											.latitude(lat)
											.longitude(lng))
									.observed(new AstroComputerV2.OBS()
											.alt(sru.getHe())
											.z(sru.getZ()))));
					// Calculate Saturn observed prms
					sru.calculate(lat, lng, acv2.getSaturnGHA(), acv2.getSaturnDecl());
					wanderingBodies.add(new AstroComputerV2.GP()
							.name("saturn")
							.decl(acv2.getSaturnDecl())
							.gha(acv2.getSaturnGHA()).bodyFromPos(new AstroComputerV2.BodyFromPos()
									.observer(new AstroComputerV2.Pos()
											.latitude(lat)
											.longitude(lng))
									.observed(new AstroComputerV2.OBS()
											.alt(sru.getHe())
											.z(sru.getZ()))));
					data = data.wandering(wanderingBodies)
							.meanObliquity(acv2.getMeanObliquityOfEcliptic());
				}

				if (stars) {
					List<AstroComputerV2.GP> starPositions = new ArrayList<>();
					Arrays.asList(Star.getCatalog()).stream()
							.forEach(star -> {
								Core.starPos(star.getStarName());
								starPositions.add(new AstroComputerV2.GP()
									.name(star.getStarName()) // Also available star.getConstellation()
									.gha(Context.GHAstar)
									.decl(Context.DECstar));
							});
					data = data.stars(starPositions);
				}

				String content = new Gson().toJson(data);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());

			} catch (Exception ex) {

				System.err.println("Error converting to Json:\n" + data.toString());

				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0401")
								.errorMessage(ex.toString())
								.errorStack(Arrays.asList(ex.getStackTrace())
																		.stream()
																		.map(te -> te.toString())
																		.collect(Collectors.toList())));
				// More details
//				String stackTrace = new Gson().toJson(ex.getStackTrace());
//				RESTProcessorUtil.generateResponseHeaders(response, stackTrace.length());
//				response.setPayload(stackTrace.getBytes());

				return response;
			}
		}
		return response;
	}

	/**
	 * Compute the declinations of given bodies (one or more) between two UTC dates, each hour.
	 * {
	 *   bodies: ['Sun', 'Moon', 'Venus', 'Mars', 'Jupiter', 'Saturn'],
	 *   from: '2019-12-15T00:00:00',
	 *   to: '2019-12-25T00:00:00'
	 * }
	 * @param request Must contain a json object containing the bodies, from and to dates.
	 * @return Array of Json Objects, one for each body with the declination hour by hour
	 */
	private Response getBodyDeclination(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		AstroComputerV2 acv2 = new AstroComputerV2();

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					DeclinationOptions options = gson.fromJson(stringReader, DeclinationOptions.class);
					List<String> invalids = checkInvalidBodies(options.bodies);
					if (!invalids.isEmpty()) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0402")
										.errorMessage(String.format("Invalid body(ies): %s",
												invalids.stream()
														.collect(Collectors.joining(", ")))));
						return response;
					}
					// Dates
					if (options.from == null || options.to == null) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0403")
										.errorMessage("to and from dates are required."));
						return response;
					}
					DURATION_FMT.setTimeZone(TimeZone.getTimeZone(UTC_TZ));

					Date from = DURATION_FMT.parse(options.from);
					Date to = DURATION_FMT.parse(options.to);
					Calendar fromCal = Calendar.getInstance(TimeZone.getTimeZone(UTC_TZ));
					fromCal.setTime(from);
					Calendar toCal = Calendar.getInstance(TimeZone.getTimeZone(UTC_TZ));
					toCal.setTime(to);

//					System.out.println(String.format("Calculating between %s and %s", UTC_FMT.format(fromCal.getTime()), UTC_FMT.format(toCal.getTime())));
					if (!toCal.after(fromCal)) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0404")
										.errorMessage(String.format("Bad chronology, %s should be AFTER %s", UTC_FMT.format(toCal.getTime()), UTC_FMT.format(fromCal.getTime()))));
						return response;
					}
					// All prms OK, proceeding.
					Calendar date = fromCal;
					boolean keepWorking = true;
					final Map<String, SortedMap<String, Double>> declHolder = new HashMap<>();
					// Init map
					options.bodies.forEach(body -> declHolder.put(body, new TreeMap<>()));
					while (keepWorking) {
						acv2.calculate(
								date.get(Calendar.YEAR),
								date.get(Calendar.MONTH) + 1,
								date.get(Calendar.DAY_OF_MONTH),
								date.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
								date.get(Calendar.MINUTE),
								date.get(Calendar.SECOND));
						for (String body : options.bodies) {
							double decl = 0D;
							switch (body) {
								case "Sun":
									decl = acv2.getSunDecl();
									break;
								case "Moon":
									decl = acv2.getMoonDecl();
									break;
								case "Venus":
									decl = acv2.getVenusDecl();
									break;
								case "Mars":
									decl = acv2.getMarsDecl();
									break;
								case "Jupiter":
									decl = acv2.getJupiterDecl();
									break;
								case "Saturn":
									decl = acv2.getSaturnDecl();
									break;
								default:
									break;
							}
							declHolder.get(body).put(DURATION_FMT.format(date.getTime()), decl);
						}
						date.add(Calendar.HOUR, 1);
						if (date.after(toCal)) {
							keepWorking = false;
						}
					}
					String content = new Gson().toJson(declHolder);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0401-1")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0400-1")
							.errorMessage("Empty payload. Cannot proceed."));
			return response;
		}
		return response;
	}

	private final static List<String> VALID_BODIES = Arrays.asList("Sun", "Moon", "Venus", "Mars", "Jupiter", "Saturn");
	private static List<String> checkInvalidBodies(List<String> bodies) {
		List<String> invalidBodies = new ArrayList<>();
		bodies.forEach(body -> {
			if (!VALID_BODIES.contains(body)) {
				invalidBodies.add(body);
			}
		});
		return Collections.unmodifiableList(invalidBodies); // Immutable, just for fun ;)
	}

	/**
	 * Computes the Sun and Moon Declination and altitude for a given position, and given time (UTC).
	 *
	 * @param request MUST contain a GeoPoint payload (observer's position), and query String prms from (duration format), to (duration format), and tz (timezone name).
	 * @return
	 */
	private Response getSunMoonDecAlt(Request request) {
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
									.errorCode("ASTRO-0007")
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
							.errorCode("ASTRO-0008")
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
								.errorCode("ASTRO-0009")
								.errorMessage(ex.toString()));
				return response;
			}
			if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(tzName)) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0010")
								.errorMessage(String.format("Invalid TimeZone: %s", tzName)));
				return response;
			}
		}

		List<SunMoonDecAlt> list = new ArrayList<>();
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone(tzName));
		try {
			Date from = DURATION_FMT.parse(fromPrm);
			Date to = DURATION_FMT.parse(toPrm);
			Calendar toCal = Calendar.getInstance(TimeZone.getTimeZone(tzName));
			toCal.setTime(to);
			Calendar current = Calendar.getInstance(TimeZone.getTimeZone(tzName));
			current.setTime(from);
			if ("true".equals(System.getProperty("astro.verbose", "false"))) {
				System.out.println("Starting Sun and Moon data calculation at " + current.getTime() + " (" + fromPrm + ")");
			}
			do {
				Calendar utc = (Calendar)current.clone();
				utc.getTime(); // Bug? Needed to apply the new timezone...
				utc.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
				AstroComputerV2 acv2 = new AstroComputerV2();
				double[] astroData = acv2.getSunMoonAltDecl(
						utc.get(Calendar.YEAR),
						utc.get(Calendar.MONTH) + 1,
						utc.get(Calendar.DAY_OF_MONTH),
						utc.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
						utc.get(Calendar.MINUTE),
						0, // current.get(Calendar.SECOND),
						pos.getL(),
						pos.getG());

				SunMoonDecAlt data = new SunMoonDecAlt()
						.epoch(current.getTimeInMillis())
						.lat(pos.getL())
						.lng(pos.getG())
						.sunAlt(astroData[AstroComputerV2.HE_SUN_IDX])
						.sunDecl(astroData[AstroComputerV2.DEC_SUN_IDX])
						.moonAlt(astroData[AstroComputerV2.HE_MOON_IDX])
						.moonDecl(astroData[AstroComputerV2.DEC_MOON_IDX])
						.moonPhase(astroData[AstroComputerV2.MOON_PHASE_IDX]);
				list.add(data);
				if ("true".equals(System.getProperty("astro.verbose", "false"))) {
					System.out.println(String.format("%04d-%02d-%02d %02d:%02d:%02d, %s, hSun: %.02f",
							utc.get(Calendar.YEAR),
							utc.get(Calendar.MONTH) + 1,
							utc.get(Calendar.DATE),
							utc.get(Calendar.HOUR_OF_DAY),
							utc.get(Calendar.MINUTE),
							utc.get(Calendar.SECOND),
							utc.getTime(), astroData[AstroComputerV2.HE_SUN_IDX]));
				}
				current.add(Calendar.MINUTE , 5); // Hard coded for now, 5 minutes interval.
			} while (current.before(toCal));
			String content = new Gson().toJson(list);
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

	private String generateAstroData(AlmanacOptions options) throws Exception {
		try {
			// Extract parameters
			File temp = File.createTempFile("astro", ".xml");
			String tempFileName = temp.getAbsolutePath();
			String[] prms = null;
			if ("continuous".equals(options.type.type())) {
				int card = 6;
				if (options.month > 0) {
					card = 8;
				}
				if (options.day > 0) {
					card = 10;
				}
				prms = new String[card];
				prms[0] = "-type"; prms[1] = options.type.type();
				prms[2] = "-year"; prms[3] = String.valueOf(options.year);
				prms[4] = "-out"; prms[5] = tempFileName;
				if (options.month > 0) {
					prms[6] = "-month"; prms[7] = String.valueOf(options.month);
				}
				if (options.day > 0) {
					prms[8] = "-day"; prms[9] = String.valueOf(options.day);
				}
			} else {
				prms = new String[]{
						"-type", options.type.type(),

						"-from-year", String.valueOf(options.fromYear),
						"-from-month", String.valueOf(options.fromMonth),
						"-from-day", String.valueOf(options.fromDay),

						"-to-year", String.valueOf(options.toYear),
						"-to-month", String.valueOf(options.toMonth),
						"-to-day", String.valueOf(options.toDay),

						"-out", tempFileName
				};
			}
			if ("true".equals(System.getProperty("astro.verbose", "false"))) {
				System.out.println(String.format("Invoking AlmanacComputer with %s", Arrays.asList(prms).stream()
						.collect(Collectors.joining(" "))));
			}
			AlmanacComputer.main(prms);
			return tempFileName;

		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Publish almanac (generate pdf) for a given period of time.
	 * Supports a payload in the body, in json format:
	 * @see {@link AlmanacOptions}
	 *
	 * @param request
	 * @return
	 */
	private Response publishAlmanac(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				try {
					Gson gson = new GsonBuilder().create();
					StringReader stringReader = new StringReader(payload);
					AlmanacOptions options = gson.fromJson(stringReader, AlmanacOptions.class);
					String tempFileName = generateAstroData(options);
					System.out.println("Data Generation completed.");
					// Ready for transformation
					try {
						String tempPdfFileName = File.createTempFile("almanac", ".pdf").getAbsolutePath();
						String almanacTxPrm = String.format("%s %s %s %s", options.language, options.withStars ? "true" : "false", tempFileName, tempPdfFileName);
						// Script name in a System variable, must be in the xsl folder
						String cmd = "." + File.separator + "xsl" + File.separator + String.format("%s ", System.getProperty("publishalmanac.script", "publishalmanac.sh")) + almanacTxPrm;
						System.out.println("Tx Command:" + cmd);
						Process p = Runtime.getRuntime().exec(cmd);
						BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = null;
						while ((line = stdout.readLine()) != null) {
							System.out.println(line);
						}
						int exitStatus = p.waitFor();
						System.out.println("Script completed, status " + exitStatus);
						System.out.println(String.format("See %s", tempPdfFileName));
						cmd = String.format("mv %s web", tempPdfFileName);
						p = Runtime.getRuntime().exec(cmd);
						stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
						line = null;
						while ((line = stdout.readLine()) != null) {
							System.out.println(line);
						}
						exitStatus = p.waitFor();
						System.out.println("Copy command completed, status " + exitStatus);
						response.setPayload(String.format(".%s", tempPdfFileName.substring(tempPdfFileName.lastIndexOf(File.separator))).getBytes());
					} catch (Exception ex) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0104")
										.errorMessage(ex.toString()));
						return response;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0103")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0101")
							.errorMessage("Required payload not found."));
			return response;
		}
		return response;
	}

	/**
	 * Publish lunar distances (generate pdf) for a given period of time.
	 * Supports a payload in the body, in json format:
	 * @see {@link AlmanacOptions}
	 *
	 * @param request
	 * @return
	 */
	private Response publishLunar(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				try {
					Gson gson = new GsonBuilder().create();
					StringReader stringReader = new StringReader(payload);
					AlmanacOptions options = gson.fromJson(stringReader, AlmanacOptions.class);
					String tempFileName = generateAstroData(options);
					System.out.println("Data Generation completed.");
					// Ready for transformation
					try {
						String tempPdfFileName = File.createTempFile("lunar", ".pdf").getAbsolutePath();
						String almanacTxPrm = String.format("%s %s %s", options.language, tempFileName, tempPdfFileName);
						// Script name in a System variable. Must be in the xsl folder
						String cmd = "." + File.separator + "xsl" + File.separator + String.format("%s ", System.getProperty("publishlunar.script", "publishlunar.sh")) + almanacTxPrm;
						System.out.println("Tx Command:" + cmd);
						Process p = Runtime.getRuntime().exec(cmd);
						BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = null;
						while ((line = stdout.readLine()) != null) {
							System.out.println(line);
						}
						int exitStatus = p.waitFor();
						System.out.println("Script completed, status " + exitStatus);
						System.out.println(String.format("See %s", tempPdfFileName));
						cmd = String.format("mv %s web", tempPdfFileName);
						p = Runtime.getRuntime().exec(cmd);
						stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
						line = null;
						while ((line = stdout.readLine()) != null) {
							System.out.println(line);
						}
						exitStatus = p.waitFor();
						System.out.println("Copy command completed, status " + exitStatus);
						response.setPayload(String.format(".%s", tempPdfFileName.substring(tempPdfFileName.lastIndexOf(File.separator))).getBytes());
					} catch (Exception ex) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0204")
										.errorMessage(ex.toString()));
						return response;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0203")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("ASTRO-0201")
							.errorMessage("Required payload not found."));
			return response;
		}
		return response;
	}

	/**
	 * Publish perpetual almanac (generate pdf) for a given period of time.
	 * Supports a payload in the body, in json format:
	 * <pre>
	 * {
	 *   "from": 2017,
	 *   "to": 2020,
	 * }
	 * </pre>
	 * @param request
	 * @return
	 */
	private Response publishPerpetual(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				try {
					Gson gson = new GsonBuilder().create();
					StringReader stringReader = new StringReader(payload);
					String errMess = "";
					PerpetualAlmanacOptions options;
					try {
						options = gson.fromJson(stringReader, PerpetualAlmanacOptions.class);
						if (options.from > 2_100 || options.from < 1_900) {
							errMess += ((errMess.length() > 0 ? "\n" : "") + "Invalid from year, must be in [1900..2100].");
						}
						if (options.to > 2_100 || options.to < 1_900) {
							errMess += ((errMess.length() > 0 ? "\n" : "") + "Invalid to year, must be in [1900..2100].");
						}
						if (!errMess.isEmpty()) {
							response = HTTPServer.buildErrorResponse(response,
									Response.BAD_REQUEST,
									new HTTPServer.ErrorPayload()
											.errorCode("ASTRO-0300")
											.errorMessage(errMess));
							return response;
						}
					} catch (Exception ex) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0301")
										.errorMessage(ex.toString()));
						return response;
					}
					try {
						// Right parameters
						File temp = File.createTempFile("perpetual", ".xml");
						String tempFileName = temp.getAbsolutePath();
						String[] prms = new String[]{
								String.valueOf(options.from),
								String.valueOf(options.to),
								tempFileName
						};
						if ("true".equals(System.getProperty("astro.verbose", "false"))) {
							System.out.println(String.format("Invoking Almanac Publisher with %s", Arrays.asList(prms).stream()
									.collect(Collectors.joining(" "))));
						}
						Publisher.main(prms);
						System.out.println("Data Generation completed.");
						// Ready for transformation
						try {
							String tempPdfFileName = File.createTempFile("perpetual", ".pdf").getAbsolutePath();
							String almanacTxPrm = String.format("%s %s", tempFileName, tempPdfFileName);
							// Script name in a System variable, must be in the xsl folder
							String cmd = "." + File.separator + "xsl" + File.separator + String.format("%s ", System.getProperty("publishperpetual.script", "publishperpetual.sh")) + almanacTxPrm;
							System.out.println("Tx Command:" + cmd);
							Process p = Runtime.getRuntime().exec(cmd);
							BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
							String line = null;
							while ((line = stdout.readLine()) != null) {
								System.out.println(line);
							}
							int exitStatus = p.waitFor();
							System.out.println("Script completed, status " + exitStatus);
							System.out.println(String.format("See %s", tempPdfFileName));
							cmd = String.format("mv %s web", tempPdfFileName);
							p = Runtime.getRuntime().exec(cmd);
							stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
							line = null;
							while ((line = stdout.readLine()) != null) {
								System.out.println(line);
							}
							exitStatus = p.waitFor();
							System.out.println("Copy command completed, status " + exitStatus);
							response.setPayload(String.format(".%s", tempPdfFileName.substring(tempPdfFileName.lastIndexOf(File.separator))).getBytes());
						} catch (Exception ex) {
							response = HTTPServer.buildErrorResponse(response,
									Response.BAD_REQUEST,
									new HTTPServer.ErrorPayload()
											.errorCode("ASTRO-0304")
											.errorMessage(ex.toString()));
							return response;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("ASTRO-0303")
										.errorMessage(ex.toString()));
						return response;
					}
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("ASTRO-0302")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("ASTRO-0306")
								.errorMessage("Required payload not found."));
				return response;
			}
		}
		return response;
	}


	public static class CelestialBodyData {
		String name; // Sun, Moon, Planet, or star.
		double instrumentalAltitude;
		enum Limb {
			NONE, LOWER, UPPER
		}
		Limb limb = Limb.NONE;
		double eyeHeight;

		public CelestialBodyData name(String name) {
			this.name = name;
			return this;
		}
		public CelestialBodyData instrumentalAltitude(double instrumentalAltitude) {
			this.instrumentalAltitude = instrumentalAltitude;
			return this;
		}
		public CelestialBodyData limb(Limb limb) {
			this.limb = limb;
			return this;
		}
		public CelestialBodyData eyeHeight(double eyeHeight) {
			this.eyeHeight = eyeHeight;
			return this;
		}
	}
	public static class SightReductionData {
		AstroComputerV2.Pos estimatedPosition;
		String utcDate; // Duration Format
		CelestialBodyData cbd;

		public SightReductionData estimatedPosition(AstroComputerV2.Pos position) {
			this.estimatedPosition = position;
			return this;
		}
		public SightReductionData utcDate(String date) {
			this.utcDate = date;
			return this;
		}
		public SightReductionData celestialBodyData(CelestialBodyData cbd) {
			this.cbd = cbd;
			return this;
		}
	}

	/**
	 * Used to see what the data will look like in JSON.
	 * No other reason to exist, will eventually disappear.
	 *
	 * @param request
	 * @return
	 */
	private Response getSightReductionUserData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		SightReductionData userData = new SightReductionData()
				.utcDate("2018-11-05T20:50:52")
				.estimatedPosition(new AstroComputerV2.Pos().latitude(37.4090).longitude(-122.7654))
				.celestialBodyData(new CelestialBodyData()
																.name("Sun")
																.eyeHeight(1.8)
																.instrumentalAltitude(35.0740)
																.limb(CelestialBodyData.Limb.LOWER));
		String content = new Gson().toJson(userData);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * Expects a json payload containing:
	 * - Observation date and time (UTC)
	 * - Estimated position
	 * - Observed body
	 *    - Name
	 *    - Sextant altitude (instrumental)
	 *    - Upper/Lower limb
	 *    Eye height (meters)
	 *
	 *    Looks like this in JSON:
	 *    {
	 *     "estimatedPosition": {
	 *         "latitude": 37.409,
	 *         "longitude": -122.7654
	 *     },
	 *     "utcDate": "2018-11-05T20:50:52",
	 *     "cbd": {
	 *         "name": "Sun",
	 *         "instrumentalAltitude": 35.074,
	 *         "limb": "LOWER",
	 *         "eyeHeight": 1.8
	 *     }
	 *  }
	 * @param request
	 * @return
	 */
	private HTTPServer.Response sightReduction(HTTPServer.Request request) {

		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		String payload = new String(request.getContent());

		return sightReductionImplementation(response, payload, false);
	}


	private HTTPServer.Response reverseSightReduction(HTTPServer.Request request) {

		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		String payload = new String(request.getContent());

		return sightReductionImplementation(response, payload, true);
	}

	private HTTPServer.Response sightReductionImplementation(HTTPServer.Response response, String payload, boolean reverse) {

		if (!"null".equals(payload)) {
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			try {
				SightReductionData userData = gson.fromJson(stringReader, SightReductionData.class);
				// Validate the body name
				String bodyName = userData.cbd.name;
				if (!bodyName.equals("Sun") &&
						!bodyName.equals("Moon") &&
						!bodyName.equals("Venus") &&
						!bodyName.equals("Mars") &&
						!bodyName.equals("Jupiter") &&
						!bodyName.equals("Saturn") &&
						Star.getStar(bodyName) == null) {
					// Body not recognized
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("NAV-0003")
									.errorMessage(String.format("Body [%s] unknown.", bodyName)));
					return response;
				} else { // Proceed
					DURATION_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
					try {
						Date from = DURATION_FMT.parse(userData.utcDate);
						Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
						current.setTime(from);
						if ("true".equals(System.getProperty("astro.verbose", "false"))) {
							System.out.println("Starting Sight Reduction calculation at " + current.getTime() + " (" + userData.utcDate + ")");
						}
						AstroComputerV2 acv2 = new AstroComputerV2();
						acv2.calculate(
								current.get(Calendar.YEAR),
								current.get(Calendar.MONTH) + 1,
								current.get(Calendar.DAY_OF_MONTH),
								current.get(Calendar.HOUR_OF_DAY), // and not HOUR !!!!
								current.get(Calendar.MINUTE),
								current.get(Calendar.SECOND));

						double gha = 0, decl = 0;
						double hp = 0, sd = 0;

						double hDip = 0;
						double refr = 0;
						double parallax = 0;
						double obsAlt = 0;
						double totalCorrection = 0d;

						double lunar = -1;

						// Depends on the body
						switch (bodyName) {
							case "Sun":
								gha = acv2.getSunGHA();
								decl = acv2.getSunDecl();
								hp  = Context.HPsun / 3600d;
								sd  = Context.SDsun / 3600d;
								lunar = Context.LDist;
								break;
							case "Moon":
								gha = acv2.getMoonGHA();
								decl = acv2.getMoonDecl();
								hp  = Context.HPmoon / 3600d;
								sd  = Context.SDmoon / 3600d;
								break;
							case "Venus":
								gha = acv2.getVenusGHA();
								decl = acv2.getVenusDecl();
								hp  = Context.HPvenus / 3600d;
								sd  = Context.SDvenus / 3600d;
								lunar = Context.moonVenusDist;
								break;
							case "Mars":
								gha = acv2.getMarsGHA();
								decl = acv2.getMarsDecl();
								hp  = Context.HPmars / 3600d;
								sd  = Context.SDmars / 3600d;
								lunar = Context.moonMarsDist;
								break;
							case "Jupiter":
								gha = acv2.getJupiterGHA();
								decl = acv2.getJupiterDecl();
								hp  = Context.HPjupiter / 3600d;
								sd  = Context.SDjupiter / 3600d;
								lunar = Context.moonJupiterDist;
								break;
							case "Saturn":
								gha = acv2.getSaturnGHA();
								decl = acv2.getSaturnDecl();
								hp  = Context.HPsaturn / 3600d;
								sd  = Context.SDsaturn / 3600d;
								lunar = Context.moonSaturnDist;
								break;
							default: // Stars
								Core.starPos(bodyName);
								gha = Context.GHAstar;
								decl = Context.DECstar;
								hp = 0d;
								sd = 0d;
								lunar = Context.starMoonDist;
								break;
						}

						Map<String, Double> reduced = new HashMap<>();
						if (lunar != -1) { // i.e. not shooting the moon
							reduced.put("lunar-distance", lunar);
						}
						reduced.put("gha", gha);
						reduced.put("decl", decl);
						reduced.put("sd", sd);
						reduced.put("hp", hp);

						SightReductionUtil sru = new SightReductionUtil();

						sru.setL(userData.estimatedPosition.getLatitude());
						sru.setG(userData.estimatedPosition.getLongitude());

						sru.setAHG(gha);
						sru.setD(decl);
						sru.calculate();

						if (!reverse) {
							obsAlt = SightReductionUtil.observedAltitude(userData.cbd.instrumentalAltitude,
									userData.cbd.eyeHeight,
									hp,    // Returned in seconds, sent in degrees
									sd,    // Returned in seconds, sent in degrees
									userData.cbd.limb.equals(CelestialBodyData.Limb.LOWER) ? SightReductionUtil.LOWER_LIMB : SightReductionUtil.UPPER_LIMB,
									"true".equals(System.getProperty("astro.verbose", "false")));

							hDip = sru.getHorizonDip();
							refr = sru.getRefraction();
							parallax = sru.getPa();

							totalCorrection = 0d;
							totalCorrection -= (hDip / 60D);
							totalCorrection -= (refr / 60D);
							totalCorrection += (parallax);
							if (userData.cbd.limb.equals(CelestialBodyData.Limb.UPPER)) {
								sd = -sd;
							} else if (userData.cbd.limb.equals(CelestialBodyData.Limb.NONE)) {
								sd = 0;
							}
							totalCorrection += sd;

							sru.calculate(userData.estimatedPosition.getLatitude(), userData.estimatedPosition.getLongitude(), gha, decl);

							double estimatedAltitude = sru.getHe();
							double z = sru.getZ();

							double intercept = obsAlt - estimatedAltitude;

							reduced.put("observed-altitude-degrees", obsAlt);
							reduced.put("estimated-altitude-degrees", estimatedAltitude);
							reduced.put("z", z);

							reduced.put("horizon-depression-minutes", hDip); // In minutes of arc
							reduced.put("total-correction-minutes", totalCorrection * 60); // In minutes of arc
							reduced.put("horizontal-parallax-minutes", hp * 60d); // In minutes of arc
							reduced.put("parallax-minutes", parallax * 60d); // In minutes of arc
							reduced.put("semi-diameter-minutes", sd * 60); // In minutes of arc
							reduced.put("refraction-minutes", refr); // In minutes of arc
							reduced.put("intercept-degrees", intercept); // In degrees
							reduced.put("delta-t", acv2.getDeltaT()); // In seconds

							if ("true".equals(System.getProperty("astro.verbose", "false"))) {
								System.out.println("For eye height " + DF22.format(userData.cbd.eyeHeight) + " m, horizon dip = " + DF22.format(hDip) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("Refraction " + DF22.format(refr) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("For hp " + DF22.format(hp * 60d) + "', parallax " + DF22.format(parallax * 60d) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("Semi-diameter: " + DF22.format(sd * 60d) + "'");
								System.out.println("Intercept:" + DF22.format(Math.abs(intercept) * 60d) + "' " + (intercept < 0 ? "away from" : "towards") + " " + bodyName);
							}
						} else { // Reverse sight
							obsAlt = sru.getHe();
							hDip = SightReductionUtil.getHorizonDip(userData.cbd.eyeHeight);
							// sd, we have already
							parallax = SightReductionUtil.getParallax(hp, obsAlt);
							refr = SightReductionUtil.getRefraction(obsAlt - parallax);
							double hi = obsAlt;
							if (userData.cbd.limb.equals(CelestialBodyData.Limb.UPPER)) {
								sd = -sd;
							} else if (userData.cbd.limb.equals(CelestialBodyData.Limb.NONE)) {
								sd = 0;
							}
							totalCorrection = 0d;
							totalCorrection -= (hDip / 60D);
							totalCorrection -= (refr / 60D);
							totalCorrection += (parallax);
							totalCorrection += sd;
							hi -= sd;
							hi += (hDip / 60d);
							hi -= parallax;
							hi += (refr / 60d);

							if ("true".equals(System.getProperty("astro.verbose", "false"))) {
								System.out.println("For eye height " + DF22.format(userData.cbd.eyeHeight) + " m, horizon dip = " + DF22.format(hDip) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("Refraction " + DF22.format(refr) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("For hp " + DF22.format(hp * 60d) + "', parallax " + DF22.format(parallax * 60d) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("Semi-diameter: " + DF22.format(sd * 60d) + "'");
								System.out.println("  - Total Corr. :" + DF22.format(totalCorrection * 60d) + "'");
								System.out.println("Hi " + hi);
							}

							reduced.put("instrumental-altitude", hi);
							reduced.put("observed-altitude-degrees", obsAlt); // To observe... (hi, with corrections)
							reduced.put("horizon-depression-minutes", hDip); // In minutes of arc
							reduced.put("total-correction-minutes", totalCorrection * 60); // In minutes of arc
							reduced.put("horizontal-parallax-minutes", hp * 60d); // In minutes of arc
							reduced.put("parallax-minutes", parallax * 60d); // In minutes of arc
							reduced.put("semi-diameter-minutes", sd * 60); // In minutes of arc
							reduced.put("refraction-minutes", refr); // In minutes of arc
							reduced.put("delta-t", acv2.getDeltaT()); // In seconds
						}

						String content = new Gson().toJson(reduced);
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
//					return response;
					} catch (ParseException pe) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("NAV-0004")
										.errorMessage(pe.toString()));
						return response;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("NAV-0001")
								.errorMessage(ex.toString()));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("NAV-0002")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
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

	public static class FmtDate {
		long epoch;
		int year;
		int month; // Jan: 1, Dec: 12
		int day;
		int hour;
		int min;
		int sec;
		String tz;

		@Override
		public String toString() {
			return "FmtDate{" +
					"epoch=" + epoch +
					", year=" + year +
					", month=" + month +
					", day=" + day +
					", hour=" + hour +
					", min=" + min +
					", sec=" + sec +
					", tz='" + tz + '\'' +
					'}';
		}

		public FmtDate epoch(long epoch) {
			this.epoch = epoch;
			return this;
		}
		public FmtDate year(int year) {
			this.year = year;
			return this;
		}
		public FmtDate month(int month) {
			this.month = month;
			return this;
		}
		public FmtDate day(int day) {
			this.day = day;
			return this;
		}
		public FmtDate hour(int hour) {
			this.hour = hour;
			return this;
		}
		public FmtDate min(int min) {
			this.min = min;
			return this;
		}
		public FmtDate sec(int sec) {
			this.sec = sec;
			return this;
		}
		public FmtDate tz(String tz) {
			this.tz = tz;
			return this;
		}
	}

	public static class PositionsInTheSky {
		long epoch;
		double deltaT;
		AstroComputerV2.GP sun;
		AstroComputerV2.GP moon;
		double moonPhase; // Moon only, obviously
		double ghaAries;
		List<AstroComputerV2.GreatCircleWayPointWithBodyFromPos> moonToSunSkyRoute;
		double moonTilt;
		List<AstroComputerV2.GP> wanderingBodies;
		List<AstroComputerV2.GP> stars;
		double eclipticObliquity; // Mean
		AstroComputerV2.Pos from;
		AstroComputerV2.OBS sunObs;
		AstroComputerV2.OBS moonObs;
		AstroComputerV2.OBS ariesObs;
		FmtDate tPass;
		FmtDate solarDate;

		@Override
		public String toString() {
			return String.format("Epoch: %d\nDeltaT: %f\nSun:%s\nMoon:%s\nMoonPhase:%f\nGHAAries:%f\nRoute to Sun:%s\nWand.Bodies:%s\nStars:%s\n" +
							"Ecl.Obl:%f\nFrom:%s\nSun Obs:%s\nMoon Obs:%s\nAries Obs:%s\nTPass:%s\nSolar Date:%s",
					epoch, deltaT, sun, moon, moonPhase, ghaAries, moonToSunSkyRoute,
					wanderingBodies, stars, eclipticObliquity, from, sunObs, moonObs, ariesObs, tPass, solarDate);
		}

		public PositionsInTheSky epoch(long epoch) {
			this.epoch = epoch;
			return this;
		}
		public PositionsInTheSky deltaT(double deltaT) {
			this.deltaT = deltaT;
			return this;
		}
		public PositionsInTheSky ghaAries(double gha) {
			this.ghaAries = gha;
			return this;
		}
		public PositionsInTheSky sun(AstroComputerV2.GP sun) {
			this.sun = sun;
			return this;
		}
		public PositionsInTheSky moon(AstroComputerV2.GP moon) {
			this.moon = moon;
			return this;
		}
		public PositionsInTheSky moonPhase(double phase) {
			this.moonPhase = phase;
			return this;
		}
		public PositionsInTheSky moonToSunSkyRoute(List<AstroComputerV2.GreatCircleWayPointWithBodyFromPos> moonToSunSkyRoute) {
			this.moonToSunSkyRoute = moonToSunSkyRoute;
			return this;
		}
		public PositionsInTheSky moonTilt(double moonTilt) {
			this.moonTilt = moonTilt;
			return this;
		}

		public PositionsInTheSky from(AstroComputerV2.Pos pos) {
			this.from = pos;
			return this;
		}

		public PositionsInTheSky sunObs(AstroComputerV2.OBS sun) {
			this.sunObs = sun;
			return this;
		}

		public PositionsInTheSky moonObs(AstroComputerV2.OBS moon) {
			this.moonObs = moon;
			return this;
		}

		public PositionsInTheSky ariesObs(AstroComputerV2.OBS aries) {
			this.ariesObs = aries;
			return this;
		}

		public PositionsInTheSky tPass(FmtDate tPass) {
			this.tPass = tPass;
			return this;
		}

		public PositionsInTheSky solar(FmtDate solar) {
			this.solarDate = solar;
			return this;
		}

		public PositionsInTheSky meanObliquity(double obl) {
			this.eclipticObliquity = obl;
			return this;
		}

		public PositionsInTheSky wandering(List<AstroComputerV2.GP> bodies) {
			this.wanderingBodies = bodies;
			return this;
		}

		public PositionsInTheSky stars(List<AstroComputerV2.GP> stars) {
			this.stars = stars;
			return this;
		}
	}

	public static class SunMoonDecAlt {
		long epoch;
		double lat;
		double lng;
		double sunDecl;
		double moonDecl;
		double sunAlt;
		double moonAlt;
		double moonPhase;

		public SunMoonDecAlt epoch(long epoch) {
			this.epoch = epoch;
			return this;
		}

		public SunMoonDecAlt lat(double lat) {
			this.lat = lat;
			return this;
		}

		public SunMoonDecAlt lng(double lng) {
			this.lng = lng;
			return this;
		}

		public SunMoonDecAlt sunDecl(double sunDecl) {
			this.sunDecl = sunDecl;
			return this;
		}

		public SunMoonDecAlt moonDecl(double moonDecl) {
			this.moonDecl = moonDecl;
			return this;
		}

		public SunMoonDecAlt sunAlt(double sunAlt) {
			this.sunAlt = sunAlt;
			return this;
		}

		public SunMoonDecAlt moonAlt(double moonAlt) {
			this.moonAlt = moonAlt;
			return this;
		}

		public SunMoonDecAlt moonPhase(double moonPhase) {
			this.moonPhase = moonPhase;
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
		double altitude; // aka elevation
		double z;
		double eot;
		long riseTime; // epoch
		long setTime;  // epoch
		long sunTransitTime; // epoch
		double riseZ;
		double setZ;

		public BodyDataForPos(long epoch, double lat, double lng, String body) {
			this.epoch = epoch;
			this.lat = lat;
			this.lng = lng;
			this.body = body;
		}

		public BodyDataForPos altitude(double altitude) {
			this.altitude = altitude;
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
		public BodyDataForPos riseTime(long riseTime) {
			this.riseTime = riseTime;
			return this;
		}
		public BodyDataForPos setTime(long setTime) {
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
		public BodyDataForPos setTransit(long transit) {
			this.sunTransitTime = transit;
			return this;
		}
	}

	private BodyDataForPos getSunData(double lat, double lng) {
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		return getSunDataForDate(lat, lng, current);
	}

	private BodyDataForPos getSunDataForDate(double lat, double lng, Calendar current) {
		AstroComputerV2 acv2 = new AstroComputerV2();
		acv2.setDateTime(current.get(Calendar.YEAR),
				current.get(Calendar.MONTH) + 1,
				current.get(Calendar.DATE),
				current.get(Calendar.HOUR_OF_DAY),
				current.get(Calendar.MINUTE),
				current.get(Calendar.SECOND));
		acv2.calculate();
		SightReductionUtil sru = new SightReductionUtil(acv2.getSunGHA(),
				acv2.getSunDecl(),
				lat,
				lng);
		sru.calculate();
		double he = sru.getHe().doubleValue();
		double z = sru.getZ().doubleValue();
		double sunDecl = acv2.getSunDecl();
		double sunGHA = acv2.getSunGHA();

		long sunTransitTime = acv2.getSunTransitTime(lat, lng);

//		double[] sunRiseAndSet = acv2.sunRiseAndSet(lat, lng);
//		Calendar dayOne = Calendar.getInstance(current.getTimeZone()); // TimeZone.getTimeZone("Etc/UTC"));
//		// 00:00:00
//		dayOne.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DATE), 0, 0, 0);
//		dayOne.set(Calendar.MILLISECOND, 0);
//		Calendar rise = (Calendar)dayOne.clone();
//		Calendar set = (Calendar)dayOne.clone();
//
//		rise.setTimeZone(TimeZone.getTimeZone("Etc/UTC")); // current.getTimeZone());
//		set.setTimeZone(TimeZone.getTimeZone("Etc/UTC")); // current.getTimeZone());
//
//		TimeUtil.DMS dmsRise = TimeUtil.decimalToDMS(sunRiseAndSet[acv2.UTC_RISE_IDX]);
//		try {
//			rise.add(Calendar.HOUR_OF_DAY, dmsRise.getHours());
//			rise.add(Calendar.MINUTE, dmsRise.getMinutes());
//			rise.add(Calendar.SECOND, (int)Math.floor(dmsRise.getSeconds()));
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		TimeUtil.DMS dmsSet = TimeUtil.decimalToDMS(sunRiseAndSet[acv2.UTC_SET_IDX]);
//		try {
//			set.add(Calendar.HOUR_OF_DAY, dmsSet.getHours());
//			set.add(Calendar.MINUTE, dmsSet.getMinutes());
//			set.add(Calendar.SECOND, (int)Math.floor(dmsSet.getSeconds()));
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		if ("true".equals(System.getProperty("astro.verbose", "false"))) {
//			System.out.println("Day origin:" + dayOne.getTime() + " (" + NumberFormat.getInstance().format(dayOne.getTimeInMillis()) + ")");
//			System.out.println(
//					"Rise:" + rise.getTime() + " (" + NumberFormat.getInstance().format(sunRiseAndSet[acv2.UTC_RISE_IDX]) +
//							"), Set:" + set.getTime() + " (" + NumberFormat.getInstance().format(sunRiseAndSet[acv2.UTC_SET_IDX]) + ")");
//
//			System.out.println("Rise Time Zone:" + rise.getTimeZone());
//		}

		AstroComputerV2.EpochAndZ[] epochAndZs = acv2.sunRiseAndSetEpoch(lat, lng);


		// Get Equation of time, used to calculate solar time.
		double eot = acv2.getSunMeridianPassageTime(lat, lng); // in decimal hours

//		return new BodyDataForPos(current.getTimeInMillis(), lat, lng, "Sun")
//				.decl(sunDecl)
//				.gha(sunGHA)
//				.altitude(he)
//				.z(z)
//				.eot(eot)
//				.riseTime(rise.getTimeInMillis())
//				.setTime(set.getTimeInMillis())
//				.setTransit(sunTransitTime)
//				.riseZ(sunRiseAndSet[acv2.RISE_Z_IDX])
//				.setZ(sunRiseAndSet[acv2.SET_Z_IDX]);
		return new BodyDataForPos(current.getTimeInMillis(), lat, lng, "Sun")
				.decl(sunDecl)
				.gha(sunGHA)
				.altitude(he)
				.z(z)
				.eot(eot)
				.riseTime(epochAndZs[0].getEpoch())
				.setTime(epochAndZs[1].getEpoch())
				.setTransit(sunTransitTime)
				.riseZ(epochAndZs[0].getZ())
				.setZ(epochAndZs[1].getZ());
	}

	private static class BodyAt {
		long epoch;
		double alt;
		double z;
		public BodyAt(long epoch, double alt, double z) {
			this.epoch = epoch;
			this.alt = alt;
			this.z = z;
		}
	}

	private List<BodyAt> getSunDataForAllDay(double lat, double lng, Integer step) {
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		return getSunDataForAllDay(lat, lng, step, now);
	}

	private List<BodyAt> getSunDataForAllDay(double lat, double lng, Integer step, Calendar today) {
		BodyDataForPos bodyData = getSunDataForDate(lat, lng, today);
		AstroComputerV2 acv2 = new AstroComputerV2();

		long from = bodyData.riseTime; // if sunTransitTime exists: from (transit-time - 12) to (transit-time + 12) ?
		long to = bodyData.setTime;

		if (bodyData.sunTransitTime != 0L) { // Parameter for the full path, or just positive elevations?
			from = bodyData.sunTransitTime - (12 * 3_600_000);
			to = bodyData.sunTransitTime + (12 * 3_600_000);
		}

		long _STEP_MINUTES = 1_000 * 60 * (step == null ? 10 : step); // In ms. Default 10 minutes.

		List<BodyAt> posList = new ArrayList<>();

		for (long time=from; time<=to; time += _STEP_MINUTES) {

			Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
			current.setTimeInMillis(time);

			acv2.setDateTime(current.get(Calendar.YEAR),
					current.get(Calendar.MONTH) + 1,
					current.get(Calendar.DATE),
					current.get(Calendar.HOUR_OF_DAY),
					current.get(Calendar.MINUTE),
					current.get(Calendar.SECOND));
			acv2.calculate();
			SightReductionUtil sru = new SightReductionUtil(acv2.getSunGHA(),
					acv2.getSunDecl(),
					lat,
					lng);
			sru.calculate();
			double he = sru.getHe().doubleValue();
			double z = sru.getZ().doubleValue();

//		System.out.println("Calculating Sun Data at "+ current.getTime() +
//					" from " + GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS, GeomUtil.LEADING_SIGN) +
//					" / " + GeomUtil.decToSex(lng, GeomUtil.SWING, GeomUtil.EW, GeomUtil.LEADING_SIGN) +
//			    ", Alt:" + he + ", Z:" + z);

			posList.add(new BodyAt(time, he, z));
		}
		return posList;
	}

	public enum AlmanacType {
		CONTINUOUS("continuous"),
		FROM_TO("from-to");

		private String type;

		AlmanacType(String type) {
			this.type = type;
		}

		public String type() {
			return this.type;
		}
	}

	private static class AlmanacOptions {
		AlmanacType type;
		String language;
		boolean withStars;

		int day;
		int month;
		int year;

		int fromYear;
		int toYear;
		int fromMonth;
		int toMonth;
		int fromDay;
		int toDay;
	}

	private static class PerpetualAlmanacOptions {
		int from;
		int to;
	}

	private static class DeclinationOptions {
		List<String> bodies;
		String from;
		String to;
	}
}
