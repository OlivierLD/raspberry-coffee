package gc;

import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.GreatCircleWayPoint;

public class GCTest04 {

	public static void main(String... args) {
		GreatCircle gc = new GreatCircle();
		// Find satellite heading (direction), and altitude
		// Satellite I-4 F3 Americas: 0N, 98W, I-4 F1 APAC, Asia Pac: 144E, Alphasat, Europe, West Asia, Africa: 25E
		// Home 37 44.94 N, 122 30.44 W
//  GeoPoint fromHome = new GeoPoint(GeomUtil.sexToDec("37", "44.04"), - GeomUtil.sexToDec("122", "30.44"));
		GreatCirclePoint fromHome = new GreatCirclePoint(37.75, -122.5);
//  GeoPoint fromHome = new GeoPoint(GeomUtil.sexToDec("0", "44.04"), - GeomUtil.sexToDec("98", "30.44"));
//  GeoPoint toSat    = new GeoPoint(GeomUtil.sexToDec("0", "0"), - GeomUtil.sexToDec("98", "0"));
		GreatCirclePoint toSat = new GreatCirclePoint(0, -98);
//  GeoPoint toSat    = new GeoPoint(GeomUtil.sexToDec("0", "0"), GeomUtil.sexToDec("144", "0"));
//  GeoPoint toSat    = new GeoPoint(GeomUtil.sexToDec("0", "0"), GeomUtil.sexToDec("25", "0"));
		gc.setStartInDegrees(fromHome);
		gc.setArrivalInDegrees(toSat);
		gc.calculateGreatCircle(10);
		double alt = 90d - Math.toDegrees(gc.getDistance());
		GreatCircleWayPoint firstPoint = gc.getRoute().get(0);
		double heading = firstPoint.getZ();
		System.out.println("Satellite: Alt:" + alt + " , true heading " + heading);
	}
}
