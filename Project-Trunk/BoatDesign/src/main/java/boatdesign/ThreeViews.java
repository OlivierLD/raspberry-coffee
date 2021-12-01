package boatdesign;

import bezier.Bezier;
import boatdesign.swingstuff.NewDataPanel;
import boatdesign.threeD.BoatBox3D;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsg.SwingUtils.Box3D;
import gsg.SwingUtils.SwingUtils;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.SwingUtils.fullui.ThreeDPanelWithWidgets;
import gsg.VectorUtils;

import javax.swing.*;
// import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A Swing Application
 *
 * Using default WhiteBoard Writer
 * <p>
 * 2D Bezier example. (<- So so...)
 * With draggable control points (hence the MouseListener, MouseMotionListener).
 * <p>
 * 2 Bézier curves:
 * - One for the rail
 * - One for the keel
 * <p>
 * Bow is correlated, transom too. See in {@link BoatBox3D}
 *
 * Calculation done in BoatBox3D, look for "// Actual shape calculation takes place here." in this BoatBox3D class.
 */
public class ThreeViews {

    // See in the starting script LOGGING_FLAG="-Djava.util.logging.config.file=./logging.properties"
    private final static Logger LOGGER = Logger.getLogger(ThreeViews.class.getName()); //  .getLogger(Logger.GLOBAL_LOGGER_NAME); // BoatBox3D.class;
    static {
        LOGGER.setLevel(Level.ALL);
    }

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minZ;
    private double maxZ;
    private double defaultLHT;

    private final static String TITLE = "3D Bezier Drawing Board (WiP). Rail and Keel.";

    private static JFrame frame;
    private final ThreeDPanelWithWidgets threeDPanel;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileSpit = new JMenuItem();

    // Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() will show as Command on Mac
    private final KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // InputEvent.CTRL_DOWN_MASK);
    private final KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // InputEvent.CTRL_DOWN_MASK);
    private final KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // InputEvent.CTRL_DOWN_MASK);
    private final KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // InputEvent.CTRL_DOWN_MASK);
    private final KeyStroke ctrlQ = KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // InputEvent.CTRL_DOWN_MASK);
    private final KeyStroke ctrlH = KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK);

    private final JMenuItem menuFileNew = new JMenuItem();
    private final JMenuItem menuFileOpen = new JMenuItem();
    private final JMenuItem menuFileEdit = new JMenuItem();
    private final JMenuItem menuFileSave = new JMenuItem();

    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private final JButton refreshButton = new JButton("Refresh Boat Shape"); // Not really useful here.

    // Screen dimension
    private final static int WIDTH = 1536; // 1024;
    private final static int HEIGHT = 800;

    protected final static ObjectMapper mapper = new ObjectMapper();
    private static Map<String, Object> initConfig = null;

    /*
     * Assumptions: Some points are correlated.
     * First rail point and bow top
     * First keel point and bow bottom
     * Transom is defined by the last rail point and last keel point.
     * - Transom Control point(s) defined by them as well.
     */
    private List<Bezier.Point3D> railCtrlPoints = new ArrayList<>();
    private List<Bezier.Point3D> keelCtrlPoints = new ArrayList<>();

    // The WhiteBoard instantiations
    private final WhiteBoardPanel whiteBoardXY; // from above
    private final WhiteBoardPanel whiteBoardXZ; // side
    private final WhiteBoardPanel whiteBoardYZ; // facing

    private VectorUtils.Vector3D centerOfHull = null; // aka Centre de Carene.

    private final BoatBox3D box3D;

    private JTextPane dataTextArea = null;
    private JTextPane boatDataTextArea = null;
    private JTextPane messageTextArea = null;

    private static ThreeViews instance;

    private String description;
    private List<String> comments;

    private void fileNew_ActionPerformed(ActionEvent ae) {
        getLogger().log(Level.INFO, "File New...");
        /*
        Need to get:
        minX, maxX, minY, maxY, minZ, maxZ, defaultLHT
        description, comments
         */
        NewDataPanel panel = new NewDataPanel();
        // Default values?
        panel.setValues(-350,
                350,
                -250,
                250,
                -60,
                120,
                650,
                "Mini",
                "Default values.");

        int resp = JOptionPane.showConfirmDialog(frame,
                panel,
                "New Boat Data - All values in cm in this form.",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            // Grab the data
            NewDataPanel.PanelData panelData = panel.getPanelData();
            Map<String, Object> theMap = new HashMap<>();
            theMap.put("description", panelData.getDescription());
            String comments = panelData.getComments();
            if (comments != null) {
                String[] split = comments.split("\n");
                theMap.put("comments", Arrays.asList(split));
            }
            // Reset
            this.keelCtrlPoints.clear();
            this.railCtrlPoints.clear();

            double minX = panelData.getMinX() * 1e-02;
            double maxX = panelData.getMaxX() * 1e-02;
            double minY = panelData.getMinY() * 1e-02;
            double maxY = panelData.getMaxY() * 1e-02;
            double minZ = panelData.getMinZ() * 1e-02;
            double maxZ = panelData.getMaxZ() * 1e-02;
            // Reshape from [0, X], instead of [-X, X]
            if (minX != 0) {
                maxX -= minX;
                minX -= minX;
            }
            double lht = panelData.getDefaultLHT() * 1e-02;
            // TODO Check values above... min < max, etc.

            // Create arbitrary points
            this.keelCtrlPoints.add(new Bezier.Point3D()
                    .x(0 * 1e2)
                    .y(0 * 1e2)
                    .z((minZ) * 1e2));
            this.keelCtrlPoints.add(new Bezier.Point3D()
                    .x(lht * 1e2)
                    .y(0 * 1e2)
                    .z((minZ * 0.75) * 1e2));
            this.railCtrlPoints.add(new Bezier.Point3D()
                    .x(0 * 1e2)
                    .y(0 /*(maxY * 0.75)*/ * 1e2)
                    .z((maxZ * 0.75) * 1e2));
            this.railCtrlPoints.add(new Bezier.Point3D()
                    .x(lht * 1e2)
                    .y((maxY * 0.75) * 1e2)
                    .z((maxZ * 0.75) * 1e2));

            Map<String, Object> points = new HashMap<>();
            points.put("keel", this.keelCtrlPoints);
            points.put("rail", this.railCtrlPoints);
            theMap.put("default-points", points);

            Map<String, Object> dimensions = new HashMap<>();
            dimensions.put("default-lht", lht);
            dimensions.put("box-x", Arrays.asList(minX, maxX));
            dimensions.put("box-y", Arrays.asList(minY, maxY));
            dimensions.put("box-z", Arrays.asList(minZ, maxZ));
            theMap.put("dimensions", dimensions);

            initConfig = theMap;
            this.reLoadConfig(false);
        }
    }

    private void fileOpen_ActionPerformed(ActionEvent ae) {
        String fName = SwingUtils.chooseFile(null,
                JFileChooser.FILES_ONLY,
                new String[]{"json"},
                "Data Files",
                ".",
                "Select",
                "Choose Data File");
        // And then...
        getLogger().log(Level.INFO, String.format("Opening %s\n", fName));
        File config = new File(fName);
        if (config.exists()) {
            try {
                var configResource = config.toURI().toURL(); // Java 11!
                initConfig = mapper.readValue(configResource.openStream(), Map.class);
                this.reLoadConfig(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.printf("Warning: no %s was found.\n", fName); // Zis is bizarre
        }

    }

    private void fileEdit_ActionPerformed(ActionEvent ae) {

        NewDataPanel panel = new NewDataPanel();
        // Current values.
        panel.setValues(this.box3D.getMinX(),
                this.box3D.getMaxX(),
                this.box3D.getMinY(),
                this.box3D.getMaxY(),
                this.box3D.getMinZ(),
                this.box3D.getMaxZ(),
                this.box3D.getDefaultLHT(),
                this.description,
                this.comments == null ? null : this.comments.stream().collect(Collectors.joining("\n")));

        int resp = JOptionPane.showConfirmDialog(frame,
                panel,
                "Edit Boat Data - All values in cm in this form.",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            // Grab the data
            NewDataPanel.PanelData panelData = panel.getPanelData();
            Map<String, Object> theMap = new HashMap<>();
            theMap.put("description", panelData.getDescription());
            String comments = panelData.getComments();
            if (comments != null) {
                String[] split = comments.split("\n");
                theMap.put("comments", Arrays.asList(split));
            }

            // Reset
            double minX = panelData.getMinX() * 1e-02;
            double maxX = panelData.getMaxX() * 1e-02;
            double minY = panelData.getMinY() * 1e-02;
            double maxY = panelData.getMaxY() * 1e-02;
            double minZ = panelData.getMinZ() * 1e-02;
            double maxZ = panelData.getMaxZ() * 1e-02;
            // Reshape from [0, X], instead of [-X, X]
            if (minX != 0) {
                maxX -= minX;
                minX -= minX;
            }
            double lht = panelData.getDefaultLHT() * 1e-02;
            // TODO Check values above... min < max, etc.

            // Create NO arbitrary points. Keep them as they are.

            Map<String, Object> points = new HashMap<>();
            points.put("keel", this.keelCtrlPoints);
            points.put("rail", this.railCtrlPoints);
            theMap.put("default-points", points);

            Map<String, Object> dimensions = new HashMap<>();
            dimensions.put("default-lht", lht);
            dimensions.put("box-x", Arrays.asList(minX, maxX));
            dimensions.put("box-y", Arrays.asList(minY, maxY));
            dimensions.put("box-z", Arrays.asList(minZ, maxZ));
            theMap.put("dimensions", dimensions);

            initConfig = theMap;
            this.reLoadConfig(false);
        }
    }

    private void fileSave_ActionPerformed(ActionEvent ae) {
        /*
        minX, maxX, minY, maxY, minZ, maxZ, defaultLHT
        description, comments
         */
        NewDataPanel panel = new NewDataPanel(true);

        panel.setValues(this.box3D.getMinX(),
                this.box3D.getMaxX(),
                this.box3D.getMinY(),
                this.box3D.getMaxY(),
                this.box3D.getMinZ(),
                this.box3D.getMaxZ(),
                this.box3D.getDefaultLHT(),
                this.description,
                this.comments == null ? null : this.comments.stream().collect(Collectors.joining("\n")));

        int resp = JOptionPane.showConfirmDialog(frame,
                panel,
                "Boat Data",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            // Grab the data
            NewDataPanel.PanelData panelData = panel.getPanelData();
            Map<String, Object> theMap = new HashMap<>();
            theMap.put("description", panelData.getDescription());
            String comments = panelData.getComments();
            if (comments != null) {
                String[] split = comments.split("\n");
                theMap.put("comments", split);
            }
            Map<String, Object> points = new HashMap<>();
            points.put("keel", this.keelCtrlPoints);
            points.put("rail", this.railCtrlPoints);
            theMap.put("default-points", points);

            Map<String, Object> dimensions = new HashMap<>();
            dimensions.put("default-lht", panelData.getDefaultLHT() * 1e-02);
            double minX = panelData.getMinX() * 1e-02;
            double maxX = panelData.getMaxX() * 1e-02;
            // Reshape from [0, X], instead of [-X, X]
            if (minX != 0) {
                maxX -= minX;
                minX -= minX;
            }
            dimensions.put("box-x", new double[]{minX, maxX});
            dimensions.put("box-y", new double[]{panelData.getMinY() * 1e-02, panelData.getMaxY() * 1e-02});
            dimensions.put("box-z", new double[]{panelData.getMinZ() * 1e-02, panelData.getMaxZ() * 1e-02});
            theMap.put("dimensions", dimensions);

            try {
                String value = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(theMap);
                String fileName = panelData.getFileName();
                if (fileName != null) {
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                        bw.write(value);
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    getLogger().log(Level.INFO, "Honk!!");
                    JOptionPane.showMessageDialog(frame,
                            "Please provide a file name!",
                            "File name is missing",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    private void fileSpit_ActionPerformed(ActionEvent ae) {
        getLogger().log(Level.INFO, "Ctrl Points:\nRail:");
        this.railCtrlPoints.forEach(pt -> getLogger().log(Level.INFO, String.format("%s", pt)));
        getLogger().log(Level.INFO, "Keel:");
        this.keelCtrlPoints.forEach(pt -> getLogger().log(Level.INFO, String.format("%s", pt)));
    }

    private void fileExit_ActionPerformed(ActionEvent ae) {
//        System.out.printf("Exit requested %s, bye now.\n", ae);
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
//        System.out.printf("Help requested %s\n", ae);
        String HelpContent = "Boat Design, based on Bézier curves.\n" +
                "Manage the Bézier Control Points on the three panes on the left\n" +
                "and see the result on the right.\n" +
                "Draw the full boat by hitting the 'Refresh Boat Shape' button.";
        JOptionPane.showMessageDialog(frame, HelpContent, TITLE, JOptionPane.PLAIN_MESSAGE);
    }

    private void refreshBoatShape() {
        AtomicBoolean keepLooping = new AtomicBoolean(true);
        Thread repainter = new Thread(() -> {
            while (keepLooping.get()) {
                try {
                    this.box3D.repaint();
                } catch (Exception cme) {
                    System.err.println(">>> " + cme);
                }
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    // Absorb
                }
            }
            // Done
            getLogger().log(Level.INFO, "Done repainting.");
        });
        repainter.start();

        Thread refresher = new Thread(() -> {
            getLogger().log(Level.INFO, "Starting refresh...");
            refreshButton.setEnabled(false);
            boatDataTextArea.setText("Re-calculating...");
            // TODO Stop thread if already running.
            this.box3D.refreshData(false, map -> {
                String json;
                try {
                    json = mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(map);
                } catch (JsonProcessingException jpe) {
                    json = map.toString();
                }
                boatDataTextArea.setText(json);

                if (map.get("lwl-start") != null) {
                    // CC position
                    Double xCC = (Double) map.get("cc-x");
                    Double zCC = (Double) map.get("cc-z");
                    if (xCC != null && zCC != null) {
                        // Display on 2D whiteboards
                        centerOfHull = new VectorUtils.Vector3D()
                                .x(xCC * 1e2)
                                .y(0d)
                                .z(zCC * 1e2);
                    }
                    // Displacement for the area curve
                    Map<Double, Double> displacementMap = (Map<Double, Double>) map.get("displacement-x-map");
                    double lwlStart = (double) map.get("lwl-start");
                    double lwlEnd = (double) map.get("lwl-end");
                    // TODO: send to XY
                    getLogger().log(Level.INFO, "Will send to Area Curve");
                    if (true) {
                        // 1 - Find max area
                        Double wbMaxY = whiteBoardXY.getForcedMaxY();
                        AtomicReference<Double> max = new AtomicReference<>(-Double.MAX_VALUE);
                        displacementMap.forEach((k, v) -> max.set(Math.max(max.get(), v)));
                        getLogger().log(Level.INFO, "Max area: " + max);
                        double ratio = (wbMaxY / max.get());
                        // TODO Remove the area curve if it exists
                        List<VectorUtils.Vector2D> areas = new ArrayList<>();
                        areas.add(new VectorUtils.Vector2D().x(lwlStart * 1e2).y(0d));
                        displacementMap.forEach((k, v) -> areas.add(new VectorUtils.Vector2D().x(k).y(v * ratio)));
                        areas.add(new VectorUtils.Vector2D().x(lwlEnd * 1e2).y(0d));

                        WhiteBoardPanel.DataSerie areasSerie = new WhiteBoardPanel.DataSerie()
                                .data(areas)
                                .graphicType(WhiteBoardPanel.GraphicType.AREA)
                                .areaGradient(new Color(255, 0, 0, 128), new Color(255, 165, 0, 128))
                                .color(Color.BLACK);
                        whiteBoardXY.addSerie(areasSerie);
                    }
                }
            }, mess -> {
                messageTextArea.setText(mess);
            });
            // Stop repainter
            keepLooping.set(false);
            getLogger().log(Level.INFO, "Refresh completed!");
            refreshButton.setEnabled(true);
            this.box3D.repaint();
            if (centerOfHull != null) {
                // XZ
                List<VectorUtils.Vector2D> xzCC = List.of(new VectorUtils.Vector2D(centerOfHull.getX(), centerOfHull.getZ()));
                WhiteBoardPanel.DataSerie ccXZSerie = new WhiteBoardPanel.DataSerie()
                        .data(xzCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                whiteBoardXZ.addSerie(ccXZSerie);

                WhiteBoardPanel.TextSerie ccXZTextSerie = new WhiteBoardPanel.TextSerie(xzCC.get(0), "CC", 0, 6, WhiteBoardPanel.TextSerie.Justification.CENTER);
                ccXZTextSerie.setTextColor(new Color(0, 102, 0, 200));
                ccXZTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                whiteBoardXZ.resetAllText();
                whiteBoardXZ.addTextSerie(ccXZTextSerie);
                whiteBoardXZ.repaint();
                // YZ
                List<VectorUtils.Vector2D> yzCC = List.of(new VectorUtils.Vector2D(centerOfHull.getY(), centerOfHull.getZ()));
                WhiteBoardPanel.DataSerie ccYZSerie = new WhiteBoardPanel.DataSerie()
                        .data(yzCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                whiteBoardYZ.addSerie(ccYZSerie);
                WhiteBoardPanel.TextSerie ccYZTextSerie = new WhiteBoardPanel.TextSerie(yzCC.get(0), "CC", 7, -6, WhiteBoardPanel.TextSerie.Justification.LEFT);
                ccYZTextSerie.setTextColor(new Color(0, 102, 0, 200));
                ccYZTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                whiteBoardYZ.resetAllText();
                whiteBoardYZ.addTextSerie(ccYZTextSerie);
                whiteBoardYZ.repaint();
                // XY
                List<VectorUtils.Vector2D> xyCC = List.of(new VectorUtils.Vector2D(centerOfHull.getX(), centerOfHull.getY()));
                WhiteBoardPanel.DataSerie ccXYSerie = new WhiteBoardPanel.DataSerie()
                        .data(xyCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                whiteBoardXY.addSerie(ccXYSerie);
                WhiteBoardPanel.TextSerie ccXYTextSerie = new WhiteBoardPanel.TextSerie(xyCC.get(0), "CC", 0, -14, WhiteBoardPanel.TextSerie.Justification.CENTER);
                ccXYTextSerie.setTextColor(new Color(0, 102, 0, 200));
                ccXYTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                whiteBoardXY.resetAllText();
                whiteBoardXY.addTextSerie(ccXYTextSerie);
                whiteBoardXY.repaint();
            }
        });
        refresher.start();
    }

    private Map<String, Object> generateBezierJson() {
        return Map.of("rail", railCtrlPoints, "keel", keelCtrlPoints);
    }

    private void refreshData() {

        if (railCtrlPoints.size() > 0 && keelCtrlPoints.size() > 0) {

            // Tell the 3D box
            this.box3D.setRailCtrlPoints(railCtrlPoints); // The rail.
            this.box3D.setKeelCtrlPoints(keelCtrlPoints); // The keel.

            // Display in textArea
            try {
                String json = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(generateBezierJson());
                dataTextArea.setText(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

//            String content = "Control Points:\nRail:\n" + railCtrlPoints.stream()
//                    .map(pt -> String.format("%d: %s", railCtrlPoints.indexOf(pt), pt.toString()))
//                    .collect(Collectors.joining("\n"));
//            content += "\nKeel:\n" + keelCtrlPoints.stream()
//                    .map(pt -> String.format("%d: %s", keelCtrlPoints.indexOf(pt), pt.toString()))
//                    .collect(Collectors.joining("\n"));
//
//            dataTextArea.setText(content);

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
                    // getLogger().log(Level.INFO, String.format("%.03f: %s", t, tick.toString()));
                    bezierRailPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }
            double[] xRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getX)
                    .toArray();
            double[] yRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getY)
                    .toArray();
            double[] zRailCtrlPoints = railCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getZ)
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
                    .mapToDouble(VectorUtils.Vector3D::getX)
                    .toArray();
            double[] yData = bezierRailPoints.stream()
                    .mapToDouble(VectorUtils.Vector3D::getY)
                    .toArray();
            double[] zData = bezierRailPoints.stream()
                    .mapToDouble(VectorUtils.Vector3D::getZ)
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
                    // getLogger().log(Level.INFO, String.format("%.03f: %s", t, tick.toString()));
                    bezierKeelPoints.add(new VectorUtils.Vector3D(tick.getX(), tick.getY(), tick.getZ()));
                }
            }
            double[] xKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getX)
                    .toArray();
            double[] yKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getY)
                    .toArray();
            double[] zKeelCtrlPoints = keelCtrlPoints.stream()
                    .mapToDouble(Bezier.Point3D::getZ)
                    .toArray();
            List<VectorUtils.Vector2D> keelCtrlPtsXYVectors = new ArrayList<>();
            for (int i = 0; i < xKeelCtrlPoints.length; i++) {
                synchronized(keelCtrlPtsXYVectors) {
                    keelCtrlPtsXYVectors.add(new VectorUtils.Vector2D(xKeelCtrlPoints[i], yKeelCtrlPoints[i]));
                }
            }
            List<VectorUtils.Vector2D> keelCtrlPtsXZVectors = new ArrayList<>();
            for (int i = 0; i < xKeelCtrlPoints.length; i++) {
                synchronized(keelCtrlPtsXZVectors) {
                    keelCtrlPtsXZVectors.add(new VectorUtils.Vector2D(xKeelCtrlPoints[i], zKeelCtrlPoints[i]));
                }
            }
            List<VectorUtils.Vector2D> keelCtrlPtsYZVectors = new ArrayList<>();
            for (int i = 0; i < yKeelCtrlPoints.length; i++) {
                synchronized(keelCtrlPtsYZVectors) {
                    keelCtrlPtsYZVectors.add(new VectorUtils.Vector2D(yKeelCtrlPoints[i], zKeelCtrlPoints[i]));
                }
            }

            // Curve points
            xData = bezierKeelPoints.stream()
                    .mapToDouble(VectorUtils.Vector3D::getX)
                    .toArray();
            yData = bezierKeelPoints.stream()
                    .mapToDouble(VectorUtils.Vector3D::getY)
                    .toArray();
            zData = bezierKeelPoints.stream()
                    .mapToDouble(VectorUtils.Vector3D::getZ)
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
        frame.setVisible(true);
    }

    private void initConfiguration(boolean full) {
        if (initConfig != null) {

            Map<String, Object> dimensions = (Map) initConfig.get("dimensions");
            double defaultLht = (double) dimensions.get("default-lht");
            List<Double> boxX = (List<Double>) dimensions.get("box-x");
            List<Double> boxY = (List<Double>) dimensions.get("box-y");
            List<Double> boxZ = (List<Double>) dimensions.get("box-z");

            // Values in cm
            this.defaultLHT = defaultLht * 1e2;
            this.minX = boxX.get(0) * 1e2;
            this.maxX = boxX.get(1) * 1e2;
            this.minY = boxY.get(0) * 1e2;
            this.maxY = boxY.get(1) * 1e2;
            this.minZ = boxZ.get(0) * 1e2;
            this.maxZ = boxZ.get(1) * 1e2;

            Map<String, List<Object>> defaultPoints = (Map) initConfig.get("default-points");

            if (full) {
                List railPoints = defaultPoints.get("rail");
                //            List<List<Double>> bowPoints = (List)defaultPoints.get("bow"); // Correlated, not needed here.
                List keelPoints = defaultPoints.get("keel");
                // Rail
                synchronized (this.railCtrlPoints) {
                    railPoints.forEach(pt -> {
                        if (pt instanceof Map) {
                            this.railCtrlPoints.add(new Bezier.Point3D(((Map<String, Double>) pt).get("x"), ((Map<String, Double>) pt).get("y"), ((Map<String, Double>) pt).get("z")));
                        } else if (pt instanceof Bezier.Point3D) {
                            this.railCtrlPoints.add((Bezier.Point3D) pt);
                        }
                    });
                }
                // Keel
                synchronized (this.keelCtrlPoints) {
                    keelPoints.forEach(pt -> {
                        if (pt instanceof Map) {
                            this.keelCtrlPoints.add(new Bezier.Point3D(((Map<String, Double>) pt).get("x"), ((Map<String, Double>) pt).get("y"), ((Map<String, Double>) pt).get("z")));
                        } else if (pt instanceof Bezier.Point3D) {
                            this.keelCtrlPoints.add((Bezier.Point3D) pt);
                        }
                    });
                }
            }
            this.description = (String) initConfig.get("description");
            this.comments = (List) initConfig.get("comments");
        } else {
            // TODO There is a problem when ctrlPoints is empty... Fix it.
            // Initialize [0, 10, 0], [550, 105, 0]
            railCtrlPoints.add(new Bezier.Point3D(0, 10, 0));
            railCtrlPoints.add(new Bezier.Point3D(550, 105, 0));

            keelCtrlPoints.add(new Bezier.Point3D(10d, 0d, -5d));
            keelCtrlPoints.add(new Bezier.Point3D(550d, 0d, 5d));
        }
    }

    private void refreshBox3D() {
        // Tell the 3D box
        this.box3D.setRailCtrlPoints(railCtrlPoints); // The rail.
        this.box3D.setKeelCtrlPoints(keelCtrlPoints); // The keel.
    }

    private void initComponents() {

        this.refreshBox3D();

        // Override defaults (not mandatory)

        // XY
        whiteBoardXY.setAxisColor(Color.BLACK);
        whiteBoardXY.setGridColor(Color.GRAY);
        whiteBoardXY.setForceTickIncrement(50);
        whiteBoardXY.setEnforceXAxisAt(0d);
        whiteBoardXY.setEnforceYAxisAt(0d);

        whiteBoardXY.setWithGrid(true);
        whiteBoardXY.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXY.setGraphicTitle("XY - Top"); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXY.setTitleJustification(WhiteBoardPanel.TitleJustification.RIGHT);
        whiteBoardXY.setSize(new Dimension(800, 200));
        whiteBoardXY.setPreferredSize(new Dimension(600, 250));
        whiteBoardXY.setTextColor(new Color(192, 192, 192));
        whiteBoardXY.setTitleFont(whiteBoardXY.getFont().deriveFont(Font.BOLD | Font.ITALIC, 16));
        whiteBoardXY.setGraphicMargins(30);
        whiteBoardXY.setXEqualsY(true); // false);
        whiteBoardXY.setXAxisLabel("X");
        whiteBoardXY.setYAxisLabel("Y");
        whiteBoardXY.setAxisLabelCircleColor(Color.RED);
        whiteBoardXY.setAxisLabelColor(Color.BLUE);
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
        whiteBoardXZ.setGraphicTitle("XZ - Side"); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardXZ.setTitleJustification(WhiteBoardPanel.TitleJustification.RIGHT);
        whiteBoardXZ.setSize(new Dimension(800, 200));
        whiteBoardXZ.setPreferredSize(new Dimension(600, 250));
        whiteBoardXZ.setTextColor(new Color(192, 192, 192));
        whiteBoardXZ.setTitleFont(whiteBoardXZ.getFont().deriveFont(Font.BOLD | Font.ITALIC, 16));
        whiteBoardXZ.setGraphicMargins(30);
        whiteBoardXZ.setXEqualsY(true); // false);
        whiteBoardXZ.setXAxisLabel("X");
        whiteBoardXZ.setYAxisLabel("Z");
        whiteBoardXZ.setAxisLabelCircleColor(Color.RED);
        whiteBoardXZ.setAxisLabelColor(Color.BLUE);
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
        whiteBoardYZ.setGraphicTitle("YZ - Face"); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoardYZ.setTitleJustification(WhiteBoardPanel.TitleJustification.RIGHT);
        whiteBoardYZ.setSize(new Dimension(400, 200));
        whiteBoardYZ.setPreferredSize(new Dimension(400, 250));
        whiteBoardYZ.setTextColor(new Color(192, 192, 192));
        whiteBoardYZ.setTitleFont(whiteBoardYZ.getFont().deriveFont(Font.BOLD | Font.ITALIC, 16));
        whiteBoardYZ.setGraphicMargins(30);
        whiteBoardYZ.setXEqualsY(true); // false);
        whiteBoardYZ.setXAxisLabel("Y");
        whiteBoardYZ.setYAxisLabel("Z");
        whiteBoardYZ.setAxisLabelCircleColor(Color.RED);
        whiteBoardYZ.setAxisLabelColor(Color.BLUE);
        // Enforce Y amplitude
        whiteBoardYZ.setForcedMinY(-50d);
        whiteBoardYZ.setForcedMaxY(100d);

        // ThreeViewsV2 instance = this;

        whiteBoardXY.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                getLogger().log(Level.INFO, "Click on whiteboard");
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
//                  getLogger().log(Level.INFO, "Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
                            // int height = whiteBoardXY.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXY, Orientation.XY);
                if (closePoint != null) {
//            getLogger().log(Level.INFO, "Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardXY.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                getLogger().log(Level.INFO, "Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXY.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXY.getCanvasToSpaceYTransformer();
//                    int height = whiteBoardXY.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newX = canvasToSpaceXTransformer.apply(e.getX());
                        double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
//                getLogger().log(Level.INFO, "Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXY, Orientation.XY);
                if (closePoint != null) {
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
//                        getLogger().log(Level.INFO, "Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
                } else {
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }
                VectorUtils.Vector2D whiteBoardMousePos = getWhiteBoardMousePos(e, whiteBoardXY); //, Orientation.XZ);
                if (whiteBoardMousePos != null) {
                    whiteBoardXY.setToolTipText(String.format("<html>X: %.02f<br>Y: %.02f</html>",
                            whiteBoardMousePos.getX(), whiteBoardMousePos.getY()));
                }
            }
        });

        whiteBoardXZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                getLogger().log(Level.INFO, "Click on whiteboard");
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
//                  getLogger().log(Level.INFO, "Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXZ.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXZ.getCanvasToSpaceYTransformer();
//                            int height = whiteBoardXZ.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
//                              System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().x(newX).z(newY);
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
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                if (closePoint != null) {
//            getLogger().log(Level.INFO, "Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardXZ.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                getLogger().log(Level.INFO, "Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardXZ.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardXZ.getCanvasToSpaceYTransformer();
//                    int height = whiteBoardXZ.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newX = canvasToSpaceXTransformer.apply(e.getX());
                        double newZ = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
//                getLogger().log(Level.INFO, "Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                if (closePoint != null) {
                    whiteBoardXZ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
//                        getLogger().log(Level.INFO, "Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
//                    closestPointIndex = railCtrlPoints.indexOf(closePoint);
                } else {
                    whiteBoardXZ.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }
                VectorUtils.Vector2D whiteBoardMousePos = getWhiteBoardMousePos(e, whiteBoardXZ); //, Orientation.XZ);
                if (whiteBoardMousePos != null) {
                    whiteBoardXZ.setToolTipText(String.format("<html>X: %.02f<br>Z: %.02f</html>",
                            whiteBoardMousePos.getX(), whiteBoardMousePos.getY()));
                }
            }
        });

        whiteBoardYZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                getLogger().log(Level.INFO, "Click on whiteboard");
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
//                  getLogger().log(Level.INFO, "Response:" + response);
                    if (response == JOptionPane.OK_OPTION) {
                        try {
                            int newIndex = addPointPanel.getPos();
                            AddCtrlPointPanel.CurveName curve = addPointPanel.getCurve();
                            Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardYZ.getCanvasToSpaceXTransformer();
                            Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardYZ.getCanvasToSpaceYTransformer();
//                            int height = whiteBoardYZ.getHeight();
                            if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                                double newX = canvasToSpaceXTransformer.apply(e.getX());
                                double newY = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
//                              System.out.printf("Point dragged to %f / %f\n", newX, newY);
                                Bezier.Point3D point3D = new Bezier.Point3D().y(newX).z(newY);
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
//            getLogger().log(Level.INFO, "Found it!");
                    whiteBoardXY.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        });
        whiteBoardYZ.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
