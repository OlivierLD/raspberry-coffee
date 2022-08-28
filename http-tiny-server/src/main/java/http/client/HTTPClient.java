package http.client;

import http.HTTPServer;
import http.HttpHeaders;
import utils.StaticUtil;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very limited Java HTTP Client, just suitable for what is
 * required - so far - in this project.
 */
public class HTTPClient {

	private final static boolean DEBUG = "true".equals(System.getProperty("http.client.verbose", "false"));

	public static HTTPServer.Response doRequest(HTTPServer.Request request) throws Exception {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		URL url = new URL(request.getPath(true));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(request.getVerb());
		for (String h : request.getHeaders().keySet()) {
			conn.setRequestProperty(h, request.getHeaders().get(h));
		}
		conn.setUseCaches(false);
		if (!"GET".equals(request.getVerb())) { // TODO May need tweaks...
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(request.getContent());
			os.flush();
			os.close();
		}
		response.setStatus(conn.getResponseCode());

		Map<String, List<String>> headerFields = conn.getHeaderFields();
		Map<String, String> map = new HashMap<>();
		headerFields.keySet().forEach(key -> {
			if (key != null) { // Case of the HTTP/1.1 200 OK, for example.
				map.put(key, String.join(",", headerFields.get(key)));
			}
		});
		response.setHeaders(map);

		// Response payload
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		StringBuilder sb = new StringBuilder();
		String output;
		if (DEBUG) {
			System.out.println("Output from Server .... \n");
		}
		while ((output = br.readLine()) != null) {
			if (DEBUG) {
				System.out.println(output);
			}
			sb.append(output);
		}
		response.setPayload(sb.toString().getBytes());
		conn.disconnect();

		return response;
	}

