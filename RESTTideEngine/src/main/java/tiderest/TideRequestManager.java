package tiderest;

import http.HTTPServer;
import http.RESTRequestManager;
import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.util.List;
import java.util.Map;

public class TideRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private RESTImplementation restImplementation;

	private List<Coefficient> constSpeed = null;
	private List<TideStation> stationData = null;
	private Map<String, String> coeffDefinitions = null;
	private TideServer tideServer = null;

	private BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

	public TideRequestManager() {
		this(null);
	}
	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public TideRequestManager(TideServer parent) {
		this.tideServer = parent;
		restImplementation = new RESTImplementation(this);

		try {
			backEndTideComputer.connect();
			backEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public BackEndTideComputer getBackEndTideComputer() {
		return this.backEndTideComputer;
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
		return tideServer.getAllOperationList();
	}

	protected List<Coefficient> getConstSpeed() throws Exception {
		try {
			if (this.constSpeed == null) {
//			System.out.println("Creating constants list");
				this.constSpeed = BackEndTideComputer.buildSiteConstSpeed();
			}
			return this.constSpeed;
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * All strings UTF-8 Encoded.
	 *
	 * @return
	 * @throws Exception
	 */
	protected List<TideStation> getStationList() throws Exception {
		try {
			if (this.stationData == null) {
//			System.out.println("Creating stations list");
				this.stationData = BackEndTideComputer.getStationData();
			}
			return this.stationData;
		} catch (Exception ex) {
			throw ex;
		}
	}

	protected Map<String, String> getCoeffDefinitions() {
		if (this.coeffDefinitions == null) {
			this.coeffDefinitions = TideUtilities.COEFF_DEFINITION;
		}
		return this.coeffDefinitions;
	}
}
