package tideengine;

import org.xml.sax.InputSource;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Access method agnostic front end.
 * Calls the right methods, depending on the chosen option (XML, SQL, JAVA, json, etc)
 */
public class BackEndTideComputer {
	private static Constituents constituentsObject = null;
	private static Stations stationsObject = null;

	private static boolean verbose = "true".equals(System.getProperty("tide.verbose", "false"));

	public static Stations getStations() {
		return stationsObject;
	}

	public static Constituents getConstituents() {
		return constituentsObject;
	}

	public static void connect() throws Exception {
		long before = 0L, after = 0L;
		BackEndXMLTideComputer.setVerbose(verbose);
		if (verbose) {
			before = System.currentTimeMillis();
		}
		constituentsObject = BackEndXMLTideComputer.buildConstituents(); // Uses SAX
		stationsObject = BackEndXMLTideComputer.getTideStations();       // Uses SAX
		if (verbose) {
			after = System.currentTimeMillis();
			System.out.println("Objects loaded in " + Long.toString(after - before) + " ms");
		}
	}

	public static void disconnect() throws Exception {
	}

	public static List<Coefficient> buildSiteConstSpeed() throws Exception {
		List<Coefficient> constSpeed = null;
		constSpeed = buildSiteConstSpeed(constituentsObject);
		return constSpeed;
	}

	public static double getAmplitudeFix(int year, String name) throws Exception {
		double d = 0;
		d = getAmplitudeFix(constituentsObject, year, name);
		return d;
	}

	public static double getEpochFix(int year, String name) throws Exception {
		double d = 0;
		d = getEpochFix(constituentsObject, year, name);
		return d;
	}

	public static TideStation findTideStation(String stationName, int year) throws Exception {
		TideStation ts = null;
		ts = findTideStation(stationName, year, constituentsObject, stationsObject);
		return ts;
	}

	public static List<TideStation> getStationData() throws Exception {
		List<TideStation> alts = null;
		alts = getStationData(stationsObject);
		return alts;
	}

	public static TreeMap<String, TideUtilities.StationTreeNode> buildStationTree() {
		TreeMap<String, TideUtilities.StationTreeNode> st = null;

		st = TideUtilities.buildStationTree(stationsObject);
		return st;
	}

	public static InputStream getZipInputStream(String zipStream, String entryName) throws Exception {
		ZipInputStream zip = new ZipInputStream(BackEndXMLTideComputer.class.getResourceAsStream(zipStream));
		InputStream is = null;
		boolean go = true;
		while (go) {
			ZipEntry ze = zip.getNextEntry();
			if (ze == null)
				go = false;
			else {
				if (ze.getName().equals(entryName)) {
					is = zip;
					go = false;
				}
			}
		}
		if (is == null) {
			throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
		}
		return is;
	}

	public static InputSource getZipInputSource(String zipStream, String entryName) throws Exception {
		ZipInputStream zip = new ZipInputStream(BackEndXMLTideComputer.class.getResourceAsStream(zipStream));
		InputSource is = null;
		boolean go = true;
		while (go) {
			ZipEntry ze = zip.getNextEntry();
			if (ze == null) {
				go = false;
			} else {
				if (ze.getName().equals(entryName)) {
					is = new InputSource(zip);
					is.setEncoding("ISO-8859-1");
					go = false;
				}
			}
		}
		if (is == null) {
			throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
		}
		return is;
	}

	public static List<Coefficient> buildSiteConstSpeed(Constituents doc) throws Exception {
		List<Coefficient> csal = new ArrayList<>();
		Map<String, Constituents.ConstSpeed> csm = doc.getConstSpeedMap();
		csm.keySet()
				.stream()
				.forEach(key -> {
					Constituents.ConstSpeed cs = csm.get(key);
					Coefficient coef = new Coefficient(cs.getCoeffName(), cs.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH);
					csal.add(coef);
				});

		return csal;
	}

	public static double getAmplitudeFix(Constituents doc, int year, String name) throws Exception {
		double d = 0;
		try {
			Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
			double f = cs.getFactors().get(year);
			d = f;
		} catch (Exception ex) {
			System.err.println("Error for [" + name + "] in [" + year + "]");
			throw ex;
		}
		return d;
	}

	public static double getEpochFix(Constituents doc, int year, String name) throws Exception {
		double d = 0;
		try {
			Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
			double f = cs.getEquilibrium().get(year);
			d = f * TideUtilities.COEFF_FOR_EPOCH;
		} catch (Exception ex) {
			System.err.println("Error for [" + name + "] in [" + year + "]");
			throw ex;
		}
		return d;
	}

	public static TideStation findTideStation(@Nonnull  String stationName, int year, @Nonnull Constituents constituents, @Nonnull Stations stations) throws Exception {
		long before = System.currentTimeMillis();
		TideStation station = stations.getStations().get(stationName);
		if (station == null) { // Try match
			System.out.println(String.format("%s not found, trying partial match.", stationName));
			Set<String> keys = stations.getStations().keySet();
			for (String s : keys) {
				if (s.contains(stationName)) {
					station = stations.getStations().get(s);
					if (station != null)
						break;
				}
			}
		}
		long after = System.currentTimeMillis();
		if (verbose) {
			System.out.println("Finding the node took " + Long.toString(after - before) + " ms");
		}

		// Fix for the given year
//  System.out.println("findTideStation: We are in " + year + ", coeff fixed for " + station.yearHarmonicsFixed());
		if (station != null && station.yearHarmonicsFixed() != -1 && station.yearHarmonicsFixed() != year) { // Then reload station data from source
			System.out.println("Reloading Station Data for corrections in year " + year);
			try {
				TideStation newTs = reloadTideStation(URLDecoder.decode(station.getFullName(), "UTF-8"));
				stations.getStations().put(station.getFullName(), newTs);
				station = newTs;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// Correction to the Harmonics
		if (station != null && station.yearHarmonicsFixed() == -1) {
			for (Harmonic harm : station.getHarmonics()) {
				String name = harm.getName();
				if (!"x".equals(name)) {
					double amplitudeFix = getAmplitudeFix(constituents, year, name);
					double epochFix = getEpochFix(constituents, year, name);

					harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
					harm.setEpoch(harm.getEpoch() - epochFix);

					//      System.out.println(stationName + ": Amplitude Fix for " + name + " in " + year + " is " + amplitudeFix + " (->" + harm.getAmplitude() + ")");
					//      System.out.println(stationName + ": Epoch Fix for " + name + " in " + year + " is " + epochFix + " (->" + harm.getEpoch() + ")");
				}
			}
			station.setHarmonicsFixedForYear(year);
			if (verbose)
				System.out.println("Sites coefficients of [" + station.getFullName() + "] fixed for " + year);
		} else if (verbose) {
			System.out.println("Coefficients already fixed for " + year);
		}
		return station;
	}

	private static TideStation reloadTideStation(String stationName) throws Exception {
		TideStation ts = null;
		ts = BackEndXMLTideComputer.reloadOneStation(stationName);
		return ts;
	}

	public static List<TideStation> getStationData(Stations stations) throws Exception {
		long before = System.currentTimeMillis();
		List<TideStation> stationData = new ArrayList<>();
		Set<String> keys = stations.getStations().keySet();
		for (String k : keys) {
			try {
				stationData.add(stations.getStations().get(k));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		long after = System.currentTimeMillis();
		if (verbose) {
			System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
		}
		return stationData;
	}

	public static void setVerbose(boolean v) {
		verbose = v;
		BackEndXMLTideComputer.setVerbose(v);
	}
}
