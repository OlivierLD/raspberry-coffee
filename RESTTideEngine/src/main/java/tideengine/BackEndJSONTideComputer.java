package tideengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import tideengine.contracts.BackendDataComputer;
import tideengine.utils.ZipUtils;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BackEndJSONTideComputer implements BackendDataComputer {

	public final static String ARCHIVE_STREAM = "json/json.zip";
	public final static String CONSTITUENTS_ENTRY = "constituents.json";
	public final static String STATIONS_ENTRY = "stations.json";

	private static ObjectMapper mapper = new ObjectMapper();

	private static boolean verbose = false;

	@Override
	public void connect() throws Exception {
	}

	@Override
	public void disconnect() throws Exception {
	}

	@Override
	public Constituents buildConstituents() throws Exception {
		Constituents constituents;
		try {
			InputStream is = ZipUtils.getZipInputStream(BackEndJSONTideComputer.class, ARCHIVE_STREAM, CONSTITUENTS_ENTRY);
			Map<String, Map<String, Object>> constMap = mapper.readValue(is, Map.class);
			constituents = new Constituents();
			Map<String, Object> subMap = constMap.get("constSpeedMap");
			AtomicInteger rank = new AtomicInteger(0);
			subMap.keySet().forEach(k -> {
				Map<String, Object> one = (Map)subMap.get(k);
				rank.set(rank.intValue() + 1);
				Constituents.ConstSpeed constSpeed = new Constituents.ConstSpeed(rank.get(), (String)one.get("coeffName"), (Double)one.get("coeffValue"));
				Map<String, Double> factors = (Map)one.get("factors");
				factors.keySet().forEach(year -> {
					constSpeed.getFactors().put(Integer.parseInt(year), factors.get(year));
				});
				// constSpeed.getFactors().putAll((Map)one.get("factors"));
				Map<String, Double> equilibrium = (Map)one.get("equilibrium");
				equilibrium.keySet().forEach(year -> {
					constSpeed.getEquilibrium().put(Integer.parseInt(year), factors.get(year));
				});
				// constSpeed.getEquilibrium().putAll((Map)one.get("equilibrium"));
				constituents.getConstSpeedMap().put(k, constSpeed);
			});
		} catch (Exception ex) {
			throw ex;
		}
		return constituents;
	}

	@Override
	public Stations getTideStations() throws Exception {
		return new Stations(getStationData());
	}

	@Override
	public Map<String, TideStation> getStationData() throws Exception {
		Map<String, TideStation> stationData;
		try {
			InputStream is = ZipUtils.getZipInputStream(BackEndJSONTideComputer.class, ARCHIVE_STREAM, STATIONS_ENTRY);
			Map<String, Object> mapData = mapper.readValue(is, Map.class);
			stationData = new HashMap<>();
			Map<String, Object> subMap = (Map)mapData.get("stations");
			subMap.keySet().forEach(k -> {
				TideStation ts = new TideStation();
				Map<String, Object> tsMap = (Map)subMap.get(k);
				String fullName = (String)tsMap.get("fullName");
				ts.setFullName(fullName);
				((List<String>)tsMap.get("nameParts")).forEach(namePart -> ts.getNameParts().add(namePart));
				ts.setLatitude((Double)tsMap.get("latitude"));
				ts.setLongitude((Double)tsMap.get("longitude"));
				ts.setTimeOffset((String)tsMap.get("timeOffset"));
				ts.setTimeZone((String)tsMap.get("timeZone"));
				ts.setUnit((String)tsMap.get("unit"));
				ts.setBaseHeight((Double)tsMap.get("baseHeight"));
				((List)tsMap.get("harmonics")).forEach(h ->
					ts.getHarmonics().add(new Harmonic((String)((Map)h).get("name"),
							                           (Double)((Map)h).get("amplitude"),
							                           (Double)((Map)h).get("epoch"))));
				stationData.put(fullName, ts);
			});
		} catch (Exception ex) {
			throw ex;
		}
		return stationData;
	}

	// Get data as a List, not as a Map
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

	@Override
	public TideStation reloadOneStation(String stationName) throws Exception {
		return null;
	}

	@Override
	public void setVerbose(boolean verbose) {
		BackEndJSONTideComputer.verbose = verbose;
	}
}
