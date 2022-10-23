package calc.calculation;

import calc.*;
//import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanacV2.*;
import calc.calculation.nauticalalmanac.Star;
import calc.calculation.nauticalalmanac.Utils;
import utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Non-Static utilities
 * <br/>
 * Use -Dastro.verbose=true for more output.
 * <br/>
 * Provide deltaT as a System variable to enforce it: -DdeltaT=68.9677 ,
 * will be calculated otherwise.
 * <p/>
 * @see TimeUtil#getDeltaT(int, int)
 * @see <a href="http://aa.usno.navy.mil/data/docs/celnavtable.php">http://aa.usno.navy.mil/data/docs/celnavtable.php</a>
 * @see <a href="http://maia.usno.navy.mil/">http://maia.usno.navy.mil/</a>
 * @see <a href="http://maia.usno.navy.mil/ser7/deltat.data">http://maia.usno.navy.mil/ser7/deltat.data</a>
 * @see <a href="https://www.usno.navy.mil/USNO/earth-orientation/eo-products/long-term">https://www.usno.navy.mil/USNO/earth-orientation/eo-products/long-term</a>
 *
 */
public class AstroComputerV2 {

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

    private int year = -1, month = -1, day = -1, hour = -1, minute = -1, second = -1;
    private double deltaT = 66.4749d; // 2011. Overridden by deltaT system variable, or calculated on the fly.

    private final static String[] WEEK_DAYS = {
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };
    private String dow = "";
    private String moonPhase = "";

    private final ContextV2 context = new ContextV2();
    private boolean calculateHasBeenInvoked = false;

    public AstroComputerV2() {
        this.calculateHasBeenInvoked = false;
        this.context.starName = null;
    }

