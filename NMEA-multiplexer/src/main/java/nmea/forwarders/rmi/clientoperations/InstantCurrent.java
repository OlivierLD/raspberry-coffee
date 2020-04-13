package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;
import nmea.parser.Angle360;
import nmea.parser.Current;
import nmea.parser.Speed;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class InstantCurrent implements Task<Current>, Serializable {

	private static final long serialVersionUID = 227L;

	public InstantCurrent() {
	}

	public Current execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		Current currentDefinition = null;
		if (cache != null) {
			double cDir = ((Angle360) cache.get(NMEADataCache.CDR)).getValue();
			double cSpeed = ((Speed) cache.get(NMEADataCache.CSP)).getValue();

			currentDefinition = new Current((int)Math.round(cDir), cSpeed);
		}
		return currentDefinition;
	}
}
