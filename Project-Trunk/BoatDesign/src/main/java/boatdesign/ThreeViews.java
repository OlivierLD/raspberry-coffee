package boatdesign;

import bezier.Bezier;
import boatdesign.threeD.BoatBox3D;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.SwingUtils.fullui.ThreeDPanelWithWidgets;
import gsg.VectorUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Using default WhiteBoard Writer
 *
 * 2D Bezier example. (<- So so...)
 * With draggable control points (hence the MouseListener, MouseMotionListener).
 *
 * 2 Bézier curves:
 * - One for the rail
 * - One for the keel
 *
 * Bow is correlated, transom too. See in {@link BoatBox3D}
 */
public class ThreeViews {

    private final static String TITLE = "3D Bezier Drawing Board. Rail and Keel.";

    private static JFrame frame;
    private ThreeDPanelWithWidgets threeDPanel;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileSpit = new JMenuItem();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Refresh Boat Shape"); // Not really useful here.

    private final static int WIDTH = 1536; // 1024;
    private final static int HEIGHT = 800;

    private static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Object> initConfig = null;

    /*
     * Some points are correlated.
     * First rail point and bow top
     * First keel point and bow bottom
     * Transom is defined by the last rail point and last keel point.
     * - Transom Control point(s) defined by them as well.
     */
    private List<Bezier.Point3D> railCtrlPoints = new ArrayList<>();
    private List<Bezier.Point3D> keelCtrlPoints = new ArrayList<>();

    // The WhiteBoard instantiations
    private WhiteBoardPanel whiteBoardXY = null; // from above
    private WhiteBoardPanel whiteBoardXZ = null; // side
    private WhiteBoardPanel whiteBoardYZ = null; // facing

    private Box3D box3D = null;

    private JTextPane dataTextArea = null;

    private static ThreeViews instance;

    private void fileSpit_ActionPerformed(ActionEvent ae) {
        System.out.println("Ctrl Points:\nRail:");
        this.railCtrlPoints.forEach(pt -> {
            System.out.println(String.format("%s", pt));
        });
        System.out.println("Keel:");
        this.keelCtrlPoints.forEach(pt -> {
            System.out.println(String.format("%s", pt));
        });
    }
    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.printf("Exit requested %s, bye now.\n", ae);
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.printf("Help requested %s\n", ae);
        JOptionPane.showMessageDialog(frame, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }
    private void refreshBoatShape() {
        Thread refresher = new Thread(() -> {
            System.out.println("Starting refresh...");
            // TODO Synchronization, ping for refresh/repaint. Stop thread if already running.
            ((BoatBox3D) this.box3D).refreshData();
            System.out.println("Refresh completed!");
            this.box3D.repaint();
        });
        refresher.start();
    }

