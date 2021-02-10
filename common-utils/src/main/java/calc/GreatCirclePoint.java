package calc;

import java.io.Serializable;

public final class GreatCirclePoint
				implements Serializable {
	double latitude;
	double longitude;

	public GreatCirclePoint(double l, double g) {
		latitude = l;
		longitude = g;
	}

	public GreatCirclePoint(GeoPoint gp) {
		latitude = gp.getL();
		longitude = gp.getG();
	}

	public double getL() {
		return latitude;
	}

	public double getG() {
		return longitude;
	}

	public void setL(double l) {
		latitude = l;
	}

	public void setG(double g) {
		longitude = g;
	}

	public boolean equals(GreatCirclePoint p) {
		String g = GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW);
		String gp = GeomUtil.decToSex(p.getG(), GeomUtil.SHELL, GeomUtil.EW);
		String l = GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS);
		String lp = GeomUtil.decToSex(p.getL(), GeomUtil.SHELL, GeomUtil.NS);
		return g.equals(gp) && l.equals(lp);
	}

	/**
	 * In nautical miles
	 *
	 * @param target the point to aim to.
	 * @return the distance, in nm.
	 */
	public double orthoDistanceBetween(GreatCirclePoint target) {
		GreatCircle gc = new GreatCircle();
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getL()), Math.toRadians(target.getG())));
		gc.calculateGreatCircle(1);
		double d = Math.toDegrees(gc.getDistance());
		return d * 60D;
	}

	/**
	 * In nautical degrees
	 *
	 * @param target the point to aim to.
	 * @return the distance, in degrees.
	 */
	public double gcDistanceBetween(GreatCirclePoint target) {
		double d = GreatCircle.getGCDistanceInDegrees(this, target);
		return d;
	}

	/**
	 * In nautical miles
	 *
	 * @param target the point to aim to.
	 * @return the distance, in nm.
	 */
	public double loxoDistanceBetween(GreatCirclePoint target) {
		GreatCircle gc = new GreatCircle();
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getL()), Math.toRadians(target.getG())));
		GreatCircle.RLData rlData = gc.calculateRhumbLine();
		double d = rlData.getdLoxo();

		return d;
	}

	public String toString() {
		String str = GeomUtil.decToSex(this.latitude, GeomUtil.SWING, GeomUtil.NS) + " / " +
						GeomUtil.decToSex(this.longitude, GeomUtil.SWING, GeomUtil.EW);
		return str;
	}

	public GreatCirclePoint degreesToRadians() {
		return new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG()));
	}

	public GreatCirclePoint radiansToDegrees() {
		return new GreatCirclePoint(Math.toDegrees(this.getL()), Math.toDegrees(this.getG()));
	}

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
		for (int i = 0; i < 10_000; i++)
			d = p1.loxoDistanceBetween(p2);
		long after = System.currentTimeMillis();
		System.out.println("10000 Loxo :" + Long.toString(after - before) + " ms.");

		before = System.currentTimeMillis();
		d = 0D;
		for (int i = 0; i < 10_000; i++)
			d = p1.orthoDistanceBetween(p2);
		after = System.currentTimeMillis();
		System.out.println("10000 Ortho:" + Long.toString(after - before) + " ms.");

		before = System.currentTimeMillis();
		d = 0D;
		for (int i = 0; i < 10_000; i++)
			d = p1.gcDistanceBetween(p2);
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
