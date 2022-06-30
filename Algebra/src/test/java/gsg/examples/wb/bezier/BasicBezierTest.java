package gsg.examples.wb.bezier;

import bezier.Bezier;

import java.util.List;
/*
 * No GUI
 */
public class BasicBezierTest {
    private final static List<Bezier.Point3D> ctrlPoints = List.of(
            new Bezier.Point3D(-60, -20, 0),
            new Bezier.Point3D(0, 40, 0),
            new Bezier.Point3D(20, -40, 0),
//            new Bezier.Point3D(60, -20, 0));
//            new Bezier.Point3D(-30, -30, 0));
            new Bezier.Point3D(-50, 30, 0));

    public static void main(String... args) {
        Bezier bezier = new Bezier(ctrlPoints);
        for (double t=0.0; t<=1.0; t+=0.1) {
            try {
                Bezier.Point3D pt = bezier.recurse(ctrlPoints, t);
                System.out.printf("For t=%.02f => x: %.02f, y: %.02f, z: %.02f\n", t, pt.getX(), pt.getY(), pt.getZ());
            } catch (Bezier.BezierException be) {
                be.printStackTrace();
                break;
            }
        }
    }
}
