package fortest;

import http.HTTPServer;
import http.HTTPServerInterface;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;

import java.util.List;

public class One implements HTTPServerInterface {

	private boolean httpVerbose = false;
	private HTTPServer httpServer = null;
	private int httpPort = -1;
	private RESTImplementation restImplementation;

	public One() {
		try {
			BackEndTideComputer.connect();
			BackEndTideComputer.setVerbose(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		restImplementation = new RESTImplementation(this);
		startHttpServer(9999);
	}

	public static void main(String... args) {
		new One();
	}

	protected List<TideStation> getStationList() throws Exception {
		try {
			List<TideStation> stationData = BackEndTideComputer.getStationData();
			return stationData;
		} catch (Exception ex) {
			throw ex;
		}
	}

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

	public void startHttpServer(int port) {
		try {
			this.httpServer = new HTTPServer(port, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
