package calc.calculation;

import calc.*;
import calc.calculation.nauticalalmanac.*;
import utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Static utilities
 * <p>
 * Provide deltaT as a System variable: -DdeltaT=68.9677
 * See:
 * http://aa.usno.navy.mil/data/docs/celnavtable.php,
 * http://maia.usno.navy.mil/
 * http://maia.usno.navy.mil/ser7/deltat.data
 * <p>
 * https://www.usno.navy.mil/USNO/earth-orientation/eo-products/long-term
 */
public class AstroComputer {

    public static class GP {
        String name;
        double decl;
        double gha;
        BodyFromPos fromPos;

        @Override
        public String toString() {
            return String.format("Name:%s, decl:%f, gha:%f, From:%s", name, decl, gha, (fromPos != null ? fromPos.toString() : "null"));
        }

        public GP name(String body) {
            this.name = body;
            return this;
        }

        public GP decl(double d) {
            this.decl = d;
            return this;
        }

        public GP gha(double d) {
            this.gha = d;
            return this;
        }

        public GP bodyFromPos(BodyFromPos fromPos) {
            this.fromPos = fromPos;
            return this;
        }
    }

    public static class OBS {
        double alt;
        double z;

        @Override
        public String toString() {
            return "OBS{" +
                    "alt=" + alt +
                    ", z=" + z +
                    '}';
        }

        public OBS alt(double alt) {
            this.alt = alt;
            return this;
        }

        public OBS z(double z) {
            this.z = z;
            return this;
        }
    }

    public static class Pos {
        double latitude;
        double longitude;