	/**
	 * Simplified version for GET. Use {@link #doRequest(HTTPServer.Request)} for a more exhaustive implementation.
	 * @param urlStr The full URL, like httpX://machine:port/the/path?query=string
	 * @param headers Map of headers
	 * @return the returned payload
	 * @throws Exception Oops
	 */
	public static String doGet(String urlStr, Map<String, String> headers) throws Exception {
		int responseCode = 0;
		String getContent = "";
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (headers != null) {
				for (String h : headers.keySet()) {
					conn.setRequestProperty(h, headers.get(h));
				}
			}
			conn.setUseCaches(false);
			responseCode = conn.getResponseCode();

			if (DEBUG) {
				System.out.println("Done. (" + responseCode + ")");
			}

			InputStream inputStream = conn.getInputStream();
			byte[] aByte = new byte[2];

			byte[] content = null;
			int nbLoop = 1;
			long started = System.currentTimeMillis();

			while (inputStream.read(aByte, 0, 1) != -1) {
				content = StaticUtil.appendByte(content, aByte[0]);
				if (content.length > (nbLoop * 1_000)) {
					long now = System.currentTimeMillis();
					long delta = now - started;
					double rate = (double) content.length / ((double) delta / 1_000D);
					if (DEBUG) {
						System.out.println(String.format("Downloading at %.02f bytes per second.", rate));
					}
					nbLoop++;
				}
			}
			conn.disconnect();
			getContent = new String(content);
		} catch (EOFException eofe) {
			System.out.println("EOFException"); // That's ok, nothing is returned
			eofe.printStackTrace();
			throw eofe;
		} catch (SocketException se) {
//			System.out.println("SocketException in HTTPClient"); // OK too.
//			se.printStackTrace();
			throw se;
		} catch (Exception ex) {
//			ex.printStackTrace();
			throw ex;
		}
		return getContent;
	}

	/**
	 * Simplified version for POST. Use {@link #doRequest(HTTPServer.Request)} for a more exhaustive implementation.
	 * @param urlStr The full URL, like httpX://machine:port/the/path?query=string
	 * @param headers Map of headers
	 * @param payload The payload, as a String
	 * @return the simplified HTTPResponse (in this class, NOT HTTPServer.Response)
	 * @throws Exception Oops
	 */
	public static HTTPResponse doPost(String urlStr, Map<String, String> headers, String payload) throws Exception {
		int responseCode = 0;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//  String userCredentials = "username:password";
//  String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
//  conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestMethod("POST");
		for (String h : headers.keySet()) {
			conn.setRequestProperty(h, headers.get(h));
		}
		if (headers == null || (headers != null && !headers.containsKey(HttpHeaders.CONTENT_TYPE))) {
			conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON); // Uhu ?
		}
		if (payload != null) {
			conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.getBytes().length));
		}
		// conn.setRequestProperty("Content-Language", "en-US");
		conn.setUseCaches(false);

		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(payload != null ? payload.getBytes() : "".getBytes());
		os.flush();
		os.close();

		responseCode = conn.getResponseCode();

		HTTPResponse response = new HTTPResponse();
		response.code = responseCode;

		if (true) { // Ben oui tiens!
			// Response payload
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String output;
			if (DEBUG) {
				System.out.println("Output from Server .... \n");
			}
			while ((output = br.readLine()) != null) {
				if (DEBUG) {
					System.out.println(output);
				}
				sb.append(output);
			}
			response.response = sb.toString();
		}
		conn.disconnect();

		return response;
	}

	/**
	 * Simplified version for PUT. Use {@link #doRequest(HTTPServer.Request)} for a more exhaustive implementation.
	 * @param urlStr The full URL, like httpX://machine:port/the/path?query=string
	 * @param headers Map of headers
	 * @param payload The payload, as a String
	 * @return the simplified HTTPResponse (in this class, NOT HTTPServer.Response)
	 * @throws Exception Oops
	 */
	public static HTTPResponse doPut(String urlStr, Map<String, String> headers, String payload) throws Exception {
		int responseCode = 0;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//  String userCredentials = "username:password";
//  String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
//  conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestMethod("PUT");
		headers.keySet().forEach(k -> conn.setRequestProperty(k, headers.get(k)));
		conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON); // Uhu ?
		if (payload != null) {
			conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.getBytes().length));
		}
		// conn.setRequestProperty("Content-Language", "en-US");
		conn.setUseCaches(false);

		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(payload != null ? payload.getBytes() : "".getBytes());
		os.flush();
		os.close();

		responseCode = conn.getResponseCode();

		HTTPResponse response = new HTTPResponse();
		response.code = responseCode;

		if (true) { // Ben oui tiens!
			// Response payload
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String output;
			if (DEBUG) {
				System.out.println("Output from Server .... \n");
			}
			while ((output = br.readLine()) != null) {
				if (DEBUG) {
					System.out.println(output);
				}
				sb.append(output);
			}
			response.response = sb.toString();
		}
		conn.disconnect();

		return response;
	}

	// This should NOT work. This is for experiment.
	public static int doCustomVerb(String verb, String urlStr, Map<String, String> headers, String payload) throws Exception {
		int responseCode = 0;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//  String userCredentials = "username:password";
//  String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
//  conn.setRequestProperty ("Authorization", basicAuth);
		conn.setRequestMethod(verb);
		for (String h : headers.keySet()) {
			conn.setRequestProperty(h, headers.get(h));
		}
		if (payload != null) {
			conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, HttpHeaders.APPLICATION_JSON);
			conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(payload.getBytes().length));
			// conn.setRequestProperty("Content-Language", "en-US");
			conn.setUseCaches(false);

			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(payload.getBytes());
			os.flush();
			os.close();
		}

		responseCode = conn.getResponseCode();

		return responseCode;
	}

	public static String getContent(String url) throws Exception {
		String ret;
		try {
			byte[] content = readURL(new URL(url));
			ret = new String(content);
		} catch (Exception e) {
			throw e;
		}
		return ret;
	}

	private static byte[] readURL(URL url) throws Exception {
		byte[] content = null;
		try {
			URLConnection newURLConn = url.openConnection();
			InputStream inputStream = newURLConn.getInputStream();
			byte[] aByte = new byte[2];
			long started = System.currentTimeMillis();
			int nbLoop = 1;
			while (inputStream.read(aByte, 0, 1) != -1) {
				content = StaticUtil.appendByte(content, aByte[0]);
				if (content.length > (nbLoop * 1_000)) {
					long now = System.currentTimeMillis();
					long delta = now - started;
					double rate = (double) content.length / ((double) delta / 1_000D);
					System.out.println("Downloading at " + rate + " bytes per second.");
					nbLoop++;
				}
			}
		} catch (IOException e) {
			if (DEBUG) {
				System.err.println("ReadURL for " + url.toString() + ", newURLConn failed :\n" + e.toString());
			}
			throw e;
		} catch (Exception e) {
			System.err.println("Exception for: " + url.toString());
			throw e;
		}
		return content;
	}

	public static class HTTPResponse {
		int code;
		String response;

		public int getCode() {
			return this.code;
		}
		public String getPayload() {
			return this.response;
		}
	}
}
