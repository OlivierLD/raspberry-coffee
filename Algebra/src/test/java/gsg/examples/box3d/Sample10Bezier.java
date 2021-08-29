package gsg.examples.box3d;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;
import gsg.examples.box3d.fullui.ThreeDFrameWithWidgets;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Draw a 3D Bezier
 * With interactive Swing widgets.
 */
public class Sample10Bezier {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {

        final int MIN_X = -100;
        final int MAX_X =  100;
        final int MIN_Y = -100;
        final int MAX_Y =  100;
        final int MIN_Z = -100;
        final int MAX_Z =  100;

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);
        box3D.setxMin(MIN_X);
        box3D.setxMax(MAX_X);
        box3D.setyMin(MIN_Y);
        box3D.setyMax(MAX_Y);
        box3D.setzMin(MIN_Z);
        box3D.setzMax(MAX_Z);

        // Drop Ctrl Points here
        List<Bezier.Point3D> ctrlPoints = List.of(
                new Bezier.Point3D(-60, -80, 30),
                new Bezier.Point3D(60, -40, -50),
                new Bezier.Point3D(45, 30, 60),
                new Bezier.Point3D(30, 60, -30),
                new Bezier.Point3D(-60, 90, -30));
        // Generate the data, the Bezier curve.
        Bezier bezier = new Bezier(ctrlPoints);
        List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezier.getBezierPoint(t);
//            System.out.println(String.format("%.02f: %s", t, tick.toString()));
            bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        // Do something specific here, after the box drawing. What's drawn, actually.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            // Link the control points
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            VectorUtils.Vector3D from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPoints) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }

            // Plot the control points
            g2d.setColor(Color.BLUE);
            ctrlPoints.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });

            // The actual bezier
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(5));
            from = null;
            for (int i=0; i<bezierPoints.size(); i++) {
                VectorUtils.Vector3D to = bezierPoints.get(i);
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
