package weatherstation.logger.servers;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Very basic HTTP Server, just an example.
 * Could/Should eventually be replaced by the http.HTTPServer in common-utils.
 */
public class HTTPServer {
	private boolean verbose = "true".equals(System.getProperty("http.verbose", "false"));
	private String data;
	private int _port = 0;

	private long started = 0L;
	private List<Consumer<Request>> callbacks = null;

	private final static class HttpHeaders {
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String CONTENT_LENGTH = "Content-Length";
		public final static String USER_AGENT = "User-Agent";
		public final static String ACCEPT = "Accept";

		public final static String TEXT_PLAIN = "text/plain";
		public final static String TEXT_XML = "text/xml";
		public final static String APPLICATION_JSON = "application/json";
	}

	public HTTPServer() throws Exception {
		// Bind the server
		String port = "8080";

		port = System.getProperty("http.port", port);

		System.out.println("HTTP Port:" + port);

		JSONObject dummyPayload = new JSONObject( // Free sample
				"{ \"dir\": 350.0, \n" +
						"  \"avgdir\": 345.67,\n" +
						"  \"volts\": 3.4567,\n" +
						"  \"speed\": 12.345,\n" +
						"  \"gust\": 13.456,\n" +
						"  \"rain\": 0.1,\n" +
						"  \"press\": 101300.00,\n" +
						"  \"temp\": 18.34,\n" +
						"  \"hum\": 58.5,\n" +
						"  \"cputemp\": 34.56 }");
		this.data = dummyPayload.toString();

		try {
			_port = Integer.parseInt(port);
		} catch (NumberFormatException nfe) {
			throw nfe;
		}

		// Infinite loop, waiting for requests
		Thread httpListenerThread = new Thread("HTTPListener") {
			public void run() {
				boolean go = true;
				try {
					Map<String, String> header = new HashMap<>();
					ServerSocket ss = new ServerSocket(_port);
					System.out.println(">>>>>>>>>>>>>");
					System.out.println(">> Port " + _port + " opened successfully.");
					System.out.println(">>>>>>>>>>>>>");
					while (go) {
						if (verbose) {
							System.out.println(">>> Top of the loop");
						}
						Socket client = ss.accept();
						if (verbose) {
							System.out.println(">>> HTTP Server, socket connection accepted");
						}
						Request request = null;
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
						String line;
						while ((line = in.readLine()) != null) {
							if (verbose) {
								System.out.println(">>> HTTP Request:" + line);
							}
							if (line.length() == 0) {
								break;
							} else {
								String firstWord = line.substring(0, line.indexOf(" "));
								if (Request.VERBS.contains(firstWord)) { // Start Line
									String[] requestElements = line.split(" ");
									request = new Request(requestElements[0], requestElements[1], requestElements[2]);
								}

								if (line.startsWith("POST /exit") || line.startsWith("GET /exit")) {
									System.out.println(">>> HTTP Server, received an exit signal");
									go = false;
								} else if (line.startsWith("GET / ") || line.startsWith("GET /all ")) {
									// Usual case, reply.
									System.out.println(">>> HTTP Server, received data request <<<");
								}
							}
							//          System.out.println("Read:[" + line + "]");
							if (line.indexOf(":") > -1) { // Header?
								String headerKey = line.substring(0, line.indexOf(":"));
								String headerValue = line.substring(line.indexOf(":") + 1);
								header.put(headerKey, headerValue);
							}
						}
						if (verbose) {
							System.out.println(">>> HTTP Server, Request has been read.");
						}

						if (callbacks != null) {
							if (request != null) {
								request.setHeaders(header);
							}
							final Request _request = request;
							callbacks.forEach(listener -> {
								listener.accept(_request);
							});
						}

						String contentType = HttpHeaders.TEXT_PLAIN;
						String content = "exit";
						if (go) {
							if (("/all".equals(request.getPath()) || "/".equals(request.getPath())) && "GET".equals(request.getVerb())) {
								contentType = HttpHeaders.APPLICATION_JSON;
								content = (generateContent());
							} else {
								content = ""; // Duh...
							}
						}
						if (true || content.length() > 0) {
							// Headers?
							out.print("HTTP/1.1 200 \r\n");
							out.print("Content-Type: " + contentType + "\r\n");
							out.print("Content-Length: " + content.length() + "\r\n");
							out.print("Access-Control-Allow-Origin: *\r\n");
							// Time running (since started)
							long now = System.currentTimeMillis();
							out.print("Time-Up-ms:" + String.valueOf(now - started) + "\r\n"); // Custom Header
							out.print("\r\n"); // End Of Header
							//
							out.println(content);
							if (verbose) {
								System.out.println(">>> HTTP Server, content [" + content + "]");
							}
						}
						if (verbose) {
							System.out.println(">>> HTTP Server, pushing/flushing to client.");
						}
						out.flush();
						out.close();
						in.close();
						client.close();
						if (verbose) {
							System.out.println(">>> HTTP Server, loop bottom");
						}
					}
					if (verbose) {
						System.out.println(">>> HTTP Server, exiting");
					}
					ss.close();
				} catch (Exception e) {
					System.err.println(">>> Port " + _port + ", " + e.toString() + " >>>");
					e.printStackTrace();
					System.err.println("<<< Port " + _port + " <<<");
				} finally {
					System.out.println(">>> HTTP Server is done.");
				}
			}
		};
		if (verbose) {
			System.out.println(">>> HTTP Server, Starting listener thread");
		}
		httpListenerThread.start();
		if (verbose) {
			System.out.println(">>> HTTP Server, Listener thread started");
		}
		started = System.currentTimeMillis();
	}

