package context;

import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;
import nmea.ais.AISParser;
import nmea.mux.context.Context;
import nmea.parser.Angle;
import nmea.parser.Angle180;
import nmea.parser.Angle180EW;
import nmea.parser.Angle360;
import nmea.parser.ApparentWind;
import nmea.parser.Current;
import nmea.parser.Depth;
import nmea.parser.Distance;
import nmea.parser.GLL;
import nmea.parser.GeoPos;
import nmea.parser.HDG;
import nmea.parser.NMEADoubleValueHolder;
import nmea.parser.OverGround;
import nmea.parser.Pressure;
import nmea.parser.RMB;
import nmea.parser.RMC;
import nmea.parser.SVData;
import nmea.parser.SolarDate;
import nmea.parser.Speed;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import nmea.parser.Temperature;
import nmea.parser.TrueWind;
import nmea.parser.UTC;
import nmea.parser.UTCDate;
import nmea.parser.UTCTime;
import nmea.parser.VHW;
import nmea.parser.VLW;
import nmea.parser.Wind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static nmea.utils.NMEAUtils.longitudeToTime;

/**
 * For AIS, see system property "put.ais.in.cache"
 */
public class NMEADataCache
		extends ConcurrentHashMap<String, Object>
		implements Serializable {

	// Keys. TODO, make this an enum?
	public static final String NB_MESS_PROCESSED = "NbMess"; // Retrieved when pulling the whole cache.
	public static final String LAST_NMEA_SENTENCE = "NMEA";

	public static final String SOG = "SOG";
	public static final String POSITION = "Position";
	public static final String GPS_DATE_TIME = "GPS Date & Time";
	public static final String GPS_TIME = "GPS Time";
	public static final String GPS_SOLAR_TIME = "Solar Time";
	public static final String RMC_STATUS = "RMCStatus";
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

	public static final String PITCH = "Pitch";
	public static final String ROLL = "Roll";

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

	public static final String TIME_RUNNING = "Time Running";

	public static final String VMG_ON_WIND = "VMG on Wind";
	public static final String VMG_ON_WP = "VMG to Waypoint";

	public static final String ALTITUDE = "Altitude";
	public static final String SMALL_DISTANCE = "Small Distance"; // For runners
	public static final String DELTA_ALTITUDE = "Delta Altitude";

	public static final String PRATE = "prate";
	public static final String DEW_POINT_TEMP = "dewpoint";

	public static final String NMEA_AS_IS = "NMEA_AS_IS";

	public static final String USER_DEFINED = "UserDefined"; // Free, whatever you like! At your own risk!

	public static final String AIS = "ais";

	// End of keys

	private Map<Integer, Map<Integer, AISParser.AISRecord>> aisMap = new ConcurrentHashMap<>();
	private final static long AIS_MAX_AGE =     600_000L; // Ten minutes. TODO A parameter
	private final static long AIS_CLEANUP_FREQ = 60_000L; // One minute

	private Thread aisCleaner = null;

	// Damping ArrayList's
	private transient int dampingSize = 1;

	private transient static List<String> NOT_TO_RESET = Arrays.asList(
					BSP_FACTOR,
					AWS_FACTOR,
					AWA_OFFSET,
					HDG_OFFSET,
					MAX_LEEWAY,
					DEFAULT_DECLINATION,
					DEVIATION_FILE,
					DEVIATION_DATA,
					DAMPING); /*,
					CALCULATED_CURRENT); */

	private transient HashMap<String, List<Object>> dampingMap = new HashMap<>();

	private transient long started = 0L;

	private transient double maxAlt = -Double.MAX_VALUE;
	private transient double minAlt =  Double.MAX_VALUE;
	private transient GeoPos previousPosition = null;

	private AISParser aisParser = new AISParser();

	public NMEADataCache() {
		super();
		started = System.currentTimeMillis();
		if (System.getProperty("nmea.cache.verbose", "false").equals("true")) {
			System.out.println("+=================================+");
			System.out.println("| Instantiating an NMEADataCache. |");
			System.out.println("+=================================+");
		}
		init();
	}

	public long getStartTime() {
		return this.started;
	}

	private void init() {
		dampingMap.put(BSP, new ArrayList<>());
		dampingMap.put(HDG_TRUE, new ArrayList<>());
		dampingMap.put(AWA, new ArrayList<>());
		dampingMap.put(AWS, new ArrayList<>());
		dampingMap.put(TWA, new ArrayList<>());
		dampingMap.put(TWS, new ArrayList<>());
		dampingMap.put(TWD, new ArrayList<>());
		dampingMap.put(CSP, new ArrayList<>());
		dampingMap.put(CDR, new ArrayList<>());
		dampingMap.put(COG, new ArrayList<>());
		dampingMap.put(SOG, new ArrayList<>());
		dampingMap.put(LEEWAY, new ArrayList<>());

		String strLat  = System.getProperty("default.mux.latitude");
		String strLong =  System.getProperty("default.mux.longitude");
		if (strLat != null && !strLat.isEmpty() && strLong != null && !strLong.isEmpty()) {
			try {
				double lat = Double.parseDouble(strLat);
				double lng = Double.parseDouble(strLong);
				GeoPos defaultPos = new GeoPos(lat, lng);
				synchronized (this) {
					this.put(POSITION, defaultPos);
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		// Initialization
		synchronized (this) {
			this.put(CALCULATED_CURRENT, new HashMap<Long, CurrentDefinition>());
			this.put(NMEA_AS_IS, new HashMap<String, Object>()); // Data is String (regular sentence) or List (like for GSV, List<String>))
		}

		if ("true".equals(System.getProperty("put.ais.in.cache", "true"))) {
			// Start AIS Cleaner thread
			aisCleaner = new Thread(() -> {
				while (true) {
					// Cleanup?
					try {
						Iterator<Integer> mmsiIterator = aisMap.keySet().iterator();
						synchronized (aisMap) {
							while (mmsiIterator.hasNext()) {
								Integer mmsi = mmsiIterator.next();
								Map<Integer, AISParser.AISRecord> typesMap = aisMap.get(mmsi);
								Iterator<Integer> typeIterator = typesMap.keySet().iterator();
								synchronized (typesMap) {
									while (typeIterator.hasNext()) {
										Integer type = typeIterator.next();
										if (type != 21 && type != 4) {
											AISParser.AISRecord aisRecord = typesMap.get(type);
											if (System.currentTimeMillis() - aisRecord.getRecordTimeStamp() > AIS_MAX_AGE) {
												System.out.println(String.format("=== Cleanup: Removing AIS Record type %d from %d ===", type, mmsi));
												typesMap.remove(type);
											}
										}
									}
									if (typesMap.size() == 0) {
										aisMap.remove(mmsi);
									}
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					try {
						Thread.sleep(AIS_CLEANUP_FREQ);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}, "AISCleaner");
			aisCleaner.start();
		}
	}

	public void reset() {
//	synchronized (this) {
			this.keySet()
							.stream()
							.filter(k -> !NOT_TO_RESET.contains(k))
							.forEach(k -> this.remove(k));
//			Map<Long, NMEADataCache.CurrentDefinition> currentMap = (Map<Long, NMEADataCache.CurrentDefinition>)this.get(NMEADataCache.CALCULATED_CURRENT);
//			if (currentMap != null) {
//				synchronized (currentMap) {
//					currentMap.keySet().stream().forEach(tbl -> {
//						synchronized (currentMap) {
//							currentMap.remove(tbl);
//						}
//					});
//				}
//			}
//		this.started = 0L;
			this.started = System.currentTimeMillis();
			this.maxAlt = -Double.MAX_VALUE;
			this.minAlt =  Double.MAX_VALUE;
			this.previousPosition = null;
		    System.out.println(">>> In Reset: invoking init() on the NMEADataCache.");
			this.init();
//	}
	}

	@Override
	public /*synchronized*/ Object put(String key, Object value) {
		Object o = null;
		synchronized (this) {
			try {
				o = super.put(key, value);
			} catch (Exception ex) {
				System.err.printf("Caught Exception for key [%s], value [%s]:", key, value);
				ex.printStackTrace();
			}
		}
		if (dampingSize > 1 && dampingMap.containsKey(key)) {
			List<Object> ald = dampingMap.get(key);
			ald.add(value);
			while (ald.size() > dampingSize) {
				ald.remove(0);
			}
		}
		return o;
	}

	private double feedSmallDistance(GeoPos lastPos) {
		Object smallDistObj = this.get(SMALL_DISTANCE);
		double smallDist = 0;
		if (smallDistObj != null) {
			if (this.previousPosition != null && lastPos != null) {
				double previousDist = (Double) smallDistObj;
				double distanceFromPreviousPos = GeomUtil.haversineNm(this.previousPosition.lat, this.previousPosition.lng, lastPos.lat, lastPos.lng);
				smallDist = previousDist + distanceFromPreviousPos;
			}
		}
		return smallDist;
	}

	private Date getSolarDateFromEOT(Date utc, double latitude, double longitude) {
		Calendar current = GregorianCalendar.getInstance();
		current.setTime(utc);
		AstroComputerV2 acv2 = new AstroComputerV2();
		acv2.setDateTime(current.get(Calendar.YEAR),
				current.get(Calendar.MONTH) + 1,
				current.get(Calendar.DAY_OF_MONTH),
				current.get(Calendar.HOUR_OF_DAY),
				current.get(Calendar.MINUTE),
				current.get(Calendar.SECOND));
		acv2.calculate();
		SightReductionUtil sru = new SightReductionUtil(acv2.getSunGHA(),
				acv2.getSunDecl(),
				latitude,
				longitude);
		sru.calculate();
		double he = sru.getHe().doubleValue();
		double z = sru.getZ().doubleValue();  // TODO Push those 2 in the cache?
		// Get Equation of time, used to calculate solar time.
		double eot = acv2.getSunMeridianPassageTime(latitude, longitude); // in decimal hours

		long ms = utc.getTime();
		Date solar = new Date(ms + Math.round((12 - eot) * 3_600_000));
		return solar;
	}

	/**
	 * Close to AutoParse. Could be more widely used!
	 * @param nmeaSentence
	 */
	public void parseAndFeed(String nmeaSentence) {
		if (StringParsers.validCheckSum(nmeaSentence)) {
			// Increment # of messages processed
			Long nbMess = (Long)this.get(NB_MESS_PROCESSED);
			if (nbMess == null) {
				nbMess = 1L;
			} else {
				nbMess++;
			}
			this.put(NB_MESS_PROCESSED, nbMess);

			// Feed pure NMEA cache (NMEA sentences, as they are)
			String sentenceId = StringParsers.getSentenceID(nmeaSentence);
			Map<String, Object> asIsMap = (Map<String, Object>)this.get(NMEA_AS_IS);
			switch(sentenceId) {
				case "GSV":
					asIsMap.put(sentenceId, StringParsers.getGSVList());
					break;
				case "VDM": // AIS
					sentenceId = "AIS";
				default:
					if (asIsMap != null) {
						asIsMap.put(sentenceId, nmeaSentence);
					} else {
						System.out.println(">>> Oops! asIsMap is null!");
					}
					break;
			}
			synchronized (this) {
//		  this.put(NMEA_AS_IS, asIsMap);
				this.put(LAST_NMEA_SENTENCE, nmeaSentence);
			}
			if (nmeaSentence.startsWith(AISParser.AIS_PREFIX)) { // AIS. There is a thread to cleanup old AIS records
				if ("true".equals(System.getProperty("put.ais.in.cache", "true"))) {
					try {
						AISParser.AISRecord rec = aisParser.parseAIS(nmeaSentence);
						if (rec != null) { // Case of Multi-Record or un-managed type
							Map<Integer, AISParser.AISRecord> mapOfTypes = aisMap.get(rec.getMMSI());
							if (mapOfTypes == null) {
								mapOfTypes = new ConcurrentHashMap<>();
							}
							synchronized (mapOfTypes) {
								mapOfTypes.put(rec.getMessageType(), rec);
							}
							synchronized (aisMap) {
								aisMap.put(rec.getMMSI(), mapOfTypes);  // Id is the MMSI/type.
							}
							//	System.out.println("(" + aisMap.size() + " boat(s)) " + rec.toString());
							if (System.getProperty("ais.cache.verbose", "false").equals("true")) {
								System.out.println(String.format(">> AIS %s, type %s goes into cache: %s", rec.getMMSI(), rec.getMessageType(), rec.toString()));
							}
							if (System.getProperty("ais.cache.verbose", "false").equals("true")) { // Debug
								if (rec.getMessageType() == 5) {
									System.out.println(String.format(">>> \tAIS Mess Type 5: %s, %s", nmeaSentence.trim(), rec.toString()));
								}
							}
						}
						synchronized (this) {
							synchronized (aisMap) {
								this.put(AIS, aisMap);
							}
						}
					} catch (AISParser.AISException aisExc) {
						if (System.getProperty("ais.cache.verbose", "false").equals("true")) {
							System.err.println(String.format(">> AIS %s, %s", nmeaSentence, aisExc.toString()));
						}
					} catch (Exception ex) {
						System.err.println(String.format("\nFor AIS Sentence [%s]", nmeaSentence));
						ex.printStackTrace();
					}
				}
			} else { // NMEA
				String id = StringParsers.getSentenceID(nmeaSentence);
				switch (id) {
					case "GGA":
						List<Object> gga = StringParsers.parseGGA(nmeaSentence);
						GeoPos ggaPos = (GeoPos) gga.get(StringParsers.GGA_POS_IDX);
						if (ggaPos != null) {
							this.put(POSITION, ggaPos);
							if (this.previousPosition != null) {
								double smallDist = feedSmallDistance(ggaPos);
								this.put(SMALL_DISTANCE, smallDist);
							}
							this.previousPosition = ggaPos;
						}
						if (!"true".equals(System.getProperty("do.not.use.GGA.date.time"))) { // Not good when replaying, contains only H:M:S, no Y:N:D
							UTC ggaDate = (UTC) gga.get(StringParsers.GGA_UTC_IDX);
							if (ggaDate != null) {
								this.put(GPS_DATE_TIME, new UTCDate(ggaDate.getDate()));
							}
						}
						//	int ggaNbSat = (Integer)gga.get(StringParsers.GGA_NBSAT_IDX);
						double ggaAlt = (Double) gga.get(StringParsers.GGA_ALT_IDX);
						this.put(ALTITUDE, ggaAlt);
						this.minAlt = Math.min(this.minAlt, ggaAlt);
						this.maxAlt = Math.max(this.maxAlt, ggaAlt);
						this.put(DELTA_ALTITUDE, (this.maxAlt - this.minAlt));
//						System.out.println(String.format("Alt: Min %.02f, Max %.02f, Diff %.02f", this.minAlt, this.maxAlt, (this.maxAlt - this.minAlt)));
						break;
					case "RMC":
						RMC rmc = null;
						try {
							rmc = StringParsers.parseRMC(nmeaSentence);
						} catch (Exception ex) { // Some NULL happen to sneak in some strings... TBD.
							System.err.println("Managed >>");
							ex.printStackTrace();
						}
						if (rmc != null) {
							if (rmc.isValid()) {
								this.put(RMC_STATUS, true);
								this.put(POSITION, rmc.getGp());
								if (this.previousPosition != null) {
									double smallDist = feedSmallDistance(rmc.getGp());
									this.put(SMALL_DISTANCE, smallDist);
								}
								this.previousPosition = rmc.getGp();
								this.put(COG, new Angle360(rmc.getCog()));
								this.put(SOG, new Speed(rmc.getSog()));
								if (rmc.getDeclination() != -Double.MAX_VALUE) {
//									System.out.printf("RMC: Decl in cache => %f %n", rmc.getDeclination());
									this.put(DECLINATION, new Angle180EW(rmc.getDeclination()));
								}
								if (rmc.getRmcDate() != null) {
									this.put(GPS_DATE_TIME, new UTCDate(rmc.getRmcDate()));
								}
								if (rmc.getRmcTime() != null) {
									this.put(GPS_TIME, new UTCTime(rmc.getRmcTime()));
								}
								if ("true".equals(System.getProperty("rmc.verbose"))) {
									System.out.println(String.format("RMC: From [%s], GPS date: %s, GPS Time: %s", nmeaSentence.trim(), StringParsers.SDF_UTC.format(rmc.getRmcDate()), StringParsers.SDF_UTC.format(rmc.getRmcTime())));
								}
								if ((rmc.getRmcDate() != null || rmc.getRmcTime() != null) && rmc.getGp() != null) {
									long solarTime = -1L;
									if ("true".equals(System.getProperty("calculate.solar.with.eot")) && rmc.getGp() != null) {
										Date solarDateFromEOT = getSolarDateFromEOT(rmc.getRmcDate() != null ? rmc.getRmcDate() : rmc.getRmcTime(), rmc.getGp().lat, rmc.getGp().lng);
										this.put(GPS_SOLAR_TIME, new SolarDate(solarDateFromEOT));
										// Debug
										if (false) { // For comparison
											System.out.println(String.format("Solar Date with EoT: %s", SolarDate.FMT.format(solarDateFromEOT)));
											if (rmc.getRmcDate() != null) {
												solarTime = rmc.getRmcDate().getTime() + longitudeToTime(rmc.getGp().lng);
											} else {
												solarTime = rmc.getRmcTime().getTime() + longitudeToTime(rmc.getGp().lng);
											}
											Date solarDate = new Date(solarTime);
											System.out.println(String.format("Solar Date from G  : %s", SolarDate.FMT.format(solarDate)));
											System.out.println("-----------------------------------------");
										}
									} else {
										if (rmc.getRmcDate() != null) {
											solarTime = rmc.getRmcDate().getTime() + longitudeToTime(rmc.getGp().lng);
										} else {
											solarTime = rmc.getRmcTime().getTime() + longitudeToTime(rmc.getGp().lng);
										}
										Date solarDate = new Date(solarTime);
										this.put(GPS_SOLAR_TIME, new SolarDate(solarDate));
									}
								}
							} else {
								this.put(RMC_STATUS, false);
								// Date and time may be available...
								if (rmc.getRmcDate() != null && rmc.getRmcTime() != null) {
									this.put(GPS_DATE_TIME, new UTCDate(rmc.getRmcDate()));
									this.put(GPS_TIME, new UTCTime(rmc.getRmcTime()));
								} else {
									if (System.getProperty("nmea.cache.verbose", "false").equals("true")) {
										System.out.println(String.format("RMC not valid yet [%s]", nmeaSentence));
									}
								}
							}
						}
						break;
					case "ZDA":
						UTCDate utc = StringParsers.parseZDA(nmeaSentence);
						if (utc != null) {
							this.put(GPS_DATE_TIME, utc);
							UTCTime utcTime = new UTCTime(utc.getValue());
							this.put(GPS_TIME, utcTime);

							if ("true".equals(System.getProperty("zda.verbose"))) {
								System.out.println(String.format("ZDA: From [%s], GPS date: %s, GPS Time: %s", nmeaSentence, StringParsers.SDF_UTC.format(utc.getValue()), StringParsers.SDF_UTC.format(utcTime.getValue())));
							}
							GeoPos pos = (GeoPos) this.get(POSITION);
							if (pos != null) {
								if ("true".equals(System.getProperty("calculate.solar.with.eot"))) {
									Date solarDateFromEOT = getSolarDateFromEOT(utc.getValue(), pos.lat, pos.lng);
									this.put(GPS_SOLAR_TIME, new SolarDate(solarDateFromEOT));
								} else {
									long solarTime = utc.getValue().getTime() + longitudeToTime(pos.lng);
									Date solarDate = new Date(solarTime);
									this.put(GPS_SOLAR_TIME, new SolarDate(solarDate));
								}
							}
						}
						break;
					case "VHW": // Water Speed and Heading
						VHW vhw = StringParsers.parseVHW(nmeaSentence);
						if (vhw == null) {
							return;
						}
						double bsp = vhw.getBsp();
						double hdm = vhw.getHdm();
						if (bsp != -Double.MAX_VALUE) {
							this.put(BSP, new Speed(bsp));
						}
						this.put(HDG_COMPASS, new Angle360(hdm /* - dec */));
						break;
					case "VLW": // Log
						VLW vlw = StringParsers.parseVLW(nmeaSentence);
						HashMap<String, Object> map = new HashMap<String, Object>(2);
						this.put(LOG, new Distance(vlw.getLog()));
						this.put(DAILY_LOG, new Distance(vlw.getDaily()));
						break;
					case "MTW": // Water Temperature
						double wt = StringParsers.parseMTW(nmeaSentence);
						this.put(WATER_TEMP, new Temperature(wt));
						break;
					case "MTA": // Air Temperature
						double at = StringParsers.parseMTA(nmeaSentence);
						this.put(AIR_TEMP, new Temperature(at));
						break;
					case "MMB": // Barometric Pressure
						double p = StringParsers.parseMMB(nmeaSentence); // in mb
						this.put(BARO_PRESS, new Pressure(p));
						break;
					case "MWV": // Apparent Wind Speed and Direction
						Wind wind = StringParsers.parseMWV(nmeaSentence);
						if (wind != null && wind instanceof ApparentWind) { // TODO: TrueWind not used for now
							this.put(AWS, new Speed(wind.getSpeed()));
							int awa = wind.getAngle();
							if (awa > 180) {
								awa -= 360;
							}
							this.put(AWA, new Angle180(awa));
						}
						break;
					case "VDR":
						Current current = StringParsers.parseVDR(nmeaSentence);
						this.put(NMEADataCache.VDR_CURRENT, current);
						break;
					case "VWR": // Apparent Wind Speed and Direction (2)
						ApparentWind aWind = StringParsers.parseVWR(nmeaSentence);
						if (aWind != null) {
							this.put(AWS, new Speed(aWind.getSpeed()));
							int awa = aWind.getAngle();
							if (awa > 180) {
								awa -= 360;
							}
							this.put(AWA, new Angle180(awa));
						}
						break;
					case "VTG": // Speed and Course over Ground
						OverGround og = StringParsers.parseVTG(nmeaSentence);
						if (og != null) {
							this.put(COG, new Angle360(og.getCourse()));
							this.put(SOG, new Speed(og.getSpeed()));
						}
						break;
					case "GLL": // Lat & Long, UTC (No date, just time)
						GLL gll = StringParsers.parseGLL(nmeaSentence);
						if (gll != null) {
							GeoPos pos = gll.getGllPos();
							if (pos != null) {
								this.put(POSITION, pos);
								if (this.previousPosition != null) {
									double smallDist = feedSmallDistance(pos);
									this.put(SMALL_DISTANCE, smallDist);
								}
								this.previousPosition = pos;
							}
							if (!"true".equals(System.getProperty("do.not.use.GLL.date.time"))) { // Not good when replaying, contains only H:M:S, no Y:N:D
								Date date = gll.getGllTime();
								if (date != null) {
									this.put(GPS_TIME, new UTCTime(date));
									if ("true".equals(System.getProperty("calculate.solar.with.eot")) && pos != null) {
										Date solarDateFromEOT = getSolarDateFromEOT(date, pos.lat, pos.lng);
										this.put(GPS_SOLAR_TIME, new SolarDate(solarDateFromEOT));
									} else {
										long solarTime = date.getTime() + longitudeToTime(pos.lng);
										Date solarDate = new Date(solarTime);
										this.put(GPS_SOLAR_TIME, new SolarDate(solarDate));
									}
								}
							} else {
								if ("true".equals(System.getProperty("verbose", "false"))) {
									System.out.println("\t>> NOT Injecting GLL Time in the cache");
								}
							}
						}
						break;
					case "HDM": // Heading, magnetic
						int hdg = StringParsers.parseHDM(nmeaSentence);
//						this.put(HDG_COMPASS, new Angle360(hdg));
						this.put(HDG_MAG, new Angle360(hdg));
						break;
					case "HDT": // Heading, true
						this.put(NMEADataCache.HDG_TRUE, new Angle360(StringParsers.parseHDT(nmeaSentence)));
						break;
					case "HDG": // Heading
						HDG hdgData = StringParsers.parseHDG(nmeaSentence);
						int heading = (int) hdgData.getHeading();
						double dev = hdgData.getDeviation();
						double var = hdgData.getVariation();
						if (dev == -Double.MAX_VALUE && var == -Double.MAX_VALUE) {
							this.put(HDG_COMPASS, new Angle360(heading));
						} else {
							double dec = 0d;
							if (dev != -Double.MAX_VALUE) {
								dec = dev;
							} else {
								dec = var;
							}
							if (!"true".equals(System.getProperty("rmc.decl.only"))) {
//								System.out.printf("HDG: Decl in cache => %f %n", dec);
								this.put(DECLINATION, new Angle180EW(dec));
//							} else {
//								System.out.printf("HDG: NOT in the cache: %f %n", dec);
							}
							this.put(HDG_COMPASS, new Angle360(heading /* - dec */));
						}
						break;
					case "RMB":
						RMB rmb = StringParsers.parseRMB(nmeaSentence);
						if (rmb != null) {
							this.put(XTE, new Distance(rmb.getXte()));
							this.put(WP_POS, rmb.getDest());
							this.put(FROM_WP, rmb.getOwpid());
							this.put(TO_WP, rmb.getDwpid());
							this.put(D2WP, new Distance(rmb.getRtd()));
							this.put(B2WP, new Angle360(rmb.getBtd()));
							this.put(S2WP, new Speed(rmb.getDcv()));
							this.put(S2STEER, rmb.getDts());
						}
						break;
					case "DBT": // Depth
						float fb = StringParsers.parseDBT(nmeaSentence, StringParsers.DEPTH_IN_METERS);
						this.put(DBT, new Depth(fb));
						break;
					case "DPT": // Depth
						float fp = StringParsers.parseDPT(nmeaSentence, StringParsers.DEPTH_IN_METERS);
						this.put(DBT, new Depth(fp));
						break;
					case "GSV": // Satellites in view
						Map<Integer, SVData> satmap = StringParsers.parseGSV(nmeaSentence);
						if (satmap != null) {
							this.put(SAT_IN_VIEW, satmap);
						}
						break;
					case "MDA": // Meteorological composite (Humidity, among others)
						StringParsers.MDA mda = StringParsers.parseMDA(nmeaSentence);
						if (mda.airT != null) {
							this.put(NMEADataCache.AIR_TEMP, new Temperature(mda.airT));
						}
						if (mda.waterT != null) {
							this.put(NMEADataCache.WATER_TEMP, new Temperature(mda.waterT));
						}
						if (mda.pressBar != null) {
							this.put(NMEADataCache.BARO_PRESS, new Pressure(mda.pressBar * 1_000));
						}
						if (mda.relHum != null) {
							this.put(NMEADataCache.RELATIVE_HUMIDITY, mda.relHum);
						}
						if (mda.dewC != null) {
							this.put(NMEADataCache.DEW_POINT_TEMP, mda.dewC);
						}
						// TODO: More MDA data...
						break;
					case "XTE": // Cross Track Error
						// TODO: Implement
						break;
					case "XDR": // Transducer measurement
						List<StringGenerator.XDRElement> xdr = StringParsers.parseXDR(nmeaSentence);
						if (xdr != null) {
							for (StringGenerator.XDRElement xe : xdr) {
								StringGenerator.XDRTypes type = xe.getTypeNunit();
								double val = xe.getValue();
								if (type.equals(StringGenerator.XDRTypes.HUMIDITY)) {
									this.put(RELATIVE_HUMIDITY, val);
								} else if (type.equals(StringGenerator.XDRTypes.PRESSURE_B)) {
									this.put(BARO_PRESS, new Pressure(val * 1_000));
								} else if (type.equals(StringGenerator.XDRTypes.VOLTAGE)) {
									this.put(BATTERY, (float)(val));
								} else if (type.equals(StringGenerator.XDRTypes.GENERIC)) { // Consider it as prate.
									this.put(PRATE, (float)(val));
								} else if (type.equals(StringGenerator.XDRTypes.ANGULAR_DISPLACEMENT) &&
										xe.getTransducerName().equals(StringGenerator.XDR_PTCH)) {
									this.put(PITCH, val);
								} else if (type.equals(StringGenerator.XDRTypes.ANGULAR_DISPLACEMENT) &&
										xe.getTransducerName().equals(StringGenerator.XDR_ROLL)) {
									this.put(ROLL, val);
								} else {
									if ("true".equals(System.getProperty("verbose", "false"))) {
										System.out.println("Un-managed XDR Type:" + type.toString());
									}
								}
							}
						}
						break;
					case "MWD": // Wind Speed and Direction
						TrueWind mwdWind = StringParsers.parseMWD(nmeaSentence);
						if (mwdWind != null) {
							this.put(TWS, new Speed(mwdWind.getSpeed()));
							this.put(TWD, new Angle360(mwdWind.getAngle()));
						}
						break;
					case "VWT": // True Wind Speed and Angle (deprecated, use MWV)
						TrueWind trueWind = StringParsers.parseVWT(nmeaSentence);
						if (trueWind != null) {
							this.put(TWS, new Speed(trueWind.getSpeed()));
							this.put(TWA, new Angle180(trueWind.getAngle()));
							Angle360 trueHeading = (Angle360) this.get(HDG_TRUE);
							if (trueHeading != null) {
								double twd = trueHeading.getValue() + trueWind.getAngle();
								System.out.println("TWD: " + twd); // TODO: Implement put(TWD, new Angle360(twd))
							}
						}
						break;
					case "BAT":     // Battery Voltage. Not Standard, from the Raspberry Pi. There is an XDR Voltage...
						float volt = StringParsers.parseBAT(nmeaSentence);
						if (volt > -1) {
							this.put(BATTERY, (volt));
						}
						break;
					case "STD":     // Cache age. Not Standard. From Original cache
						long age = StringParsers.parseSTD(nmeaSentence);
						if (age > -1) {
							this.put(TIME_RUNNING, (age));
						}
						break;
					default:
						if (System.getProperty("verbose", "false").equals("true")) {
							System.out.println(String.format("NMEA Sentence [%s] not managed by parseAndFeed.", id));
						}
						break;
				}
			}
		}
	}

	/**
	 * @param key identifies the data to get
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
					if (cl == null) {
						cl = v.getClass();
					}
					if (v instanceof Double) {
						sum += ((Double) v).doubleValue();
					} else if (v instanceof NMEADoubleValueHolder) {
						// Debug
						if (false && key.equals(TWD)) {
							System.out.print(((NMEADoubleValueHolder) v).getDoubleValue() + ";");
						}
						if (v instanceof Angle) { // Angle360 || v instanceof Angle180 || v instanceof Angle180EW || v instanceof Angle180LR)
							double val = ((NMEADoubleValueHolder) v).getDoubleValue();
							sumCos += (Math.cos(Math.toRadians(val)));
							sumSin += (Math.sin(Math.toRadians(val)));
						} else {
							sum += ((NMEADoubleValueHolder) v).getDoubleValue();
						}
					} else {
						System.out.println("What'zat:" + v.getClass().getName());
					}
				}
				try {
					if (ald.size() != 0) { // Average here
						sum /= ald.size();
						sumCos /= ald.size();
						sumSin /= ald.size();
					}
					if (cl != null) {
						if (cl.equals(Double.class)) {
							ret = (sum);
						} else {
							ret = Class.forName(cl.getName()).getDeclaredConstructor().newInstance();
							if (ret instanceof Angle) { // Angle360 || ret instanceof Angle180 || ret instanceof Angle180EW || ret instanceof Angle180LR)
								double a = Math.toDegrees(Math.acos(sumCos));
								if (sumSin < 0) {
									a = 360d - a;
								}
								sum = a;
							}
							((NMEADoubleValueHolder) ret).setDoubleValue(sum);
						}
					} else {
						ret = super.get(key);
					}
				} catch (Exception ex) {
					Context.getInstance().getLogger().log(Level.INFO, String.format("For key", key), ex);
				}
			} else {
				ret = super.get(key);
//				if (ret == null) {
//					long age = System.currentTimeMillis() - started;
//					ret = new Long(age);
//				}
			}
		} catch (ConcurrentModificationException cme) {
			Context.getInstance().getLogger().log(Level.INFO, String.format("Conflict for key [%s]", key), cme);
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
		for (String k : keys) {
			dampingMap.get(k).clear();
		}
	}

	public static class CurrentDefinition implements Serializable {
		private long bufferLength; // in ms
		private Speed speed;
		private Angle360 direction;
		private int nbPoints = 0;
		private String oldest = "";
		private String latest = "";
		private long len = 0L; // Len in ms

		public long getBufferLength() {
			return bufferLength;
		}

		public Speed getSpeed() {
			return speed;
		}

		public Angle360 getDirection() {
			return direction;
		}

		public CurrentDefinition(long bl, Speed sp, Angle360 dir, int nbp, String old, String last, long len) {
			this.bufferLength = bl;
			this.speed = sp;
			this.direction = dir;
			this.nbPoints = nbp;
			this.oldest = old;
			this.latest = last;
			this.len = len;
		}
	}
}
