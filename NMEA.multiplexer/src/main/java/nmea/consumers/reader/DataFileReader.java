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
	private boolean loop = true;

	public DataFileReader(List<NMEAListener> al, String fName) {
		this(al, fName, 500);
	}
	public DataFileReader(List<NMEAListener> al, String fName, long pause) {
		super(al);
		if (verbose) {
			System.out.println(this.getClass().getName() + ": There are " + al.size() + " listener(s)");
		}
		this.dataFileName = fName;
		this.betweenRecords = pause;
	}

	public String getFileNme() {
		return this.dataFileName;
	}
	public long getBetweenRecord() { return this.betweenRecords; }
	public boolean getLoop() {
		return this.loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	@Override
	public void startReader() {
		super.enableReading();
		try {
			this.fis = new FileInputStream(this.dataFileName);
			while (canRead()) {
				double size = Math.random();
				int dim = 1 + ((int) (750 * size)); // At least 1, no zero. Random size of the data chunk to read.
				byte[] ba = new byte[dim];
				try {
					int l = fis.read(ba);
//      System.out.println("Read " + l);
					if (l != -1 && dim > 0) { // dim should be always greater than 0
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
						this.fis.close();
						if (loop) {
							if (verbose) {
								System.out.println(String.format("Read:%d, Dim:%d (size: %f)", l, dim, size));
								System.out.println("===== Reseting Reader =====");
							}
							this.fis = new FileInputStream(this.dataFileName); // reopen
						} else {
							if (true || verbose) {
								System.out.println(">> End of stream. Not looping. <<");
							}
							break;
						}
					}
				} catch (IOException ioe) { // stream may hve been closed (loop = false)
					ioe.printStackTrace();
				}
			}
			if (loop) {
				try {
					this.fis.close();
				} catch (IOException ioe) {
					// Absorb.
					System.err.println("OnClose:");
					ioe.printStackTrace();
				}
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
