package nmea.computers;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.api.NMEAParser;
import nmea.computers.current.LongTimeCurrentCalculator;
import nmea.parser.*;
import nmea.utils.NMEAUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extra Data Computer. True Wind and Current.
 *
 * The NMEADataCache must have been initialized.
 * See {@link ApplicationContext#initCache(String, double, double, double, double, double, double, int)}.
 * <br>
 * True Wind calculation requires:
 * <table border='1' summary="Required data">
 * <tr><td>GPS: COG &amp; SOG, Declination</td><td>RMC, VTG, HDM</td></tr>
 * <tr><td>AWS, AWA</td><td>MWV, VWR</td></tr>
 * <tr><td>TRUE Heading</td><td>VHW, HDT, HDM</td></tr>
 * <tr><td>Leeway</td><td><i>Estimated</i> (initialization parameter)</td></tr>
 * </table>
 * Also takes care of possible corrections:
 * <ul>
 * <li>BSP Coeff</li>
 * <li>AWS coeff, AWA offset</li>
 * <li>Heading Offset</li>
 * </ul>
 * <br>
 * See {@link ApplicationContext} and {@link NMEADataCache}
 */
public class ExtraDataComputer extends Computer {

	private final static String DEFAULT_PREFIX = "OS"; // OlivSoft

	private String generatedStringsPrefix = DEFAULT_PREFIX;
	private List<LongTimeCurrentCalculator> longTimeCurrentCalculator = new ArrayList<>();

	private final List<String> requiredStrings = Arrays.asList(new String[]{
			"RMC",   // Recommended Minimum
			"VHW",   // Water speed and heading
			"VTG",   // Track made good and Ground speed
			"HDG",   // Heading - Deviation & Variation
			"HDM",   // Heading - Magnetic
			"HDT",   // Heading - True
			"MWV",   // Wind Speed and Angle
			"VWR"}); // Relative Wind Speed and Angle

	public ExtraDataComputer(Multiplexer mux) {
	  this(mux, DEFAULT_PREFIX, LongTimeCurrentCalculator.DEFAULT_BUFFER_LENGTH);
	}

	public void setPrefix(String prefix) {
		this.generatedStringsPrefix = prefix;
	}

	public ExtraDataComputer(Multiplexer mux, String prefix, Long... tbl) {
		super(mux);
		if (prefix == null || prefix.length() != 2) {
			throw new RuntimeException("Prefix must exist, and be EXACTLY 2 character long.");
		}
		this.generatedStringsPrefix = prefix;
		for (long bl : tbl) { // in seconds here.
			LongTimeCurrentCalculator ltcc = new LongTimeCurrentCalculator(bl * 1_000); // In ms here.
			ltcc.setVerbose(this.verbose);
			ltcc.start();
			this.longTimeCurrentCalculator.add(ltcc);
		}
	}

	/**
	 * Receives the data, and potentially produces new ones.
	 *
	 * @param mess The message to write
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
		if (StringParsers.validCheckSum(sentence)) {
			String sentenceID = StringParsers.getSentenceID(sentence);
			if (!generatedStringsPrefix.equals(StringParsers.getDeviceID(sentence)) && // To prevent re-computing of computed data.
							requiredStrings.contains(sentenceID)) { // Then process
				if (this.verbose) {
					System.out.println(">>> TrueWind computer using " + sentence);
				}
				NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
				switch (sentenceID) {
					case "RMC": // Recommended Minimum, version C
						RMC rmc = StringParsers.parseRMC(sentence);
						if (rmc != null && rmc.isValid()) {
							Map<String, Object> rmcMap = new HashMap<>(5);
							rmcMap.put(NMEADataCache.SOG, new Speed(rmc.getSog()));
							rmcMap.put(NMEADataCache.POSITION, rmc.getGp());
							Date date = rmc.getRmcDate();
							if (date != null) {
								rmcMap.put(NMEADataCache.GPS_DATE_TIME, new UTCDate(date));
							} else {
								rmcMap.put(NMEADataCache.GPS_DATE_TIME, null);
							}
							Date time = rmc.getRmcTime();
							// When re-playing, set -Drmc.time.ok=false
							if (time != null && "true".equals(System.getProperty("rmc.time.ok", "true"))) {
								rmcMap.put(NMEADataCache.GPS_TIME, new UTCTime(time));
							}
							rmcMap.put(NMEADataCache.COG, new Angle360(rmc.getCog()));
							rmcMap.put(NMEADataCache.DECLINATION, new Angle180EW(rmc.getDeclination()));

							// Compute Solar Time here
							try {
								if (rmc != null && (rmc.getRmcDate() != null || rmc.getRmcTime() != null) && rmc.getGp() != null) {
									long solarTime = -1L;
									if (rmc.getRmcDate() != null) {
										solarTime = rmc.getRmcDate().getTime() + NMEAUtils.longitudeToTime(rmc.getGp().lng);
									} else {
										solarTime = rmc.getRmcTime().getTime() + NMEAUtils.longitudeToTime(rmc.getGp().lng);
									}
									Date solarDate = new Date(solarTime);
									rmcMap.put(NMEADataCache.GPS_SOLAR_TIME, new SolarDate(solarDate));
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							try {
								// Warning: No null in a ConcurrentHashMap::putAll!!
								Set<String> keys = rmcMap.keySet();
								List<String> toRemove = new ArrayList<>();
								keys.forEach(k -> {
									if (rmcMap.get(k) == null) { // One level only for this map.
										toRemove.add(k);
									}
								});
								if (toRemove.size() > 0) {
									synchronized (rmcMap) {
										toRemove.forEach(k -> rmcMap.remove(k));
									}
								}
								cache.putAll(rmcMap);
							} catch (Exception ex) {
								System.err.printf("--- Managed: putAll for %s ---%n", rmcMap);
								ex.printStackTrace();
								System.err.println("----------------------------");
							}
						}
						break;
					case "VTG": // Track Made Good and Ground Speed
						OverGround overGround = StringParsers.parseVTG(sentence);
						if (overGround != null) {
							Map<String, Object> map = new HashMap<>(2);
							map.put(NMEADataCache.COG, new Angle360(overGround.getCourse()));
							map.put(NMEADataCache.SOG, new Speed(overGround.getSpeed()));
							cache.putAll(map);
						}
						break;
					case "VHW": // Water Speed and Heading
						VHW vhw = StringParsers.parseVHW(sentence);
						if (vhw == null) {
							return;
						}
						double bsp = vhw.getBsp();
				//	double hdm = vhw[StringParsers.HDM_in_VHW];
						if (bsp != -Double.MAX_VALUE) {
							cache.put(NMEADataCache.BSP, new Speed(bsp));
						}
						break;
					case "HDG": // Heading, Deviation & Variation
						HDG hdgs = StringParsers.parseHDG(sentence);
						int hdg = (int) hdgs.getHeading();
						double dev = hdgs.getDeviation();
						double var = hdgs.getVariation();
						if (dev == -Double.MAX_VALUE && var == -Double.MAX_VALUE) {
							cache.put(NMEADataCache.HDG_COMPASS, new Angle360(hdg));
						} else {
							double dec = 0d;
							if (dev != -Double.MAX_VALUE && var == 0d) {
								dec = dev;
							} else {
								dec = var;
							}
							if (!"true".equals(System.getProperty("rmc.decl.only"))) {
								cache.put(NMEADataCache.DECLINATION, new Angle180EW(dec));
							}
							cache.put(NMEADataCache.HDG_COMPASS, new Angle360(hdg /* - dec */));
						}
						break;
					case "HDM": // Heading, Magnetic
						int hdm = StringParsers.parseHDM(sentence);
						cache.put(NMEADataCache.HDG_COMPASS, new Angle360(hdm));
						break;
					case "HDT": // Heading, True
						int hdt = StringParsers.parseHDT(sentence);
						cache.put(NMEADataCache.HDG_TRUE, new Angle360(hdt));
					case "MWV": // Wind Speed and Angle
						Wind mwv = StringParsers.parseMWV(sentence);
						if (mwv != null && mwv instanceof ApparentWind) { // TrueWind not used for now
							Map<String, Object> map = new HashMap<>(2);
							map.put(NMEADataCache.AWS, new Speed(mwv.getSpeed()));
							int awa = mwv.getAngle();
							if (awa > 180) {
								awa -= 360;
							}
							map.put(NMEADataCache.AWA, new Angle180(awa));
							cache.putAll(map);
						}
						break;
					case "VWR": // Relative Wind Speed and Angle
						ApparentWind vwr = StringParsers.parseVWR(sentence);
						if (vwr != null) {
							Map<String, Object> map = new HashMap<>(2);
							map.put(NMEADataCache.AWS, new Speed(vwr.getSpeed()));
							int awa = vwr.getAngle();
							if (awa > 180) {
								awa -= 360;
							}
							map.put(NMEADataCache.AWA, new Angle180(awa));
							cache.putAll(map);
						}
						break;
					default:
						break;
				}
				double twa = 0d;
				double tws = 0d;
				double twd = 0;
				double decl = 0d;
				double csp = 0d;
				int cdr = -1;

				int producedWith = -1;
				if (cache != null) {
					synchronized (cache) {
						NMEAUtils.computeAndSendValuesToCache(cache);
						// True Wind
						try {
							twa = ((Angle180) cache.get(NMEADataCache.TWA)).getValue();
						} catch (NullPointerException ignore) {
						} catch (Exception ignore) {
							System.err.println("From " + this.getClass().getName() + ", getting TWA from the cache:" + ignore.toString());
						}
						try {
							tws = ((TrueWindSpeed) cache.get(NMEADataCache.TWS)).getValue();
						} catch (NullPointerException ignore) {
						} catch (Exception ignore) {
							System.err.println("From " + this.getClass().getName() + ", getting TWS from the cache:" + ignore.toString());
						}
						try {
							twd = ((Angle360) cache.get(NMEADataCache.TWD)).getValue();
						} catch (NullPointerException ignore) {
						} catch (Exception ignore) {
							System.err.println("From " + this.getClass().getName() + ", getting TWD from the cache:" + ignore.toString());
						}
						try {
							decl = ((Angle180EW) cache.get(NMEADataCache.DECLINATION)).getValue();
						} catch (NullPointerException ignore) {
						} catch (Exception ignore) {
							System.err.println("From " + this.getClass().getName() + ", getting Decl from the cache:" + ignore.toString());
						}

						try {
							long currentTimeBuffer = 0L;
							@SuppressWarnings("unchecked")
							Map<Long, NMEADataCache.CurrentDefinition> currentMap =
									((Map<Long, NMEADataCache.CurrentDefinition>) cache.get(NMEADataCache.CALCULATED_CURRENT));
							Set<Long> keys = currentMap.keySet();
							if (this.verbose && keys.size() != 1) {
								System.out.println("1 - Nb entry(ies) in Calculated Current Map: " + keys.size());
							}
							for (Long l : keys) {
								int tbl = (int) (l / (1_000)); // in seconds
								if (tbl > currentTimeBuffer) { // Take the biggest one.
									currentTimeBuffer = tbl;
									csp = currentMap.get(l).getSpeed().getValue();
									cdr = (int) Math.round(currentMap.get(l).getDirection().getValue());
									if (this.verbose) {
										System.out.println(String.format("Current Calculated with %d s: Speed: %.02f, Dir: %d", tbl, csp, cdr));
									}
									producedWith = tbl;
								} else {
									if (this.verbose) {
										System.out.println(String.format("Skipping %d ms", tbl));
									}
								}
							}
						} catch (NullPointerException ignore) {
						} catch (Exception ignore) {
							System.err.println("From " + this.getClass().getName() + ", getting CALCULATED_CURRENT from the cache:" + ignore.toString());
						}
					}
				}
				//  System.out.println("From TrueWindSentenceInsertion, TWS:" + tws);

				String nmeaVWT = StringGenerator.generateVWT(generatedStringsPrefix, tws, twa);
				String nmeaMWV = StringGenerator.generateMWV(generatedStringsPrefix, tws,
								(int) Math.round(twa),
								StringParsers.TRUE_WIND);
				String nmeaMWD = StringGenerator.generateMWD(generatedStringsPrefix, twd, tws, decl);

				this.produce(nmeaMWV + NMEAParser.STANDARD_NMEA_EOS);
				this.produce(nmeaVWT + NMEAParser.STANDARD_NMEA_EOS);
				this.produce(nmeaMWD + NMEAParser.STANDARD_NMEA_EOS);

				if (csp != 0 && !Double.isNaN(csp) && cdr != -1) {
					if (verbose) {
						System.out.println(String.format(">>>                                    Producing Current Speed %f, dir %d (with %d s)", csp, cdr, producedWith));
					}
					String nmeaVDR = StringGenerator.generateVDR(generatedStringsPrefix, csp, cdr, cdr - decl);
					this.produce(nmeaVDR + NMEAParser.STANDARD_NMEA_EOS);
				}
			}
		}
	}

	@Override
	public void setVerbose(boolean verbose) {
		super.setVerbose(verbose);
		this.longTimeCurrentCalculator.stream().forEach(ltcc -> ltcc.setVerbose(verbose));
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing True Wind, " + this.getClass().getName());
		if (this.longTimeCurrentCalculator != null) {
			this.longTimeCurrentCalculator.stream().forEach(ltcc -> ltcc.stop());
		}
	}

	public void resetCurrentComputers() {
		this.longTimeCurrentCalculator.stream().forEach(ltcc -> ltcc.resetBuffers());
	}

	public static class ComputerBean {
		private String cls;
		private String type = "tw-current";
		private String timeBufferLength = "600000"; // Default is 10 minutes.
		private int cacheSize = 0;
		private String tbSize = "";
		private boolean verbose = false;
		private String prefix = "OS";

		public int getCacheSize() {
			return this.cacheSize;
		}

		public String getTimeBufferLength() {
			return timeBufferLength;
		}

		public String getPrefix() {
			return prefix;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public ComputerBean(ExtraDataComputer instance) {
			this.cls = instance.getClass().getName();
			this.cacheSize = ApplicationContext.getInstance().getDataCache().size();
			this.verbose = instance.isVerbose();
			this.timeBufferLength = instance.longTimeCurrentCalculator
							.stream()
							.map(ltcc -> String.valueOf(ltcc.getBufferLength()))
							.collect(Collectors.joining(", "));
			this.tbSize = instance.longTimeCurrentCalculator  // tb: Time Buffer
							.stream()
							.map(ltcc -> String.valueOf(ltcc.getBufferSize()))
							.collect(Collectors.joining(", "));
			this.prefix = instance.generatedStringsPrefix;
		}
	}

	@Override
	public Object getBean() {
		return new ComputerBean(this);
	}
}
