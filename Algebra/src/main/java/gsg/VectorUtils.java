package gsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic vector manipulation, translated from Python.
 * <br/>
 * Static methods.
 * <br/>
 * <i>C'est <b>MOI</b> qui l'ai fait.</i>
 */
public class VectorUtils {

    /**
     * A 2D Vector
     */
    public static class Vector2D {
        private double x;
        private double y;

        public Vector2D() {
        }

        public Vector2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Vector2D x(double x) {
            this.x = x;
            return this;
        }

        public Vector2D y(double y) {
            this.y = y;
            return this;
        }

        public double getX() {
            return this.x;
        }

        // For Polar coordinates
        public double getLength() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        // For Polar coordinates
        public double getAngle() {
            return this.y;
        }

        @Override
        public String toString() {
            return (String.format("X:%f, Y:%f", this.x, this.y));
        }
    }

    /**
     * A 3D Vector
     */
    public static class Vector3D {
        private double x;
        private double y;
        private double z;

        public Vector3D() {
        }

        public Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3D(double[] coord) {
            assert(coord.length == 3);
            this.x = coord[0];
            this.y = coord[1];
            this.z = coord[2];
        }

        public Vector3D x(double x) {
            this.x = x;
            return this;
        }

        public Vector3D y(double y) {
            this.y = y;
            return this;
        }

        public Vector3D z(double z) {
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
            return (String.format("X:%f, Y:%f, Z:%f", this.x, this.y, this.z));
        }
    }

    //    def subtract(v1,v2):
    //        return (v1[0] - v2[0], v1[1] - v2[1])
    public static Vector2D subtract(Vector2D one, Vector2D two) {
        return new Vector2D(one.getX() - two.getX(), one.getY() - two.getY());
    }

    public static Vector3D subtract(Vector3D one, Vector3D two) {
        return new Vector3D(one.getX() - two.getX(), one.getY() - two.getY(), one.getZ() - two.getZ());
    }

    //    def add(*vectors):
    //        return (sum([v[0] for v in vectors]), sum([v[1] for v in vectors]))
    public static Vector2D add2D(List<Vector2D> vectors) {
        return new Vector2D(vectors.stream().mapToDouble(Vector2D::getX).sum(),
                            vectors.stream().mapToDouble(Vector2D::getY).sum());
    }

    public static Vector3D add3D(List<Vector3D> vectors) {
        return new Vector3D(vectors.stream().mapToDouble(Vector3D::getX).sum(),
                vectors.stream().mapToDouble(Vector3D::getY).sum(),
                vectors.stream().mapToDouble(Vector3D::getZ).sum());
    }

    public static Vector2D multiply(Vector2D v, double mul) {
        return new Vector2D(v.getX() * mul, v.getY() * mul);
    }

    public static Vector3D multiply(Vector3D v, double mul) {
        return new Vector3D(v.getX() * mul, v.getY() * mul, v.getZ() * mul);
    }

