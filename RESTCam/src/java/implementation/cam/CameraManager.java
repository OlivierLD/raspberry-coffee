package implementation.cam;


import java.io.IOException;

public class CameraManager {
	private final static boolean verbose = true;
	private final static String SNAPSHOT_COMMAND_1 = "raspistill -rot 180 --width 200 --height 150 --timeout 1 --output %s/snap%s.jpg --nopreview";

	// For a webcam
	// Requires sudo apt-get install fswebcam
	// See http://www.raspberrypi.org/documentation/usage/webcams/ for some doc.
	private final static String SNAPSHOT_COMMAND_2 = "fswebcam snap%s.jpg";

	// Slow motion:
	private final static String SNAPSHOT_COMMAND_3 = "raspivid -w 640 -h 480 -fps 90 -t 30000 -o vid.h264";

	public static void snap(String name) {
		Runtime rt = Runtime.getRuntime();

		try {
			long before = System.currentTimeMillis();
			String command = String.format(SNAPSHOT_COMMAND_1, "web", name);
			Process snap = rt.exec(command);
			snap.waitFor(); // Sync
			long after = System.currentTimeMillis();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String... args) {
		snap("-test");
	}

}