        @Override
        public String toString() {
            return String.format("%s/%s", GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS), GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW));
        }

        public Pos latitude(double lat) {
            this.latitude = lat;
            return this;
        }

        public Pos longitude(double lng) {
            this.longitude = lng;
            return this;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static class BodyFromPos {
        Pos observer;
        OBS observed;

        @Override
        public String toString() {
            return "BodyFromPos{" +
                    "observer=" + observer +
                    ", observed=" + observed +
                    '}';
        }

        public BodyFromPos observer(Pos from) {
            this.observer = from;
            return this;
        }

        public BodyFromPos observed(OBS asSeen) {
            this.observed = asSeen;
            return this;
        }
    }

    public static class GreatCircleWayPointWithBodyFromPos extends GreatCircleWayPoint {
        private BodyFromPos wpFromPos;

        public GreatCircleWayPointWithBodyFromPos(GreatCirclePoint p, Double z) {
            super(p, z);
        }

        public BodyFromPos getWpFromPos() {
            return wpFromPos;
        }

        public void setWpFromPos(BodyFromPos wpFromPos) {
            this.wpFromPos = wpFromPos;
        }

        @Override
        public String toString() {
            return "GreatCircleWayPointWithBodyFromPos{" +
                    "wpFromPos=" + wpFromPos +
                    '}';
        }
    }

    private static int year = -1, month = -1, day = -1, hour = -1, minute = -1, second = -1;
    private static double deltaT = 66.4749d; // 2011. Overridden by deltaT system variable, or calculated on the fly.

    private final static String[] WEEK_DAYS = {
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };
    private static String dow = "";
    private static String moonPhase = "";

    // Updated after the calculate invocation.
    public static synchronized double getDeltaT() {
        return deltaT;
    }

    public static synchronized void setDateTime(int y, int m, int d, int h, int mi, int s) {
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;
    }

    public static synchronized Calendar getCalculationDateTime() {
        Calendar calcDate = GregorianCalendar.getInstance();
        calcDate.set(Calendar.YEAR, year);
        calcDate.set(Calendar.MONTH, month - 1);
        calcDate.set(Calendar.DAY_OF_MONTH, day);

        calcDate.set(Calendar.HOUR_OF_DAY, hour);
        calcDate.set(Calendar.MINUTE, minute);
        calcDate.set(Calendar.SECOND, second);

        return calcDate;
    }

    /**
     * Time are UTC
     *
     * @param y  year
     * @param m  Month. Attention: Jan=1, Dec=12 !!!! Does NOT start with 0.
     * @param d  day
     * @param h  hour
     * @param mi minute
     * @param s  second
     * @return Phase in Degrees
     */
    public static synchronized double getMoonPhase(int y, int m, int d, int h, int mi, int s) {
        double phase = 0f;
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;

        calculate();
        phase = Context.lambdaMapp - Context.lambda_sun;
        while (phase < 0d) phase += 360d;
        return phase;
    }

    /**
     * Assume that calculate has been invoked already
     *
     * @return
     */
    public static synchronized double getMoonPhase() {
        double phase = Context.lambdaMapp - Context.lambda_sun;
        while (phase < 0d) {
            phase += 360d;
        }
        return phase;
    }

    /**
     * Get the moon tilt
     *
     * @param obsLatitude  Observer's latitude in degrees
     * @param obsLongitude Observer's longitude in degrees
     * @return Moon tilt, in degrees
     */
    public static synchronized double getMoonTilt(double obsLatitude, double obsLongitude) {
        final SightReductionUtil sru = new SightReductionUtil();

        double moonLongitude = AstroComputer.ghaToLongitude(AstroComputer.getMoonGHA());
        double sunLongitude = AstroComputer.ghaToLongitude(AstroComputer.getSunGHA());
        GreatCircle gc = new GreatCircle();
        gc.setStartInDegrees(new GreatCirclePoint(new GeoPoint(AstroComputer.getMoonDecl(), moonLongitude)));
        gc.setArrivalInDegrees(new GreatCirclePoint(new GeoPoint(AstroComputer.getSunDecl(), sunLongitude)));
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format("MoonTilt: Calculating Great Circle from %s/%s to %s/%s",
                    GeomUtil.decToSex(AstroComputer.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(moonLongitude, GeomUtil.SWING, GeomUtil.EW).trim(),
                    GeomUtil.decToSex(AstroComputer.getSunDecl(), GeomUtil.SWING, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(sunLongitude, GeomUtil.SWING, GeomUtil.EW).trim()));
        }
        gc.calculateGreatCircle(20); // 20 points in the GC...
        double finalLat = obsLatitude;
        double finalLng = obsLongitude;
        /*
         * All in one operation.
         * For each point of the Moon-to-Sun Great Circle,
         * calculate how they are seen from the observer's position.
         */
//		Vector<GreatCircleWayPoint> greatCircleWayPoints = GreatCircle.inDegrees(gc.getRoute()); // In Degrees
        List<GreatCircleWayPointWithBodyFromPos> route = GreatCircle.inDegrees(gc.getRoute()).stream()
                .map(rwp -> {
                    GreatCircleWayPointWithBodyFromPos gcwpwbfp = new GreatCircleWayPointWithBodyFromPos(rwp.getPoint(), rwp.getZ());
                    if (rwp.getPoint() != null) {
                        sru.calculate(finalLat, finalLng, AstroComputer.longitudeToGHA(rwp.getPoint().getG()), rwp.getPoint().getL());
                        gcwpwbfp.setWpFromPos(new BodyFromPos()
                                .observer(new Pos()
                                        .latitude(finalLat)
                                        .longitude(finalLng))
                                .observed(new OBS()
                                        .alt(sru.getHe())    // as L
                                        .z(sru.getZ())));    // as G
                    }
                    return gcwpwbfp;
                }).collect(Collectors.toList());

//		System.out.println(String.format("At %d %02d %02d - %02d:%02d:%02d UTC:", year, month, day, hour, minute, second));
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format("From (%.03f/%.03f) to (%.03f/%.03f) => Z: %f",
                    route.get(0).getWpFromPos().observed.z,
                    route.get(0).getWpFromPos().observed.alt,
                    route.get(1).getWpFromPos().observed.z,
                    route.get(1).getWpFromPos().observed.alt,
                    route.get(0).getZ()));
        }

        // Take the first triangle, from the Moon.
        double z0 = route.get(0).getWpFromPos().observed.z;
        double z1 = route.get(1).getWpFromPos().observed.z;

        double alt0 = route.get(0).getWpFromPos().observed.alt;
        double alt1 = route.get(1).getWpFromPos().observed.alt;

        double deltaZ = z1 - z0;
        if (deltaZ > 180) { // like 358 - 2, should be 358 - 362.
            deltaZ -= 360;
        }
        double deltaElev = alt1 - alt0;

        double alpha = Math.toDegrees(Math.atan2(deltaElev, deltaZ)); // atan2 from -Pi to Pi

        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format("At %d %02d %02d - %02d:%02d:%02d UTC:", year, month, day, hour, minute, second));
            System.out.println(String.format("0 - Z: %.03f, El: %.03f", z0, alt0));
            System.out.println(String.format("1 - Z: %.03f, El: %.03f", z1, alt1));
            // Full Moon-Sun path
            System.out.println("Full Path:");
            route.forEach(wp -> {
                System.out.printf(" - Z %.03f, Alt %.03f => to next: %.03f%n", wp.getWpFromPos().observed.z, wp.getWpFromPos().observed.alt, wp.getZ());
            });
            System.out.printf("\u03b4Z: %f, \u03b4El: %f => \u03b1 = %f%n", deltaZ, deltaElev, alpha);
        }

        if (deltaElev > 0) {
            if (deltaZ > 0) { // positive angle, like 52
                alpha *= -1;
            } else { // Angle > 90, like 116
                if (alpha < 90) {
                    alpha -= 90;
                } else {
                    alpha = 180 - alpha;
                }
            }
        } else {
            if (deltaZ > 0) { // negative angle, like -52
                alpha *= -1;
            } else { // Negative, < -90, like -116
                if (alpha > -90) {
                    alpha += 90;
                } else {
                    alpha = -180 - alpha;
                }
            }
        }
