package poc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;
import poc.data.GribDate;
import poc.data.GribType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class GRIBDump {

	private final static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));

	private Map<GribDate, Map<GribType, Float[][]>> gribDataMap = null;
	private List<String> feedback = null;

	public GRIBDump() {
		super();
	}

	public final Map<GribDate, Map<GribType, Float[][]>> dump(GribFile gribFile) {
		feedback = new ArrayList<>(1);
		try {
//    GribPDSParamTable.turnOffJGRIBLogging();
			gribDataMap = new HashMap<>();

			for (int i = 0; i < gribFile.getLightRecords().length; i++) {
				try {
					GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
					GribRecordPDS grpds = gr.getPDS(); // Headers and Data
					GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
					GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

					Date date = grpds.getGMTForecastTime().getTime();

					int width = grgds.getGridNX();
					int height = grgds.getGridNY();
					double stepX = grgds.getGridDX();
					double stepY = grgds.getGridDY();
					double top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
					double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
					double left = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
					double right = Math.max(grgds.getGridLon1(), grgds.getGridLon2());

					String type = grpds.getType();
					String description = grpds.getDescription();
					String unit = grpds.getUnit();

					if (right - left > 180) { // then swap. like left=-110, right=130
						double tmp = right;
						right = left;
						left = tmp;
					}

					GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

					Float[][] data = new Float[height][width];
					float val = 0F;
					for (int col = 0; col < width; col++) {
						for (int row = 0; row < height; row++) {
							try {
								val = gr.getValue(col, row);
								if (val > grbds.getMaxValue() || val < grbds.getMinValue()) {
									if (verbose) {
										System.out.println("type:" + type + " val:" + val + " is out of [" + grbds.getMinValue() + ", " + grbds.getMaxValue() + "]");
									}
									val = grbds.getMinValue(); // TODO Make sure that's right...
								}
								data[row][col] = val;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}

					Map<GribType, Float[][]> subMap = gribDataMap.get(gDate);
					if (subMap == null) {
						subMap = new HashMap<>();
					}
					subMap.put(new GribType(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
					gribDataMap.put(gDate, subMap);
				} catch (NoValidGribException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			feedback.add(ex.toString());
		}
		return gribDataMap;
	}

	public void dumpFeedback() {
		if (feedback != null) {
			feedback.stream().forEach(System.out::println);
		}
	}

	/**
	 * For Gson to work properly...
	 *
	 * @param data
	 * @return
	 */
	public static List<DatedGRIB> expandGrib(Map<GribDate, Map<GribType, Float[][]>> data) {
		List<DatedGRIB> grib = new ArrayList<>();
		// Sort by date
		SortedSet<GribDate> ss = new TreeSet<>(data.keySet());
		for (GribDate gribDate : ss) {
			Map<GribType, Float[][]> typeMaps = data.get(gribDate);
			DatedGRIB datedGRIB = new DatedGRIB();
			datedGRIB.gribDate = gribDate;
			datedGRIB.typedData = new ArrayList<>();

			for (GribType type : typeMaps.keySet()) {
				GRIBTypedData typedData = new GRIBTypedData();
				typedData.gribType = type;
				typedData.data = typeMaps.get(type);
				datedGRIB.typedData.add(typedData);
			}
			grib.add(datedGRIB);
		}
		return grib;
	}

	public static class GRIBTypedData {
		GribType gribType;
		Float[][] data;
	}

	public static class DatedGRIB {
		GribDate gribDate;
		List<GRIBTypedData> typedData;
	}

	public List<DatedGRIB> getExpandedGBRIB(GribFile gf) {
		return expandGrib(dump(gf));
	}

	// For standalone tests
	public static void main(String... args) throws Exception {
		GRIBDump gribDump = new GRIBDump();
//	"GRIB_2017_10_16_07_31_47_PDT.grb", "GRIB_2009_02_25_Sample.grb";
//	String gribFileName = "GRIB_2009_02_25_Sample.grb";
		String gribFileName = "grib.grb";
		URL gribURL = new File(gribFileName).toURI().toURL();
		GribFile gf = new GribFile(gribURL.openStream());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(gribDump.getExpandedGBRIB(gf)));

		if (verbose) {
			System.out.println("Done:");
		}
		if (verbose) {
			gribDump.dumpFeedback();
		}
	}
}
