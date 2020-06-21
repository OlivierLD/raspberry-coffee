package image.snap;

import utils.TimeUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

/**
 * -Dsnap.verbose
 */
public class SnapSnapSnap extends Thread {

	public static class SnapConfig {
		private int rot = 0;
		private int width = 640;
		private int height = 480;
		private long wait = 1_000L;
		private String snapName = "snap.jpg";

		public SnapConfig() {
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

		public String getSnapName() {
			return snapName;
		}

		public void setSnapName(String snapName) {
			this.snapName = snapName;
		}
	}
	private SnapConfig config = new SnapConfig();

	private static NumberFormat nf = NumberFormat.getInstance();

	public static class SnapStatus {
		private int rot = 0;
		private int width = 640;
		private int height = 480;
		private long wait = 1_000L;
		private String snapName = "snap.jpg";
		private boolean threadRunning = false;
		private String state = "";

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public SnapStatus() {
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

		public String getSnapName() {
			return snapName;
		}

		public void setSnapName(String snapName) {
			this.snapName = snapName;
		}

		public boolean isThreadRunning() {
			return threadRunning;
		}

		public void setThreadRunning(boolean threadRunning) {
			this.threadRunning = threadRunning;
		}
	}

	public SnapConfig getConfig() {
		return config;
	}

	public void setConfig(SnapConfig config) {
		this.config = config;
	}

	public String getSnapName() {
		return config.snapName;
	}

	public void setSnapName(String snapName) {
		this.config.snapName = snapName;
	}

	public int getRot() {
		return config.rot;
	}

	public void setRot(int rot) {
		this.config.rot = rot;
	}

	public int getWidth() {
		return config.width;
	}

	public void setWidth(int width) {
		this.config.width = width;
	}

	public int getHeight() {
		return config.height;
	}

	public void setHeight(int height) {
		this.config.height = height;
	}

	public long getWait() {
		return config.wait;
	}

	public void setWait(long wait) {
		this.config.wait = wait;
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

	public SnapStatus getSnapStatus() {
		SnapStatus snapStatus = new SnapStatus();
		snapStatus.setHeight(this.config.height);
		snapStatus.setWidth(this.config.width);
		snapStatus.setRot(this.config.rot);
		snapStatus.setSnapName(this.config.snapName);
		snapStatus.setWait(this.config.wait);
		snapStatus.setThreadRunning(this.isAlive());
		snapStatus.setState(this.getState().toString());
		return snapStatus;
	}

	public static String snap(String name, int rot, int width, int height)
			throws Exception {
		Runtime rt = Runtime.getRuntime();
		// NOTE: The web directory has to exist on the Raspberry Pi
		String snapshotName = name; // String.format("web/%s.jpg", name);
		try {
			String command = String.format(SNAPSHOT_COMMAND_1, rot, width, height, snapshotName);
			long before = System.currentTimeMillis();
			Process snap = rt.exec(command);
			final int exitValue = snap.waitFor(); // Sync
			long after = System.currentTimeMillis();
			if (exitValue != 0) {
				System.err.println("Failed to execute the following command: " + command + " due to the following error(s):");
				try (final BufferedReader b = new BufferedReader(new InputStreamReader(snap.getErrorStream()))) {
					String line;
					if ((line = b.readLine()) != null) {
						System.err.println(line);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if ("true".equals(System.getProperty("snap.verbose", "false"))) {
				System.out.println(String.format("%s Executing [%s], snapshot is in %s, (%s ms)",
						SnapSnapSnap.class.getName(),
						command,
						snapshotName,
						nf.format(after - before)));
			}
		} catch (InterruptedException ie) {
			throw ie;
		} catch (IOException ioe) {
			throw ioe;
		}
		return snapshotName;
	}

	// Config in the setConfig and such.
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
				SnapSnapSnap.snap(this.config.snapName, this.config.rot, this.config.width, this.config.height);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("snap.verbose", "false"))) {
					ex.printStackTrace();
				} else {
					System.err.println(ex.getMessage());
				}
			}
			// Wait...
			TimeUtil.delay(this.config.wait);
		}
	}

}
