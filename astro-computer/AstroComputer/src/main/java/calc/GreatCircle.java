package calc;

import java.util.Vector;

public final class GreatCircle {
	public static final int TO_NORTH = 0;
	public static final int TO_SOUTH = 1;
	public static final int TO_EAST = 2;
	public static final int TO_WEST = 3;
	
	private int ewDir;
	private int nsDir;
	private GreatCirclePoint start;   // Angles in radians
	private GreatCirclePoint arrival; // Angles in radians
	private Vector<GreatCircleWayPoint> route;

	private static final double TOLERANCE = 1D;

	public GreatCircle() {
		this.start = null;
		this.arrival = null;
	}

	/**
	 *
	 * @param startingPoint angle values in radians
	 * @param arrivalPoint  angle values in radians
	 */
	public GreatCircle(GreatCirclePoint startingPoint, GreatCirclePoint arrivalPoint) {
		this.start = startingPoint;
		this.arrival = arrivalPoint;
	}

	/**
	 * Coordinates in radians
	 *
	 * @param p the position to start from
	 */
	public void setStart(GreatCirclePoint p) {
		this.start = p;
	}

	public GreatCircle start(GreatCirclePoint p) {
		this.start = p;
		return this;
	}

	public GreatCircle startInDegrees(GreatCirclePoint p) {
		this.start = new GreatCirclePoint(Math.toRadians(p.getL()),
				Math.toRadians(p.getG()));
		return this;
	}

	public GreatCircle arrival(GreatCirclePoint p) {
		this.arrival = p;
		return this;
	}

	public GreatCircle arrivalInDegrees(GreatCirclePoint p) {
		this.arrival = new GreatCirclePoint(Math.toRadians(p.getL()),
				Math.toRadians(p.getG()));
		return this;
	}

	public void setStartInDegrees(GreatCirclePoint p) {
		this.start = new GreatCirclePoint(Math.toRadians(p.getL()),
						 Math.toRadians(p.getG()));
	}

	/**
	 * Coordinates in radians
	 *
	 * @param p the arrival position
	 */
	public void setArrival(GreatCirclePoint p) {
		this.arrival = p;
	}

	public void setArrivalInDegrees(GreatCirclePoint p) {
		this.arrival = new GreatCirclePoint(Math.toRadians(p.getL()),
						Math.toRadians(p.getG()));
	}

	public GreatCirclePoint getStart() {
		return this.start;
	}

	public GreatCirclePoint getArrival() {
		return this.arrival;
	}

	public int getNS() {
		return this.nsDir;
	}

	public int getEW() {
		return this.ewDir;
	}

	/**
	 * See http://ressources.profmarine.fr/ortho/AC_TDC1_orthoV2.pdf, for validations...
	 *
	 * @return Initial Route Angle, in radians
	 */
	public double getInitialRouteAngle() {
		return GreatCircle.getInitialRouteAngle(this.start, this.arrival);
	}

	/**
	 *
	 * @param from angle values in radians
	 * @param to angle values in radians
	 * @return Initial Route Angle, in radians
	 */
	public static double getInitialRouteAngle(GreatCirclePoint from, GreatCirclePoint to) {
//		double g = to.getG() - from.getG();
		double g = from.getG() - to.getG();
		if (g > Math.PI) {
			g = (2 * Math.PI) - g;
		}
		if (g < -Math.PI) {
			g += (2 * Math.PI);
		}

		double lA = to.getL();
		double lD = from.getL();
		double gcArc = Math.acos((Math.sin(lD) * Math.sin(lA)) + (Math.cos(lD) * Math.cos(lA) * Math.cos(g)));
		// System.out.println(String.format("M: %.03f nm", 60 * Math.toDegrees(gcArc)));
		double ira = Math.asin((Math.sin(g) * Math.cos(lA)) / Math.sin(gcArc));
		if (ira > 0) { // From the north
			if (g < 0) { // to West
				ira = (2 * Math.PI) - ira;
			}
		} else { // From the south
			ira = Math.abs(ira);
			if (g > 0) { // to East
				ira = Math.PI - ira;
			} else { // To West
				ira = Math.PI + ira;
			}
		}
		return ira;
	}

