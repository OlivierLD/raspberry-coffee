package computers;

import nmea.api.Multiplexer;
import servers.Forwarder;

public abstract class Computer implements Forwarder {

	private Multiplexer multiplexer;

	public Computer(Multiplexer mux){
		this.multiplexer = mux;
	}

	protected synchronized void produce(String mess) {
		this.multiplexer.onData(mess);
	}
}
