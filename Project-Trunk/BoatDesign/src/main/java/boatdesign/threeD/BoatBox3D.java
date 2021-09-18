package boatdesign.threeD;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgetsV2;
import gsg.VectorUtils;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Many hard-coded values...
 */
public class BoatBox3D extends Box3D {

    private static boolean verbose = false;

    private final int MIN_X =    0;
    private final int MAX_X =  600;
    private final int MIN_Y = -110;
    private final int MAX_Y =  110;
    private final int MIN_Z =  -30;
    private final int MAX_Z =  100;

    private double xOffset = 25.0;
    private double centerOnXValue = 300.0;

    // TODO A prm for the number of points per frame (bezier's t)

    private boolean justTheBoat = false;

    private boolean symmetrical = true;
    private boolean drawFrameCtrlPoints = true;
    private double frameIncrement = 10d; // 10.0; // 50.0;
    private double wlIncrement = 10d; // 10.0; // 50.0;
    private double buttockIncrement = 10d; // 10.0; // 50.0;

    private List<Double> hValues = List.of(-10d, 0d, 10d, 20d, 30d, 40d, 50d);
//    private List<Double> hValues = List.of(-10d, -5d, 0d, 5d, 10d, 15d, 20d, 25d, 30d, 35d, 40d, 45d, 50d);
    private List<Double> vValues = List.of(20d, 40d, 60d, 80d, 100d);
//    private List<Double> vValues = List.of(10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d);
    private boolean frames = true;
    private boolean waterlines = true;
    private boolean buttocks = true;

