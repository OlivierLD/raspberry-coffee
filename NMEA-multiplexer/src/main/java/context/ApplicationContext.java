package context;

import nmea.parser.Angle180EW;
import nmea.parser.GeoPos;
import nmea.utils.MuxNMEAUtils;
import nmea.utils.NMEAUtils;

import java.util.List;

public class ApplicationContext {

	private static ApplicationContext instance = null;
	NMEADataCache dataCache = null;

	private ApplicationContext() {
	}

	public NMEADataCache getDataCache() {
		if (this.dataCache != null) {
			GeoPos position = (GeoPos) this.dataCache.get(NMEADataCache.POSITION);
			if (position != null) {
				if (position.gridSquare == null || position.gridSquare.isEmpty()) {
					this.dataCache.put(NMEADataCache.POSITION, position.updateGridSquare());
				}
			}
		}
		return this.dataCache;
	}

	public static synchronized ApplicationContext getInstance() {
		if (instance == null) {
			instance = new ApplicationContext();
		}
		return instance;
	}

	public void initCache(String deviationFileName, // Default "zero-deviation.csv"
	                      double maxLeeway,         // Default 0
	                      double bspFactor,         // Default 1
	                      double awsFactor,         // Default 1
	                      double awaOffset,         // Default 0
	                      double hdgOffset,         // Default 0
	                      double defaultDeclination,// Default 0
	                      int damping) {            // Default 1

		dataCache = new NMEADataCache();

		try {
			List<double[]> deviationCurve = NMEAUtils.loadDeviationCurve(deviationFileName);
			dataCache.put(NMEADataCache.DEVIATION_FILE, deviationFileName);
			dataCache.put(NMEADataCache.DEVIATION_DATA, deviationCurve);
		} catch (Exception ex) {
			System.err.println("No deviation curve, sorry.");
			ex.printStackTrace();
			System.err.println("... moving on anyway.");
		}
		dataCache.put(NMEADataCache.MAX_LEEWAY, maxLeeway);

		dataCache.put(NMEADataCache.BSP_FACTOR, bspFactor);
		dataCache.put(NMEADataCache.AWS_FACTOR, awsFactor);
		dataCache.put(NMEADataCache.AWA_OFFSET, awaOffset);
		dataCache.put(NMEADataCache.HDG_OFFSET, hdgOffset);

		dataCache.put(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(defaultDeclination));
		dataCache.put(NMEADataCache.DAMPING, damping);
	}
}