	public static class Request {
		public final static List<String> VERBS = Arrays.asList(
				"GET",
				"POST",
				"DELETE",
				"PUT",
				"PATCH"
		);

		private String verb;
		private String path;
		private String protocol;
		private byte[] content;
		private Map<String, String> headers;
		private String requestPattern;

		private Map<String, String> queryStringParameters;

		public Request() {
		}

		public Request(String verb, String path, String protocol) {
			this.verb = verb;
			String[] pathAndQueryString = path.split("\\?");
			this.path = pathAndQueryString[0];
			if (pathAndQueryString.length > 1) {
				String[] nvPairs = pathAndQueryString[1].split("&");
				Arrays.asList(nvPairs).stream().forEach(nv -> {
					if (queryStringParameters == null) {
						queryStringParameters = new HashMap<>();
					}
					String[] nameValue = nv.split("=");
					queryStringParameters.put(nameValue[0], (nameValue.length > 1 ? nameValue[1] : null));
				});
			}
			this.protocol = protocol;
		}

		public String getResource() {
			return getVerb() + " " + getPath();
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

		public String getVerb() {
			return verb;
		}

		public String getPath() {
			return path;
		}

		public String getProtocol() {
			return protocol;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public Map<String, String> getQueryStringParameters() {
			return queryStringParameters;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public String getRequestPattern() {
			return requestPattern;
		}

		public void setRequestPattern(String requestPattern) {
			this.requestPattern = requestPattern;
		}

		@Override
		public String toString() {
			final StringBuffer string = new StringBuffer();
			string.append(this.verb + " " + this.path + " " + this.protocol);

			if (this.headers != null) {
				this.headers.keySet().stream()
						.forEach(k -> {
							string.append("\n" + k + ":" + this.headers.get(k));
						});
			}
			if (this.content != null) {
				string.append("\n\n" + new String(this.content));
			}

			return string.toString();
		}
	}

	public void addListener(Consumer<Request> listener) {
		if (callbacks == null) {
			callbacks = new ArrayList<>();
		}
		callbacks.add(listener);
	}

	public void setData(String str) {
		if (verbose) {
			System.out.println(">>> HTTP Server, setData:" + str);
		}
		this.data = str;
	}

	private String generateContent() {
		String str = this.data;
		if (verbose) {
			System.out.println(str);
		}
		return str;
	}

	//  For dev tests
	public static void main(String... args) throws Exception {
		//System.setProperty("http.port", "9999");
		new HTTPServer();
		Thread t = new Thread("HTTPWaiter") {
			public void run() {
				synchronized (this) {
					try {
						this.wait();
					} catch (Exception ex) {
					}
				}
			}
		};
		t.start();
	}
}
