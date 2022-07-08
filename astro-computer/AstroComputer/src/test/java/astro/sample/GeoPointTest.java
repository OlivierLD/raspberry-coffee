package astro.sample;

import calc.GeoPoint;
import calc.GeomUtil;

public class GeoPointTest {
    public static void main(String... args) {
        GeoPoint p1 = new GeoPoint(37, -122);
        GeoPoint p2 = new GeoPoint(38, -121);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("-----------------------------------");

        p1 = new GeoPoint(62, 153);
        p2 = new GeoPoint(62, -135);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("-----------------------------------");

        p1 = new GeoPoint(28, -139);
        p2 = new GeoPoint(26, 147);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("GC   :" + p1.gcDistanceBetween(p2));
        System.out.println("-----------------------------------");

        long before = System.currentTimeMillis();
        double d = 0D;
        for (int i = 0; i < 10000; i++)
            d = p1.loxoDistanceBetween(p2);
        long after = System.currentTimeMillis();
        System.out.println("10000 Loxo :" + Long.toString(after - before) + " ms.");

        before = System.currentTimeMillis();
        d = 0D;
        for (int i = 0; i < 10000; i++)
            d = p1.orthoDistanceBetween(p2);
        after = System.currentTimeMillis();
        System.out.println("10000 Ortho:" + Long.toString(after - before) + " ms.");

        before = System.currentTimeMillis();
        d = 0D;
        for (int i = 0; i < 10000; i++)
            d = p1.gcDistanceBetween(p2);
        after = System.currentTimeMillis();
        System.out.println("10000 GC   :" + Long.toString(after - before) + " ms.");
        System.out.println("-----------------------------------");

        p1 = new GeoPoint(GeomUtil.sexToDec("38", "31.44"), -GeomUtil.sexToDec("128", "17.95"));
        p2 = new GeoPoint(GeomUtil.sexToDec("38", "33.99"), -GeomUtil.sexToDec("128", "36.98"));
        System.out.println("Distance between " + p1.toString() + " and " + p2.toString() + ": " + p1.gcDistanceBetween(p2) + " nm");

        System.out.println("----------------------");
        p1 = new GeoPoint(20.02, -155.85);
        p2 = new GeoPoint(19.98, -155.89);
        System.out.println("Distance between " + p1.toString() + " and " + p2.toString() + ": " + p1.gcDistanceBetween(p2) + " nm");
    }
}
