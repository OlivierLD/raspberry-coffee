package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 *
 * This one could be used to implement a full console.
 */
public class NMEACache implements Task<NMEADataCache>, Serializable {

	private static final long serialVersionUID = 227L;

	public NMEACache() {
	}

	public NMEADataCache execute() {
		return ApplicationContext.getInstance().getDataCache();
	}
}
