package nmea.computers;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.parser.StringParsers;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Puts a map in the USER_DEFINED property.
 * WARNING!!! : This one is never cleaned!!
 */
public class AISTargetLogger extends Computer {

	private static class TargetNameTimeStamp {
		long lastSeen;
		String vesselName;
	}

	private Map<Integer, TargetNameTimeStamp> targetMap = null;

	public AISTargetLogger(Multiplexer mux) {
		super(mux);
	}

	/**
	 * Log vessel name is available along with timestamp.
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
//	System.out.println(String.format("In AIS Target Computer, write method: %s", sentence));

		if (StringParsers.validCheckSum(sentence)) {
			if (sentence.startsWith(AISParser.AIS_PREFIX)) {
				try {
					AISParser.AISRecord aisRecord = AISParser.parseAIS(sentence);
					if (aisRecord != null) {
						if (aisRecord.getMessageType() == 5 ||
								aisRecord.getMessageType() == 24) {
							NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
							int mmsi = aisRecord.getMMSI();
							String vesselName = aisRecord.getVesselName();
							if (!vesselName.isEmpty()) {
								long recordTimeStamp = aisRecord.getRecordTimeStamp();
								Object userMap = cache.get(NMEADataCache.USER_DEFINED);
								if (userMap == null) {
									targetMap = new HashMap<>();
								} else {
									if (userMap instanceof Map) {
										targetMap = (Map<Integer, TargetNameTimeStamp>) userMap;
									}
								}
								TargetNameTimeStamp tnts = new TargetNameTimeStamp();
								tnts.lastSeen = recordTimeStamp;
								tnts.vesselName = vesselName;
								targetMap.put(mmsi, tnts);
								cache.put(NMEADataCache.USER_DEFINED, targetMap);
							}
						}
					}
				} catch (AISParser.AISException aisException) { // un-managed AIS type
					// Absorb
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing AIS Target data, " + this.getClass().getName());
	}

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);
		// Anything else?
	}

	public static class AISTargetLoggerBean {
		private String cls;
		private String type = "ais-target-computer";
		private boolean verbose;

		public AISTargetLoggerBean(AISTargetLogger instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new AISTargetLoggerBean(this);
	}
}
