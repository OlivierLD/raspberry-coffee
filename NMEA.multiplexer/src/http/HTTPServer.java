package http;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import http.utils.DumpUtil;

/**
 * Used for the REST interface of the Multiplexer
 * <p>
 * Get the list of the multiplexed channels,
 * Get the list of the forwarders
 * Add channel, forwarder
 * Delete channel, forwarder
 * <p>
 * GET, POST, DELETE - no PUT, no PATCH (for now)
 * <br>
 * Also serves as a regular HTTP server for static documents (in the /web directory).
 * <br>
 * Has two static resources:
 * <ul>
 *   <li>/exit</li>
 *   <li>/test</li>
 * </ul>
 *
 */
public class HTTPServer {
	private boolean verbose = "true".equals(System.getProperty("http.verbose", "false"));

	public static class Request {
		public final static List<String> VERBS = Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH");

		private String verb;
		private String path;
		private String protocol;
		private byte[] content;
		private Map<String, String> headers;

		public Request() {
		}

		public Request(String verb, String path, String protocol) {
			this.verb = verb;
			this.path = path;
			this.protocol = protocol;
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

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}
	}

	public static class Response {
		private int status;
		private String protocol;

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

		private Map<String, String> headers;
		private byte[] payload;
	}

	public boolean isRunning() {
		return keepRunning;
	}

	public void stopRunning() {
		this.keepRunning = false;
	}

	private boolean keepRunning = true;
	private HTTPServerInterface requestManager = null;

	private static int defaultPort = 9999;

	public HTTPServer() throws Exception {
		this(defaultPort, null);
	}

	public HTTPServer(int port) throws Exception {
		this(port, null);
	}

	public HTTPServer(HTTPServerInterface requestManager) throws Exception {
		this(defaultPort, requestManager);
	}

	public HTTPServer(int port, HTTPServerInterface requestManager) throws Exception {
		this.requestManager = requestManager;
		// Infinite loop, waiting for requests
		Thread httpListenerThread = new Thread("HTTPListener") {
			public void run() {
				try {
					boolean okToStop = false;
					ServerSocket ss = new ServerSocket(port);
					System.out.println("Port " + port + " opened successfully.");
					while (isRunning()) {
						Socket client = ss.accept();
//					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						InputStreamReader in = new InputStreamReader(client.getInputStream());
						PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
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
						if (verbose)
							System.out.println(">>> Top of the loop <<<");
						while (keepReading) {
							if (top) { // Ugly :(
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
										System.out.println(">>> End of InputStream <<<");
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
//							    System.out.println("HTTP Request line : " + line);
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
													System.out.println(">>> New request: " + line + " <<<");
												}
											}
										}
										if (request != null && !inPayload) {
											if (line.indexOf(":") > -1) // Header?
											{
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
							System.out.println(">>> End of HTTP Request <<<");
						}
						if (request != null) {
							String path = request.getPath();

							if ("/exit".equals(path)) {
								System.out.println("Received an exit signal");
								Response response = new Response(request.getProtocol(), 200);
								String content = "Exiting";
								Map<String, String> responseHeaders = new HashMap<>();
								responseHeaders.put("Content-Type", "plain/text");
								responseHeaders.put("Content-Length", String.valueOf(content.length()));
								responseHeaders.put("Access-Control-Allow-Origin", "*");
								response.setHeaders(responseHeaders);
								response.setPayload(content.getBytes());
								sendResponse(response, out);
								okToStop = true;
							} else if ("/test".equals(path)) {
								Response response = new Response(request.getProtocol(), 200);
								String content = "Test is OK";
								if (request.getContent() != null && request.getContent().length > 0) {
									content += String.format("\nYour payload was [%s]", new String(request.getContent()));
								}
								Map<String, String> responseHeaders = new HashMap<>();
								responseHeaders.put("Content-Type", "plain/text");
								responseHeaders.put("Content-Length", String.valueOf(content.length()));
								responseHeaders.put("Access-Control-Allow-Origin", "*");
								response.setHeaders(responseHeaders);
								response.setPayload(content.getBytes());
								sendResponse(response, out);
							} else if (path.startsWith("/web/")) {                                    // Assume this is static content. TODO Tweak that.
								Response response = new Response(request.getProtocol(), 200);
								String content = readStaticContent("." + path);
								Map<String, String> responseHeaders = new HashMap<>();
								responseHeaders.put("Content-Type", getContentType(path));
								responseHeaders.put("Content-Length", String.valueOf(content.length()));
								responseHeaders.put("Access-Control-Allow-Origin", "*");
								response.setHeaders(responseHeaders);
								response.setPayload(content.getBytes());
								sendResponse(response, out);
							} else {
								if (requestManager != null) {
									Response response = requestManager.onRequest(request); // REST Request, most likely.
									sendResponse(response, out);
								}
							}
						} else { // TODO See when this happens...
							System.out.println("What?");
							System.out.println(String.format("line: %s, in payload: %s, request %s", lineAvailable, inPayload, request));
						}
						out.flush();
						out.close();
						in.close();
						client.close();
						if (okToStop)
							stopRunning();
					}
					ss.close();
				} catch (Exception e) {
					System.err.println(">>> Port " + port + ", " + e.toString() + " >>>");
					e.printStackTrace();
					System.err.println("<<< Port " + port + " <<<");
				} finally {
					if (verbose)
						System.out.println("HTTP Server is done.");
					if (waiter != null) {
						synchronized (waiter) {
							waiter.notify();
						}
					}
				}
			}
		};
		httpListenerThread.start();
	}

	private static String getContentType(String f) { // TODO add more types
		String contentType = "text/plain";
		if (f.endsWith(".html"))
			contentType = "text/html";
		else if (f.endsWith(".js"))
			contentType = "text/javascript";
		else if (f.endsWith(".css"))
			contentType = "text/css";
		return contentType;
	}

	private String readStaticContent(String path) {
		String content = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			content = sb.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return content;
	}

	private void sendResponse(Response response, PrintWriter out) {
		out.print(String.format("%s %d \r\n", response.getProtocol(), response.getStatus()));
		if (response.getHeaders() != null) {
			response.getHeaders().keySet().stream().forEach(k -> out.print(String.format("%s: %s\r\n", k, response.getHeaders().get(k))));
		}
		out.print("\r\n"); // End Of Header
		if (response.getPayload() != null) {
			out.println(new String(response.getPayload()));
		}
	}

	private static Thread waiter = null;

	//  For dev tests
	public static void main(String[] args) throws Exception {
		//System.setProperty("http.port", "9999");
		new HTTPServer(9999);
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
	}
}
