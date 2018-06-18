package main;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.PinState;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
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

	private final static String PWS_PREFIX = "/pws";

	public RESTImplementation() {
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	public static class STH10Data {
		double temperature;
		double humidity;

		public STH10Data temperature(double t) {
			this.temperature = t;
			return this;
		}
		public STH10Data humidity(double h) {
			this.humidity = h;
			return this;
		}
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
					PWS_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations."),
			new Operation(
					"GET",
					PWS_PREFIX + "/sth10-data",
					this::getSTH10Data,
					"Get device Data. Temperature, humidity"),
			new Operation(
					"GET",
					PWS_PREFIX + "/relay-state",
					this::getRelayState,
					"Get relay state - ON of OFF."),
			new Operation(
					"GET",
					PWS_PREFIX + "/last-watering-time",
					this::getLastWateringTime,
					"Get last watering time as a long."),
			new Operation(
					"POST",
					PWS_PREFIX + "/sth10-data",
					this::setSTH10Data,
					"Set device Data. Temperature, humidity, for simulation"),
			new Operation(
					"PUT",
					PWS_PREFIX + "/relay-state",
					this::setRelayState,
					"Flip the relay - ON of OFF."));

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

	private Response getSTH10Data(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		STH10Data data = new STH10Data()
				.temperature(STH10.getTemperature())
				.humidity(STH10.getHumidity());
  	String content = new Gson().toJson(data);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/**
	 * Expects a payload like {
	 *     "temperature": 20.902205308538058,
	 *     "humidity": 58.56286683356113
	 * }
	 * @param request
	 * @return
	 */
	private Response setSTH10Data(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					STH10Data data = gson.fromJson(stringReader, STH10Data.class);
					STH10.setHumidity(data.humidity);
					STH10.setTemperature(data.temperature);
				} catch (Exception ex1) {
					ex1.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("PWS-0003")
									.errorMessage(ex1.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("PWS-0002")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("PWS-0001")
							.errorMessage("Request payload not found"));
			return response;
		}

		return response;
	}

	private Response getRelayState(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		PinState relayState = STH10.getRelayState();
		String content = new Gson().toJson(relayState);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private Response getLastWateringTime(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Long lastWateringTime = STH10.getLastWateringTime();
		String content = new Gson().toJson(lastWateringTime);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/**
	 * Expects payload "HIGH" or "LOW"
	 * @param request
	 * @return
	 */
	private Response setRelayState(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					String data = gson.fromJson(stringReader, String.class);
					PinState state = PinState.valueOf(data);
					STH10.setRelayState(state);
				} catch (Exception ex1) {
					ex1.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("PWS-0006")
									.errorMessage(ex1.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("PWS-0005")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("PWS-0004")
							.errorMessage("Request payload not found"));
			return response;
		}

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
