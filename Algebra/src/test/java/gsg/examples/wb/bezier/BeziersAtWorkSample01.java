package gsg.examples.wb.bezier;

import bezier.Bezier;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * More Abstraction, using default WhiteBoard Writer
 *
 * You can focus only on the data, not on the display. See the main method.
 * 2D Bezier example.
 *
 * Shows how the Bezier is elaborated, graphically. 3 Ctrl points.
 */
public class BeziersAtWorkSample01 {

    private final static String TITLE = "Simple 2D Bezier sample. 3 Ctrl Points.";
    // All z = 0, 2D bezier.
    // 3 control points
    private List<Bezier.Point3D> ctrlPoints = List.of(
            new Bezier.Point3D(-60, -40, 0),
            new Bezier.Point3D(0, 40, 0),
            new Bezier.Point3D(40, -20, 0));

    private JFrame frame;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Reset Data");
    private final JButton animateButton = new JButton("Animate Bézier");

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;

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

    private void refreshData() {

        // Generate the data, the Bézier curve.
        Bezier bezier = new Bezier(ctrlPoints);
        List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>(); // The points to display.
        for (double t=0; t<=1.0; t+=1e-3) {
            Bezier.Point3D tick = bezier.getBezierPoint(t);
            // System.out.println(String.format("%.03f: %s", t, tick.toString()));
            bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
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
        animateButton.addActionListener(e -> {
            if (animator == null || !animator.isAlive()) {
                System.out.println("Starting new Animator");
                animateSuspended = false;
                animator = new Thread(() -> animate(), "Animator");
                animator.start();
            } else {
                System.out.println("Animator already running");
                if (animateSuspended) { // Resuming
                    if (animator.isAlive()) {
                        System.out.println("... Resuming");
//                        synchronized (animator) { // That one does not work as expected...
//                            animator.notify();
//                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                        System.out.println("    Notification sent.");
                    }
                    animateSuspended = false;
                } else {
                    System.out.println("... Suspending");
                    animateSuspended = true;
                }
            }
        });
        animateButton.setToolTipText("Start / Stop");

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

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        bottomPanel.add(animateButton);

        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
//        frame.pack();
    }

    public BeziersAtWorkSample01() {
    }
    private boolean animateSuspended = false;
    private Object lock = new Object(); // wait/notify on the animator thread did not work for me... :(
    private Thread animator = null;

    /**
     * Call in the thread animating the Bezier on the WhiteBoard.
     */
    public void animate() {
        whiteBoard.resetAllData();

        Bezier bezier = new Bezier(ctrlPoints);

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

        // Bezier ctrl points serie
        WhiteBoardPanel.DataSerie ctrlSerie = new WhiteBoardPanel.DataSerie()
                .data(ctrlPtsVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                .lineThickness(1)
                .color(Color.RED);
        whiteBoard.addSerie(ctrlSerie);

        whiteBoard.repaint();

        // Now, animate.
        final double tIncrement = 1e-3; // 0.005;
        for (double t=0.0; t<=1 + tIncrement; t += tIncrement) {

            try {
                final double _t = t;
                AtomicReference<WhiteBoardPanel.DataSerie> progressSerieReference = new AtomicReference<>(null);
                SwingUtilities.invokeAndWait(() -> {

                    List<VectorUtils.Vector3D> bezierPoints = new ArrayList<>(); // The points to display.
                    for (double bezierT=0; bezierT<=_t; bezierT += tIncrement) {
                        Bezier.Point3D tick = bezier.getBezierPoint(bezierT);
                        // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                        bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                    }
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
                    // Bezier points serie
                    WhiteBoardPanel.DataSerie dataSerie = new WhiteBoardPanel.DataSerie()
                            .data(dataVectors)
                            .graphicType(WhiteBoardPanel.GraphicType.LINE)
                            .lineThickness(3)
                            .color(Color.BLUE);

                    whiteBoard.resetAllData();
                    whiteBoard.resetAllText();

                    WhiteBoardPanel.TextSerie currentPosition = new WhiteBoardPanel.TextSerie(new VectorUtils.Vector2D(-56d, 37d), String.format("t = %.03f", _t));
                    whiteBoard.addTextSerie(currentPosition);

                    // Main Ctrl points
                    whiteBoard.addSerie(ctrlSerie);
                    // Bezier
                    whiteBoard.addSerie(dataSerie);

                    // Progress
                    // For 3 ctrl points, two-segments, hard-coded
                    Bezier.Point3D point3D_01 = Bezier.withProgressT(ctrlPoints.get(0), ctrlPoints.get(1), _t);
                    Bezier.Point3D point3D_02 = Bezier.withProgressT(ctrlPoints.get(1), ctrlPoints.get(2), _t);

                    List<VectorUtils.Vector2D> progressPtsVectors = new ArrayList<>();
                    progressPtsVectors.add(new VectorUtils.Vector2D(point3D_01.getX(), point3D_01.getY()));
                    progressPtsVectors.add(new VectorUtils.Vector2D(point3D_02.getX(), point3D_02.getY()));
                    // Bezier progress points serie

                    WhiteBoardPanel.DataSerie progressSerie = new WhiteBoardPanel.DataSerie()
                            .data(progressPtsVectors)
                            .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                            .lineThickness(1)
                            .color(Color.RED);

                    if (progressSerieReference.get() != null) {
                        whiteBoard.removeSerie(progressSerieReference.get());
                    }
                    whiteBoard.addSerie(progressSerie);
                    progressSerieReference.set(progressSerie);

                    // Last Bezier point
                    whiteBoard.addSerie(new WhiteBoardPanel.DataSerie()
                            .data(List.of(new VectorUtils.Vector2D(dataVectors.get(dataVectors.size() - 1).getX(),
                                                                   dataVectors.get(dataVectors.size() - 1).getY())))
                            .graphicType(WhiteBoardPanel.GraphicType.POINTS)
                            .circleDiam(10)
                            .color(Color.BLUE));

                    whiteBoard.repaint();
                    if (true) {
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException ie) {
                            // Absorb
                        }
                    }
                });
                if (animateSuspended) {
                    try {
                        synchronized(lock) {
                            System.out.println("    Waiting");
                            lock.wait(); // Expect notify
                            System.out.println("    Waiter notified !");
                        }
                        animateSuspended = false; // double layer...
                    } catch (InterruptedException ie) {
                        System.err.println("Exception in the wait:");
                        ie.printStackTrace();
                    }
//                } else {
//                    System.out.println("Keep working !");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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

        BeziersAtWorkSample01 thisThing = new BeziersAtWorkSample01();// This one has instantiated the white board
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