//                getLogger().log(Level.INFO, "Dragged on whiteboard");
                if (closestPointIndex > -1) {
                    Function<Integer, Double> canvasToSpaceXTransformer = whiteBoardYZ.getCanvasToSpaceXTransformer();
                    Function<Integer, Double> canvasToSpaceYTransformer = whiteBoardYZ.getCanvasToSpaceYTransformer();
//                    int height = whiteBoardYZ.getHeight();
                    if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
                        double newY = canvasToSpaceXTransformer.apply(e.getX());
                        double newZ = canvasToSpaceYTransformer.apply(/*height -*/ e.getY());
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
//                getLogger().log(Level.INFO, "Moved on whiteboard (MotionListener)");
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardYZ, Orientation.YZ);
                if (closePoint != null) {
                    whiteBoardYZ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (railCtrlPoints.contains(closePoint)) {
                        closestPointIndex = railCtrlPoints.indexOf(closePoint);
                    } else {
//                        getLogger().log(Level.INFO, "Close Point on the keel!");
                        closestPointIndex = railCtrlPoints.size() + keelCtrlPoints.indexOf(closePoint);
                    }
//                    closestPointIndex = railCtrlPoints.indexOf(closePoint);
                } else {
                    whiteBoardYZ.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    closestPointIndex = -1;
                }
                VectorUtils.Vector2D whiteBoardMousePos = getWhiteBoardMousePos(e, whiteBoardYZ); //, Orientation.XZ);
                if (whiteBoardMousePos != null) {
                    whiteBoardYZ.setToolTipText(String.format("<html>Y: %.02f<br>Z: %.02f</html>",
                            whiteBoardMousePos.getX(), whiteBoardMousePos.getY()));
                }
            }
        });

        // The JFrame
        frame = new JFrame(TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        frameSize.height = Math.min(frameSize.height, screenSize.height);
        frameSize.width = Math.min(frameSize.width, screenSize.width);
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
        menuFileSpit.addActionListener(this::fileSpit_ActionPerformed);

        menuFileNew.setText("New...");
        menuFileNew.setAccelerator(ctrlN);
        menuFileNew.addActionListener(this::fileNew_ActionPerformed);
        menuFileOpen.setText("Open...");
        menuFileOpen.setAccelerator(ctrlO);
        menuFileOpen.addActionListener(this::fileOpen_ActionPerformed);
        menuFileEdit.setText("Edit...");
        menuFileEdit.setAccelerator(ctrlE);
        menuFileEdit.addActionListener(this::fileEdit_ActionPerformed);
        menuFileSave.setText("Save...");
        menuFileSave.setAccelerator(ctrlS);
        menuFileSave.addActionListener(this::fileSave_ActionPerformed);

        menuFileExit.setText("Exit");
        menuFileExit.setAccelerator(ctrlQ);
        menuFileExit.addActionListener(this::fileExit_ActionPerformed);
        menuHelp.setText("Help");
        menuHelpAbout.setText("About");
        menuHelpAbout.setAccelerator(ctrlH);
        menuHelpAbout.addActionListener(this::helpAbout_ActionPerformed);
        menuFile.add(menuFileSpit);
        menuFile.add(new JSeparator());
        menuFile.add(menuFileNew);
        menuFile.add(menuFileOpen);
        menuFile.add(menuFileEdit);
        menuFile.add(menuFileSave);
        menuFile.add(new JSeparator());
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
        ctrlPointsPanel.setBorder(BorderFactory.createTitledBorder("Bezier Data"));
        dataTextArea = new JTextPane();
        dataTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.setPreferredSize(new Dimension(300, 225));
        // dataScrollPane.setSize(new Dimension(300, 250));
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
        JCheckBox framesCheckBox = new JCheckBox("Frames");
        JCheckBox beamsCheckBox = new JCheckBox("Beams");
        JCheckBox waterlinesCheckBox = new JCheckBox("Waterlines");
        JCheckBox buttocksCheckBox = new JCheckBox("Buttocks");
        JCheckBox ctrlPointsCheckBox = new JCheckBox("Ctrl-Points");

        JPanel frameStepPanel = new JPanel();
        JFormattedTextField frameStepValue = new JFormattedTextField(new DecimalFormat("#0.0"));
        frameStepValue.setValue(this.box3D.getFrameIncrement());
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
                        box3D.setFrameIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe);
                    }
                }
            }
        });
        frameStepPanel.add(new JLabel("Step:"));
        frameStepPanel.add(frameStepValue);

        JPanel wlStepPanel = new JPanel();
        JFormattedTextField wlStepValue = new JFormattedTextField(new DecimalFormat("#0.00"));
        wlStepValue.setValue(this.box3D.getWlIncrement());
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
                        box3D.setWlIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe);
                    }
                }
            }
        });
        wlStepPanel.add(new JLabel("Step:"));
        wlStepPanel.add(wlStepValue);

        JPanel buttockStepPanel = new JPanel();
        JFormattedTextField buttockStepValue = new JFormattedTextField(new DecimalFormat("#0.0"));
        buttockStepValue.setValue(this.box3D.getButtockIncrement());
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
                        box3D.setButtockIncrement(val);
                        box3D.repaint();
                    } catch (NumberFormatException nfe) {
                        System.err.println(nfe);
                    }
                }
            }
        });
        buttockStepPanel.add(new JLabel("Step:"));
        buttockStepPanel.add(buttockStepValue);

        boolean justBoatSelected = this.box3D.isJustTheBoat();
        justBoatCheckBox.setSelected(this.box3D.isJustTheBoat());
        justBoatCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setJustTheBoat(selected);
            this.box3D.repaint();
            ctrlPointsCheckBox.setEnabled(!selected);
        });
        ctrlPointsCheckBox.setEnabled(!justBoatSelected);

        symmetricCheckBox.setSelected(this.box3D.isSymmetrical());
        symmetricCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setSymmetrical(selected);
            this.box3D.repaint();
        });

        framesCheckBox.setSelected(this.box3D.isFrames());
        framesCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setFrames(selected);
            this.box3D.repaint();
            beamsCheckBox.setEnabled(selected);

        });
        beamsCheckBox.setSelected(this.box3D.isBeams());
        beamsCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setBeams(selected);
            this.box3D.repaint();
        });


        waterlinesCheckBox.setSelected(this.box3D.isWaterlines());
        waterlinesCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setWaterlines(selected);
            this.box3D.repaint();
        });
        buttocksCheckBox.setSelected(this.box3D.isButtocks());
        buttocksCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setButtocks(selected);
            this.box3D.repaint();
        });
        ctrlPointsCheckBox.setSelected(this.box3D.isDrawFrameCtrlPoints());
        ctrlPointsCheckBox.addActionListener(evt -> {
            boolean selected = ((JCheckBox) evt.getSource()).isSelected();
//            System.out.printf("Checkbox is %s\n", selected ? "selected" : "not selected");
            this.box3D.setDrawFrameCtrlPoints(selected);
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

        JPanel frameBoxesPanel = new JPanel();
//        frameBoxesPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        frameBoxesPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        frameBoxesPanel.add(framesCheckBox);
        frameBoxesPanel.add(beamsCheckBox);

        topWidgetsPanel.add(frameBoxesPanel,
                new GridBagConstraints(2,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(waterlinesCheckBox,
                new GridBagConstraints(3,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(buttocksCheckBox,
                new GridBagConstraints(4,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(ctrlPointsCheckBox,
                new GridBagConstraints(5,
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
                new GridBagConstraints(2,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(wlStepPanel,
                new GridBagConstraints(3,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        topWidgetsPanel.add(buttockStepPanel,
                new GridBagConstraints(4,
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

        JPanel bottomRightPanel = new JPanel(new GridBagLayout());
        bottomRightPanel.add(ctrlPointsPanel,
                new GridBagConstraints(0,
                        0,
                        1,
                        2,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        JPanel boatDataPanel = new JPanel(new BorderLayout());
        boatDataPanel.setBorder(BorderFactory.createTitledBorder("Boat Data"));
        boatDataTextArea = new JTextPane();
        boatDataTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        boatDataTextArea.setText("Boat Data...");
        JScrollPane boatDataScrollPane = new JScrollPane(boatDataTextArea);
        boatDataScrollPane.setPreferredSize(new Dimension(300, 100));
        boatDataPanel.add(boatDataScrollPane, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        messageTextArea = new JTextPane();
        messageTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        messageTextArea.setText("Messages...");
        JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
        messageScrollPane.setPreferredSize(new Dimension(300, 100));
        messagePanel.add(messageScrollPane, BorderLayout.NORTH);

        bottomRightPanel.add(boatDataPanel,
                new GridBagConstraints(1,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomRightPanel.add(messagePanel,
                new GridBagConstraints(1,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        rightPane.add(bottomRightPanel, BorderLayout.SOUTH);

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

        this.initConfiguration(true);
        /*BoatBox3D*/
        this.box3D = new BoatBox3D(minX, maxX, minY, maxY, minZ, maxZ, defaultLHT, this);
        threeDPanel = new ThreeDPanelWithWidgets(box3D);
        this.initComponents();
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    private void reLoadConfig(boolean full) {
        if (full) {
            this.railCtrlPoints.clear();
            this.keelCtrlPoints.clear();
        }

        try {
            this.initConfiguration(full);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Refresh also the offsets and Co, default-lht etc.
        this.box3D.refreshValues(this.minX, this.maxX, this.minY, this.maxY, this.minZ, this.maxZ, this.defaultLHT);
        // Refresh the text fields
        this.threeDPanel.setMinXValue();
        this.threeDPanel.setMaxXValue();
        this.threeDPanel.setMinYValue();
        this.threeDPanel.setMaxYValue();
        this.threeDPanel.setMinZValue();
        this.threeDPanel.setMaxZValue();

        // Refresh WhiteBoards dimensions
        // From above
        this.whiteBoardXY.setForcedMinY(0d);
        this.whiteBoardXY.setForcedMaxY(this.maxY / 2);

        // Facing
        this.whiteBoardYZ.setForcedMinY(this.minZ);
        this.whiteBoardYZ.setForcedMaxY(this.maxZ);

        // Side view
        this.whiteBoardXZ.setForcedMinY(this.minZ);
        this.whiteBoardXZ.setForcedMaxY(this.maxZ);

        this.refreshData();
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

    // Find the position (in the 2D space) of the mouse pointer on the white board.
    private VectorUtils.Vector2D getWhiteBoardMousePos(MouseEvent me, WhiteBoardPanel wbp) {

        VectorUtils.Vector2D where = null;
        Function<Integer, Double> canvasToSpaceXTransformer = wbp.getCanvasToSpaceXTransformer();
        Function<Integer, Double> canvasToSpaceYTransformer = wbp.getCanvasToSpaceYTransformer();
//        int height = wbp.getHeight();
        if (canvasToSpaceXTransformer != null && canvasToSpaceYTransformer != null) {
            double x = canvasToSpaceXTransformer.apply(me.getX());
            double y = canvasToSpaceYTransformer.apply(me.getY()); // + wbp.getForcedMinY();
//            getLogger().log(Level.INFO, String.format("Mouse: X:%d, Y:%d - height: %d, fMinY: %.02f, fMaxY:%.02f, X:%.02f, Y:%.02f",
//                    me.getX(), me.getY(),
//                    height, wbp.getForcedMinY(), wbp.getForcedMaxY(), x, y));
            where = new VectorUtils.Vector2D().x(x).y(y);
        }
        return where;
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
        private final JMenuItem deleteMenuItem;
        private final JMenuItem editMenuItem;

        private final ThreeViews parent;
        private final Bezier.Point3D closePoint;

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
        private final transient Border border = BorderFactory.createEtchedBorder();
        private final GridBagLayout layoutMain = new GridBagLayout();
        private final JLabel xLabel = new JLabel("X");
        private final JFormattedTextField xValue = new JFormattedTextField(new DecimalFormat("#0.0000"));
        private final JLabel yLabel = new JLabel("Y");
        private final JFormattedTextField yValue = new JFormattedTextField(new DecimalFormat("#0.0000"));
        private final JLabel zLabel = new JLabel("Z");
        private final JFormattedTextField zValue = new JFormattedTextField(new DecimalFormat("#0.0000"));

        private final double x, y, z;

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
        private final transient Border border = BorderFactory.createEtchedBorder();
        private final GridBagLayout layoutMain = new GridBagLayout();
        private final JRadioButton railButton = new JRadioButton("Rail");
        private final JRadioButton keelButton = new JRadioButton("Keel");
        private final ButtonGroup group = new ButtonGroup();

        private final JLabel posLabel = new JLabel("Index in Rail...");
        private final JFormattedTextField posValue = new JFormattedTextField(new DecimalFormat("#0"));

        enum CurveName {
            RAIL, KEEL
        }

        private final int railCard, keelCard;

        public AddCtrlPointPanel(int railCardinality, int keelCardinality) {
            this.railCard = railCardinality;
            this.keelCard = keelCardinality;

            this.railButton.addChangeListener(evt -> {
                String label = ((JRadioButton) evt.getSource()).isSelected() ? "rail" : "keel";
                int card = ((JRadioButton) evt.getSource()).isSelected() ? this.railCard : this.keelCard;
                posLabel.setText(String.format("Index in %s [0..%d]", label, card));
            });
            this.keelButton.addChangeListener(evt -> {
                String label = ((JRadioButton) evt.getSource()).isSelected() ? "keel" : "rail";
                int card = ((JRadioButton) evt.getSource()).isSelected() ? this.keelCard : this.railCard;
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
        String initFileName = "init.json";
        initFileName = System.getProperty("init-file", initFileName);
        System.out.printf("Opening %s\n", initFileName);

        URL configResource = null;
        try {
            ClassLoader classLoader = ThreeViews.class.getClassLoader();
            configResource = classLoader.getResource(initFileName); // At the root of the resources folder.
            LOGGER.log(Level.INFO, String.format("Reading URL %s (for %s)", configResource, initFileName));
            initConfig = mapper.readValue(configResource.openStream(), Map.class);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, String.format("Error reading URL %s (for %s), trying File System", configResource, initFileName), ex);
            File config = new File(initFileName);
            if (config.exists()) {
                try {
                    configResource = config.toURI().toURL();
                    initConfig = mapper.readValue(configResource.openStream(), Map.class);
                    LOGGER.log(Level.INFO, String.format("Read URL %s (for %s)", configResource, initFileName));
                } catch (Exception ex2) {
                    LOGGER.log(Level.WARNING, String.format("Error reading URL %s (for %s).", configResource, initFileName), ex2);
                    ex2.printStackTrace();
                }
            } else {
                LOGGER.log(Level.SEVERE, String.format("Error: no %s was found.", initFileName));
            }
        }

        ThreeViews thisThing = new ThreeViews();// This one has instantiated the white boards

        thisThing.refreshData(); // Display data the first time.
        thisThing.show();
    }
}
