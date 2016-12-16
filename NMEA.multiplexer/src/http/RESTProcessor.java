package http;

import com.google.gson.Gson;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RESTProcessor {

	private static boolean pathMatches(String pattern, String path) {
		boolean match = true;
		String[] patternElem = pattern.split("/");
		String[] pathElem = path.split("/");

		if (patternElem.length == pathElem.length) {
			for (int i=0; match && i<patternElem.length; i++) {
				if (patternElem[i].startsWith("{") && patternElem[i].endsWith("}")) {
					match = true;
				} else {
					match = patternElem[i].equals(pathElem[i]);
				}
			}
		} else {
			match = false;
		}
		return match;
	}

	private static List<String> getPrmValues(String pattern, String path) {
		List<String> returned = new ArrayList<>();
		String[] patternElem = pattern.split("/");
		String[] pathElem = path.split("/");

		if (patternElem.length == pathElem.length) {
			for (int i = 0; i < patternElem.length; i++) {
				if (patternElem[i].startsWith("{") && patternElem[i].endsWith("}")) {
					returned.add(pathElem[i]);
				}
			}
		}
		return returned;
	}

	enum ResourceProcessor {
		GetOpList(
						"GET",
						"/oplist",
						RESTProcessor::defaultREST, // TODO Make sure it matches the oplist.
						"List of all available operations."),
		GetSerialPorts(
						"GET",
						"/serial-ports",
						RESTProcessor::getSerialPorts,
						"Get the list of the available serial ports."),
		GetChannelList(
						"GET",
						"/channels",
						null,
						""),

		RESOURCE_TWO   ("POST",   "/one/{x}/two/{y}", RESTProcessor::defaultREST, ""),
		RESOURCE_THREE ("PUT",    "/one/{x}/two/{y}", RESTProcessor::defaultREST, ""),
		RESOURCE_FOUR  ("DELETE", "/first/{prm}",     RESTProcessor::defaultREST, "");

		private final String verb;
		private final String path;
		private final Function<HTTPServer.Request, HTTPServer.Response> fn;
		private final String description;

		ResourceProcessor(String verb,
		                  String path,
		                  Function<HTTPServer.Request, HTTPServer.Response> fn,
		                  String description) {
			this.verb = verb;
			this.path = path;
			this.fn = fn;
			this.description = description;
		}

		public String verb() { return this.verb; }
		public String path() { return this.path; }
		public Function<HTTPServer.Request, HTTPServer.Response> fn() { return this.fn; }
		public String description() { return this.description; }
	}

	private static HTTPServer.Response defaultREST(HTTPServer.Request request) {
		return null;
	}

	public static HTTPServer.Response processRequest(HTTPServer.Request request, HTTPServer.Response defaultResponse) {

		for (ResourceProcessor rp : ResourceProcessor.values()) {
			if (rp.verb().equals(request.getVerb()) && pathMatches(rp.path(), request.getPath())) {
				HTTPServer.Response processed = rp.fn().apply(request);
				return processed;
			}
		}
		return defaultResponse;
	}

	private static HTTPServer.Response getSerialPorts(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), 200);

		List<String> portList = getSerialPortList();
		Object[] portArray = portList.toArray(new Object[portList.size()]);
		String content = new Gson().toJson(portArray).toString();
		generateHappyResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/*** Utility(ies) ***/
	private static void generateHappyResponseHeaders(HTTPServer.Response response, int contentLength) {
		Map<String, String> responseHeaders = new HashMap<>();
		responseHeaders.put("Content-Type", "application/json");
		responseHeaders.put("Content-Length", String.valueOf(contentLength));
		responseHeaders.put("Access-Control-Allow-Origin", "*");
		response.setHeaders(responseHeaders);
	}

	/*** Implementation methods, they do the actual job ***/

	private static List<String> getSerialPortList() {
		List<String> portList = new ArrayList<>();
		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			portList.add(cpi.getName());
		}
		return portList;
	}
}
