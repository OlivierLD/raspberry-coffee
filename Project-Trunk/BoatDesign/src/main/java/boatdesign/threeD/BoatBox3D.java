package boatdesign.threeD;

import bezier.Bezier;
import boatdesign.ThreeViews;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Many hard-coded values and options...
 *
 * Also see -Dspit-out-points=true
 */
public class BoatBox3D extends Box3D {

    public final static String TYPE = "type";
    public final static String DATA = "data";
    public final static String FRAME = "FRAME";
    public final static String BEAM = "BEAM";
    public final static String WATERLINE = "WATERLINE";
    public final static String BUTTOCK = "BUTTOCK";

    private ThreeViews parent;

    private final static boolean verbose = false;

    // Default values, overridden from a config
    private final static int MIN_X =  -25;
    private final static int MAX_X =  575;
    private final static int MIN_Y = -200;
    private final static int MAX_Y =  200;
    private final static int MIN_Z =  -30;
    private final static int MAX_Z =  100;

    private final static int DEFAULT_LHT = 550;

    private double xOffset = 25.0;
    private double centerOnXValue = (MAX_X - MIN_X) / 2d;

    private double defaultLHT = 0d;

    // TODO A prm for the number of points per frame and beam (bezier's t)

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
    private boolean beams = true;
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

    public List<List<Bezier.Point3D>> getFrameCtrlPts() {
        return frameCtrlPts;
    }

    public List<List<Bezier.Point3D>> getBeamCtrlPts() {
        return beamCtrlPts;
    }

    public List<VectorUtils.Vector3D> getBezierPointsRail() {
        return bezierPointsRail;
    }

    public List<VectorUtils.Vector3D> getBezierPointsBow() {
        return bezierPointsBow;
    }

    public List<VectorUtils.Vector3D> getBezierPointsKeel() {
        return bezierPointsKeel;
    }

    public List<VectorUtils.Vector3D> getBezierPointsTransom() {
        return bezierPointsTransom;
    }

    public List<Bezier.Point3D> getTransomCtrlPoint() {
        return ctrlPointsTransom;
    }

    private List<List<Bezier.Point3D>> frameCtrlPts = new ArrayList<>();
    private List<List<Bezier.Point3D>> beamCtrlPts = new ArrayList<>();

    private List<VectorUtils.Vector3D> bezierPointsRail = new ArrayList<>();
    private List<VectorUtils.Vector3D> bezierPointsBow = new ArrayList<>();
    private List<VectorUtils.Vector3D> bezierPointsKeel = new ArrayList<>();
    // Extrapolated.
    private List<VectorUtils.Vector3D> bezierPointsTransom = new ArrayList<>();

    private List<List<VectorUtils.Vector3D>> frameBezierPts = new ArrayList<>();
    private List<List<VectorUtils.Vector3D>> beamBezierPts = new ArrayList<>();
    private List<List<Bezier.Point3D>> hLines = new ArrayList<>();
    private List<List<Bezier.Point3D>> vLines = new ArrayList<>();

    protected BoatBox3D instance = this;

    public BoatBox3D() {
//        super(ThreeDFrameWithWidgetsV2.DEFAULT_WIDTH, ThreeDFrameWithWidgetsV2.DEFAULT_HEIGHT); // TODO Move this in another constructor?
        this(MIN_X, MAX_X, MIN_Y, MAX_Y, MIN_Z, MAX_Z, DEFAULT_LHT);
    }
    public BoatBox3D(double minX,
                     double maxX,
                     double minY,
                     double maxY,
                     double minZ,
                     double maxZ,
                     double defaultLht) {
        this(minX, maxX, minY, maxY, minZ, maxZ, defaultLht, null);
    }

    public BoatBox3D(double minX,
                     double maxX,
                     double minY,
                     double maxY,
                     double minZ,
                     double maxZ,
                     double defaultLht,
                     ThreeViews parent) {

        // Display Logger Config?
        LogManager logManager = LogManager.getLogManager();
        if (parent != null) {
            this.parent = parent;
            System.out.printf("Log available in %s, level %s\nLog file pattern %s\n",
                    parent.getLogger().getName(),
                    parent.getLogger().getLevel().getName(),
                    logManager.getProperty("java.util.logging.FileHandler.pattern")
            );
            parent.getLogger().log(Level.INFO, "BoatBox3D Constructor.");
        }

        this.refreshValues(minX, maxX, minY, maxY, minZ, maxZ, defaultLht);
//        centerOnXValue = (maxX - minX) / 2.0; // defaultLht / 2.0;
//        xOffset = centerOnXValue - (defaultLht / 2);
//
//        this.setxMin(minX - centerOnXValue);
//        this.setxMax(maxX - centerOnXValue);
//        this.setyMin(minY);
//        this.setyMax(maxY);
//        this.setzMin(minZ);
//        this.setzMax(maxZ);
//
//        this.setXLabelTransformer(x -> String.valueOf(x + (defaultLht / 2.0)));

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
                if (frames && drawFrameCtrlPoints) {
                    try {
                        for (List<Bezier.Point3D> ctrlPts : frameCtrlPts) { // draw segments
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

                        // Manage the beamCtrlPoints
                        if (beams) {
                            for (List<Bezier.Point3D> ctrlPts : beamCtrlPts) { // draw segments. TODO Manage symmetrical
                                from = null;
                                for (Bezier.Point3D ctrlPoint : ctrlPts) {
                                    if (from != null) {
                                        VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                                        instance.drawSegment(g2d, from, to);
                                    }
                                    from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                                }
                            }
                        }
                    } catch (ConcurrentModificationException cme) {
                        if (this.parent != null) {
                            this.parent.getLogger().log(Level.WARNING, "Concurrent Modification", cme);
                        } else {
                            System.err.println(cme);
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
                    try {
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
                        // beamCtrlPoints. TODO Manage symmetrical
                        if (beams) {
                            for (List<Bezier.Point3D> ctrlPts : beamCtrlPts) {
                                ctrlPts.forEach(pt -> {
                                    VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                                    instance.drawCircle(g2d, at, 3);
                                });
                            }
                        }

                    } catch (ConcurrentModificationException cme) {
                        if (this.parent != null) {
                            this.parent.getLogger().log(Level.WARNING, "Concurrent Modification", cme);
                        } else {
                            System.err.println(cme);
                        }
                    }
                }
            }

            // The actual Béziers
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
                List<VectorUtils.Vector3D> otherBezierPointsTransom = new ArrayList<>();
                // Duplicate it, do not use the List, it would alter its points.
                bezierPointsTransom.stream().forEach(pt -> {
                    VectorUtils.Vector3D clone = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                    otherBezierPointsTransom.add(clone);
                });
                for (VectorUtils.Vector3D to : otherBezierPointsTransom) {
                    to = to.y(-to.getY());
                    if (from != null) {
                        instance.drawSegment(g2d, from, to);
                    }
                    from = to;
                }

            }

            // All the frames, and possibly beams.
            if (frames) {
                g2d.setStroke(new BasicStroke(1));
                try {
                    // Frames
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
                            // Duplicate it, do not use the List, it would alter its points.
                            List<VectorUtils.Vector3D> otherFrameBeziersPts = new ArrayList<>();
                            bezierPoints.stream().forEach(pt -> {
                                VectorUtils.Vector3D clone = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                                otherFrameBeziersPts.add(clone);
                            });
                            from = null;
                            for (VectorUtils.Vector3D to : otherFrameBeziersPts) {
                                to = to.y(-to.getY());
                                if (from != null) {
                                    instance.drawSegment(g2d, from, to);
                                }
                                from = to;
                            }
                        }
                    }
                    // Beams? (WiP)
                    if (beams) {
                        for (List<VectorUtils.Vector3D> bezierPoints : beamBezierPts) { // TODO Synchronize?
                            from = null;
                            for (VectorUtils.Vector3D to : bezierPoints) {
                                if (from != null) {
                                    instance.drawSegment(g2d, from, to);
                                }
                                from = to;
                            }
                            // TODO Manage symmetrical y/n
                        }
                    }

                } catch (ConcurrentModificationException cme) {
                    if (this.parent != null) {
                        this.parent.getLogger().log(Level.WARNING, "Concurrent Modification", cme);
                    } else {
                        System.err.println(cme);
                    }
                }
            }

