package nmea.forwarders.rmi.clientoperations;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;
import nmea.parser.Angle360;
import nmea.parser.GeoPos;
import nmea.parser.Speed;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 */
public class TrueWind implements Task<nmea.parser.TrueWind>, Serializable {

	private static final long serialVersionUID = 227L;

	public TrueWind() {
	}

	public nmea.parser.TrueWind execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		nmea.parser.TrueWind tw = null;
		if (cache != null) {
			Speed speed = (Speed) cache.get(NMEADataCache.TWS);
			Angle360 dir = (Angle360)cache.get(NMEADataCache.TWD);
			tw = new nmea.parser.TrueWind((int)Math.round(dir.getValue()), speed.getValue());
		}
		return tw;
	}
}