    //    def length(v):
    //        return sqrt(v[0]**2 + v[1]**2)
    public static double length(Vector2D v) {
        return Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getY(), 2));
    }

    public static double length(Vector3D v) {
        return Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getY(), 2) + Math.pow(v.getZ(), 2));
    }

    //    def distance(v1,v2):
    //        return length(subtract(v1,v2))
    public static double distance(Vector2D v1, Vector2D v2) {
        return length(subtract(v1, v2));
    }

    //    def perimeter(vectors):
    //    distances = [distance(vectors[i], vectors[(i+1)%len(vectors)])
    //        for i in range(0,len(vectors))]
    //        return sum(distances)
    public static double perimeter(List<Vector2D> vectors) {
        double dist = 0d;
        for (int i = 0; i < vectors.size(); i++) {
            dist += distance(vectors.get(i), vectors.get((i + 1) % vectors.size()));  // Smart stuff!
        }
        return dist;
    }

    //    def scale(scalar,v):
    //        return (scalar * v[0], scalar * v[1])
    public static Vector2D scale(double scalar, Vector2D v) {
        return new Vector2D(scalar * v.getX(), scalar * v.getY());
    }

    public static Vector3D scale(double scalar, Vector3D v) {
        return new Vector3D(scalar * v.getX(), scalar * v.getY(), scalar * v.getZ());
    }

    //    def to_cartesian(polar_vector):
    //        length, angle = polar_vector[0], polar_vector[1]
    //        return (length*cos(angle), length*sin(angle))
    /**
     * @param length as you can guess
     * @param angle in <u><i>Radians</i></u>, thank you.
     * @return the cartesian equivalent of the polar coordinates
     */
    public static Vector2D toCartesian(double length, double angle) {
        return new Vector2D(length * Math.cos(angle), length * Math.sin(angle));
    }
    public static Vector2D toCartesian(Vector2D vector) {
        return new Vector2D(vector.getLength() * Math.cos(vector.getAngle()), vector.getLength() * Math.sin(vector.getAngle()));
    }

    //    def translate(translation, vectors):
    //            return [add(translation, v) for v in vectors]
    public static Vector2D translate(Vector2D translation, Vector2D vector) {
        return translate(translation, Arrays.asList(vector)).stream().findFirst().get();
    }

    public static Vector3D translate(Vector3D translation, Vector3D vector) {
        return translate(translation, Arrays.asList(vector)).stream().findFirst().get();
    }

    public static List<Vector2D> translate(Vector2D translation, List<Vector2D> vectors) {
        List<Vector2D> translated = new ArrayList<>();
        vectors.forEach(v -> translated.add(add2D(Arrays.asList(translation, v))));
        return translated;
    }

    public static List<Vector3D> translate(Vector3D translation, List<Vector3D> vectors) {
        List<Vector3D> translated = new ArrayList<>();
        vectors.forEach(v -> translated.add(add3D(Arrays.asList(translation, v))));
        return translated;
    }

    //    def to_polar(vector):
    //        x, y = vector[0], vector[1]
    //        angle = atan2(y,x)
    //        return (length(vector), angle)
    public static Vector2D toPolar(Vector2D vector) {
        double angle = Math.atan2(vector.getY(), vector.getX()); // Warning! Y first, then X
        return new Vector2D(length(vector), angle);
    }

    //    def rotate(angle, vectors):
    //            polars = [to_polar(v) for v in vectors]
    //            return [to_cartesian((l, a+angle)) for l,a in polars]
    public static Vector2D rotate(double angle, Vector2D vector) {
        return rotate(angle, Arrays.asList(vector)).stream().findFirst().get();
    }

    public static List<Vector2D> rotate(double angle, List<Vector2D> vectors) {
        List<Vector2D> rotated = new ArrayList<>();
        vectors.forEach(v -> {
            Vector2D polar = toPolar(v);
            rotated.add(toCartesian(polar.getLength(), polar.getAngle() + angle));
        });
        return rotated;
    }

    /**
     * See https://stackoverflow.com/questions/14607640/rotating-a-vector-in-3d-space
     *
     * @param original
     * @param onX in radians, trigonometric way (counter-clockwise)
     * @param onY in radians, trigonometric way (counter-clockwise)
     * @param onZ in radians, trigonometric way (counter-clockwise)
     * @return the rotated vector.
     */
    public static Vector3D rotate(Vector3D original, double onX, double onY, double onZ) {
        double x = original.getX();
        double y = original.getY();
        double z = original.getZ();

        /*
        Around Z-axis:
    |cos θ   −sin θ   0| |x|   |x cos θ − y sin θ|   |x'|
    |sin θ    cos θ   0| |y| = |x sin θ + y cos θ| = |y'|
    |  0       0      1| |z|   |        z        |   |z'|
         */

        double newX1 = (x * Math.cos(onZ)) - (y * Math.sin(onZ));
        double newY1 = (x * Math.sin(onZ)) + (y * Math.cos(onZ));
        double newZ1 = z;

        /*
        Around Y-axis:
    | cos θ    0   sin θ| |x|   | x cos θ + z sin θ|   |x'|
    |   0      1       0| |y| = |         y        | = |y'|
    |−sin θ    0   cos θ| |z|   |−x sin θ + z cos θ|   |z'|
         */
        double newX2 = (newX1 * Math.cos(onY)) + (newZ1 * Math.sin(onY));
        double newY2 = newY1;
        double newZ2 = (-newX1 * Math.sin(onY)) + (newZ1 * Math.cos(onY));

        /*
        Around X-axis:
    |1     0           0| |x|   |        x        |   |x'|
    |0   cos θ    −sin θ| |y| = |y cos θ − z sin θ| = |y'|
    |0   sin θ     cos θ| |z|   |y sin θ + z cos θ|   |z'|
         */
        double newX3 = newX2;
        double newY3 = (newY2 * Math.cos(onX)) - (newZ2 * Math.sin(onX));
        double newZ3 = (newY2 * Math.sin(onX)) + (newZ2 * Math.cos(onX));

        return new Vector3D(newX3, newY3, newZ3);
    }

    public static Vector3D findMiddle(Vector3D one, Vector3D two) {
        Vector3D midVector = new Vector3D((two.getX() - one.getX()) / 2,
                (two.getY() - one.getY()) / 2,
                (two.getZ() - one.getZ()) / 2);
        return add3D(Arrays.asList(one, midVector));
    }

    /*
    def dot(u,v):
        return sum([coord1 * coord2 for coord1, coord2 in zip(u,v)])

    where zip() :
    a = ("John", "Charles", "Mike")
    b = ("Jenny", "Christy", "Monica")
    x = zip(a, b)
    # use the tuple() function to display a readable version of the result:
    print(tuple(x))
    >>> (('John', 'Jenny'), ('Charles', 'Christy'), ('Mike', 'Monica'))

     */

    /*
     * Dot product (in French: produit scalaire):
     * - if positive: vectors are convergent
     * - if negative: vectors are divergent
     * - if zero: vectors are perpendicular
     */
    public static double dot(Vector2D v1, Vector2D v2) {
        return (v1.getX() * v2.getX()) + (v1.getY() * v2.getY());
    }

    public static double dot(Vector3D v1, Vector3D v2) {
        return (v1.getX() * v2.getX()) + (v1.getY() * v2.getY()) + (v1.getZ() * v2.getZ());
    }

    public static double angleBetween(Vector2D v1, Vector2D v2) {
        return Math.acos(dot(v1, v2) / (length(v1) * length(v2)));
    }

    public static double angleBetween(Vector3D v1, Vector3D v2) {
        return Math.acos(dot(v1, v2) / (length(v1) * length(v2)));
    }

    // Cross Product (produit vectoriel)
    // Dim 2 vectors
    // See https://pythonexamples.org/numpy-cross-product/
    public static double cross(Vector2D v1, Vector2D v2) {
        return ((v1.getX() * v2.getY()) - (v1.getY() * v2.getX()));
    }

    // Dim 3 vectors
