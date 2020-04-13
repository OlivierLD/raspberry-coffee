package nmea.computers;

import nmea.api.Multiplexer;
import nmea.forwarders.Forwarder;

import java.util.Properties;

public abstract class Computer implements Forwarder {

	private Multiplexer multiplexer;

	protected boolean verbose = false;
	protected Properties props = null;

	public Computer(Multiplexer mux){
		this.multiplexer = mux;
	}

	/**
	 * Broadcasts data to all forwarders and computers.
	 * @param mess
	 */
	protected synchronized void produce(String mess) {
		this.multiplexer.onData(mess);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setProperties(Properties props) {
		this.props = props;
	}
}
