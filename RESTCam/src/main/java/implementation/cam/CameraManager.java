package implementation.cam;


import java.io.IOException;

public class CameraManager {
	private final static boolean verbose = "true".equals(System.getProperty("cam.verbose", "false"));

	private final static String SNAPSHOT_COMMAND_1 = "raspistill -rot %d --width %d --height %d --timeout 1 --output %s --nopreview";

	// For a webcam
	// Requires sudo apt-get install fswebcam
	// See http://www.raspberrypi.org/documentation/usage/webcams/ for some doc.
	private final static String SNAPSHOT_COMMAND_2 = "fswebcam snap%s.jpg";

	// Slow motion:
	private final static String SNAPSHOT_COMMAND_3 = "raspivid -w 640 -h 480 -fps 90 -t 30000 -o vid.h264";

	private static int tilt = 0, heading = 0;

	public static String snap(String name, int rot, int width, int height)
			throws Exception {
		Runtime rt = Runtime.getRuntime();
		String snapshotName = String.format("web/%s.jpg", name);
		try {
			long before = System.currentTimeMillis();
			String command = String.format(SNAPSHOT_COMMAND_1, rot, width, height, snapshotName);
			if (verbose) {
				System.out.println(String.format("Executing [%s]", command));
			}
			Process snap = rt.exec(command);
			snap.waitFor(); // Sync
			long after = System.currentTimeMillis();
		} catch (InterruptedException ie) {
			throw ie;
		} catch (IOException ioe) {
			throw ioe;
		}
		return snapshotName;
	}

	public static int getTilt() {
		return tilt;
	}
	public static int getHeading() {
		return heading;
	}

	public static void setTilt(int t) {
		tilt = t;
	}
	public static void setHeading(int h) {
		heading = h;
	}

	public static void main(String... args) {
		try {
			snap("-test", 180, 640, 480);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