    // Updated after the calculate invocation.
    public synchronized double getDeltaT() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.deltaT;
    }

    public synchronized void setDateTime(int y, int m, int d, int h, int mi, int s) {
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;
        this.calculateHasBeenInvoked = false;
        this.context.starName = null;
    }

    public synchronized void setDateTime(long epoch) {
        Calendar calcDate = GregorianCalendar.getInstance();
        calcDate.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        calcDate.setTimeInMillis(epoch);
        this.year = calcDate.get(Calendar.YEAR);
        this.month = calcDate.get(Calendar.MONTH) + 1;
        this.day = calcDate.get(Calendar.DAY_OF_MONTH);
        this.hour = calcDate.get(Calendar.HOUR_OF_DAY);
        this.minute = calcDate.get(Calendar.MINUTE);
        this.second = calcDate.get(Calendar.SECOND);
        this.calculateHasBeenInvoked = false;
        this.context.starName = null;
    }

    public synchronized Calendar getCalculationDateTime() {
        Calendar calcDate = GregorianCalendar.getInstance();
        calcDate.set(Calendar.YEAR, this.year);
        calcDate.set(Calendar.MONTH, this.month - 1);
        calcDate.set(Calendar.DAY_OF_MONTH, this.day);

        calcDate.set(Calendar.HOUR_OF_DAY, this.hour);
        calcDate.set(Calendar.MINUTE, this.minute);
        calcDate.set(Calendar.SECOND, this.second);

        return calcDate;
    }

    /**
     * Time are UTC.
     * <br/>
     * This method can be invoked without having invoked the {@link AstroComputerV2#calculate()} before.
     *
     * @param y  year
     * @param m  Month. Attention: Jan=1, Dec=12 !!!! Does NOT start with 0.
     * @param d  day
     * @param h  hour
     * @param mi minute
     * @param s  second
     * @return Phase in Degrees
     */
    public synchronized double getMoonPhase(int y, int m, int d, int h, int mi, int s) {
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;

        this.calculate();
        double phase = this.context.lambdaMapp - this.context.lambda_sun;
        while (phase < 0d) {
            phase += 360d;
        }
        return phase;
    }

    /**
     * Assume that calculate has been invoked already
     *
     * @return moon phase in degrees [0..360]
     */
    public synchronized double getMoonPhase() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        double phase = this.context.lambdaMapp - this.context.lambda_sun;
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
    public synchronized double getMoonTilt(double obsLatitude, double obsLongitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        final SightReductionUtil sru = new SightReductionUtil();

        double moonLongitude = AstroComputerV2.ghaToLongitude(this.getMoonGHA());
        double sunLongitude = AstroComputerV2.ghaToLongitude(this.getSunGHA());
        GreatCircle gc = new GreatCircle();
        gc.setStartInDegrees(new GreatCirclePoint(new GeoPoint(this.getMoonDecl(), moonLongitude)));
        gc.setArrivalInDegrees(new GreatCirclePoint(new GeoPoint(this.getSunDecl(), sunLongitude)));
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.printf("MoonTilt: Calculating Great Circle from %s/%s to %s/%s\n",
                    GeomUtil.decToSex(this.getMoonDecl(), GeomUtil.SWING, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(moonLongitude, GeomUtil.SWING, GeomUtil.EW).trim(),
                    GeomUtil.decToSex(this.getSunDecl(), GeomUtil.SWING, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(sunLongitude, GeomUtil.SWING, GeomUtil.EW).trim());
        }
        double distanceInDegrees = gc.getDistanceInDegrees();
        int nbSteps = (distanceInDegrees < 10) ? 20 : (int)(Math.round(2 * distanceInDegrees));
        gc.calculateGreatCircle(nbSteps);
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
                        sru.calculate(finalLat, finalLng, AstroComputerV2.longitudeToGHA(rwp.getPoint().getG()), rwp.getPoint().getL());
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
            System.out.printf("From (%.03f/%.03f) to (%.03f/%.03f) => Z: %f\n",
                    route.get(0).getWpFromPos().observed.z,
                    route.get(0).getWpFromPos().observed.alt,
                    route.get(1).getWpFromPos().observed.z,
                    route.get(1).getWpFromPos().observed.alt,
                    route.get(0).getZ());
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
            System.out.printf("At %d %02d %02d - %02d:%02d:%02d UTC:\n", this.year, this.month, this.day, this.hour, this.minute, this.second);
            System.out.printf("0 - Z: %.03f, El: %.03f\n", z0, alt0);
            System.out.printf("1 - Z: %.03f, El: %.03f\n", z1, alt1);
            // Full Moon-Sun path
            System.out.println("Full Path:");
            route.forEach(wp -> System.out.printf(" - Z %.03f, Alt %.03f => to next: %.03f%n", wp.getWpFromPos().observed.z, wp.getWpFromPos().observed.alt, wp.getZ()));
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
     * Uses IRA. To prefer when finished.
     *
     * @param obsLatitude Observer's Latitude
     * @param obsLongitude Observer's Longitude
     * @return The moon tilt
     */
    public synchronized double getMoonTiltV2(double obsLatitude, double obsLongitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        final SightReductionUtil sru = new SightReductionUtil();

//		double moonLongitude = AstroComputer.ghaToLongitude(AstroComputer.getMoonGHA());
//		double sunLongitude = AstroComputer.ghaToLongitude(AstroComputer.getSunGHA());

        sru.calculate(obsLatitude, obsLongitude, this.getMoonGHA(), this.getMoonDecl());
        double moonZ = sru.getZ();
        double moonAlt = sru.getHe();
        if (moonZ > 180) {
            moonZ -= 360;
        }
        sru.calculate(obsLatitude, obsLongitude, this.getSunGHA(), this.getSunDecl());
        double sunZ = sru.getZ();
        double sunAlt = sru.getHe();
        if (sunZ > 180) {
            sunZ -= 360;
        }

        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.printf("V2 - At %d %02d %02d - %02d:%02d:%02d UTC:\n", this.year, this.month, this.day, this.hour, this.minute, this.second);
            System.out.printf("V2 - Moon Z: %.03f, El: %.03f\n", moonZ, moonAlt);
            System.out.printf("V2 - Sun  Z: %.03f, El: %.03f\n", sunZ, sunAlt);
        }
        if (false) { // Radians
            double alpha = GreatCircle.getInitialRouteAngle(
                    new GreatCirclePoint(Math.toRadians(moonAlt), Math.toRadians(moonZ)),
                    new GreatCirclePoint(Math.toRadians(sunAlt), Math.toRadians(sunZ)));

//		System.out.println(String.format("ARI: %f", Math.toDegrees(alpha)));
            return Math.toDegrees(alpha);
        } else {    // Degrees
            double alpha = GreatCircle.getInitialRouteAngleInDegrees( // alpha is returned in degrees
                    new GreatCirclePoint(moonAlt, moonZ),
                    new GreatCirclePoint(sunAlt, sunZ));
//		System.out.println(String.format("ARI: %f", alpha));
            return alpha;
        }
    }

    public synchronized void calculate(int y, int m, int d, int h, int mi, int s) {
        this.calculate(y, m, d, h, mi, s, false);
    }
    /**
     * @param y  Year, like 2019
     * @param m  Month, [1..12]                   <- !!! Unlike Java's Calendar, which is zero-based
     * @param d  Day of month [1..28, 29, 30, 31]
     * @param h  Hour of the day [0..23]
     * @param mi Minutes [0..59]
     * @param s  Seconds [0..59], no milli-sec.
     */
    public synchronized void calculate(int y, int m, int d, int h, int mi, int s, boolean reCalcDeltaT) {
        this.setDateTime(y, m, d, h, mi, s);
        this.calculate(reCalcDeltaT);
    }

    private final static String AUTO = "AUTO";
    private final static String AUTO_PREFIX = "AUTO:"; // Used in AUTO:2020-06, like -DdeltaT=AUTO:2020-06

    public synchronized void calculate() {
        calculate(false);
    }

    public synchronized void calculate(boolean recalculateDeltaTforCalcDate) {

        if (this.year == -1 && this.month == -1 && this.day == -1 &&
                this.hour == -1 && this.minute == -1 && this.second == -1) {  // Then use current system date
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.println("Using System Time");
            }
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
            this.setDateTime(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    date.get(Calendar.MINUTE),
                    date.get(Calendar.SECOND));
        }

        // deltaT="AUTO" or "AUTO:2020-06", for other almanac than the current (aka now) one.
        if (!recalculateDeltaTforCalcDate) {
            String deltaTStr = System.getProperty("deltaT", String.valueOf(deltaT)); // Default, see above... Careful.
            if (deltaTStr.equals(AUTO) || deltaTStr == null || deltaTStr.trim().isEmpty()) {
                Calendar now = GregorianCalendar.getInstance(); // Current time.
                deltaT = TimeUtil.getDeltaT(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
            } else if (deltaTStr.startsWith(AUTO_PREFIX)) {     // AUTO at a given time (month-year)
                String value = deltaTStr.substring(AUTO_PREFIX.length());
                String[] splitted = value.split("-");
                int intYear = Integer.parseInt(splitted[0]);
                int intMonth = Integer.parseInt(splitted[1]);
                deltaT = TimeUtil.getDeltaT(intYear, intMonth);
            } else if (deltaTStr != null && !deltaTStr.isEmpty()) {
                deltaT = Double.parseDouble(deltaTStr);         // Provided by the user
            }
        } else {
            Calendar calculationDateTime = this.getCalculationDateTime();
            deltaT = TimeUtil.getDeltaT(calculationDateTime.get(Calendar.YEAR), calculationDateTime.get(Calendar.MONTH) + 1);
        }

//		System.out.println(String.format("Using DeltaT: %f", deltaT));

        Core.julianDate(this.context, this.year, this.month, this.day, this.hour, this.minute, this.second, deltaT);
        Anomalies.nutation(this.context);
        Anomalies.aberration(this.context);

        Core.aries(this.context);
        Core.sun(this.context);

        Moon.compute(this.context);

        Venus.compute(this.context);
        Mars.compute(this.context);
        Jupiter.compute(this.context);
        Saturn.compute(this.context);
        Core.polaris(this.context);
        this.moonPhase = Core.moonPhase(this.context);
        this.dow = WEEK_DAYS[Core.weekDay(this.context)];

        this.calculateHasBeenInvoked = true;
    }

    /**
     *
     * @param starName Name of the star (as in the CATALOG)
     */
    public synchronized void starPos(String starName) { // }, ContextV2 context) {
        assert (this.context != null);
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        this.context.starName = starName;
        //Read catalog
        Star star = Star.getStar(starName);
        if (star != null) {
            //Read star in catalog
            double RAstar0 = 15d * star.getRa();
            double DECstar0 = star.getDec();
            double dRAstar = 15d * star.getDeltaRa() / 3_600d;
            double dDECstar = star.getDeltaDec() / 3_600d;
            double par = star.getPar() / 3_600d;

            //Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
            double RAstar1 = RAstar0 + this.context.TE * dRAstar;
            double DECstar1 = DECstar0 + this.context.TE * dDECstar;

            //Mean obliquity of ecliptic at 2000.0 in degrees
//    double eps0_2000 = 23.439291111;

            //Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
            double lambdastar1 = Math.atan2((Utils.sind(RAstar1) * Utils.cosd(this.context.EPS0_2000) + Utils.tand(DECstar1) * Utils.sind(this.context.EPS0_2000)), Utils.cosd(RAstar1));
            double betastar1 = Math.asin(Utils.sind(DECstar1) * Utils.cosd(this.context.EPS0_2000) - Utils.cosd(DECstar1) * Utils.sind(this.context.EPS0_2000) * Utils.sind(RAstar1));

            //Precession
            double eta = Math.toRadians(47.0029 * this.context.TE - 0.03302 * this.context.TE2 + 0.00006 * this.context.TE3) / 3_600d;
            double PI0 = Math.toRadians(174.876384 - (869.8089 * this.context.TE + 0.03536 * this.context.TE2) / 3_600d);
            double p0 = Math.toRadians(5_029.0966 * this.context.TE + 1.11113 * this.context.TE2 - 0.0000006 * this.context.TE3) / 3_600d;
            double A1 = Math.cos(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1) - Math.sin(eta) * Math.sin(betastar1);
            double B1 = Math.cos(betastar1) * Math.cos(PI0 - lambdastar1);
            double C1 = Math.cos(eta) * Math.sin(betastar1) + Math.sin(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1);
            double lambdastar2 = p0 + PI0 - Math.atan2(A1, B1);
            double betastar2 = Math.asin(C1);

            //Annual parallax
            double par_lambda = Math.toRadians(par * Math.sin(Math.toRadians(this.context.Lsun_true) - lambdastar2) / Math.cos(betastar2));
            double par_beta = -Math.toRadians(par * Math.sin(betastar2) * Math.cos(Math.toRadians(this.context.Lsun_true) - lambdastar2));

            lambdastar2 += par_lambda;
            betastar2 += par_beta;

            // Nutation in longitude
            lambdastar2 += Math.toRadians(this.context.delta_psi);

            // Aberration
//    double kappa = Math.toRadians(20.49552) / 3_600d;
//    double pi0 = Math.toRadians(102.93735 + 1.71953 *this.context.TE + 0.00046 * Context.TE2);
//    double e = 0.016708617 - 0.000042037 * Context.TE - 0.0000001236 * Context.TE2;

            double dlambdastar = (this.context.e * this.context.kappa * Math.cos(this.context.pi0 - lambdastar2) - this.context.kappa * Math.cos(Math.toRadians(this.context.Lsun_true) - lambdastar2)) / Math.cos(betastar2);
            double dbetastar = -context.kappa * Math.sin(betastar2) * (Math.sin(Math.toRadians(this.context.Lsun_true) - lambdastar2) - this.context.e * Math.sin(this.context.pi0 - lambdastar2));

            lambdastar2 += dlambdastar;
            betastar2 += dbetastar;

            // Transformation back to equatorial coordinates in radians
            double RAstar2 = Math.atan2((Math.sin(lambdastar2) * Utils.cosd(this.context.eps) - Math.tan(betastar2) * Utils.sind(this.context.eps)), Math.cos(lambdastar2));
            double DECstar2 = Math.asin(Math.sin(betastar2) * Utils.cosd(this.context.eps) + Math.cos(betastar2) * Utils.sind(this.context.eps) * Math.sin(lambdastar2));

            //Lunar distance of star
           this.context.starMoonDist = Math.toDegrees(Math.acos(Utils.sind(this.context.DECmoon) * Math.sin(DECstar2) + Utils.cosd(this.context.DECmoon) * Math.cos(DECstar2) * Utils.cosd(this.context.RAmoon - Math.toDegrees(RAstar2))));

            // Finals
           this.context.GHAstar = Utils.trunc(this.context.GHAAtrue - Math.toDegrees(RAstar2));
           this.context.SHAstar = Utils.trunc(360 - Math.toDegrees(RAstar2));
           this.context.DECstar = Math.toDegrees(DECstar2);
        } else {
            System.out.println(starName + " not found in the catalog...");
        }
    }

    public synchronized double getStarGHA(String starName) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        if (!starName.equals(this.context.starName)) {
            throw new RuntimeException(String.format("starPos was not invoked for %s (%s)", starName, this.context.starName));
        }
        return this.context.GHAstar;
    }
    public synchronized double getStarSHA(String starName) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        if (!starName.equals(this.context.starName)) {
            throw new RuntimeException(String.format("starPos was not invoked for %s (%s)", starName, this.context.starName));
        }
        return this.context.SHAstar;
    }
    public synchronized double getStarDec(String starName) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        if (!starName.equals(this.context.starName)) {
            throw new RuntimeException(String.format("starPos was not invoked for %s (%s)", starName, this.context.starName));
        }
        return this.context.DECstar;
    }
    public synchronized double getStarMoonDist(String starName) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        if (!starName.equals(this.context.starName)) {
            throw new RuntimeException(String.format("starPos was not invoked for %s (%s)", starName, this.context.starName));
        }
        return this.context.starMoonDist;
    }

    public final static int UTC_RISE_IDX = 0;
    public final static int UTC_SET_IDX = 1;
    public final static int RISE_Z_IDX = 2;
    public final static int SET_Z_IDX = 3;

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

    private double[] testSun(Calendar current, double lat, double lng) {
        this.setDateTime(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH) + 1,
                current.get(Calendar.DATE),
                current.get(Calendar.HOUR_OF_DAY),
                current.get(Calendar.MINUTE),
                current.get(Calendar.SECOND));
        this.calculate();
        SightReductionUtil sru = new SightReductionUtil(
                this.getSunGHA(),
                this.getSunDecl(),
                lat,
                lng);
        sru.calculate();
        double he = sru.getHe();
        double z = sru.getZ();
        return new double[] {he, z};
    }

    /**
     * Note: The calculate() method must have been invoked before.
     * <p>
     * TODO: Fine tune (by checking the elevation) after this calculation, which is not 100% accurate...
     *
     * @param latitude Observer's latitude
     * @return the time of rise and set of the body (Sun in that case).
     * @see <http://aa.usno.navy.mil/data/docs/RS_OneYear.php>
     * @see <http://www.jgiesen.de/SunMoonHorizon/>
     * @deprecated Use #sunRiseAndSetEpoch
     */
    @Deprecated
    public synchronized double[] sunRiseAndSet(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        //  out.println("Sun HP:" + this.context.HPsun);
        //  out.println("Sun SD:" + this.context.SDsun);
        double h0 = (this.context.HPsun / 3_600d) - (this.context.SDsun / 3_600d); // - (34d / 60d);
//  System.out.println(">>> DEBUG >>> H0:" + h0 + ", Sin Sun H0:" + Math.sin(Math.toRadians(h0)));
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(this.context.DECsun)));
        double t = Math.acos(cost);
        double lon = longitude;

