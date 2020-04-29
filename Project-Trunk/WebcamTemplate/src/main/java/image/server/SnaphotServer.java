package image.server;

import http.HTTPServer;
import image.snap.SnapSnapSnap;

import java.util.List;
import java.util.stream.Collectors;

public class SnaphotServer {
	private HTTPServer httpServer = null;
	private int httpPort = 1234;

	protected static SnapSnapSnap snap = null;
	public final static String SNAP_NAME = "web/snap.jpg";

	public SnaphotServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.println(String.format("Running on port %d", httpPort));
		this.httpServer = startHttpServer(httpPort, new SnapRequestManager(this));
		// Add other features here if needed...
//	this.httpServer.addRequestManager(new AstroRequestManager());

		snap = new SnapSnapSnap("SnapThread");
		snap.setSnapName(SNAP_NAME);
		snap.setRot(180);
		snap.start();

	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {
		new SnaphotServer();
	}

	public HTTPServer startHttpServer(int port, SnapRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager) {
				public void onExit() {
					// Reset the servos to zero before closing.
					System.out.println(">> Stopping snap before shutting down.");
					if (snap != null) {
						snap.stopSnapping();
					}
					try {
						Thread.sleep(2_000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			};
			newHttpServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}
