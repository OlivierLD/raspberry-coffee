package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;
import nmea.parser.Current;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class CalculatedCurrent implements Task<Current>, Serializable {

	private static final long serialVersionUID = 227L;

	public CalculatedCurrent() {
	}

	public Current execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		Current currentDefinition = null;
		if (cache != null) {
			currentDefinition = (Current) cache.get(NMEADataCache.VDR_CURRENT);
		}
		return currentDefinition;
	}
}
