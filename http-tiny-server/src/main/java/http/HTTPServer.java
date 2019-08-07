package http;

import com.google.gson.Gson;
import http.client.HTTPClient;
import utils.DumpUtil;
import utils.StaticUtil;
import utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Can be used for the REST interface of an HTTP Server.
 * Can also be used as a Proxy
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
 * <p>
 * <em>
 * Warning: This is a <b>very lightweight</b> HTTP server. It is certainly not supposed to scale!!
 * It's NOT J2EE compliant, it is NOT JAX-RS based. But it will handle your requests.
 * </em>
 * </p>
 * </p>
 * <p>
 * Logging can be done. See <code>-Djava.util.logging.config.file=[path]/logging.properties</code>
 * <br>
 * See <a href="https://docs.oracle.com/cd/E23549_01/doc.1111/e14568/handler.htm">https://docs.oracle.com/cd/E23549_01/doc.1111/e14568/handler.htm</a>
 *
 * System properties to look at:
 * - http.verbose
 * - http.verbose.dump
 * - http.port
 * - static.docs
 * - static.zip.docs
 * - autobind
 * - web.archive
 *
 * </p>
 */
public class HTTPServer {
	private static boolean verbose = "true".equals(System.getProperty("http.verbose", "false"));
	private static boolean verboseDump = "true".equals(System.getProperty("http.verbose.dump", "false"));
	private int port = -1;

	private Thread httpListenerThread;

	private Function<HTTPServer.Request, HTTPServer.Response> proxyFunction = null;

	/*
	  For CORS, to be returned in the Response:
	  Nice article: https://www.html5rocks.com/en/tutorials/cors/

		cres.getHeaders().add("Access-Control-Allow-Origin", "*");
		cres.getHeaders().add("Access-Control-Allow-Headers", "*"); // <== !!
		cres.getHeaders().add("Access-Control-Expose-Headers", "Access-Token"); // Header(s) to expose, CSV
		cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
		cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//  cres.getHeaders().add("Access-Control-Max-Age", "1209600");
	 */

	public static class Request {
		public final static List<String> VERBS = Arrays.asList(
				"GET",
				"POST",
				"DELETE",
				"PUT",
				"PATCH",
				"OPTIONS",
				"HEAD"
		);

		private String verb;
		private String path;
		private String protocol;
		private byte[] content;
		private Map<String, String> headers;
		private String requestPattern;

		private Map<String, String> queryStringParameters;

		public Request() {}

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
			return getPath(false);
		}

