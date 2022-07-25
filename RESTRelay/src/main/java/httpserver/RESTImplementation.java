package httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import relay.RelayManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	private final static boolean verbose = "true".equals(System.getProperty("relay.verbose", "false"));
	private final static String RELAY_PREFIX = "/relay";

	private final RelayRequestManager relayRequestManager;
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
	private final List<Operation> operations = Arrays.asList(
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
					"Get the relay status"),
			new Operation(
					"GET",
					RELAY_PREFIX + "/relay-map",
					this::getRelayMap,
					"Returns the relay map (in json)."),
			new Operation(
					 "POST",
					RELAY_PREFIX + "/terminate",
					this::terminateManager,
					"Terminates the relayManager. Careful with that one.")
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
	 * @param request the REST Request
	 * @return REST Response
	 */
	private Response setRelayStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		List<String> pathParameters = request.getPathParameters();
		if (verbose) {
			List<String> pathPrmNames = request.getPathParameterNames();
			for (int i=0; i<pathPrmNames.size(); i++) {
				System.out.printf("%s = %s%n", pathPrmNames.get(i), pathParameters.get(i));
			}
		}

		String contentType = request.getHeaders().get("Content-Type");

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.printf("Tx Request: %s\n", payload);
				}
				RelayStatus relayStatus = null;
				if (contentType.trim().startsWith("multipart/form-data;")) {
					try {
						Map<String, Object> formDataParameters = RESTProcessorUtil.getFormDataParameters(contentType.trim(), request.getContent());
						String status = (String)formDataParameters.get("status");
						if (status != null) {
							relayStatus = new RelayStatus();
							relayStatus.status = status.equals("on");
						}
						// For tests
						byte[] imageData = (byte[])formDataParameters.get("importData");
						if (imageData != null) {
							try {
								ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
								BufferedImage bImage2 = ImageIO.read(bis);
								if (bImage2 != null) {
									ImageIO.write(bImage2, "jpg", new File("output.jpg"));
									System.out.println("image created");
								} else {
									System.out.println("Something went wrong with the image...");
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					} catch (InvalidParameterException ipe) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("RELAY-0006")
										.errorMessage(ipe.getMessage()));
						return response;

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
	 * @param request REST Request
	 * @return REST Response
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

	private Response getRelayMap(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (this.physicalRelayManager != null) {
			Map replayMap = this.physicalRelayManager.getRelayMap();
			String content = new Gson().toJson(replayMap);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("RELAY-0003")
							.errorMessage("Request Manager is null"));
			return response;
		}
		return response;
	}

	/**
	 * @param request REST Request
	 * @return REST Response
	 */
	private Response terminateManager(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		if (this.relayRequestManager != null) {
			this.relayRequestManager.shutdownRelayManager();
			String content = new Gson().toJson("OK");
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("RELAY-0003")
							.errorMessage("Request Manager is null"));
			return response;
		}

		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request REST Request
	 * @return REST Response
	 */
	private Response emptyOperation(Request request) {
		return new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
	}

	public static class RelayStatus {
		boolean status;
	}
}
