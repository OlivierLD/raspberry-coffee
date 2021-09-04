package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgets;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Boxes. Translate, Scale.
 * With all Swing widget to interact with the figure.
 */
public class Sample08 {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);

        // Do something specific here, after the box drawing.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            // A Cube. 4 x 4 x 4, centered on [0,0,0]
            VectorUtils.Vector3D topFrontLeft = new VectorUtils.Vector3D(-2.0, -2.0, 2.0);
            VectorUtils.Vector3D topFrontRight = new VectorUtils.Vector3D(2.0, -2.0, 2.0);
            VectorUtils.Vector3D topBackLeft = new VectorUtils.Vector3D(-2.0, -2.0, -2.0);
            VectorUtils.Vector3D topBackRight = new VectorUtils.Vector3D(2.0, -2.0, -2.0);

            VectorUtils.Vector3D bottomFrontLeft = new VectorUtils.Vector3D(-2.0, 2.0, 2.0);
            VectorUtils.Vector3D bottomFrontRight = new VectorUtils.Vector3D(2.0, 2.0, 2.0);
            VectorUtils.Vector3D bottomBackLeft = new VectorUtils.Vector3D(-2.0, 2.0, -2.0);
            VectorUtils.Vector3D bottomBackRight = new VectorUtils.Vector3D(2.0, 2.0, -2.0);

            java.util.List<VectorUtils.Vector3D> vertices = Arrays.asList(topFrontLeft,
                    topBackLeft,
                    topBackRight,
                    topFrontRight,
                    bottomFrontLeft,
                    bottomBackLeft,
                    bottomBackRight,
                    bottomFrontRight);

            g2d.setStroke(new BasicStroke(2));
            box3D.drawBox(g2d, vertices, Color.BLUE, null);

            // Small ones, 8. Scaled and Translated.
            g2d.setStroke(new BasicStroke(1));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(-1, 1, 1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(-1, -1, 1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(1, 1, 1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(1, -1, 1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(-1, 1, -1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(-1, -1, -1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(1, 1, -1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));
            box3D.drawBox(g2d, vertices.stream().map(v3d -> VectorUtils.translate(new VectorUtils.Vector3D(1, -1, -1), VectorUtils.scale(0.25, v3d))).collect(Collectors.toList()), Color.RED, new Color(0, 0, 250, 30));

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC, 24f));
            g2d.setColor(new Color(106, 86, 205));
            g2d.drawString("Drawing boxes, scaled, translated",10, 18 + 32);

        };
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D);
        frame.setVisible(true);
    }
}
