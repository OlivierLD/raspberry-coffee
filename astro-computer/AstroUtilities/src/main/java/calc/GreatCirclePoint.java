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
        return GreatCircle.getGCDistanceInDegrees(this, target);
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
        return rlData.getdLoxo();
    }

    public String toString() {
        return String.format("%s / %s",
                GeomUtil.decToSex(this.latitude, GeomUtil.SWING, GeomUtil.NS),
                GeomUtil.decToSex(this.longitude, GeomUtil.SWING, GeomUtil.EW));
    }

    public GreatCirclePoint degreesToRadians() {
        return new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG()));
    }

    public GreatCirclePoint radiansToDegrees() {
        return new GreatCirclePoint(Math.toDegrees(this.getL()), Math.toDegrees(this.getG()));
    }

}
