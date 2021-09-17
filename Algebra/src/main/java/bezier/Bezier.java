package bezier;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Good resource at https://javascript.info/bezier-curve

public class Bezier {
    public static class Point3D {
        private double x, y, z;

        public Point3D() {
        }

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point3D x(double x) {
            this.x = x;
            return this;
        }

        public Point3D y(double y) {
            this.y = y;
            return this;
        }

        public Point3D z(double z) {
            this.z = z;
            return this;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }

        @Override
        public String toString() {
            return String.format("X: %f, Y:%f, Z: %f", this.x, this.y, this.z);
        }
    }

    private List<Point3D> controlPoints;

    public Bezier() {
    }

    public Bezier(List<Point3D> controlPoints) {
        this.controlPoints = controlPoints;
    }

    public Bezier(Point3D... controlPoints) {
        this.controlPoints = Arrays.asList(controlPoints);
    }

    public List<Point3D> getControlPoints() {
        return this.controlPoints;
    }

    public void addControlPoint(Point3D ctrlPoint) {
        if (this.controlPoints == null) {
            this.controlPoints = new ArrayList<>();
        }
        this.controlPoints.add(ctrlPoint);
    }

    public void removeControlPoint(Point3D ctrlPoint) {
        if (this.controlPoints.contains(ctrlPoint)) {
            this.controlPoints.remove(ctrlPoint);
        }
    }

    private Point3D withProgressT(Point3D from, Point3D to, double t) {
        double deltaX = to.getX() - from.getX();
        double deltaY = to.getY() - from.getY();
        double deltaZ = to.getZ() - from.getZ();

        return new Point3D()
                .x(from.getX() + (deltaX * t))
                .y(from.getY() + (deltaY * t))
                .z(from.getZ() + (deltaZ * t));
    }

    public Point3D recurse(List<Point3D> ctrl, double t) {
//        System.out.printf("\trecurse -> %d ctrl points.\n", ctrl.size());
        if (ctrl.size() > 3) { // Recurse until size = 3
            List<Point3D> inside = new ArrayList<>();
            for (int ptIdx = 0; ptIdx < ctrl.size() - 1; ptIdx++) {
                Point3D insider = withProgressT(ctrl.get(ptIdx), ctrl.get(ptIdx + 1), t);
                inside.add(insider);
            }
            return recurse(inside, t);
        } else if (ctrl.size() == 2) { // straight forward
            Point3D tPoint = withProgressT(ctrl.get(0), ctrl.get(1), t);
            return tPoint;
        } else {
            Point3D one = withProgressT(ctrl.get(0), ctrl.get(1), t);
            Point3D two = withProgressT(ctrl.get(1), ctrl.get(2), t);
            Point3D tPoint = withProgressT(one, two, t);
            return tPoint;
        }
    }

    /**
     *
     * @param t in [0..1]
     * @return
     */
    public Point3D getBezierPoint(double t) {
        if (this.controlPoints == null) {
            throw new RuntimeException("There is no control point in this Bezier!");
        }
        if (this.controlPoints.size() < 2) {
            throw new RuntimeException(String.format("Only %d control point(s) in this Bezier, need at least 2", this.controlPoints.size()));
        }
        return recurse(this.controlPoints, t);
    }

    public enum Coordinate {
        X, Y, Z
    }

    public double[] getMinMax(Coordinate coordinate, double deltaT) {
        double[] minMax = { Double.MAX_VALUE, -Double.MAX_VALUE };
        for (double t=0; t<=1.0; t+=deltaT) {
            Point3D tick = this.getBezierPoint(t);
//            System.out.println(String.format("%.02f: %s", t, tick.toString()));
            switch (coordinate) {
                case X:
                    minMax[0] = Math.min(minMax[0], tick.getX());
                    minMax[1] = Math.max(minMax[1], tick.getX());
                    break;
                case Y:
                    minMax[0] = Math.min(minMax[0], tick.getY());
                    minMax[1] = Math.max(minMax[1], tick.getY());
                    break;
                case Z:
                    minMax[0] = Math.min(minMax[0], tick.getZ());
                    minMax[1] = Math.max(minMax[1], tick.getZ());
                    break;
                default:
                    break;
            }
        }
        return minMax;
    }