    // Hard coded values to start with.
    private List<Bezier.Point3D> ctrlPointsRail = List.of(  // Rail
            new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, 75.000000), // PT B
            new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 21.428571, 75.0), // 68.928571),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 69.642857, 86.785714, 47.500000),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 272.142857, 129.642857, 45.357143),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 65.0, 56.000000));  // PT X

    private List<Bezier.Point3D> ctrlPointsBow = List.of( // Bow (Bow transom, actually)
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 10.000000, 75.000000),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, 75.000000), // PT B
            new Bezier.Point3D((-centerOnXValue + xOffset) + 10.000000, 0.000000, -5.000000)); // PT C

    private List<Bezier.Point3D> ctrlPointsKeel = List.of( // Keel
            new Bezier.Point3D((-centerOnXValue + xOffset) + 10.000000, 0.000000, -5.000000), // PT C
            new Bezier.Point3D((-centerOnXValue + xOffset) + 290.357143, 0.000000, -29.642857),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 0.000000, 5.000000)); // PT A

    // This one is recalculated with the keel and the rail.
    private List<Bezier.Point3D> ctrlPointsTransom = List.of( // Transom
            new Bezier.Point3D((-centerOnXValue + xOffset) + 550.0, 65.0, 56.000000),   // PT X
            new Bezier.Point3D((-centerOnXValue + xOffset) + 550.0, 65.0, 5.642857),
            new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 0.000000, 5.000000)); // PT A

    private List<List<Bezier.Point3D>> frameCtrlPts = new ArrayList<>();
    private List<VectorUtils.Vector3D> bezierPointsRail = new ArrayList<>();
    private List<VectorUtils.Vector3D> bezierPointsBow = new ArrayList<>();
    private List<VectorUtils.Vector3D> bezierPointsKeel = new ArrayList<>();
    // Extrapolated.
    private List<VectorUtils.Vector3D> bezierPointsTransom = new ArrayList<>();

    private List<List<VectorUtils.Vector3D>> frameBezierPts = new ArrayList<>();
    private List<List<Bezier.Point3D>> hLines = new ArrayList<>();
    private List<List<Bezier.Point3D>> vLines = new ArrayList<>();

    protected BoatBox3D instance = this;

    public BoatBox3D() {
        super(ThreeDFrameWithWidgetsV2.DEFAULT_WIDTH, ThreeDFrameWithWidgetsV2.DEFAULT_HEIGHT);
        this.setxMin(MIN_X - centerOnXValue);
        this.setxMax(MAX_X - centerOnXValue);
        this.setyMin(MIN_Y);
        this.setyMax(MAX_Y);
        this.setzMin(MIN_Z);
        this.setzMax(MAX_Z);

        this.setXLabelTransformer(x -> String.valueOf(x + 275));

//        BoatBox3D instance = this;

        Consumer<Graphics2D> afterDrawer = g2d -> {
//            System.out.println("Starting rendering");
            long beforeRend = System.currentTimeMillis();

            if (false) { /// On demand, in a thread?
                refreshData();
            }
            // Link the control points
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            VectorUtils.Vector3D from = null;
            if (!justTheBoat) {
                // 1 - Rail
                for (Bezier.Point3D ctrlPoint : ctrlPointsRail) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        instance.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsRail) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            instance.drawSegment(g2d, from, to);
                        }
                        from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                    }
                }

                // 2 - Bow
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsBow) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        instance.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsBow) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            instance.drawSegment(g2d, from, to);
                        }
                        from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                    }
                }

                // 3 - Keel
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsKeel) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        instance.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                // 4 - Transom
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsTransom) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        instance.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsTransom) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            instance.drawSegment(g2d, from, to);
                        }
                        from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                    }
                }

                // All the frames
                if (drawFrameCtrlPoints) {
                    for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                        from = null;
                        for (Bezier.Point3D ctrlPoint : ctrlPts) {
                            if (from != null) {
                                VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                                instance.drawSegment(g2d, from, to);
                            }
                            from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        }
                    }
                    if (symmetrical) {
                        for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                            from = null;
                            for (Bezier.Point3D ctrlPoint : ctrlPts) {
                                if (from != null) {
                                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                                    instance.drawSegment(g2d, from, to);
                                }
                                from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            }
                        }
                    }
                }
                // Plot the control points
                g2d.setColor(Color.BLUE);
                int fontSize = g2d.getFont().getSize();
                // Rail(s)
                ctrlPointsRail.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    instance.drawCircle(g2d, at, 6);
                    // Below, an example of drawString
                    String str = String.valueOf(ctrlPointsRail.indexOf(pt) + 1);
                    Color g2dColor = g2d.getColor();
                    g2d.setColor(Color.BLACK);
                    instance.drawStringAt(g2d, at, str, 0, -fontSize / 2, Box3D.Justification.CENTER);
                    g2d.setColor(g2dColor);
                });
                if (symmetrical) {
                    ctrlPointsRail.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        instance.drawCircle(g2d, at, 6);
                    });
                }
                // Bow
                ctrlPointsBow.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    instance.drawCircle(g2d, at, 6);
                });
                if (symmetrical) {
                    ctrlPointsBow.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        instance.drawCircle(g2d, at, 6);
                    });
                }
                // Keel
                ctrlPointsKeel.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    instance.drawCircle(g2d, at, 6);
                    // Below, an example of drawString
                    String str = String.valueOf(ctrlPointsRail.size() + ctrlPointsKeel.indexOf(pt) + 1);
                    Color g2dColor = g2d.getColor();
                    g2d.setColor(Color.BLACK);
                    instance.drawStringAt(g2d, at, str, 0, fontSize + 2, Box3D.Justification.CENTER);
                    g2d.setColor(g2dColor);
                });
                // Transom
                ctrlPointsTransom.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    instance.drawCircle(g2d, at, 6);
                });
                if (symmetrical) {
                    ctrlPointsTransom.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        instance.drawCircle(g2d, at, 6);
                    });
                }

                // Ctrl points for the frames
                if (frames && drawFrameCtrlPoints) {
                    for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                        ctrlPts.forEach(pt -> {
                            VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                            instance.drawCircle(g2d, at, 3);
                        });
                    }
                    if (symmetrical) {
                        for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                            ctrlPts.forEach(pt -> {
                                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                                instance.drawCircle(g2d, at, 3);
                            });
                        }
                    }
                }
            }

            // The actual beziers
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            from = null;
            // Rail(s)
            for (VectorUtils.Vector3D to : bezierPointsRail) {
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (VectorUtils.Vector3D to : bezierPointsRail) {
                    to = to.y(-to.getY()); // Whahaha!
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            from = null;
            // Bow
            for (VectorUtils.Vector3D to : bezierPointsBow) {
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (VectorUtils.Vector3D to : bezierPointsBow) {
                    to = to.y(-to.getY());
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            from = null;
            // Keel
            for (VectorUtils.Vector3D to : bezierPointsKeel) {
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            // Transom
            for (VectorUtils.Vector3D to : bezierPointsTransom) {
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (VectorUtils.Vector3D to : bezierPointsTransom) {
                    to = to.y(-to.getY());
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }

            // All the frames
            if (frames) {
                g2d.setStroke(new BasicStroke(1));
                for (List<VectorUtils.Vector3D> bezierPoints : frameBezierPts) {
                    from = null;
                    for (VectorUtils.Vector3D to : bezierPoints) {
                        if (from != null) {
                            instance.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                }
                if (symmetrical) {
                    for (List<VectorUtils.Vector3D> bezierPoints : frameBezierPts) {
                        from = null;
                        for (VectorUtils.Vector3D to : bezierPoints) {
                            to = to.y(-to.getY());
                            if (from != null) {
                                instance.drawSegment(g2d, from, to);
                            }
                            from = to;
                        }
                    }
                }
            }

            if (waterlines) { // Display
                for (List<Bezier.Point3D> waterLine : hLines) {
                    // Is it waterline (z=0) ?
                    if (waterLine.get(0).getZ() == 0) {
                        g2d.setColor(Color.BLUE);
                        g2d.setStroke(new BasicStroke(2));
                    } else {
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(1));
                    }

                    from = null;
                    for (Bezier.Point3D waterLinePt : waterLine) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), waterLinePt.getY(), waterLinePt.getZ());
                        if (from != null) {
                            instance.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                    if (symmetrical) {
                        from = null;
                        for (Bezier.Point3D waterLinePt : waterLine) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), -waterLinePt.getY(), waterLinePt.getZ());
                            if (from != null) {
                                instance.drawSegment(g2d, from, to);
                            }
                            from = to;
                        }
                    }
                }
            }
            if (buttocks) {
                for (List<Bezier.Point3D> vLine : vLines) {
                    from = null;
                    for (Bezier.Point3D vLinePt : vLine) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(vLinePt.getX(), vLinePt.getY(), vLinePt.getZ());
                        if (from != null) {
                            instance.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                    if (symmetrical) {
                        from = null;
                        for (Bezier.Point3D vLinePt : vLine) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(vLinePt.getX(), -vLinePt.getY(), vLinePt.getZ());
                            if (from != null) {
                                instance.drawSegment(g2d, from, to);
                            }
                            from = to;
                        }
                    }
                }
            }

            long afterRend = System.currentTimeMillis();
//            System.out.printf("Rendering took %s ms\n", NumberFormat.getInstance().format(afterRend - beforeRend));
        };
        // Invoke the above
        this.setAfterDrawer(afterDrawer);

    }

    public double getFrameIncrement() {
        return frameIncrement;
    }

    public void setFrameIncrement(double frameIncrement) {
        this.frameIncrement = frameIncrement;
    }

    public double getWlIncrement() {
        return wlIncrement;
    }

    public void setWlIncrement(double wlIncrement) {
        this.wlIncrement = wlIncrement;
    }

    public double getButtockIncrement() {
        return buttockIncrement;
    }

    public void setButtockIncrement(double buttockIncrement) {
        this.buttockIncrement = buttockIncrement;
    }

    public boolean isJustTheBoat() {
        return justTheBoat;
    }

    public void setJustTheBoat(boolean justTheBoat) {
        this.justTheBoat = justTheBoat;
    }

    public boolean isSymmetrical() {
        return symmetrical;
    }

    public void setSymmetrical(boolean symmetrical) {
        this.symmetrical = symmetrical;
    }

    public boolean isDrawFrameCtrlPoints() {
        return drawFrameCtrlPoints;
    }

    public void setDrawFrameCtrlPoints(boolean drawFrameCtrlPoints) {
        this.drawFrameCtrlPoints = drawFrameCtrlPoints;
    }

    public boolean isFrames() {
        return frames;
    }

    public void setFrames(boolean frames) {
        this.frames = frames;
    }

    public boolean isWaterlines() {
        return waterlines;
    }

    public void setWaterlines(boolean waterlines) {
        this.waterlines = waterlines;
    }

    public boolean isButtocks() {
        return buttocks;
    }

    public void setButtocks(boolean buttocks) {
        this.buttocks = buttocks;
    }

    private static double xCenterOfHull = -1;
    private static double zCenterOfHull =  1;
    private static double calculateDisplacement(Map<Double, Double> displacementMap,
                                                double lwlStart,
                                                double lwlEnd) {
        AtomicReference<Double> disp = new AtomicReference<>(0d);
        AtomicReference<Double> prevArea = new AtomicReference<>(-1d);
        AtomicReference<Double> prevX = new AtomicReference<>(-1d);
        displacementMap.keySet().stream()
                .forEach(x -> {
                    double area = displacementMap.get(x);
                    if (prevArea.get() != -1) {
                        double avgArea = (prevArea.get() + area) / 2.0;   // Average
                        double deltaX = x - (prevX.get() == -1 ? lwlStart : prevX.get());
                        disp.set(disp.get() + (deltaX * 1e-2 * avgArea * 1e-4));
                    }
                    prevArea.set(area);
                    prevX.set(x);
                });
        if (prevX.get() < lwlEnd) { // there is more...
            double avgArea = (prevArea.get() + 0) / 2.0;
            double deltaX = lwlEnd - (prevX.get() == -1 ? lwlStart : prevX.get());
            disp.set(disp.get() + (deltaX * 1e-2 * avgArea * 1e-4));
        }
        double displacement = disp.get();

        // Find center?
        disp.set(0d);
        prevArea.set(-1d);
        prevX.set(-1d);
        Set<Double> keys = displacementMap.keySet();
        Iterator<Double> iterator = keys.iterator();
        while (iterator.hasNext()) {
            double x = iterator.next();
//            System.out.println(x);
            double area = displacementMap.get(x);
            if (prevArea.get() != -1) {
                double avgArea = (prevArea.get() + area) / 2.0;
                double deltaX = x - (prevX.get() == -1 ? lwlStart : prevX.get());
                double prevDisp = disp.get();
                double toAdd = (deltaX * 1e-2 * avgArea * 1e-4);
                double missing = ((displacement / 2.0) - prevDisp);
                if (missing < toAdd) {
                    double addX = deltaX * (missing / toAdd);
                    xCenterOfHull = (prevX.get() + addX);
//                    System.out.println("Found CC at " + xCenterOfHull);
                    break;
                }
                disp.set(prevDisp + toAdd);
            }
            prevArea.set(area);
            prevX.set(x);
        }

        return 2 * displacement;
    }

    private static double calculateZDisplacement(Map<Double, Double> displacementMap) {
        AtomicReference<Double> disp = new AtomicReference<>(0d);
        AtomicReference<Double> prevArea = new AtomicReference<>(-1d);
        AtomicReference<Double> prevZ = new AtomicReference<>(0d);
        displacementMap.keySet().stream()
                .forEach(z -> {
                    double area = displacementMap.get(z);
                    if (prevArea.get() != -1) {
                        double avgArea = (prevArea.get() + area) / 2.0;   // Average
                        double deltaZ = z - prevZ.get();
                        disp.set(disp.get() + (deltaZ * 1e-2 * avgArea * 1e-4));
                    }
                    prevArea.set(area);
                    prevZ.set(z);
                });
        double displacement = disp.get();
        if (true || verbose) {
            System.out.printf("From Z Displ: %.03f m3\n", displacement);
        }

        // Find Z center?
        disp.set(0d);
        prevArea.set(-1d);
        prevZ.set(-1d);
        Set<Double> keys = displacementMap.keySet();
        Iterator<Double> iterator = keys.iterator();
        while (iterator.hasNext()) {
            double z = iterator.next();
//            System.out.println(x);
            double area = displacementMap.get(z);
            if (prevArea.get() != -1) {

                double avgArea = (prevArea.get() + area) / 2.0;   // Average
                double deltaZ = z - prevZ.get();
                disp.set(disp.get() + (deltaZ * 1e-2 * avgArea * 1e-4));

                double prevDisp = disp.get();
                double toAdd = (deltaZ * 1e-2 * avgArea * 1e-4);
                double missing = ((displacement / 2.0) - prevDisp);
                if (missing < toAdd) {
                    double addZ = deltaZ * (missing / toAdd);
                    zCenterOfHull = (prevZ.get() + addZ);
//                    System.out.println("Found CC at " + xCenterOfHull);
                    break;
                }
                disp.set(prevDisp + toAdd);
            }
            prevArea.set(area);
            prevZ.set(z);
        }

        return 2 * displacement;
    }

    private static boolean listContainsBezierPoint(List<Bezier.Point3D> list, Bezier.Point3D point) {
        for (Bezier.Point3D pt : list) {
            if (pt.getX() - point.getX() == 0 &&
                    pt.getY() - point.getY() == 0 &&
                    pt.getZ() - point.getZ() == 0) {
                return true;
            }
        }
        return false;
    }

    // Re-generates the boat
    public void refreshData() {
        refreshData(null);
    }
    public void refreshData(Consumer<String> callback) {

        // TODO Parameterize the t+=0.01

        this.frameCtrlPts = new ArrayList<>();
        this.bezierPointsRail = new ArrayList<>();
        this.bezierPointsBow = new ArrayList<>();
        this.bezierPointsKeel = new ArrayList<>();
        // Extrapolated.
        this.bezierPointsTransom = new ArrayList<>();

        this.frameBezierPts = new ArrayList<>();
        this.hLines = new ArrayList<>();
        this.vLines = new ArrayList<>();

        // Generate the data, the Bézier curves.

        // Also find the widest point
        double maxWidth = 0d, maxWidthX = 0d;
        double maxHeight = 0d;
        double maxDepth = 0d, maxDepthX = 0d;
        double lwl = 0.0, lwlStart = 0d, lwlEnd = 0d;
        Bezier.Point3D maxWidthPoint = null;
        Bezier bezierRail = new Bezier(ctrlPointsRail);
        for (double t=0; t<=1.001; t+=0.01) { // TODO Verify that limit (double...)
            Bezier.Point3D tick = bezierRail.getBezierPoint(t);
            bezierPointsRail.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
//            System.out.printf("Rail Bezier X: %f\n", tick.getX());
            if (tick.getY() > maxWidth) {
                maxWidth = tick.getY();
                maxWidthPoint = tick;
            }
            if (tick.getZ() > maxHeight) {
                maxHeight = tick.getZ();
            }
        }
        maxWidthX = maxWidthPoint.getX() - (-centerOnXValue + xOffset);
        if (verbose) {
            System.out.printf("Max Width: %f, at X:%f\n", maxWidth, maxWidthPoint.getX() - (-centerOnXValue + xOffset));
        }

        Bezier bezierBow = new Bezier(ctrlPointsBow);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierBow.getBezierPoint(t);
            bezierPointsBow.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        double tFor0 = 0;
        try {
            tFor0 = bezierBow.getTForGivenZ(0.0, 1E-1, 0, 1E-4, false);
        } catch (Bezier.TooDeepRecursionException tdre) {
            // TODO Manage that
            tdre.printStackTrace();
            tFor0 = -1;
        }
        Bezier.Point3D wlPoint1 = bezierBow.getBezierPoint(tFor0);

        Bezier bezierKeel = new Bezier(ctrlPointsKeel);
        try {
            tFor0 = bezierKeel.getTForGivenZ(0.0, 1E-1, 0, 1E-4, true);
        } catch (Bezier.TooDeepRecursionException tdre) {
            // TODO Manage that
            tdre.printStackTrace();
            tFor0 = -1;
        }
        Bezier.Point3D wlPoint2 = bezierKeel.getBezierPoint(tFor0);
        lwlStart = wlPoint1.getX()  - (-centerOnXValue + xOffset);
        lwlEnd = wlPoint2.getX() - (-centerOnXValue + xOffset);
        lwl = wlPoint2.getX() - wlPoint1.getX();
        if (verbose) {
            System.out.printf("LWL: %f\n", lwl);
        }
        // Also find the deepest point
        Bezier.Point3D maxDepthPoint = null;
        for (double t=0; t<=1.001; t+=0.01) { // TODO Limit (double...)
            Bezier.Point3D tick = bezierKeel.getBezierPoint(t);
            bezierPointsKeel.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
            if (tick.getZ() < maxDepth) {
                maxDepth = tick.getZ();
                maxDepthPoint = tick;
            }
        }
        maxDepthX = maxDepthPoint.getX() - (-centerOnXValue + xOffset);
        if (verbose) {
            System.out.printf("Max Depth: %f, at X:%f\n", maxDepth, maxDepthPoint.getX() - (-centerOnXValue + xOffset));
        }

        // This one is correlated, re-calculated
        Bezier bezierTransom = new Bezier(ctrlPointsTransom);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierTransom.getBezierPoint(t);
            bezierPointsTransom.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        List<Bezier> frameBeziers = new ArrayList<>();
        Map<Double, Double> displacementXMap = new LinkedHashMap<>();
        Map<Double, Double> displacementZMap = new LinkedHashMap<>();
        double displ = 0d;
        double prismCoeff = 0d;
        if (frames) {
            // Extrapolate all the frames (to the end of rail. could be the same as transom)
            // Displacement...
            double maxFrameArea = 0d;
            // TODO _x <= ? Make sure the end has a frame...
            for (double _x = (-centerOnXValue + xOffset) + frameIncrement; _x </*=*/ (-centerOnXValue + xOffset) + 550.0; _x += frameIncrement) {
                if (verbose) {
                    System.out.printf("... Calculating frame %.03f... ", _x);
                }
                long one = System.currentTimeMillis();
                boolean increase = (bezierRail.getBezierPoint(0).getX() < bezierRail.getBezierPoint(1).getX());
                double tx = 0;
                try {
                    tx = bezierRail.getTForGivenX(0.0, 1E-1, _x, 1E-4, increase);
                } catch (Bezier.TooDeepRecursionException tdre) {
                    // TODO Manage that
                    tdre.printStackTrace();
                    tx = -1;
                }
                Bezier.Point3D _top = bezierRail.getBezierPoint(tx);
                increase = (bezierKeel.getBezierPoint(0).getX() < bezierKeel.getBezierPoint(1).getX());
                try {
                    tx = bezierKeel.getTForGivenX(0.0, 1E-1, _x, 1E-4, increase);
                } catch (Bezier.TooDeepRecursionException tdre) {
                    // TODO Manage that
                    tdre.printStackTrace();
                    tx = -1;
                }
                Bezier.Point3D _bottom = bezierKeel.getBezierPoint(tx);

                List<Bezier.Point3D> ctrlPointsFrame = List.of(
                        new Bezier.Point3D(_x, _top.getY(), _top.getZ()),
                        new Bezier.Point3D(_x, _top.getY(), _bottom.getZ()),
                        new Bezier.Point3D(_x, _bottom.getY(), _bottom.getZ()));

                frameCtrlPts.add(ctrlPointsFrame);
                Bezier bezierFrame = new Bezier(ctrlPointsFrame);
                frameBeziers.add(bezierFrame);
                List<VectorUtils.Vector3D> bezierPointsFrame = new ArrayList<>();
                double frameArea = 0.0;
                double prevY = -1.0;
                double prevZ = 0.0;
                for (double t = 0; t <= 1.0; t += 0.01) {
                    Bezier.Point3D tick = bezierFrame.getBezierPoint(t);
                    bezierPointsFrame.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                    if (tick.getZ() <= 0) {
                        if (prevY != -1.0) {
                            double y = tick.getY();
                            // rectangle above
                            double rectAboveArea = Math.abs((y - prevY) * tick.getZ());
                            // triangle below
                            double triangleBelowArea = Math.abs((y - prevY) * (tick.getZ() - prevZ) / 2.0);
                            frameArea += (rectAboveArea + triangleBelowArea);
                        }
                        prevY = tick.getY();
                        prevZ = tick.getZ();
                    }
                }
                if (frameArea > 0) {
                    displacementXMap.put(_x - (-centerOnXValue + xOffset), frameArea);
                }
                if (verbose) {
                    System.out.printf("(area: %f) ", frameArea);
                }
                maxFrameArea = Math.max(maxFrameArea, frameArea);

                frameBezierPts.add(bezierPointsFrame);
                long two = System.currentTimeMillis();
                if (verbose) {
                    System.out.printf(" in %s ms.\n", NumberFormat.getInstance().format(two - one));
                }
            }
            // displ?
            if (verbose) {
                System.out.printf("- Max area: %f, LWL: %f\n", maxFrameArea, lwl);
            }
            // Rough estimation
//        double prismCoeff = 0.55;
//        double displ = (2 * maxFrameArea * 1e-4) * (lwl * 1e-2) * prismCoeff;
//        if (verbose) {
//            System.out.printf("\nEstimated displacement: %.03f m3\n\n", displ);
//        }
            // More precisely (and sets the X pos of CC)
            displ = calculateDisplacement(displacementXMap, lwlStart, lwlEnd);
            prismCoeff = displ / (2 * maxFrameArea * 1e-4 * lwl * 1e-2);
            if (verbose) {
                System.out.printf("\nCalculated displacement: %.03f m3\n\n", displ);
            }
        }

        if (waterlines) { // Construction
            // H lines. Use a step for waterlines. maxDepth, maxHeight, wlIncrement.
            double from = Math.ceil(maxDepth / wlIncrement) * wlIncrement;
            double to = Math.floor(maxHeight / wlIncrement) * wlIncrement;
            if (verbose) {
                System.out.printf("WL from %f to %f\n", from, to);
            }
            hValues = new ArrayList<>();
            // Bottom to top
            for (double wl = from; wl <= to; wl += wlIncrement) {
                hValues.add(wl);
            }
            hValues.forEach(z -> {
                if (verbose) {
                    System.out.println("Waterline for z=" + z);
                }
                List<Bezier.Point3D> waterLine = new ArrayList<>();
                try {
                    double[] keelMinMax = bezierKeel.getMinMax(Bezier.Coordinate.Z, 1e-4);
                    AtomicBoolean noPointYet = new AtomicBoolean(true);
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getZ() < bezierBow.getBezierPoint(1).getZ());
                    double tBow = 0;
                    try {
                        tBow = bezierBow.getTForGivenZ(0, 1E-1, z, 1E-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tBow = -1;
                    }
                    if (tBow != -1) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
//                        if (!waterLine.contains(bezierPoint)) {
                          if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                            waterLine.add(bezierPoint);
                            noPointYet.set(false);
                        }
                    }
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getZ() < bezier.getBezierPoint(1).getZ());
                        double t = 0;
                        try {
                            t = bezier.getTForGivenZ(0, 1E-1, z, 1E-4, increase);
                        } catch (Bezier.TooDeepRecursionException tdre) {
                            // TODO Manage that
                            tdre.printStackTrace();
                            t = -1;
                        }
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
//                            if (!waterLine.contains(bezierPoint)) { // TODO On the coordinates...
                              if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                                waterLine.add(bezierPoint);
                            }
//                            waterLine.add(bezierPoint);
                            noPointYet.set(false);
                        } else {
                            if (verbose) {
                                System.out.printf("Waterline not found for z=%.02f, X=%.02f (no point yet:%b)\n", z, bezier.getControlPoints().get(0).getX(), noPointYet.get());
                            }
                            // If noPointYet: look before the min. After otherwise
                            if (noPointYet.get()) {
                                increase = false; // (bezierKeel.getBezierPoint(0).getZ() < bezierKeel.getBezierPoint(1).getZ());
                                // Warning: keel goes down before going up! Hence the tMinKeel
                                try {
                                    t = bezierKeel.getTForGivenZ(0, 1E-1, z, 1E-3, increase);
                                } catch (Bezier.TooDeepRecursionException tdre) {
                                    // TODO Manage that
                                    tdre.printStackTrace();
                                    t = -1;
                                }
                                if (t != -1) {
                                    Bezier.Point3D bezierPoint = bezierKeel.getBezierPoint(t);
                                    waterLine.add(bezierPoint);
                                    noPointYet.set(false); // ?? Comment that?
                                    if (verbose) {
                                        System.out.printf("For z=%f, adding first point at %s\n", z, bezierPoint);
                                    }
                                }
                            } else { // WiP...
                                if (verbose) {
                                    System.out.printf("For z=%f, adding last point...\n", z);
                                }
                                try {
                                    // keelMinMax[0] + 0.1: Pb when finding an extremum...
                                    double tMinKeel = 0;
                                    try {
                                        tMinKeel = bezierKeel.getTForGivenZ(0, 1e-1, keelMinMax[0] + 1, 1e-3, false);
                                    } catch (Bezier.TooDeepRecursionException tdre) {
                                        tdre.printStackTrace();
                                        tMinKeel = -1;
                                    }
                                    if (tMinKeel != -1) {
                                        increase = true; // (bezierKeel.getBezierPoint(0).getZ() < bezierKeel.getBezierPoint(1).getZ());
                                        // Warning: keel goes down before going up! Hence the tMinKeel
                                        try {
                                            t = bezierKeel.getTForGivenZ(tMinKeel, 1E-1, z, 1E-4, increase);
                                        } catch (Bezier.TooDeepRecursionException tdre) {
//                                            tdre.printStackTrace();
                                            System.out.println("... Keel. Retrying the other way (2)");
                                            try {
                                                t = bezierKeel.getTForGivenZ(tMinKeel, 1E-1, z, 1E-4, !increase);
                                            } catch (Bezier.TooDeepRecursionException tdre2) {
                                                tdre2.printStackTrace();
                                                t = -1;
                                            }
                                        }
                                        if (t != -1) {
                                            Bezier.Point3D bezierPoint = bezierKeel.getBezierPoint(t);
//                                            if (!waterLine.contains(bezierPoint)) { // TODO On the coordinates...
                                            if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                                                waterLine.add(bezierPoint);
                                            }
                                        }
                                    } else {
                                        if (verbose) {
                                            System.out.println("Min Keel not found!");
                                        }
                                    }
                                } catch (Throwable ex) {
                                    ex.printStackTrace(); // Stack Overflow?
                                }
                            }
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getZ() < bezierTransom.getBezierPoint(1).getZ());
                    double tTransom = 0;
                    try {
                        tTransom = bezierTransom.getTForGivenZ(0, 1E-1, z, 1E-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tTransom = -1;
                    }
                    if (tTransom != -1) {
                        Bezier.Point3D bezierPoint = bezierTransom.getBezierPoint(tTransom);
                        waterLine.add(bezierPoint);
                    } else {
                        if (true) { // WiP...
                            // keelMinMax[0] + 0.1: Pb when finding an extremum...
                            double tMinKeel = 0;
                            try {
                                tMinKeel = bezierKeel.getTForGivenZ(0, 1e-1, keelMinMax[0] + 1, 1e-3, false);
                            } catch (Bezier.TooDeepRecursionException tdre) {
                                tdre.printStackTrace();
                                tMinKeel = -1;
                            }
                            if (tMinKeel != -1) {
                                boolean increase = true; // (bezierKeel.getBezierPoint(0).getZ() < bezierKeel.getBezierPoint(1).getZ());
                                // Warning: keel goes down before going up! Hence the tMinKeel
                                double t =0;
                                try {
                                    t = bezierKeel.getTForGivenZ(tMinKeel, 1E-1, z, 1E-4, increase);
                                } catch (Bezier.TooDeepRecursionException tdre) {
//                                    tdre.printStackTrace();
                                    System.out.println("... Keel. Retrying the other way (1)");
                                    try {
                                        t = bezierKeel.getTForGivenZ(tMinKeel, 1E-1, z, 1E-4, !increase);
                                    } catch (Bezier.TooDeepRecursionException tdre2) {
                                        tdre2.printStackTrace();
                                        t = -1;
                                    }
                                }
                                if (t != -1) {
                                    Bezier.Point3D bezierPoint = bezierKeel.getBezierPoint(t);
                                    if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                                        waterLine.add(bezierPoint);
                                    }
                                }
                            } else {
                                if (verbose) {
                                    System.out.println("Min Keel not found!");
                                }
                            }
                        }
                    }
                    // Add to the list
                    hLines.add(waterLine);
                    // Displacement (CC's height/depth)?
                    if (z <= 0) { // In the water
                        // Add to displacementZMap
                        final AtomicReference<Double> wlArea = new AtomicReference<>(0d);
                        final AtomicReference<Double> prevX = new AtomicReference<>(-1d);
                        final AtomicReference<Double> prevY = new AtomicReference<>(0d);
                        waterLine.stream()   // X, Y. TODO Recenter X
                                .forEach(pt -> {
//                                    System.out.println("-> " + pt);
                                    double x = pt.getX()  - (-centerOnXValue + xOffset);
                                    double y = pt.getY();
                                    if (prevX.get() != -1d) {
                                        double deltaX = x - prevX.get();
                                        double deltaY = y - prevY.get();
                                        // rectangle
                                        double areaOne = deltaX * prevY.get();
                                        // triangle at the end (+ or -)
                                        double areaTwo = deltaX * deltaY / 2.0;
                                        wlArea.set(wlArea.get() + (areaOne + areaTwo));
                                    }
                                    prevX.set(x);
                                    prevY.set(y);
                                });
                        if (verbose) {
                            System.out.printf("WL %.02f: %f (cm 2)\n", z, wlArea.get());
                        }
                        displacementZMap.put(z, wlArea.get());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            double zDispl = calculateZDisplacement(displacementZMap);
            System.out.printf("Disp on Z: %.05f m3, zCC: %.03f m", (zDispl * 2), (zCenterOfHull * 1e-2));
        }
        if (buttocks) {
            double from = buttockIncrement;
            double to = Math.floor(maxWidth / buttockIncrement) * buttockIncrement;
            if (verbose) {
                System.out.printf("Buttock from %f to %f\n", from, to);
            }
            vValues = new ArrayList<>();
            for (double buttock = from; buttock <= to; buttock += buttockIncrement) {
                vValues.add(buttock);
            }
            // V lines.
            vValues.forEach(y -> {
                if (verbose) {
                    System.out.println("Vline for y=" + y);
                }
                List<Bezier.Point3D> vLine = new ArrayList<>();
                try {
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getY() < bezierBow.getBezierPoint(1).getY());
                    double tBow = 0;
                    try {
                        tBow = bezierBow.getTForGivenY(0, 1E-1, y, 1E-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tBow = -1;
                    }
                    if (tBow != -1) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
                        vLine.add(bezierPoint);
                    }
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getY() < bezier.getBezierPoint(1).getY());
                        double t = 0;
                        try {
                            t = bezier.getTForGivenY(0, 1E-1, y, 1E-4, increase);
                        } catch (Bezier.TooDeepRecursionException tdre) {
                            // TODO Manage that
                            tdre.printStackTrace();
                            t = -1;
                        }
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                            vLine.add(bezierPoint);
                        } else {
                            if (verbose) {
                                System.out.printf("Vline not found for Y=%.02f, X=%.02f\n", y, bezier.getControlPoints().get(0).getX());
                            }
                            // Look on the keel? After the min?
                            if (false) {
                                // WiP...
                            }
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getY() < bezierTransom.getBezierPoint(1).getY());
                    double tTransom = 0;
                    try {
                        tTransom = bezierTransom.getTForGivenY(0, 1E-1, y, 1E-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tTransom = -1;
                    }
                    if (tTransom != -1) {
                        Bezier.Point3D bezierPoint = bezierTransom.getBezierPoint(tTransom);
                        vLine.add(bezierPoint);
                    }
                    // Add to the list
                    vLines.add(vLine);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (callback != null) {
            String callbackMessage =
                    String.format("Max Width: %f m (at %f m)\n" +
                                    "Max height: %f m\n" +
                                    "Max depth: %f m (at %f m)\n" +
                                    "LWL: %f m (%f to %f)\n" +
                                    "Displ: %f m3\n" +
                                    "Prismatic Coeff: %f\n" +
                                    "Center of hull at %f m (depth %f m)",
                            (maxWidth * 1e-2), (maxWidthX * 1e-2),
                            (maxHeight * 1e-2),
                            (maxDepth * 1e-2), (maxDepthX * 1e-2),
                            (lwl * 1e-2), (lwlStart * 1e-2), (lwlEnd * 1e-2),
                            displ, prismCoeff,
                            (xCenterOfHull * 1e-2), (zCenterOfHull * 1e-2));
            callback.accept(callbackMessage);
        }
    }

    private void correlate() {
        // 1 - Bow
        List<Bezier.Point3D> tempBow = new ArrayList<>(ctrlPointsBow);
        // First point of the rail is first point of the bow
        tempBow.get(0).x(ctrlPointsRail.get(0).getX())
                .y(ctrlPointsRail.get(0).getY())
                .z(ctrlPointsRail.get(0).getZ());
        // First point of the keel is last point of the bow
        int lastBowIdx = tempBow.size() - 1;
        tempBow.get(lastBowIdx).x(ctrlPointsKeel.get(0).getX())
                .y(ctrlPointsKeel.get(0).getY())
                .z(ctrlPointsKeel.get(0).getZ());
        this.ctrlPointsBow = tempBow;

        // 2 - Transom
        this.ctrlPointsTransom = new ArrayList<>();
        int lastRailIdx = this.ctrlPointsRail.size() - 1;
        int lastKeelIdx = this.ctrlPointsKeel.size() - 1;
        this.ctrlPointsTransom.add(new Bezier.Point3D(          // Top ext
                this.ctrlPointsRail.get(lastRailIdx).getX(),
                this.ctrlPointsRail.get(lastRailIdx).getY(),
                this.ctrlPointsRail.get(lastRailIdx).getZ()));
        this.ctrlPointsTransom.add(new Bezier.Point3D(          // Ctrl point
                this.ctrlPointsKeel.get(lastKeelIdx).getX(),
                this.ctrlPointsRail.get(lastRailIdx).getY(),
                this.ctrlPointsKeel.get(lastKeelIdx).getZ()));
        this.ctrlPointsTransom.add(new Bezier.Point3D(          // Bottom int
                this.ctrlPointsKeel.get(lastKeelIdx).getX(),
                this.ctrlPointsKeel.get(lastKeelIdx).getY(),
                this.ctrlPointsKeel.get(lastKeelIdx).getZ()));

    }

    public void setRailCtrlPoints(List<Bezier.Point3D> ctrlPointsRail) {
        this.ctrlPointsRail = new ArrayList<>();
        // Re-calculate with center and offset
        ctrlPointsRail.forEach(cp -> this.ctrlPointsRail.add(new Bezier.Point3D()
                .x((-centerOnXValue + xOffset) + cp.getX())
                .y(cp.getY())
                .z(cp.getZ())));
        correlate();
        this.repaint();
    }

    public void setKeelCtrlPoints(List<Bezier.Point3D> ctrlPointsKeel) {
        this.ctrlPointsKeel = new ArrayList<>();
        // Re-calculate with center and offset
        ctrlPointsKeel.forEach(cp -> this.ctrlPointsKeel.add(new Bezier.Point3D()
                .x((-centerOnXValue + xOffset) + cp.getX())
                .y(cp.getY())
                .z(cp.getZ())));
        correlate();
        this.repaint();
    }
}
