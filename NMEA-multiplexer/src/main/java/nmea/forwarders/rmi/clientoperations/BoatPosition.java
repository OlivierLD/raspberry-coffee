package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;
import nmea.parser.GeoPos;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class BoatPosition implements Task<GeoPos>, Serializable {

	private static final long serialVersionUID = 227L;

	public BoatPosition() {
	}

	public GeoPos execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		GeoPos position = null;
		if (cache != null) {
			position = (GeoPos)cache.get(NMEADataCache.POSITION);
		}
		return position;
	}
}
