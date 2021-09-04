package gsg.examples.box3d;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgets;
import gsg.VectorUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Draw several 3D Bezier curves.
 * With interactive Swing widgets.
 */
public class Sample11 {

    private static double getTForGivenX(Bezier bezier, double startAt, double inc, double x, double precision) {
        double tForX = 0;
        for (double t=startAt; t<=1; t+=inc) {
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            if (tick.getX() > x) { // Assume that X is always growing.
                if (Math.abs(tick.getX() - x) < precision) {
                    return t;
                } else {
                    return getTForGivenX(bezier, startAt - inc, inc / 10.0, x, precision);
                }
            }
        }
        return tForX;
    }

    /**
     * @param args the command line arguments. Not used.
     */
    public static void main(String... args) {

        final int MIN_X = -10;
        final int MAX_X =  600;
        final int MIN_Y = -10;
        final int MAX_Y =  110;
        final int MIN_Z = -30;
        final int MAX_Z =  100;

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);
        box3D.setxMin(MIN_X);
        box3D.setxMax(MAX_X);
        box3D.setyMin(MIN_Y);
        box3D.setyMax(MAX_Y);
        box3D.setzMin(MIN_Z);
        box3D.setzMax(MAX_Z);

        // Drop Ctrl Points here
        List<Bezier.Point3D> ctrlPointsLivet = List.of(
                new Bezier.Point3D(0.000000, 10.000000, 75.000000),
                new Bezier.Point3D(115.714286, 116.785714, 48.571429),
                new Bezier.Point3D(377.142857, 111.428571, 48.571429),
                new Bezier.Point3D(550.0, 61.071429, 56.000000));

        List<Bezier.Point3D> ctrlPointsBow = List.of(
                new Bezier.Point3D(0.000000, 10.000000, 75.000000),
                new Bezier.Point3D(0.000000, 0.000000, -5.000000));

        List<Bezier.Point3D> ctrlPointsKeel = List.of(
                new Bezier.Point3D(0.000000, 0.000000, -5.000000),
                new Bezier.Point3D(290.357143, 0.000000, -29.642857),
                new Bezier.Point3D(550.000000, 0.000000, 5.000000));

        List<Bezier.Point3D> ctrlPointsTransom = List.of(
                new Bezier.Point3D(550.0, 61.071429, 56.000000),
                new Bezier.Point3D(550, 62, 5.642857),
                new Bezier.Point3D(550.000000, 0.000000, 5.000000));

        // Generate the data, the Bézier curves.
        Bezier bezierLivet = new Bezier(ctrlPointsLivet);
        List<VectorUtils.Vector3D> bezierPointsLivet = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierLivet.getBezierPoint(t);
            bezierPointsLivet.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierBow = new Bezier(ctrlPointsBow);
        List<VectorUtils.Vector3D> bezierPointsBow = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierBow.getBezierPoint(t);
            bezierPointsBow.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierKeel = new Bezier(ctrlPointsKeel);
        List<VectorUtils.Vector3D> bezierPointsKeel = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierKeel.getBezierPoint(t);
            bezierPointsKeel.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierTransom = new Bezier(ctrlPointsTransom);
        List<VectorUtils.Vector3D> bezierPointsTransom = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierTransom.getBezierPoint(t);
            bezierPointsTransom.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        // Maitre couple - or so
        double x = 300; // the one to find
        double t300 = getTForGivenX(bezierLivet, 0.0, 1E-1, x, 1E-4);
        Bezier.Point3D top = bezierLivet.getBezierPoint(t300);
        System.out.printf("For x=%f, t=%f - X:%f, Y:%f, Z:%f\n", x, t300, top.getX(), top.getY(), top.getZ());
        t300 = getTForGivenX(bezierKeel, 0.0, 1E-1, x, 1E-4);
        Bezier.Point3D bottom = bezierKeel.getBezierPoint(t300);
        System.out.printf("For x=%f, t=%f - X:%f, Y:%f, Z:%f\n", x, t300, bottom.getX(), bottom.getY(), bottom.getZ());

        List<Bezier.Point3D> ctrlPointsMC = List.of(
                new Bezier.Point3D(x, top.getY(), top.getZ()),
                new Bezier.Point3D(x, top.getY(), bottom.getZ()),
                new Bezier.Point3D(x, bottom.getY(), bottom.getZ()));
        Bezier bezierMC = new Bezier(ctrlPointsMC);
        List<VectorUtils.Vector3D> bezierPointsMC = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierMC.getBezierPoint(t);
            bezierPointsMC.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        // Do something specific here, after the box drawing. What's drawn, actually.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            // Link the control points
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            VectorUtils.Vector3D from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPointsLivet) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }
            from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPointsBow) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }
            from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPointsKeel) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }
            from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPointsTransom) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }
            from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPointsMC) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }

            // Plot the control points
            g2d.setColor(Color.BLUE);
            ctrlPointsLivet.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });
            ctrlPointsBow.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });
            ctrlPointsKeel.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });
            ctrlPointsTransom.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });
            ctrlPointsMC.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });

            // The actual bezier
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            from = null;
            for (int i=0; i<bezierPointsLivet.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsLivet.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            for (int i=0; i<bezierPointsBow.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsBow.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            for (int i=0; i<bezierPointsKeel.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsKeel.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            for (int i=0; i<bezierPointsTransom.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsTransom.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            for (int i=0; i<bezierPointsMC.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsMC.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
        };
        // Invoke the above
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D);
        frame.setVisible(true);
    }
}
