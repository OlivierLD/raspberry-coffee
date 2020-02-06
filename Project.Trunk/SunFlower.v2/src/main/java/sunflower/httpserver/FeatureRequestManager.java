package sunflower.httpserver;

import com.pi4j.io.gpio.Pin;
import http.HTTPServer;
import http.RESTRequestManager;
import sunflower.SunFlowerDriver;
import utils.PinUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private SunFlowerServer sunFlowerServer = null;
	private SunFlowerDriver featureManager = null; // Physical

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
			this.featureManager.go();
		}, "feature-thread");
		featureThread.start();
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