//  while (lon < -180D)
//    lon += 360D;
        //  out.println("Lon:" + lon + ", Eot:" + this.context.EoT + " (" + (this.context.EoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
        double utRise = 12D - (this.context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (this.context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        // Based on http://en.wikipedia.org/wiki/Sunrise_equation
        //double phi = Math.toRadians(latitude);
        //double delta = Math.toRadians(this.context.DECsun);
        //double omega = Math.acos(- Math.tan(phi) * Math.tan(delta));
        //utRise = 12D - (this.context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(omega) / 15D);
        //utSet  = 12D - (this.context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(omega) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(this.context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        return new double[]{utRise, utSet, Z, 360d - Z};
    }

    public synchronized EpochAndZ[] sunRiseAndSetEpoch(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }

        double h0 = (this.context.HPsun / 3_600d) - (this.context.SDsun / 3_600d); // - (34d / 60d);
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(this.context.DECsun)));
        double t = Math.acos(cost);
        double lon = longitude;

        double utRise = 12D - (this.context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (this.context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(this.context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        double zRise = Z;
        double zSet = (360D - Z);

//		return new double[]{utRise, utSet, Z, 360d - Z};
        Calendar rise = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        Calendar set = (Calendar) rise.clone();

        TimeUtil.DMS dms = TimeUtil.decimalToDMS(utRise);

        rise.set(Calendar.YEAR, this.year);
        rise.set(Calendar.MONTH, this.month - 1);
        rise.set(Calendar.DAY_OF_MONTH, this.day);

        rise.set(Calendar.HOUR_OF_DAY, dms.getHours());
        rise.set(Calendar.MINUTE, dms.getMinutes());
        rise.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));

//		System.out.println("Rise:" + new Date(rise.getTimeInMillis()));

        // Fine tuning
        double[] riseTest = this.testSun(rise, latitude, longitude);
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.printf(">>>> 1st estimation: H rise (%s): %02f\n", new Date(rise.getTimeInMillis()), riseTest[0]);
        }
        if (riseTest[0] != 0) { // Elevation not 0, then adjust
            while (riseTest[0] > 0) {
                rise.add(Calendar.SECOND, -10);
                riseTest = this.testSun(rise, latitude, longitude);
            }
            // Starting tuning
            while (riseTest[0] < 0) {
                rise.add(Calendar.SECOND, 1);
                riseTest = this.testSun(rise, latitude, longitude);
            }
            zRise = riseTest[1];
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.printf(">> Tuned: Rising at %s, h:%f, z=%02f\272\n", new Date(rise.getTimeInMillis()), riseTest[0], riseTest[1]);
            }
        }
        long epochRise = rise.getTimeInMillis();

        dms = TimeUtil.decimalToDMS(utSet);

        set.set(Calendar.YEAR, this.year);
        set.set(Calendar.MONTH, this.month - 1);
        set.set(Calendar.DAY_OF_MONTH, this.day);

        set.set(Calendar.HOUR_OF_DAY, dms.getHours());
        set.set(Calendar.MINUTE, dms.getMinutes());
        set.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));
        // Fine tuning
        riseTest = testSun(set, latitude, longitude);
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.printf(">>>> 1st estimation: H set (%s): %02f\n", new Date(set.getTimeInMillis()), riseTest[0]);
        }
        if (riseTest[0] != 0) { // Elevation not 0, then adjust
            while (riseTest[0] < 0) {
                set.add(Calendar.SECOND, -10);
                riseTest = this.testSun(set, latitude, longitude);
            }
            // Starting tuning
            while (riseTest[0] > 0) {
                set.add(Calendar.SECOND, 1);
                riseTest = this.testSun(set, latitude, longitude);
            }
            zSet = riseTest[1];
            if ("true".equals(System.getProperty("astro.verbose"))) {
                System.out.printf(">> Tuned: Setting at %s, h:%f, z=%02f\272\n", new Date(set.getTimeInMillis()), riseTest[0], riseTest[1]);
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
     * @return meridian passage time in decimal hours.
     */
    public double getSunMeridianPassageTime(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        double t = (12d - (this.context.EoT / 60d));
        double deltaG = longitude / 15.0;
        return t - deltaG;
    }

    /**
     * @param latitude Observer's latitude
     * @param longitude Observer's longitude
     * @return as an epoch (today based)
     */
    public long getSunTransitTime(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        double inHours = this.getSunMeridianPassageTime(latitude, longitude);
        TimeUtil.DMS dms = TimeUtil.decimalToDMS(inHours);
        cal.set(Calendar.YEAR, this.year);
        cal.set(Calendar.MONTH, this.month - 1);
        cal.set(Calendar.DAY_OF_MONTH, this.day);

        cal.set(Calendar.HOUR_OF_DAY, dms.getHours());
        cal.set(Calendar.MINUTE, dms.getMinutes());
        cal.set(Calendar.SECOND, (int) Math.floor(dms.getSeconds()));

        return cal.getTimeInMillis();
    }

    /**
     * A Date format, without a time-zone
     * All members are numbers.
     * Name of the class stands for Year-Month-Day-Hours-Minutes-Seconds
     */
    public static class YMDHMSs {
        private int year;
        private int month; // [1..12]
        private int day;
        private int h24;
        private int minutes;
        private float seconds;

        public YMDHMSs() {}
        public YMDHMSs year(int year) {
            this.year = year;
            return this;
        }
        public YMDHMSs month(int month) {
            this.month = month;
            return this;
        }
        public YMDHMSs day(int day) {
            this.day = day;
            return this;
        }
        public YMDHMSs h24(int h24) {
            this.h24 = h24;
            return this;
        }
        public YMDHMSs minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }
        public YMDHMSs seconds(float seconds) {
            this.seconds = seconds;
            return this;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }

        public int getH24() {
            return h24;
        }

        public int getMinutes() {
            return minutes;
        }

        public float getSeconds() {
            return seconds;
        }
    }

    /**
     * Get the solar date at the time the "calculate" was invoked, for the position given as parameters
     * @param latitude User's latitude, at the time when {@link AstroComputerV2#calculate()} was invoked
     * @param longitude User's longitude, at the time when {@link AstroComputerV2#calculate()} was invoked
     * @return the Solar Date (No TZ)
     */
    public YMDHMSs getSolarDateAtPos(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }

        Calendar cal = GregorianCalendar.getInstance(); // TimeZone.getTimeZone("etc/UTC"));
        double eotInHours = this.getSunMeridianPassageTime(latitude, longitude);
        // TimeUtil.DMS dms = TimeUtil.decimalToDMS(inHours);
        cal.set(Calendar.YEAR, this.year);
        cal.set(Calendar.MONTH, this.month - 1);
        cal.set(Calendar.DAY_OF_MONTH, this.day);

        cal.set(Calendar.HOUR_OF_DAY, this.hour);
        cal.set(Calendar.MINUTE, this.minute);
        cal.set(Calendar.SECOND, (int) Math.floor(this.second));

        long ms = cal.getTimeInMillis();

        Date solar = new Date(ms + Math.round((12 - eotInHours) * 3_600_000));

        final SimpleDateFormat YEAR_FMT = new SimpleDateFormat("yyyy");
        final SimpleDateFormat MONTH_FMT = new SimpleDateFormat("MM");
        final SimpleDateFormat DAY_FMT = new SimpleDateFormat("dd");
        final SimpleDateFormat HOUR_FMT = new SimpleDateFormat("HH");
        final SimpleDateFormat MINUTE_FMT = new SimpleDateFormat("mm");
        final SimpleDateFormat SECOND_FMT = new SimpleDateFormat("ss");

        YEAR_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        MONTH_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        DAY_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        HOUR_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        MINUTE_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        SECOND_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));

        YMDHMSs solarDate = new YMDHMSs()
                .year(Integer.parseInt(YEAR_FMT.format(solar)))
                .month(Integer.parseInt(MONTH_FMT.format(solar)))
                .day(Integer.parseInt(DAY_FMT.format(solar)))
                .h24(Integer.parseInt(HOUR_FMT.format(solar)))
                .minutes(Integer.parseInt(MINUTE_FMT.format(solar)))
                .seconds(Float.parseFloat(SECOND_FMT.format(solar)));

        return solarDate;
    }

    public double getSunElevAtTransit(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        long sunTransitTime = this.getSunTransitTime(latitude, longitude); // TODO Look into that one.
        // double sunTransitTime2 = getSunMeridianPassageTime(latitude, longitude);

        // Requires another instance of the AstroComputer to calculate situation at transit time.
        AstroComputerV2 secondAstroComputer = new AstroComputerV2();
        secondAstroComputer.setDateTime(sunTransitTime); // WARNING!  Changes all the context!!
        secondAstroComputer.calculate();

//        System.out.printf("\tIn getSunElevAtTransit: calculation time is %s, Sun GHA: %f\n",
//                secondAstroComputer.getCalculationDateTime().getTime(),
//                secondAstroComputer.getSunGHA());

        double declAtTransit = secondAstroComputer.getSunDecl();
//        System.out.println("\tH at Sun Transit:" + (90.0 - (latitude - declAtTransit)));

        boolean WITH_Z_CHECK = false;

        if (WITH_Z_CHECK) {
            SightReductionUtil sru = new SightReductionUtil();

            sru.setL(latitude);
            sru.setG(longitude);

            sru.setAHG(secondAstroComputer.getSunGHA());
            sru.setD(secondAstroComputer.getSunDecl());
            sru.calculate();
            double obsAlt = sru.getHe();
            double z = sru.getZ(); // Should be 180.

            System.out.printf("At transit Time %s: Elev:%f, Z:%f < should be 180.\n", secondAstroComputer.getCalculationDateTime().getTime(), obsAlt, z);
        }
        return (90.0 - (latitude - declAtTransit));
    }

    public synchronized double[] sunRiseAndSet_wikipedia(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        double cost = Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(this.context.DECsun));
        double t = Math.acos(cost);
        double lon = longitude;
        double utRise = 12D - (this.context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        double utSet = 12D - (this.context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

        double Z = Math.acos((Math.sin(Math.toRadians(this.context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
                (0.9999 * Math.cos(Math.toRadians(latitude))));
        Z = Math.toDegrees(Z);

        return new double[]{utRise, utSet, Z, 360d - Z};
    }

    /**
     * See http://aa.usno.navy.mil/data/docs/RS_OneYear.php
     * <br>
     * See http://www.jgiesen.de/SunMoonHorizon/
     */
    public synchronized double[] moonRiseAndSet(double latitude, double longitude) {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        //  out.println("Moon HP:" + (this.context.HPmoon / 60) + "'");
        //  out.println("Moon SD:" + (this.context.SDmoon / 60) + "'");
        double h0 = (this.context.HPmoon / 3_600d) - (this.context.SDmoon / 3_600d) - (34d / 60d);
        //  out.println("Moon H0:" + h0);
        double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(this.context.DECmoon)));
        double t = Math.acos(cost);
        double lon = longitude;
        while (lon < -180D) {
            lon += 360D;
        }
        //  out.println("Moon Eot:" + context.moonEoT + " (" + (this.context.moonEoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
        double utRise = 12D - (this.context.moonEoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
        while (utRise < 0) {
            utRise += 24;
        }
        while (utRise > 24) {
            utRise -= 24;
        }
        double utSet = 12D - (this.context.moonEoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);
        while (utSet < 0) {
            utSet += 24;
        }
        while (utSet > 24) {
            utSet -= 24;
        }

        return new double[]{utRise, utSet};
    }

    public synchronized double getMoonIllum() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.k_moon;
    }

    public synchronized void setDeltaT(double deltaT) {
        if ("true".equals(System.getProperty("astro.verbose"))) {
            System.out.println("...DeltaT set to " + deltaT);
        }
        this.deltaT = deltaT;
    }

    public static synchronized double getTimeZoneOffsetInHours(TimeZone tz) {
        return getTimeZoneOffsetInHours(tz, new Date());
    }

    public static synchronized double getTimeZoneOffsetInHours(TimeZone tz, Date when) {
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

    public static synchronized double getTimeOffsetInHours(String timeOffset) {
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

    public synchronized double[] getSunMoon(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double[] values = new double[5];
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;

        this.calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(this.context.GHAsun);
        sru.setD(this.context.DECsun);
        sru.calculate();
        values[SUN_ALT_IDX] = sru.getHe();
        values[SUN_Z_IDX] = sru.getZ();
        // Moon
        sru.setAHG(this.context.GHAmoon);
        sru.setD(this.context.DECmoon);
        sru.calculate();
        values[MOON_ALT_IDX] = sru.getHe();
        values[MOON_Z_IDX] = sru.getZ();

        double ahl = this.context.GHAAtrue + lng;
        while (ahl < 0.0) {
            ahl += 360.0;
        }
        while (ahl > 360.0) {
            ahl -= 360.0;
        }
        values[LHA_ARIES_IDX] = ahl;

        return values;
    }

    public synchronized double getSunAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double value = 0d;
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;

        this.calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(this.context.GHAsun);
        sru.setD(this.context.DECsun);
        sru.calculate();
        value = sru.getHe();

        return value;
    }

    public synchronized double getMoonAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double value = 0d;
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;

        this.calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Moon
        sru.setAHG(this.context.GHAmoon);
        sru.setD(this.context.DECmoon);
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
    public synchronized double[] getSunMoonAltDecl(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
        double[] values = new double[5];
        this.year = y;
        this.month = m;
        this.day = d;
        this.hour = h;
        this.minute = mi;
        this.second = s;

//  System.out.println(y + "-" + month + "-" + day + " " + h + ":" + mi + ":" + s);

        this.calculate();
        SightReductionUtil sru = new SightReductionUtil();
        sru.setL(lat);
        sru.setG(lng);

        // Sun
        sru.setAHG(this.context.GHAsun);
        sru.setD(this.context.DECsun);
        sru.calculate();
        values[HE_SUN_IDX] = sru.getHe();
        // Moon
        sru.setAHG(this.context.GHAmoon);
        sru.setD(this.context.DECmoon);
        sru.calculate();
        values[HE_MOON_IDX] = sru.getHe();

        values[DEC_SUN_IDX] = this.context.DECsun;
        values[DEC_MOON_IDX] = this.context.DECmoon;

        double moonPhase = getMoonPhase(y, m, d, h, m, s);
        values[MOON_PHASE_IDX] = moonPhase;

        return values;
    }

    /**
     * Warning: Context must have been initialized!
     *
     * @return The Sun Declination
     */
    public synchronized double getSunDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECsun;
    }

    public synchronized double getSunGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAsun;
    }

    public synchronized double getSunRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAsun;
    }

    public synchronized double getSunSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDsun;
    }

    public synchronized double getSunHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPsun;
    }

    public synchronized double getAriesGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAAtrue;
    }

    public synchronized double getMoonDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECmoon;
    }

    public synchronized double getMoonGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAmoon;
    }

    public synchronized double getMoonRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAmoon;
    }

    public synchronized double getMoonSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDmoon;
    }

    public synchronized double getMoonHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPmoon;
    }

    public synchronized double getVenusDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECvenus;
    }

    public synchronized double getVenusGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAvenus;
    }

    public synchronized double getVenusRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAvenus;
    }

    public synchronized double getVenusSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDvenus;
    }

    public synchronized double getVenusHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPvenus;
    }

    public synchronized double getMarsDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECmars;
    }

    public synchronized double getMarsGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAmars;
    }

    public synchronized double getMarsRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAmars;
    }

    public synchronized double getMarsSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDmars;
    }

    public synchronized double getMarsHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPmars;
    }

    public synchronized double getJupiterDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECjupiter;
    }

    public synchronized double getJupiterGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAjupiter;
    }

    public synchronized double getJupiterRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAjupiter;
    }

    public synchronized double getJupiterSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDjupiter;
    }

    public synchronized double getJupiterHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPjupiter;
    }

    public synchronized double getSaturnDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECsaturn;
    }

    public synchronized double getSaturnGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHAsaturn;
    }

    public synchronized double getSaturnRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RAsaturn;
    }

    public synchronized double getSaturnSd() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.SDsaturn;
    }

    public synchronized double getSaturnHp() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.HPsaturn;
    }

    public synchronized double getPolarisDecl() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.DECpol;
    }

    public synchronized double getPolarisGHA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.GHApol;
    }

    public synchronized double getPolarisRA() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.RApol;
    }

    public synchronized double getEoT() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.EoT;
    }

    public synchronized double getLDist() { // Moon Sun
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.LDist;
    }

    public synchronized double getVenusMoonDist() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.moonVenusDist;
    }

    public synchronized double getMarsMoonDist() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.moonMarsDist;
    }

    public synchronized double getJupiterMoonDist() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.moonJupiterDist;
    }

    public synchronized double getSaturnMoonDist() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.moonSaturnDist;
    }

    public synchronized String getWeekDay() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.dow;
    }

    public synchronized String getMoonPhaseStr() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.moonPhase;
    }

    // Etc. Whatever is needed

    public synchronized double getMeanObliquityOfEcliptic() {
        if (!this.calculateHasBeenInvoked) {
            throw new RuntimeException("Calculation was never invoked in this context");
        }
        return this.context.eps0;
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

    public synchronized Map<String, Object> getAllCalculatedData() {
        Map<String, Object> fullMap = new HashMap<>();

        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("year", this.year);
        contextMap.put("month", this.month);
        contextMap.put("day", this.day);
        contextMap.put("hour", this.hour);
        contextMap.put("minute", this.minute);
        contextMap.put("second", this.second);
        contextMap.put("delta-t", this.deltaT);

        Map<String, Object> sunMap = new HashMap<>();
        sunMap.put("dec", this.context.DECsun);
        sunMap.put("gha", this.context.GHAsun);
        sunMap.put("ra", this.context.RAsun);
        sunMap.put("sd", this.context.SDsun);
        sunMap.put("hp", this.context.HPsun);

        Map<String, Object> moonMap = new HashMap<>();
        moonMap.put("dec", this.context.DECmoon);
        moonMap.put("gha", this.context.GHAmoon);
        moonMap.put("ra", this.context.RAmoon);
        moonMap.put("sd", this.context.SDmoon);
        moonMap.put("hp", this.context.HPmoon);

        Map<String, Object> venusMap = new HashMap<>();
        venusMap.put("dec", this.context.DECvenus);
        venusMap.put("gha", this.context.GHAvenus);
        venusMap.put("ra", this.context.RAvenus);
        venusMap.put("sd", this.context.SDvenus);
        venusMap.put("hp", this.context.HPvenus);

        Map<String, Object> marsMap = new HashMap<>();
        marsMap.put("dec", this.context.DECmars);
        marsMap.put("gha", this.context.GHAmars);
        marsMap.put("ra", this.context.RAmars);
        marsMap.put("sd", this.context.SDmars);
        marsMap.put("hp", this.context.HPmars);

        Map<String, Object> jupiterMap = new HashMap<>();
        jupiterMap.put("dec", this.context.DECjupiter);
        jupiterMap.put("gha", this.context.GHAjupiter);
        jupiterMap.put("ra", this.context.RAjupiter);
        jupiterMap.put("sd", this.context.SDjupiter);
        jupiterMap.put("hp", this.context.HPjupiter);

        Map<String, Object> saturnMap = new HashMap<>();
        saturnMap.put("dec", this.context.DECsaturn);
        saturnMap.put("gha", this.context.GHAsaturn);
        saturnMap.put("ra", this.context.RAsaturn);
        saturnMap.put("sd", this.context.SDsaturn);
        saturnMap.put("hp", this.context.HPsaturn);

        Map<String, Object> polarisMap = new HashMap<>();
        polarisMap.put("dec", this.context.DECpol);
        polarisMap.put("gha", this.context.GHApol);
        polarisMap.put("ra", this.context.RApol);

        fullMap.put("context", contextMap);

        fullMap.put("sun", sunMap);
        fullMap.put("moon", moonMap);
        fullMap.put("venus", venusMap);
        fullMap.put("mars", marsMap);
        fullMap.put("jupiter", jupiterMap);
        fullMap.put("saturn", saturnMap);
        fullMap.put("polaris", polarisMap);

        fullMap.put("aries-gha", this.context.GHAAtrue);

        fullMap.put("eot", this.context.EoT);
        fullMap.put("lunar-dist", this.context.LDist);
        fullMap.put("day-of-week", this.dow);
        fullMap.put("moon-phase", this.context.moonPhase);
        fullMap.put("mean-obliquity-of-ecliptic", this.context.eps0);

        // More if needed!

        return fullMap;
    }
}
