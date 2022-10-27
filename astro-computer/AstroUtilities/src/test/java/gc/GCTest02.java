package gc;


import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;

public class GCTest02 {
    public static void main(String... args) {

        // N-S: should be 60.
        double gcDistInNM = GreatCircle.getGCDistanceInDegrees(
                new GreatCirclePoint(37.73, -122.50),
                new GreatCirclePoint(38.73, -122.50)
        );
        System.out.println("Dist:" + gcDistInNM);

        GreatCirclePoint dr = GreatCircle.dr(new GreatCirclePoint(Math.toRadians(45D),
                Math.toRadians(-130D)),
                55,
                270);
        System.out.println("Reaching " + new GreatCirclePoint(Math.toDegrees(dr.getL()),
                Math.toDegrees(dr.getG())).toString());
        System.out.println("Done.");

        System.out.println("----------------------");

        GreatCirclePoint p1 = new GreatCirclePoint(Math.toRadians(20.02), Math.toRadians(-155.85));
        GreatCirclePoint p2 = new GreatCirclePoint(Math.toRadians(19.98), Math.toRadians(-155.89));

        String from = GeomUtil.decToSex(Math.toDegrees(p1.getL()), GeomUtil.SWING, GeomUtil.NS) + ", " +
                GeomUtil.decToSex(Math.toDegrees(p1.getG()), GeomUtil.SWING, GeomUtil.EW);
        String to = GeomUtil.decToSex(Math.toDegrees(p2.getL()), GeomUtil.SWING, GeomUtil.NS) + ", " +
                GeomUtil.decToSex(Math.toDegrees(p2.getG()), GeomUtil.SWING, GeomUtil.EW);

        System.out.println(String.format("Distance between %s and %s = %.04f nm, %.04f km",
                from,
                to,
                (p1.gcDistanceBetween(p2) * 60),
                (p1.gcDistanceBetween(p2) * 60 * 1.852))
        );

        // Step: 0.010 km between N  37 20.13' / W 121 42.96' and N  37 20.13' / W 121 42.96' (17-Jun-2017 11:42:37)
        p1 = new GreatCirclePoint(
                Math.toRadians(GeomUtil.sexToDec("37", "20.13")),
                Math.toRadians(GeomUtil.sexToDec("-121", "42.96"))
        );
        p2 = new GreatCirclePoint(
                Math.toRadians(GeomUtil.sexToDec("37", "20.13")),
                Math.toRadians(GeomUtil.sexToDec("-121", "42.96"))
        );

        from = GeomUtil.decToSex(Math.toDegrees(p1.getL()), GeomUtil.SWING, GeomUtil.NS) + ", " +
                GeomUtil.decToSex(Math.toDegrees(p1.getG()), GeomUtil.SWING, GeomUtil.EW);
        to = GeomUtil.decToSex(Math.toDegrees(p2.getL()), GeomUtil.SWING, GeomUtil.NS) + ", " +
                GeomUtil.decToSex(Math.toDegrees(p2.getG()), GeomUtil.SWING, GeomUtil.EW);

        System.out.println(String.format("Distance between %s and %s = %.04f nm, %.04f km",
                from,
                to,
                (p1.gcDistanceBetween(p2) * 60),
                (p1.gcDistanceBetween(p2) * 60 * 1.852))
        );
    }
}
