package httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import relay.RelayManager;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	private static boolean verbose = "true".equals(System.getProperty("relay.verbose", "false"));
	private final static String RELAY_PREFIX = "/relay";

	private RelayRequestManager relayRequestManager;
	private RelayManager physicalRelayManager = null;

	public RESTImplementation(RelayRequestManager restRequestManager) {

		this.relayRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	public void setRelayManager(RelayManager relayManager) {
		this.physicalRelayManager = relayManager;
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
					RELAY_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the Relay service."),
			new Operation(
					"POST",
					RELAY_PREFIX + "/status/{relay-id}",
					this::setRelayStatus,
					"Set the relay status, and return its json representation."),
			new Operation(
					"GET",
					RELAY_PREFIX + "/status/{relay-id}",
					this::getRelayStatus,
					"Get the relay status")
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
	 * The JSON payload is a requests like this
	 * {
	 *     "status": false
	 * }
	 * or form-data: status: on|off
	 *
	 * @param request
	 * @return
	 */
	private Response setRelayStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters();
		if (verbose) {
			List<String> pathPrmNames = request.getPathParameterNames();
			for (int i=0; i<pathPrmNames.size(); i++) {
				System.out.println(String.format("%s = %s", pathPrmNames.get(i), pathParameters.get(i)));
			}
		}

		String contentType = request.getHeaders().get("Content-Type");

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("Tx Request: %s", payload));
				}
				RelayStatus relayStatus = null;
				if (contentType.trim().startsWith("multipart/form-data;")) {
/*
 formData would look like this:
----------------------------690508146199201755172091
Content-Disposition: form-data; name="status"

on
----------------------------690508146199201755172091--
*/
					String separator = contentType.substring("multipart/form-data;".length()).split("=")[1]; // The part after 'boundary='
					String[] formPayloadElements = payload.split(separator + "\r\n");
					for (String oneFormParam : formPayloadElements) {
						String[] paramElements = oneFormParam.split("\r\n");
						if (paramElements.length > 1) {
							if (paramElements[0].contains("form-data; name=\"status\"")) {
								String value = paramElements[2];
								relayStatus = new RelayStatus();
								relayStatus.status = value.equals("on");
							}
						}
					}
				} else { // assume application/json
					Gson gson = new GsonBuilder().create();
					StringReader stringReader = new StringReader(payload);
					try {
						relayStatus = gson.fromJson(stringReader, RelayStatus.class);
					} catch (Exception ex) {
						ex.printStackTrace();
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("RELAY-0004")
										.errorMessage(ex.toString()));
						return response;
					}
				}
				if (relayStatus == null) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("RELAY-0005")
									.errorMessage("No status found, no json payload, no form-data..."));
					return response;
				}
				try {
					int relayNum = Integer.parseInt(pathParameters.get(0));
					// Set Relay status here
			//	System.out.println(String.format("Setting relay #%d %s", relayNum, (relayStatus.status ? "ON" : "OFF")));
					if (this.physicalRelayManager != null) {
						this.physicalRelayManager.set(relayNum, (relayStatus.status ? "on" : "off")); // TODO an enum
					}
					String content = new Gson().toJson(relayStatus);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("RELAY-0003")
									.errorMessage(ex1.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("RELAY-0002")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("RELAY-0002")
							.errorMessage("Request payload not found"));
			return response;
		}

		return response;
	}

 	/**
	 * For dev.
	 * @param request
	 * @return
	 */
	private Response getRelayStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathParameters = request.getPathParameters();

		RelayStatus rs = new RelayStatus();
		// Get status here
		try {
			int relayNum = Integer.parseInt(pathParameters.get(0));
			boolean onOff = this.physicalRelayManager.get(relayNum);
			rs.status = onOff;
			String content = new Gson().toJson(rs);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex1) {
			ex1.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("RELAY-0004")
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

	public static class RelayStatus {
		boolean status;
	}
}
