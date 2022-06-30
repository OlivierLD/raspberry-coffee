package gsg.examples.wb.bezier;

import bezier.Bezier;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * No GUI
 */
public class BasicBezierTest001 {
    private final static List<Bezier.Point3D> ctrlPoints = List.of( // Just one. Not enough.
            new Bezier.Point3D(-50, 30, 0));

    @Test
    public void expectException() {
        Bezier bezier = new Bezier(ctrlPoints);
        for (double t=0.0; t<=1.0; t+=0.1) {
            try {
                Bezier.Point3D pt = bezier.recurse(ctrlPoints, t);
                System.out.printf("For t=%.02f => x: %.02f, y: %.02f, z: %.02f\n", t, pt.getX(), pt.getY(), pt.getZ());
                fail("Bezier.BezierException should have been thrown.");
            } catch (Bezier.BezierException be) {
//                be.printStackTrace();
                assertTrue("Unexpected Exception type", be instanceof Bezier.BezierException);
                break;
            }
        }
    }

//    public static void main(String... args) {
//        Bezier bezier = new Bezier(ctrlPoints);
//        for (double t=0.0; t<=1.0; t+=0.1) {
//            try {
//                Bezier.Point3D pt = bezier.recurse(ctrlPoints, t);
//                System.out.printf("For t=%.02f => x: %.02f, y: %.02f, z: %.02f\n", t, pt.getX(), pt.getY(), pt.getZ());
//            } catch (Bezier.BezierException be) {
//                be.printStackTrace();
//                break;
//            }
//        }
//    }
}
