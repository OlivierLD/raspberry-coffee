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

	public static boolean pathMatches(String pattern, String path) {
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

	public static List<String> getPrmValues(String pattern, String path) {
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


	/*** Utility(ies) ***/

	public static HTTPServer.Response defaultREST(HTTPServer.Request request) {
		return null;
	}

	public static void generateHappyResponseHeaders(HTTPServer.Response response, int contentLength) {
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
