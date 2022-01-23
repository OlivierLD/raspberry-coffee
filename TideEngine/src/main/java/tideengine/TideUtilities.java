package tideengine;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class TideUtilities {
	private final static boolean verbose = false;
	public final static double FEET_2_METERS = 0.30480061d; // US feet to meters
	public final static double COEFF_FOR_EPOCH = 0.017453292519943289D;
	public final static DecimalFormat DF22 = new DecimalFormat("#0.00");
	public final static DecimalFormat DF22PLUS = new DecimalFormat("#0.00");

	static {
		DF22PLUS.setPositivePrefix("+");
	}

	public final static DecimalFormat DF2 = new DecimalFormat("#0");
	public final static DecimalFormat DF2PLUS = new DecimalFormat("#0");

	static {
		DF2PLUS.setPositivePrefix("+");
	}

	public final static DecimalFormat DF31 = new DecimalFormat("##0.0");
	public final static DecimalFormat DF13 = new DecimalFormat("##0.000");
	public final static DecimalFormat DF36 = new DecimalFormat("##0.000000");

	public final static Map<String, String> COEFF_DEFINITION = new HashMap<>();

	static {
		COEFF_DEFINITION.put("M2", "Principal lunar semidiurnal constituent");
		COEFF_DEFINITION.put("S2", "Principal solar semidiurnal constituent");
		COEFF_DEFINITION.put("N2", "Larger lunar elliptic semidiurnal constituent");
		COEFF_DEFINITION.put("K1", "Lunar diurnal constituent");
		COEFF_DEFINITION.put("M4", "Shallow water overtides of principal lunar constituent");
		COEFF_DEFINITION.put("O1", "Lunar diurnal constituent");
		COEFF_DEFINITION.put("M6", "Shallow water overtides of principal lunar constituent");
		COEFF_DEFINITION.put("MK3", "Shallow water terdiurnal");
		COEFF_DEFINITION.put("S4", "Shallow water overtides of principal solar constituent");
		COEFF_DEFINITION.put("MN4", "Shallow water quarter diurnal constituent");
		COEFF_DEFINITION.put("NU2", "Larger lunar evectional constituent");
		COEFF_DEFINITION.put("S6", "Shallow water overtides of principal solar constituent");
		COEFF_DEFINITION.put("MU2", "Variational constituent");
		COEFF_DEFINITION.put("2N2", "Lunar elliptical semidiurnal second");
		COEFF_DEFINITION.put("OO1", "Lunar diurnal");
		COEFF_DEFINITION.put("LAM2", "Smaller lunar evectional constituent");
		COEFF_DEFINITION.put("S1", "Solar diurnal constituent");
		COEFF_DEFINITION.put("M1", "Smaller lunar elliptic diurnal constituent");
		COEFF_DEFINITION.put("J1", "Smaller lunar elliptic diurnal constituent");
		COEFF_DEFINITION.put("MM", "Lunar monthly constituent");
		COEFF_DEFINITION.put("SSA", "Solar semiannual constituent");
		COEFF_DEFINITION.put("SA", "Solar annual constituent");
		COEFF_DEFINITION.put("MSF", "Lunisolar synodic fortnightly constituent");
		COEFF_DEFINITION.put("MF", "Lunisolar fortnightly constituent");
		COEFF_DEFINITION.put("RHO", "Larger lunar evectional diurnal constituent");
		COEFF_DEFINITION.put("Q1", "Larger lunar elliptic diurnal constituent");
		COEFF_DEFINITION.put("T2", "Larger solar elliptic constituent");
		COEFF_DEFINITION.put("R2", "Smaller solar elliptic constituent");
		COEFF_DEFINITION.put("2Q1", "Larger elliptic diurnal");
		COEFF_DEFINITION.put("P1", "Solar diurnal constituent");
		COEFF_DEFINITION.put("2SM2", "Shallow water semidiurnal constituent");
		COEFF_DEFINITION.put("M3", "Lunar terdiurnal constituent");
		COEFF_DEFINITION.put("L2", "Smaller lunar elliptic semidiurnal constituent");
		COEFF_DEFINITION.put("2MK3", "Shallow water terdiurnal constituent");
		COEFF_DEFINITION.put("K2", "Lunisolar semidiurnal constituent");
		COEFF_DEFINITION.put("M8", "Shallow water eighth diurnal constituent");
		COEFF_DEFINITION.put("MS4", "Shallow water quarter diurnal constituent");
	}

	private final static String[] ORDERED_COEFF = {"M2", "S2", "N2", "K1", "M4", "O1", "M6", "MK3", "S4",
			"MN4", "NU2", "S6", "MU2", "2N2", "OO1", "LAM2", "S1", "M1",
			"J1", "MM", "SSA", "SA", "MSF", "MF", "RHO", "Q1", "T2",
			"R2", "2Q1", "P1", "2SM2", "M3", "L2", "2MK3", "K2", "M8",
			"MS4"};

	public static String[] getOrderedCoeff() {
		return ORDERED_COEFF.clone();
	}

	public static Map<String, StationTreeNode> buildStationTree(String stationFileName) {
		InputSource is = null;
		try {
			is = new InputSource(new FileInputStream(new File(stationFileName)));
			is.setEncoding("ISO-8859-1");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return buildStationTree(is);
	}

	public static Map<String, StationTreeNode> buildStationTree(InputSource stationFileInputSource) {
		Map<String, StationTreeNode> set = new TreeMap<>();

		long before = System.currentTimeMillis();
		StationObserver sf = new StationObserver();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			sf.setTreeToPopulate(set);
			saxParser.parse(stationFileInputSource, sf);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		long after = System.currentTimeMillis();
		if (verbose) System.out.println("Populating the tree took " + Long.toString(after - before) + " ms");

		return set;
	}

	public static TreeMap<String, StationTreeNode> buildStationTree(Stations stations) {
		TreeMap<String, StationTreeNode> set = new TreeMap<>();

		long before = System.currentTimeMillis();
		try {
			Set<String> keys = stations.getStations().keySet();
			for (String k : keys) {
				TideStation station = stations.getStations().get(k);
				addStationToTree(station, set);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		long after = System.currentTimeMillis();
		if (verbose) System.out.println("Populating the tree took " + Long.toString(after - before) + " ms");

		return set;
	}

	/**
	 * Rendering on System.out
	 *
	 * @param tree
	 * @param level
	 */
	public static void renderTree(TreeMap<String, StationTreeNode> tree, int level) {
		Set<String> keys = tree.keySet();
		for (String key : keys) {
			StationTreeNode stn = tree.get(key);
			for (int i = 0; i < (2 * level); i++) { // Indentation
				System.out.print(" ");
			}
			System.out.println(stn.toString()); // Station name
			if (stn.getSubTree().size() > 0) {
				renderTree(stn.getSubTree(), level + 1);
			}
		}
	}

	public static double feetToMeters(double d) {
		return d * FEET_2_METERS;
	}

	public static double metersToFeet(double d) {
		return d / FEET_2_METERS;
	}

	public static double getWaterHeight(TideStation ts, List<Coefficient> constSpeed, Calendar when) throws Exception {
		double wh = 0d;
		if (ts != null) {
			// Calculate min/max, for the graph
			int year = when.get(Calendar.YEAR);
			// Calc Jan 1st of the current year
			Calendar jan1st = null;
			if (false) {
				jan1st = new GregorianCalendar(year, Calendar.JANUARY, 1);
			} else {
				jan1st = new GregorianCalendar();
				jan1st.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
//      jan1st.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
				jan1st.set(Calendar.YEAR, year);
				jan1st.set(Calendar.MONTH, Calendar.JANUARY);
				jan1st.set(Calendar.DAY_OF_MONTH, 1);
				jan1st.set(Calendar.HOUR_OF_DAY, 0);
				jan1st.set(Calendar.MINUTE, 0);
				jan1st.set(Calendar.SECOND, 0);
				jan1st.set(Calendar.MILLISECOND, 0);
			}
			wh = getWaterHeight(when, jan1st, ts, constSpeed, true);
//    System.out.println("Water Height in " + ts.getFullName() + " on " + when.toString() + " is " + DF22.format(wh) + " " + ts.getUnit());
		} else if (verbose) {
			System.out.println("Ooch!");
		}

		return wh;
	}

	public static double getWaterHeight(Calendar d, Calendar jan1st, TideStation ts, List<Coefficient> constSpeed) {
		return getWaterHeight(d, jan1st, ts, constSpeed, false);
	}

	public static double getWaterHeight(Calendar d, Calendar jan1st, TideStation ts, List<Coefficient> constSpeed, boolean b) {
		double value = 0d;

		double stationBaseHeight = ts.getBaseHeight();
		long nbSecSinceJan1st = (d.getTimeInMillis() - jan1st.getTimeInMillis()) / 1000L;
//  long nbSecSinceJan1st = (d.getTime().getTime() - jan1st.getTime().getTime() ) / 1000L;
//  System.out.println(" ----- NbSec for " + d.getTime().toString() + " = " + nbSecSinceJan1st);
		double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
		if ("true".equals(System.getProperty("tide.verbose"))) {
			System.out.println("Used TimeOffset in hours:" + timeOffset + ", base height:" + stationBaseHeight);
		}
		value = stationBaseHeight;
		for (int i = 0; i < constSpeed.size(); i++) {
			assert (ts.getHarmonics().get(i).getName().equals(constSpeed.get(i).getName()));
			if (!ts.getHarmonics().get(i).getName().equals(constSpeed.get(i).getName())) {
				System.out.println("..... Mismatch!!!");
			}
			value += (ts.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - ts.getHarmonics().get(i).getEpoch()));
			if ("true".equals(System.getProperty("tide.verbose"))) {
				System.out.printf("Coeff %s - Amplitude: %f, Speed Value: %f, Epoch: %f => Value: %f\n",
						constSpeed.get(i).getName(),
						ts.getHarmonics().get(i).getAmplitude(),
						constSpeed.get(i).getValue(),
						ts.getHarmonics().get(i).getEpoch(),
						value);
			}
//    if (b &&
//        d.get(Calendar.MINUTE) == 0 &&
//        d.get(Calendar.HOUR_OF_DAY) == 0 &&
//        "J1".equals(ts.getHarmonics().get(i).getName()))
//      System.out.println("TS Coefficient:" + ts.getHarmonics().get(i).getName() + " ampl:" + ts.getHarmonics().get(i).getAmplitude() + ", epoch:" + ts.getHarmonics().get(i).getEpoch() + ", timeOffset:" + timeOffset + " value:" + value + ", Date:" + d.getTime().toString());
		}
		if (verbose) System.out.println("-----------------------------");
		if (ts.getUnit().indexOf("^2") > -1) {
			value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
		}
		return value;
	}

	public final static int RISING = 1;
	public final static int FALLING = -1;

	private final static SimpleDateFormat SDF_TIDE = new SimpleDateFormat("EEE, MMM dd, ''yy HH:mm z Z");

	public static List<TimedValue> getTideTableForOneDay(TideStation ts, List<Coefficient> constSpeed, int year, int month, int day, String timeZone2Use) {
		double low1 = Double.NaN;
		double low2 = Double.NaN;
		double high1 = Double.NaN;
		double high2 = Double.NaN;
		Calendar low1Cal = null;
		Calendar low2Cal = null;
		Calendar high1Cal = null;
		Calendar high2Cal = null;
		List<TimedValue> slackList = new ArrayList<>();
		int trend = 0;

		double previousWH = Double.NaN;

		for (int h = 0; h < 24; h++) {
			for (int m = 0; m < 60; m++) {
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone2Use != null ? timeZone2Use : ts.getTimeZone()));
				cal.set(year,
						month,
						day,
						h, m, 0);
				double wh = 0;
				try {
					wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (!Double.isNaN(previousWH)) {
					if (ts.isCurrentStation()) {
						if ((previousWH > 0 && wh <= 0) || (previousWH < 0 && wh >= 0)) {
							slackList.add(new TimedValue("Slack", cal, 0d));
						}
					}
					if (trend == 0) {
						if (previousWH > wh) {
							trend = FALLING;
						} else if (previousWH < wh) {
							trend = RISING;
						}
					} else {
						switch (trend) {
							case RISING:
								if (previousWH > wh) { // Now going down
									Calendar prev = (Calendar) cal.clone();
									prev.add(Calendar.MINUTE, -1);
									if (Double.isNaN(high1)) {
										high1 = previousWH;
										cal.add(Calendar.MINUTE, -1);
										high1Cal = cal;
									} else {
										high2 = previousWH;
										cal.add(Calendar.MINUTE, -1);
										high2Cal = cal;
									}
									trend = FALLING; // Now falling
								}
								break;
							case FALLING:
								if (previousWH < wh) { // Now going up
									Calendar prev = (Calendar) cal.clone();
									prev.add(Calendar.MINUTE, -1);
									if (Double.isNaN(low1)) {
										low1 = previousWH;
										cal.add(Calendar.MINUTE, -1);
										low1Cal = cal;
									} else {
										low2 = previousWH;
										cal.add(Calendar.MINUTE, -1);
										low2Cal = cal;
									}
									trend = RISING; // Now rising
								}
								break;
						}
					}
				}
				previousWH = wh;
			}
		}
		List<TimedValue> timeList = new ArrayList<>(4);
		SDF_TIDE.setTimeZone(TimeZone.getTimeZone(timeZone2Use != null ? timeZone2Use : ts.getTimeZone()));
		if (low1Cal != null) {
			timeList.add(new TimedValue("LW", low1Cal, low1).unit(ts.getDisplayUnit()).formattedDate(SDF_TIDE.format(low1Cal.getTime())));
		}
		if (low2Cal != null) {
			timeList.add(new TimedValue("LW", low2Cal, low2).unit(ts.getDisplayUnit()).formattedDate(SDF_TIDE.format(low2Cal.getTime())));
		}
		if (high1Cal != null) {
			timeList.add(new TimedValue("HW", high1Cal, high1).unit(ts.getDisplayUnit()).formattedDate(SDF_TIDE.format(high1Cal.getTime())));
		}
		if (high2Cal != null) {
			timeList.add(new TimedValue("HW", high2Cal, high2).unit(ts.getDisplayUnit()).formattedDate(SDF_TIDE.format(high2Cal.getTime())));
		}
		if (ts.isCurrentStation() && slackList != null && slackList.size() > 0) {
			slackList.stream().forEach(timeList::add);
		}
		Collections.sort(timeList);
		return timeList;
	}

	public static String getHarmonicCoeffName(TideStation ts,
	                                          List<Coefficient> constSpeed,
	                                          int constSpeedIdx) {
		String name = "";
		if (ts != null) {
			name = ts.getHarmonics().get(constSpeedIdx).getName();
		} else {
			name = constSpeed.get(constSpeedIdx).getName();
		}
		return name;
	}

	public static String getHarmonicCoeffDefinition(String name) {
		return COEFF_DEFINITION.get(name);
	}

	public static double getHarmonicValue(Date d,
	                                      Date jan1st,
	                                      TideStation ts,
	                                      List<Coefficient> constSpeed,
	                                      int constSpeedIdx) {
		double value = 0d;

		double stationBaseHeight = ts.getBaseHeight();
		long nbSecSinceJan1st = (d.getTime() - jan1st.getTime()) / 1000L;
		double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
		value = stationBaseHeight;

		value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
		if (ts.getUnit().indexOf("^2") > -1) {
			value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
		}
		return value;
	}

	public static double getHarmonicValue(Date d,
	                                      Date jan1st,
	                                      TideStation ts,
	                                      List<Coefficient> constSpeed,
	                                      String coeffName) {
		double value = 0d;

		double stationBaseHeight = ts.getBaseHeight();
		long nbSecSinceJan1st = (d.getTime() - jan1st.getTime()) / 1000L;
		double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
		value = stationBaseHeight;

		int constSpeedIdx = getHarmonicIndex(ts.getHarmonics(), coeffName);
//  int constSpeedIdx = getHarmonicIndex(constSpeed, coeffName);

		value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
		if (ts.getUnit().indexOf("^2") > -1) {
			value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
		}
		return value;
	}

	public static int getHarmonicIndex(List<Harmonic> alh, String name) {
		int idx = 0;
		boolean found = false;
		for (Harmonic h : alh) {
			if (h.getName().equals(name)) {
				found = true;
				break;
			} else
				idx++;
		}
		if (!found) {
			System.out.println("Coeff [" + name + "] not found.");
		}
		return (found ? idx : -1);
	}

	public final static int MIN_POS = 0;
	public final static int MAX_POS = 1;

	public static double[] getMinMaxWH(TideStation ts, List<Coefficient> constSpeed, Calendar when) throws Exception {
		double[] minMax = {0d, 0d};
		if (ts != null) {
			// Calculate min/max, for the graph
			int year = when.get(Calendar.YEAR);
			// Calc Jan 1st of the current year
			Calendar jan1st = new GregorianCalendar(year, Calendar.JANUARY, 1);
			Calendar dec31st = new GregorianCalendar(year, Calendar.DECEMBER, 31, 12, 0); // 31 Dec, At noon
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			Calendar date = (Calendar) jan1st.clone();
			while (date.before(dec31st)) {
				double d = getWaterHeight(date, jan1st, ts, constSpeed);
//      System.out.println("Height at " + ts.getFullName() + " at " + date.getTime() + " = " + d);
				max = Math.max(max, d);
				min = Math.min(min, d);

				date.add(Calendar.HOUR, 2); // date = new Date(date.getTime() + (7200 * 1000)); // Plus 2 hours
			}
			//  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
			minMax[MIN_POS] = min;
			minMax[MAX_POS] = max;
		}
		return minMax;
	}

	public static double[] getMinMaxWH(TideStation ts, List<Coefficient> constSpeed, Calendar from, Calendar to) throws Exception {
		double[] minMax = {0d, 0d};
		if (ts != null) {
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			// Calculate min/max, for the graph
			Calendar date = (Calendar) from.clone();
			while (date.getTime().before(to.getTime())) {
				// Calc Jan 1st of the current year
				Calendar jan1st = new GregorianCalendar(date.get(Calendar.YEAR), 0, 1);
				double d = getWaterHeight(date, jan1st, ts, constSpeed);
				max = Math.max(max, d);
				min = Math.min(min, d);

				date.add(Calendar.HOUR, 2); // Plus 2 hours
			}
			//  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
			minMax[MIN_POS] = min;
			minMax[MAX_POS] = max;
		}
		return minMax;
	}

	public static double getWaterHeightIn(double d, TideStation ts, String unit) {
		double val = d;
		if (ts.isCurrentStation()) {
			throw new RuntimeException(ts.getFullName() + " is a current station. Method getWaterHeightIn applies only to tide stations.");
		}
		if (!unit.equals(ts.getUnit())) {
			if (!unit.equals(TideStation.METERS) && !unit.equals(TideStation.FEET))
				throw new RuntimeException("Unsupported unit [" + unit + "]. Only " + TideStation.METERS + " or " + TideStation.FEET + " please.");
			if (unit.equals(TideStation.METERS) && ts.getUnit().equals(TideStation.FEET)) {
				val *= FEET_2_METERS;
			} else {
				val /= FEET_2_METERS;
			}
		}
		return val;
	}

	public static List<String[]> getStationHarmonicConstituents(TideStation ts, List<Coefficient> constSpeed) {
		List<String[]> hcList = new ArrayList<>();
		int rank = 1;

		Calendar now = GregorianCalendar.getInstance();
		Calendar jan1st = new GregorianCalendar(now.get(Calendar.YEAR), Calendar.JANUARY, 1);

		long nbSecSinceJan1st = (now.getTimeInMillis() - jan1st.getTimeInMillis()) / 1000L;
		double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;

		for (String k : COEFF_DEFINITION.keySet()) {
			int constIdx = getHarmonicIndex(ts.getHarmonics(), k);
			if (constIdx > -1) {
				double amplitude = ts.getHarmonics().get(constIdx).getAmplitude();
				double epoch = ts.getHarmonics().get(constIdx).getEpoch();
				double speed = constSpeed.get(constIdx).getValue();
				double phase = Math.toDegrees(speed * timeOffset - epoch) % 360;

				String[] line = {Integer.toString(rank),
						k,
						DF13.format(amplitude),
						DF31.format(phase),
						DF36.format(speed)
				};
				hcList.add(line);
				rank++;
			}
		}
		return hcList;
	}

	private static void addStationToTree(TideStation ts, Map<String, StationTreeNode> currentTree) {
		String timeZoneLabel = "";
		try {
			timeZoneLabel = ts.getTimeZone().substring(0, ts.getTimeZone().indexOf("/"));
		} catch (Exception ex) {
			System.err.println(ex.toString() + " for " + ts.getFullName() + " , " + ts.getTimeZone());
		}
		StationTreeNode tzstn = currentTree.get(timeZoneLabel);
		if (tzstn == null) {
			tzstn = new StationTreeNode(timeZoneLabel);
			currentTree.put(timeZoneLabel, tzstn);
		}
		currentTree = tzstn.getSubTree();
		String timeZoneLabel2 = ts.getTimeZone().substring(ts.getTimeZone().indexOf("/") + 1);
		tzstn = currentTree.get(timeZoneLabel2);
		if (tzstn == null) {
			tzstn = new StationTreeNode(timeZoneLabel2);
			currentTree.put(timeZoneLabel2, tzstn);
		}
		currentTree = tzstn.getSubTree();

		StationTreeNode stn = null;
		for (String name : ts.getNameParts()) {
			stn = currentTree.get(name);
			if (stn == null) {
				stn = new StationTreeNode(name);
				stn.setStationType(ts.isCurrentStation() ? StationTreeNode.CURRENT_STATION : StationTreeNode.TIDE_STATION);
				currentTree.put(name, stn);
			}
			currentTree = stn.getSubTree();
		}
		stn.setFullStationName(ts.getFullName());
	}

	public static class StationTreeNode implements Comparable {
		public final static int TIDE_STATION = 1;
		public final static int CURRENT_STATION = 2;

		private String label = "";
		private String fullStationName = null;
		private int stationType = 0;
		private TreeMap<String, StationTreeNode> subTree = new TreeMap<>();

		public StationTreeNode(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return this.label;
		}

		public int compareTo(Object o) {
			return this.label.compareTo(o.toString());
		}

		public TreeMap<String, StationTreeNode> getSubTree() {
			return subTree;
		}

		public void setFullStationName(String fullStationName) {
			this.fullStationName = fullStationName;
		}

		public String getFullStationName() {
			return fullStationName;
		}

		public void setStationType(int stationType) {
			this.stationType = stationType;
		}

		public int getStationType() {
			return stationType;
		}

		public boolean equals(Object o) {
			return (o instanceof StationTreeNode && this.compareTo(o) == 0);
		}
	}

	public static class StationObserver extends DefaultHandler {
		private TideStation ts = null;

		private boolean foundStation = false;
		private boolean foundNameCollection = false;

		private Map<String, StationTreeNode> tree = null;

		public void setTreeToPopulate(Map<String, StationTreeNode> tree) {
			this.tree = tree;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			//    super.startElement(uri, localName, qName, attributes);
			if (!foundStation && "station".equals(qName)) {
				String name = attributes.getValue("name");
				foundStation = true;
				ts = new TideStation();
				ts.setFullName(name);
			} else if (foundStation) {
				if ("name-collection".equals(qName)) {
					foundNameCollection = true;
				} else if ("name-part".equals(qName) && foundNameCollection) {
					ts.getNameParts().add(attributes.getValue("name"));
				} else if ("position".equals(qName)) {
					ts.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
					ts.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
				} else if ("time-zone".equals(qName)) {
					ts.setTimeZone(attributes.getValue("name"));
					ts.setTimeOffset(attributes.getValue("offset"));
				} else if ("base-height".equals(qName)) {
					ts.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
					ts.setUnit(attributes.getValue("unit"));
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);
			if (foundStation && "station".equals(qName)) {
				foundStation = false;
				addStationToTree(ts, tree);
			} else if (foundNameCollection && "name-collection".equals(qName)) {
				foundNameCollection = false;
			}
		}
	}

	public static class TimedValue implements Comparable<TimedValue> {
		private Calendar cal;
		double value;
		String type = "";

		long epoch;
		String unit;
		String formattedDate;

		public TimedValue(String type, Calendar cal, double d) {
			this.type = type;
			this.cal = cal;
			this.epoch = cal.getTimeInMillis();
			this.value = d;
		}

		public TimedValue unit(String unit) {
			this.unit = unit;
			return this;
		}

		public TimedValue formattedDate(String formattedDate) {
			this.formattedDate = formattedDate;
			return this;
		}

		public int compareTo(TimedValue tv) {
			return this.cal.compareTo(tv.getCalendar());
		}

		public Calendar getCalendar() {
			return cal;
		}

		public double getValue() {
			return value;
		}

		public String getType() {
			return type;
		}
	}

	public static class SpecialPrm {
		private int tideType;
		private int fromHour;
		private int toHour;
		private int[] weekdays;

		public void setTideType(int tideType) {
			this.tideType = tideType;
		}

		public int getTideType() {
			return tideType;
		}

		public void setFromHour(int fromHour) {
			this.fromHour = fromHour;
		}

		public int getFromHour() {
			return fromHour;
		}

		public void setToHour(int toHour) {
			this.toHour = toHour;
		}

		public int getToHour() {
			return toHour;
		}

		public void setWeekdays(int[] weekdays) {
			this.weekdays = weekdays;
		}

		public int[] getWeekdays() {
			return weekdays;
		}
	}
}
