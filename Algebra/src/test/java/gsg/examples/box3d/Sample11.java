package gsg.examples.box3d;

import bezier.Bezier;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgets;
import gsg.VectorUtils;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Draw several 3D Bezier curves.
 * With interactive Swing widgets.
 */
public class Sample11 {

    /**
     * Warning: this assumes that X is constantly growing/increasing
     * @param bezier
     * @param startAt Value for t
     * @param inc
     * @param x the one we look the t for
     * @param precision return when diff lower then precision
     * @return the t
     */
    private static double getTForGivenX(Bezier bezier, double startAt, double inc, double x, double precision) {
        double tForX = 0;
        for (double t=startAt; t<=1+inc; t+=inc) {  // TODO verify the limit
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            if (tick.getX() > x) { // Assume that X is always growing. !!
                if (Math.abs(tick.getX() - x) < precision) {
                    return t;
                } else {
                    return getTForGivenX(bezier, startAt - inc, inc / 10.0, x, precision);
                }
            }
        }
        return tForX; // means not found...
    }

    /**
     * Warning: this assumes that Z is constantly increasing if inc > 0, DEcreasing otherwise
     * @param bezier
     * @param startAt Value for t. 1 or 0 for the first iteration
     * @param inc
     * @param z the one we look the t for
     * @param precision return when diff lower then precision
     * @return the t
     */
    private static double getTForGivenZ(Bezier bezier, double startAt, double inc, double z, double precision) {
        // TODO if bezier has 2 points...
        double tForZ = 0;
        for (double t=startAt; (inc > 0 ? t<=1+inc : t>0); t+=inc) {  // TODO verify the limits
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            // Assume that Z is always growing or decreasing. !!
//            if ((inc > 0 && tick.getZ() > z) || (inc < 0 && tick.getZ() < z)) {
            if (tick.getZ() > z) {
                if (Math.abs(tick.getZ() - z) < precision) {
                    return t;
                } else {
                    return getTForGivenZ(bezier, startAt - Math.abs(inc), inc / 10.0, z, precision);
                }
            }
        }
        return tForZ; // means not found...
    }

    private final static String BOAT_PREFIX = "--justTheBoat=";
    private final static String CTRL_PREFIX = "--drawFrameCtrlPoints=";
    private final static String SYM_PREFIX = "--symmetrical=";
    private final static String INC_PREFIX = "--frameIncrement=";

    /**
     * @param args the command line arguments.
     */
    public static void main(String... args) {

        final int MIN_X =    0;
        final int MAX_X =  600;
        final int MIN_Y = -110;
        final int MAX_Y =  110;
        final int MIN_Z = -30;
        final int MAX_Z =  100;

        boolean _justTheBoat = true;

        boolean _symmetrical = true;
        boolean _drawFrameCtrlPoints = false;
        double _frameIncrement = 10.0;

        boolean waterline = false;

        double xOffset = 25.0;
        double centerOnXValue = 300.0;

        // Gradle can send
        // -PappArgs="--justTheBoat=false --drawFrameCtrlPoints=true --symmetrical=false --frameIncrement=50"
        System.out.printf("We have %d arg(s).\n", args.length);
        for (String arg : args) {
            System.out.println("Arg: " + arg);
            if (arg.startsWith(BOAT_PREFIX)) {
                _justTheBoat = "true".equals(arg.substring(BOAT_PREFIX.length()));
            } else if (arg.startsWith(CTRL_PREFIX)) {
                _drawFrameCtrlPoints = "true".equals(arg.substring(CTRL_PREFIX.length()));
            } else if (arg.startsWith(SYM_PREFIX)) {
                _symmetrical = "true".equals(arg.substring(SYM_PREFIX.length()));
            } else if (arg.startsWith(INC_PREFIX)) {
                _frameIncrement = Double.parseDouble(arg.substring(INC_PREFIX.length()));
            }
        }

        final boolean justTheBoat = _justTheBoat;
        final boolean drawFrameCtrlPoints = _drawFrameCtrlPoints;
        final boolean symmetrical = _symmetrical;
        final double frameIncrement = _frameIncrement;

//        System.out.println("Several variables in the code for you to play with:");
//        System.out.println("boolean justTheBoat = true;\n" +
//                "\n" +
//                "boolean symmetrical = true;\n" +
//                "boolean drawFrameCtrlPoints = false;\n" +
//                "double frameIncrement = 10.0;\n" +
//                "\n" +
//                "double xOffset = 25.0;\n" +
//                "double centerOnXValue = 300.0;");

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
        for (double t=0; t<=1.001; t+=0.01) { // TODO Verify that limit
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
        for (double t=0; t<=1.001; t+=0.01) { // TODO Limit
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

        List<Bezier.Point3D> waterLine = new ArrayList<>();

        for (double _x=(-centerOnXValue + xOffset) + frameIncrement; _x< /*=*/(-centerOnXValue + xOffset) + 550.0; _x+=frameIncrement) {
            double tx = getTForGivenX(bezierRail, 0.0, 1E-1, _x, 1E-4);
            Bezier.Point3D _top = bezierRail.getBezierPoint(tx);
            tx = getTForGivenX(bezierKeel, 0.0, 1E-1, _x, 1E-4);
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
        // Waterline?
        if (waterline) {
            // 1 - bow
            double z = 0; // Water Level
            // Those go from top to bottom
//            double t = getTForGivenZ(bezierBow, 1, -1 * 1E-1, z, 1E-4); // TODO 2-point Bezier
            frameBeziers.forEach(bezier -> {
                double t = getTForGivenZ(bezier, 1, -1 * 1E-1, z, 1E-4);
                Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                waterLine.add(bezierPoint);
//                System.out.printf("Aha! %f -> %s\n", t, bezier.getBezierPoint(t));
            });
        }

        long after = System.currentTimeMillis();
        System.out.printf("Point calculation took %S ms\n", NumberFormat.getInstance().format(after - before));

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

            // Waterline ?
            if (waterline) {
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

            long afterRend = System.currentTimeMillis();
//            System.out.printf("Rendering took %s ms\n", NumberFormat.getInstance().format(afterRend - beforeRend));
        };
        // Invoke the above
        box3D.setAfterDrawer(afterDrawer);

        ThreeDFrameWithWidgets frame = new ThreeDFrameWithWidgets(box3D, "A nice little Bézier boat");
        frame.setVisible(true);
    }
}
