package tests.findclosest;

import calc.GeoPoint;
import calc.calculation.AstroComputer;
import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is just a test, not a unit-test
 *
 * From a given position, find the closest tide station.
 */
public class FindClosest {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");

    private final static double USER_POS_LATITUDE = 37.7489;
    private final static double USER_POS_LONGITUDE = -122.5070;

    private static BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

    /**
     * All values in radians
     *
     * @return in radians
     */
    public static double getDistance(double fromL, double fromG, double toL, double toG) {
        return Math.acos((Math.sin(fromL) * Math.sin(toL)) + (Math.cos(fromL) * Math.cos(toL) * Math.cos(toG - fromG)));
    }

    public static void main(String... args) throws Exception {
        System.out.println(args.length + " Argument(s)...");

        Calendar now = GregorianCalendar.getInstance(); // Now. Current DateTime

//		double distNM = 60.0 * Math.toDegrees(getDistance(Math.toRadians(37.73), Math.toRadians(-122.50), Math.toRadians(38.73), Math.toRadians(-122.50)));
//		System.out.println("Dist:" + distNM);

        try {
            System.out.println(FindClosestResourceBundle.buildMessage("title"));
        } catch (NullPointerException npe) {
            System.out.println("Look at that!");
        }

        backEndTideComputer.connect();
        backEndTideComputer.setVerbose(false);

        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

        List<TideStation> stationData = BackEndTideComputer.getStationData();
        System.out.printf("%d stations\n", stationData.size());

        long before = System.currentTimeMillis();
        Optional<TideStation> closest = stationData.stream()
                .sorted(Comparator.comparingDouble(station ->
                        /*60.0 * Math.toDegrees(*/getDistance(Math.toRadians(USER_POS_LATITUDE),
                        Math.toRadians(USER_POS_LONGITUDE),
                        Math.toRadians(station.getLatitude()),
                        Math.toRadians(station.getLongitude()))/*)*/))
                .findFirst();
        long after = System.currentTimeMillis();
        System.out.printf("Closest station found in %d ms.\n", (after - before));

        TideStation ts = closest.orElseThrow();

        if (ts != null) {
            String location = URLDecoder.decode(ts.getFullName(), "UTF-8");
            System.out.println(String.format(">> From %s, closest station is %s, distance %.02f nm.",
                    new GeoPoint(USER_POS_LATITUDE, USER_POS_LONGITUDE),
                    location,
                    60d * Math.toDegrees(getDistance(Math.toRadians(USER_POS_LATITUDE),
                            Math.toRadians(USER_POS_LONGITUDE),
                            Math.toRadians(ts.getLatitude()),
                            Math.toRadians(ts.getLongitude())))));

            now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
            System.out.printf("Now: %s\n", SDF.format(now.getTime()));

			double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
			System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
			double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
			System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());

			// Low and high water for current date/time
            List<TideUtilities.TimedValue> table = TideUtilities.getTideTableForOneDay(
                    ts,
                    constSpeed, now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH), null);

            table.forEach(tv -> System.out.printf("%s %s : %s %s%n",
                    tv.getType(),
                    tv.getCalendar().getTime(),
                    TideUtilities.DF22PLUS.format(tv.getValue()),
                    ts.getDisplayUnit()));

            // Bonus: Astronomical Data
            System.setProperty("astro.verbose", "false");
            AstroComputer.calculate(); // Default: now
            double moonPhase = AstroComputer.getMoonPhase();
            double moonTilt = AstroComputer.getMoonTilt(USER_POS_LATITUDE, USER_POS_LONGITUDE);
            System.out.printf("Moon Phase: %.01f\272, Tilt: %.01f\272", moonPhase, moonTilt);
        } else {
            System.out.println("Not found :(");
        }
        backEndTideComputer.disconnect();
    }
}
