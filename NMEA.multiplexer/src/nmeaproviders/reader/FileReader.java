package nmeaproviders.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * A Simulator, taking its inputs from a file
 */
public class FileReader extends NMEAReader {
	private String dataFileName = null;
	private FileInputStream fis;

	public FileReader(List<NMEAListener> al, String fName) {
		super(al);
		System.out.println("There are " + al.size() + " listener(s)");
		this.dataFileName = fName;
	}

	public String getFileNme() {
		return this.dataFileName;
	}

	@Override
	public void read() {
		super.enableReading();
		try {
			this.fis = new FileInputStream(this.dataFileName);
			while (canRead()) {
				double size = Math.random();
				int dim = (int) (750 * size);
				byte[] ba = new byte[dim];
				int l = fis.read(ba);
//      System.out.println("Read " + l);
				if (l != -1 && dim > 0) {
					String nmeaContent = new String(ba);
					if ("true".equals(System.getProperty("verbose", "false")))
						System.out.println("Spitting out [" + nmeaContent + "]");
					fireDataRead(new NMEAEvent(this, nmeaContent));
					try {
						Thread.sleep(500);
					} catch (Exception ignore) {
					}
				} else {
					System.out.println("===== Reseting Reader =====");
					this.fis.close();
					this.fis = new FileInputStream(this.dataFileName); // reopen
				}
			}
			try {
				this.fis.close();
			} catch (IOException ioe) {
				// Absorb.
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