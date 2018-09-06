package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Taking its inputs from a file
 */
public class DataFileReader extends NMEAReader {
	private String dataFileName = null;
	private FileInputStream fis;
	private long betweenRecords = 500L;

	public DataFileReader(List<NMEAListener> al, String fName) {
		this(al, fName, 500);
	}
	public DataFileReader(List<NMEAListener> al, String fName, long pause) {
		super(al);
		if (verbose)
		  System.out.println(this.getClass().getName() + ": There are " + al.size() + " listener(s)");
		this.dataFileName = fName;
		this.betweenRecords = pause;
	}

	public String getFileNme() {
		return this.dataFileName;
	}
	public long getBetweenRecord() { return this.betweenRecords; }

	@Override
	public void startReader() {
		super.enableReading();
		try {
			this.fis = new FileInputStream(this.dataFileName);
			while (canRead()) {
				double size = Math.random();
				int dim = 1 + ((int) (750 * size)); // At least 1, no zero
				byte[] ba = new byte[dim];
				int l = fis.read(ba);
//      System.out.println("Read " + l);
				if (l != -1 && dim > 0) {
					String nmeaContent = new String(ba);
					if (verbose) {
						System.out.println("Spitting out [" + nmeaContent + "]");
					}
					fireDataRead(new NMEAEvent(this, nmeaContent));
					try {
						Thread.sleep(this.betweenRecords);
					} catch (Exception ignore) {
						System.err.println("Err when trying to sleep:");
						ignore.printStackTrace();
					}
				} else {
					if (verbose) {
						System.out.println(String.format("Read:%d, Dim:%d (size: %f)", l, dim, size));
						System.out.println("===== Reseting Reader =====");
					}
					this.fis.close();
					this.fis = new FileInputStream(this.dataFileName); // reopen
				}
			}
			try {
				this.fis.close();
			} catch (IOException ioe) {
				// Absorb.
				System.err.println("OnClose:");
				ioe.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void closeReader() throws Exception {
		if (this.fis != null) {
			try {
				this.fis.close();
			} catch (IOException ioe) {
				// Absorb.
			}
		}
	}
}
