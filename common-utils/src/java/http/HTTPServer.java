package http;

import com.google.gson.Gson;
import http.client.HTTPClient;
import utils.DumpUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Used for the REST interface of an HTTP Server.
 *
 * GET, POST, DELETE, PUT, no PATCH (for now)
 * <br>
 * Also serves as a regular HTTP server for static documents (in the /web directory).
 * <br>
 * Has two static resources:
 * <ul>
 * <li><code>/exit</code> to exit the HTTP server (cannot be restarted).</li>
 * <li><code>/test</code> to test the HTTP server availability</li>
 * </ul>
 * <p>
 * Query parameter 'verbose' will turn verbose on or off.
 * To turn it on: give verbose no value, or 'on', 'true', 'yes' (non case sensitive).
 * To turn it off: any other value.
 * <br>
 * Example: <code>http://localhost:9999/web/admin.html?verbose=on</code>
 * <br>
 * <em>
 * Warning: This is a <b>very lightweight</b> HTTP server. It is not supposed to scale!!
 * </em>
 * </p>
 * <p>
 * Logging can be done. See <code>-Djava.util.logging.config.file=[path]/logging.properties</code>
 * <br>
 * See <a href="https://docs.oracle.com/cd/E23549_01/doc.1111/e14568/handler.htm">https://docs.oracle.com/cd/E23549_01/doc.1111/e14568/handler.htm</a>
 * </p>
 */
public class HTTPServer {
	private boolean verbose = "true".equals(System.getProperty("http.verbose", "false"));
	private int port = -1;

	private Thread httpListenerThread;

