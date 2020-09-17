package nmea.forwarders;

import http.client.HTTPClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RESTPublisher implements Forwarder {
	private RESTPublisher instance = this;

	private Properties props = null;

	private int httpPort = 80; // Default
	private String serverName = "localhost"; // Default
	private String restResource = null;
	private String verb = "POST"; // default
	private Map<String, String> headers = null;

	private HTTPClient restClient = null;

	public RESTPublisher() {
	}

	@Override
	public void write(byte[] message) {
		if (restClient == null) {
			restClient = new HTTPClient();
		}
		try {
			switch(this.verb) {
				case "POST":
					HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(String.format("http://%s:%d%s", serverName, httpPort, restResource), headers, new String(message));
					break;
				default:
					break;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
	}

	public static class RESTBean {
		private String cls;
		private int port;
		private String serverName;
		private String verb;
		private String resource;
		private String type = "rest";

		public int getPort() {
			return port;
		}

		public RESTBean(RESTPublisher instance) {
			cls = instance.getClass().getName();
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
			String headers = props.getProperty("http.headers");
			if (headers != null) {
				String[] headerArray = headers.split(",");
				for (String h : headerArray) {
					String[] nv = h.split(":");
					if (nv.length != 2) {
						// Oops!
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

	public static void main(String... args) {
		String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
		try {
			RESTPublisher restPublisher = new RESTPublisher();

			Properties props = new Properties();
			props.put("server.name", "192.168.42.6");
			props.put("server.port", "8080");
			props.put("rest.resource", "/rest/endpoint?qs=prm");
			props.put("rest.verb", "POST");
			props.put("http.headers", "Content-Type:plain/text");
			restPublisher.setProperties(props);

			for (int i = 0; i < 50; i++) {
				System.out.println("Ping...");
				try {
					restPublisher.write(wpl.getBytes());
				} catch (Exception ex) {
					System.err.println(ex.getLocalizedMessage());
				}
				try {
					Thread.sleep(1_000L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