    public double getTForGivenX(double startAt, double inc, double x, double precision) throws TooDeepRecursionException {
        return getTForGivenX(startAt, inc, x, precision, true);
    }
    public double getTForGivenX(double startAt, double inc, double x, double precision, boolean increasing) throws TooDeepRecursionException {
        return getTForGivenVal(startAt, inc, x, precision, Coordinate.X, increasing);
    }
    public double getTForGivenY(double startAt, double inc, double y, double precision) throws TooDeepRecursionException {
        return getTForGivenY(startAt, inc, y, precision, true);
    }
    public double getTForGivenY(double startAt, double inc, double y, double precision, boolean increasing) throws TooDeepRecursionException {
        return getTForGivenVal(startAt, inc, y, precision, Coordinate.Y, increasing);
    }
    public double getTForGivenZ(double startAt, double inc, double z, double precision) throws TooDeepRecursionException {
        return getTForGivenZ(startAt, inc, z, precision, true);
    }
    public double getTForGivenZ(double startAt, double inc, double z, double precision, boolean increasing) throws TooDeepRecursionException {
        return getTForGivenVal(startAt, inc, z, precision, Coordinate.Z, increasing);
    }

    private static NumberFormat formatter = new DecimalFormat("0.#####E0");

    public static class TooDeepRecursionException extends Exception {
        public TooDeepRecursionException() {
            super();
        }
        public TooDeepRecursionException(String message) {
            super(message);
        }
    }

    /**
     * Warning: this assumes that X is constantly growing/increasing
     *
     * @param startAt Value for t
     * @param inc
     * @param val the one we look the t for
     * @param precision return when diff lower than precision
     * @param coordinate X, Y, Z
     * @param increasing default true, set to false if the value we look for goes high to low.
     * @return the t
     */
    private double getTForGivenVal(double startAt,
                                   double inc,
                                   double val,
                                   double precision,
                                   Coordinate coordinate,
                                   boolean increasing) throws TooDeepRecursionException {

//        System.out.println("getTForGivenVal: inc=" + formatter.format(inc));
        if (inc < 1e-10) { // TODO This is a bug, needs a fix...
//            System.out.println("Exiting getTForGivenVal: inc=" + formatter.format(inc));
            throw new TooDeepRecursionException(String.format("Too Deep Recursion: inc = %s", formatter.format(inc)));
//            return -1;
        }

        if (this.controlPoints.size() < 2) {
            return -1;
        }
        if (this.controlPoints.size() == 2) {
            double val1 = this.controlPoints.get(0).getX();
            double val2 = this.controlPoints.get(1).getX();
            if (coordinate == Coordinate.Y) {
                val1 = this.controlPoints.get(0).getY();
                val2 = this.controlPoints.get(1).getY();
            } else if (coordinate == Coordinate.Z) {
                val1 = this.controlPoints.get(0).getZ();
                val2 = this.controlPoints.get(1).getZ();
            }
            if (val < Math.min(val1, val2) || val > Math.max(val1, val2)) {
                return -1;
            } else {
                return ((val - val1) / (val2 - val1));
            }
        }
        double[] minMax = this.getMinMax(coordinate, precision); // precision? or other value?

        if (val < minMax[0] || val > minMax[1]) {
            return -1; // Not in range!
        }
        for (double t=startAt; t<=1+inc; t+=inc) {  // TODO verify the limit (of a double...)
            Bezier.Point3D tick = this.getBezierPoint(t);
            double tickVal = tick.getX();
            if (coordinate == Coordinate.Y) {
                tickVal = tick.getY();
            } else if (coordinate == Coordinate.Z) {
                tickVal = tick.getZ();
            }
            if ((increasing && tickVal >= val) || (!increasing && tickVal <= val)) {
                if (Math.abs(tickVal - val) < precision) {
                    return t;
                } else {
                    double newStartAt = startAt - inc;
                    // newStartAt < 0: more precision required
//                    if (newStartAt < 0 && t == 0) {
//                         return t;
//                    }
                    return getTForGivenVal(newStartAt < 0 ? 0 : newStartAt, inc / 10.0, val, precision, coordinate, increasing);
                }
            }
        }
        return -1; // means not found...
    }

