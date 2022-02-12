package tests;

import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;
import tideengine.TideUtilities.TimedValue;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is just a test, not a unit-test.
 * It fails if it does not work ;)
 */
public class SimplestMain {
//    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");

    public static void main(String... args) throws Exception {
        System.out.println(args.length + " Argument(s)...");
        boolean xmlTest = false;

        System.setProperty("tide.verbose", "true");

        if (xmlTest) {
            System.out.println("XML Tests");
        } else {
            System.out.println("JSON Tests");
            System.setProperty("tide.flavor", "JSON");
        }
        final BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
        backEndTideComputer.connect();
        backEndTideComputer.setVerbose(true);

        // Some tests
        TideStation ts = null;

        long before = 0;
        long after = 0;

        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

        Calendar now = GregorianCalendar.getInstance();
        String location;
//        final String STATION_PATTERN = "Port Townsend";
      final String STATION_PATTERN = "Port-Navalo";
//        final String STATION_PATTERN = "ICELAND";

        System.setProperty("tide.verbose", "false");
		location = URLEncoder.encode(STATION_PATTERN, "UTF-8").replace("+", "%20");
        ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
        if (ts != null) {
            now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
            if (ts != null) {
                if (false) {
                    double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
                    System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
                }
                before = System.currentTimeMillis();
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
                after = System.currentTimeMillis();
                System.out.println("-----------------------------");
                System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + STATION_PATTERN + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
                System.out.println("-----------------------------");
                System.out.printf("Done is %s ms.\n", NumberFormat.getInstance().format(after - before));
            }
        } else {
            System.out.println(String.format("%s not found :(", location));
        }
        backEndTideComputer.disconnect();
    }
}
