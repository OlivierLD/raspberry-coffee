package computers;

import nmea.api.Multiplexer;
import nmea.forwarders.Forwarder;

public abstract class Computer implements Forwarder {

	private Multiplexer multiplexer;

	protected boolean verbose = false;

	public Computer(Multiplexer mux){
		this.multiplexer = mux;
	}

	protected synchronized void produce(String mess) {
		this.multiplexer.onData(mess);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