    private void refreshData() {

        if (railCtrlPoints.size() > 0 && keelCtrlPoints.size() > 0) {

            // Tell the 3D box
            ((BoatBox3D)this.box3D).setRailCtrlPoints(railCtrlPoints); // The rail.
            ((BoatBox3D)this.box3D).setKeelCtrlPoints(keelCtrlPoints); // The keel.

            // Display in textArea
            String content = "Control Points:\nRail:\n" + railCtrlPoints.stream()
                    .map(pt -> String.format("%d: %s", railCtrlPoints.indexOf(pt), pt.toString()))
                    .collect(Collectors.joining("\n"));
            content += "\nKeel:\n" + keelCtrlPoints.stream()
                    .map(pt -> String.format("%d: %s", keelCtrlPoints.indexOf(pt), pt.toString()))
                    .collect(Collectors.joining("\n"));

            dataTextArea.setText(content);

            /*
             * Prepare data for display
             */

            // Generate the data, the Bézier curve(s).

            // 1 - Rail Ctrl Points
            Bezier railBezier = new Bezier(railCtrlPoints);
            List<VectorUtils.Vector3D> bezierRailPoints = new ArrayList<>(); // The points to display.
            if (railCtrlPoints.size() > 2) { // 3 points minimum.
                for (double t = 0; t <= 1.0; t += 1E-3) {
                    Bezier.Point3D tick = railBezier.getBezierPoint(t);
                    // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                    bezierRailPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }
            double[] xRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            double[] yRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            double[] zRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> railCtrlPtsXYVectors = new ArrayList<>();
            for (int i = 0; i < xRailCtrlPoints.length; i++) {
                railCtrlPtsXYVectors.add(new VectorUtils.Vector2D(xRailCtrlPoints[i], yRailCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> railCtrlPtsXZVectors = new ArrayList<>();
            for (int i = 0; i < xRailCtrlPoints.length; i++) {
                railCtrlPtsXZVectors.add(new VectorUtils.Vector2D(xRailCtrlPoints[i], zRailCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> railCtrlPtsYZVectors = new ArrayList<>();
            for (int i = 0; i < yRailCtrlPoints.length; i++) {
                railCtrlPtsYZVectors.add(new VectorUtils.Vector2D(yRailCtrlPoints[i], zRailCtrlPoints[i]));
            }

            // Curve points
            double[] xData = bezierRailPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            double[] yData = bezierRailPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            double[] zData = bezierRailPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> railDataXYVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                railDataXYVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
            }
            List<VectorUtils.Vector2D> railDataXZVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                railDataXZVectors.add(new VectorUtils.Vector2D(xData[i], zData[i]));
            }
            List<VectorUtils.Vector2D> railDataYZVectors = new ArrayList<>();
            for (int i = 0; i < yData.length; i++) {
                railDataYZVectors.add(new VectorUtils.Vector2D(yData[i], zData[i]));
            }
            // 2 - Keel Ctrl Points
            Bezier keelBezier = new Bezier(keelCtrlPoints);
            List<VectorUtils.Vector3D> bezierKeelPoints = new ArrayList<>(); // The points to display.
            if (keelCtrlPoints.size() > 2) { // 3 points minimum.
                for (double t = 0; t <= 1.0; t += 1E-3) {
                    Bezier.Point3D tick = keelBezier.getBezierPoint(t);
                    // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                    bezierKeelPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }
            double[] xKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            double[] yKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            double[] zKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> keelCtrlPtsXYVectors = new ArrayList<>();
            for (int i = 0; i < xKeelCtrlPoints.length; i++) {
                keelCtrlPtsXYVectors.add(new VectorUtils.Vector2D(xKeelCtrlPoints[i], yKeelCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> keelCtrlPtsXZVectors = new ArrayList<>();
            for (int i = 0; i < xKeelCtrlPoints.length; i++) {
                keelCtrlPtsXZVectors.add(new VectorUtils.Vector2D(xKeelCtrlPoints[i], zKeelCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> keelCtrlPtsYZVectors = new ArrayList<>();
            for (int i = 0; i < yKeelCtrlPoints.length; i++) {
                keelCtrlPtsYZVectors.add(new VectorUtils.Vector2D(yKeelCtrlPoints[i], zKeelCtrlPoints[i]));
            }

            // Curve points
            xData = bezierKeelPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            yData = bezierKeelPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            zData = bezierKeelPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> keelDataXYVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                keelDataXYVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
            }
            List<VectorUtils.Vector2D> keelDataXZVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                keelDataXZVectors.add(new VectorUtils.Vector2D(xData[i], zData[i]));
            }
            List<VectorUtils.Vector2D> keelDataYZVectors = new ArrayList<>();
            for (int i = 0; i < yData.length; i++) {
                keelDataYZVectors.add(new VectorUtils.Vector2D(yData[i], zData[i]));
            }

            whiteBoardXY.resetAllData();
            whiteBoardXZ.resetAllData();
            whiteBoardYZ.resetAllData();

            // Bezier ctrl points series
            // XY - Rail
            WhiteBoardPanel.DataSerie railCtrlXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(railCtrlPtsXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXY.addSerie(railCtrlXYSerie);
            // XY - Keel
            WhiteBoardPanel.DataSerie keelCtrlXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXY.addSerie(keelCtrlXYSerie);

            // XZ - Rail
            WhiteBoardPanel.DataSerie railCtrlXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railCtrlPtsXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXZ.addSerie(railCtrlXZSerie);
            // XZ - Keel
            WhiteBoardPanel.DataSerie keelCtrlXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXZ.addSerie(keelCtrlXZSerie);

            // YZ - Rail
            WhiteBoardPanel.DataSerie railCtrlYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railCtrlPtsYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardYZ.addSerie(railCtrlYZSerie);
            // YZ - Keel
            WhiteBoardPanel.DataSerie keelCtrlYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardYZ.addSerie(keelCtrlYZSerie);

            // Bezier points series
            // XY - Rail
            WhiteBoardPanel.DataSerie railDataXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXY.addSerie(railDataXYSerie);
            // XY - Keel
            WhiteBoardPanel.DataSerie keelDataXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXY.addSerie(keelDataXYSerie);

            // XZ - Rail
            WhiteBoardPanel.DataSerie railDataXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXZ.addSerie(railDataXZSerie);
            // XZ - Keel
            WhiteBoardPanel.DataSerie keelDataXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXZ.addSerie(keelDataXZSerie);

            // YZ - Rail
            WhiteBoardPanel.DataSerie railDataYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(railDataYZSerie);
            // YZ - Keel
            WhiteBoardPanel.DataSerie keelDataYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(keelDataYZSerie);

            // Finally, display it.
            whiteBoardXY.repaint();  // This is for a pure Swing context
            whiteBoardXZ.repaint();  // This is for a pure Swing context
            whiteBoardYZ.repaint();  // This is for a pure Swing context

            this.box3D.repaint();
        } else {
            dataTextArea.setText(" - No points -");
        }
    }

    private void show() {
        this.frame.setVisible(true);
    }

    private void initComponents() {

        if (initConfig != null) {
            Map<String, List<Object>> defaultPoints = (Map)initConfig.get("default-points");
            List<List<Double>> railPoints = (List)defaultPoints.get("rail");
            List<List<Double>> bowPoints = (List)defaultPoints.get("bow"); // Correlated, not needed here.
            List<List<Double>> keelPoints = (List)defaultPoints.get("keel");
            // Rail
            railPoints.forEach(pt -> {
                railCtrlPoints.add(new Bezier.Point3D(pt.get(0), pt.get(1), pt.get(2)));
            });
            // Keel
            keelPoints.forEach(pt -> {
                keelCtrlPoints.add(new Bezier.Point3D(pt.get(0), pt.get(1), pt.get(2)));
            });

        } else {
            // TODO There is a problem when ctrlPoints is empty... Fix it.
            // Initialize [0, 10, 0], [550, 105, 0]
            railCtrlPoints.add(new Bezier.Point3D(0, 10, 0));
            railCtrlPoints.add(new Bezier.Point3D(550, 105, 0));

            keelCtrlPoints.add(new Bezier.Point3D(10d, 0d, -5d));
            keelCtrlPoints.add(new Bezier.Point3D(550d, 0d, 5d));
        }
        // Tell the 3D box
        ((BoatBox3D)this.box3D).setRailCtrlPoints(railCtrlPoints); // The rail.
        ((BoatBox3D)this.box3D).setKeelCtrlPoints(keelCtrlPoints); // The keel.

        // Override defaults (not mandatory)

        // XY
        whiteBoardXY.setAxisColor(Color.BLACK);
        whiteBoardXY.setGridColor(Color.GRAY);
        whiteBoardXY.setForceTickIncrement(50);
        whiteBoardXY.setEnforceXAxisAt(0d);
        whiteBoardXY.setEnforceYAxisAt(0d);

        whiteBoardXY.setWithGrid(true);
        whiteBoardXY.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXY.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXY.setSize(new Dimension(800, 200));
        whiteBoardXY.setPreferredSize(new Dimension(600, 200));
        whiteBoardXY.setTextColor(Color.RED);
        whiteBoardXY.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoardXY.setGraphicMargins(30);
        whiteBoardXY.setXEqualsY(true); // false);
        // Enforce Y amplitude
        whiteBoardXY.setForcedMinY(0d);
        whiteBoardXY.setForcedMaxY(150d);

        // XZ
        whiteBoardXZ.setAxisColor(Color.BLACK);
        whiteBoardXZ.setGridColor(Color.GRAY);
        whiteBoardXZ.setForceTickIncrement(50);
        whiteBoardXZ.setEnforceXAxisAt(0d);
        whiteBoardXZ.setEnforceYAxisAt(0d);

        whiteBoardXZ.setWithGrid(true);
        whiteBoardXZ.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXZ.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXZ.setSize(new Dimension(800, 200));
        whiteBoardXZ.setPreferredSize(new Dimension(600, 200));
        whiteBoardXZ.setTextColor(Color.RED);
        whiteBoardXZ.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoardXZ.setGraphicMargins(30);
        whiteBoardXZ.setXEqualsY(true); // false);
        // Enforce Y amplitude
        whiteBoardXZ.setForcedMinY(-50d);
        whiteBoardXZ.setForcedMaxY(100d);

        // YZ
        whiteBoardYZ.setAxisColor(Color.BLACK);
        whiteBoardYZ.setGridColor(Color.GRAY);
        whiteBoardYZ.setForceTickIncrement(50);
        whiteBoardYZ.setEnforceXAxisAt(0d);
        whiteBoardYZ.setEnforceYAxisAt(0d);

        whiteBoardYZ.setWithGrid(true);
        whiteBoardYZ.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardYZ.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardYZ.setSize(new Dimension(400, 200));
        whiteBoardYZ.setPreferredSize(new Dimension(400, 200));
        whiteBoardYZ.setTextColor(Color.RED);
        whiteBoardYZ.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoardYZ.setGraphicMargins(30);
        whiteBoardYZ.setXEqualsY(true); // false);
        // Enforce Y amplitude
        whiteBoardYZ.setForcedMinY(-50d);
        whiteBoardYZ.setForcedMaxY(100d);

        // ThreeViewsV2 instance = this;

        whiteBoardXY.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                System.out.println("Click on whiteboard");
                if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
                    Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXY, Orientation.XY);
                    if (closePoint != null) {
                        BezierPopup popup = new BezierPopup(instance, closePoint);
                        popup.show(whiteBoardXY, e.getX(), e.getY());
                    }
                } else {
                    // Regular click.
                    // Drop point here. Where is the list
                    AddCtrlPointPanel addPointPanel = new AddCtrlPointPanel(railCtrlPoints.size(), keelCtrlPoints.size());
                    int response = JOptionPane.showConfirmDialog(frame,
                            addPointPanel,
                            "Add Control Point",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
//                  System.out.println("Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                            int height = whiteBoardXY.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(height - e.getY());
//                              System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).y(newY);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                List<Bezier.Point3D> origList = curve == AddCtrlPointPanel.CurveName.RAIL ? railCtrlPoints: keelCtrlPoints;
                                for (int i=0; i<newIndex; i++) {
                                    newList.add(origList.get(i));
                                }
                                newList.add(point3D);
                                for (int i = newIndex; i< origList.size(); i++) {
                                    newList.add(origList.get(i));
                                }
                                if (curve == AddCtrlPointPanel.CurveName.RAIL) {
                                    railCtrlPoints = newList;
                                } else {
                                    keelCtrlPoints = newList;
                                }
                                System.out.printf("List now has %d elements.\n", origList.size());
                                refreshData();
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
//        System.out.printf("Mouse clicked x: %d y: %d\n", e.getX(), e.getY());
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXY, Orientation.XY);
                if (closePoint != null) {
//            System.out.println("Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardXY.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                System.out.println("Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                    int height = whiteBoardXY.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newX = canvasToSpaceXTransformer.apply(e.getX());
                        double newY = canvasToSpaceYTransformer.apply(height - e.getY());
//                System.out.printf("Point dragged to %f / %f\n", newX, newY);
                        if (closestPointIndex < railCtrlPoints.size()) {
                            Bezier.Point3D point3D = railCtrlPoints.get(closestPointIndex);
                            point3D.x(newX).y(newY);
                        } else {
                            Bezier.Point3D point3D = keelCtrlPoints.get(closestPointIndex - railCtrlPoints.size());
                            point3D.x(newX).y(newY);
                        }
                        refreshData();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
//                System.out.println("Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXY, Orientation.XY);
                if (closePoint != null) {
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
                        System.out.println("Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
                } else {
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }

            }
        });

        whiteBoardXZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                System.out.println("Click on whiteboard");
                if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
                    Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                    if (closePoint != null) {
                        BezierPopup popup = new BezierPopup(instance, closePoint);
                        popup.show(whiteBoardXZ, e.getX(), e.getY());
                    }
                } else {
                    // Regular click.
                    // Drop point here. Where is the list
                    AddCtrlPointPanel addPointPanel = new AddCtrlPointPanel(railCtrlPoints.size(), keelCtrlPoints.size());
                    int response = JOptionPane.showConfirmDialog(frame,
                            addPointPanel,
                            "Add Control Point",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
//                  System.out.println("Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                            int height = whiteBoardXY.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(height - e.getY());
//                              System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).y(newY);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                List<Bezier.Point3D> origList = curve == AddCtrlPointPanel.CurveName.RAIL ? railCtrlPoints: keelCtrlPoints;
                                for (int i=0; i<newIndex; i++) {
                                    newList.add(origList.get(i));
                                }
                                newList.add(point3D);
                                for (int i = newIndex; i< origList.size(); i++) {
                                    newList.add(origList.get(i));
                                }
                                if (curve == AddCtrlPointPanel.CurveName.RAIL) {
                                    railCtrlPoints = newList;
                                } else {
                                    keelCtrlPoints = newList;
                                }
                                System.out.printf("List now has %d elements.\n", origList.size());
                                refreshData();
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
//        System.out.printf("Mouse clicked x: %d y: %d\n", e.getX(), e.getY());
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                if (closePoint != null) {
//            System.out.println("Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardXZ.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                System.out.println("Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXZ.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXZ.getCanvasToSpaceYTransformer();
                    int height = whiteBoardXZ.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newX = canvasToSpaceXTransformer.apply(e.getX());
                        double newZ = canvasToSpaceYTransformer.apply(height - e.getY());
//                System.out.printf("Point dragged to %f / %f\n", newX, newY);
                        if (closestPointIndex < railCtrlPoints.size()) {
                            Bezier.Point3D point3D = railCtrlPoints.get(closestPointIndex);
                            point3D.x(newX).z(newZ);
                        } else {
                            Bezier.Point3D point3D = keelCtrlPoints.get(closestPointIndex - railCtrlPoints.size());
                            point3D.x(newX).z(newZ);
                        }
                        refreshData();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
//                System.out.println("Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                if (closePoint != null) {
                    whiteBoardXZ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
                        System.out.println("Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
//                    closestPointIndex = railCtrlPoints.indexOf(closePoint);
                } else {
                    whiteBoardXZ.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }

            }
        });

        whiteBoardYZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                System.out.println("Click on whiteboard");
                if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
                    Bezier.Point3D closePoint = getClosePoint(e, whiteBoardYZ, Orientation.YZ);
                    if (closePoint != null) {
                        BezierPopup popup = new BezierPopup(instance, closePoint);
                        popup.show(whiteBoardYZ, e.getX(), e.getY());
                    }
                } else {
                    // Regular click.
                    // Drop point here. Where is the list
                    AddCtrlPointPanel addPointPanel = new AddCtrlPointPanel(railCtrlPoints.size(), keelCtrlPoints.size());
                    int response = JOptionPane.showConfirmDialog(frame,
                            addPointPanel,
                            "Add Control Point",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
//                  System.out.println("Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                            int height = whiteBoardXY.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(height - e.getY());
//                              System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).y(newY);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                List<Bezier.Point3D> origList = curve == AddCtrlPointPanel.CurveName.RAIL ? railCtrlPoints : keelCtrlPoints;
                                for (int i = 0; i < newIndex; i++) {
                                    newList.add(origList.get(i));
                                }
                                newList.add(point3D);
                                for (int i = newIndex; i < origList.size(); i++) {
                                    newList.add(origList.get(i));
                                }
                                if (curve == AddCtrlPointPanel.CurveName.RAIL) {
                                    railCtrlPoints = newList;
                                } else {
                                    keelCtrlPoints = newList;
                                }
                                System.out.printf("List now has %d elements.\n", origList.size());
                                refreshData();
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
//        System.out.printf("Mouse clicked x: %d y: %d\n", e.getX(), e.getY());
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardYZ, Orientation.YZ);
                if (closePoint != null) {
//            System.out.println("Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardYZ.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                System.out.println("Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardYZ.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardYZ.getCanvasToSpaceYTransformer();
                    int height = whiteBoardYZ.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newY = canvasToSpaceXTransformer.apply(e.getX());
                        double newZ = canvasToSpaceYTransformer.apply(height - e.getY());
//                System.out.printf("Point dragged to %f / %f\n", newX, newY);
                        if (closestPointIndex < railCtrlPoints.size()) {
                            Bezier.Point3D point3D = railCtrlPoints.get(closestPointIndex);
                            point3D.y(newY).z(newZ);
                        } else {
                            Bezier.Point3D point3D = keelCtrlPoints.get(closestPointIndex - railCtrlPoints.size());
                            point3D.y(newY).z(newZ);
                        }
                        refreshData();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
//                System.out.println("Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardYZ, Orientation.YZ);
                if (closePoint != null) {
                    whiteBoardYZ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
                        System.out.println("Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
//                    closestPointIndex = railCtrlPoints.indexOf(closePoint);
                } else {
                    whiteBoardYZ.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }

            }
        });

        // The JFrame
        frame = new JFrame(TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        frameSize.height = Math.min(frameSize.height, screenSize.height);
        frameSize.width  = Math.min(frameSize.width, screenSize.width);
        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(WIDTH, HEIGHT + 50 + 10); // 50: ... menu, title bar, etc. 10: button
            frame.setSize(frameSize);
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        refreshButton.addActionListener(e -> refreshBoatShape());

        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        menuFile.setText("File");
        menuFileSpit.setText("Spit out points");
        menuFileSpit.addActionListener(ae -> fileSpit_ActionPerformed(ae));
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(ae -> fileExit_ActionPerformed(ae));
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(ae -> helpAbout_ActionPerformed(ae));
        menuFile.add(menuFileSpit);
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
        menuHelp.add(menuHelpAbout);
        menuBar.add(menuHelp);

        topLabel = new JLabel(" " + TITLE); // Ugly left padding!
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        JPanel whiteBoardsPanel = new JPanel(new GridBagLayout());

//        JLabel label1 = new JLabel("Label 1");
//        JLabel label2 = new JLabel("Label 2");
//        JLabel label3 = new JLabel("Label 3");

        JPanel ctrlPointsPanel = new JPanel(new BorderLayout());
        ctrlPointsPanel.setBorder(BorderFactory.createTitledBorder("Data Placeholder"));
        dataTextArea = new JTextPane();
        dataTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        dataTextArea.setPreferredSize(new Dimension(300, 300));
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);

        ctrlPointsPanel.add(dataScrollPane, BorderLayout.NORTH);

        whiteBoardsPanel.add(whiteBoardXZ,         // Side
                new GridBagConstraints(0,
                        0,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.NONE,
                        new Insets(10, 5, 0, 5), 0, 0));
        whiteBoardsPanel.add(whiteBoardYZ,       // Face
                new GridBagConstraints(0,
                        1,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.NONE,
                        new Insets(10, 5, 0, 5), 0, 0));
        whiteBoardsPanel.add(whiteBoardXY,       // From above
                new GridBagConstraints(0,
                        2,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.NORTH,
                        GridBagConstraints.NONE,
                        new Insets(10, 5, 0, 5), 0, 0));

        JPanel topWidgetsPanel = new JPanel(new GridBagLayout());
        JCheckBox justBoatCheckBox = new JCheckBox("Just boat");
        JCheckBox symmetricCheckBox = new JCheckBox("Symmetrical");
        JCheckBox waterlinesCheckBox = new JCheckBox("Waterlines");
        JCheckBox buttocksCheckBox = new JCheckBox("Buttocks");
        JCheckBox ctrlPointsCheckBox = new JCheckBox("Ctrl-Points");

        JPanel frameStepPanel = new JPanel();
        JFormattedTextField frameStepValue = new JFormattedTextField(new DecimalFormat("#0.0"));
        frameStepValue.setValue(((BoatBox3D)this.box3D).getFrameIncrement());
        frameStepValue.setPreferredSize(new Dimension(60, 20));
        frameStepValue.setHorizontalAlignment(SwingConstants.RIGHT);
        frameStepValue.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValue();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValue();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValue();
            }
            public void checkValue() {
                if (!frameStepValue.getText().trim().isEmpty()) {
                    try {
                        double val = Double.parseDouble(frameStepValue.getText());
                        ((BoatBox3D)box3D).setFrameIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe.toString());
                    }
                }
            }
        });
        frameStepPanel.add(new JLabel("Frame Step:"));
        frameStepPanel.add(frameStepValue);

        JPanel wlStepPanel = new JPanel();
        JFormattedTextField wlStepValue = new JFormattedTextField(new DecimalFormat("#0.0"));
        wlStepValue.setValue(((BoatBox3D)this.box3D).getWlIncrement());
        wlStepValue.setPreferredSize(new Dimension(60, 20));
        wlStepValue.setHorizontalAlignment(SwingConstants.RIGHT);
        wlStepValue.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValue();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValue();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValue();
            }
            public void checkValue() {
                if (!wlStepValue.getText().trim().isEmpty()) {
                    try {
                        double val = Double.parseDouble(wlStepValue.getText());
                        ((BoatBox3D)box3D).setWlIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe.toString());
                    }
                }
            }
        });
        wlStepPanel.add(new JLabel("Waterline Step:"));
        wlStepPanel.add(wlStepValue);

        JPanel buttockStepPanel = new JPanel();
        JFormattedTextField buttockStepValue = new JFormattedTextField(new DecimalFormat("#0.0"));
        buttockStepValue.setValue(((BoatBox3D)this.box3D).getButtockIncrement());
        buttockStepValue.setPreferredSize(new Dimension(60, 20));
        buttockStepValue.setHorizontalAlignment(SwingConstants.RIGHT);
        buttockStepValue.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValue();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValue();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValue();
            }
            public void checkValue() {
                if (!buttockStepValue.getText().trim().isEmpty()) {
                    try {
                        double val = Double.parseDouble(buttockStepValue.getText());
                        ((BoatBox3D)box3D).setButtockIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe.toString());
                    }
                }
            }
        });
        buttockStepPanel.add(new JLabel("Buttock Step:"));
        buttockStepPanel.add(buttockStepValue);

        boolean justBoatSelected = ((BoatBox3D)this.box3D).isJustTheBoat();
        justBoatCheckBox.setSelected(((BoatBox3D)this.box3D).isJustTheBoat());
        justBoatCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox)evt.getSource()).isSelected();
            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            ((BoatBox3D)this.box3D).setJustTheBoat(selected);
            this.box3D.repaint();
            ctrlPointsCheckBox.setEnabled(!selected);
        });
        ctrlPointsCheckBox.setEnabled(!justBoatSelected);

        symmetricCheckBox.setSelected(((BoatBox3D)this.box3D).isSymmetrical());
        symmetricCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox)evt.getSource()).isSelected();
            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            ((BoatBox3D)this.box3D).setSymmetrical(selected);
            this.box3D.repaint();
        });
        waterlinesCheckBox.setSelected(((BoatBox3D)this.box3D).isWaterlines());
        waterlinesCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox)evt.getSource()).isSelected();
            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            ((BoatBox3D)this.box3D).setWaterlines(selected);
            this.box3D.repaint();
        });
        buttocksCheckBox.setSelected(((BoatBox3D)this.box3D).isButtocks());
        buttocksCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox)evt.getSource()).isSelected();
            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            ((BoatBox3D)this.box3D).setButtocks(selected);
            this.box3D.repaint();
        });
        ctrlPointsCheckBox.setSelected(((BoatBox3D)this.box3D).isDrawFrameCtrlPoints());
        ctrlPointsCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox)evt.getSource()).isSelected();
            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            ((BoatBox3D)this.box3D).setDrawFrameCtrlPoints(selected);
            this.box3D.repaint();
        });

        // Line 1
        topWidgetsPanel.add(justBoatCheckBox,
                new GridBagConstraints(0,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(symmetricCheckBox,
                new GridBagConstraints(1,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        topWidgetsPanel.add(waterlinesCheckBox,
                new GridBagConstraints(2,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(buttocksCheckBox,
                new GridBagConstraints(3,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(ctrlPointsCheckBox,
                new GridBagConstraints(4,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        // Line 2
        topWidgetsPanel.add(frameStepPanel,
                new GridBagConstraints(0,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(wlStepPanel,
                new GridBagConstraints(1,
                       1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(buttockStepPanel,
                new GridBagConstraints(2,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        JPanel rightPane = new JPanel(new BorderLayout());
        rightPane.add(topWidgetsPanel, BorderLayout.NORTH);
        rightPane.add(threeDPanel, BorderLayout.CENTER);
        rightPane.add(ctrlPointsPanel, BorderLayout.SOUTH);

        whiteBoardsPanel.add(rightPane,
                new GridBagConstraints(1,
                        0,
                        1,
                        3,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

//        whiteBoardsPanel.add(threeDPanel, // ctrlPointsPanel,
//                new GridBagConstraints(1,
//                        1,
//                        1,
//                        2,
//                        1.0,
//                        0.0,
//                        GridBagConstraints.WEST,
//                        GridBagConstraints.BOTH,
//                        new Insets(0, 0, 0, 0), 0, 0));

        JScrollPane jScrollPane = new JScrollPane(whiteBoardsPanel);
        frame.getContentPane().add(jScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.add(refreshButton, new GridBagConstraints(0,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 10), 0, 0));

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
//        frame.pack();
    }

    public ThreeViews() {
        instance = this;

        this.whiteBoardXY = new WhiteBoardPanel(); // from above
        this.whiteBoardXZ = new WhiteBoardPanel(); // side
        this.whiteBoardYZ = new WhiteBoardPanel(); // facing

        /*BoatBox3D*/ this.box3D = new BoatBox3D();
        threeDPanel = new ThreeDPanelWithWidgets(box3D);

        this.initComponents();
    }

    enum Orientation {
        XY, XZ, YZ
    }

    private Bezier.Point3D lookForClosePoint(MouseEvent me,
                                             List<Bezier.Point3D> ctrlPoints,
                                             Orientation orientation,
                                             Function<Double, Integer> xTransformer,
                                             Function<Double, Integer> yTransformer,
                                             int canvasHeight) {
        Bezier.Point3D closePoint = null;
        for (Bezier.Point3D ctrlPt : ctrlPoints) {
            double ptX = ctrlPt.getX();
            double ptY = ctrlPt.getY();
            if (orientation == Orientation.XZ) {
                ptX = ctrlPt.getX();
                ptY = ctrlPt.getZ();
            } else if (orientation == Orientation.YZ) {
                ptX = ctrlPt.getY();
                ptY = ctrlPt.getZ();
            }
            Integer canvasX = xTransformer.apply(ptX);
            Integer canvasY = yTransformer.apply(ptY);
            if (Math.abs(me.getX() - canvasX) < 5 && Math.abs(me.getY() - (canvasHeight - canvasY)) < 5) { // 5: proximity tolerance
//                    System.out.printf("DeltaX: %d, DeltaY: %d\n", Math.abs(e.getX() - canvasX), Math.abs(e.getY() - (height - canvasY)));
//                    System.out.printf("Close to %s\n", ctrlPt);
                closePoint = ctrlPt;
                break;
            }
        }
        return closePoint;
    }

    // Find the control point close to the mouse pointer.
    private Bezier.Point3D getClosePoint(MouseEvent me, WhiteBoardPanel wbp, Orientation orientation) {
        Bezier.Point3D closePoint = null;
        Function<Double, Integer> spaceToCanvasXTransformer = wbp.getSpaceToCanvasXTransformer();
        Function<Double, Integer> spaceToCanvasYTransformer = wbp.getSpaceToCanvasYTransformer();
        int height = wbp.getHeight();
        if (spaceToCanvasXTransformer != null && spaceToCanvasYTransformer != null) {
            // Look on the rail
            closePoint = lookForClosePoint(me, railCtrlPoints, orientation, spaceToCanvasXTransformer, spaceToCanvasYTransformer, height);
            if (closePoint == null) { // Then look on the keel
                closePoint = lookForClosePoint(me, keelCtrlPoints, orientation, spaceToCanvasXTransformer, spaceToCanvasYTransformer, height);
            }
        }
        return closePoint;
    }

    private int closestPointIndex = -1;

    static class BezierPopup extends JPopupMenu
            implements ActionListener,
            PopupMenuListener {
        private JMenuItem deleteMenuItem;
        private JMenuItem editMenuItem;

        private ThreeViews parent;
        private Bezier.Point3D closePoint;

        private final static String DELETE_CTRL_POINT = "Delete Ctrl Point";
        private final static String EDIT_CTRL_POINT = "Edit Ctrl Point";

        public BezierPopup(ThreeViews parent, Bezier.Point3D closePoint) {
            super();
            this.parent = parent;
            this.closePoint = closePoint;

            deleteMenuItem = new JMenuItem(DELETE_CTRL_POINT);
            this.add(deleteMenuItem);
            deleteMenuItem.addActionListener(this);
            editMenuItem = new JMenuItem(EDIT_CTRL_POINT);
            this.add(editMenuItem);
            editMenuItem.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand().equals(DELETE_CTRL_POINT)) {
                if (this.closePoint != null) {
                    if (this.parent.railCtrlPoints.contains(this.closePoint)) {
                        this.parent.railCtrlPoints.remove(this.closePoint);
                    } else if (this.parent.keelCtrlPoints.contains(this.closePoint)) {
                        this.parent.keelCtrlPoints.remove(this.closePoint);
                    }
                    this.parent.refreshData();
                }
            } else if (event.getActionCommand().equals(EDIT_CTRL_POINT)) {
                if (this.closePoint != null) {
                    CtrlPointEditor cpEditor = new CtrlPointEditor(this.closePoint.getX(), this.closePoint.getY(), this.closePoint.getZ());
                    int response = JOptionPane.showConfirmDialog(frame,
                            cpEditor,
                            "Edit Control Point",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (response == JOptionPane.OK_OPTION) {
                        // Update Control Point in the list
                        double x = cpEditor.getXValue();
                        double y = cpEditor.getYValue();
                        double z = cpEditor.getZValue();
                        if (this.parent.railCtrlPoints.contains(this.closePoint)) {
                            int idx = this.parent.railCtrlPoints.indexOf(this.closePoint);
                            this.parent.railCtrlPoints.get(idx).x(x).y(y).z(z);
                        } else if (this.parent.keelCtrlPoints.contains(this.closePoint)) {
                            int idx = this.parent.keelCtrlPoints.indexOf(this.closePoint);
                            this.parent.keelCtrlPoints.get(idx).x(x).y(y).z(z);
                        }
                    }
                    this.parent.refreshData();
                }
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    static class CtrlPointEditor extends JPanel {
        private transient Border border = BorderFactory.createEtchedBorder();
        private GridBagLayout layoutMain = new GridBagLayout();
        private JLabel xLabel = new JLabel("X");
        private final JFormattedTextField xValue = new JFormattedTextField(new DecimalFormat("#0.0000"));
        private JLabel yLabel = new JLabel("Y");
        private final JFormattedTextField yValue = new JFormattedTextField(new DecimalFormat("#0.0000"));
        private JLabel zLabel = new JLabel("Z");
        private final JFormattedTextField zValue = new JFormattedTextField(new DecimalFormat("#0.0000"));

        private double x, y, z;

        public CtrlPointEditor(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            try {
                this.jbInit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public double getXValue() {
            return Double.parseDouble(this.xValue.getText());
        }

        public double getYValue() {
            return Double.parseDouble(this.yValue.getText());
        }

        public double getZValue() {
            return Double.parseDouble(this.zValue.getText());
        }

        private void jbInit() {
            this.setLayout(layoutMain);
            this.setBorder(border);

            xValue.setHorizontalAlignment(SwingConstants.RIGHT);
            xValue.setPreferredSize(new Dimension(80, 20));
            this.add(xLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            this.add(xValue, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            yValue.setHorizontalAlignment(SwingConstants.RIGHT);
            yValue.setPreferredSize(new Dimension(80, 20));
            this.add(yLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            this.add(yValue, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            zValue.setHorizontalAlignment(SwingConstants.RIGHT);
            zValue.setPreferredSize(new Dimension(80, 20));
            this.add(zLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            this.add(zValue, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));

            this.xValue.setValue(this.x);
            this.yValue.setValue(this.y);
            this.zValue.setValue(this.z);
        }
    }

    static class AddCtrlPointPanel extends JPanel {
        private transient Border border = BorderFactory.createEtchedBorder();
        private GridBagLayout layoutMain = new GridBagLayout();
        private JRadioButton railButton = new JRadioButton("Rail");
        private JRadioButton keelButton = new JRadioButton("Keel");
        private ButtonGroup group = new ButtonGroup();

        private JLabel posLabel = new JLabel("Index in Rail...");
        private final JFormattedTextField posValue = new JFormattedTextField(new DecimalFormat("#0"));

        enum CurveName {
            RAIL, KEEL
        }

        private int railCard, keelCard;

        public AddCtrlPointPanel(int railCardinality, int keelCardinality) {
            this.railCard = railCardinality;
            this.keelCard = keelCardinality;

            this.railButton.addChangeListener(evt -> {
                String label = ((JRadioButton)evt.getSource()).isSelected() ? "rail" : "keel";
                int card = ((JRadioButton)evt.getSource()).isSelected() ? this.railCard : this.keelCard;
                posLabel.setText(String.format("Index in %s [0..%d]", label, card));
            });
            this.keelButton.addChangeListener(evt -> {
                String label = ((JRadioButton)evt.getSource()).isSelected() ? "keel" : "rail";
                int card = ((JRadioButton)evt.getSource()).isSelected() ? this.keelCard : this.railCard;
                posLabel.setText(String.format("Index in %s [0..%d]", label, card));
            });

            try {
                this.jbInit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public int getPos() {
            return Integer.parseInt(this.posValue.getText());
        }

        public CurveName getCurve() {
            return railButton.isSelected() ? CurveName.RAIL : CurveName.KEEL;
        }

        private void jbInit() {
            group.add(railButton);
            group.add(keelButton);
            railButton.setSelected(true);
            keelButton.setSelected(false);
            posLabel.setText(String.format("Index in %s [0..%d]", "rail", this.railCard));

            this.setLayout(layoutMain);
            this.setBorder(border);

            posValue.setHorizontalAlignment(SwingConstants.RIGHT);
            posValue.setPreferredSize(new Dimension(80, 20));
            posValue.setSize(new Dimension(80, 20));

            this.add(railButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            this.add(keelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));

            this.add(posLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
            this.add(posValue, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(5, 15, 0, 15), 0, 0));
        }
    }

    public static void main(String... args) {

        try {
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("----------------------------------------------");
        System.out.printf("Running from folder %s\n", System.getProperty("user.dir"));
        System.out.printf("Java Version %s\n", System.getProperty("java.version"));
        System.out.println("----------------------------------------------");

        File config = new File("init.json");
        if (config.exists()) {
            try {
                URL configResource = config.toURI().toURL();
                initConfig = mapper.readValue(configResource.openStream(), Map.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Warning: no init.json was found.");
        }

        ThreeViews thisThing = new ThreeViews();// This one has instantiated the white boards

        thisThing.refreshData(); // Display data the first time.
        thisThing.show();
    }
}
