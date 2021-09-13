package boatdesign.threeD;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgetsV2;
import gsg.VectorUtils;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Many hard-coded values...
 */
public class BoatBox3D extends Box3D {

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

//    private List<Double> hValues = List.of(-10d, 0d, 10d, 20d, 30d, 40d, 50d);
    private List<Double> hValues = List.of(-10d, -5d, 0d, 5d, 10d, 15d, 20d, 25d, 30d, 35d, 40d, 45d, 50d);
//    private List<Double> vValues = List.of(20d, 40d, 60d, 80d, 100d);
    private List<Double> vValues = List.of(10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d);
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
                if (drawFrameCtrlPoints) {
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
            for (int i=0; i<bezierPointsRail.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsRail.get(i);
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsRail.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsRail.get(i);
                    to = to.y(-to.getY()); // Whahaha!
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            from = null;
            for (int i=0; i<bezierPointsBow.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsBow.get(i);
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsBow.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsBow.get(i);
                    to = to.y(-to.getY());
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            from = null;
            for (int i=0; i<bezierPointsKeel.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsKeel.get(i);
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            from = null;
            for (int i=0; i<bezierPointsTransom.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsTransom.get(i);
                if (from != null) {
                    instance.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsTransom.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsTransom.get(i);
                    to = to.y(-to.getY());
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }

            // All the frames
            g2d.setStroke(new BasicStroke(1));
            for (List<VectorUtils.Vector3D> bezierPoints : frameBezierPts) {
                from = null;
                for (int i = 0; i < bezierPoints.size(); i++) {
                    VectorUtils.Vector3D to = bezierPoints.get(i);
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            if (symmetrical) {
                for (List<VectorUtils.Vector3D> bezierPoints : frameBezierPts) {
                    from = null;
                    for (int i = 0; i < bezierPoints.size(); i++) {
                        VectorUtils.Vector3D to = bezierPoints.get(i);
                        to = to.y(-to.getY());
                        if (from != null) {
                            instance.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                }
            }

            if (waterlines) {
                for (List<Bezier.Point3D> waterLine : hLines) {

                    // Is it waterline (z=0) ?
//                    (waterLine.get(0).getZ() == 0)
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

    // Re-generates the boat
    public void refreshData() {
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
        double maxWidth = 0d;
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
        }
        System.out.printf("Max Width: %f, at X:%f\n", maxWidth, maxWidthPoint.getX() - (-centerOnXValue + xOffset));

        Bezier bezierBow = new Bezier(ctrlPointsBow);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierBow.getBezierPoint(t);
            bezierPointsBow.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierKeel = new Bezier(ctrlPointsKeel);

        // Also find the deepest point
        double maxDepth = 0d;
        Bezier.Point3D maxDepthPoint = null;
        for (double t=0; t<=1.001; t+=0.01) { // TODO Limit (double...)
            Bezier.Point3D tick = bezierKeel.getBezierPoint(t);
            bezierPointsKeel.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
            if (tick.getZ() < maxDepth) {
                maxDepth = tick.getZ();
                maxDepthPoint = tick;
            }
        }
        System.out.printf("Max Depth: %f, at X:%f\n", maxDepth, maxDepthPoint.getX() - (-centerOnXValue + xOffset));

        // This one is correlated, re-calculated
        Bezier bezierTransom = new Bezier(ctrlPointsTransom);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierTransom.getBezierPoint(t);
            bezierPointsTransom.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        // Extrapolate all the frames
        List<Bezier> frameBeziers = new ArrayList<>();
        // TODO _x <= ? Make sure the end has a frame...
        // TODO Displacement...
        for (double _x=(-centerOnXValue + xOffset) + frameIncrement; _x< /*=*/(-centerOnXValue + xOffset) + 550.0; _x+=frameIncrement) {
            System.out.printf("... Calculating frame %.03f... ", _x);
            long one = System.currentTimeMillis();
            boolean increase = (bezierRail.getBezierPoint(0).getX() < bezierRail.getBezierPoint(1).getX());
            double tx = bezierRail.getTForGivenX(0.0, 1E-1, _x, 1E-4, increase);
            Bezier.Point3D _top = bezierRail.getBezierPoint(tx);
            increase = (bezierKeel.getBezierPoint(0).getX() < bezierKeel.getBezierPoint(1).getX());
            tx = bezierKeel.getTForGivenX(0.0, 1E-1, _x, 1E-4, increase);
            Bezier.Point3D _bottom = bezierKeel.getBezierPoint(tx);

            List<Bezier.Point3D> ctrlPointsFrame = List.of(
                    new Bezier.Point3D(_x, _top.getY(), _top.getZ()),
                    new Bezier.Point3D(_x, _top.getY(), _bottom.getZ()),
                    new Bezier.Point3D(_x, _bottom.getY(), _bottom.getZ()));

            frameCtrlPts.add(ctrlPointsFrame);
            Bezier bezierFrame = new Bezier(ctrlPointsFrame);
            frameBeziers.add(bezierFrame);
            List<VectorUtils.Vector3D> bezierPointsFrame = new ArrayList<>();
            for (double t=0; t<=1.0; t+=0.01) {
                Bezier.Point3D tick = bezierFrame.getBezierPoint(t);
                bezierPointsFrame.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
            }
            frameBezierPts.add(bezierPointsFrame);
            long two = System.currentTimeMillis();
            System.out.printf(" in %s ms.\n", NumberFormat.getInstance().format(two - one));
        }

        if (waterlines) {
            // H lines

            // TODO Find the LWL
            hValues.forEach(z -> {
                System.out.println("Waterline for z=" + z);
                List<Bezier.Point3D> waterLine = new ArrayList<>();
                try {
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getZ() < bezierBow.getBezierPoint(1).getZ());
                    double tBow = bezierBow.getTForGivenZ(0, 1E-1, z, 1E-4, increasing);
                    if (tBow != -1) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
                        waterLine.add(bezierPoint);
                    }
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getZ() < bezier.getBezierPoint(1).getZ());
                        double t = bezier.getTForGivenZ(0, 1E-1, z, 1E-4, increase);
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                            waterLine.add(bezierPoint);
                        } else {
                            System.out.printf("Waterline not found for Z=%.02f, X=%.02f\n", z, bezier.getControlPoints().get(0).getX());
                            // Look on the keel? After the min
                            if (false) { // WiP...
                                double[] keelMinMax = bezierKeel.getMinMax(Bezier.Coordinate.Z, 1e-4);
                                // keelMinMax[0] + 0.1: Pb when finding an extremum...
                                double tMinKeel = bezierKeel.getTForGivenZ(0, 1e-1, keelMinMax[0] + 0.1, 1e-4, false);
                                if (tMinKeel != -1) {
                                    increase = true; // (bezierKeel.getBezierPoint(0).getZ() < bezierKeel.getBezierPoint(1).getZ());
                                    // Warning: keel goes down before going up! Hence the tMinKeel
                                    t = bezierKeel.getTForGivenZ(tMinKeel, 1E-1, z, 1E-4, increase);
                                    if (t != -1) {
                                        Bezier.Point3D bezierPoint = bezierKeel.getBezierPoint(t);
                                        waterLine.add(bezierPoint);
                                    }
                                } else {
                                    System.out.println("Min Keel not found!");
                                }
                            }
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getZ() < bezierTransom.getBezierPoint(1).getZ());
                    double tTransom = bezierTransom.getTForGivenZ(0, 1E-1, z, 1E-4, increasing);
                    if (tTransom != -1) {
                        Bezier.Point3D bezierPoint = bezierTransom.getBezierPoint(tTransom);
                        waterLine.add(bezierPoint);
                    }
                    // Add to the list
                    hLines.add(waterLine);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (buttocks) {
            // V lines
            vValues.forEach(y -> {
                System.out.println("Vline for y=" + y);
                List<Bezier.Point3D> vLine = new ArrayList<>();
                try {
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getY() < bezierBow.getBezierPoint(1).getY());
                    double tBow = bezierBow.getTForGivenY(0, 1E-1, y, 1E-4, increasing);
                    if (tBow != -1) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
                        vLine.add(bezierPoint);
                    }
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getY() < bezier.getBezierPoint(1).getY());
                        double t = bezier.getTForGivenY(0, 1E-1, y, 1E-4, increase);
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                            vLine.add(bezierPoint);
                        } else {
                            System.out.printf("Vline not found for Y=%.02f, X=%.02f\n", y, bezier.getControlPoints().get(0).getX());
                            // Look on the keel? After the min
                            if (false) {
                                // WiP...
                            }
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getY() < bezierTransom.getBezierPoint(1).getY());
                    double tTransom = bezierTransom.getTForGivenY(0, 1E-1, y, 1E-4, increasing);
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
    }

    private void correlate() {
        // 1 - Bow
        List<Bezier.Point3D> tempBow = new ArrayList<>();
        tempBow.addAll(ctrlPointsBow);
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
        ctrlPointsRail.forEach(cp -> {
            this.ctrlPointsRail.add(new Bezier.Point3D()
                    .x((-centerOnXValue + xOffset) + cp.getX())
                    .y(cp.getY())
                    .z(cp.getZ()));
        });
        correlate();
        this.repaint();
    }

    public void setKeelCtrlPoints(List<Bezier.Point3D> ctrlPointsKeel) {
        this.ctrlPointsKeel = new ArrayList<>();
        // Re-calculate with center and offset
        ctrlPointsKeel.forEach(cp -> {
            this.ctrlPointsKeel.add(new Bezier.Point3D()
                    .x((-centerOnXValue + xOffset) + cp.getX())
                    .y(cp.getY())
                    .z(cp.getZ()));
        });
        correlate();
        this.repaint();
    }
}
