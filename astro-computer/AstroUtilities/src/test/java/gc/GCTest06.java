package gc;

import calc.GeomUtil;
import calc.GreatCirclePoint;

public class GCTest06 {

    public static void main(String... args) {
        GreatCirclePoint p1 = new GreatCirclePoint(37, -122);
        GreatCirclePoint p2 = new GreatCirclePoint(38, -121);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("-----------------------------------");

        p1 = new GreatCirclePoint(62, 153);
        p2 = new GreatCirclePoint(62, -135);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("-----------------------------------");

        p1 = new GreatCirclePoint(28, -139);
        p2 = new GreatCirclePoint(26, 147);
        System.out.println("Ortho:" + p1.orthoDistanceBetween(p2));
        System.out.println("Loxo :" + p1.loxoDistanceBetween(p2));
        System.out.println("GC   :" + p1.gcDistanceBetween(p2));
        System.out.println("-----------------------------------");

        long before = System.currentTimeMillis();
        double d = 0D;
        for (int i = 0; i < 10_000; i++) {
            d = p1.loxoDistanceBetween(p2);
        }
        long after = System.currentTimeMillis();
        System.out.println("10000 Loxo :" + Long.toString(after - before) + " ms.");

        before = System.currentTimeMillis();
        d = 0D;
        for (int i = 0; i < 10_000; i++) {
            d = p1.orthoDistanceBetween(p2);
        }
        after = System.currentTimeMillis();
        System.out.println("10000 Ortho:" + Long.toString(after - before) + " ms.");

        before = System.currentTimeMillis();
        d = 0D;
        for (int i = 0; i < 10_000; i++) {
            d = p1.gcDistanceBetween(p2);
        }
        after = System.currentTimeMillis();
        System.out.println("10000 GC   :" + Long.toString(after - before) + " ms.");
        System.out.println("-----------------------------------");

        p1 = new GreatCirclePoint(GeomUtil.sexToDec("38", "31.44"), -GeomUtil.sexToDec("128", "17.95"));
        p2 = new GreatCirclePoint(GeomUtil.sexToDec("38", "33.99"), -GeomUtil.sexToDec("128", "36.98"));
        System.out.println("Distance between " + p1.toString() + " and " + p2.toString() + ": " + p1.gcDistanceBetween(p2) + " nm");

        System.out.println("----------------------");
        p1 = new GreatCirclePoint(20.02, -155.85);
        p2 = new GreatCirclePoint(19.98, -155.89);
        System.out.println("Distance between " + p1.toString() + " and " + p2.toString() + ": " + p1.gcDistanceBetween(p2) + " nm");
    }

}
