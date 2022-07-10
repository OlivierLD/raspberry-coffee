package httpserver;

import com.pi4j.io.gpio.Pin;
import http.HTTPServer;
import http.RESTRequestManager;
import relay.RelayManager;
import sensors.ADCChannel;
import utils.PinUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestManager implements RESTRequestManager {

	private final boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private final RESTImplementation restImplementation;

	private HttpRequestServer httpRequestServer = null;
	// Physical
	private RelayManager relayManager = null;
	private ADCChannel adcChannel = null;

	public HttpRequestManager() throws Exception {
		this(null);
	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public HttpRequestManager(HttpRequestServer parent) throws Exception {
		// Relay map
		String mapStr = System.getProperty("relay.map", "1:11,2:12");
		//                                                         | |  | |
		//                                                         | |  | Physical pin #12 (GPIO_1)
		//                                                         | |  Relay num for this app
		//                                                         | Physical pin #11 (GPIO_0)
		//                                                         Relay num for this app
		int miso = 0, mosi = 10, clk = 11, cs = 8, channel = 0;

		String misoStr = System.getProperty("miso.pin", String.valueOf(miso));
		String mosiStr = System.getProperty("mosi.pin", String.valueOf(mosi));
		String clkStr  = System.getProperty("clk.pin", String.valueOf(clk));
		String csStr   = System.getProperty("cs.pin", String.valueOf(cs));

		String adcChannelStr   = System.getProperty("adc.channel", String.valueOf(channel));

		Map<Integer, Pin> relayMap = null;
		try {
			relayMap = buildRelayMap(mapStr);
			if ("true".equals(System.getProperty("server.verbose", "false"))) {
				relayMap.entrySet().forEach(entry -> System.out.printf("Relay #%d mapped to pin %d (%s) \n",
						entry.getKey(),
						PinUtil.findByPin(entry.getValue()).pinNumber(),
						PinUtil.findByPin(entry.getValue()).pinName() ));
			}
		} catch (Exception ex) {
			throw ex;
		}
		try {
			miso = Integer.parseInt(misoStr);
			mosi = Integer.parseInt(mosiStr);
			clk = Integer.parseInt(clkStr);
			cs = Integer.parseInt(csStr);
			channel = Integer.parseInt(adcChannelStr);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		if ("true".equals(System.getProperty("server.verbose", "false"))) {
			System.out.printf("MISO:%d MOSI:%d CLK:%d CS:%d, Channel:%d\n", miso, mosi, clk, cs, channel);
		}
		this.relayManager = new RelayManager(relayMap);
		this.adcChannel = new ADCChannel(miso, mosi, clk, cs, channel);

		this.httpRequestServer = parent;
		restImplementation = new RESTImplementation(this);
		restImplementation.setRelayManager(this.relayManager);
		restImplementation.setADCChannel(this.adcChannel);
	}

	public void onExit() { // Cleanup
		if ("true".equals(System.getProperty("server.verbose", "false"))) {
			System.out.println("Cleaning up - HttpRequestManager");
		}
		this.relayManager.shutdown();
		this.adcChannel.close();
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
				Pin physicalNumber = PinUtil.getPinByPhysicalNumber(pinNum);
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
	 * @return response as defined in the {@link RESTImplementation}
	 * @throws UnsupportedOperationException When op is not registered
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
		return httpRequestServer.getAllOperationList();
	}

}
