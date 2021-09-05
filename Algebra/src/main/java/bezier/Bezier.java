package bezier;

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

    // Quick example
    public static void main(String... args) {

        Bezier bezier = new Bezier(
                new Point3D(0, 0, 0),
                new Point3D(20, 30, 10),
                new Point3D(15, 35, 20),
                new Point3D(10, 20, 0)
        );

//        Bezier bezier = new Bezier(
//                new Bezier.Point3D(0.000000 - 275, 0.000000, 75.000000),
//                new Bezier.Point3D(0.000000 - 275, 21.428571, 68.928571),
//                new Bezier.Point3D(69.642857 - 275, 86.785714, 47.500000),
//                new Bezier.Point3D(272.142857 - 275, 129.642857, 45.357143),
//                new Bezier.Point3D(550.000000 - 275, 65.0, 56.000000));

        for (double t=0; t<=1.0; t+=0.1) {
            Point3D tick = bezier.getBezierPoint(t);
            System.out.println(String.format("%.02f: %s", t, tick.toString()));
        }
    }
}