	/**
	 * Prefer this one, rather than getInitialRouteAngleInDegreesV2
	 *
	 * @param from all values in degrees
	 * @param to all values in degrees
	 * @return Initial Route Angle in degrees
	 */
	public static double getInitialRouteAngleInDegrees(GreatCirclePoint from, GreatCirclePoint to) {
//		double g = to.getG() - from.getG();
		double g = from.getG() - to.getG();
		if (g > 180) {
			g = 360 - g;
		}
		if (g < -180) {
			g += 360;
		}
		double lA = to.getL();
		double lD = from.getL();
		double gcArc = Math.acos((Math.sin(Math.toRadians(lD)) * Math.sin(Math.toRadians(lA))) +
				(Math.cos(Math.toRadians(lD)) * Math.cos(Math.toRadians(lA)) * Math.cos(Math.toRadians(g))));
		// System.out.println(String.format("M: %.03f nm", 60 * Math.toDegrees(gcArc)));
		double V = Math.asin((Math.sin(Math.toRadians(g)) * Math.cos(Math.toRadians(lA))) / Math.sin(gcArc));
		if (V > 0) { // From the north
			if (g < 0) { // to West
				V = (2 * Math.PI) - V;
			}
		} else { // From the south
//			V = Math.abs(V);
			if (g > 0) { // to East
				V = Math.PI - V;
			} else { // To West
				V = Math.PI + V;
			}
		}
		return Math.toDegrees(V);
	}

	/**
	 * Prefer getInitialRouteAngleInDegrees
	 *
	 * @param from all values in degrees
	 * @param to all values in degrees
	 * @return Initial Route Angle in degrees
	 */
	public static double getInitialRouteAngleInDegreesV2(GreatCirclePoint from, GreatCirclePoint to) {
//		double g = to.getG() - from.getG();
		double g = from.getG() - to.getG();
		if (g > 180) {
			g = 360 - g;
		}
		if (g < -180) {
			g += 360;
		}
		double lA = to.getL();
		double lD = from.getL();

		double V = Math.atan(Math.sin(Math.toRadians(g)) /
				((Math.cos(Math.toRadians(lD)) * Math.tan(Math.toRadians(lA))) - (Math.sin(Math.toRadians(lD)) * Math.cos(Math.toRadians(g)))));

		if (V > 0) { // From the north
			if (g < 0) { // to West
				V = (2 * Math.PI) - V;
			}
		} else { // From the south
//			V = Math.abs(V);
			if (g > 0) { // to East
				V = Math.PI - V;
			} else { // To West
				V = Math.PI + V;
			}
		}
		return Math.toDegrees(V);
	}

	public void calculateGreatCircle(int nbPoints) {
		if (arrival.getL() > start.getL()) {
			nsDir = TO_NORTH;
		} else {
			nsDir = TO_SOUTH;
		}
		if (arrival.getG() > start.getG()) {
			ewDir = TO_EAST;
		} else {
			ewDir = TO_WEST;
		}
		if (Math.abs(arrival.getG() - start.getG()) > Math.PI) {
			if (ewDir == TO_EAST) {
				ewDir = TO_WEST;
				arrival.setG(arrival.getG() - (2 * Math.PI));
			} else {
				ewDir = TO_EAST;
				arrival.setG((2 * Math.PI) + arrival.getG());
			}
		}
		double deltaG = arrival.getG() - start.getG();
		route = new Vector<>(nbPoints);
		double interval = deltaG / (double) nbPoints;
		GreatCirclePoint smallStart = start;
		boolean go = true;
		for (double g = start.getG(); route.size() <= nbPoints; g += interval) {
			double deltag = arrival.getG() - g;
			double tanStartAngle = Math.sin(deltag) / (Math.cos(smallStart.getL()) * Math.tan(arrival.getL()) - Math.sin(smallStart.getL()) * Math.cos(deltag));
			double smallL = Math.atan(Math.tan(smallStart.getL()) * Math.cos(interval) + Math.sin(interval) / (tanStartAngle * Math.cos(smallStart.getL())));
			double rpG = g + interval;
			if (rpG > Math.PI) {
				rpG -= (2 * Math.PI);
			}
			if (rpG < -Math.PI) {
				rpG = (2 * Math.PI) + rpG;
			}
			GreatCirclePoint routePoint = new GreatCirclePoint(smallL, rpG);
			double ari = Math.toDegrees(Math.atan(tanStartAngle));
			if (ari < 0.0D) {
				ari = Math.abs(ari);
			}

			int _nsDir;
			if (routePoint.getL() > smallStart.getL()) {
				_nsDir = TO_NORTH;
			} else {
				_nsDir = TO_SOUTH;
			}
			double arrG = routePoint.getG();
			double staG = smallStart.getG();
			if (sign(arrG) != sign(staG)) {
				if (sign(arrG) > 0) {
					arrG -= (2 * Math.PI);
				} else {
					arrG = Math.PI - arrG;
				}
			}
			int _ewDir;
			if (arrG > staG) {
				_ewDir = TO_EAST;
			} else {
				_ewDir = TO_WEST;
			}
			double _start = 0.0D;
			if (_nsDir == TO_SOUTH) {
				_start = 180D;
				if (_ewDir == TO_EAST) {
					ari = _start - ari;
				} else {
					ari = _start + ari;
				}
			} else if (_ewDir == TO_EAST) {
				ari = _start + ari;
			} else {
				ari = _start - ari;
			}
			while (ari < 0.0D) {
				ari += 360;
			}
			route.addElement(new GreatCircleWayPoint(smallStart, arrival.equals(smallStart) ? null : (Double.isNaN(ari) ? null : ari)));
			smallStart = routePoint;
		}
	}