    // Quick example
    public static void main(String... args) {

//        Bezier bezier = new Bezier(
//                new Bezier.Point3D(0.000000, 0.000000, 75.000000),
//                new Bezier.Point3D(0.000000, 0.000000, -5.000000));

//        Bezier bezier = new Bezier(
//                new Bezier.Point3D(-5.000000, 0.000000, 75.000000),
//                new Bezier.Point3D(10.000000, 0.000000, -5.000000));

        Bezier bezier = new Bezier(
//                new Point3D(0, 0, 0),
//                new Point3D(20, 30, 10),
//                new Point3D(15, 35, 20),
//                new Point3D(10, 20, 0)

                new Point3D(-265, 17.678544, 69.790446),
                new Point3D(-265, 17.678544, -5.831876),
                new Point3D(-265, 0, -5.831876)
        );



//        Bezier bezier = new Bezier(
//                new Bezier.Point3D(0.000000 - 275, 0.000000, 75.000000),
//                new Bezier.Point3D(0.000000 - 275, 21.428571, 68.928571),
//                new Bezier.Point3D(69.642857 - 275, 86.785714, 47.500000),
//                new Bezier.Point3D(272.142857 - 275, 129.642857, 45.357143),
//                new Bezier.Point3D(550.000000 - 275, 65.0, 56.000000));

        for (double t=0; t<=1.001; t+=0.01) { // 1.001 ? WTF!
            Point3D tick = bezier.getBezierPoint(t);
            System.out.println(String.format("t: %.02f => %s", t, tick.toString()));
        }

        System.out.println();

        double[] minMax = bezier.getMinMax(Coordinate.X, 1e-4);
        System.out.printf("MinX:%f, MaxX:%f\n", minMax[0], minMax[1]);
        minMax = bezier.getMinMax(Coordinate.Y, 1e-4);
        System.out.printf("MinY:%f, MaxY:%f\n", minMax[0], minMax[1]);
        minMax = bezier.getMinMax(Coordinate.Z, 1e-4);
        System.out.printf("MinZ:%f, MaxZ:%f\n", minMax[0], minMax[1]);

        System.out.println();

        double xToFind = 12; // 120;
        double t = 0;
        try {
            t = bezier.getTForGivenX(0, 1E-1, xToFind, 1E-4);
        } catch (Bezier.TooDeepRecursionException tdre) {
            tdre.printStackTrace();
        }
        if (t < 0) {
            System.out.printf("No X=%f in this bezier.\n", xToFind);
        } else {
            System.out.printf("For X=%f, found t: %f -> %s.\n", xToFind, t, bezier.getBezierPoint(t));
        }

        double yToFind = 28; // 120;
        try {
            t = bezier.getTForGivenY(0, 1E-1, yToFind, 1E-4);
        } catch (Bezier.TooDeepRecursionException tdre) {
            tdre.printStackTrace();
        }
        if (t < 0) {
            System.out.printf("No Y=%f in this bezier.\n", yToFind);
        } else {
            System.out.printf("For Y=%f, found t: %f -> %s.\n", yToFind, t, bezier.getBezierPoint(t));
        }

        double zToFind = 0; // 10; // 120;
        try {
            t = bezier.getTForGivenZ(0, 1E-1, zToFind, 1E-4, (bezier.getBezierPoint(0).getZ() < bezier.getBezierPoint(1).getZ()));
        } catch (Bezier.TooDeepRecursionException tdre) {
            tdre.printStackTrace();
        }
        if (t < 0) {
            System.out.printf("No Z=%f in this bezier.\n", zToFind);
        } else {
            System.out.printf("For Z=%f, found t: %f -> %s.\n", zToFind, t, bezier.getBezierPoint(t));
        }
    }
}
