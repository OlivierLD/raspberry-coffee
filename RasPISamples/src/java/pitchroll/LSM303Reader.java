package pitchroll;

import com.pi4j.io.i2c.I2CFactory;
import i2c.sensor.LSM303;
import java.io.IOException;

/**
 * Feeds a WebSocket server with pitch and roll data.
 */
public class LSM303Reader {

	private static boolean verbose = "true".equals(System.getProperty("lsm303.verbose", "false"));
	private LSM303 lsm303;

	private static final long BETWEEN_LOOPS = 1_000L; // TODO: Make it an external parameter?
	private static boolean read = true;

	public LSM303Reader() {
		try {
			this.lsm303 = new LSM303();
		} catch (I2CFactory.UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void startReader() {
		this.lsm303.startReading();
		while (read) {
			// Read data every 1 second
			try {
				double pitch = lsm303.getPitch();
				double roll  = lsm303.getRoll();
				// TODO Feed WS
				if (verbose) {
					System.out.println(String.format("Pitch: %.02f, roll: %.02f", pitch, roll));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(BETWEEN_LOOPS);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println(String.format(">>> %s done reading. Bye.", this.getClass().getName()));
		try {
			this.closeReader();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			Thread.sleep(2_000L);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	public void closeReader() throws Exception {
		this.lsm303.setKeepReading(false);
	}

	public static void main(String... args) {
		LSM303Reader reader = new LSM303Reader();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			read = false;
		}));

		reader.startReader();

		read = false;
	}
}