//		System.out.println(String.format("Tilt: %f", alpha));
        return alpha;
    }

    /**
     * Uses IRA
     *
     * @param obsLatitude
     * @param obsLongitude
     * @return
     */
    public static synchronized double getMoonTiltV2(double obsLatitude, double obsLongitude) {
        final SightReductionUtil sru = new SightReductionUtil();

//		double moonLongitude = AstroComputer.ghaToLongitude(AstroComputer.getMoonGHA());
//		double sunLongitude = AstroComputer.ghaToLongitude(AstroComputer.getSunGHA());

        sru.calculate(obsLatitude, obsLongitude, AstroComputer.getMoonGHA(), AstroComputer.getMoonDecl());
        double moonZ = sru.getZ();
        double moonAlt = sru.getHe();
        if (moonZ > 180) {
            moonZ -= 360;
        }
        sru.calculate(obsLatitude, obsLongitude, AstroComputer.getSunGHA(), AstroComputer.getSunDecl());
        double sunZ = sru.getZ();
        double sunAlt = sru.getHe();
        if (sunZ > 180) {
            sunZ -= 360;
        }

        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format("V2 - At %d %02d %02d - %02d:%02d:%02d UTC:", year, month, day, hour, minute, second));
            System.out.println(String.format("V2 - Moon Z: %.03f, El: %.03f", moonZ, moonAlt));
            System.out.println(String.format("V2 - Sun  Z: %.03f, El: %.03f", sunZ, sunAlt));
        }
        if (false) {
            double alpha = GreatCircle.getInitialRouteAngle(
                    new GreatCirclePoint(Math.toRadians(moonAlt), Math.toRadians(moonZ)),
                    new GreatCirclePoint(Math.toRadians(sunAlt), Math.toRadians(sunZ)));

//		System.out.println(String.format("ARI: %f", Math.toDegrees(alpha)));
            return Math.toDegrees(alpha);
        } else {
            double alpha = GreatCircle.getInitialRouteAngleInDegrees( // alpha is returned in degrees
                    new GreatCirclePoint(moonAlt, moonZ),
                    new GreatCirclePoint(sunAlt, sunZ));
//		System.out.println(String.format("ARI: %f", alpha));
            return alpha;
        }
    }

    /**
     * @param y  Year, like 2019
     * @param m  Month, [1..12]                   <- !!! Unlike Java's Calendar, which is zero-based
     * @param d  Day of month [1..28, 29, 30, 31]
     * @param h  Hour of the day [0..23]
     * @param mi Minutes [0..59]
     * @param s  Seconds [0..59], no milli-sec.
     */
    public static synchronized void calculate(int y, int m, int d, int h, int mi, int s) {
        setDateTime(y, m, d, h, mi, s);
        calculate();
    }

    private final static String AUTO = "AUTO";
    private final static String AUTO_PREFIX = "AUTO:"; // Used in AUTO:2020-06

    public static synchronized void calculate() {

        if (year == -1 && month == -1 && day == -1 &&
                hour == -1 && minute == -1 && second == -1) {  // Then use current system date
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.println("Using System Time");
            }
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
            setDateTime(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    date.get(Calendar.MINUTE),
                    date.get(Calendar.SECOND));
        }


        // deltaT="AUTO" or "AUTO:2020-06", for other almanac than the current (aka now) one.
        String deltaTStr = System.getProperty("deltaT", String.valueOf(deltaT)); // Default, see above... Careful.
        if (deltaTStr.equals(AUTO)) {
            Calendar now = GregorianCalendar.getInstance();
            deltaT = TimeUtil.getDeltaT(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        } else if (deltaTStr.startsWith(AUTO_PREFIX)) {
            String value = deltaTStr.substring(AUTO_PREFIX.length());
            String[] splitted = value.split("-");
            int intYear = Integer.parseInt(splitted[0]);
            int intMonth = Integer.parseInt(splitted[1]);
            deltaT = TimeUtil.getDeltaT(intYear, intMonth);
        } else if (deltaTStr != null && !deltaTStr.isEmpty()) {
            deltaT = Double.parseDouble(deltaTStr);
        }

//		System.out.println(String.format("Using DeltaT: %f", deltaT));

        Core.julianDate(year, month, day, hour, minute, second, deltaT);
        Anomalies.nutation();
        Anomalies.aberration();

        Core.aries();
        Core.sun();

        Moon.compute();

        Venus.compute();
        Mars.compute();
        Jupiter.compute();
        Saturn.compute();
        Core.polaris();
        moonPhase = Core.moonPhase();
        dow = WEEK_DAYS[Core.weekDay()];
    }

    public final static int UTC_RISE_IDX = 0;
    public final static int UTC_SET_IDX = 1;
    public final static int RISE_Z_IDX = 2;
    public final static int SET_Z_IDX = 3;

    /**
     * Note: The calculate() method must have been invoked before.
     * <p>
     * TODO: Fine tune (by checking the elevation) after this calculation, which is not 100% accurate...
     *
     * @param latitude
     * @return the time of rise and set of the body (Sun in that case).
     * @see http://aa.usno.navy.mil/data/docs/RS_OneYear.php
     * @see http://www.jgiesen.de/SunMoonHorizon/
     * @deprecated Use #sunRiseAndSetEpoch
     */
    public static synchronized double[] sunRiseAndSet(double latitude, double longitude) {
        //  out.println("Sun HP:" + Context.HPsun);
        //  out.println("Sun SD:" + Context.SDsun);
        double h0 = (Context.HPsun / 3_600d) - (Context.SDsun / 3_600d); // - (34d / 60d);
//  System.out.println(">>> DEBUG >>> H0:" + h0 + ", Sin Sun H0:" + Math.sin(Math.toRadians(h0)));
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun)));
        double t = Math.acos(cost);
        double lon = longitude;

