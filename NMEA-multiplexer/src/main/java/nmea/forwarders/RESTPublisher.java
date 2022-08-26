package nmea.forwarders;

import http.client.HTTPClient;
import nmea.ais.AISParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// See the test, in the test folder.
public class RESTPublisher implements Forwarder {
	private Properties props = null;

	private int httpPort = 80;                  // Default
	private String serverName = "localhost";    // Default
	private String restResource = null;         // Required. No default.
	private String protocol = "http";           // default
	private String verb = "POST";               // default
	private Map<String, String> headers = null; // Optional

	private HTTPClient restClient = null;

	private AISParser aisParser;

	public RESTPublisher() {
	}
	public RESTPublisher(String verb,
						 String serverName,
						 int port,
						 String resource) {
		this.verb = verb;
		this.serverName = serverName;
		this.httpPort = port;
		this.restResource = resource;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public String getServerName() {
		return serverName;
	}

	public String getRestResource() {
		return restResource;
	}

	public String getVerb() {
		return verb;
	}

	/**
	 * Note: This can be sub-classed to filter the sentences,
	 *       and send a formatted message to a Screen Server (for example)...
	 *       See https://github.com/OlivierLD/raspberry-coffee/tree/master/http-client-samples
	 * @param message The NMEA sentence
	 */
	@Override
	public void write(byte[] message) {
		if (restClient == null) {
			restClient = new HTTPClient();
		}
		try {
			switch(this.verb) {
				case "POST":
					String postRequest = String.format("%s://%s:%d%s", protocol, serverName, httpPort, restResource);
					String strContent = new String(message).trim();
//					System.out.println("Verbose: [" + this.props.getProperty("verbose") + "]");
					if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
						System.out.printf("%s\n%s\n", postRequest, strContent);
						if ("true".equals(System.getProperty("parse.ais"))) {
							if (strContent.startsWith(AISParser.AIS_PREFIX)) {
								if (aisParser == null) {
									aisParser = new AISParser();
								}
								try {
									System.out.println(aisParser.parseAIS(strContent));
								} catch (AISParser.AISException t) {
									System.err.println(t.toString());
								}
							}
						}
					}
					HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(postRequest, headers, strContent);
					if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
						System.out.printf("POST %s with %s: Response code %d, message: %s\n",
								postRequest,
								strContent,
								httpResponse.getCode(),
								httpResponse.getPayload());
					}
					break;
				case "PUT": // TODO See if that's needed...
				default:
					break;
			}
		} catch (Exception ex) {
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.err.println(">> Error!");
				ex.printStackTrace();
			}
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
	}

	public static class RESTBean {
		private final String cls;
		private final String protocol;
		private final int port;
		private final String serverName;
		private final String verb;
		private final String resource;
		private final String type = "rest";

		public String getProtocol() {
			return protocol;
		}
		public String getVerb() {
			return verb;
		}
		public String getServerName() {
			return serverName;
		}
		public int getPort() {
			return port;
		}
		public String getResource() {
			return resource;
		}

		public RESTBean(RESTPublisher instance) {
			cls = instance.getClass().getName();
			protocol = instance.protocol;
			port = instance.httpPort;
			serverName = instance.serverName;
			verb = instance.verb;
			resource = instance.restResource;
		}
	}

	@Override
	public Object getBean() {
		return new RESTBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
		if (this.props != null) {
			// server name, port, rest resource, verb, headers
			this.serverName = props.getProperty("server.name", this.serverName);
			this.httpPort = Integer.parseInt(props.getProperty("server.port", String.valueOf(this.httpPort)));
			this.restResource = props.getProperty("rest.resource");
			this.verb = props.getProperty("rest.verb", this.verb);
			String propProtocol = props.getProperty("rest.protocol");
			if (propProtocol != null) {
				this.protocol = propProtocol; // http or https
			}
			String headers = props.getProperty("http.headers");
			if (headers != null) {
				String[] headerArray = headers.split(",");
				for (String h : headerArray) {
					String[] nv = h.split(":");
					if (nv.length != 2) {
						// Oops! TODO Honk!
						System.err.println("WTFrench!");
					} else {
						if (this.headers == null) {
							this.headers = new HashMap<>();
						}
						this.headers.put(nv[0], nv[1]);
					}
				}
			}
		}
	}
}
