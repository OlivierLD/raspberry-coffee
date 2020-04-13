package httpserver;

import com.pi4j.io.gpio.Pin;
import http.HTTPServer;
import http.RESTRequestManager;
import sensors.ADCChannel;
import utils.PinUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private HttpRequestServer httpRequestServer = null;
	// Physical
	private ADCChannel adcChannel = null;

	public HttpRequestManager() throws Exception {
		this(null);
	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public HttpRequestManager(HttpRequestServer parent) throws Exception {

		int miso = 0, mosi = 10, clk = 11, cs = 8, channel = 0;

		String misoStr = System.getProperty("miso.pin", String.valueOf(miso));
		String mosiStr = System.getProperty("mosi.pin", String.valueOf(mosi));
		String clkStr  = System.getProperty("clk.pin", String.valueOf(clk));
		String csStr   = System.getProperty("cs.pin", String.valueOf(cs));

		String adcChannelStr   = System.getProperty("adc.channel", String.valueOf(channel));

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
			System.out.println(String.format("MISO:%d MOSI:%d CLK:%d CS:%d, Channel:%d", miso, mosi, clk, cs, channel));
		}
		this.adcChannel = new ADCChannel(miso, mosi, clk, cs, channel);

		this.httpRequestServer = parent;
		restImplementation = new RESTImplementation(this);
		restImplementation.setADCChannel(this.adcChannel);
	}

	public void onExit() { // Cleanup
		if ("true".equals(System.getProperty("server.verbose", "false"))) {
			System.out.println("Cleaning up - HttpRequestManager");
		}
		this.adcChannel.close();
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return response as defined in the {@link RESTImplementation}
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
		return httpRequestServer.getAllOperationList();
	}

}
