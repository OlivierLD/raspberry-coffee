package nmea.computers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
 * Will contain the names of the vessels, from message types 5 and 24.
 * WARNING!!! : This one is never cleaned!!
 */
public class AISTargetLogger extends Computer {

	private static class TargetNameTimeStamp {
		private long lastSeen;
		private String vesselName;
		private String callSign;
		private String destination;

		public TargetNameTimeStamp lastSeen(long lastSeen) {
			this.lastSeen = lastSeen;
			return this;
		}
		public TargetNameTimeStamp vesselName(String vesselName) {
			this.vesselName = vesselName;
			return this;
		}
		public TargetNameTimeStamp callSign(String callSign) {
			this.callSign = callSign;
			return this;
		}
		public TargetNameTimeStamp destination(String destination) {
			this.destination = destination;
			return this;
		}

		public long getLastSeen() {
			return lastSeen;
		}

		public void setLastSeen(long lastSeen) {
			this.lastSeen = lastSeen;
		}

		public String getVesselName() {
			return vesselName;
		}

		public void setVesselName(String vesselName) {
			this.vesselName = vesselName;
		}

		public String getCallSign() {
			return callSign;
		}

		public void setCallSign(String callSign) {
			this.callSign = callSign;
		}

		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}
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
						if (this.isVerbose()) {
							System.out.println(String.format("%s received AIS MessType #%d, %s (verb: %s)", this.getClass().getName(), aisRecord.getMessageType(), sentence.trim(), this.verbose));
						}
						if (aisRecord.getMessageType() == 5 ||
								aisRecord.getMessageType() == 24) {
							NMEADataCache cache = ApplicationContext.getInstance().getDataCache();

							if (this.isVerbose()) {
								System.out.println(String.format("-----------------------------\nCache is %snull!\n-----------------------------", cache == null ? "" : "not "));
							}
							int mmsi = aisRecord.getMMSI();
							String vesselName = aisRecord.getVesselName();
							if (!vesselName.isEmpty()) {
								long recordTimeStamp = aisRecord.getRecordTimeStamp();
								String callSign = aisRecord.getCallSign();
								String destination = aisRecord.getDestination();
								Object userMap = cache.get(NMEADataCache.USER_DEFINED);
								if (userMap == null) {
									targetMap = new HashMap<>();
								} else {
									if (userMap instanceof Map) {
										targetMap = (Map<Integer, TargetNameTimeStamp>) userMap;
									}
								}
								TargetNameTimeStamp tnts = new TargetNameTimeStamp()
										.lastSeen(recordTimeStamp)
										.vesselName(vesselName)
										.callSign(callSign)
										.destination(destination);
								targetMap.put(mmsi, tnts);
								synchronized (cache) {
									cache.put(NMEADataCache.USER_DEFINED, targetMap);
								}
								if (this.isVerbose()) {
									JsonElement jsonElement = new Gson().toJsonTree(cache);
									System.out.println("-------------------------------");
									System.out.println(String.format("Cache is now:\n%s", jsonElement.toString()));
									System.out.println("-------------------------------");
								}
							}
						}
					} else {
						if (this.isVerbose()) {
							System.out.println(String.format(">>\tIn %s, AIS Mess null for %s", this.getClass().getName(), sentence.trim()));
						}
					}
				} catch (AISParser.AISException aisException) { // un-managed AIS type
					// Absorb
				}
			}
		}
	}

	@Override
	public void setVerbose(boolean verbose) {
		super.setVerbose(verbose);
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing AIS Target data, " + this.getClass().getName());
	}

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);
		// Anything else?
		this.setVerbose("true".equals(props.getProperty("verbose")));
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

	// For JSON Tests
	public static void main(String... args) {
		Map<Integer, TargetNameTimeStamp> targetMap = new HashMap<>();
		TargetNameTimeStamp tnts = new TargetNameTimeStamp()
				.lastSeen(1234567)
				.vesselName("ZEBULON")
				.callSign("WDC7278")
				.destination("FAR-AWAY");
		targetMap.put(87654321, tnts);
		ApplicationContext.getInstance().initCache("null.csv", 10, 1, 1, 0, 0, 14, 1);
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		if (cache == null) {
			cache = new NMEADataCache();
		}
		cache.put(NMEADataCache.USER_DEFINED, targetMap);
		System.out.println("Cache:" + cache);
		JsonElement jsonElement = new Gson().toJsonTree(cache);
		System.out.println("JSON:" + jsonElement.toString());

		System.exit(0);
	}
}