//  while (lon < -180D)
//    lon += 360D;
        //  out.println("Lon:" + lon + ", Eot:" + Context.EoT + " (" + (Context.EoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
        double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        // Based on http://en.wikipedia.org/wiki/Sunrise_equation
        //double phi = Math.toRadians(latitude);
        //double delta = Math.toRadians(Context.DECsun);
        //double omega = Math.acos(- Math.tan(phi) * Math.tan(delta));
        //utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(omega) / 15D);
        //utSet  = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(omega) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        return new double[]{utRise, utSet, Z, 360d - Z};
    }

    public static class EpochAndZ {
        private long epoch;
        private double z;

        public long getEpoch() {
            return epoch;
        }

        public double getZ() {
            return z;
        }

        public EpochAndZ epoch(long epoch) {
            this.epoch = epoch;
            return this;
        }

        public EpochAndZ z(double z) {
            this.z = z;
            return this;
        }
    }

    private static double[] testSun(Calendar current, double lat, double lng) {
        AstroComputer.setDateTime(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH) + 1,
                current.get(Calendar.DATE),
                current.get(Calendar.HOUR_OF_DAY),
                current.get(Calendar.MINUTE),
                current.get(Calendar.SECOND));
        AstroComputer.calculate();
        SightReductionUtil sru = new SightReductionUtil(
                AstroComputer.getSunGHA(),
                AstroComputer.getSunDecl(),
                lat,
                lng);
        sru.calculate();
        double he = sru.getHe().doubleValue();
        double z = sru.getZ().doubleValue();
        return new double[]{he, z};
    }

    public static synchronized EpochAndZ[] sunRiseAndSetEpoch(double latitude, double longitude) {

        double h0 = (Context.HPsun / 3_600d) - (Context.SDsun / 3_600d); // - (34d / 60d);
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun)));
        double t = Math.acos(cost);
        double lon = longitude;

        double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        double zRise = Z;
        double zSet = (360D - Z);

//		return new double[]{utRise, utSet, Z, 360d - Z};
        Calendar rise = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        Calendar set = (Calendar) rise.clone();

        TimeUtil.DMS dms = TimeUtil.decimalToDMS(utRise);

        rise.set(Calendar.YEAR, year);
        rise.set(Calendar.MONTH, month - 1);
        rise.set(Calendar.DAY_OF_MONTH, day);

        rise.set(Calendar.HOUR_OF_DAY, dms.getHours());
        rise.set(Calendar.MINUTE, dms.getMinutes());
        rise.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));

