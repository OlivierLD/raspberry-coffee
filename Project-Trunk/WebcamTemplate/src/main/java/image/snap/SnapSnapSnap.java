package image.snap;

import utils.TimeUtil;

import java.io.IOException;

/**
 * -Dsnap.verbose
 */
public class SnapSnapSnap extends Thread {

	private int rot = 0;
	private int width = 640;
	private int height = 480;
	private long wait = 1_000L;
	private String snapName = "snap.jpg";

	public String getSnapName() {
		return snapName;
	}

	public void setSnapName(String snapName) {
		this.snapName = snapName;
	}

	public int getRot() {
		return rot;
	}

	public void setRot(int rot) {
		this.rot = rot;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long getWait() {
		return wait;
	}

	public void setWait(long wait) {
		this.wait = wait;
	}

	public void setKeepSnapping(boolean keepSnapping) {
		this.keepSnapping = keepSnapping;
	}

	// The --timeout seem to degrade the quality of the picture, specially outside...
	private final static String SNAPSHOT_COMMAND_1 = "raspistill -rot %d --width %d --height %d --output %s --nopreview"; // --timeout 1

	// For a webcam
	// Requires sudo apt-get install fswebcam
	// See http://www.raspberrypi.org/documentation/usage/webcams/ for some doc.
	private final static String SNAPSHOT_COMMAND_2 = "fswebcam snap%s.jpg";

	// Slow motion:
	private final static String SNAPSHOT_COMMAND_3 = "raspivid -w 640 -h 480 -fps 90 -t 30000 -o vid.h264";

	public static String snap(String name, int rot, int width, int height)
			throws Exception {
		Runtime rt = Runtime.getRuntime();
		// NOTE: The web directory has to exist on the Raspberry Pi
		String snapshotName = name; // String.format("web/%s.jpg", name);
		try {
			String command = String.format(SNAPSHOT_COMMAND_1, rot, width, height, snapshotName);
			if ("true".equals(System.getProperty("snap.verbose", "false"))) {
				System.out.println("Snapshot name will be " + snapshotName);
				System.out.println(String.format("HTTPLogger Executing [%s]", command));
			}
			long before = System.currentTimeMillis();
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

	// TODO rot, width, height, wait
	public SnapSnapSnap() {
		super();
	}
	public SnapSnapSnap(String threadName) {
		super(threadName);
	}

	private boolean keepSnapping = true;

	public void stopSnapping() {
		this.keepSnapping = false;
	}

	@Override
	public void run() {
		while (this.keepSnapping) {
			try {
				SnapSnapSnap.snap(this.snapName, this.rot, this.width, this.height);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// Wait...
			TimeUtil.delay(this.wait);
		}
	}

}