	/**
	 * GC Distance
	 * @return in radians
	 */
	public double getDistance() {
//		double cos = Math.sin(this.start.getL()) * Math.sin(this.arrival.getL()) + Math.cos(this.start.getL()) *
//				Math.cos(this.arrival.getL()) * Math.cos(this.arrival.getG() - this.start.getG());
//		return Math.acos(cos);
		return GreatCircle.getDistance(this.start, this.arrival);
	}
	
	public static double getDistance(GreatCirclePoint from, GreatCirclePoint to) {
		double cos = Math.sin(from.getL()) * Math.sin(to.getL()) + Math.cos(from.getL()) * 
				Math.cos(to.getL()) * Math.cos(to.getG() - from.getG());
		return Math.acos(cos);
	}

	public double getDistanceInDegrees() {
		return Math.toDegrees(this.getDistance());
	}

	public static double getDistanceInDegrees(GreatCirclePoint from, GreatCirclePoint to) {
		return Math.toDegrees(getDistance(from, to));
	}
	
	public double getDistanceInNM() {
		return (this.getDistanceInDegrees() * 60D);
	}

	/**
	 * @param from in degrees
	 * @param to   in degrees
	 * @return in nautical miles
	 */
	public static double getDistanceInNM(GreatCirclePoint from, GreatCirclePoint to) {
		return (GreatCircle.getDistanceInDegrees(from, to) * 60D);
	}

	/**
	 * Input in radians, output in nautical miles.
	 * 
	 * @param from in Radians
	 * @param to   in Radians
	 * @return in miles
	 */
	public static double getGCDistance(GreatCirclePoint from, GreatCirclePoint to) {
		double cos = Math.sin(from.getL()) * Math.sin(to.getL()) + Math.cos(from.getL()) * Math.cos(to.getL()) * Math.cos(to.getG() - from.getG());
		double dist = Math.acos(cos);
		return Math.toDegrees(dist) * 60D;
	}

	/**
	 * Input in degrees, output in nautical miles.
	 *
	 * @param from in Degrees
	 * @param to   in Degrees
	 * @return in miles
	 */
	public static double getGCDistanceInDegrees(GreatCirclePoint from, GreatCirclePoint to) {
		double cos = Math.sin(Math.toRadians(from.getL())) * Math.sin(Math.toRadians(to.getL())) + Math.cos(Math.toRadians(from.getL())) * Math.cos(Math.toRadians(to.getL())) * Math.cos(Math.toRadians(to.getG()) - Math.toRadians(from.getG()));
		double dist = Math.acos(cos);
		return Math.toDegrees(dist) * 60D;
	}

	/**
	 * RhumbLine data holder
	 */
	public static class RLData {
		double rv;
		double dLoxo;

		public RLData(double rv, double d) {
			this.rv = rv;
			this.dLoxo = d;
		}

		public double getRv() {
			return rv;
		}

		public double getdLoxo() {
			return dLoxo;
		}
	}

	public RLData calculateRhumbLine() {
		return GreatCircle.calculateRhumbLine(this.start, this.arrival);
	}

