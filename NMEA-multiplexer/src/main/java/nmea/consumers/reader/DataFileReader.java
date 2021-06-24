package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;
import utils.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Taking its inputs from a file
 */
public class DataFileReader extends NMEAReader {
	private String dataFileName = null;
	private InputStream fis;
	private long betweenRecords = 500L;
	private boolean loop = true;
	private boolean zip = false;
	private String pathInArchive = "";

	public DataFileReader(List<NMEAListener> al, String fName) {
		this(null, al, fName, 500, false, null);
	}
	public DataFileReader(String threadName, List<NMEAListener> al, String fName, long pause) {
		this(threadName, al, fName, pause, false, null);
	}
	public DataFileReader(String threadName, List<NMEAListener> al, String fName) {
		this(threadName, al, fName, 500, false, null);
	}
	public DataFileReader(List<NMEAListener> al, String fName, long pause) {
		this(null, al, fName, pause, false, null);
	}
	public DataFileReader(String threadName, List<NMEAListener> al, String fName, long pause, boolean isZip, String pathInZip) {
		this(threadName, al, fName, 500, true,false, null);
	}
	public DataFileReader(String threadName, List<NMEAListener> al, String fName, long pause, boolean loop, boolean isZip, String pathInZip) {
		super(threadName, al);
		if (verbose) {
			System.out.println(this.getClass().getName() + ": There are " + al.size() + " listener(s)");
		}
		this.dataFileName = fName;
		this.betweenRecords = pause;
		this.zip = isZip;
		this.pathInArchive = pathInZip;
		this.loop = loop;
	}

	public String getFileName() {
		return this.dataFileName;
	}
	public long getBetweenRecord() { return this.betweenRecords; }
	public boolean getLoop() {
		return this.loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	public boolean getZip() {
		return zip;
	}
	public String getPathInArchive() {
		return pathInArchive;
	}

	@Override
	public void startReader() {
		super.enableReading();
		try {
			if (this.getZip()) {
				ZipFile zipFile = new ZipFile(this.dataFileName);
//				Enumeration<? extends ZipEntry> entries = zipFile.entries();
//				while (entries.hasMoreElements()) {
//					ZipEntry entry = entries.nextElement();
//					if (entry.isDirectory()) {
//						System.out.println(String.format(">> In archive: dir  : %s", entry.getName()));
//					} else {
//						System.out.println(String.format(">> In archive: file : %s", entry.getName()));
//					}
//				}
				ZipEntry zipEntry = zipFile.getEntry(this.pathInArchive); // Used if zip=true
				if (zipEntry == null) { // Path not found in the zip, take first entry.
					zipEntry = zipFile.entries().nextElement();
				}
				this.fis = zipFile.getInputStream(zipEntry);
			} else {
				this.fis = new FileInputStream(this.dataFileName);
			}
			while (this.canRead()) {
				double size = Math.random();
				int dim = 1 + ((int) (750 * size)); // At least 1, no zero. Random size of the data chunk to read.
				byte[] ba = new byte[dim];
				try {
					int l = fis.read(ba);
//      System.out.println("Read " + l);
					if (l != -1 && dim > 0) { // dim should be always greater than 0
						String nmeaContent = new String(ba);
						if (this.getZip()) { // Workaround. From a zip, some NULs have been seen sneaking in the string...
							nmeaContent = StringUtils.removeNullsFromString(nmeaContent);
						}
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
						if (this.loop) {
							if (this.verbose || true) {
								System.out.println(String.format("Read:%d, Dim:%d (size: %f)", l, dim, size));
								System.out.println("===== Resetting Reader =====");
							}
							if (this.getZip()) {
								ZipFile zipFile = new ZipFile(this.dataFileName);
								ZipEntry zipEntry = zipFile.getEntry(this.pathInArchive); // Mandatory if zip=true
								if (zipEntry == null) { // Path not found in the zip, take first entry.
									zipEntry = zipFile.entries().nextElement();
								}
								this.fis = zipFile.getInputStream(zipEntry);
							} else {
								this.fis = new FileInputStream(this.dataFileName);
							}
						} else {
							if (true || this.verbose) {
								System.out.println(">> End of stream. Not looping. <<");
							}
							break;
						}
					}
				} catch (IOException ioe) { // stream may have been closed (loop = false)
					ioe.printStackTrace();
				}
			}
			if (this.loop) {
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
