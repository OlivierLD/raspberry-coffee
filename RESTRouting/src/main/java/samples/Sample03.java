package samples;

import com.google.gson.Gson;
import gribprocessing.utils.GRIBUtils;
import jgrib.GribFile;
import poc.GRIBDump;
import poc.data.GribDate;
import poc.data.GribType;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Sample03 {

	private final static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));

	/**
	 * From request to JSON.
	 * Skeleton of the REST service.
	 *
	 * @param args unused
	 */
	public static void main(String... args) {
		String request = "GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN";

		try {
			String gribFileName = "grib.grb";
			GRIBUtils.getGRIB(GRIBUtils.generateGRIBRequest(request), ".", gribFileName, verbose);

			GRIBDump dump = new GRIBDump();
			URL gribURL = new File(gribFileName).toURI().toURL();
			GribFile gf = new GribFile(gribURL.openStream());

			Map<GribDate, Map<GribType, Float[][]>> gribMap = dump.dump(gf);
			String json = new Gson().toJson(gribMap);

			if (verbose) {
				System.out.println("Done:");
			}
			System.out.println(json);
			if (verbose) {
				dump.dumpFeedback();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
