package image.server;

import http.HTTPServer;
import image.snap.SnapSnapSnap;
import org.opencv.core.Core;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SnaphotServer {
	private HTTPServer httpServer;
	private int httpPort = 1234;

	protected static SnapSnapSnap snap = null;
	private final static String SNAP_NAME    = "./%s/snap.jpg";    // Get directory from -Dstatic.docs, first element
	private final static String TX_SNAP_NAME = "./%s/snap_tx.jpg"; // Get directory from -Dstatic.docs, first element

	public static String snapshotName;
	public static String txSnapshotName;

	private String stripSeparators(String str) { // Remove first and last File.separator
		String stripped = str;
		while (stripped.startsWith(File.separator)) {
			stripped = stripped.substring(1);
		}
		while (stripped.endsWith(File.separator)) {
			stripped = stripped.substring(0, stripped.length() - 1);
		}
		return stripped;
	}

	public SnaphotServer() {

		if ("true".equals(System.getProperty("with.opencv", "true"))) {
			try {
				System.out.println("Loading " + Core.NATIVE_LIBRARY_NAME);
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			} catch (UnsatisfiedLinkError ule) {
				ule.printStackTrace();
			}
		}

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

		snapshotName = String.format(SNAP_NAME, stripSeparators(this.httpServer.getStaticDocumentsLocation().get(0)));
		txSnapshotName = String.format(TX_SNAP_NAME, stripSeparators(this.httpServer.getStaticDocumentsLocation().get(0)));

		if ("true".equals(System.getProperty("snap.verbose", "false"))) {
			System.out.println(String.format("Snapshots will be stored in %s", snapshotName));
			System.out.println(String.format("Transformed snapshots will be stored in %s", txSnapshotName));
		}

		snap = new SnapSnapSnap("SnapThread");
		snap.setSnapName(snapshotName);
		snap.setRot(180);
	}

	protected void setSnapThreadConfig(SnapSnapSnap.SnapStatus config) throws Exception {

		SnapSnapSnap.SnapConfig snapConfig = snap.getConfig();
		snapConfig.setHeight(config.getHeight());
		snapConfig.setWidth(config.getWidth());
		snapConfig.setRot(config.getRot());
		snapConfig.setWait(config.getWait());
		snapConfig.setSnapName(config.getSnapName());
		if ("true".equals(System.getProperty("snap.verbose", "false"))) {
			System.out.println("Setting SnapThread config");
		}
		snap.setConfig(snapConfig);
	}

	protected void startSnapThread(SnapSnapSnap.SnapStatus config) throws Exception {
		// TODO See if setSnapThreadConfig can be reused
		SnapSnapSnap.SnapConfig snapConfig = snap.getConfig();
		snapConfig.setHeight(config.getHeight());
		snapConfig.setWidth(config.getWidth());
		snapConfig.setRot(config.getRot());
		snapConfig.setWait(config.getWait());
		snapConfig.setSnapName(config.getSnapName());
		if ("true".equals(System.getProperty("snap.verbose", "false"))) {
			System.out.println("(re)starting SnapThread");
		}
		// New one? If TERMINATED, yes.
		if (Thread.State.TERMINATED.toString().equals(config.getState())) {
			if ("true".equals(System.getProperty("snap.verbose", "false"))) {
				System.out.println("\tCreating new Snap Thread.");
			}
			snap = new SnapSnapSnap("SnapThread");
		}
		snap.setConfig(snapConfig);
		snap.start();
	}

	protected void stopSnapThread() throws Exception {
		snap.stopSnapping();
	}

	protected void takeOneSnap() {

	}

	protected void startStopMotion() {

	}

	protected SnapSnapSnap.SnapStatus getSnapThreadStatus() throws Exception {
		if (this.snap != null) {
			return this.snap.getSnapStatus();
		} else {
			return null;
		}
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
			Properties props = new Properties();
			String staticDocPaths = System.getProperty("static.docs");
			if (staticDocPaths != null) {
				props.put("static.docs", staticDocPaths);
			}
			newHttpServer = new HTTPServer(port, requestManager, props) {
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