//		System.out.println("Rise:" + new Date(rise.getTimeInMillis()));

        // Fine tuning
        double[] riseTest = testSun(rise, latitude, longitude);
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format(">>>> 1st estimation: H rise (%s): %02f", new Date(rise.getTimeInMillis()).toString(), riseTest[0]));
        }
        if (riseTest[0] != 0) { // Elevation not 0, then adjust
            while (riseTest[0] > 0) {
                rise.add(Calendar.SECOND, -10);
                riseTest = testSun(rise, latitude, longitude);
            }
            // Starting tuning
            while (riseTest[0] < 0) {
                rise.add(Calendar.SECOND, 1);
                riseTest = testSun(rise, latitude, longitude);
            }
            zRise = riseTest[1];
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.println(String.format(">> Tuned: Rising at %s, h:%f, z=%02f\272", new Date(rise.getTimeInMillis()).toString(), riseTest[0], riseTest[1]));
            }
        }
        long epochRise = rise.getTimeInMillis();

        dms = TimeUtil.decimalToDMS(utSet);

        set.set(Calendar.YEAR, year);
        set.set(Calendar.MONTH, month - 1);
        set.set(Calendar.DAY_OF_MONTH, day);

        set.set(Calendar.HOUR_OF_DAY, dms.getHours());
        set.set(Calendar.MINUTE, dms.getMinutes());
        set.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));
        // Fine tuning
        riseTest = testSun(set, latitude, longitude);
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println(String.format(">>>> 1st estimation: H set (%s): %02f", new Date(set.getTimeInMillis()).toString(), riseTest[0]));
        }
        if (riseTest[0] != 0) { // Elevation not 0, then adjust
            while (riseTest[0] < 0) {
                set.add(Calendar.SECOND, -10);
                riseTest = testSun(set, latitude, longitude);
            }
            // Starting tuning
            while (riseTest[0] > 0) {
                set.add(Calendar.SECOND, 1);
                riseTest = testSun(set, latitude, longitude);
            }
            zSet = riseTest[1];
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.println(String.format(">> Tuned: Setting at %s, h:%f, z=%02f\272", new Date(set.getTimeInMillis()).toString(), riseTest[0], riseTest[1]));
            }
        }
        long epochSet = set.getTimeInMillis();

        EpochAndZ[] result = {
                new EpochAndZ().epoch(epochRise).z(zRise),
                new EpochAndZ().epoch(epochSet).z(zSet)
        };

        return result;
    }

    /**
     * @param latitude  in degrees
     * @param longitude in degrees
     * @return meridian passage time in hours.
     */
    public static double getSunMeridianPassageTime(double latitude, double longitude) {
        double t = (12d - (Context.EoT / 60d));
        double deltaG = longitude / 15D;
        return t - deltaG;
    }

    /**
     * @param latitude
     * @param longitude
     * @return as an epoch (today based)
     */
    public static long getSunTransitTime(double latitude, double longitude) {
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        double inHours = getSunMeridianPassageTime(latitude, longitude);
        TimeUtil.DMS dms = TimeUtil.decimalToDMS(inHours);
        cal.set(Calendar.HOUR_OF_DAY, dms.getHours());
        cal.set(Calendar.MINUTE, dms.getMinutes());
        cal.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));

        return cal.getTimeInMillis();
    }

    public static synchronized double[] sunRiseAndSet_wikipedia(double latitude, double longitude) {
        double cost = Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun));
        double t = Math.acos(cost);
        double lon = longitude;
        double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        return new double[]{utRise, utSet, Z, 360d - Z};
    }

    /**
     * See http://aa.usno.navy.mil/data/docs/RS_OneYear.php
     * <br>
     * See http://www.jgiesen.de/SunMoonHorizon/
     */
    public static synchronized double[] moonRiseAndSet(double latitude, double longitude) {
        //  out.println("Moon HP:" + (Context.HPmoon / 60) + "'");
        //  out.println("Moon SD:" + (Context.SDmoon / 60) + "'");
        double h0 = (Context.HPmoon / 3_600d) - (Context.SDmoon / 3_600d) - (34d / 60d);
        //  out.println("Moon H0:" + h0);
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECmoon)));
        double t = Math.acos(cost);
        double lon = longitude;
        while (lon < -180D) {
            lon += 360D;
        }
        //  out.println("Moon Eot:" + Context.moonEoT + " (" + (Context.moonEoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
        double utRise = 12D - (Context.moonEoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        while (utRise < 0) {
            utRise += 24;
        }
        while (utRise > 24) {
            utRise -= 24;
        }
        double utSet = 12D - (Context.moonEoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);
        while (utSet < 0) {
            utSet += 24;
        }
        while (utSet > 24) {
            utSet -= 24;
        }

        return new double[]{utRise, utSet};
    }

    public static synchronized double getMoonIllum() {
        return Context.k_moon;
    }

    public static synchronized void setDeltaT(double deltaT) {
        System.out.println("...DeltaT set to " + deltaT);
        AstroComputer.deltaT = deltaT;
    }

    public static final synchronized double getTimeZoneOffsetInHours(TimeZone tz) {
        return getTimeZoneOffsetInHours(tz, new Date());
    }

    public static final synchronized double getTimeZoneOffsetInHours(TimeZone tz, Date when) {
        double d = 0;
        if (false) {
            SimpleDateFormat sdf = new SimpleDateFormat("Z");
            sdf.setTimeZone(tz);
            String s = sdf.format(new Date());
            if (s.startsWith("+")) {
                s = s.substring(1);
            }
            int i = Integer.parseInt(s);
            d = (int) (i / 100);
            int m = (int) (i % 100);
            d += (m / 60d);
        } else {
            d = (tz.getOffset(when.getTime()) / (3_600_000d));
        }
        return d;
    }

    public static final synchronized double getTimeOffsetInHours(String timeOffset) {
//  System.out.println("Managing:" + timeOffset);
        double d = 0d;
        String[] hm = timeOffset.split(":");
        int h = Integer.parseInt(hm[0]);
        int m = Integer.parseInt(hm[1]);
        if (h > 0) {
            d = h + (m / 60d);
        }
        if (h < 0) {
            d = h - (m / 60d);
        }
        return d;
    }

    public final static int SUN_ALT_IDX = 0;
    public final static int SUN_Z_IDX = 1;
    public final static int MOON_ALT_IDX = 2;
    public final static int MOON_Z_IDX = 3;
    public final static int LHA_ARIES_IDX = 4;

    public static synchronized double[] getSunMoon(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double[] values = new double[5];
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;

        calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(Context.GHAsun);
        sru.setD(Context.DECsun);
        sru.calculate();
        values[SUN_ALT_IDX] = sru.getHe();
        values[SUN_Z_IDX] = sru.getZ();
        // Moon
        sru.setAHG(Context.GHAmoon);
        sru.setD(Context.DECmoon);
        sru.calculate();
        values[MOON_ALT_IDX] = sru.getHe();
        values[MOON_Z_IDX] = sru.getZ();

        double ahl = Context.GHAAtrue + lng;
        while (ahl < 0.0) {
            ahl += 360.0;
        }
        while (ahl > 360.0) {
            ahl -= 360.0;
        }
        values[LHA_ARIES_IDX] = ahl;

        return values;
    }

    public static synchronized double getSunAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double value = 0d;
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;

        calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(Context.GHAsun);
        sru.setD(Context.DECsun);
        sru.calculate();
        value = sru.getHe();

        return value;
    }

    public static synchronized double getMoonAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double value = 0d;
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;

        calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Moon
        sru.setAHG(Context.GHAmoon);
        sru.setD(Context.DECmoon);
        sru.calculate();
        value = sru.getHe();

        return value;
    }

    public final static int HE_SUN_IDX = 0;
    public final static int HE_MOON_IDX = 1;
    public final static int DEC_SUN_IDX = 2;
    public final static int DEC_MOON_IDX = 3;
    public final static int MOON_PHASE_IDX = 4;

    /**
     * Returns Altitude and Declination, for Sun and Moon,
     * for a given UTC time, at a given location.
     *
     * @param y   year
     * @param m   month (like Calendar.MONTH + 1, Jan is 1)
     * @param d   day of the month
     * @param h   hour of the day
     * @param mi  minutes
     * @param s   seconds
     * @param lat latitude
     * @param lng longitude
     * @return an array of 4 doubles. See HE_SUN_IDX, HE_MOON_IDX, DEC_SUN_IDX and DEC_MOON_IDX.
     * <p>
     * TODO Make it a Map<String, Double>
     */
    public static synchronized double[] getSunMoonAltDecl(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double[] values = new double[5];
        year = y;
        month = m;
        day = d;
        hour = h;
        minute = mi;
        second = s;

//  System.out.println(y + "-" + month + "-" + day + " " + h + ":" + mi + ":" + s);

        calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(Context.GHAsun);
        sru.setD(Context.DECsun);
        sru.calculate();
        values[HE_SUN_IDX] = sru.getHe();
        // Moon
        sru.setAHG(Context.GHAmoon);
        sru.setD(Context.DECmoon);
        sru.calculate();
        values[HE_MOON_IDX] = sru.getHe();

        values[DEC_SUN_IDX] = Context.DECsun;
        values[DEC_MOON_IDX] = Context.DECmoon;

        double moonPhase = getMoonPhase(y, m, d, h, m, s);
        values[MOON_PHASE_IDX] = moonPhase;

        return values;
    }

    /**
     * Warning: Context must have been initialized!
     *
     * @return
     */
    public static synchronized double getSunDecl() {
        return Context.DECsun;
    }

    public static synchronized double getSunGHA() {
        return Context.GHAsun;
    }

    public static synchronized double getSunRA() {
        return Context.RAsun;
    }

    public static synchronized double getSunSd() {
        return Context.SDsun;
    }

    public static synchronized double getSunHp() {
        return Context.HPsun;
    }

    public static synchronized double getAriesGHA() {
        return Context.GHAAtrue;
    }

    public static synchronized double getMoonDecl() {
        return Context.DECmoon;
    }

    public static synchronized double getMoonGHA() {
        return Context.GHAmoon;
    }

    public static synchronized double getMoonRA() {
        return Context.RAmoon;
    }

    public static synchronized double getMoonSd() {
        return Context.SDmoon;
    }

    public static synchronized double getMoonHp() {
        return Context.HPmoon;
    }

    public static synchronized double getVenusDecl() {
        return Context.DECvenus;
    }

    public static synchronized double getVenusGHA() {
        return Context.GHAvenus;
    }

    public static synchronized double getVenusRA() {
        return Context.RAvenus;
    }

    public static synchronized double getVenusSd() {
        return Context.SDvenus;
    }

    public static synchronized double getVenusHp() {
        return Context.HPvenus;
    }

    public static synchronized double getMarsDecl() {
        return Context.DECmars;
    }

    public static synchronized double getMarsGHA() {
        return Context.GHAmars;
    }

    public static synchronized double getMarsRA() {
        return Context.RAmars;
    }

    public static synchronized double getMarsSd() {
        return Context.SDmars;
    }

    public static synchronized double getMarsHp() {
        return Context.HPmars;
    }

    public static synchronized double getJupiterDecl() {
        return Context.DECjupiter;
    }

    public static synchronized double getJupiterGHA() {
        return Context.GHAjupiter;
    }

    public static synchronized double getJupiterRA() {
        return Context.RAjupiter;
    }

    public static synchronized double getJupiterSd() {
        return Context.SDjupiter;
    }

    public static synchronized double getJupiterHp() {
        return Context.HPjupiter;
    }

    public static synchronized double getSaturnDecl() {
        return Context.DECsaturn;
    }

    public static synchronized double getSaturnGHA() {
        return Context.GHAsaturn;
    }

    public static synchronized double getSaturnRA() {
        return Context.RAsaturn;
    }

    public static synchronized double getSaturnSd() {
        return Context.SDsaturn;
    }

    public static synchronized double getSaturnHp() {
        return Context.HPsaturn;
    }

    public static synchronized double getPolarisDecl() {
        return Context.DECpol;
    }

    public static synchronized double getPolarisGHA() {
        return Context.GHApol;
    }

    public static synchronized double getPolarisRA() {
        return Context.RApol;
    }

    public static synchronized double getEoT() {
        return Context.EoT;
    }

    public static synchronized double getLDist() {
        return Context.LDist;
    }

    public static synchronized String getWeekDay() {
        return dow;
    }

    public static synchronized String getMoonPhaseStr() {
        return moonPhase;
    }

    // Etc. Whatever is needed

    public static synchronized double getMeanObliquityOfEcliptic() {
        return Context.eps0;
    }

    public static synchronized double ghaToLongitude(double gha) {
        double longitude = 0;
        if (gha < 180) {
            longitude = -gha;
        }
        if (gha >= 180) {
            longitude = 360 - gha;
        }
        return longitude;
    }

    public static synchronized double longitudeToGHA(double longitude) {
        double gha = 0;
        if (longitude < 0) { // W
            gha = -longitude;
        }
        if (longitude > 0) { // E
            gha = 360 - longitude;
        }
        return gha;
    }

    public static synchronized Map<String, Object> getAllCalculatedData() {
        Map<String, Object> fullMap = new HashMap<>();

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("year", year);
        contextMap.put("month", month);
        contextMap.put("day", day);
        contextMap.put("hour", hour);
        contextMap.put("minute", minute);
        contextMap.put("second", second);
        contextMap.put("delta-t", deltaT);

        Map<String, Object> sunMap = new HashMap<>();
        sunMap.put("dec", Context.DECsun);
        sunMap.put("gha", Context.GHAsun);
        sunMap.put("ra", Context.RAsun);
        sunMap.put("sd", Context.SDsun);
        sunMap.put("hp", Context.HPsun);

        Map<String, Object> moonMap = new HashMap<>();
        moonMap.put("dec", Context.DECmoon);
        moonMap.put("gha", Context.GHAmoon);
        moonMap.put("ra", Context.RAmoon);
        moonMap.put("sd", Context.SDmoon);
        moonMap.put("hp", Context.HPmoon);

        Map<String, Object> venusMap = new HashMap<>();
        venusMap.put("dec", Context.DECvenus);
        venusMap.put("gha", Context.GHAvenus);
        venusMap.put("ra", Context.RAvenus);
        venusMap.put("sd", Context.SDvenus);
        venusMap.put("hp", Context.HPvenus);

        Map<String, Object> marsMap = new HashMap<>();
        marsMap.put("dec", Context.DECmars);
        marsMap.put("gha", Context.GHAmars);
        marsMap.put("ra", Context.RAmars);
        marsMap.put("sd", Context.SDmars);
        marsMap.put("hp", Context.HPmars);

        Map<String, Object> jupiterMap = new HashMap<>();
        jupiterMap.put("dec", Context.DECjupiter);
        jupiterMap.put("gha", Context.GHAjupiter);
        jupiterMap.put("ra", Context.RAjupiter);
        jupiterMap.put("sd", Context.SDjupiter);
        jupiterMap.put("hp", Context.HPjupiter);

        Map<String, Object> saturnMap = new HashMap<>();
        saturnMap.put("dec", Context.DECsaturn);
        saturnMap.put("gha", Context.GHAsaturn);
        saturnMap.put("ra", Context.RAsaturn);
        saturnMap.put("sd", Context.SDsaturn);
        saturnMap.put("hp", Context.HPsaturn);

        Map<String, Object> polarisMap = new HashMap<>();
        polarisMap.put("dec", Context.DECpol);
        polarisMap.put("gha", Context.GHApol);
        polarisMap.put("ra", Context.RApol);

        fullMap.put("context", contextMap);

        fullMap.put("sun", sunMap);
        fullMap.put("moon", moonMap);
        fullMap.put("venus", venusMap);
        fullMap.put("mars", marsMap);
        fullMap.put("jupiter", jupiterMap);
        fullMap.put("saturn", saturnMap);
        fullMap.put("polaris", polarisMap);

        fullMap.put("aries-gha", Context.GHAAtrue);

        fullMap.put("eot", Context.EoT);
        fullMap.put("lunar-dist", Context.LDist);
        fullMap.put("day-of-week", dow);
        fullMap.put("moon-phase", Context.moonPhase);
        fullMap.put("mean-obliquity-of-ecliptic", Context.eps0);

        // More if needed!

        return fullMap;
    }

    // This is for tests
    public static void main(String... args) {

        SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
//		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

        System.setProperty("deltaT", "AUTO");

        System.out.println(String.format("Moon phase for date %d-%d-%d %d:%d:%d: ", 2011, 8, 22, 12, 00, 00) + getMoonPhase(2011, 8, 22, 12, 00, 00));
        System.out.println("TimeOffset:" + getTimeOffsetInHours("-09:30"));
        String[] tz = new String[]{"Pacific/Marquesas", "America/Los_Angeles", "GMT", "Europe/Paris", "Europe/Moscow", "Australia/Sydney", "Australia/Adelaide"};
        for (int i = 0; i < tz.length; i++) {
            System.out.println("TimeOffset for " + tz[i] + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz[i])));
        }
        System.out.println("TZ:" + TimeZone.getTimeZone(tz[0]).getDisplayName() + ", " + (TimeZone.getTimeZone(tz[0]).getOffset(new Date().getTime()) / (3_600_000d)));

        String timeZone = "America/Los_Angeles";
        Calendar cal = GregorianCalendar.getInstance();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        double d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.getTime();
        System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
        d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        AstroComputer.calculate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));

        // SF Home
        double lat = 37.7489;
        double lng = -122.5070;

        System.out.println(String.format("\nFrom position %s / %s", GeomUtil.decToSex(lat, GeomUtil.SWING, GeomUtil.NS), GeomUtil.decToSex(lng, GeomUtil.SWING, GeomUtil.EW)));

        double sunMeridianPassageTime = getSunMeridianPassageTime(lat, lng);
        System.out.println(String.format("Sun EoT: %f", sunMeridianPassageTime));

        long sunTransit = getSunTransitTime(lat, lng);
        Date tt = new Date(sunTransit);
        System.out.println("Transit Time:" + tt.toString());

        double[] riseAndSet = sunRiseAndSet(lat, lng);
        System.out.println(String.format("Time Rise: %f, Time Set: %f, ZRise: %f, ZSet: %f", riseAndSet[0], riseAndSet[1], riseAndSet[2], riseAndSet[3]));

        System.out.println(String.format("Moon Phase (no specific date, current one) : %f", AstroComputer.getMoonPhase()));

        System.out.println(String.format("Sun data:\nDeclination: %s\nGHA: %s",
                GeomUtil.decToSex(getSunDecl(), GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(getSunGHA(), GeomUtil.SWING, GeomUtil.NONE)));

        SightReductionUtil sru = new SightReductionUtil();

        sru.setL(lat);
        sru.setG(lng);

        sru.setAHG(getSunGHA());
        sru.setD(getSunDecl());
        sru.calculate();
        double obsAlt = sru.getHe();
        double z = sru.getZ();

        System.out.println(String.format("Elev.: %s, Z: %.02f\272", GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), z));

        EpochAndZ[] epochAndZs = sunRiseAndSetEpoch(lat, lng);

        System.out.println("\nWith epochs");
        System.out.println(String.format("Rise Date: %s (Z:%.02f\272)\nSet Date: %s (Z:%.02f\272)",
                new Date(epochAndZs[0].getEpoch()).toString(),
                epochAndZs[0].getZ(),
                new Date(epochAndZs[1].getEpoch()).toString(),
                epochAndZs[1].getZ()));

        // Moon tilt
        Calendar date2 = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        System.out.println(String.format("Setting Cal Date to %d-%02d-%02d %02d:%02d:%02d",
                date2.get(Calendar.YEAR),
                date2.get(Calendar.MONTH) + 1,
                date2.get(Calendar.DAY_OF_MONTH),
                date2.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date2.get(Calendar.MINUTE),
                date2.get(Calendar.SECOND)));
        AstroComputer.setDateTime(date2.get(Calendar.YEAR),
                date2.get(Calendar.MONTH) + 1,
                date2.get(Calendar.DAY_OF_MONTH),
                date2.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date2.get(Calendar.MINUTE),
                date2.get(Calendar.SECOND));

        AstroComputer.calculate();
        double moonTilt = AstroComputer.getMoonTilt(lat, lng);
        Calendar calculationDateTime = getCalculationDateTime();
        System.out.println(String.format("At %s, Moon Tilt: %.03f", SDF_UTC.format(calculationDateTime.getTime()), moonTilt));
    }
}
