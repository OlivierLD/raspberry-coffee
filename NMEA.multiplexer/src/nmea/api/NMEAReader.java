package nmea.api;

import java.util.List;

import javax.swing.text.Utilities;

/**
 * A Model. This is an abstract class to extend to implement your own data-source.
 * Examples are given for a file containing the data - that can be used as a simulator,
 * and for a Serial Port, that can be used for the real world.
 *
 * @author Olivier Le Diouris
 * @version 1.0
 */
public abstract class NMEAReader extends Thread {
	private List<NMEAListener> NMEAListeners = null; // new ArrayList(2);

	protected boolean goRead = true;
	private NMEAReader instance = this;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	protected boolean verbose = false;

	public NMEAReader() {
		this(null, false);
	}

	public NMEAReader(List<NMEAListener> al) {
		this(al, false);
	}

	public NMEAReader(List<NMEAListener> al, boolean verbose) {
		this.verbose = verbose;
		if (verbose)
			System.out.println(this.getClass().getName() + ":Creating reader");
		NMEAListeners = al;
		this.addNMEAListener(new NMEAListener() {
			public void stopReading(NMEAEvent e) {
				System.out.println("- Stop reading " + instance.getClass().getName());
				goRead = false;
			}
		});
	}

	/**
	 * The one that tells the Controller to start working
	 *
	 * @see nmea.api.NMEAParser
	 */
	protected void fireDataRead(NMEAEvent e) {
		for (int i = 0; i < NMEAListeners.size(); i++) {
			NMEAListener l = NMEAListeners.get(i);
			l.dataRead(e);
		}
	}

	protected void fireStopReading(NMEAEvent e) {
		for (int i = 0; i < NMEAListeners.size(); i++) {
			NMEAListener l = NMEAListeners.get(i);
			l.stopReading(e);
		}
	}

	public synchronized void addNMEAListener(NMEAListener l) {
		if (!NMEAListeners.contains(l)) {
			NMEAListeners.add(l);
		}
	}

	public synchronized void removeNMEAListener(NMEAListener l) {
		NMEAListeners.remove(l);
	}

	public boolean canRead() {
		return goRead;
	}

	public void enableReading() {
		this.goRead = true;
	}

	/**
	 * Customize, overwrite this class to get plugged on the right datasource
	 * like a Serial Port for example.
	 */
	public abstract void read() throws Exception;

	public abstract void closeReader() throws Exception;

	public void run() {
		if (verbose) // nmea.api.NMEAReader
			System.out.println(this.getClass().getName() + ":Reader Running");
		try {
			read();
		} catch (Exception ex) {
			for (int i = 0; i < NMEAListeners.size(); i++) {
				NMEAListener l = NMEAListeners.get(i);
				l.fireError(ex);
			}
			throw new RuntimeException(ex);
		}
	}
}
