package context;

import nmea.parser.Angle180EW;
import nmea.utils.NMEAUtils;

import java.util.List;

public class ApplicationContext {

	private static ApplicationContext applicationContext = null;
	NMEADataCache dataCache = null;

	private ApplicationContext() {
	}

	public NMEADataCache getDataCache() {
		return this.dataCache;
	}

	public static synchronized ApplicationContext getInstance() {
		if (applicationContext == null)
			applicationContext = new ApplicationContext();
		return applicationContext;
	}

	public void initCache(String deviationFileName, double maxLeeway, double bspFactor, double awsFactor, double awaOffset, double hdgOffset, double defaultDeclination, int damping) {

		dataCache = new NMEADataCache();

		List<double[]> deviationCurve = NMEAUtils.loadDeviationCurve(deviationFileName);
		dataCache.put(NMEADataCache.DEVIATION_FILE, deviationFileName);
		dataCache.put(NMEADataCache.DEVIATION_DATA, deviationCurve);
		dataCache.put(NMEADataCache.MAX_LEEWAY, maxLeeway);

		dataCache.put(NMEADataCache.BSP_FACTOR, bspFactor);
		dataCache.put(NMEADataCache.AWS_FACTOR, awsFactor);
		dataCache.put(NMEADataCache.AWA_OFFSET, awaOffset);
		dataCache.put(NMEADataCache.HDG_OFFSET, hdgOffset);

		dataCache.put(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(defaultDeclination));
		dataCache.put(NMEADataCache.DAMPING, damping);
	}
}
