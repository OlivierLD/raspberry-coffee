package gc;

import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;


public class GCTest03 {

    static final double BRISBANE_LATITUDE = -27.45;
    static final double BRISBANE_LONGITUDE = 153.073333;

    static final double RATATONGA_LATITUDE = 34.9;
    static final double RATATONGA_LONGITUDE = -139.8333;

    public static void main(String... args) {

        double gcDistInNM = GreatCircle.getGCDistanceInDegrees(
                new GreatCirclePoint(BRISBANE_LATITUDE, BRISBANE_LONGITUDE),
                new GreatCirclePoint(RATATONGA_LATITUDE, RATATONGA_LONGITUDE));
        System.out.println("Brisbane to Ratatonga Dist:" + gcDistInNM);
        System.out.println("Expect ARI ~49\272");

        double initialRouteAngle = GreatCircle.getInitialRouteAngle(
                new GreatCirclePoint(Math.toRadians(BRISBANE_LATITUDE), Math.toRadians(BRISBANE_LONGITUDE)),
                new GreatCirclePoint(Math.toRadians(RATATONGA_LATITUDE), Math.toRadians(RATATONGA_LONGITUDE)));

        System.out.println("ARI    :" + Math.toDegrees(initialRouteAngle));

//        double initialRouteAngleDeg = GreatCircle.getInitialRouteAngleInDegrees(
        double initialRouteAngleDeg = GreatCircle.getInitialRouteAngleInDegreesV2(
                new GreatCirclePoint(BRISBANE_LATITUDE, BRISBANE_LONGITUDE),
                new GreatCirclePoint(RATATONGA_LATITUDE, RATATONGA_LONGITUDE));

        System.out.println("ARI deg:" + initialRouteAngleDeg);

        System.out.println("--- Expect ~49.7\272 ---");
        // Moon to Sun
        double moonZ =  -154.724, moonElev =  23.992;
        double sunZ = -178.090, sunElev = 37.835;

        double moonSunIRADeg = GreatCircle.getInitialRouteAngleInDegrees(
                new GreatCirclePoint(moonElev, moonZ),
                new GreatCirclePoint(sunElev, sunZ));

        System.out.println("Moon-Sun ARI deg:" + moonSunIRADeg);

    }
}
