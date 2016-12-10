package context;

import nmea.parser.Angle;
import nmea.parser.Angle360;
import nmea.parser.NMEADoubleValueHolder;
import nmea.parser.Speed;
import nmea.parser.StringParsers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class NMEADataCache extends HashMap<String, Object> implements Serializable {
	public static final String LAST_NMEA_SENTENCE = "NMEA";

	public static final String SOG = "SOG";
	public static final String POSITION = "Boat Position";
	public static final String GPS_DATE_TIME = "GPS Date & Time";
	public static final String GPS_TIME = "GPS Time";
	public static final String GPS_SOLAR_TIME = "Solar Time";
	public static final String COG = "COG";
	public static final String DECLINATION = "D";
	public static final String BSP = "BSP";
	public static final String LOG = "Log";
	public static final String DAILY_LOG = "Daily";
	public static final String WATER_TEMP = "Water Temperature";
	public static final String AIR_TEMP = "Air Temperature";
	public static final String BARO_PRESS = "Barometric Pressure";
	public static final String RELATIVE_HUMIDITY = "Relative Humidity";
	public static final String AWA = "AWA";
	public static final String AWS = "AWS";
	public static final String HDG_COMPASS = "HDG c.";
	public static final String HDG_MAG = "HDG mag.";
	public static final String HDG_TRUE = "HDG true";
	public static final String DEVIATION = "d";
	public static final String VARIATION = "W";
	public static final String TWA = "TWA";
	public static final String TWS = "TWS";
	public static final String TWD = "TWD";
	public static final String CSP = "CSP";
	public static final String CDR = "CDR";
	public static final String XTE = "XTE";
	public static final String FROM_WP = "From Waypoint";
	public static final String TO_WP = "To Waypoint";
	public static final String WP_POS = "WayPoint pos";
	public static final String DBT = "Depth";
	public static final String D2WP = "Distance to WP";
	public static final String B2WP = "Bearing to WP";
	public static final String S2WP = "Speed to WP";
	public static final String S2STEER = "Steer";
	public static final String LEEWAY = "Leeway";
	public static final String CMG = "CMG";
	public static final String PERF = "Performance";
	public static final String SAT_IN_VIEW = "Satellites in view";

	public static final String BATTERY = "Battery Voltage";
	public static final String CALCULATED_CURRENT = "Current calculated with damping";
	public static final String VDR_CURRENT = "Set and Drift";

	public static final String BSP_FACTOR = "BSP Factor";
	public static final String AWS_FACTOR = "AWS Factor";
	public static final String AWA_OFFSET = "AWA Offset";
	public static final String HDG_OFFSET = "HDG Offset";
	public static final String MAX_LEEWAY = "Max Leeway";

	public static final String DEVIATION_FILE = "Deviation file name";
	public static final String DEVIATION_DATA = "Deviation data";
	public static final String DEFAULT_DECLINATION = "Default Declination";
	public static final String DAMPING = "Damping";

	public static final String POLAR_FILE_NAME = "Polar File name";
	public static final String POLAR_FACTOR = "Polar Factor";

	public static final String TIME_RUNNING = "Time Running";

	public static final String DISPLAY_WEB_WT = "Display Web Water Temp";
	public static final String DISPLAY_WEB_AT = "Display Web Air Temp";
	public static final String DISPLAY_WEB_GDT = "Display Web GPSDateTime";
	public static final String DISPLAY_WEB_PRMSL = "Display Web PRMSL";
	public static final String DISPLAY_WEB_HUM = "Display Web HUM";
	public static final String DISPLAY_WEB_VOLT = "Display Web Volt";

	// Damping ArrayList's
	private int dampingSize = 1;

	private transient HashMap<String, List<Object>> dampingMap = new HashMap<String, List<Object>>();

	private long started = 0L;

	private NMEADataCache instance = this;

	public NMEADataCache() {
		super();
		started = System.currentTimeMillis();
		if (System.getProperty("verbose", "false").equals("true")) {
			System.out.println("+=================================+");
			System.out.println("| Instantiating an NMEADataCache. |");
			System.out.println("+=================================+");
		}

		dampingMap.put(BSP, new ArrayList<Object>());
		dampingMap.put(HDG_TRUE, new ArrayList<Object>());
		dampingMap.put(AWA, new ArrayList<Object>());
		dampingMap.put(AWS, new ArrayList<Object>());
		dampingMap.put(TWA, new ArrayList<Object>());
		dampingMap.put(TWS, new ArrayList<Object>());
		dampingMap.put(TWD, new ArrayList<Object>());
		dampingMap.put(CSP, new ArrayList<Object>());
		dampingMap.put(CDR, new ArrayList<Object>());
		dampingMap.put(COG, new ArrayList<Object>());
		dampingMap.put(SOG, new ArrayList<Object>());
		dampingMap.put(LEEWAY, new ArrayList<Object>());

		// Initialization
		this.put(CALCULATED_CURRENT, new HashMap<Long, CurrentDefinition>());
	}

	@Override
	public /*synchronized*/ Object put(String key, Object value) {
		Object o = null;
		synchronized (this) {
			o = super.put(key, value);
		}
		if (dampingSize > 1 && dampingMap.containsKey(key)) {
			List<Object> ald = dampingMap.get(key);
			ald.add(value);
			while (ald.size() > dampingSize)
				ald.remove(0);
		}
		return o;
	}

	// For debug
	double prevTWD = 0d;

	/**
	 * @param key
	 * @return Damped Data, by default
	 */
	@Override
	public /*synchronized*/ Object get(Object key) {
		return get(key, true);
	}

	public /*synchronized*/ Object get(Object key, boolean useDamping) {
		Object ret = null;
		try {
			//  System.out.println("Damping = " + dampingSize);
			if (useDamping && dampingSize > 1 && dampingMap != null && dampingMap.containsKey(key)) {
				Class cl = null;
				List<?> ald = dampingMap.get(key);
				double sum = 0d;
				double sumCos = 0d,
								sumSin = 0d;

				for (Object v : ald) {
					if (cl == null)
						cl = v.getClass();
					if (v instanceof Double)
						sum += ((Double) v).doubleValue();
					else if (v instanceof NMEADoubleValueHolder) {
						// Debug
						if (false && key.equals(TWD))
							System.out.print(((NMEADoubleValueHolder) v).getDoubleValue() + ";");

						if (v instanceof Angle) // Angle360 || v instanceof Angle180 || v instanceof Angle180EW || v instanceof Angle180LR)
						{
							double val = ((NMEADoubleValueHolder) v).getDoubleValue();
							sumCos += (Math.cos(Math.toRadians(val)));
							sumSin += (Math.sin(Math.toRadians(val)));
						} else
							sum += ((NMEADoubleValueHolder) v).getDoubleValue();
					} else
						System.out.println("What'zat:" + v.getClass().getName());
				}
				try {
					if (ald.size() != 0) // Average here
					{
						sum /= ald.size();
						sumCos /= ald.size();
						sumSin /= ald.size();
					}
					if (cl != null) {
						if (cl.equals(Double.class)) {
							ret = new Double(sum);
						} else {
							ret = Class.forName(cl.getName()).newInstance();
							if (ret instanceof Angle) // Angle360 || ret instanceof Angle180 || ret instanceof Angle180EW || ret instanceof Angle180LR)
							{
								double a = Math.toDegrees(Math.acos(sumCos));
								if (sumSin < 0)
									a = 360d - a;
								sum = a;
							}
							((NMEADoubleValueHolder) ret).setDoubleValue(sum);
						}
					} else
						ret = super.get(key);
				} catch (Exception ex) {
					System.err.println("For key:" + key);
					ex.printStackTrace();
				}
			} else {
				ret = super.get(key);
//				if (ret == null) {
//					long age = System.currentTimeMillis() - started;
//					ret = new Long(age);
//				}
			}
		} catch (ConcurrentModificationException cme) {
			System.err.println("Conflict for key [" + key + "] -> " + cme.toString());
		}
		return ret;
	}

	public void setDampingSize(int dampingSize) {
		System.out.println("Setting Damping to " + dampingSize);
		this.dampingSize = dampingSize;
	}

	public int getDampingSize() {
		return dampingSize;
	}

	public void resetDampingBuffers() {
		Set<String> keys = dampingMap.keySet();
		for (String k : keys)
			dampingMap.get(k).clear();
	}

	public static class CurrentDefinition {
		private long bufferLength; // in ms
		private Speed speed;

		public long getBufferLength() {
			return bufferLength;
		}

		public Speed getSpeed() {
			return speed;
		}

		public Angle360 getDirection() {
			return direction;
		}

		private Angle360 direction;

		public CurrentDefinition(long bl, Speed sp, Angle360 dir) {
			this.bufferLength = bl;
			this.speed = sp;
			this.direction = dir;
		}
	}

	private static String generateCacheAge(String devicePrefix, long age) {
		String std = devicePrefix + "STD,";
		std += Long.toString(age);
		// Checksum
		int cs = StringParsers.calculateCheckSum(std);
		std += ("*" + lpad(Integer.toString(cs, 16).toUpperCase(), "0", 2));
		return "$" + std;
	}

	private static String lpad(String s, String with, int len) {
		String str = s;
		while (str.length() < len)
			str = with + str;
		return str;
	}
}
