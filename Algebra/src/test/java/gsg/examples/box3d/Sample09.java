package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;
import gsg.WaveFrontUtils;
import gsg.examples.box3d.fullui.ThreeDFrameWithWidgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * WaveFront, revival!
 * With all Swing widget to interact with the figure.
 */
public class Sample09 {

    private final static String[] OBJ_FILE_NAME = new String[] {
            "./wavefront/paperboat.obj",
            "./wavefront/CheoyLee42.obj",
            "./wavefront/CheoyLee42Rig.obj",
            "./wavefront/CheoyLee42Sails.obj",
            "./wavefront/MerryDream.obj",
            "./wavefront/Mehari.obj",
            "./wavefront/kayak.obj",
            "./wavefront/Trimaran.obj"
    };
    private static int fileIndex = 0;
    private final static String IDX_PRM_PREFIX = "--idx:"; // Command line prm, use it like --idx:0 to --idx:7 to choose the file to process.

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) throws Exception {

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(IDX_PRM_PREFIX)) {
                String prmValue = args[i].substring(IDX_PRM_PREFIX.length());
                fileIndex = Integer.parseInt(prmValue);
                if (fileIndex < 0 || fileIndex > (OBJ_FILE_NAME.length - 1)) {
                    System.err.printf("File Index must be in [0 .. %d], resetting to 0.\n", (OBJ_FILE_NAME.length - 1));
                    fileIndex = 0;
                } else {
                    System.out.printf(">> Will use file %s\n", OBJ_FILE_NAME[fileIndex]);
                }
            }
        }

        System.out.println("----------------------------------------------");
        System.out.println(String.format("Running from folder %s", System.getProperty("user.dir")));
        System.out.println(String.format("Java Version %s", System.getProperty("java.version")));
        System.out.println("----------------------------------------------");

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);
        File file = new File(OBJ_FILE_NAME[fileIndex]);
        WaveFrontUtils.WaveFrontObj obj;
        if (!file.exists()) {
            System.out.println("File not found, Oops...");
            System.exit(1);
        }

        obj = WaveFrontUtils.parseWaveFrontObj(new BufferedReader(new FileReader(file)));
        // Count artifacts:
        System.out.printf("%d vertices, %d edges\n", obj.getVertices().size(), obj.getEdges().size());

        // Find extrema, to center the figure
        double maxX = obj.getVertices().stream().mapToDouble(v3d -> v3d.getX()).max().getAsDouble();
        double minX = obj.getVertices().stream().mapToDouble(v3d -> v3d.getX()).min().getAsDouble();
        double maxY = obj.getVertices().stream().mapToDouble(v3d -> v3d.getY()).max().getAsDouble();
        double minY = obj.getVertices().stream().mapToDouble(v3d -> v3d.getY()).min().getAsDouble();
        double maxZ = obj.getVertices().stream().mapToDouble(v3d -> v3d.getZ()).max().getAsDouble();
        double minZ = obj.getVertices().stream().mapToDouble(v3d -> v3d.getZ()).min().getAsDouble();

        System.out.println("Extrema:");
        System.out.printf("X in [%f, %f]\n", minX, maxX);
        System.out.printf("Y in [%f, %f]\n", minY, maxY);
        System.out.printf("Z in [%f, %f]\n", minZ, maxZ);

        // Use the re-centerer to slide the object in its referential.
//        VectorUtils.Vector3D recenterer = new VectorUtils.Vector3D((maxX - minX) / 2,
//                (maxY - minY) / 2,
//                (maxZ - minZ) / 2);
        VectorUtils.Vector3D recenterer = new VectorUtils.Vector3D(-(maxX - minX) / 2, 0, 0);

        box3D.setxMin(minX -(maxX - minX) / 2);
        box3D.setxMax(maxX -(maxX - minX) / 2);
        box3D.setyMin(minY);
        box3D.setyMax(maxY);
        box3D.setzMin(minZ);
        box3D.setzMax(maxZ);

        // Do something specific here, after the box drawing.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            g2d.setStroke(new BasicStroke(1));

            obj.getEdges().forEach(line -> {
                int from = line[0] - 1;  // Warning: those indexes are 1-based, not 0.
                int to = line[1] - 1;
                try {
                    VectorUtils.Vector3D fromV3Rotated = VectorUtils.rotate(VectorUtils.add3D(Arrays.asList(obj.getVertices().get(from), recenterer)),
                            Math.toRadians(box3D.getRotOnX()),
                            Math.toRadians(box3D.getRotOnY()),
                            Math.toRadians(box3D.getRotOnZ()));
                    VectorUtils.Vector3D toV3Rotated = VectorUtils.rotate(VectorUtils.add3D(Arrays.asList(obj.getVertices().get(to), recenterer)),
                            Math.toRadians(box3D.getRotOnX()),
                            Math.toRadians(box3D.getRotOnY()),
                            Math.toRadians(box3D.getRotOnZ()));
                    Function<VectorUtils.Vector3D, Point> transformer = box3D.getTransformer();
                    Point fromPoint = transformer.apply(fromV3Rotated);
                    Point toPoint = transformer.apply(toV3Rotated);
                    g2d.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
                } catch (IndexOutOfBoundsException ioobe) { // Error in the obj.
                    ioobe.printStackTrace();
                }
            });

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC, 24f));
            g2d.setColor(new Color(106, 86, 205));
            g2d.drawString(OBJ_FILE_NAME[fileIndex],10, 18 + 32);

        };
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D);
        frame.setVisible(true);
    }
}
