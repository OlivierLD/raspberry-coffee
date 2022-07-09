package httpserver;

import com.pi4j.io.gpio.Pin;
import http.HTTPServer;
import http.RESTRequestManager;
import relay.RelayManager;
import utils.PinUtil;
import utils.gpio.StringToGPIOPin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelayRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private RelayServer relayServer = null;
	private RelayManager relayManager = null; // Physical

	public RelayRequestManager() throws Exception {
		this(null);
	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public RelayRequestManager(RelayServer parent) throws Exception {
		// Relay map
		String mapStr = System.getProperty("relay.map", "1:11,2:12");
		//                                                         | |  | |
		//                                                         | |  | Physical pin #12 (GPIO_1)
		//                                                         | |  Relay num for this app
		//                                                         | Physical pin #11 (GPIO_0)
		//                                                         Relay num for this app
		Map<Integer, Pin> relayMap = null;
		try {
			relayMap = buildRelayMap(mapStr);
			if ("true".equals(System.getProperty("relay.verbose", "false"))) {
				relayMap.entrySet().forEach(entry -> {
					System.out.println(String.format("Relay #%d mapped to pin %d (%s) ",
							entry.getKey(),
							PinUtil.findByPin(entry.getValue().getName()).pinNumber(),
							PinUtil.findByPin(entry.getValue().getName()).pinName() ));
				});
			}
		} catch (Exception ex) {
			throw ex;
		}
		this.relayManager = new RelayManager(relayMap);
		this.relayServer = parent;
		restImplementation = new RESTImplementation(this);
		restImplementation.setRelayManager(this.relayManager);
	}

	private Map<Integer, Pin> buildRelayMap(String strMap) {
		Map<Integer, Pin> map = new HashMap<>();
		String[] array = strMap.split(",");
		Arrays.stream(array).forEach(relayPrm -> {
			String[] tuple = relayPrm.split(":");
			if (tuple == null || tuple.length != 2) {
				throw new RuntimeException(String.format("In [%s], bad element [%s]", strMap, relayPrm));
			}
			try {
				int relayNum = Integer.parseInt(tuple[0]);
				int pinNum = Integer.parseInt(tuple[1]);
				Pin physicalNumber = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(pinNum));
				if (physicalNumber == null) {
					throw new RuntimeException(String.format("In [%s], element [%s], pin #%d does not exist", strMap, relayPrm, pinNum));
				}
				map.put(relayNum, physicalNumber);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(String.format("In [%s], element [%s], bad numbers", strMap, relayPrm));
			}
		});

		return map;
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
		return relayServer.getAllOperationList();
	}

}
