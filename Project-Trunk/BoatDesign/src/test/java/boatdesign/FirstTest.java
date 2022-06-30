package boatdesign;

import bezier.Bezier;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
//import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Using default WhiteBoard Writer
 *
 * THIS IS NOT A UNIT TEST.
 *
 * 2D Bezier example.
 * With draggable control points (hence the MouseListener, MouseMotionListener).
 */
public class FirstTest {

    private final static String TITLE = "One 3D Bezier sample (draggable control points)";

    private JFrame frame;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileSpit = new JMenuItem();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Refresh Data"); // Not really useful here.

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Object> initConfig = null;

    // All z = 0, 2D bezier.
    private List<Bezier.Point3D> ctrlPoints = new ArrayList<>();

    // The WhiteBoard instantiation
    private final static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private void fileSpit_ActionPerformed(ActionEvent ae) {
        System.out.println("Ctrl Points:");
        this.ctrlPoints.forEach(pt -> System.out.printf("%s\n", pt));
    }
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
        if (ctrlPoints.size() > 2) { // 3 points minimum.
            for (double t = 0; t <= 1.0; t += 1E-3) {
                Bezier.Point3D tick = bezier.getBezierPoint(t);
                // System.out.println(String.format("%.03f: %s", t, tick.toString()));
                bezierPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
            }
        }
        // Prepare data for display
        // Ctrl Points
        double[] xCtrlPoints = ctrlPoints.stream()
                .mapToDouble(Bezier.Point3D::getX)
                .toArray();
        double[] yCtrlPoints = ctrlPoints.stream()
                .mapToDouble(Bezier.Point3D::getY)
                .toArray();
        List<VectorUtils.Vector2D> ctrlPtsXYVectors = new ArrayList<>();
        for (int i=0; i<xCtrlPoints.length; i++) {
            ctrlPtsXYVectors.add(new VectorUtils.Vector2D(xCtrlPoints[i], yCtrlPoints[i]));
        }

        // Curve points
        double[] xData = bezierPoints.stream()
                .mapToDouble(VectorUtils.Vector3D::getX)
                .toArray();
        double[] yData = bezierPoints.stream()
                .mapToDouble(VectorUtils.Vector3D::getY)
                .toArray();
        List<VectorUtils.Vector2D> dataXYVectors = new ArrayList<>();
        for (int i=0; i<xData.length; i++) {
            dataXYVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
        }

        whiteBoard.setAxisColor(Color.BLACK);
        whiteBoard.setWithGrid(true);
        whiteBoard.resetAllData();

        // Bezier ctrl points series
        // XY
        WhiteBoardPanel.DataSerie ctrlXYSerie = new WhiteBoardPanel.DataSerie()
                .data(ctrlPtsXYVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                .lineThickness(1)
                .color(Color.ORANGE);
        whiteBoard.addSerie(ctrlXYSerie);

        // Bezier points series
        // XY
        WhiteBoardPanel.DataSerie dataXYSerie = new WhiteBoardPanel.DataSerie()
                .data(dataXYVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                .lineThickness(3)
                .color(Color.BLUE);
        whiteBoard.addSerie(dataXYSerie);

        // Finally, display it.
        whiteBoard.repaint();  // This is for a pure Swing context
    }

    private void show() {
        this.frame.setVisible(true);
    }

    private void initComponents() {

        // Initialize [0, 10, 0], [550, 105, 0]
//        ctrlPoints.add(new Bezier.Point3D(0, 10, 0));
//        ctrlPoints.add(new Bezier.Point3D(550, 105, 0));
        if (initConfig != null) {
            Map<String, List<Object>> defaultPoints = (Map)initConfig.get("default-points");
            List<Map<String, Double>> railPoints = (List)defaultPoints.get("rail");
            // List<List<Double>> railPoints = (List)defaultPoints.get("rail");
            // Just the rail for now
            railPoints.stream().forEach(pt -> ctrlPoints.add(new Bezier.Point3D(pt.get("x"), pt.get("y"), pt.get("z"))));
        } else {
            ctrlPoints.add(new Bezier.Point3D(0, 10, 0));
            ctrlPoints.add(new Bezier.Point3D(550, 105, 0));
        }
        FirstTest instance = this;

        whiteBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.println("Click on whiteboard");
                if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
                    Bezier.Point3D closePoint = getClosePoint(e);
                    if (closePoint != null) {
                        BezierPopup popup = new BezierPopup(instance, closePoint);
                        popup.show(whiteBoard, e.getX(), e.getY());
                    }
                } else {
                    // Regular click.
                    // Drop point here. Where is the list
                    String response = JOptionPane.showInputDialog(
                            frame,
                            String.format("Where to insert new point (index in the list [0..%d]) ?", ctrlPoints.size()),
                            "Add Control Point",
                            JOptionPane.QUESTION_MESSAGE);
//            System.out.println("Response:" + response);
                    if (response != null && !response.isEmpty()) {
                        try {
                            int newIndex = Integer.parseInt(response);
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoard.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoard.getCanvasToSpaceYTransformer();
                            int height = whiteBoard.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
                Bezier.Point3D closePoint = getClosePoint(e);
                if (closePoint != null) {
//            System.out.println("Found it!");
                    whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });

        whiteBoard.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                System.out.println("Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoard.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoard.getCanvasToSpaceYTransformer();
                    int height = whiteBoard.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newX = canvasToSpaceXTransformer.apply(e.getX());
                        double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
                System.out.println("Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e);
                if (closePoint != null) {
                    whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    closestPointIndex = ctrlPoints.indexOf(closePoint);
                } else {
                    whiteBoard.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
        menuFileSpit.addActionListener(this::fileSpit_ActionPerformed);
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(this::fileExit_ActionPerformed);
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(this::helpAbout_ActionPerformed);
        menuFile.add(menuFileSpit);
        menuFile.add(menuFileExit);
        menuBar.add(menuFile);
        menuHelp.add(menuHelpAbout);
        menuBar.add(menuHelp);

        topLabel = new JLabel(" " + TITLE); // Ugly!
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        JPanel whiteBoardsPanel = new JPanel(new GridBagLayout());
        JScrollPane jScrollPane = new JScrollPane(whiteBoardsPanel);
//        jScrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT + 50 + 10));
        whiteBoard.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        whiteBoardsPanel.add(whiteBoard, // forTest, // ah merde, // whiteBoardXY,
                new GridBagConstraints(0,
                        0,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

//        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);
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

    public FirstTest() {
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

    static class BezierPopup extends JPopupMenu
            implements ActionListener,
            PopupMenuListener {
        private final JMenuItem deleteMenuItem;

        private final FirstTest parent;
        private final Bezier.Point3D closePoint;

        private final static String DELETE_CTRL_POINT = "Delete Ctrl Point";

        public BezierPopup(FirstTest parent, Bezier.Point3D closePoint) {
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

        String initFileName = "init.json";
        try {
            ClassLoader classLoader = FirstTest.class.getClassLoader();
            URL configResource = classLoader.getResource(initFileName); // At the root of the resources folder.
            initConfig = mapper.readValue(configResource.openStream(), Map.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        File config = new File(initFileName);
//        if (config.exists()) {
//            try {
//                URL configResource = config.toURI().toURL();
//                initConfig = mapper.readValue(configResource.openStream(), Map.class);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        } else {
//            System.out.println("Warning: no init.json was found.");
//        }

        FirstTest thisThing = new FirstTest();// This one has instantiated the white board
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
