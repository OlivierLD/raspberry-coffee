package nmea.api;

import java.util.List;

/**
 * A Model. This is an abstract class to extend to implement your own data-source.
 *
 * Examples are given for a file containing the data - that can be used as a simulator,
 * and for a Serial Port, that can be used for the real world.
 *
 * The NMEAReader reads the actual NMEA stream. It is invoked by the NMEAClient, that also has
 * an NMEAParser that makes sense of the NMEA Sentences it is being sent to process.
 *
 * The abstract {@link #startReader()} method is here to do the actual reading.
 *
 * @author Olivier Le Diouris
 * @version 1.0
 */
public abstract class NMEAReader extends Thread {
	private List<NMEAListener> NMEAListeners = null; // new ArrayList<>(2);

	protected boolean goRead = true;
	private NMEAReader instance = this;
	protected boolean verbose = false;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
		if (verbose && NMEAListeners != null) {
			System.out.println(this.getClass().getName() + ": There are " + this.NMEAListeners.size() + " listener(s)");
		}
	}

	public NMEAReader() {
		this(null, null, false);
	}

	public NMEAReader(String threadName) {
		this(threadName, null, false);
	}

	public NMEAReader(String threadName, List<NMEAListener> al) {
		this(threadName, al, false);
	}

	public NMEAReader(List<NMEAListener> al) {
		this(null, al, false);
	}

	public NMEAReader(List<NMEAListener> al, boolean verbose) {
		this(null, al, verbose);
	}
	public NMEAReader(String threadName, List<NMEAListener> al, boolean verbose) {
		super(threadName);
		this.verbose = verbose;
		if (verbose) {
			System.out.println(this.getClass().getName() + ":Creating reader");
		}
		NMEAListeners = al;
		this.addNMEAListener(new NMEAListener() {
			public void stopReading(NMEAEvent e) {
				System.out.println("- Stop reading " + instance.getClass().getName());
				goRead = false;
			}
		});
	}

	/*
	 * The one that tells the Controller to start working
	 *
	 * @see nmea.api.NMEAParser
	 */
	protected void fireDataRead(NMEAEvent e) {
		synchronized(this.NMEAListeners) {
			this.NMEAListeners.stream().forEach(listener -> {
				synchronized (listener) {
					listener.dataRead(e);
				}
			});
		}
	}

	protected void fireStopReading(NMEAEvent e) {
		synchronized(this.NMEAListeners) {
			this.NMEAListeners.stream().forEach(listener -> {
				synchronized (listener) {
					listener.stopReading(e);
				}
			});
		}
	}

	public synchronized void addNMEAListener(NMEAListener l) {
		if (!this.NMEAListeners.contains(l)) {
			synchronized(this.NMEAListeners) {
				this.NMEAListeners.add(l);
			}
		}
	}

	public synchronized void removeNMEAListener(NMEAListener l) {
		synchronized(this.NMEAListeners) {
			NMEAListeners.remove(l);
		}
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
	 *
	 * Use the {@link #canRead()} method to know if you can keep reading.
	 * @throws Exception when something goes wrong. No shit!
	 */
	public abstract void startReader() throws Exception;

	public abstract void closeReader() throws Exception;

	@Override
	public void run() {
		if (verbose) {
			System.out.println(String.format(">> %s: Reader Running", this.getClass().getName()));
		}
		try {
			startReader();
		} catch (Exception ex) {
			this.NMEAListeners.stream().forEach(listener -> listener.fireError(ex));
			throw new RuntimeException(ex);
		} finally {
			if (verbose) {
				System.out.println(String.format(">> %s: Reader Setting Completed", this.getClass().getName()));
			}
		}
	}
}
