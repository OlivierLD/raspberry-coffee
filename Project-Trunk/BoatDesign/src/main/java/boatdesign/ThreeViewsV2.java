package boatdesign;

import bezier.Bezier;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.SwingUtils.fullui.ThreeDFrameWithWidgets;
import gsg.SwingUtils.fullui.ThreeDPanelWithWidgets;
import gsg.VectorUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Using default WhiteBoard Writer
 *
 * 2D Bezier example.
 * With draggable control points (hence the MouseListener, MouseMotionListener).
 */
public class ThreeViewsV2 {

    private final static String TITLE = "One 3D Bezier Drawing Board";

    private JFrame frame;
    private ThreeDPanelWithWidgets threeDPanel;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileSpit = new JMenuItem();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Refresh Data"); // Not really useful here.

    private final static int WIDTH = 1024;
    private final static int HEIGHT = 800;

    private static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Object> initConfig = null;

    // All z = 0, 2D bezier.
    private List<Bezier.Point3D> ctrlPoints = new ArrayList<>();

    // The WhiteBoard instantiations
    private WhiteBoardPanel whiteBoardXY = null; // from above
    private WhiteBoardPanel whiteBoardXZ = null; // side
    private WhiteBoardPanel whiteBoardYZ = null; // facing

    private Box3D box3D = null;

    private JTextPane dataTextArea = null;