	public static RLData calculateRhumbLine(GreatCirclePoint from, GreatCirclePoint to) {
		int nsDir = -1, ewDir = -1;
		double rv, dLoxo;
		if (to.getL() > from.getL()) {
			nsDir = TO_NORTH;
		} else {
			nsDir = TO_SOUTH;
		}
		double arrG = to.getG();
		double staG = from.getG();
		if (sign(arrG) != sign(staG) && Math.abs(arrG - staG) > Math.PI) {
			if (sign(arrG) > 0) {
				arrG -= (2 * Math.PI);
			} else {
				arrG = Math.PI - arrG;
			}
		}
		if (arrG - staG > 0.0D) {
			ewDir = TO_EAST;
		} else {
			ewDir = TO_WEST;
		}
		double deltaL = Math.toDegrees((to.getL() - from.getL())) * 60D;
		double radianDeltaG = to.getG() - from.getG();
		if (Math.abs(radianDeltaG) > Math.PI) {
			radianDeltaG = (2 * Math.PI) - Math.abs(radianDeltaG);
		}
		double deltaG = Math.toDegrees(radianDeltaG) * 60D;
		if (deltaG < 0.0D) {
			deltaG = -deltaG;
		}
		double startLC = Math.log(Math.tan((Math.PI / 4D) + from.getL() / 2D));
		double arrLC = Math.log(Math.tan((Math.PI / 4D) + to.getL() / 2D));
		double deltaLC = 3_437.7467707849396D * (arrLC - startLC);
		if (deltaLC != 0d) {
			rv = Math.atan(deltaG / deltaLC);
		} else if (radianDeltaG > 0d) {
			rv = (Math.PI / 2D);
		} else {
			rv = (3 * Math.PI / 2D);
		}
		if (deltaL != 0d) {
			dLoxo = deltaL / Math.cos(rv);
		} else {
			dLoxo = deltaG * Math.cos(from.getL()); // TASK Make sure that's right...
		}
		if (dLoxo < 0.0D) {
			dLoxo = -dLoxo;
		}
		if (rv < 0.0D) {
			rv = -rv;
		}
		if (ewDir == TO_EAST) {
			if (nsDir != TO_NORTH) {
				rv = Math.PI - rv;
			}
		} else if (deltaLC != 0d) {
			if (nsDir == TO_NORTH) {
				rv = (2 * Math.PI) - rv;
			} else {
				rv = Math.PI + rv;
			}
		}
		while (rv >= (2 * Math.PI)) {
			rv -= (2 * Math.PI);
		}
		return new RLData(rv, dLoxo);
	}

	/*
	 * Points coordinates in Radians
	 */
	public static double calculateRhumLineDistance(GreatCirclePoint f, GreatCirclePoint t) {
		int _nsDir = 0;
		if (t.getL() > f.getL()) {
			_nsDir = TO_NORTH;
		} else {
			_nsDir = TO_SOUTH;
		}
		double arrG = t.getG();
		double staG = f.getG();
		if (sign(arrG) != sign(staG) && Math.abs(arrG - staG) > Math.PI) {
			if (sign(arrG) > 0) {
				arrG -= (2 * Math.PI);
			} else {
				arrG = Math.PI - arrG;
			}
		}
		int _ewDir;
		if ((arrG - staG) > 0.0D) {
			_ewDir = TO_EAST;
		} else {
			_ewDir = TO_WEST;
		}
		double deltaL = Math.toDegrees((t.getL() - f.getL())) * 60D;
		double radianDeltaG = t.getG() - f.getG();
		if (Math.abs(radianDeltaG) > Math.PI) {
			radianDeltaG = (2 * Math.PI) - Math.abs(radianDeltaG);
		}
		double deltaG = Math.toDegrees(radianDeltaG) * 60D;
		if (deltaG < 0.0D) {
			deltaG = -deltaG;
		}
		double startLC = Math.log(Math.tan((Math.PI / 4D) + f.getL() / 2D));
		double arrLC = Math.log(Math.tan((Math.PI / 4D) + t.getL() / 2D));
		double deltaLC = 3_437.7467707849396D * (arrLC - startLC);
		double _rv = 0.0D;
		if (deltaLC != 0d) {
			_rv = Math.atan(deltaG / deltaLC);
		} else {
			if (radianDeltaG > 0d) {
				_rv = (Math.PI / 2D);
			} else {
				_rv = (3 * Math.PI / 2D);
			}
		}
		double _dLoxo = deltaL / Math.cos(_rv);
		if (deltaL == 0) {
			_dLoxo = radianDeltaG * Math.cos(Math.toRadians(f.getL()));
		}
		if (_dLoxo < 0.0D) {
			_dLoxo = -_dLoxo;
		}
		if (_rv < 0.0D) {
			_rv = -_rv;
		}
		if (_ewDir == TO_EAST) {
			if (_nsDir != TO_NORTH) {
				_rv = Math.PI - _rv;
			}
		} else if (deltaLC != 0d) {
			if (_nsDir == TO_NORTH) {
				_rv = (2 * Math.PI) - _rv;
			} else {
				_rv = Math.PI + _rv;
			}
		}
		for (; _rv >= (2 * Math.PI); _rv -= (2 * Math.PI));
		return _dLoxo;
	}