            if (waterlines) { // Display
                try {
                    for (List<Bezier.Point3D> waterLine : hLines) {
                        // Is it waterline (z=0) ?
//                    if (waterLine.get(0).getZ() == 0) { // TODO Watch that, ... Some display -2 :(
                        if (waterLine.size() > 1 && Math.round(waterLine.get(1).getZ()) == 0) {
                            g2d.setColor(Color.BLUE);  /// WATERLINE Color
                            g2d.setStroke(new BasicStroke(2));
                        } else {
                            g2d.setColor(Color.RED);
                            g2d.setStroke(new BasicStroke(1));
                        }

                        from = null;
                        for (Bezier.Point3D waterLinePt : waterLine) {
                            VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), waterLinePt.getY(), waterLinePt.getZ());
                            if (from != null) {
                                if (Math.abs(from.getX() - to.getX()) <= frameIncrement) {
                                    instance.drawSegment(g2d, from, to);
                                } else {
                                    if (verbose) {
                                        System.out.printf("Skipping %s to %s (inc %f)", from, to, frameIncrement);
                                    }
                                }
                            }
                            from = to;
                        }
                        if (symmetrical) {
                            from = null;
                            for (Bezier.Point3D waterLinePt : waterLine) {
                                VectorUtils.Vector3D to = new VectorUtils.Vector3D(waterLinePt.getX(), -waterLinePt.getY(), waterLinePt.getZ());
                                if (from != null) {
                                    if (Math.abs(from.getX() - to.getX()) <= frameIncrement) {
                                        instance.drawSegment(g2d, from, to);
                                    } else {
                                        if (verbose) {
                                            System.out.printf("Skipping %s to %s (inc %f)", from, to, frameIncrement);
                                        }
                                    }
                                }
                                from = to;
                            }
                        }
                    }
                } catch (ConcurrentModificationException cme) {
                    if (this.parent != null) {
                        this.parent.getLogger().log(Level.WARNING, "Concurrent Modification", cme);
                    } else {
                        System.err.println(cme);
                    }
                }
            }
            if (buttocks) { // Display
                for (List<Bezier.Point3D> vLine : vLines) {

                    g2d.setStroke(new BasicStroke(1));

                    from = null;
                    try {
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
                    } catch (ConcurrentModificationException cme) {
                        if (this.parent != null) {
                            this.parent.getLogger().log(Level.WARNING, "Concurrent Modification", cme);
                        } else {
                            System.err.println(cme);
                        }
                    }
                }
            }

            long afterRend = System.currentTimeMillis();
