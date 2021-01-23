package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;
import gsg.examples.box3d.fullui.ThreeDFrameWithWidgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Draw a 3D spring
 * With interactive Swing widgets.
 */
public class Sample07 {

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

        final int SPRING_RADIUS = 50;

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);
        box3D.setxMin(MIN_X);
        box3D.setxMax(MAX_X);
        box3D.setyMin(MIN_Y);
        box3D.setyMax(MAX_Y);
        box3D.setzMin(MIN_Z);
        box3D.setzMax(MAX_Z);

        double deltaZ = MAX_Z - MIN_Z;

        // Generate the data, the spring.
        List<VectorUtils.Vector3D> spring = new ArrayList<>();
        int nbPoints = 1_000;
        int nbLoops = 5;
        for (int i=0; i<=nbPoints; i++) {
            double z = MIN_Z + (i * deltaZ / nbPoints);
            double theta = (i % (nbPoints / nbLoops)) * (360d / (nbPoints / nbLoops));
//            System.out.printf("i: %d, theta: %.02f\n", i, theta);
            double x = SPRING_RADIUS * Math.sin(Math.toRadians(theta));
            double y = SPRING_RADIUS * Math.cos(Math.toRadians(theta));
            spring.add(new VectorUtils.Vector3D(x, y, z));
        }

        // Do something specific here, after the box drawing. What's drawn.
        Consumer<Graphics2D> afterDrawer = g2d -> {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(5));
            VectorUtils.Vector3D from = null;
            for (int i=0; i<spring.size(); i++) {
                VectorUtils.Vector3D to = spring.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
        };
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D);
        frame.setVisible(true);
    }
}
