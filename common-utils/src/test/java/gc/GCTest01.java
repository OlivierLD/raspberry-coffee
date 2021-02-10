package gc;

import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.GreatCircleWayPoint;

import java.util.Vector;

public class GCTest01 {

    public static void main(String... args) {
        GreatCirclePoint start = new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("37", "38")), Math.toRadians(-GeomUtil.sexToDec("122", "46")));
//  GeoPoint p = dr(start, 30D, 230D);
        GreatCirclePoint p = new GreatCirclePoint(Math.toRadians(GeomUtil.sexToDec("20", "00")), Math.toRadians(-GeomUtil.sexToDec("150", "00")));
        System.out.println("Arriving:" + GeomUtil.decToSex(Math.toDegrees(p.getL()), GeomUtil.SWING, GeomUtil.NS) + ", " + GeomUtil.decToSex(Math.toDegrees(p.getG()), GeomUtil.SWING, GeomUtil.EW));
        GreatCircle test = new GreatCircle();
        test.setStart(start);
        test.setArrival(p);
        test.calculateGreatCircle(20);
        double gcDist = Math.toDegrees(test.getDistance()) * 60.0;
        System.out.println("GC Dist (nm):" + gcDist);
        Vector<GreatCircleWayPoint> gcRoute = test.getRoute();
        gcRoute.forEach(gcwp -> System.out.printf("Z:%.03f to %s%n", gcwp.getZ(), gcwp.getPoint().toString()));

        double ari = GreatCircle.getInitialRouteAngle(start, p);
//        if (ari < 0) {
//            ari += (2 * Math.PI);
//        }
        System.out.println("Initial Route Angle = " + Math.toDegrees(ari));

        GreatCircle.RLData rlData = test.calculateRhumbLine();
        double dist = rlData.getdLoxo();
        double route = rlData.getRv();
        System.out.println("Dist Loxo:" + dist + " (GC:" + gcDist + "), route (loxo):" + Math.toDegrees(route));

        System.out.println("-------------");
        test = new GreatCircle();
        test.setStart(new GreatCirclePoint(Math.toRadians(47.67941), Math.toRadians(-3.368855)));
        test.setArrival(new GreatCirclePoint(Math.toRadians(47.666931), Math.toRadians(-3.39822)));
        rlData = test.calculateRhumbLine();
        dist = rlData.getdLoxo();
        route = rlData.getRv();
        gcDist = Math.toDegrees(test.getDistance()) * 60.0;
        System.out.println("Dist (loxo):" + dist + " (GC:" + gcDist + "), route (loxo):" + Math.toDegrees(route));
    }

}
