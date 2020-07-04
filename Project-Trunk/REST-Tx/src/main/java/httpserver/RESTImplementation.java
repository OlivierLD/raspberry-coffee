package httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import http.HttpHeaders;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import oracle.xml.util.XMLException;
import org.xml.sax.SAXException;
import utils.XMLUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
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

	private static boolean verbose = "true".equals(System.getProperty("server.verbose", "false"));
	private final static String SERVER_PREFIX = "/server";

	private HttpRequestManager httpRequestManager;

	public RESTImplementation(HttpRequestManager restRequestManager) {

		this.httpRequestManager = restRequestManager;
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
					SERVER_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on this service."),
			new Operation(
					"POST",
					SERVER_PREFIX + "/schema-process",
					this::processSchema,
					"Upload Schema, WiP."),
			new Operation(
					"POST",
					SERVER_PREFIX + "/xml-to-json",
					this::xmlToJson,
					"XML to JSON WiP."),
			new Operation(
					"POST",
					SERVER_PREFIX + "/xml-xsl",
					this::xslt,
					"XML and XSL WiP."),
			new Operation(
					"POST",
					SERVER_PREFIX + "/duh",
					this::emptyOperation,
					"PlaceHolder.")
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
	 * WIP
	 *
	 * @param request
	 * @return
	 */
	private Response processSchema(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		response.setHeaders(new HashMap<>());

		Map<String, String> requestHeaders = request.getHeaders();
		System.out.println(">>> ---- Headers ----");
		requestHeaders.keySet()
				.forEach(headerKey -> System.out.println(String.format("%s: [%s]", headerKey, requestHeaders.get(headerKey))));
		System.out.println("<<< -----------------");

		String contentType = requestHeaders.get(HttpHeaders.CONTENT_TYPE);
		if (contentType == null) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload("Content-type header MUST be provided".getBytes());
			return response;
		}
		contentType = contentType.trim();
		if (!HttpHeaders.TEXT_XML.equals(contentType)) {
			response.setStatus(Response.NOT_IMPLEMENTED);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(String.format("Content-type [%s] not supported (yet)", contentType).getBytes());
			return response;
		}
		// Now proceed
		byte[] content = request.getContent();
		// System.out.println(String.format("Content: len:%d byte(s), %s", content.length, new String(content)));

		try {
			Map<String, Object> map = XMLUtils.processSchema(content);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
			JsonElement jsonElement = new Gson().toJsonTree(map);
			String jsonContent = jsonElement.toString();
			response.getHeaders().put(HttpHeaders.CONTENT_LENGTH, Integer.toString(jsonContent.getBytes().length));
			response.setPayload(jsonContent.getBytes());
		} catch (IOException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (XMLException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (SAXException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		}
		return response;
	}

	private Response xmlToJson(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		response.setHeaders(new HashMap<>());

		Map<String, String> requestHeaders = request.getHeaders();
		System.out.println(">>> ---- Headers ----");
		requestHeaders.keySet()
				.forEach(headerKey -> System.out.println(String.format("%s: [%s]", headerKey, requestHeaders.get(headerKey))));
		System.out.println("<<< -----------------");

		String contentType = requestHeaders.get(HttpHeaders.CONTENT_TYPE);
		if (contentType == null) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload("Content-type header MUST be provided".getBytes());
			return response;
		}
		contentType = contentType.trim();
		if (!HttpHeaders.TEXT_XML.equals(contentType)) {
			response.setStatus(Response.NOT_IMPLEMENTED);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(String.format("Content-type [%s] not supported (yet)", contentType).getBytes());
			return response;
		}
		// Now proceed
		byte[] content = request.getContent();
		// System.out.println(String.format("Content: len:%d byte(s), %s", content.length, new String(content)));

		try {
			byte[] transformed = XMLUtils.applyStylesheet(content);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);

			JsonObject jsonObject = new JsonParser().parse(new String(transformed)).getAsJsonObject();
			String jsonContent = jsonObject.toString();
			response.getHeaders().put(HttpHeaders.CONTENT_LENGTH, Integer.toString(jsonContent.getBytes().length));
			response.setPayload(jsonContent.getBytes());
		} catch (IOException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (XMLException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (SAXException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		}
		return response;
	}

	private Response xslt(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		response.setHeaders(new HashMap<>());

		Map<String, String> requestHeaders = request.getHeaders();
		System.out.println(">>> ---- Headers ----");
		requestHeaders.keySet()
				.forEach(headerKey -> System.out.println(String.format("%s: [%s]", headerKey, requestHeaders.get(headerKey))));
		System.out.println("<<< -----------------");

		String contentType = requestHeaders.get(HttpHeaders.CONTENT_TYPE);
		if (contentType == null) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload("Content-type header MUST be provided".getBytes());
			return response;
		}
		contentType = contentType.trim();
		if (!HttpHeaders.TEXT_XML.equals(contentType)) {
			response.setStatus(Response.NOT_IMPLEMENTED);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(String.format("Content-type [%s] not supported (yet)", contentType).getBytes());
			return response;
		}
		// Now proceed
		byte[] content = request.getContent();
		// System.out.println(String.format("Content: len:%d byte(s), %s", content.length, new String(content)));
		JsonObject inputJsonObject = new JsonParser().parse(new String(content)).getAsJsonObject();

		try {
			byte[] transformed = XMLUtils.processStylesheet(inputJsonObject.get("xml").getAsString().getBytes(), inputJsonObject.get("xsl").getAsString().getBytes());
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, "plain/text");
			response.getHeaders().put(HttpHeaders.CONTENT_LENGTH, Integer.toString(transformed.length));
			response.setPayload(transformed);
		} catch (IOException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (XMLException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
			return response;
		} catch (SAXException e) {
			response.setStatus(Response.BAD_REQUEST);
			response.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_PLAIN);
			response.setPayload(e.toString().getBytes());
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

}
