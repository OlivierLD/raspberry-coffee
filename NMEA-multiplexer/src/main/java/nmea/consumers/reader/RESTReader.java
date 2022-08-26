package nmea.consumers.reader;

import http.client.HTTPClient;
import nmea.ais.AISParser;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;

import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TCP reader
 */
public class RESTReader extends NMEAReader {
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static int DEFAULT_HTTP_PORT = 80;
	private final static String DEFAULT_PROTOCOL = "http";
	private final static String DEFAULT_QUERY_PATH = "/";
	private final static String DEFAULT_QUERY_STRING = "";

	private int httpPort = DEFAULT_HTTP_PORT;
	private String hostName = DEFAULT_HOST_NAME;
	private String protocol = DEFAULT_PROTOCOL;
	private String queryPath = DEFAULT_QUERY_PATH;
	private String queryString = DEFAULT_QUERY_STRING;

	public RESTReader(List<NMEAListener> al) {
		this(null, al, DEFAULT_PROTOCOL, DEFAULT_HOST_NAME, DEFAULT_HTTP_PORT, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING);
	}

	public RESTReader(List<NMEAListener> al, int http) {
		this(null, al, DEFAULT_PROTOCOL, DEFAULT_HOST_NAME, http, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING);
	}

	public RESTReader(List<NMEAListener> al, String host, int http) {
		this(null, al, DEFAULT_PROTOCOL, host, http, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING);
	}
	public RESTReader(String threadName, List<NMEAListener> al, String protocol, String host, int http, String path, String qs) {
		super(threadName != null ? threadName : "rest-thread", al);
		this.protocol = protocol;
		this.hostName = host;
		this.httpPort = http;
		this.queryPath = path;
		this.queryString = qs;
	}

//	private Socket skt = null; // TODO HTTPClient
	private HTTPClient restClient;

	public String getProtocol() {
		return this.protocol;
	}
	public String getHostname() {
		return this.hostName;
	}
	public int getPort() {
		return this.httpPort;
	}
	public String getQueryPath() {
		return this.queryPath;
	}
	public String getQueryString() {
		return this.queryString;
	}

	private String restURL;

	@Override
	public void startReader() {
		super.enableReading();

		restURL = String.format("%s://%s:%d%s%s",
				this.protocol,
				this.hostName,
				this.httpPort,
				this.queryPath,
				this.queryString);
		try {
			Map<String, String> headers = new HashMap<>();
			if (restClient == null) {
				restClient = new HTTPClient();
			}
			while (this.canRead()) {
				try {
					String httpResponse = HTTPClient.doGet(restURL, headers);
					if (verbose) { // TODO A parameter
						System.out.println("Bing!");
					}
					// TODO return the response message/status ?
					NMEAEvent n = new NMEAEvent(this, httpResponse);
					super.fireDataRead(n);
				} catch (BindException be) {
					System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + httpPort);
					be.printStackTrace();
					manageError(be);
				} catch (final SocketException se) {
					if (se.getMessage().indexOf("Connection refused") > -1) {
						System.out.println("Refused (1)");
//						se.printStackTrace();
					} else if (se.getMessage().indexOf("Connection reset") > -1) {
						System.out.println("Reset (2)");
					} else {
						boolean tryAgain = false;
						if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (1)");
							}
							tryAgain = true;
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (2)");
							}
						} else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (3)");
							}
							tryAgain = true;
						} else if (se instanceof ConnectException) { // Et hop!
							tryAgain = false;
							System.err.println("REST :" + se.getMessage());
						} else {
							tryAgain = false;
							System.err.println("REST Server:" + se.getMessage());
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// TODO Wait like 1 sec.
			}
			System.out.println("Stop Reading REST server.");
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
	}

	@Override
	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			if (restClient != null) {
				this.goRead = false;
				restClient = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for REST */ }

}
