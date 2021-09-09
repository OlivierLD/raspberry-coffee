package gsg.examples.wb.bezier;

import bezier.Bezier;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;
import lowpass.Filter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * More Abstraction, using default WhiteBoard Writer
 * You can focus only on the data, not on the display. See the main method.
 * 2D Bezier example.
 * With draggable control points (hence the MouseListener, MouseMotionListener).
 */
public class SwingSample11 implements MouseListener, MouseMotionListener {

    private final static String TITLE = "Simple 2D Bezier sample (draggable control points)";

    private JFrame frame;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Refresh Data"); // Not really useful here.

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;

    // All z = 0, 2D bezier.
    // 5 control points
    private List<Bezier.Point3D> ctrlPoints = List.of(  // Warning: List.of generates an immutable list.
            new Bezier.Point3D(-60, -80, 0),
            new Bezier.Point3D(60, -40, 0),
            new Bezier.Point3D(45, 30, 0),
            new Bezier.Point3D(-30, 20, 0),
            new Bezier.Point3D(-30, 90, 0));

    // A frame for a small boat?
//    private List<Bezier.Point3D> ctrlPoints = List.of(  // Warning: List.of generates an immutable list.
//            new Bezier.Point3D(0, -25, 0),
//            new Bezier.Point3D(100, 0, 0),
//            new Bezier.Point3D(105, 56, 0));

    // The WhiteBoard instantiation
    private final static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.printf("Exit requested %s\n", ae);
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.printf("Help requested %s\n", ae);
        JOptionPane.showMessageDialog(whiteBoard, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Get the t value for a given X on the curve.
     *
     * @param bezier
     * @param startAt 0 to begin with
     * @param inc t increment for first iteration
     * @param x the X to find
     * @param precision acceptable difference
     * @return
     */
    private static double getTForGivenX(Bezier bezier, double startAt, double inc, double x, double precision) {
        double tForX = 0;
        for (double t=startAt; t<=1; t+=inc) {
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            if (tick.getX() > x) { // Assume that X is always growing.
                if (Math.abs(tick.getX() - x) < precision) {
                    return t;
                } else {
                    return getTForGivenX(bezier, startAt - inc, inc / 10.0, x, precision);
                }
            }
        }
        return tForX;
    }

    private void refreshData() {

        // Generate the data, the Bézier curve.
        Bezier bezier = new Bezier(ctrlPoints);
        List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>(); // The points to display.
        for (double t=0; t<=1.0; t+=1E-3) {
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            // System.out.println(String.format("%.03f: %s", t, tick.toString()));
            bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
        }
        // For test: Find t for a given X
        if (false) {
            double x = 60; // the one to find
            double t = getTForGivenX(bezier, 0.0, 1E-1, x, 1E-4);
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
        List<VectorUtils.Vector2D> ctrlPtsVectors = new ArrayList<>();
        for (int i=0; i<xCtrlPoints.length; i++) {
            ctrlPtsVectors.add(new VectorUtils.Vector2D(xCtrlPoints[i], yCtrlPoints[i]));
//            System.out.printf("Adding X:%f, Y:%f\n", xCtrlPoints[i], yCtrlPoints[i]);
        }

        // Curve points
        double[] xData = bezierPoints.stream()
                .mapToDouble(bp -> bp.getX())
                .toArray();
        double[] yData = bezierPoints.stream()
                .mapToDouble(bp -> bp.getY())
                .toArray();
        List<VectorUtils.Vector2D> dataVectors = new ArrayList<>();
        for (int i=0; i<xData.length; i++) {
            dataVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
        }

        whiteBoard.setAxisColor(Color.BLACK);
        whiteBoard.setGridColor(Color.GRAY);
        whiteBoard.setWithGrid(true);
        whiteBoard.setXEqualsY(true);
        whiteBoard.setForceTickIncrement(20);
        whiteBoard.setEnforceXAxisAt(0d);
        whiteBoard.setEnforceYAxisAt(0d);

        whiteBoard.resetAllData();

        // Bezier ctrl points serie
        WhiteBoardPanel.DataSerie ctrlSerie = new WhiteBoardPanel.DataSerie()
                .data(ctrlPtsVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                .lineThickness(1)
                .color(Color.ORANGE);
        whiteBoard.addSerie(ctrlSerie);

        // Bezier points serie
        WhiteBoardPanel.DataSerie dataSerie = new WhiteBoardPanel.DataSerie()
                .data(dataVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                .lineThickness(3)
                .color(Color.BLUE);
        whiteBoard.addSerie(dataSerie);

        // Finally, display it.
        whiteBoard.repaint();  // This is for a pure Swing context
    }

    private void show() {
        this.frame.setVisible(true);
    }

    private void initComponents() {
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
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(ae -> fileExit_ActionPerformed(ae));
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(ae -> helpAbout_ActionPerformed(ae));
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
        menuHelp.add(menuHelpAbout);
        menuBar.add(menuHelp);

        topLabel = new JLabel(" " + TITLE); // Ugly!
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);

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

    public SwingSample11() {
        whiteBoard.addMouseListener(this);
        whiteBoard.addMouseMotionListener(this);
    }

    // Find the control point close to the mouse pointer.
    private Bezier.Point3D getClosePoint(MouseEvent me) {
        Bezier.Point3D closePoint = null;
        Function<Double, Integer> spaceToCanvasXTransformer = whiteBoard.getSpaceToCanvasXTransformer();
        Function<Double, Integer> spaceToCanvasYTransformer = whiteBoard.getSpaceToCanvasYTransformer();
        int height = whiteBoard.getHeight();
        if (spaceToCanvasXTransformer != null && spaceToCanvasYTransformer != null) {
            for (Bezier.Point3D ctrlPt : ctrlPoints) {
                Integer canvasX = spaceToCanvasXTransformer.apply(ctrlPt.getX());
                Integer canvasY = spaceToCanvasYTransformer.apply(ctrlPt.getY());
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

    @Override
    public void mouseMoved(MouseEvent e) {
//        System.out.println("Mouse moved");
        Bezier.Point3D closePoint = getClosePoint(e);
        if (closePoint != null) {
            whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closestPointIndex = ctrlPoints.indexOf(closePoint);
        } else {
            whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            closestPointIndex = -1;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
    @Override
    public void mousePressed(MouseEvent e) {
//        System.out.printf("Mouse clicked x: %d y: %d\n", e.getX(), e.getY());
        Bezier.Point3D closePoint = getClosePoint(e);
        if (closePoint != null) {
//            System.out.println("Found it!");
            whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        if (closestPointIndex > -1) {
            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoard.getCanvasToSpaceXTransformer();
            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoard.getCanvasToSpaceYTransformer();
            int height = whiteBoard.getHeight();
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

        SwingSample11 thisThing = new SwingSample11();// This one has instantiated the white board
        thisThing.initComponents();

        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(125, 0, 255, 255));
        whiteBoard.setWithGrid(false);
        whiteBoard.setBgColor(new Color(250, 250, 250, 255));
        whiteBoard.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoard.setSize(new Dimension(800, 600));
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoard.setGraphicMargins(30);
        whiteBoard.setXEqualsY(true); // false);
        // Enforce Y amplitude
        // whiteBoard.setForcedMinY(0d);
        // whiteBoard.setForcedMaxY(100d);

        thisThing.refreshData(); // Display data the first time.
        thisThing.show();

    }
}
