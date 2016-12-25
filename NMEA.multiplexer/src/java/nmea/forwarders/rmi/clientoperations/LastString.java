package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class LastString implements Task<String>, Serializable {

	private static final long serialVersionUID = 227L;
	private String lastString = "None";

	public LastString() {
	}

	public String execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		if (cache != null) {
			this.lastString = (String)cache.get(NMEADataCache.LAST_NMEA_SENTENCE);
		}
		return this.lastString;
	}
}
