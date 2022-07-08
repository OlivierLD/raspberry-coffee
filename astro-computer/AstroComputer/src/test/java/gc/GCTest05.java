package gc;

import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.GreatCircleWayPoint;

public class GCTest05 {
	// Examples of distances and directions calculations
	public static void main(String... args) {
		GreatCirclePoint from = new GreatCirclePoint(37.501282801564244, -122.48082160949707); // the boat
		GreatCirclePoint to = new GreatCirclePoint(37.49403906867881, -122.48468399047852); // HMB Entrance

		double dist = from.gcDistanceBetween(to);
		double heading = GreatCircle.calculateRhumbLineRoute(from.degreesToRadians(),
				to.degreesToRadians());
		double dist2 = GreatCircle.calculateRhumLineDistance(from.degreesToRadians(),
				to.degreesToRadians());
		System.out.println("GC Dist:" + dist + ", Loxo Dist:" + dist2 + " nm, " + (dist2 * 1852) + " m, heading " + Math.toDegrees(heading));

		GreatCircle gc = new GreatCircle();

		if (true) {
			gc.setStartInDegrees(from);
			gc.setArrivalInDegrees(to);
			gc.calculateGreatCircle(10);
			dist = Math.toDegrees(gc.getDistance()) * 60;
			GreatCircleWayPoint first = gc.getRoute().get(0);
			heading = first.getZ();
			System.out.println("Ortho Dist:" + dist + " nm, " + (dist * 1852) + " m, heading " + heading);
		}
		// Find satellite heading (direction), and altitude
		// Satellite I-4 F3 Americas: 0N, 98W, I-4 F1 APAC, Asia Pac: 144E, Alphasat, Europe, West Asia, Africa: 25E
		// Home 37 44.94 N, 122 30.44 W
		GreatCirclePoint fromHome = new GreatCirclePoint(GeomUtil.sexToDec("37", "44.04"), -GeomUtil.sexToDec("122", "30.44"));
//  GeoPoint fromHome = new GeoPoint(GeomUtil.sexToDec("0", "44.04"), - GeomUtil.sexToDec("98", "30.44"));
		GreatCirclePoint toSat = new GreatCirclePoint(GeomUtil.sexToDec("0", "0"), -GeomUtil.sexToDec("98", "0"));
//  GeoPoint toSat    = new GeoPoint(GeomUtil.sexToDec("0", "0"), GeomUtil.sexToDec("144", "0"));
//  GeoPoint toSat    = new GeoPoint(GeomUtil.sexToDec("0", "0"), GeomUtil.sexToDec("25", "0"));
		gc.setStartInDegrees(fromHome);
		gc.setArrivalInDegrees(toSat);
		gc.calculateGreatCircle(10);
		double alt = 90d - Math.toDegrees(gc.getDistance());
		GreatCircleWayPoint firstPoint = gc.getRoute().get(0);
		heading = firstPoint.getZ();
		System.out.println("Satellite: Alt:" + alt + " , true heading " + heading);
	}
}
