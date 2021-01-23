package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;
import gsg.examples.box3d.fullui.ThreeDFrameWithWidgets;

import java.awt.*;
import java.util.function.Consumer;

/**
 * SurroundingBox'es
 * With all Swing widget to interact with the figure.
 * Adding 2 vectors tip-to-tail.
 */
public class Sample06 {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);

        // Do something specific here, after the box drawing. What's drawn.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            double[] spatialPointOne = new double[] { 1d, 1d, 1.5d };
            double[] spatialPointTwo = new double[] { 2d, 3d, -0.5d };
            double[] spatialPointThree = new double[] { -2d, -2d, -1.5d };

            // Draw surrounding boxes
            g2d.setColor(Color.BLUE);
            // Dotted lines for the cube
            g2d.setStroke(new BasicStroke(1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[] { 2f, 0f, 2f },
                    2f));

            box3D.drawSurroundingBox(g2d, spatialPointOne, spatialPointTwo);
            g2d.setStroke(new BasicStroke(2));
//            box3D.drawSegment(g2d, spatialPointOne, spatialPointTwo);
            box3D.drawArrow(g2d, spatialPointOne, spatialPointTwo);
            VectorUtils.Vector3D middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointOne),
                    new VectorUtils.Vector3D(spatialPointTwo));
            box3D.plotStringAt(g2d, "Vector A", middle, true);

            g2d.setColor(Color.RED);
            // Dotted lines for the cube
            g2d.setStroke(new BasicStroke(1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[] { 4f, 0f, 5f },
                    2f));
            box3D.drawSurroundingBox(g2d, spatialPointTwo, spatialPointThree);
            g2d.setStroke(new BasicStroke(2));
//            box3D.drawSegment(g2d, spatialPointTwo, spatialPointThree);
            box3D.drawArrow(g2d, spatialPointTwo, spatialPointThree);
            middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointTwo),
                    new VectorUtils.Vector3D(spatialPointThree));
            box3D.plotStringAt(g2d, "Vector B", middle, true);

            // Draw sum
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.GREEN);
//            box3D.drawSegment(g2d, spatialPointOne, spatialPointThree);
            box3D.drawArrow(g2d, spatialPointOne, spatialPointThree);
            middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointOne),
                    new VectorUtils.Vector3D(spatialPointThree));
            box3D.plotStringAt(g2d, "(A+B)", middle, true);

            g2d.setColor(Color.BLUE);
            g2d.drawString("Vector (A)",10, 18 + 16);
            g2d.setColor(Color.RED);
            g2d.drawString("Vector (B)",10, 18 + 16 + 16);
            g2d.setColor(Color.GREEN);
            g2d.drawString("Vector (A+B)",10, 18 + 16 + 16 + 16);
        };
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D);
        frame.setVisible(true);
    }
}
