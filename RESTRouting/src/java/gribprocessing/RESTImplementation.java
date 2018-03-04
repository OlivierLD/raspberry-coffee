package gribprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gribprocessing.utils.GRIBUtils;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import jgrib.GribFile;
import poc.GRIBDump;
import poc.data.GribDate;
import poc.data.GribType;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * The SunFlower will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));
	private final static String GRIB_PREFIX = "/grib";

	private GRIBRequestManager gribRequestManager;

	public RESTImplementation(GRIBRequestManager restRequestManager) {

		this.gribRequestManager = restRequestManager;
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
					GRIB_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the Img service."),
			new Operation(
					"POST",
					GRIB_PREFIX + "/get-data",
					this::requestGRIBData,
					"Request a GRIB download from the web, and return its json representation."));

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
	 * The payload is a list of requests, like this
	 *
	 * {
	 *   "request": "GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN"
	 * }
	 *
	 * @param request
	 * @return
	 */
	private Response requestGRIBData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("Tx Request: %s", payload));
				}
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					GRIBRequest gribRequest = gson.fromJson(stringReader, GRIBRequest.class);
					URL gribURL = null;
					GRIBDump dump = new GRIBDump();
					if (gribRequest.request.startsWith("file:")) { // Reusing grib file
						gribURL = new URI(gribRequest.request).toURL();
					} else {
						try {
							String dir = gribRequest.directory;
							if (dir == null) {
								dir = ".";
							}
							File location = new File(dir);
							if (!location.exists()) {
								boolean ok = location.mkdirs();
								System.out.println(String.format("Created directory(ies) %s:", dir) + ok);
							}
							String gribFileName = "grib.grb";
							System.out.println(String.format(" >> Will pull %s into %s", gribFileName, dir));
							GRIBUtils.getGRIB(GRIBUtils.generateGRIBRequest(gribRequest.request), dir, gribFileName, true);
							gribURL = new File(dir, gribFileName).toURI().toURL();
						} catch (Exception ex) {
							ex.printStackTrace();
							response = HTTPServer.buildErrorResponse(response,
									Response.BAD_REQUEST,
									new HTTPServer.ErrorPayload()
											.errorCode("GRIB-0004")
											.errorMessage(ex.toString()));
							return response;
						}
					}

					GribFile gf = new GribFile(gribURL.openStream());
					List<GRIBDump.DatedGRIB> expandedGBRIB = dump.getExpandedGBRIB(gf);
					String content = new Gson().toJson(expandedGBRIB);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("GRIB-0003")
									.errorMessage(ex1.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("GRIB-0002")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0002")
							.errorMessage("Request payload not found"));
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
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}

	public static class GRIBRequest {
		String request;
		String directory;
	}
}
