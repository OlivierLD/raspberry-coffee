package astrorest;

import http.HTTPServer;
import http.RESTRequestManager;
import utils.TimeUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class AstroRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;


	// See http://maia.usno.navy.mil/ser7/deltat.data
	private static double deltaT = 68.8033;// June 2017
	static {
		String deltaTStr = System.getProperty("deltaT", String.valueOf(deltaT));
		if (deltaTStr.equals("AUTO")) {
			Calendar now = GregorianCalendar.getInstance();
			deltaT = TimeUtil.getDeltaT(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
		} else if (deltaTStr != null) {
			deltaT = Double.parseDouble(deltaTStr);
		}
	}

	public AstroRequestManager() {
		System.out.println(String.format("Using Delta-T:%f", deltaT));
		restImplementation = new RESTImplementation(this);
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
}