    private void fileSpit_ActionPerformed(ActionEvent ae) {
        System.out.println("Ctrl Points:");
        this.ctrlPoints.forEach(pt -> {
            System.out.println(String.format("%s", pt));
        });
    }
    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.printf("Exit requested %s\n", ae);
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.printf("Help requested %s\n", ae);
        JOptionPane.showMessageDialog(frame, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    private void refreshData() {

        if (ctrlPoints.size() > 0) {
            // Display in textArea
            String content = ctrlPoints.stream()
                    .map(pt -> String.format("%d: %s", ctrlPoints.indexOf(pt), pt.toString()))
                    .collect(Collectors.joining("\n"));
            dataTextArea.setText("Control Points:\n" + content);

                    // Generate the data, the Bézier curve.
            Bezier bezier = new Bezier(ctrlPoints);
            List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>(); // The points to display.
            if (ctrlPoints.size() > 2) { // 3 points minimum.
                for (double t = 0; t <= 1.0; t += 1E-3) {
                    Bezier.Point3D tick = bezier.getBezierPoint(t);
                    // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                    bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }
            // For test: Find t for a given X
            if (false) {
                double x = 60; // the one to find
//                double t = getTForGivenX(bezier, 0.0, 1E-1, x, 1E-4);
                double t = bezier.getTForGivenX(0.0, 1E-1, x, 1E-4);
                Bezier.Point3D tick = bezier.getBezierPoint(t);
                System.out.printf("For x=%f, t=%f - X:%f, Y:%f\n", x, t, tick.getX(), tick.getY());
            }

            // Prepare data for display
            // Ctrl Points
            double[] xCtrlPoints = ctrlPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            double[] yCtrlPoints = ctrlPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            double[] zCtrlPoints = ctrlPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> ctrlPtsXYVectors = new ArrayList<>();
            for (int i = 0; i < xCtrlPoints.length; i++) {
                ctrlPtsXYVectors.add(new VectorUtils.Vector2D(xCtrlPoints[i], yCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> ctrlPtsXZVectors = new ArrayList<>();
            for (int i = 0; i < xCtrlPoints.length; i++) {
                ctrlPtsXZVectors.add(new VectorUtils.Vector2D(xCtrlPoints[i], zCtrlPoints[i]));
            }
            List<VectorUtils.Vector2D> ctrlPtsYZVectors = new ArrayList<>();
            for (int i = 0; i < yCtrlPoints.length; i++) {
                ctrlPtsYZVectors.add(new VectorUtils.Vector2D(yCtrlPoints[i], zCtrlPoints[i]));
            }

            // Curve points
            double[] xData = bezierPoints.stream()
                    .mapToDouble(bp -> bp.getX())
                    .toArray();
            double[] yData = bezierPoints.stream()
                    .mapToDouble(bp -> bp.getY())
                    .toArray();
            double[] zData = bezierPoints.stream()
                    .mapToDouble(bp -> bp.getZ())
                    .toArray();
            List<VectorUtils.Vector2D> dataXYVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataXYVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
            }
            List<VectorUtils.Vector2D> dataXZVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataXZVectors.add(new VectorUtils.Vector2D(xData[i], zData[i]));
            }
            List<VectorUtils.Vector2D> dataYZVectors = new ArrayList<>();
            for (int i = 0; i < yData.length; i++) {
                dataYZVectors.add(new VectorUtils.Vector2D(yData[i], zData[i]));
            }

            whiteBoardXY.resetAllData();
            whiteBoardXZ.resetAllData();
            whiteBoardYZ.resetAllData();

            // Bezier ctrl points series
            // XY
            WhiteBoardPanel.DataSerie ctrlXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(ctrlPtsXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXY.addSerie(ctrlXYSerie);
            // XZ
            WhiteBoardPanel.DataSerie ctrlXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(ctrlPtsXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardXZ.addSerie(ctrlXZSerie);
            // YZ
            WhiteBoardPanel.DataSerie ctrlYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(ctrlPtsYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            whiteBoardYZ.addSerie(ctrlYZSerie);

            // Bezier points series
            // XY
            WhiteBoardPanel.DataSerie dataXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXY.addSerie(dataXYSerie);
            // XZ
            WhiteBoardPanel.DataSerie dataXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXZ.addSerie(dataXZSerie);
            // YZ
            WhiteBoardPanel.DataSerie dataYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(dataYZSerie);

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
            List<List<Double>> defaultPoints = (List)initConfig.get("default-points");
            defaultPoints.forEach(pt -> {
                ctrlPoints.add(new Bezier.Point3D(pt.get(0), pt.get(1), pt.get(2)));
            });
        } else {
            // TODO There is a problem when ctrlPoints is empty... Fix it.
            // Initialize [0, 10, 0], [550, 105, 0]
            ctrlPoints.add(new Bezier.Point3D(0, 10, 0));
            ctrlPoints.add(new Bezier.Point3D(550, 105, 0));
        }

        // Override defaults (not mandatory)
        whiteBoardXY.setAxisColor(Color.GRAY);
        whiteBoardXY.setWithGrid(true);
        whiteBoardXY.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXY.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXY.setSize(new Dimension(800, 200));
        whiteBoardXY.setPreferredSize(new Dimension(800, 200));
        whiteBoardXY.setTextColor(Color.RED);
        whiteBoardXY.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoardXY.setGraphicMargins(30);
        whiteBoardXY.setXEqualsY(true); // false);
        // Enforce Y amplitude
        whiteBoardXY.setForcedMinY(0d);
        whiteBoardXY.setForcedMaxY(150d);

        whiteBoardXY.setEnforceXAxisAt(0d);
        whiteBoardXY.setEnforceYAxisAt(0d);

        whiteBoardXZ.setAxisColor(Color.GRAY);
        whiteBoardXZ.setWithGrid(true);
        whiteBoardXZ.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXZ.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXZ.setSize(new Dimension(800, 200));
        whiteBoardXZ.setPreferredSize(new Dimension(800, 200));
        whiteBoardXZ.setTextColor(Color.RED);
        whiteBoardXZ.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoardXZ.setGraphicMargins(30);
        whiteBoardXZ.setXEqualsY(true); // false);
        // Enforce Y amplitude
        whiteBoardXZ.setForcedMinY(-50d);
        whiteBoardXZ.setForcedMaxY(100d);

        whiteBoardXZ.setEnforceXAxisAt(0d);
        whiteBoardXZ.setEnforceYAxisAt(0d);

        whiteBoardYZ.setAxisColor(Color.GRAY);
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

        whiteBoardYZ.setEnforceXAxisAt(0d);
        whiteBoardYZ.setEnforceYAxisAt(0d);

        ThreeViewsV2 instance = this;

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
                    String response = JOptionPane.showInputDialog(
                            frame,
                            String.format("Where to insert new point (index in the list [0..%d]) ?", ctrlPoints.size()),
                            "Add Control Point",
                            JOptionPane.QUESTION_MESSAGE);
//                  System.out.println("Response:" + response);
                    if (response != null && !response.isEmpty()) {
                        try {
                            int newIndex = Integer.parseInt(response);
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                            int height = whiteBoardXY.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(height - e.getY());
//                      System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).y(newY);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                for (int i=0; i<newIndex; i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                newList.add(point3D);
                                for (int i=newIndex; i<ctrlPoints.size(); i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                ctrlPoints = newList;
                                System.out.printf("List now has %d elements.\n", ctrlPoints.size());
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
                        Bezier.Point3D point3D = ctrlPoints.get(closestPointIndex);
                        point3D.x(newX).y(newY);
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
                    closestPointIndex = ctrlPoints.indexOf(closePoint);
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
                    String response = JOptionPane.showInputDialog(
                            frame,
                            String.format("Where to insert new point (index in the list [0..%d]) ?", ctrlPoints.size()),
                            "Add Control Point",
                            JOptionPane.QUESTION_MESSAGE);
    //              System.out.println("Response:" + response);
                    if (response != null && !response.isEmpty()) {
                        try {
                            int newIndex = Integer.parseInt(response);
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXZ.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXZ.getCanvasToSpaceYTransformer();
                            int height = whiteBoardXZ.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newZ = canvasToSpaceYTransformer.apply(height - e.getY());
//                      System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).z(newZ);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                for (int i=0; i<newIndex; i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                newList.add(point3D);
                                for (int i=newIndex; i<ctrlPoints.size(); i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                ctrlPoints = newList;
                                System.out.printf("List now has %d elements.\n", ctrlPoints.size());
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
                        Bezier.Point3D point3D = ctrlPoints.get(closestPointIndex);
                        point3D.x(newX).z(newZ);
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
                    closestPointIndex = ctrlPoints.indexOf(closePoint);
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
                        popup.show(whiteBoardXZ, e.getX(), e.getY());
                    }
                } else {
                    // Regular click.
                    // Drop point here. Where is the list
                    String response = JOptionPane.showInputDialog(
                            frame,
                            String.format("Where to insert new point (index in the list [0..%d]) ?", ctrlPoints.size()),
                            "Add Control Point",
                            JOptionPane.QUESTION_MESSAGE);
//                  System.out.println("Response:" + response);
                    if (response != null && !response.isEmpty()) {
                        try {
                            int newIndex = Integer.parseInt(response);
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardYZ.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardYZ.getCanvasToSpaceYTransformer();
                            int height = whiteBoardYZ.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newY = canvasToSpaceXTransformer.apply(e.getX());
                                double newZ = canvasToSpaceYTransformer.apply(height - e.getY());
//                      System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().y(newY).z(newZ);
                                List<Bezier.Point3D> newList = new ArrayList<>();
                                for (int i=0; i<newIndex; i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                newList.add(point3D);
                                for (int i=newIndex; i<ctrlPoints.size(); i++) {
                                    newList.add(ctrlPoints.get(i));
                                }
                                ctrlPoints = newList;
                                System.out.printf("List now has %d elements.\n", ctrlPoints.size());
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
                        Bezier.Point3D point3D = ctrlPoints.get(closestPointIndex);
                        point3D.y(newY).z(newZ);
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
                    closestPointIndex = ctrlPoints.indexOf(closePoint);
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

        refreshButton.addActionListener(e -> refreshData());

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

        JLabel label1 = new JLabel("Label 1");
        JLabel label2 = new JLabel("Label 2");
        JLabel label3 = new JLabel("Label 3");

        JPanel ctrlPointsPanel = new JPanel(new BorderLayout());
        ctrlPointsPanel.setBorder(BorderFactory.createTitledBorder("Data Placeholder"));
        dataTextArea = new JTextPane();
        dataTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        dataTextArea.setPreferredSize(new Dimension(300, 600));
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);

        ctrlPointsPanel.add(dataScrollPane, BorderLayout.NORTH);

        whiteBoardsPanel.add(whiteBoardXZ,
                new GridBagConstraints(0,
                        0,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        whiteBoardsPanel.add(whiteBoardYZ,
                new GridBagConstraints(0,
                        1,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        whiteBoardsPanel.add(whiteBoardXY,
                new GridBagConstraints(0,
                        2,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

//        whiteBoardsPanel.add(topRight, // box3DScrollPane, // this.box3D, // topRight,
//                new GridBagConstraints(1,
//                0,
//                1,
//                2,
//                1.0,
//                0.0,
//                GridBagConstraints.WEST,
//                GridBagConstraints.NONE,
//                new Insets(0, 0, 0, 0), 0, 0));
        whiteBoardsPanel.add(threeDPanel, // ctrlPointsPanel,
                new GridBagConstraints(1,
                        0,
                        1,
                        3,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

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

    public ThreeViewsV2() {
        this.whiteBoardXY = new WhiteBoardPanel(); // from above
        this.whiteBoardXZ = new WhiteBoardPanel(); // side
        this.whiteBoardYZ = new WhiteBoardPanel(); // facing

        this.box3D = new Box3D(1200, 600);
        box3D.setxMin(-10);
        box3D.setxMax(600);
        box3D.setyMin(-50);
        box3D.setyMax(100);
        box3D.setzMin(-50);
        box3D.setzMax(150);

        Consumer<Graphics2D> afterDrawer = g2d -> {

            // Link the control points
            g2d.setColor(Color.ORANGE);
            g2d.setStroke(new BasicStroke(2));
            VectorUtils.Vector3D from = null;
            for (Bezier.Point3D ctrlPoint : ctrlPoints) {
                if (from != null) {
                    VectorUtils.Vector3D to = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
                    box3D.drawSegment(g2d, from, to);
                }
                from = new VectorUtils.Vector3D(ctrlPoint.getX(), ctrlPoint.getY(), ctrlPoint.getZ());
            }

            // Plot the control points
            g2d.setColor(Color.BLUE);
            ctrlPoints.forEach(pt -> {
                VectorUtils.Vector3D at = new VectorUtils.Vector3D(pt.getX(), pt.getY(), pt.getZ());
                box3D.drawCircle(g2d, at, 6);
            });
            // TODO Redundant, fix that
            Bezier bezier = new Bezier(ctrlPoints);
            List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>(); // The points to display.
            if (ctrlPoints.size() > 2) { // 3 points minimum.
                for (double t = 0; t <= 1.0; t += 1E-3) {
                    Bezier.Point3D tick = bezier.getBezierPoint(t);
                    // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                    bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }

            // The actual bezier
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(5));
            from = null;
            for (int i=0; i<bezierPoints.size(); i++) {
                VectorUtils.Vector3D to = bezierPoints.get(i);
                if (from != null) {
                    box3D.drawSegment(g2d, from, to);
                }
                from = to;
            }
        };

        // Invoke the above
//        box3D.setDrawer(afterDrawer);
        box3D.setAfterDrawer(afterDrawer);

//        ThreeDFrameWithWidgets threeDFrame = new ThreeDFrameWithWidgets(box3D, "Same Bézier, in 3D - Draggable");
//        threeDFrame.setVisible(true);

        threeDPanel = new ThreeDPanelWithWidgets(box3D);
    }

    enum Orientation {
        XY, XZ, YZ
    }

    // Find the control point close to the mouse pointer.
    private Bezier.Point3D getClosePoint(MouseEvent me, WhiteBoardPanel wbp, Orientation orientation) {
        Bezier.Point3D closePoint = null;
        Function<Double, Integer> spaceToCanvasXTransformer = wbp.getSpaceToCanvasXTransformer();
        Function<Double, Integer> spaceToCanvasYTransformer = wbp.getSpaceToCanvasYTransformer();
        int height = wbp.getHeight();
        if (spaceToCanvasXTransformer != null && spaceToCanvasYTransformer != null) {
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
                Integer canvasX = spaceToCanvasXTransformer.apply(ptX);
                Integer canvasY = spaceToCanvasYTransformer.apply(ptY);
                if (Math.abs(me.getX() - canvasX) < 5 && Math.abs(me.getY() - (height - canvasY)) < 5) {
//                    System.out.printf("DeltaX: %d, DeltaY: %d\n", Math.abs(e.getX() - canvasX), Math.abs(e.getY() - (height - canvasY)));
//                    System.out.printf("Close to %s\n", ctrlPt);
                    closePoint = ctrlPt;
                    break;
                }
            }
        }
        return closePoint;
    }

    private int closestPointIndex = -1;

    static class BezierPopup extends JPopupMenu
            implements ActionListener,
            PopupMenuListener {
        private JMenuItem deleteMenuItem;

        private ThreeViewsV2 parent;
        private Bezier.Point3D closePoint;

        private final static String DELETE_CTRL_POINT = "Delete Ctrl Point";

        public BezierPopup(ThreeViewsV2 parent, Bezier.Point3D closePoint) {
            super();
            this.parent = parent;
            this.closePoint = closePoint;
            deleteMenuItem = new JMenuItem(DELETE_CTRL_POINT);
            this.add(deleteMenuItem);
            deleteMenuItem.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (event.getActionCommand().equals(DELETE_CTRL_POINT)) {
                if (this.closePoint != null) {
                    this.parent.ctrlPoints.remove(this.closePoint);
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

        ThreeViewsV2 thisThing = new ThreeViewsV2();// This one has instantiated the white board

        thisThing.initComponents();

        thisThing.refreshData(); // Display data the first time.
        thisThing.show();
    }
}
