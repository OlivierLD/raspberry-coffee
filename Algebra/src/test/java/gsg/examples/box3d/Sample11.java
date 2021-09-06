package gsg.examples.box3d;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgets;
import gsg.VectorUtils;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Draw several 3D Bezier curves.
 * With interactive Swing widgets.
 */
public class Sample11 {

    private final static String BOAT_PREFIX = "--justTheBoat=";
    private final static String CTRL_PREFIX = "--drawFrameCtrlPoints=";
    private final static String SYM_PREFIX = "--symmetrical=";
    private final static String WL_PREFIX = "--water-lines=";
    private final static String BUTTOCKS_PREFIX = "--buttocks=";
    private final static String INC_PREFIX = "--frameIncrement=";

    /**
     * @param args the command line arguments.
     */
    public static void main(String... args) {

        final int MIN_X =    0;
        final int MAX_X =  600;
        final int MIN_Y = -110;
        final int MAX_Y =  110;
        final int MIN_Z =  -30;
        final int MAX_Z =  100;

        boolean _justTheBoat = true;

        boolean _symmetrical = true;
        boolean _drawFrameCtrlPoints = false;
        double _frameIncrement = 10.0;
        boolean _wl = true;
        boolean _buttocks = true;

        double xOffset = 25.0;
        double centerOnXValue = 300.0;

        // Gradle can send
        /*
         *  -PappArgs="--justTheBoat=false \
         *             --drawFrameCtrlPoints=true \
         *             --symmetrical=false \
         *             --frameIncrement=50 \
         *             --water-lines=true \
         *             --buttocks=true"
         */
        System.out.printf("We have %d arg(s).\n", args.length);
        for (String arg : args) {
            System.out.println("Arg: " + arg);
            if (arg.startsWith(BOAT_PREFIX)) {
                _justTheBoat = "true".equals(arg.substring(BOAT_PREFIX.length()));
            } else if (arg.startsWith(CTRL_PREFIX)) {
                _drawFrameCtrlPoints = "true".equals(arg.substring(CTRL_PREFIX.length()));
            } else if (arg.startsWith(SYM_PREFIX)) {
                _symmetrical = "true".equals(arg.substring(SYM_PREFIX.length()));
            } else if (arg.startsWith(WL_PREFIX)) {
                _wl = "true".equals(arg.substring(WL_PREFIX.length()));
            } else if (arg.startsWith(BUTTOCKS_PREFIX)) {
                _buttocks = "true".equals(arg.substring(BUTTOCKS_PREFIX.length()));
            } else if (arg.startsWith(INC_PREFIX)) {
                _frameIncrement = Double.parseDouble(arg.substring(INC_PREFIX.length()));
            }
        }

        final boolean justTheBoat = _justTheBoat;
        final boolean drawFrameCtrlPoints = _drawFrameCtrlPoints;
        final boolean symmetrical = _symmetrical;
        final boolean waterlines = _wl;
        final boolean buttocks = _buttocks;
        final double frameIncrement = _frameIncrement;

        Box3D box3D = new Box3D(ThreeDFrameWithWidgets.DEFAULT_WIDTH, ThreeDFrameWithWidgets.DEFAULT_HEIGHT);
        box3D.setxMin(MIN_X - centerOnXValue);
        box3D.setxMax(MAX_X - centerOnXValue);
        box3D.setyMin(MIN_Y);
        box3D.setyMax(MAX_Y);
        box3D.setzMin(MIN_Z);
        box3D.setzMax(MAX_Z);

        box3D.setXLabelTransformer(x -> String.valueOf(x + 275));

        System.out.println("Starting points calculation");
        long before = System.currentTimeMillis();
        // Drop Ctrl Points here
//        List<Bezier.Point3D> ctrlPointsRail = List.of(  // Rail
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 10.000000, 75.000000),
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 115.714286, 116.785714, 48.571429),
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 377.142857, 111.428571, 48.571429),
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.0, 61.071429, 56.000000));

        List<Bezier.Point3D> ctrlPointsRail = List.of(  // Rail
                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, 75.000000), // PT B
                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 21.428571, 68.928571),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 69.642857, 86.785714, 47.500000),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 272.142857, 129.642857, 45.357143),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 65.0, 56.000000));  // PT X

        List<Bezier.Point3D> ctrlPointsBow = List.of( // Bow (Bow transom, actually)
//                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 10.000000, 75.000000),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, 75.000000), // PT B
                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, -5.000000)); // PT C

        List<Bezier.Point3D> ctrlPointsKeel = List.of( // Keel
                new Bezier.Point3D((-centerOnXValue + xOffset) + 0.000000, 0.000000, -5.000000), // PT C
                new Bezier.Point3D((-centerOnXValue + xOffset) + 290.357143, 0.000000, -29.642857),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 0.000000, 5.000000)); // PT A

        List<Bezier.Point3D> ctrlPointsTransom = List.of( // Transom
                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.0, 65.0, 56.000000),   // PT X
                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.0, 65.0, 5.642857),
                new Bezier.Point3D((-centerOnXValue + xOffset) + 550.000000, 0.000000, 5.000000)); // PT A

        // Generate the data, the Bézier curves.
        Bezier bezierRail = new Bezier(ctrlPointsRail);
        List<VectorUtils.Vector3D> bezierPointsRail = new ArrayList<>();
        for (double t=0; t<=1.001; t+=0.01) { // TODO Verify that limit (double...)
            Bezier.Point3D tick = bezierRail.getBezierPoint(t);
            bezierPointsRail.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
//            System.out.printf("Rail Bezier X: %f\n", tick.getX());
        }
        Bezier bezierBow = new Bezier(ctrlPointsBow);
        List<VectorUtils.Vector3D> bezierPointsBow = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierBow.getBezierPoint(t);
            bezierPointsBow.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierKeel = new Bezier(ctrlPointsKeel);
        List<VectorUtils.Vector3D> bezierPointsKeel = new ArrayList<>();
        for (double t=0; t<=1.001; t+=0.01) { // TODO Limit (double...)
            Bezier.Point3D tick = bezierKeel.getBezierPoint(t);
            bezierPointsKeel.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        Bezier bezierTransom = new Bezier(ctrlPointsTransom);
        List<VectorUtils.Vector3D> bezierPointsTransom = new ArrayList<>();
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierTransom.getBezierPoint(t);
            bezierPointsTransom.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }

        // Extrapolate all the frames
        List<List<Bezier.Point3D>> frameCtrlPts = new ArrayList<>();
        List<Bezier> frameBeziers = new ArrayList<>();
        List<List<VectorUtils.Vector3D>> frameBezierPts = new ArrayList<>();

        List<List<Bezier.Point3D>> hLines = new ArrayList<>();
        List<List<Bezier.Point3D>> vLines = new ArrayList<>();

        for (double _x=(-centerOnXValue + xOffset) + frameIncrement; _x< /*=*/(-centerOnXValue + xOffset) + 550.0; _x+=frameIncrement) {
            System.out.printf("... Calculating frame %.03f\n", _x);
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
        }

        if (waterlines) {
            // H lines
            List<Double> hValues = List.of(-10d, 0d, 10d, 20d, 30d, 40d, 50d);
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
                    // Transom?
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
            List<Double> vValues = List.of(20d, 40d, 60d, 80d, 100d);
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
                    // Transom?
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

        long after = System.currentTimeMillis();
        System.out.printf("Point calculation took %s ms\n", NumberFormat.getInstance().format(after - before));

        // Do something specific here, after the box drawing. What's drawn, actually.
        Consumer<Graphics2D> afterDrawer = g2d -> {
//            System.out.println("Starting rendering");
            long beforeRend = System.currentTimeMillis();
            // Link the control points
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            VectorUtils.Vector3D from = null;
            if (!justTheBoat) {
                // 1 - Rail
                for (Bezier.Point3D ctrlPoint : ctrlPointsRail) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsRail) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            box3D.drawSegment(g2d, from, to);
                        }
                        from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                    }
                }

                // 2 - Bow
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsBow) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsBow) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            box3D.drawSegment(g2d, from, to);
                        }
                        from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                    }
                }

                // 3 - Keel
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsKeel) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                // 4 - Transom (TODO Could be a frame?)
                from = null;
                for (Bezier.Point3D ctrlPoint : ctrlPointsTransom) {
                    if (from != null) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                }
                if (symmetrical) {
                    from = null;
                    for (Bezier.Point3D ctrlPoint : ctrlPointsTransom) {
                        if (from != null) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            box3D.drawSegment(g2d, from, to);
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
                                box3D.drawSegment(g2d, from, to);
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
                                    box3D.drawSegment(g2d, from, to);
                                }
                                from = new VectorUtils.Vector3D(ctrlPoint.getX(), -ctrlPoint.getY(), ctrlPoint.getZ());
                            }
                        }
                    }
                }
                // Plot the control points
                g2d.setColor(Color.BLUE);
                ctrlPointsRail.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    box3D.drawCircle(g2d, at, 6);
                });
                if (symmetrical) {
                    ctrlPointsRail.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        box3D.drawCircle(g2d, at, 6);
                    });
                }
                ctrlPointsBow.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    box3D.drawCircle(g2d, at, 6);
                });
                if (symmetrical) {
                    ctrlPointsBow.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        box3D.drawCircle(g2d, at, 6);
                    });
                }
                ctrlPointsKeel.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    box3D.drawCircle(g2d, at, 6);
                });

                ctrlPointsTransom.forEach(pt -> {
                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    box3D.drawCircle(g2d, at, 6);
                });
                if (symmetrical) {
                    ctrlPointsTransom.forEach(pt -> {
                        VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                        box3D.drawCircle(g2d, at, 6);
                    });
                }
                // Ctrl points for the frames
                if (drawFrameCtrlPoints) {
                    for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                        ctrlPts.forEach(pt -> {
                            VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                            box3D.drawCircle(g2d, at, 3);
                        });
                    }
                    if (symmetrical) {
                        for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) {
                            ctrlPts.forEach(pt -> {
                                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), -pt.getY(), pt.getZ());
                                box3D.drawCircle(g2d, at, 3);
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
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsRail.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsRail.get(i);
                    to = to.y(-to.getY()); // Whahaha!
                    if (from != null) {
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
            }
            from = null;
            for (int i=0; i<bezierPointsBow.size(); i++) {
                VectorUtils.Vector3D to = bezierPointsBow.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsBow.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsBow.get(i);
                    to = to.y(-to.getY());
                    if (from != null) {
                        box3D.drawSegment(g2d, from, to);
                    }
                    from = to;
                }
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
            if (symmetrical) {
                from = null;
                for (int i=0; i<bezierPointsTransom.size(); i++) {
                    VectorUtils.Vector3D to = bezierPointsTransom.get(i);
                    to = to.y(-to.getY());
                    if (from != null) {
                        box3D.drawSegment(g2d, from, to);
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
                        box3D.drawSegment(g2d, from, to);
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
                            box3D.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                }
            }

            if (waterlines) {
                for (List<Bezier.Point3D> waterLine : hLines) {
                    from = null;
                    for (Bezier.Point3D waterLinePt : waterLine) {
                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), waterLinePt.getY(), waterLinePt.getZ());
                        if (from != null) {
                            box3D.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                    if (symmetrical) {
                        from = null;
                        for (Bezier.Point3D waterLinePt : waterLine) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), -waterLinePt.getY(), waterLinePt.getZ());
                            if (from != null) {
                                box3D.drawSegment(g2d, from, to);
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
                            box3D.drawSegment(g2d, from, to);
                        }
                        from = to;
                    }
                    if (symmetrical) {
                        from = null;
                        for (Bezier.Point3D vLinePt : vLine) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(vLinePt.getX(), -vLinePt.getY(), vLinePt.getZ());
                            if (from != null) {
                                box3D.drawSegment(g2d, from, to);
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
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D, "A nice little Bézier boat");
        frame.setVisible(true);
    }
}