//            System.out.printf("Rendering took %s ms\n", NumberFormat.getInstance().format(afterRend - beforeRend));
            // Center of Hull
            if (xCenterOfHull != -1 && zCenterOfHull != 1) {
                // (-centerOnXValue + xOffset)
                VectorUtils.Vector3D cc = new VectorUtils.Vector3D(xCenterOfHull + (-centerOnXValue + xOffset),
                        0.0,
                        zCenterOfHull);
                g2d.setColor(new Color(0, 102, 0, 200));
                int circleDiam = 6;
                instance.drawCircle(g2d, cc, circleDiam);
                instance.drawStringAt(g2d, cc, "CC", 0, -10, Justification.CENTER);
            }
        };
        // Invoke the above
        this.setAfterDrawer(afterDrawer);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//        System.out.println(">> Super!"); // For interception...
    }

    public void refreshValues(double minX,
                              double maxX,
                              double minY,
                              double maxY,
                              double minZ,
                              double maxZ,
                              double defaultLht) {
        this.defaultLHT = defaultLht;

        this.centerOnXValue = (maxX - minX) / 2.0; // defaultLht / 2.0;
        this.xOffset = centerOnXValue - (defaultLht / 2);

        this.setxMin(minX - centerOnXValue);
        this.setxMax(maxX - centerOnXValue);
        this.setyMin(minY);
        this.setyMax(maxY);
        this.setzMin(minZ);
        this.setzMax(maxZ);
        this.setXLabelTransformer(x -> String.valueOf(x + (defaultLht / 2.0)));
        // The text fields
        // . . .
    }

    public double getMinX() {
        return this.getxMin();
    }
    public double getMaxX() {
        return this.getxMax();
    }
    public double getMinY() {
        return this.getyMin();
    }
    public double getMaxY() {
        return this.getyMax();
    }
    public double getMinZ() {
        return this.getzMin();
    }
    public double getMaxZ() {
        return this.getzMax();
    }
    public double getDefaultLHT() {
        return this.defaultLHT;
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
    public boolean isBeams() {
        return beams;
    }

    public void setFrames(boolean frames) {
        this.frames = frames;
    }
    public void setBeams(boolean beams) {
        this.beams = beams;
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
//                    System.out.println("Found CC at X " + xCenterOfHull);
                    break;
                }
                disp.set(prevDisp + toAdd);
            }
            prevArea.set(area);
            prevX.set(x);
        }

        return 2 * displacement;
    }

    private double calculateZDisplacement(Map<Double, Double> displacementMap, double bottom) {
        AtomicReference<Double> disp = new AtomicReference<>(0d);
        AtomicReference<Double> prevArea = new AtomicReference<>(0d);
        AtomicReference<Double> prevZ = new AtomicReference<>(bottom); // 0d);

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
            if (this.parent != null) {
                this.parent.getLogger().log(Level.INFO, String.format("From Z Displ: %.03f m3\n", (displacement * 2)));
            } else {
                System.out.printf("From Z Displ: %.03f m3\n", (displacement * 2));
            }
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
            // TODO Add volume from the keel minimum.
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
                    if (this.parent != null) {
                        this.parent.getLogger().log(Level.INFO, String.format("Found CC at Z " + zCenterOfHull));
                    } else {
                        System.out.println("Found CC at Z " + zCenterOfHull);
                    }
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
//            if (pt.getX() - point.getX() == 0 &&
//                    pt.getY() - point.getY() == 0 &&
//                    pt.getZ() - point.getZ() == 0) {
            if (Math.abs(pt.getX() - point.getX()) < 1e-10 &&
                Math.abs(pt.getY() - point.getY()) < 1e-10 &&
                Math.abs(pt.getZ() - point.getZ()) < 1e-10) {
                return true;
            }
        }
        return false;
    }

    // Re-generates the boat
    private boolean keepWoring = true;
    public void setWorking(boolean b) {
        this.keepWoring = b;
    }

    public void refreshData() {
        refreshData(false, null, null, null);
    }
    public void refreshData(boolean localVerbose) {
        refreshData(localVerbose, null, null, null);
    }
    public void refreshData(boolean localVerbose,
                            Consumer<Map> callback,
                            Consumer<String> messCallback,
                            Consumer<Map> progressCallback) {

        this.keepWoring = true;
        long before = System.currentTimeMillis();
        // TODO Parameterize the t+=0.01

        // Max length, centerOnXValue, xOffset
        double maxLength = 550.0;
        double maxKeel = ctrlPointsKeel.stream()
                .mapToDouble(Bezier.Point3D::getX)
                .max()
                .getAsDouble();
        double maxRail = ctrlPointsRail.stream()
                .mapToDouble(Bezier.Point3D::getX)
                .max()
                .getAsDouble();

        maxLength = Math.max(maxKeel, maxRail) - (-centerOnXValue + xOffset);
        centerOnXValue = (maxLength / 2.0) + xOffset;

        // Reset all points
        this.frameCtrlPts = new ArrayList<>();
        this.beamCtrlPts = new ArrayList<>();
        this.bezierPointsRail = new ArrayList<>();
        this.bezierPointsBow = new ArrayList<>();
        this.bezierPointsKeel = new ArrayList<>();
        // Extrapolated.
        this.bezierPointsTransom = new ArrayList<>();

        this.frameBezierPts = new ArrayList<>();
        this.hLines = new ArrayList<>();
        this.vLines = new ArrayList<>();
        this.beamBezierPts = new ArrayList<>();

        // Center of Hull
        this.xCenterOfHull = -1;
        this.zCenterOfHull = -1;

        // Generate the data, the Bézier curves.

        // Also find the widest point
        double maxWidth = 0d, maxWidthX = 0d;
        double maxHeight = 0d;
        double maxDraft = 0d, maxDraftX = 0d;
        double lwl = 0.0, lwlStart = 0d, lwlEnd = 0d;
        Bezier.Point3D maxWidthPoint = null;
        if (messCallback != null) {
            messCallback.accept("Calculating rail");
        }
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
        if (localVerbose || verbose) {
            System.out.printf("Max Width: %f, at X:%f\n", maxWidth, maxWidthPoint.getX() - (-centerOnXValue + xOffset));
        }

        if (messCallback != null) {
            messCallback.accept("Calculating bow");
        }
        Bezier bezierBow = new Bezier(ctrlPointsBow);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierBow.getBezierPoint(t);
            bezierPointsBow.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        double tFor0 = 0;
        Bezier.Point3D wlPoint1 = null;
        try {
            tFor0 = bezierBow.getTForGivenZ(0.0, 1e-1, 0, 1e-4, false);
        } catch (Bezier.TooDeepRecursionException tdre) {
            if (this.parent != null) {
                this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
            } else {
                System.err.println(tdre);
            }
            tdre.printStackTrace();
            tFor0 = -1;
        }
        if (tFor0 != -1) {
            wlPoint1 = bezierBow.getBezierPoint(tFor0);
        }

        if (messCallback != null) {
            messCallback.accept("Calculating keel");
        }
        Bezier bezierKeel = new Bezier(ctrlPointsKeel);
        double startForLWLEnd = 0d;
        boolean dirForLWLEnd = true;
        if (wlPoint1 == null) {
            try {
                tFor0 = bezierKeel.getTForGivenZ(0.0, 1e-1, 0, 1e-4, false);
            } catch (Bezier.TooDeepRecursionException tdre) {
                if (this.parent != null) {
                    this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
                } else {
                    System.err.println(tdre);
                }
                tdre.printStackTrace();
                tFor0 = -1;
            }
            if (tFor0 != -1) {
                wlPoint1 = bezierKeel.getBezierPoint(tFor0);
//                startForLWLEnd = tFor0 + 1e-1;
//                dirForLWLEnd = false;
            }
        }
        // Also find the deepest point
        Bezier.Point3D maxDepthPoint = null;
        if (messCallback != null) {
            messCallback.accept("Calculating max depth");
        }
        double maxDepthT = -1d;
        for (double t=0; t<=1.001; t+=0.01) { // TODO Limit (double...)
            Bezier.Point3D tick = bezierKeel.getBezierPoint(t);
            bezierPointsKeel.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
            if (tick.getZ() < maxDraft) {
                maxDraft = tick.getZ();
                maxDepthPoint = tick;
                maxDepthT = t;
            }
        }
        if (maxDepthPoint != null) {
            maxDraftX = maxDepthPoint.getX() - (-centerOnXValue + xOffset);
        }
        if (localVerbose || verbose) {
            if (maxDepthPoint != null) {
                System.out.printf("Max Draft: %f, at X:%f\n", maxDraft, maxDepthPoint.getX() - (-centerOnXValue + xOffset));
            } else {
                System.out.println("No max draft");
            }
        }
        // End of LWL
        double t2For0 = -1d;
        try {
            t2For0 = bezierKeel.getTForGivenZ(maxDepthT + 0.2 /* TODO... Mmmh */, 1e-2, 0, 1e-4, true);
        } catch (Bezier.TooDeepRecursionException tdre) {
            if (this.parent != null) {
                this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
            } else {
                System.err.println(tdre);
            }
            tdre.printStackTrace();
            t2For0 = -1;
        }
        Bezier.Point3D wlPoint2 = bezierKeel.getBezierPoint(t2For0);
        if (wlPoint1 != null) {
            lwlStart = wlPoint1.getX() - (-centerOnXValue + xOffset);
        }
        lwlEnd = wlPoint2.getX() - (-centerOnXValue + xOffset);
        if (wlPoint1 != null) {
            lwl = wlPoint2.getX() - wlPoint1.getX();
        }
        if (localVerbose || verbose) {
            System.out.printf("LWL: %f\n", lwl);
        }

        // This one is correlated, re-calculated
        if (messCallback != null) {
            messCallback.accept("Calculating transom");
        }
        Bezier bezierTransom = new Bezier(ctrlPointsTransom);
        for (double t=0; t<=1.0; t+=0.01) {
            Bezier.Point3D tick = bezierTransom.getBezierPoint(t);
            if (tick.getY() < 0) {
                // Honk !!
                System.err.println("Negative Y in transom!!");
            }
            VectorUtils.Vector3D transomPt = new VectorUtils.Vector3D(tick.getX(), Math.abs(tick.getY()), tick.getZ());
            bezierPointsTransom.add(transomPt);
        }
        // TODO Transom beam here?

        /*
         * Rails and keel are done,
         * Now calculating the frames, waterlines, buttocks.
         */

        List<Bezier> frameBeziers = new ArrayList<>();
        List<Bezier> beamBeziers = new ArrayList<>();
        Map<Double, Double> displacementXMap = new LinkedHashMap<>();
        Map<Double, Double> displacementZMap = new LinkedHashMap<>();
        Map<Double, Double> fullVolumeXMap = new LinkedHashMap<>();
        double displ = 0d, fullVolume = 0d;
        double prismCoeff = 0d;
        // Actual shape calculation takes place here.
        if (frames || true) { // Do the calculations, even if display is not required.
            // Extrapolate all the frames (to the end of rail. could be the same as transom)
            // Displacement...
            double maxFrameArea = 0d;
            // TODO _x <= ? Make sure the end has a frame...
            for (double _x = (-centerOnXValue + xOffset) + frameIncrement;
                        this.keepWoring && _x </*=*/ (-centerOnXValue + xOffset) + maxLength + (0 * frameIncrement / 2); // TODO Check this (frameIncrement / 2)
                        _x += frameIncrement) {
                if (localVerbose || verbose) {
                    System.out.printf("... Calculating frame %.03f... ", _x);
                }
                if (messCallback != null) {
                    messCallback.accept(String.format("Calculating frame %.03f", _x));
                }
                long one = System.currentTimeMillis();
                boolean increase = (bezierRail.getBezierPoint(0).getX() < bezierRail.getBezierPoint(1).getX());
                double tx = 0d;
                boolean railOk = true;
                try {
                    tx = bezierRail.getTForGivenX(0.0, 1e-1, _x, 1e-4, increase);
                    // Same as keel/bow below, for rail/transom
//                    System.out.println(String.format("Rail: x:%f -> t:%f", _x, tx));
                    if (tx == -1d) {
                        railOk = false;
                        tx = bezierTransom.getTForGivenX(0.0, 1e-1, _x, 1e-4); // Not sure that's right...
                    }
                } catch (Bezier.TooDeepRecursionException tdre) {
                    if (this.parent != null) {
                        this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
                    } else {
                        System.err.println(tdre);
                    }
                    tdre.printStackTrace();
                    tx = -1;
                }
                Bezier.Point3D _top = railOk ? bezierRail.getBezierPoint(tx) : bezierTransom.getBezierPoint(tx);

                increase = (bezierKeel.getBezierPoint(0).getX() < bezierKeel.getBezierPoint(1).getX());
                boolean keelOk = true;
                try {
                    tx = bezierKeel.getTForGivenX(0.0, 1e-1, _x, 1e-4, increase);
//                    System.out.println(String.format("x:%f -> t:%f", _x, tx));
                    if (tx == -1d) { // Out of limits
                        keelOk = false;
                        tx = bezierBow.getTForGivenX(0.0, 1e-1, _x, 1e-4);
//                        System.out.println(String.format("Bow: x:%f -> t:%f", _x, tx));
                    }
                } catch (Bezier.TooDeepRecursionException tdre) {
                    if (this.parent != null) {
                        this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
                    } else {
                        System.err.println(tdre);
                    }
                    tdre.printStackTrace();
                    tx = -1;
                }
                Bezier.Point3D _bottom = keelOk ? bezierKeel.getBezierPoint(tx) : bezierBow.getBezierPoint(tx);

                List<Bezier.Point3D> ctrlPointsFrame = List.of(
                        new Bezier.Point3D(_x, _top.getY(), _top.getZ()),
                        new Bezier.Point3D(_x, _top.getY(), _bottom.getZ()),
                        new Bezier.Point3D(_x, _bottom.getY(), _bottom.getZ()));

                frameCtrlPts.add(ctrlPointsFrame);
                Bezier bezierFrame = new Bezier(ctrlPointsFrame);
                frameBeziers.add(bezierFrame);

                // Beam.
                // one on each rail, on in the middle, above, h (aka bouge) ~0.15 x total-width
                List<Bezier.Point3D> ctrlPointsBeam = List.of(
                        new Bezier.Point3D(_x, _top.getY(), _top.getZ()),
                        new Bezier.Point3D(_x, 0, _top.getZ() + (2 * _top.getY() * 0.15)),
                        new Bezier.Point3D(_x, - _top.getY(), _top.getZ()));

                beamCtrlPts.add(ctrlPointsBeam);
                Bezier bezierBeam = new Bezier(ctrlPointsBeam);
                beamBeziers.add(bezierBeam);

                // Create and add frame points
                List<VectorUtils.Vector3D> bezierPointsFrame = new ArrayList<>();
                double frameArea = 0.0;
                double prevY = -1.0;
                double prevZ = 0.0;
                for (double t = 0; t <= 1.0; t += 0.01) {
                    Bezier.Point3D tick = bezierFrame.getBezierPoint(t);
                    bezierPointsFrame.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                    if (tick.getZ() <= 0) { // Under waterline only
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
                // Iterate again to calculate volume
                double fullFrameArea = 0.0;
                for (double t = 0; t <= 1.0; t += 0.01) {
                    Bezier.Point3D tick = bezierFrame.getBezierPoint(t);
                    if (prevY != -1.0) {
                        double y = tick.getY();
                        // TODO Improve that. The top is not horizontal
                        // rectangle above
                        double rectAboveArea = Math.abs((y - prevY) * tick.getZ());
                        // triangle below
                        double triangleBelowArea = Math.abs((y - prevY) * (tick.getZ() - prevZ) / 2.0);
                        fullFrameArea += (rectAboveArea + triangleBelowArea);
                    }
                    prevY = tick.getY();
                    prevZ = tick.getZ();
                }
                if (fullFrameArea > 0) {
                    fullVolumeXMap.put(_x - (-centerOnXValue + xOffset), fullFrameArea);
                }
                if (localVerbose || verbose) {
                    System.out.printf("(area: %f) ", frameArea);
                }
                maxFrameArea = Math.max(maxFrameArea, frameArea); // For prismatic coeff.

                synchronized (frameBezierPts) {
                    frameBezierPts.add(bezierPointsFrame);
                    if (progressCallback != null) {
                        progressCallback.accept(Map.of(TYPE, FRAME,
                                                       DATA, bezierPointsFrame)); // Should be a List<VectorUtils.Vector3D>
                    }
                }

                // Beam
                List<VectorUtils.Vector3D> bezierPointsBeam = new ArrayList<>();
                for (double t = 0; t <= 1.0; t += 0.01) {
                    Bezier.Point3D tick = bezierBeam.getBezierPoint(t);
                    bezierPointsBeam.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }

                synchronized (beamBezierPts) {
                    beamBezierPts.add(bezierPointsBeam);
                    if (progressCallback != null) {
                        progressCallback.accept(Map.of(TYPE, BEAM,
                                                       DATA, bezierPointsBeam));
                    }
                }

                long two = System.currentTimeMillis();
                if (localVerbose || verbose) {
                    System.out.printf(" in %s ms.\n", NumberFormat.getInstance().format(two - one));
                }
            }
            // Transom at the end of all frames.
            if (progressCallback != null) {
                if (bezierPointsTransom != null && bezierPointsTransom.size() > 0 && bezierPointsTransom.get(0).getY() < 0) {
                    System.err.println("Negative Y in the transom!!");
                }
                progressCallback.accept(Map.of(TYPE, FRAME,
                                               DATA, bezierPointsTransom));
            }

            // Add last beam, for the transom (last rail point). TODO Add it to transom?
            if (this.keepWoring) {
                Bezier.Point3D _top = bezierRail.getBezierPoint(1.0);
                List<Bezier.Point3D> ctrlPointsBeam = List.of(
                        new Bezier.Point3D(_top.getX(), _top.getY(), _top.getZ()),
                        new Bezier.Point3D(_top.getX(), 0, _top.getZ() + (2 * _top.getY() * 0.15)),
                        new Bezier.Point3D(_top.getX(), - _top.getY(), _top.getZ()));
                beamCtrlPts.add(ctrlPointsBeam);
                Bezier bezierBeam = new Bezier(ctrlPointsBeam);
                beamBeziers.add(bezierBeam);

                List<VectorUtils.Vector3D> bezierPointsBeam = new ArrayList<>();
                for (double t = 0; t <= 1.0; t += 0.01) {
                    Bezier.Point3D tick = bezierBeam.getBezierPoint(t);
                    bezierPointsBeam.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
                synchronized (beamBezierPts) {
                    beamBezierPts.add(bezierPointsBeam); // Transom
                    if (progressCallback != null) {
                        progressCallback.accept(Map.of(TYPE, BEAM,
                                                       DATA, bezierPointsBeam));
                    }
                }
            }

            // displ?
            if (localVerbose || verbose) {
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
            if (displ > 0 && maxFrameArea != 0.0 && lwl != 0.0) {
                prismCoeff = displ / (2 * maxFrameArea * 1e-4 * lwl * 1e-2);
            }
            fullVolume = calculateDisplacement(fullVolumeXMap, 0, this.defaultLHT);

            if (localVerbose || verbose) {
                System.out.printf("\nCalculated displacement: %.03f m3\n\n", displ);
                System.out.printf("\nCalculated full volume: %.03f m3\n\n", fullVolume);
            }
        }

        if (waterlines && this.keepWoring) { // Construction. TODO Calculate anyway?
            // H lines. Use a step for waterlines. maxDepth, maxHeight, wlIncrement.
            double from = Math.ceil(maxDraft / wlIncrement) * wlIncrement;
            double to = Math.floor(maxHeight / wlIncrement) * wlIncrement;
            if (localVerbose || verbose) {
                System.out.printf("WL from %f to %f\n", from, to);
            }
            hValues = new ArrayList<>();
            // Bottom to top
            for (double wl = from; wl <= to; wl += wlIncrement) {
                hValues.add(wl);
            }
            hValues.forEach(z -> {
                if (localVerbose || verbose) {
                    System.out.println("Waterline for z=" + z);
                }
                if (messCallback != null) {
                    messCallback.accept(String.format("Calculating waterline %.03f", z));
                }
                List<Bezier.Point3D> waterLine = new ArrayList<>();
                // firstPoint, lastPoint for z ?
                Bezier.Point3D _lastWLPoint = null;
                Bezier.Point3D _firstWLPoint = null;
                double _min = Double.MAX_VALUE;
                double prevMin = Double.MAX_VALUE;
                Bezier.Point3D _previousKeelPoint = null;

                // Forward
                for (double _t=0.0; _t<=1.0; _t+=0.001) {
                    Bezier.Point3D keelBezierPoint = bezierKeel.getBezierPoint(_t);
                    _min = Math.min(_min, Math.abs(keelBezierPoint.getZ() - z));
                    if (prevMin < Math.abs(keelBezierPoint.getZ() - z)) {
//                        System.out.println("First closest found at " + _previousKeelPoint + ", min:" + prevMin);
                        _firstWLPoint = _previousKeelPoint;
                        break;
                    } else {
                        prevMin = _min;
                    }
                    _previousKeelPoint = keelBezierPoint;
                }
                // Backward
                _previousKeelPoint = null;
                _min = Double.MAX_VALUE;
                prevMin = Double.MAX_VALUE;
                for (double _t=1.0; _t>=0.0; _t-=0.001) {
                    Bezier.Point3D keelBezierPoint = bezierKeel.getBezierPoint(_t);
                    _min = Math.min(_min, Math.abs(keelBezierPoint.getZ() - z));
                    if (prevMin < Math.abs(keelBezierPoint.getZ() - z) && prevMin < 1.0) { // TODO prevMin < 1.0... YES!!!
//                        System.out.println("Second closest found at " + _previousKeelPoint + ", min:" + prevMin);
                        _lastWLPoint = _previousKeelPoint;
                        break;
                    } else {
                        prevMin = _min;
                    }
                    _previousKeelPoint = keelBezierPoint;
                }
                final Bezier.Point3D lastWLPoint = _lastWLPoint;
                final Bezier.Point3D firstWLPoint = _firstWLPoint;

                if (localVerbose || verbose) {
                    System.out.println("For Z:" + z + ", first WL point " + _firstWLPoint + ", last WL point " + _lastWLPoint);
                }

                try {
                    // double[] keelMinMax = bezierKeel.getMinMax(Bezier.Coordinate.Z, 1e-4);
                    AtomicBoolean noPointYet = new AtomicBoolean(true);
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getZ() < bezierBow.getBezierPoint(1).getZ());
                    double tBow = 0;
                    try {
                        tBow = bezierBow.getTForGivenZ(0, 1e-1, z, 1e-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tBow = -1;
                    }
                    if (tBow != -1 && tBow != Double.NaN) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
//                        System.out.println("WL " + z + " - 1 : x=" + bezierPoint.getX());
//                        if (!waterLine.contains(bezierPoint)) {
                        if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                            waterLine.add(bezierPoint);
                            noPointYet.set(false);
                        }
                    }
                    AtomicBoolean addTheFirstPoint = new AtomicBoolean(false);
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getZ() < bezier.getBezierPoint(1).getZ());
                        double t = 0;
                        try {
                            t = bezier.getTForGivenZ(0, 1e-1, z, 1e-4, increase);
                        } catch (Bezier.TooDeepRecursionException tdre) {
                            if (this.parent != null) {
                                this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
                            } else {
                                System.err.println(tdre);
                            }
                            tdre.printStackTrace();
                            t = -1;
                        }
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
//                            System.out.println("WL " + z + " - 2 : x=" + bezierPoint.getX() + ", bezier:" + bezier.getControlPoints().get(0).getX());
//                            if (!waterLine.contains(bezierPoint)) {
                            if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                                waterLine.add(bezierPoint);
                                noPointYet.set(false);
                            }
                        } else {
                            if (localVerbose || verbose) {
                                System.out.println("We will need to add the first point " + firstWLPoint);
                            }
                            addTheFirstPoint.set(true);
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getZ() < bezierTransom.getBezierPoint(1).getZ());
                    double tTransom = 0;
                    try {
                        tTransom = bezierTransom.getTForGivenZ(0, 1e-1, z, 1e-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tTransom = -1;
                    }
                    if (tTransom != -1) {
                        Bezier.Point3D bezierPoint = bezierTransom.getBezierPoint(tTransom);
                        if (!listContainsBezierPoint(waterLine, bezierPoint)) {
                            waterLine.add(bezierPoint);
                        }
                    } else {
//                        System.out.println("Z:" + z + ", ADDING last point: " + _lastWLPoint);
                        if (lastWLPoint != null && !listContainsBezierPoint(waterLine, lastWLPoint)) {
                            waterLine.add(lastWLPoint);
                        }
                    }
                    // Add first point if it exists
                    if (addTheFirstPoint.get()) {
                        if (firstWLPoint != null) {
                            waterLine.add(0, firstWLPoint); // Force in 1st po.
                        } else {
                            if (this.parent != null) {
                                this.parent.getLogger().log(Level.WARNING, "Argh! firstWLPoint is null.");
                            } else {
                                System.out.println("Argh! firstWLPoint is null.");
                            }
                        }
                    }
                    // Add to the list
                    synchronized (hLines) {
                        hLines.add(waterLine);
                        if (progressCallback != null) {
                            progressCallback.accept(Map.of(TYPE, WATERLINE,
                                                           DATA, waterLine));
                        }
                    }
                    // Displacement (CC's height/depth)?
                    if (z <= 0) { // In the water
                        // Add to displacementZMap
                        final AtomicReference<Double> wlArea = new AtomicReference<>(0d);
                        final AtomicReference<Double> prevX = new AtomicReference<>(-1d);
                        final AtomicReference<Double> prevY = new AtomicReference<>(0d);
                        waterLine.stream()   // X, Y. Recenter X
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
                        if (localVerbose || verbose) {
                            System.out.printf("WL %.02f: %f (cm 2)\n", z, wlArea.get());
                        }
                        displacementZMap.put(z, wlArea.get());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            double[] keelMinMax = bezierKeel.getMinMax(Bezier.Coordinate.Z, 1e-4);
            double zDispl = calculateZDisplacement(displacementZMap, keelMinMax[0]);
            if (this.parent != null) {
                this.parent.getLogger().log(Level.INFO, String.format("Disp on Z: %.05f m3, zCC: %.03f m\n", (zDispl), (zCenterOfHull * 1e-2)));
            } else {
                System.out.printf("Disp on Z: %.05f m3, zCC: %.03f m\n", (zDispl), (zCenterOfHull * 1e-2)); // TODO Display diff in disp in % ?
            }
        }
        if (buttocks && this.keepWoring) { // Calculate anyway?
            double from = buttockIncrement;
            double to = Math.floor(maxWidth / buttockIncrement) * buttockIncrement;
            if (localVerbose || verbose) {
                System.out.printf("Buttock from %f to %f\n", from, to);
            }
            vValues = new ArrayList<>();
            for (double buttock = from; buttock <= to; buttock += buttockIncrement) {
                vValues.add(buttock);
            }
            // V lines.
            vValues.forEach(y -> {
                if (localVerbose || verbose) {
                    System.out.println("Vline for y=" + y);
                }
                if (messCallback != null) {
                    messCallback.accept(String.format("Calculating buttock %.03f", y));
                }
                // First and last
                // firstPoint, lastPoint for y ?
                Bezier.Point3D _lastButtockPoint = null;
                Bezier.Point3D _firstButtockPoint = null;
                double _min = Double.MAX_VALUE;
                double prevMin = Double.MAX_VALUE;
                Bezier.Point3D _previousRailPoint = null;

                // Forward
                for (double _t=0.0; _t<=1.0; _t+=0.001) {
                    Bezier.Point3D railBezierPoint = bezierRail.getBezierPoint(_t);
                    _min = Math.min(_min, Math.abs(railBezierPoint.getY() - y));
                    if (prevMin < Math.abs(railBezierPoint.getY() - y)) {
//                        System.out.println("First closest found at " + _previousKeelPoint + ", min:" + prevMin);
                        _firstButtockPoint = _previousRailPoint;
                        break;
                    } else {
                        prevMin = _min;
                    }
                    _previousRailPoint = railBezierPoint;
                }
                // Backward
                _previousRailPoint = null;
                _min = Double.MAX_VALUE;
                prevMin = Double.MAX_VALUE;
                for (double _t=1.0; _t>=0.0; _t-=0.001) {
                    Bezier.Point3D railBezierPoint = bezierRail.getBezierPoint(_t);
                    _min = Math.min(_min, Math.abs(railBezierPoint.getY() - y));
                    if (prevMin < Math.abs(railBezierPoint.getY() - y) && prevMin < 1.0) { // TODO prevMin < 1.0... YES!!!
//                        System.out.println("Second closest found at " + _previousKeelPoint + ", min:" + prevMin);
                        _lastButtockPoint = _previousRailPoint;
                        break;
                    } else {
                        prevMin = _min;
                    }
                    _previousRailPoint = railBezierPoint;
                }
                final Bezier.Point3D lastButtockPoint = (_lastButtockPoint != null && _firstButtockPoint != null && (_lastButtockPoint.getX() > _firstButtockPoint.getX())) ?
                        _lastButtockPoint :
                        _lastButtockPoint != null ? _lastButtockPoint : null;
                final Bezier.Point3D firstButtockPoint = _firstButtockPoint;
                if (localVerbose || verbose) {
                    System.out.println("For Y:" + y + ", first point " + _firstButtockPoint + ", last point " + _lastButtockPoint);
                }

                List<Bezier.Point3D> vLine = new ArrayList<>();
                try {
                    // 1 - bow
                    boolean increasing = (bezierBow.getBezierPoint(0).getY() < bezierBow.getBezierPoint(1).getY());
                    double tBow = 0;
                    try {
                        tBow = bezierBow.getTForGivenY(0, 1e-1, y, 1e-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tBow = -1;
                    }
                    if (tBow != -1) {
                        Bezier.Point3D bezierPoint = bezierBow.getBezierPoint(tBow);
                        vLine.add(bezierPoint);
                    }
                    AtomicBoolean addTheFirstPoint = new AtomicBoolean(false);
                    frameBeziers.forEach(bezier -> {
                        boolean increase = (bezier.getBezierPoint(0).getY() < bezier.getBezierPoint(1).getY());
                        double t = 0.0;
                        try {
//                            System.out.printf("Looking for t, for Y=%f, and X=%f\n", y, bezier.getControlPoints().get(0).getX());
                            t = bezier.getTForGivenY(0, 1e-1, y, 1e-4, increase);
                        } catch (Bezier.TooDeepRecursionException tdre) {
                            if (this.parent != null) {
                                this.parent.getLogger().log(Level.WARNING, "Too deep recursion", tdre);
                            } else {
                                System.err.println(tdre);
                            }
                            tdre.printStackTrace();
                            t = -1;
                        }
                        if (t != -1) {
                            Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                            vLine.add(bezierPoint);
                        } else {
                            if (localVerbose || verbose) {
                                System.out.printf("Vline not found for Y=%.02f, X=%.02f\n", y, bezier.getControlPoints().get(0).getX());
                                System.out.println("We will need to add the first point " + firstButtockPoint);
                            }
                            addTheFirstPoint.set(true);
                        }
                    });
                    // Transom
                    increasing = (bezierTransom.getBezierPoint(0).getY() < bezierTransom.getBezierPoint(1).getY());
                    double tTransom = 0;
                    try {
                        tTransom = bezierTransom.getTForGivenY(0, 1e-1, y, 1e-4, increasing);
                    } catch (Bezier.TooDeepRecursionException tdre) {
                        tdre.printStackTrace();
                        tTransom = -1;
                    }
                    if (tTransom != -1) {
                        Bezier.Point3D bezierPoint = bezierTransom.getBezierPoint(tTransom);
                        vLine.add(bezierPoint);
                    } else {
                        if (lastButtockPoint != null && !listContainsBezierPoint(vLine, lastButtockPoint)) {
                            vLine.add(lastButtockPoint);
                        }
                    }
                    // Add first point if it exists
                    if (addTheFirstPoint.get()) {
                        if (firstButtockPoint != null) {
                            vLine.add(0, firstButtockPoint); // Force in 1st po.
                        } else {
                            if (this.parent != null) {
                                this.parent.getLogger().log(Level.WARNING, "Argh! firstButtockPoint is null.");
                            } else {
                                System.out.println("Argh!");
                            }
                        }
                    }
                    // Add to the list
                    synchronized (vLines) {
                        vLines.add(vLine);
                        if (progressCallback != null) {
                            progressCallback.accept(Map.of(TYPE, BUTTOCK,
                                                           DATA, vLine));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        if (callback != null) {
            Map<String, Object> callbackMessage =
                    Map.ofEntries(Map.entry("max-width", Double.valueOf(maxWidth * 1e-2)),
                            Map.entry("max-width-x", Double.valueOf(maxWidthX * 1e-2)),
                            Map.entry("max-height", Double.valueOf(maxHeight * 1e-2)),
                            Map.entry("max-depth", Double.valueOf(maxDraft * 1e-2)),
                            Map.entry("max-depth-x", Double.valueOf(maxDraftX * 1e-2)),
                            Map.entry("lwl", Double.valueOf(lwl * 1e-2)),
                            Map.entry("lwl-start", Double.valueOf(lwlStart * 1e-2)),
                            Map.entry("lwl-end", Double.valueOf(lwlEnd * 1e-2)),
                            Map.entry("displ-m3", Double.valueOf(displ)),
                            Map.entry("full-volume-m3", Double.valueOf(fullVolume)),
                            Map.entry("prism-coeff", Double.valueOf(prismCoeff)),
                            Map.entry("cc-x", Double.valueOf(xCenterOfHull * 1e-2)),
                            Map.entry("cc-z", Double.valueOf(zCenterOfHull * 1e-2)),
                            Map.entry("displacement-x-map",  displacementXMap));

//            String.format("Max Width: %f m (at %f m)\n" +
//                                    "Max height: %f m\n" +
//                                    "Max depth: %f m (at %f m)\n" +
//                                    "LWL: %f m (%f to %f)\n" +
//                                    "Displ: %f m3\n" +
//                                    "Prismatic Coeff: %f\n" +
//                                    "Center of hull at %f m (depth %f m)",
//                            (maxWidth * 1e-2), (maxWidthX * 1e-2),
//                            (maxHeight * 1e-2),
//                            (maxDepth * 1e-2), (maxDepthX * 1e-2),
//                            (lwl * 1e-2), (lwlStart * 1e-2), (lwlEnd * 1e-2),
//                            displ, prismCoeff,
//                            (xCenterOfHull * 1e-2), (zCenterOfHull * 1e-2));
            callback.accept(callbackMessage);
        }
        // Option: Spit out all calculated points. -Dspit-out-points=true
        if ("true".equals(System.getProperty("spit-out-points"))) {
            List<VectorUtils.Vector3D> allCalculatedPoints = new ArrayList<>();
            bezierPointsRail.stream().filter(pt -> bezierPointsRail.indexOf(pt) % 5 == 0)
                    .forEach(pt -> {
                        allCalculatedPoints.add(pt);
                        VectorUtils.Vector3D pt2 = new VectorUtils.Vector3D()
                                .x(pt.getX()).y(-pt.getY()).z(pt.getZ());
                        allCalculatedPoints.add(pt2);
                    });
            bezierPointsBow.stream().filter(pt -> bezierPointsBow.indexOf(pt) % 5 == 0).forEach(pt -> allCalculatedPoints.add(pt));
            bezierPointsKeel.stream().filter(pt -> bezierPointsKeel.indexOf(pt) % 5 == 0).forEach(pt -> allCalculatedPoints.add(pt));
            bezierPointsTransom.stream().filter(pt -> bezierPointsTransom.indexOf(pt) % 5 == 0)
                    .forEach(pt -> {
                        allCalculatedPoints.add(pt);
                        VectorUtils.Vector3D pt2 = new VectorUtils.Vector3D()
                                .x(pt.getX()).y(-pt.getY()).z(pt.getZ());
                        allCalculatedPoints.add(pt2);
                    });

            frameBezierPts.forEach(ptList -> {
                ptList.stream().filter(pt -> ptList.indexOf(pt) % 5 == 0).forEach(pt -> {
                    allCalculatedPoints.add(pt);
                    VectorUtils.Vector3D pt2 = new VectorUtils.Vector3D()
                            .x(pt.getX()).y(-pt.getY()).z(pt.getZ());
                    allCalculatedPoints.add(pt2);
                });
            });
            beamBezierPts.forEach(ptList -> {
                ptList.stream().filter(pt -> ptList.indexOf(pt) % 5 == 0).forEach(pt -> allCalculatedPoints.add(pt));
            });
            // Spit it out
            try {
                final ObjectMapper mapper = parent.getObjectMapper(); // new ObjectMapper();
                String allPoints = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allCalculatedPoints);
//            System.out.println(allPoints);
                File f = new File("calculated.json");
                FileWriter fw = new FileWriter(f);
                BufferedWriter out = new BufferedWriter(fw);
                out.write(allPoints + "\n");
                out.flush();
                out.close();
                System.out.printf(">> Check out %s\n", f.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        long after = System.currentTimeMillis();
        if (messCallback != null) {
            messCallback.accept(String.format("Refresh done in %s ms.", NumberFormat.getInstance().format(after - before)));
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

    public void setRailCtrlPoints(List<Bezier.Point3D> configCtrlPointsRail) {
        this.ctrlPointsRail = new ArrayList<>();
        // Re-calculate with center and offset
        configCtrlPointsRail.forEach(cp -> this.ctrlPointsRail.add(new Bezier.Point3D()
                .x((-centerOnXValue + xOffset) + cp.getX())
                .y(cp.getY())
                .z(cp.getZ())));
        correlate();
        this.repaint();
    }

    public void setKeelCtrlPoints(List<Bezier.Point3D> configCtrlPointsKeel) {
        this.ctrlPointsKeel = new ArrayList<>();
        // Re-calculate with center and offset
        configCtrlPointsKeel.forEach(cp -> this.ctrlPointsKeel.add(new Bezier.Point3D()
                .x((-centerOnXValue + xOffset) + cp.getX())
                .y(cp.getY())
                .z(cp.getZ())));
        correlate();
        this.repaint();
    }
}
