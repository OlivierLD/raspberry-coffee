package astrorest;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

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
			new Operation(
					"GET",
					"/utc",
					this::getCurrentTime,
					"Get current UTC Date.")
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

		DateHolder dateHolder = new DateHolder()
				.epoch(now)
				.utcStr(utc)
				.sysStr(sys);
		String content = new Gson().toJson(dateHolder);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	public static class DateHolder {
		long epoch;
		String utcStr;
		String sysStr;

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
	}
}