		public String getPath(boolean full) {
			String url = this.path;
			if (queryStringParameters != null && full) {
				url += "?" +
						this.queryStringParameters.keySet()
								.stream()
								.map(k -> k + "=" + queryStringParameters.get(k))
								.collect(Collectors.joining("&"));
			}
			return url;
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

		public List<String> getPathParameters() {
			return RESTProcessorUtil.getPathPrmValues(this.getRequestPattern(), this.getPath());
		}

		public List<String> getPathParameterNames() {
			return RESTProcessorUtil.getPathPrmNames(this.getRequestPattern());
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
		public final static int STATUS_OK_      = 201;
		public final static int NO_CONTENT      = 204;
		public final static int BAD_REQUEST     = 400;
		public final static int NOT_FOUND       = 404;
		public final static int TIMEOUT         = 408;
		public final static int NOT_IMPLEMENTED = 501;

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
		List<String> errorStack;

		public ErrorPayload errorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public ErrorPayload errorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return this;
		}

		public ErrorPayload errorStack(List<String> errorStack) {
			this.errorStack = errorStack;
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

	public static List<String> dumpException(Throwable ex) {
		return Arrays.stream(ex.getStackTrace())
				.map(ste -> String.format(
						"from %s.%s [%s:%d]",
						ste.getClassName(),
						ste.getMethodName(),
						ste.getFileName(),
						ste.getLineNumber()))
				.collect(Collectors.toList());
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
	private boolean autoBind = false;
	private boolean sendCleanStopSignal = true;
	private List<String> staticDocumentsLocation = null;
	private List<String> staticZippedDocumentsLocation = null;
	private static final String DEFAULT_RESOURCE = "index.html";

	// This is an array, so several apps can subscribe to the same HTTPServer.
	// A REST operation list belongs to each application.
	// In this case, the HTTPServer should probably live in a singleton.
	private final List<RESTRequestManager> requestManagers = new ArrayList<>();

	public List<RESTRequestManager> getRequestManagers() {
		synchronized(this.requestManagers) { return this.requestManagers; }
	}

	public void addRequestManager(RESTRequestManager requestManager) {
		if (requestManager != null) {
//			if (requestManagers == null) {
//				requestManagers = new ArrayList<>(1);
//			}
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
											RESTProcessorUtil.pathsAreIdentical(newOp.getPath(), op.getPath())))
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
			synchronized (requestManagers) {
				requestManagers.add(requestManager);
			}
		}
	}

	public void removeRequestManager(RESTRequestManager requestManager) {
		if (requestManagers.contains(requestManager)) {
			requestManagers.remove(requestManager);
		}
	}

	private static int defaultPort = 9999;
	static {
		String httpPort = System.getProperty("http.port", String.valueOf(defaultPort));
		try {
			defaultPort = Integer.parseInt(httpPort);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException(nfe);
		}
	}


	public int getPort() {
		return this.port;
	}

	private void incPort() {
		this.port += 1;
	}

	public HTTPServer() throws Exception {
		this(defaultPort, null, new Properties(), false);
	}

	public HTTPServer(boolean startImmediately) throws Exception {
		this(defaultPort, null, new Properties(), startImmediately);
	}

	public HTTPServer(int port) throws Exception {
		this(port, null, new Properties(), false);
	}

	public HTTPServer(int port, Properties properties) throws Exception {
		this(port, null, properties, false);
	}

	public HTTPServer(int port, boolean startImmediately) throws Exception {
		this(port, null, new Properties(), startImmediately);
	}

	public HTTPServer(Properties properties) throws Exception {
		this(defaultPort, null, properties, false);
	}

	public HTTPServer(Properties properties, boolean startImmediately) throws Exception {
		this(defaultPort, null, properties, startImmediately);
	}

	public HTTPServer(RESTRequestManager requestManager) throws Exception {
		this(defaultPort, requestManager, new Properties(), false);
	}

	public HTTPServer(RESTRequestManager requestManager, boolean startImmediately) throws Exception {
		this(defaultPort, requestManager, new Properties(), startImmediately);
	}

	/**
	 *
	 * @param port HTTP port
	 * @param requestManager for the REST Requests
	 * @throws Exception
	 */
	public HTTPServer(int port, RESTRequestManager requestManager) throws Exception {
		this(port, requestManager, new Properties(), false);
	}

	public HTTPServer(int port, RESTRequestManager requestManager, boolean startImmediately) throws Exception {
		this(port, requestManager, new Properties(), startImmediately);
	}

	/**
	 *
	 * @param port
	 * @param requestManager
	 * @param properties can contain a static.docs properties, comma-separated list of the directories considered as containing static documents.
	 *                   Defaulted to "/web/". Example: "/web/,/admin/docs/,/static/".
	 * @throws Exception
	 *
	 * Port can be overridden by -Dhttp.port. Takes precedence on anything else.
	 */
	public HTTPServer(int port, RESTRequestManager requestManager, Properties properties, boolean startImmediately) throws Exception {
		this.port = port;
//		String httpPort = System.getProperty("http.port", String.valueOf(port));
//		try {
//			this.port = Integer.parseInt(httpPort);
//		} catch (NumberFormatException nfe) {
//			throw new RuntimeException(nfe);
//		}

		if (properties == null) {
			throw new RuntimeException("Properties parameter should not be null");
		}
		this.staticDocumentsLocation = Arrays.asList(properties.getProperty("static.docs", "/web/").split(","));
		this.staticZippedDocumentsLocation = Arrays.asList(properties.getProperty("static.zip.docs", "/zip/").split(","));
		this.autoBind = "true".equals(properties.getProperty("autobind"));

		HTTPServer httpServerInstance = this;

		addRequestManager(requestManager);

		// Intercept Ctrl+C
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println(" <- HTTP: Ctrl+C intercepted.");
			onExit();
			// Send /exit
			if (httpServerInstance.sendCleanStopSignal) {
				try {
					String returned = HTTPClient.getContent(String.format("http://localhost:%d/exit", httpServerInstance.getPort()));
					System.out.println("Exiting -> " + returned);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
				}
			}
			System.out.println("HTTPServer Dead.");
		}));

		// Infinite loop, waiting for requests
		httpListenerThread = new Thread("HTTPListener") {
			public void run() {
				try {
					boolean okToStop = false;
					ServerSocket ss = null;
					boolean keepTrying = true;
					while (keepTrying) {
						try {
							ss = new ServerSocket(httpServerInstance.getPort());
							keepTrying = false;
							System.out.println(String.format("Port open: %d", httpServerInstance.getPort()));
							System.out.println("-- Dumping: --");
							List<String> st = DumpUtil.whoCalledMe();
							st.stream().forEach(el -> System.out.println(String.format("\t%s", el)));
							System.out.println("--------------");
						} catch (BindException be) {
							if (httpServerInstance.autoBind) {
								httpServerInstance.incPort();
								HTTPContext.getInstance().getLogger().info(String.format("Port in use, trying %d", httpServerInstance.getPort()));
							} else {
								System.err.println(String.format("Address in use: %d", httpServerInstance.getPort()));
//								keepTrying = false;
								System.err.println("-- Dumping: --");
								List<String> st = DumpUtil.whoCalledMe();
								st.stream().forEach(el -> System.err.println(String.format("\t%s", el)));
								System.err.println("--------------");
								throw be;
							}
						}
					}
					if (verbose) {
						HTTPContext.getInstance().getLogger().info("Port " + httpServerInstance.getPort() + " opened successfully.");
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
							if (top) { // Ugly!! Argh! :( I know. Will improve.
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
									if (verbose) {
										HTTPContext.getInstance().getLogger().info(">>> End of InputStream <<<");
									}
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
								// Super-DEBUG
//								System.out.println("======================");
//								DumpUtil.displayDualDump(sb.toString());
//								System.out.println("======================");
								if (!inPayload) {
									if (lineAvailable) {
										if (verboseDump) {
//							      System.out.println("HTTP Request line : " + line);
											DumpUtil.displayDualDump(line);
											System.out.println(); // Blank between lines
										}
										if (verbose) {
											System.out.println(line);
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
												if (line.indexOf(" ") > 0 && line.indexOf(" ") < line.indexOf(":") ) { // TODO: Not start with Verb
													// Not a GET http://machine HTTP/1.1	, with the protocol in the request
												} else {
													String headerKey = line.substring(0, line.indexOf(":"));
													String headerValue = line.substring(line.indexOf(":") + 1);
													headers.put(headerKey, headerValue);
												}
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
							} else if (pathIsZipStatic(path)) { // Static content, in an archive. See "static.zip.docs" property. Defaulted to "/zip/"
								// What zip file should we look into? Assume web.zip, unless -Dweb.archive is set.
								// url will be like /zip/index.html, where ./index.html is the path in the archive.

								Response response = new Response(request.getProtocol(), Response.STATUS_OK);
								String fName = path;
								if (fName.indexOf("?") > -1) {
									fName = fName.substring(0, fName.indexOf("?"));
								}
								String zipPath = zipPath(fName);
								if (zipPath != null) {
									fName = fName.substring(zipPath.length());

									String webArchive = System.getProperty("web.archive", "web.zip"); // TODO Make sure it is a zip archive
									if (verbose) {
										System.out.println(String.format("%s => reading %s in %s", zipPath, fName, webArchive));
									}

									InputStream is = getZipInputStream(webArchive, fName);
									if (is != null) {
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										StaticUtil.copy(is, baos);
										baos.close();
										byte[] content = baos.toByteArray();
										RESTProcessorUtil.generateResponseHeaders(response, getContentType(path), content.length);
										response.setPayload(content);

									} else {
										response = new Response(request.getProtocol(), Response.NOT_FOUND);
										response.setPayload(String.format("File [%s] not found in %s.", fName, "web.zip").getBytes());
									}
								} else {
									response = new Response(request.getProtocol(), Response.NOT_FOUND);
									response.setPayload(String.format("ZipPath not found for %s .", fName).getBytes());
								}
								sendResponse(response, out);
							} else if (pathIsStatic(path)) { // Static content, on the file system. See "static.docs" property. Defaulted to "/web/"
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
								if (requestManagers != null && requestManagers.size() > 0) {  // Manage it as a REST Request
									boolean unManagedRequest = true;
									synchronized (requestManagers) {
										try {
											final Request _request = request;
											Optional<RESTRequestManager> restRequestManager;
											synchronized (requestManagers) {
												restRequestManager = requestManagers.stream()
														.filter(rm -> rm.containsOp(_request.getVerb(), _request.getPath()))
														.findFirst();
											}
											if (restRequestManager.isPresent()) {
												unManagedRequest = false;
												Response response = restRequestManager.get().onRequest(request); // REST Request, most likely.
												try {
													sendResponse(response, out);
												} catch (Exception err) {
													System.err.println("+-----------------------------------------------");
													System.err.println(String.format("| Caught error sending back response:\n%s", response.toString()));
													System.err.println("+-----------------------------------------------");
													err.printStackTrace();
												}
											}
											// Old implementation
//											for (RESTRequestManager reqMgr : requestManagers) { // Loop on requestManagers
//												synchronized (reqMgr) {
//													try {
//														Response response = reqMgr.onRequest(request); // REST Request, most likely.
//														sendResponse(response, out);
////								          System.out.println(">> Returned REST response.");
//														unManagedRequest = false; // Found it.
//														break;
//													} catch (UnsupportedOperationException usoe) { // No such operation available, probably no the right RequestManager
//														// Absorb
//													} catch (Exception ex) {
//														System.err.println("Ooch");
//														ex.printStackTrace();
//													}
//												}
//											}
										} catch (Exception ooch){
											// Keep going
											ooch.printStackTrace();
										}
									}
									if (unManagedRequest) {
										Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
										sendResponse(response, out);
									}
								} else {
									if (verbose) {
										HTTPContext.getInstance().getLogger().info(">>> REST Request and no RequestManager. Proxy? <<<");
									}
									// explicit 'proxy' implementation
									if (proxyFunction != null) {
										// In a Thread, not to block
										final Request req = request;
										Thread proxyThread = new Thread(() -> {
											try {
												Response response = proxyFunction.apply(req);
												sendResponse(response, out); // Back to caller
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										});
										proxyThread.start();
									}
								}
							}
						} else { // Specific. Is that a GPSd request?
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
						if (okToStop) {
							stopRunning();
						}
					} // while (isRunning())
					ss.close();
				} catch (BindException be) {
					HTTPContext.getInstance().getLogger().severe(String.format(">>> BindException: Port %d, %s >>>", httpServerInstance.getPort(), be.toString()));
					HTTPContext.getInstance().getLogger().log(Level.SEVERE, be.getMessage(), be);
					HTTPContext.getInstance().getLogger().severe(String.format("<<< BindException: Port %d <<<", httpServerInstance.getPort()));
					// throw new RuntimeException(be);
					httpServerInstance.sendCleanStopSignal = false; // Not to kill already running instance on this port
				} catch (Exception e) {
					HTTPContext.getInstance().getLogger().severe(String.format(">>> Port %d, %s >>>", httpServerInstance.getPort(), e.toString()));
					HTTPContext.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
					HTTPContext.getInstance().getLogger().severe(String.format("<<< Port %d <<<", httpServerInstance.getPort()));
				} finally {
					if (verbose) {
						HTTPContext.getInstance().getLogger().info("HTTP Server is done.");
					}
					if (waiter != null) {
						synchronized (waiter) {
							waiter.notify();
						}
					}
					System.out.println("Bye from HTTP");
				}
				System.out.println("HTTP Thread, end of run.");
			}
		};

		if (startImmediately) {
			this.startServer();
		}
	}

	private static InputStream getZipInputStream(String zipName, String entryName)
			throws Exception {
		ZipInputStream zip = new ZipInputStream(new FileInputStream(zipName));
		InputStream is = null;
		boolean go = true;
		while (go) {
			ZipEntry ze = zip.getNextEntry();
			if (ze == null) {
				go = false;
			} else {
				if (ze.getName().equals(entryName)) {
					is = zip;
					go = false;
				}
			}
		}
//		if (is == null) {
//			throw new FileNotFoundException("Entry " + entryName + " not found in " + zipName);
//		}
		return is;
	}

	public void startServer() {
		httpListenerThread.start();
	}

	public Thread getHttpListenerThread() {
		return this.httpListenerThread;
	}

	public void setProxyFunction(Function<HTTPServer.Request, HTTPServer.Response> proxyFunction) {
		this.proxyFunction = proxyFunction;
	}

	private boolean pathIsStatic(String path) {
		return this.staticDocumentsLocation
				.stream()
				.filter(elmt -> path.startsWith(elmt))
				.findFirst()
				.isPresent();
	}

	private boolean pathIsZipStatic(String path) {
		return this.staticZippedDocumentsLocation
				.stream()
				.filter(elmt -> path.startsWith(elmt))
				.findFirst()
				.isPresent();
	}

	private String zipPath(String fullPath) {
		for (String path : this.staticZippedDocumentsLocation) {
			if (fullPath.startsWith(path)) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Full mime-type list at https://www.sitepoint.com/web-foundations/mime-types-complete-list/
	 *
	 * @param f
	 * @return
	 */
	private static String getContentType(String f) { // TODO add more types, as/when required
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
		} else if (f.endsWith(".json")) {
			contentType = "application/json";
		} else if (f.endsWith(".ttf")) {
			contentType = "application/x-font-ttf";
		} else {
			System.out.println(String.format("Unrecognized file type (content type) for [%s], you might want to add it to " +
							"%s", f, HTTPServer.class.getName()
					));
		}
		return contentType;
	}

	public static boolean isText(String mimeType) { // May require some tweaks...
		if (mimeType.trim().startsWith("text/") ||
				mimeType.contains("json") ||
				mimeType.contains("xml") ||
				mimeType.contains("script")) {
			return true;
		}
		return false;
	}

	private void manageSocketException(SocketException se, String content) {
		if (se.getMessage().contains("Broken pipe")) {
			if (verbose) {
				System.err.println("+-------------------------");
				System.err.println(String.format("| %s - Managed error, client hung up! Response was:\n%s", new Date().toString(), content));
				System.err.println("+-------------------------");
			}
		} else {
			System.err.println(String.format(">> Cause: %s, Message: %s", se.getCause(), se.getMessage()));
			se.printStackTrace();
		}
	}

	private void sendResponse(Response response, OutputStream os) {
		try {
			os.write(String.format("%s %d \r\n", response.getProtocol(), response.getStatus()).getBytes());
			if (response.getHeaders() != null) {
				response.getHeaders().keySet().stream().forEach(k -> {
					try {
						os.write(String.format("%s: %s\r\n", k, response.getHeaders().get(k)).getBytes());
					} catch (SocketException se) {
						manageSocketException(se, response.toString());
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				});
			}
			os.write("\r\n".getBytes()); // End Of Header
			if (response.getPayload() != null) {
				os.write(response.getPayload());
				os.flush();
			}
		} catch (SocketException se) {
			manageSocketException(se, response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Default proxy implementation.
	 * Does only some logging
	 *
	 * @param request
	 * @return
	 */
	protected static Response defaultProxy(Request request) {
		// An HTTPClient makes the received request, and sends back the response
		Response response = null;
		try {
			response = HTTPClient.doRequest(request);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		if (verbose || verboseDump) { // Dump response elements
			System.out.println();
			final int PAD = 72;
			String rCode = String.format("%sResponse code: %d", StringUtils.lpad("", PAD), response.getStatus());
			System.out.println(rCode);
			if (response.getHeaders() != null) {
				Map<String, String> respHeaders = response.getHeaders();
				if (verboseDump) {
					respHeaders.keySet().forEach(k -> DumpUtil.displayDualDump(k + ": " + respHeaders.get(k), PAD));
					System.out.println();
				}
				if (verbose) {
					respHeaders.keySet().forEach(k -> System.out.println(String.format("%s%s: %s", StringUtils.lpad("", PAD), k, respHeaders.get(k))));
				}
			}
			if (response.getPayload() != null) {
				if (response.getHeaders() != null && response.getHeaders().get("Content-Type") != null && isText(response.getHeaders().get("Content-Type"))) {
					String responsePayload = new String(response.getPayload());
					if (verboseDump) {
						DumpUtil.displayDualDump(responsePayload, PAD);
						System.out.println();
					}
					if (verbose) {
						System.out.println(String.format("%s%s", StringUtils.lpad("", PAD), responsePayload));
					}
				} else {
					String mimeType = "-none-";
					if (response.getHeaders() != null && response.getHeaders().get("Content-Type") != null) {
						mimeType = response.getHeaders().get("Content-Type");
					}
					System.out.println(String.format("... No Content-Type, or not text? [%s]", mimeType));
				}
			}
		}
		return response;
	}

	/**
	 * For possible override.
	 * Called before shutting down.
	 */
	public void onExit() {
	}

	private static Thread waiter = null;

	private static boolean withRest = true;
	/**
	 * For dev tests, example, default proxy.
 	 */
	public static void main(String... args) throws Exception {
		int port = 9999;
		try {
			port = Integer.parseInt(System.setProperty("http.port", String.valueOf(port)));
		} catch (NumberFormatException nfe) {
			if (nfe.getMessage().equals("null")) {
				// No system variable
				System.out.println(String.format("Using port %d", port));
			} else {
				nfe.printStackTrace();
			}
		}
		Properties props = new Properties();
		props.setProperty("autobind", "true"); // AutoBind test

		HTTPServer httpServer = new HTTPServer(port, props);
//		httpServer.setProxyFunction(HTTPServer::defaultProxy);

		if (withRest) {
			List<HTTPServer.Operation> opList1 = Arrays.asList(
					new HTTPServer.Operation(
							"GET",
							"/oplist",
							request -> new Response(request.getProtocol(), Response.STATUS_OK),
							"Dummy list"));

			RESTRequestManager testRequestManager = new RESTRequestManager() {

				@Override
				public Response onRequest(Request request) throws UnsupportedOperationException {
					// This is just an example, hard coded.
					Response response = new Response(request.getProtocol(), Response.STATUS_OK);

					List<Operation> opList = opList1; // Above
					String content = new Gson().toJson(opList);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
					return response;
				}

				@Override
				public List<Operation> getRESTOperationList() {
					return opList1;
				}
			};

			httpServer.addRequestManager(testRequestManager);
		}

		httpServer.startServer();
		System.out.println(String.format("Started on port %d", httpServer.getPort()));
		System.out.println(String.format("Static pages (in a zip or not)%s", (withRest ? ", plus REST service GET /oplist are available" : "")));

		if (true) {
			waiter = new Thread("HTTPWaiter") {
				public void run() {
					synchronized (this) {
						try {
							this.wait();
							System.out.println("Waiter is done waiting.");
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
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
	private HTTPServer.Response emptyOperation(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		return response;
	}

}