//    def cross(u, v):
//        ux,uy,uz = u
//        vx,vy,vz = v
//        return (uy*vz - uz*vy, uz*vx - ux*vz, ux*vy - uy*vx)
    public static Vector3D cross(Vector3D v1, Vector3D v2) {
        return new Vector3D(
                (v1.getY() * v2.getZ()) - (v1.getZ() * v2.getY()),
                (v1.getZ() * v2.getX()) - (v1.getX() * v2.getZ()),
                (v1.getX() * v2.getY()) - (v1.getY() * v2.getX()));
    }

    public static double component(Vector2D v, Vector2D direction) {
        return (dot(v, direction) / length(direction));
    }

    /**
     * Same orientation as prm vector, but of length 1
     * @param v
     * @return the vector, of norm 1
     */
    public static Vector2D unit(Vector2D v) {
        return scale(1. / length(v), v);
    }

    public static Vector3D unit(Vector3D v) {
        return scale(1. / length(v), v);
    }

    // TODO The same with 3D vectors.
    public static class GraphicRange {
        private double minX;
        private double maxX;
        private double minY;
        private double maxY;

        public GraphicRange() {
            this.minX = 0;
            this.maxX = 0;
            this.minY = 0;
            this.maxY = 0;
        }

        public double getMinX() {
            return minX;
        }

        public void setMinX(double minX) {
            this.minX = minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public void setMaxX(double maxX) {
            this.maxX = maxX;
        }

        public double getMinY() {
            return minY;
        }

        public void setMinY(double minY) {
            this.minY = minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public void setMaxY(double maxY) {
            this.maxY = maxY;
        }

        public GraphicRange minX(double minX) {
            this.minX = minX;
            return this;
        }

        public GraphicRange maxX(double maxX) {
            this.maxX = maxX;
            return this;
        }

        public GraphicRange minY(double minY) {
            this.minY = minY;
            return this;
        }

        public GraphicRange maxY(double maxY) {
            this.maxY = maxY;
            return this;
        }
    }

    public static GraphicRange findGraphicRange(Vector2D v, Vector2D... more) {
        GraphicRange graphicRange = new GraphicRange()
                .minX(Double.MAX_VALUE)
                .maxX(-Double.MAX_VALUE)
                .minY(Double.MAX_VALUE)
                .maxY(-Double.MAX_VALUE);
        graphicRange.setMinX(Math.min(graphicRange.getMinX(), v.getX()));
        graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), v.getX()));
        graphicRange.setMinY(Math.min(graphicRange.getMinY(), v.getY()));
        graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), v.getY()));
        if (more != null) {
            for (Vector2D vv : more) {
                graphicRange.setMinX(Math.min(graphicRange.getMinX(), vv.getX()));
                graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), vv.getX()));
                graphicRange.setMinY(Math.min(graphicRange.getMinY(), vv.getY()));
                graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), vv.getY()));
            }
        }
        return graphicRange;
    }

    public static GraphicRange findGraphicRange(double[] x, double[] y) {
        assert (x.length == y.length);
        GraphicRange graphicRange = new GraphicRange()
                .minX(Double.MAX_VALUE)
                .maxX(-Double.MAX_VALUE)
                .minY(Double.MAX_VALUE)
                .maxY(-Double.MAX_VALUE);
        for (int i = 0; i < x.length; i++) {
            graphicRange.setMinX(Math.min(graphicRange.getMinX(), x[i]));
            graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), x[i]));
            graphicRange.setMinY(Math.min(graphicRange.getMinY(), y[i]));
            graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), y[i]));
        }
        return graphicRange;
    }

    public static GraphicRange findGraphicRange(List<Double> x, List<Double> y) {
        assert (x.size() == y.size());
        GraphicRange graphicRange = new GraphicRange()
                .minX(Double.MAX_VALUE)
                .maxX(-Double.MAX_VALUE)
                .minY(Double.MAX_VALUE)
                .maxY(-Double.MAX_VALUE);
        for (int i = 0; i < x.size(); i++) {
            graphicRange.setMinX(Math.min(graphicRange.getMinX(), x.get(i)));
            graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), x.get(i)));
            graphicRange.setMinY(Math.min(graphicRange.getMinY(), y.get(i)));
            graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), y.get(i)));
        }
        return graphicRange;
    }

    public static GraphicRange findGraphicRange(List<Vector2D> data) {
        GraphicRange graphicRange = new GraphicRange()
                .minX(Double.MAX_VALUE)
                .maxX(-Double.MAX_VALUE)
                .minY(Double.MAX_VALUE)
                .maxY(-Double.MAX_VALUE);
        data.forEach(v -> {
            graphicRange.setMinX(Math.min(graphicRange.getMinX(), v.getX()));
            graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), v.getX()));
            graphicRange.setMinY(Math.min(graphicRange.getMinY(), v.getY()));
            graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), v.getY()));
        });
        return graphicRange;
    }

    public static GraphicRange findGraphicRanges(List<List<Vector2D>> data) {
        GraphicRange graphicRange = new GraphicRange()
                .minX(Double.MAX_VALUE)
                .maxX(-Double.MAX_VALUE)
                .minY(Double.MAX_VALUE)
                .maxY(-Double.MAX_VALUE);
        data.forEach(vList -> {
                    vList.forEach(v -> {
                        graphicRange.setMinX(Math.min(graphicRange.getMinX(), v.getX()));
                        graphicRange.setMaxX(Math.max(graphicRange.getMaxX(), v.getX()));
                        graphicRange.setMinY(Math.min(graphicRange.getMinY(), v.getY()));
                        graphicRange.setMaxY(Math.max(graphicRange.getMaxY(), v.getY()));
                    });
        });
        return graphicRange;
    }

}
