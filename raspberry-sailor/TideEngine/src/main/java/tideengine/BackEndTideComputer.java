package tideengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import tideengine.contracts.BackendDataComputer;

import javax.annotation.Nonnull;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;

/**
 * Method agnostic front end access.
 * Calls the right methods, depending on the chosen option (XML, SQL, JSON, etc)
 *
 * >> There is no abstract method here, to keep control on what's done.
 * It all depends on the Option.
 */
public class BackEndTideComputer {

	public enum Option {
		XML,
		SQLITE,
		JSON
	}

	private static Option flavor = Option.XML;
	static {
		String strFlavor = System.getProperty("tide.flavor");
		if (strFlavor != null) {
			for (Option opt : Option.values()) {
				if (strFlavor.equals(opt.name())) {
					flavor = opt;
					break;
				}
			}
		}
	}

	private BackendDataComputer dataComputer = null;

	private static Constituents constituentsObject = null;
	private static Stations stationsObject = null;

	private static boolean verbose = "true".equals(System.getProperty("tide.verbose", "false"));
	private static boolean dataVerbose = "true".equals(System.getProperty("data.verbose", "false"));

	public BackEndTideComputer() {
		if (flavor == Option.XML) {
			this.dataComputer = new BackEndXMLTideComputer();
		} else if (flavor == Option.SQLITE) {
			this.dataComputer = new BackEndSQLITETideComputer();
		} else if (flavor == Option.JSON) {
			this.dataComputer = new BackEndJSONTideComputer();
		} else {
			// TODO Other flavors...
			throw new RuntimeException(String.format("Flavor %s not supported yet.", flavor));
		}
	}

	public BackendDataComputer getDataComputer() {
		return this.dataComputer;
	}

	public static Stations getStations() {
		return stationsObject;
	}

	public static Constituents getConstituents() {
		return constituentsObject;
	}

	// Manage connection types. XML, SQL, etc.
	public void connect() throws Exception {
		if (verbose) {
			System.out.printf("Connecting (%s)\n", flavor);
		}
		long before = 0L, after = 0L;

		this.dataComputer.setVerbose(verbose);
		this.dataComputer.connect(); // This one takes the required implementation in account (XML, SQL, etc)

		if (verbose) {
			before = System.currentTimeMillis();
		}

		constituentsObject = this.dataComputer.buildConstituents(dataVerbose);
		stationsObject = this.dataComputer.getTideStations(dataVerbose);

		if (verbose) {
			after = System.currentTimeMillis();
			System.out.printf("Objects loaded in %s ms\n", NumberFormat.getInstance().format(after - before) );
		}
		if (dataVerbose) {
			// dump the maps
			ObjectMapper mapper = new ObjectMapper();
			String constituentStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(constituentsObject);
			String stationsStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(stationsObject);
			System.out.printf("Constituents:\n%s\n", constituentStr);
			System.out.println("-----");
			System.out.printf("Stations:\n%s\n", stationsStr);
			System.out.println("-----");
		}
	}

	public void disconnect() throws Exception {
		if (verbose) {
			System.out.printf("Disconnecting (%s)\n", flavor);
		}
		if (this.dataComputer != null) {
			this.dataComputer.disconnect();
		} else {
			throw new Exception(String.format("No dataComputer for %s", flavor));
		}
	}

	public static List<Coefficient> buildSiteConstSpeed() throws Exception {
		List<Coefficient> constSpeed = buildSiteConstSpeed(constituentsObject);
		return constSpeed;
	}

	public static double getAmplitudeFix(int year, String name) throws Exception {
		double d = getAmplitudeFix(constituentsObject, year, name);
		return d;
	}

	public static double getEpochFix(int year, String name) throws Exception {
		double d = getEpochFix(constituentsObject, year, name);
		return d;
	}

	public TideStation findTideStation(String stationName, int year) throws Exception {
		TideStation ts = findTideStation(stationName, year, constituentsObject, stationsObject);
		return ts;
	}

	public static List<TideStation> getStationData() throws Exception {
		List<TideStation> alts = getStationData(stationsObject);
		return alts;
	}

	public static Map<String, TideUtilities.StationTreeNode> buildStationTree() {
		TreeMap<String, TideUtilities.StationTreeNode> st = TideUtilities.buildStationTree(stationsObject);
		return st;
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

	public TideStation findTideStation(@Nonnull  String stationName, int year, @Nonnull Constituents constituents, @Nonnull Stations stations) throws Exception {
		long before = System.currentTimeMillis();
		TideStation station = stations.getStations().get(stationName);
		if (station == null) { // Try match
			System.out.println(String.format("%s not found, trying partial match.", stationName));
			Set<String> keys = stations.getStations().keySet();
			for (String s : keys) {
				if (s.toLowerCase(Locale.ROOT).contains(stationName.toLowerCase(Locale.ROOT))) {
					station = stations.getStations().get(s);
					if (station != null) {
						break;
					}
				}
			}
		}
		long after = System.currentTimeMillis();
		if (verbose) {
			System.out.printf("Finding the node took %s ms\n", NumberFormat.getInstance().format(after - before) );
		}

		// Fix for the given year
//  System.out.println("findTideStation: We are in " + year + ", coeff fixed for " + station.yearHarmonicsFixed());
		if (station != null && station.yearHarmonicsFixed() != -1 && station.yearHarmonicsFixed() != year) { // Then reload station data from source
			System.out.println("Reloading Station Data for corrections in year " + year);
			try {
				TideStation newTs = reloadTideStation(URLDecoder.decode(station.getFullName(), StandardCharsets.UTF_8));
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
			if (verbose) {
				System.out.println("Sites coefficients of [" + station.getFullName() + "] fixed for " + year);
			}
		} else if (verbose) {
			System.out.println("Coefficients already fixed for " + year);
		}
		return station;
	}

	private TideStation reloadTideStation(String stationName) throws Exception {
		TideStation ts = this.dataComputer.reloadOneStation(stationName);
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
			System.out.printf("Finding all the stations took %s ms\n", NumberFormat.getInstance().format(after - before) );
		}
		return stationData;
	}

	public void setVerbose(boolean v) {
		verbose = v;
		this.dataComputer.setVerbose(v);
	}
}
