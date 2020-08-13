package sunflower.httpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.RESTRequestManager;
import http.client.HTTPClient;
import sunflower.SunFlowerDriver;

import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private SunFlowerServer sunFlowerServer = null;
	private SunFlowerDriver featureManager = null; // Physical, the actual device (SunFlowerDriver)

	/**
	 * HDM, POS, polls the NMEA Server, feeds the featureManager (setDevicePosition, setDeviceHeading)
	 */
	private class NMEADataThread extends Thread {
		private boolean keepPolling = true;
		private String baseURL = null;

		private boolean alreadyRaisedConnectException = false;

		public NMEADataThread() {
			super();
		}
		public NMEADataThread(String name) {
			super(name);
		}
		public NMEADataThread(String name, String baseUrl) {
			super(name);
			this.baseURL = baseUrl;
		}

		public void stopPolling() {
			this.keepPolling = false;
		}

		public void run() {

			while (keepPolling) {
				// Fetch data
				double heading   = 180L;
				double latitude  = 0L;
				double longitude = 0L;

				String strLat = System.getProperty("device.lat");
				String strLng = System.getProperty("device.lng");
				if (strLat != null && strLng != null) {
					try {
						latitude = Double.parseDouble(strLat);
						longitude = Double.parseDouble(strLng);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}

				// Ping the Server
				String resource = String.format("%s/mux/cache", this.baseURL);
				try {
					String response = HTTPClient.doGet(resource, null);
//					System.out.println("Cache:" + response);
					alreadyRaisedConnectException = false;
					Gson gson = new GsonBuilder().create();
					StringReader stringReader = new StringReader(response);
					Map<String, Object> cache = gson.fromJson(stringReader, Map.class);
//					System.out.println("Cache > " + cache.toString());
					try {
						latitude = ((Double) ((Map<String, Object>) cache.get("Position")).get("lat")).doubleValue();
					} catch (NullPointerException npe) {
						if (httpVerbose) {
							System.out.println("No Latitude in the cache");
						}
					} catch (Exception ex) {
						if (httpVerbose) {
							ex.printStackTrace();
						}
					}
					try {
						longitude = ((Double) ((Map<String, Object>) cache.get("Position")).get("lng")).doubleValue();
					} catch (NullPointerException npe) {
						if (httpVerbose) {
							System.out.println("No Longitude in the cache");
						}
					} catch (Exception ex) {
						if (httpVerbose) {
							ex.printStackTrace();
						}
					}
					try {
						heading = ((Double) ((Map<String, Object>) cache.get("HDG mag.")).get("angle")).doubleValue();
					} catch (NullPointerException npe) {
						if (httpVerbose) {
							System.out.println("No heading in the cache");
						}
					} catch (Exception ex) {
						if (httpVerbose) {
							ex.printStackTrace();
						}
					}
				} catch (/*ConnectException | */ SocketException ce) {
					if (!alreadyRaisedConnectException) {
						System.out.println(              "+------------------------------------------------------------------------------------");
						System.out.println(String.format("| >>> %s:NMEA Thread connecting to %s: %s", NumberFormat.getInstance().format(System.currentTimeMillis()), resource, ce.toString()));
						System.out.println(              "+------------------------------------------------------------------------------------");
						alreadyRaisedConnectException = true;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				featureManager.setDevicePosition(latitude, longitude);
				featureManager.setDeviceHeading(heading);
				if (httpVerbose) {
					System.out.println(String.format(">> From the cache: lat: %.02f, lng: %.02f, hdg: %.02f", latitude, longitude, heading));
				}

				try {
					Thread.sleep(1_000L); //
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			System.out.println("NMEA Thread completed.");
		}
	}
	private NMEADataThread nmeaDataThread = null;

	public FeatureRequestManager() throws Exception {
		this(null);
	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 *
	 */
	public FeatureRequestManager(SunFlowerServer parent) throws Exception {

		this.featureManager = new SunFlowerDriver();
		// The heart of the system... Implement the listener.
		this.featureManager.subscribe(new SunFlowerDriver.SunFlowerEventListener() {

			@Override
			public void onNewMessage(SunFlowerDriver.EventType messageType, Object messagePayload) {
//				System.out.println(String.format("%s => %s", messageType, messagePayload));
				dataCache.put(messageType.toString(), messagePayload);
			}
		});

		this.sunFlowerServer = parent;
		restImplementation = new RESTImplementation(this);
		restImplementation.setFeatureManager(this.featureManager);

		this.featureManager.init(); // Takes care of the system variables as well.
		Thread featureThread = new Thread(() -> {
			this.featureManager.start();
		}, "feature-thread");
		featureThread.start();

		if ("true".equals(System.getProperty("ping.nmea.server", "false"))) {
			String serverUrl = System.getProperty("nmea.server.base.url", "http://localhost:9999");
			if (httpVerbose) {
				System.out.println(String.format(">>> Starting NMEA thread on %s", serverUrl));
			}
			// Will get data from /mux/cache (See RESTNavServer and NMEAMultiplexer).
			// Examples: nmea.mux.hmc5883l.properties and nmea.mux.hmc5883l.yaml, nmea.mux.hmc5883l.oled.yaml,
			nmeaDataThread = new NMEADataThread("nmea-thread", serverUrl);
			nmeaDataThread.start();
		}

	}

	private Map<String, Object> dataCache = new HashMap<>();
	public synchronized  Map<String, Object> getDataCache() {
		return this.dataCache;
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return as defined in the {@link RESTImplementation}
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
		if (this.httpVerbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	@Override
	public List<HTTPServer.Operation> getRESTOperationList() {
		return restImplementation.getOperations();
	}

	/*
	 Specific operations
	 */

	protected List<HTTPServer.Operation> getAllOperationList() {
		return sunFlowerServer.getAllOperationList();
	}

}