	/**
	 * Rhumbline aka loxodrome
	 * Points coordinates in Radians
	 * returned value in radians
	 */
	public static double calculateRhumbLineRoute(GreatCirclePoint f, GreatCirclePoint t) {
		int _nsDir = 0;
		if (t.getL() > f.getL()) {
			_nsDir = TO_NORTH;
		} else {
			_nsDir = TO_SOUTH;
		}
		double arrG = t.getG();
		double staG = f.getG();
		if (sign(arrG) != sign(staG) && Math.abs(arrG - staG) > Math.PI) {
			if (sign(arrG) > 0) {
				arrG -= (2 * Math.PI);
			} else {
				arrG = Math.PI - arrG;
			}
		}
		int _ewDir;
		if (arrG - staG > 0.0D) {
			_ewDir = TO_EAST;
		} else {
			_ewDir = TO_WEST;
		}
		double deltaL = Math.toDegrees((t.getL() - f.getL())) * 60D;
		double radianDeltaG = t.getG() - f.getG();
		if (Math.abs(radianDeltaG) > Math.PI) {
			radianDeltaG = (2 * Math.PI) - Math.abs(radianDeltaG);
		}
		double deltaG = Math.toDegrees(radianDeltaG) * 60D;
		if (deltaG < 0.0D) {
			deltaG = -deltaG;
		}
		double startLC = Math.log(Math.tan((Math.PI / 4D) + f.getL() / 2D));
		double arrLC = Math.log(Math.tan((Math.PI / 4D) + t.getL() / 2D));
		double deltaLC = 3_437.7467707849396D * (arrLC - startLC);
		double _rv = 0.0D;
		if (deltaLC != 0d) {
			_rv = Math.atan(deltaG / deltaLC);
		} else if (radianDeltaG > 0d) {
			_rv = (Math.PI / 2D);
		} else {
			_rv = (3 * Math.PI / 2D);
		}
		double _dLoxo = deltaL / Math.cos(_rv);
		if (_dLoxo < 0.0D) {
			_dLoxo = -_dLoxo;
		}
		if (_rv < 0.0D) {
			_rv = -_rv;
		}
		if (_ewDir == TO_EAST) {
			if (_nsDir != TO_NORTH) {
				_rv = Math.PI - _rv;
			}
		} else if (deltaLC != 0d) {
			if (_nsDir == TO_NORTH) {
				_rv = (2 * Math.PI) - _rv;
			} else {
				_rv = Math.PI + _rv;
			}
		}
		for (; _rv >= (2 * Math.PI); _rv -= (2 * Math.PI));
		return _rv;
	}

	private static int sign(double d) {
		if (d == 0.0D) {
			return 0;
		}
		return d >= 0.0D ? 1 : -1;
	}

//	public double getRhumbLineDistance() {
//		return dLoxo;
//	}
//
//	public double getRhumbLineRoute() {
//		return rv;
//	}

	public Vector<GreatCircleWayPoint> getRoute() {
		return route;
	}

	public static Vector<GreatCircleWayPoint> inDegrees(Vector<GreatCircleWayPoint> inRads) {
		inRads.forEach(rad -> {
			rad.getPoint().latitude = Math.toDegrees(rad.getPoint().latitude);
			rad.getPoint().longitude = Math.toDegrees(rad.getPoint().longitude);
		});
		return inRads;
	}

	/**
	 * Dead Reckoning
	 *
	 * @param from  GeoPoint, L &amp; G in Radians
	 * @param dist  distance in nm
	 * @param route route in degrees
	 * @return DR Position, L &amp; G in Radians
	 */
	public static GreatCirclePoint dr(GreatCirclePoint from, double dist, double route) {
		double deltaL = Math.toRadians(dist / 60D) * Math.cos(Math.toRadians(route));
		double l2 = from.getL() + deltaL;
//  double lc1 = Math.log(Math.tan((Math.PI / 4D) + from.getL() / 2D));
//  double lc2 = Math.log(Math.tan((Math.PI / 4D) + l2 / 2D));
//  double deltaLc = lc2 - lc1;
//  double deltaG = deltaLc * Math.tan(Math.toRadians(route));
		double deltaG = Math.toRadians(dist / (60D * Math.cos((from.getL() + l2) / 2D))) * Math.sin(Math.toRadians(route)); // 2009-mar-10
		double g2 = from.getG() + deltaG;
		return new GreatCirclePoint(l2, g2);
	}
}