	public static class Request {
		public final static List<String> VERBS = Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH");

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
			String[] pathAndQuesryString = path.split("\\?");
			this.path = pathAndQuesryString[0];
			if (pathAndQuesryString.length > 1) {
				String[] nvPairs = pathAndQuesryString[1].split("&");
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

	public static class Response {

		public final static int STATUS_OK       = 200;
		public final static int NOT_IMPLEMENTED = 501;
		public final static int NO_CONTENT      = 204;
		public final static int BAD_REQUEST     = 400;
		public final static int NOT_FOUND       = 404;
		public final static int TIMEOUT         = 408;

		private int status;
		private String protocol;
		private Map<String, String> headers;
		private byte[] payload;

		public Response() {
		}

		public Response(String protocol, int status) {
			this.protocol = protocol;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getProtocol() {
			return protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public byte[] getPayload() {
			return payload;
		}

		public void setPayload(byte[] payload) {
			this.payload = payload;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer();
			sb.append(this.status + " " + this.protocol);

			if (this.headers != null) {
				this.headers.keySet().stream()
								.forEach(k -> {
									sb.append("\n" + k + ":" + this.headers.get(k));
								});
			}
			if (this.payload != null) {
				sb.append("\n\n" + new String(this.payload));
			}
			return sb.toString();
		}
	}

	/**
	 * Used for REST. See usages of this class.
	 *
	 * Note: Parameters (query string and body payloads) are defined and managed at he operation implementation (fn) level.
	 * We are not re-writing Swagger ;)
	 *
	 */
	public static class Operation {
		String verb;
		String path;
		String description;
		Function<Request, Response> fn;

		/**
		 *
		 * @param verb GET, PUT, POST, or DELETE
		 * @param path can include {parameters}
		 * @param fn
		 * @param description
		 */
		public Operation(String verb, String path, Function<HTTPServer.Request, HTTPServer.Response> fn, String description) {
			this.verb = verb;
			this.path = path;
			this.description = description;
			this.fn = fn;
		}

		public String getVerb() {
			return verb;
		}

		public String getPath() {
			return path;
		}

		public String getDescription() {
			return description;
		}

		public Function<HTTPServer.Request, HTTPServer.Response> getFn() {
			return fn;
		}
	}

	public static class ErrorPayload {
		String errorCode;
		String errorMessage;

		public ErrorPayload errorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public ErrorPayload errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}
	}

	public static Response buildErrorResponse(Response response, int httpStatus, ErrorPayload payload) {
		response.setStatus(httpStatus);
		String content = new Gson().toJson(payload);
		RESTProcessorUtil.generateResponseHeaders(response, "application/json", content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	public boolean isRunning() {
		return keepRunning;
	}

	public void stopRunning() {
		if (verbose) {
			HTTPContext.getInstance().getLogger().info("Stop nicely (HTTP) requested");
		}
		this.keepRunning = false;
	}

	private boolean keepRunning = true;
	private List<String> staticDocumentsLocation = null;
	private static final String DEFAULT_RESOURCE = "index.html";

	// This is an array, so several apps can subscribe to the same HTTPServer.
	// A REST operation list belongs to each application.
	// In this case, the HTTPServer should probably live in a singleton.
	private List<RESTRequestManager> requestManagers = null;

	public List<RESTRequestManager> getRequestManagers() {
		return this.requestManagers;
	}

	public void addRequestManager(RESTRequestManager requestManager) {
		if (requestManager != null) {
			if (requestManagers == null) {
				requestManagers = new ArrayList<>(1);
			}
			/*
			 * Make sure no operation is duplicated across request managers.
			 * We assume that there is no duplicate in each operation list.
			 */
			if (requestManagers.size() > 0) {
				for (RESTRequestManager reqMgr : requestManagers) {
					List<Operation> opList = reqMgr.getRESTOperationList();
					List<Operation> dups = opList.stream()
							.filter(op -> requestManager.getRESTOperationList().stream()
									.filter(newOp -> (newOp.getVerb().equals(op.getVerb()) &&
											              RESTProcessorUtil.pathsAreIndentical(newOp.getPath(), op.getPath())))
									.collect(Collectors.counting()) > 0)
							.collect(Collectors.toList());
					if (dups.size() > 0) {
						String duplicates = dups.stream()
								.map(op -> op.getVerb() + " " + op.getPath())
								.collect(Collectors.joining(", "));
						throw new IllegalArgumentException(String.format("Duplicate operation%s across request managers [%s]", (dups.size() == 1 ? "" : "s"), duplicates));
					}
				}
			}
			requestManagers.add(requestManager);
		}
	}

	public void removeRequestManager(RESTRequestManager requestManager) {
		if (requestManagers.contains(requestManager)) {
			requestManagers.remove(requestManager);
		}
	}

	private static int defaultPort = 9999;

	public int getPort() {
		return this.port;
	}

	public HTTPServer() throws Exception {
		this(defaultPort, null, new Properties());
	}

	public HTTPServer(int port) throws Exception {
		this(port, null, new Properties());
	}

	public HTTPServer(Properties properties) throws Exception {
		this(defaultPort, null, properties);
	}

	public HTTPServer(RESTRequestManager requestManager) throws Exception {
		this(defaultPort, requestManager, new Properties());
	}

	public HTTPServer(int port, RESTRequestManager requestManager) throws Exception {
		this(port, requestManager, new Properties());
	}

	/**
	 *
	 * @param port
	 * @param requestManager
	 * @param properties can contain a static.docs properties, comma-separated list of the directories considered as containing static documents.
	 *                   Defaulted to "/web/". Example: "/web/,/admin/docs/,/static/".
	 * @throws Exception
	 */
	public HTTPServer(int port, RESTRequestManager requestManager, Properties properties) throws Exception {
		this.port = port;
		if (properties == null) {
			throw new RuntimeException("Properties parameter should not be null");
		}
		this.staticDocumentsLocation = Arrays.asList(properties.getProperty("static.docs", "/web/").split(","));

		addRequestManager(requestManager);
		// Infinite loop, waiting for requests
		httpListenerThread = new Thread("HTTPListener") {
			public void run() {
				try {
					boolean okToStop = false;
					ServerSocket ss = new ServerSocket(port);
					if (verbose) {
						HTTPContext.getInstance().getLogger().info("Port " + port + " opened successfully.");
					}
					while (isRunning()) {
						Socket client = ss.accept(); // Blocking read
						InputStreamReader in = new InputStreamReader(client.getInputStream());
						OutputStream out = client.getOutputStream();
						Request request = null;
						String line = "";
						boolean top = true;
						Map<String, String> headers = new HashMap<>();
//					while ((line = in.readLine()) != null)
						int read = 0;
						boolean cr = false, lf = false;
						boolean lineAvailable = false;
						boolean inPayload = false;
						StringBuffer sb = new StringBuffer();
						boolean keepReading = true;
//					System.out.println(">>> Top of the Loop <<<");
						if (verbose) {
							HTTPContext.getInstance().getLogger().info(">>> HTTP: Top of the loop <<<");
						}
						while (keepReading) {
							if (top) { // Ugly!! Argh! :(
								try {
									Thread.sleep(100L);
								} catch (InterruptedException ie) {
								}
								top = false;
							}
							try {
								if (in.ready()) {
									read = in.read();
								} else {
									if (verbose)
										HTTPContext.getInstance().getLogger().info(">>> End of InputStream <<<");
									read = -1;
								}
							} catch (IOException ioe) {
								read = -1;
							}
							if (read == -1) {
								keepReading = false;
							} else {
								if (!inPayload) {
									sb.append((char) read);
									if (!cr && read == '\r') {
										cr = true;
									}
									if (!lf && read == '\n') {
										lf = true;
									}
									if (cr && lf) {
										line = sb.toString().trim(); // trim removes CR & LF
										sb = new StringBuffer();
										lineAvailable = true;
										cr = lf = false;
									}
								} else {
									sb.append((char) read);
								}
//								System.out.println("======================");
//								DumpUtil.displayDualDump(sb.toString());
//								System.out.println("======================");
								if (!inPayload) {
									if (lineAvailable) {
										if (verbose) {
//							      System.out.println("HTTP Request line : " + line);
											DumpUtil.displayDualDump(line);
											System.out.println(); // Blank between lines
										}
										if (request != null && line.length() == 0) {
											// Payload begins
											inPayload = true;
											request.setHeaders(headers);
										}
										if (request == null && line.indexOf(" ") != -1) {
											String firstWord = line.substring(0, line.indexOf(" "));
											if (Request.VERBS.contains(firstWord)) { // Start Line
												String[] requestElements = line.split(" ");
												request = new Request(requestElements[0], requestElements[1], requestElements[2]);
												if (verbose) {
													HTTPContext.getInstance().getLogger().info(">>> New request: " + line + " <<<");
												}
											}
										}
										if (request != null && !inPayload) {
											if (line.indexOf(":") > -1) { // Header?
												String headerKey = line.substring(0, line.indexOf(":"));
												String headerValue = line.substring(line.indexOf(":") + 1);
												headers.put(headerKey, headerValue);
											}
										}
									}
									lineAvailable = false;
								}
							}
						}
						String payload = sb.toString();
						if (payload != null && request != null) {
							request.setContent(payload.getBytes());
						}
						if (verbose) {
							HTTPContext.getInstance().getLogger().info(">>> End of HTTP Request <<<");
						}
						if (request != null) {
							String path = request.getPath();
							if (request.getQueryStringParameters() != null && request.getQueryStringParameters().keySet().contains("verbose")) {
								String verb = request.getQueryStringParameters().get("verbose");
								verbose = (verb == null || verb.toUpperCase().equals("YES") || verb.toUpperCase().equals("TRUE") || verb.toUpperCase().equals("ON"));
							}
							if ("/exit".equals(path)) {
								System.out.println("Received an exit signal (path)");
								Response response = new Response(request.getProtocol(), Response.STATUS_OK);
								String content = "Exiting";
								RESTProcessorUtil.generateResponseHeaders(response, "text/html", content.length());
								response.setPayload(content.getBytes());
								sendResponse(response, out);
								okToStop = true;
							} else if ("/test".equals(path)) {
								Response response = new Response(request.getProtocol(), Response.STATUS_OK);
								String content = "Test is OK";
								if (request.getContent() != null && request.getContent().length > 0) {
									content += String.format("\nYour payload was [%s]", new String(request.getContent()));
								}
								RESTProcessorUtil.generateResponseHeaders(response, "text/html", content.length());
								response.setPayload(content.getBytes());
								sendResponse(response, out);
							} else if (pathIsStatic(path)) { // Then this is static content. See "static.docs" property.
								Response response = new Response(request.getProtocol(), Response.STATUS_OK);
								String fName = path;
								if (fName.indexOf("?") > -1) {
									fName = fName.substring(0, fName.indexOf("?"));
								}
								File f = new File("." + fName);

								if ((!f.exists() || f.isDirectory()) && fName.endsWith("/")) { // try index.html
									fName += DEFAULT_RESOURCE;
									path  += DEFAULT_RESOURCE; // Will be used for Content-Type
									f = new File("." + fName);
								}

								if (!f.exists()) {
									response = new Response(request.getProtocol(), Response.NOT_FOUND);
									response.setPayload(String.format("File [%s] not found (%s).", fName, f.getAbsolutePath()).getBytes());
								} else {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									Files.copy(f.toPath(), baos);
									baos.close();
									byte[] content = baos.toByteArray();
									RESTProcessorUtil.generateResponseHeaders(response, getContentType(path), content.length);
									response.setPayload(content);
								}
								sendResponse(response, out);
							} else {
								if (requestManagers != null) {  // Manage it as a REST Request.
									boolean unManagedRequest = true;
									for (RESTRequestManager reqMgr : requestManagers) { // Loop on requestManagers
										try {
											Response response = reqMgr.onRequest(request); // REST Request, most likely.
											sendResponse(response, out);
//								    System.out.println(">> Returned REST response.");
											unManagedRequest = false; // Found it.
											break;
										} catch (UnsupportedOperationException usoe) {
											// Absorb
										} catch (Exception ex) {
											System.err.println("Ooch");
											ex.printStackTrace();
										}
									}
									if (unManagedRequest) {
										Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
										sendResponse(response, out);
									}
								}
							}
						} else {
							if (payload != null && payload.length() > 0 && payload.startsWith("?WATCH=")) { // GPSd ?  ?WATCH={...}; ?POLL; ?DEVICE;
								System.out.println(String.format(">>>>>>>> GPSd: [%s]", payload)); // This is the first embryo of a GPSd implementation...
								String json = payload.substring("?WATCH=".length());
								String responsePayload = "{\"class\":\"SKY\",\"device\":\"/dev/pts/1\",\"time\":\"2005-07-08T11:28:07.114Z\",\"xdop\":1.55,\"hdop\":1.24,\"pdop\":1.99,\"satellites\":[{\"PRN\":23,\"el\":6,\"az\":84,\"ss\":0,\"used\":false},{\"PRN\":28,\"el\":7,\"az\":160,\"ss\":0,\"used\":false},{\"PRN\":8,\"el\":66,\"az\":189,\"ss\":44,\"used\":true},{\"PRN\":29,\"el\":13,\"az\":273,\"ss\":0,\"used\":false},{\"PRN\":10,\"el\":51,\"az\":304,\"ss\":29,\"used\":true},{\"PRN\":4,\"el\":15,\"az\":199,\"ss\":36,\"used\":true},{\"PRN\":2,\"el\":34,\"az\":241,\"ss\":43,\"used\":true},{\"PRN\":27,\"el\":71,\"az\":76,\"ss\":43,\"used\":true}]}" + "\n";
								out.write(responsePayload.getBytes());
								out.flush();
							} else if (line != null && line.length() != 0) {
								HTTPContext.getInstance().getLogger().warning(">>>>>>>>>> What?"); // TODO See when/why this happens...
								HTTPContext.getInstance().getLogger().warning(">>>>>>>>>> Last line was [" + line + "]");
								HTTPContext.getInstance().getLogger().warning(String.format(">>>>>>>>>> line: %s, in payload: %s, request %s", lineAvailable, inPayload, request));
							}
						}
						out.flush();
						out.close();
						in.close();
						client.close();
						if (okToStop)
							stopRunning();
					} // while (isRunning())
					ss.close();
				} catch (Exception e) {
					HTTPContext.getInstance().getLogger().severe(String.format(">>> Port %d, %s >>>", port, e.toString()));
					HTTPContext.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
					HTTPContext.getInstance().getLogger().severe(String.format("<<< Port %d <<<", port));
				} finally {
					if (verbose)
						HTTPContext.getInstance().getLogger().info("HTTP Server is done.");
					if (waiter != null) {
						synchronized (waiter) {
							waiter.notify();
						}
					}
					System.out.println("Bye from HTTP");
				}
			}
		};

		// Intercept Ctrl+C
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println(" <- HTTP: Ctrl+C intercepted.");
			onExit();
			// Send /exit
			try {
				String returned = HTTPClient.getContent(String.format("http://localhost:%d/exit", port));
				System.out.println("Exiting -> " + returned);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
			System.out.println("Dead.");
		}));
		httpListenerThread.start();
	}

	public Thread getHttpListenerThread() {
		return this.httpListenerThread;
	}

	private boolean pathIsStatic(String path) {
		return this.staticDocumentsLocation
				.stream()
				.filter(elmt -> path.startsWith(elmt))
				.findFirst()
				.isPresent();
	}

	/**
	 * Full mime-type list at https://www.sitepoint.com/web-foundations/mime-types-complete-list/
	 *
	 * @param f
	 * @return
	 */
	private static String getContentType(String f) { // TODO add more types, as required
		String contentType = "text/plain";
		if (f.endsWith(".html")) {
			contentType = "text/html";
		} else if (f.endsWith(".js")) {
			contentType = "text/javascript";
		} else if (f.endsWith(".css")) {
			contentType = "text/css";
		} else if (f.endsWith(".xml")) {
			contentType = "text/xml";
		} else if (f.endsWith(".ico")) {
			contentType = "image/x-icon";
		} else if (f.endsWith(".png")) {
			contentType = "image/png";
		} else if (f.endsWith(".gif")) {
			contentType = "image/gif";
		} else if (f.endsWith(".jpg") || f.endsWith(".jpeg")) {
			contentType = "image/jpeg";
		} else if (f.endsWith(".svg")) {
			contentType = "image/svg+xml";
		} else if (f.endsWith(".woff")) {
			contentType = "application/x-font-woff";
		} else if (f.endsWith(".wav")) {
			contentType = "audio/wav";
		} else if (f.endsWith(".pdf")) {
			contentType = "application/pdf";
		} else if (f.endsWith(".ttf")) {
			contentType = "application/x-font-ttf";
		} else {
			System.out.println(String.format("Unrecognized file type (content type) for [%s], you might want to add it to " +
							"%s", f, HTTPServer.class.getName()
					));
		}
		return contentType;
	}

	private void sendResponse(Response response, OutputStream os) {
		try {
			os.write(String.format("%s %d \r\n", response.getProtocol(), response.getStatus()).getBytes());
			if (response.getHeaders() != null) {
				response.getHeaders().keySet().stream().forEach(k -> {
					try {
						os.write(String.format("%s: %s\r\n", k, response.getHeaders().get(k)).getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
			os.write("\r\n".getBytes()); // End Of Header
			if (response.getPayload() != null) {
				os.write(response.getPayload());
				os.flush();
			}
		} catch (SocketException se) {
			if (se.getMessage().contains("Broken pipe")) {
				System.err.println("+-------------------------");
				System.err.println("| Oops, client hung up!");
				System.err.println("+-------------------------");
			} else {
				se.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * For possible override.
	 * Called before shutting down.
	 */
	public void onExit() {

	}

	private static Thread waiter = null;

	//  For dev tests
	public static void main(String[] args) throws Exception {
		//System.setProperty("http.port", "9999");
		HTTPServer httpServer = new HTTPServer(9999);
		System.out.println("Started");

		if (true) {
			waiter = new Thread("HTTPWaiter") {
				public void run() {
					synchronized (this) {
						try {
							this.wait();
						} catch (Exception ex) {
						}
					}
				}
			};
			waiter.start();
		} else {
			httpServer.getHttpListenerThread().join();
			System.out.println("Done (with test)");
		}
	}
}
