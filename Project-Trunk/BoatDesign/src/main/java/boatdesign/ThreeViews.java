package boatdesign;

import bezier.Bezier;
import boatdesign.swingstuff.NewDataPanel;
import boatdesign.threeD.BoatBox3D;
import boatdesign.utils.BoatDesignResourceBundle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsg.SwingUtils.SwingUtils;
import gsg.SwingUtils.WhiteBoardPanel;
import gsg.SwingUtils.fullui.ThreeDPanelWithWidgets;
import gsg.VectorUtils;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Comment;
import org.w3c.dom.Text;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A Swing Application. The main.
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

    private final static NumberFormat NUM_FMT = new DecimalFormat("#0.0000");

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
    private double xMaxWidth = -1.0;

    private final static String TITLE = BoatDesignResourceBundle.buildMessage("app-title");

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
    private final JButton refreshButton = new JButton(BoatDesignResourceBundle.buildMessage("refresh-boat-shape"));
    private final JButton stopRefreshButton = new JButton(BoatDesignResourceBundle.buildMessage("stop-refresh"));

    private final JRadioButton jsonRadioButton = new JRadioButton("json");
    private final JRadioButton scadRadioButton = new JRadioButton("scad");

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
    private final WhiteBoardPanel whiteBoardXY; // from above, water-lines
    private final WhiteBoardPanel whiteBoardXZ; // side, buttocks
    private final WhiteBoardPanel whiteBoardYZ; // facing, frames

    private static class UserWBParameters {
        boolean generateImage = false;
        boolean bw = false;
        boolean hideCP = false;
        boolean hideCC = false;
    }

    private VectorUtils.Vector3D centerOfHull = null; // aka Centre de Carene.

    private final BoatBox3D box3D;

    private JTextPane dataTextArea = null;
    private JTextPane boatDataTextArea = null;

    private JCheckBox displayXMLTextArea = null;
    private JTextPane xmlTextArea = null;
    private JTextPane messageTextArea = null;

    private static ThreeViews instance;

    private String boatName;
    private String description;
    private List<String> comments;

    // Parameter form for the whiteboard: generate image, B&W, hide ctrl-points,...)
    private class WhiteBoardOptionPanel extends JPanel {
        JCheckBox generateImage;
        JCheckBox blackAndWhite;
        JCheckBox hideCtrlPoints;
        JCheckBox hideCC;

        public WhiteBoardOptionPanel() {
            super();
            this.setPreferredSize(new Dimension(500, 100));
            this.setLayout(new FlowLayout());

            generateImage = new JCheckBox("Generate Image");
            this.add(generateImage, null);
            blackAndWhite = new JCheckBox("Black & White");
            this.add(blackAndWhite, null);
            hideCtrlPoints = new JCheckBox("Hide Ctrl-Points");
            this.add(hideCtrlPoints, null);
            hideCC = new JCheckBox("Hide CC (Center of Hull)");
            this.add(hideCC, null);
        }

        public boolean isGenerateImageChecked() {
            return (generateImage != null && generateImage.isSelected());
        }
        public boolean isBWChecked() {
            return (blackAndWhite != null && blackAndWhite.isSelected());
        }
        public boolean isHideCtrlPtsChecked() {
            return (hideCtrlPoints != null && hideCtrlPoints.isSelected());
        }
        public boolean isHideCCChecked() {
            return (hideCC != null && hideCC.isSelected());
        }
        public void setGenerateImage(boolean b) {
            if (generateImage != null) {
                generateImage.setSelected(b);
            }
        }
        public void setBW(boolean b) {
            if (blackAndWhite != null) {
                blackAndWhite.setSelected(b);
            }
        }
        public void setHideCP(boolean b) {
            if (hideCtrlPoints != null) {
                hideCtrlPoints.setSelected(b);
            }
        }
        public void setHideCC(boolean b) {
            if (hideCC != null) {
                hideCC.setSelected(b);
            }
        }
    }

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
                "Boat Name",
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
            theMap.put("boat-name", panelData.getBoatName());
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
                this.boatName,
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
            theMap.put("boat-name", panelData.getBoatName());
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
                this.boatName,
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
            theMap.put("boat-name", panelData.getBoatName());
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

    private AtomicBoolean keepLooping = new AtomicBoolean(true);

    private static XMLElement createTextNode(XMLDocument doc, String name, String value) {
        XMLElement xmlElement = (XMLElement) doc.createElement(name);
        Text nodeValue = doc.createTextNode("#text");
        xmlElement.appendChild(nodeValue);
        nodeValue.setNodeValue(value);

        return xmlElement;
    }

    private static void processFrame(XMLDocument doc, XMLElement frameCoordinates, double loa, List<Bezier.Point3D> frameBezier) {
        double x = frameBezier.get(0).getX();
        XMLElement oneFrame = (XMLElement) doc.createElement("frame");
        oneFrame.setAttribute("x", NUM_FMT.format(x + (loa * 100.0 / 2.0)));
        frameCoordinates.appendChild(oneFrame);
        // first: rail, last: keel
        double yRail = frameBezier.get(0).getY();
        double zRail = frameBezier.get(0).getZ();
        double zKeel = frameBezier.get(frameBezier.size() - 1).getZ();
        XMLElement rail = (XMLElement) doc.createElement("rail");
        oneFrame.appendChild(rail);
        rail.appendChild(createTextNode(doc, "y", NUM_FMT.format(yRail)));
        rail.appendChild(createTextNode(doc, "z", NUM_FMT.format(zRail)));

        XMLElement keel = (XMLElement) doc.createElement("keel");
        oneFrame.appendChild(keel);
        keel.appendChild(createTextNode(doc, "z", NUM_FMT.format(zKeel)));
        // Coordinates. Step: buttocks - TODO 10 for now
        for (int w=10; w<yRail; w+=10.0) {
            // Get the value for given w
            String zStrValue = "-";
            Bezier bezier = new Bezier(frameBezier);
            try {
                double t = bezier.getTForGivenY(0, 1E-1, w, 1E-4, false); // false: because => rail to keel
                Bezier.Point3D bezierPoint = bezier.getBezierPoint(t);
                double zValue = bezierPoint.getZ();
                zStrValue = NUM_FMT.format(zValue);
            } catch (Bezier.TooDeepRecursionException e) {
                e.printStackTrace();
            }

            XMLElement frameCoord = createTextNode(doc, "frame-coord-z", zStrValue);
            frameCoord.setAttribute("w", NUM_FMT.format(w));
            oneFrame.appendChild(frameCoord);
        }
    }

    private static XMLDocument buildXMLforPublishing(Map<String, Object> dataMap,
                                                     Map<String, Object> calculatedMap,
                                                     List<List<Bezier.Point3D>> allFramesCtrlPts,
                                                     List<List<Bezier.Point3D>> allBeamsCtrlPts,
                                                     List<Bezier.Point3D> ctrlPointsTransom) {

        getLogger().log(Level.INFO, "Starting XML Calculation...");

        XMLDocument doc = new XMLDocument();
        XMLElement root = (XMLElement) doc.createElement("boat-design");
        doc.appendChild(root);

        XMLElement boatData = (XMLElement) doc.createElement("boat-data");
        root.appendChild(boatData);

        boatData.appendChild(createTextNode(doc, "boat-name", (String)dataMap.get("boat-name")));

        XMLElement comments = (XMLElement) doc.createElement("comments");
        boatData.appendChild(comments);

        List<String> mapComments = (List)dataMap.get("comments");
        mapComments.forEach(mapComment -> {
            comments.appendChild(createTextNode(doc, "comment", mapComment));
        });
        boatData.appendChild(createTextNode(doc, "description", (String)dataMap.get("description")));

        XMLElement defaultPoints = (XMLElement) doc.createElement("ctrl-points");
        boatData.appendChild(defaultPoints);
        // The ctrl points
        Map<String, Object> mapDefaultPoints = (Map)dataMap.get("default-points");
        List<Object> mapKeelPoints = (List)mapDefaultPoints.get("keel");
        mapKeelPoints.forEach(kp -> {
            double x = ((Map<String, Double>)kp).get("x");
            double y = ((Map<String, Double>)kp).get("y");
            double z = ((Map<String, Double>)kp).get("z");
            XMLElement keelPoint = (XMLElement) doc.createElement("keel");
            defaultPoints.appendChild(keelPoint);

            keelPoint.appendChild(createTextNode(doc, "x", String.valueOf(x)));
            keelPoint.appendChild(createTextNode(doc, "y", String.valueOf(y)));
            keelPoint.appendChild(createTextNode(doc, "z", String.valueOf(z)));
        });

        List<Object> mapRailPoints = (List)mapDefaultPoints.get("rail");
        mapRailPoints.forEach(kp -> {
            double x = ((Map<String, Double>)kp).get("x");
            double y = ((Map<String, Double>)kp).get("y");
            double z = ((Map<String, Double>)kp).get("z");
            XMLElement railPoint = (XMLElement) doc.createElement("rail");
            defaultPoints.appendChild(railPoint);

            railPoint.appendChild(createTextNode(doc, "x", String.valueOf(x)));
            railPoint.appendChild(createTextNode(doc, "y", String.valueOf(y)));
            railPoint.appendChild(createTextNode(doc, "z", String.valueOf(z)));
        });

        // TODO Dimensions required ?
        XMLElement dimensions = (XMLElement) doc.createElement("dimensions");
        boatData.appendChild(dimensions);
        // TODO The dimensions (if required)...

        XMLElement calculated = (XMLElement) doc.createElement("calculated");
        boatData.appendChild(calculated);

        XMLElement lengths = (XMLElement) doc.createElement("lengths");
        calculated.appendChild(lengths);

        double loa = (double)((Map<String, Object>)dataMap.get("dimensions")).get("default-lht");
        lengths.appendChild(createTextNode(doc, "loa", NUM_FMT.format(loa)));
        lengths.appendChild(createTextNode(doc, "lwl-start", NUM_FMT.format((double)calculatedMap.get("lwl-start"))));
        lengths.appendChild(createTextNode(doc, "lwl-end", NUM_FMT.format((double)calculatedMap.get("lwl-end"))));
        lengths.appendChild(createTextNode(doc, "lwl", NUM_FMT.format((double)calculatedMap.get("lwl"))));

        XMLElement depths = (XMLElement) doc.createElement("depths");
        calculated.appendChild(depths);

        depths.appendChild(createTextNode(doc, "max-depth", NUM_FMT.format((double)calculatedMap.get("max-depth"))));
        depths.appendChild(createTextNode(doc, "max-depth-x", NUM_FMT.format((double)calculatedMap.get("max-depth-x"))));

        XMLElement widths = (XMLElement) doc.createElement("widths");
        calculated.appendChild(widths);

        widths.appendChild(createTextNode(doc, "max-width", NUM_FMT.format(2d * (double)calculatedMap.get("max-width"))));
        widths.appendChild(createTextNode(doc, "max-width-x", NUM_FMT.format((double)calculatedMap.get("max-width-x"))));

        XMLElement displ = (XMLElement) doc.createElement("D");
        calculated.appendChild(displ);

        displ.appendChild(createTextNode(doc, "displ", NUM_FMT.format((double)calculatedMap.get("displ-m3"))));
        displ.appendChild(createTextNode(doc, "cc-x", NUM_FMT.format((double)calculatedMap.get("cc-x"))));
        displ.appendChild(createTextNode(doc, "cc-z", NUM_FMT.format((double)calculatedMap.get("cc-z"))));

        // Keel and Rail coordinates
        XMLElement keelAndRails = (XMLElement) doc.createElement("keel-and-rails");
        root.appendChild(keelAndRails);
        // Keel
        XMLElement keel = (XMLElement) doc.createElement("keel");
        keelAndRails.appendChild(keel);
        List<Map<String, Double>> keelPoints = (List<Map<String, Double>>)((Map<String, Object>)dataMap.get("default-points")).get("keel");
        List<Bezier.Point3D> keelCtrlPoints = new ArrayList<>();
        keelPoints.forEach(kp ->  keelCtrlPoints.add(new Bezier.Point3D(kp.get("x"), kp.get("y"), kp.get("z"))) );
        Bezier keelBezier = new Bezier(keelCtrlPoints);
        for (int x=0; x<=(loa * 100.0); x+=10) { // in cm
            try {
                double t = keelBezier.getTForGivenX(0, 1E-2, x, 1E-4);
                Bezier.Point3D keelPoint = keelBezier.getBezierPoint(t);
                XMLElement keelElement = createTextNode(doc, "keel", NUM_FMT.format(keelPoint.getZ()));
                keelElement.setAttribute("x", String.valueOf(x));
                keel.appendChild(keelElement);
            } catch (Bezier.TooDeepRecursionException tdre) {
                tdre.printStackTrace();
            }
        }
        // Rail
        XMLElement rail = (XMLElement) doc.createElement("rail");
        keelAndRails.appendChild(rail);
        List<Map<String, Double>> railPoints = (List<Map<String, Double>>)((Map<String, Object>)dataMap.get("default-points")).get("rail");
        List<Bezier.Point3D> railCtrlPoints = new ArrayList<>();
        railPoints.forEach(kp ->  railCtrlPoints.add(new Bezier.Point3D(kp.get("x"), kp.get("y"), kp.get("z"))) );
        Bezier railBezier = new Bezier(railCtrlPoints);
        for (int x=0; x<=(loa * 100.0); x+=10) { // in cm
            try {
                double t = railBezier.getTForGivenX(0, 1E-2, x, 1E-4);
                Bezier.Point3D railPoint = railBezier.getBezierPoint(t);
                XMLElement railElement = (XMLElement) doc.createElement("rail");
                railElement.setAttribute("x", String.valueOf(x));
                rail.appendChild(railElement);

                railElement.appendChild(createTextNode(doc, "y", NUM_FMT.format(railPoint.getY())));
                railElement.appendChild(createTextNode(doc, "z", NUM_FMT.format(railPoint.getZ())));
            } catch (Bezier.TooDeepRecursionException tdre) {
                tdre.printStackTrace();
            }
        }

        // Drawings
        XMLElement drawings = (XMLElement) doc.createElement("drawings");
        root.appendChild(drawings);

        drawings.appendChild(createTextNode(doc, "water-lines", "../XY.png")); // TODO Real image names
        XMLElement wlCoordinates = (XMLElement) doc.createElement("wl-coordinates");
        drawings.appendChild(wlCoordinates);
        // TODO The coordinates
        Comment commentWL = doc.createComment("TODO, water-lines");
        wlCoordinates.appendChild(commentWL);

        drawings.appendChild(createTextNode(doc, "buttocks", "../XZ.png"));    // TODO Real image names
        XMLElement buttocksCoordinates = (XMLElement) doc.createElement("buttocks-coordinates");
        drawings.appendChild(buttocksCoordinates);
        // TODO The coordinates
        Comment commentB = doc.createComment("TODO, buttocks");
        buttocksCoordinates.appendChild(commentB);

        drawings.appendChild(createTextNode(doc, "frames", "../YZ.png"));      // TODO Real image names
        // Coordinates
        XMLElement frameCoordinates = (XMLElement) doc.createElement("frame-coordinates");
        drawings.appendChild(frameCoordinates);
        allFramesCtrlPts.forEach(frameBezier -> {
            processFrame(doc, frameCoordinates, loa, frameBezier);
        });
        // Transom
        processFrame(doc, frameCoordinates, loa, ctrlPointsTransom);

        getLogger().log(Level.INFO, "Done with XML Calculation!");

        return doc;
    }

    private void refreshBoatShape() {
        Thread repainter = new Thread(() -> {
            keepLooping.set(true);
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

            if (((UserWBParameters)whiteBoardXZ.getUserParameters()).generateImage) {
                System.out.println("Generating image, as requested");
                // Store XZ panel as an image
                File imageFile = new File("XZ.png");
                this.whiteBoardXZ.createImage(imageFile, "png", this.whiteBoardXZ.getWidth(), this.whiteBoardXZ.getHeight());
                System.out.printf("See file %s\n", imageFile.getAbsolutePath());
            } else {
                System.out.println("Skipping XZ image generation");
            }
            if (((UserWBParameters)whiteBoardXY.getUserParameters()).generateImage) {
                System.out.println("Generating image, as requested");
                // Store XY panel as an image
                File imageFile = new File("XY.png");
                this.whiteBoardXY.createImage(imageFile, "png", this.whiteBoardXY.getWidth(), this.whiteBoardXY.getHeight());
                System.out.printf("See file %s\n", imageFile.getAbsolutePath());
            } else {
                System.out.println("Skipping XY image generation");
            }
            if (((UserWBParameters)whiteBoardYZ.getUserParameters()).generateImage) {
                System.out.println("Generating image, as requested");
                // Store XY panel as an image
                File imageFile = new File("YZ.png");
                this.whiteBoardYZ.createImage(imageFile, "png", this.whiteBoardYZ.getWidth(), this.whiteBoardYZ.getHeight());
                System.out.printf("See file %s\n", imageFile.getAbsolutePath());
            } else {
                System.out.println("Skipping YZ image generation");
            }

        });
        repainter.start();

        Thread refresher = new Thread(() -> {
            getLogger().log(Level.INFO, "Starting refresh...");
            refreshButton.setEnabled(false);
            stopRefreshButton.setEnabled(true);
            boatDataTextArea.setText("Re-calculating...");
            // TODO Stop thread if already running.

            if (true) {
                this.whiteBoardXY.resetAllData();
                this.whiteBoardXZ.resetAllData();
                this.whiteBoardYZ.resetAllData();

                try {
                    // this.initConfiguration(true);
                    this.reLoadConfig(false); // False, do not read from the file!
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            this.whiteBoardXY.repaint();
            this.whiteBoardXZ.repaint();
            this.whiteBoardYZ.repaint();

            this.box3D.refreshData(false, map -> {
                String json;
                try {
                    json = mapper.writerWithDefaultPrettyPrinter()
                                 .writeValueAsString(map);
                } catch (JsonProcessingException jpe) {
                    json = map.toString();
                }
                boatDataTextArea.setText(json);

                // XML for XSL-FO publishing
                if (displayXMLTextArea.isSelected()) {
                    List<List<Bezier.Point3D>> allFramesCtrlPts = this.box3D.getFrameCtrlPts();
                    List<List<Bezier.Point3D>> allBeamsCtrlPts = this.box3D.getBeamCtrlPts();
                    List<Bezier.Point3D> ctrlPointsTransom = this.box3D.getTransomCtrlPoint();
                    XMLDocument doc = ThreeViews.buildXMLforPublishing(initConfig,
                            map,
                            allFramesCtrlPts,
                            allBeamsCtrlPts,
                            ctrlPointsTransom);
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        doc.print(baos);
                        String xmlString = baos.toString();
//                    System.out.println(xmlString);
                        // TODO Do we need another callback here (in refreshData) ?
                        xmlTextArea.setText(xmlString);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (map.get("lwl-start") != null) {
                    // CC position
                    Double xCC = (Double) map.get("cc-x");
                    Double zCC = (Double) map.get("cc-z");
                    if (xCC != null && zCC != null && xCC != -0.01 && zCC != -0.01) {
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
                        // TODO Remove the area curve if it exists
                        if (displacementMap.size() > 0 && max.get() != -Double.MAX_VALUE) {
                            double ratio = (wbMaxY / max.get());
                            List<VectorUtils.Vector2D> areas = new ArrayList<>();
                            areas.add(new VectorUtils.Vector2D().x(lwlStart * 1e2).y(0d));
                            displacementMap.forEach((k, v) -> areas.add(new VectorUtils.Vector2D().x(k).y(v * ratio)));
                            areas.add(new VectorUtils.Vector2D().x(lwlEnd * 1e2).y(0d));

                            WhiteBoardPanel.DataSerie areasSerie = new WhiteBoardPanel.DataSerie()
                                    .data(areas)
                                    .graphicType(WhiteBoardPanel.GraphicType.AREA)
                                    .areaGradient(new Color(255, 0, 0, 128), new Color(255, 165, 0, 128))
                                    .color(Color.BLACK);
                            if (!((UserWBParameters)whiteBoardXY.getUserParameters()).hideCC) {
                                whiteBoardXY.addSerie(areasSerie);
                            }
                        }
                    }
                }


            }, mess -> {
                messageTextArea.setText(mess);
            }, map -> {
                if (map != null) {
                    if (map.get(BoatBox3D.TYPE).equals(BoatBox3D.FRAME)) {
                        List<VectorUtils.Vector3D> data = (List)map.get(BoatBox3D.DATA); // TODO Make it a Bezier.Point3D ?
                        List<VectorUtils.Vector2D> framePtsYZVectors = new ArrayList<>();
                        // Find x position of the max width. 'data' contains the points. Rail to Keel.
                        data.forEach(pt -> {
                            // Transom sometime goes on the wrong side...
                            boolean right = pt.getX() < (this.xMaxWidth - (this.defaultLHT / 2));
                            framePtsYZVectors.add(new VectorUtils.Vector2D(right ? pt.getY() : -pt.getY(), pt.getZ()));
                        });
                        // TODO get here the required coordinates for the frame.
                        // With X, rail value, keel value, points of the frame (Y & Z), with step on Y.
                        // Keel & Rail ctrl points in initConfig.default-points

                        WhiteBoardPanel.DataSerie frameYZSerie = new WhiteBoardPanel.DataSerie()
                                .data(framePtsYZVectors)
                                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                                .lineThickness(1)
                                .color(Color.RED);
                        whiteBoardYZ.addSerie(frameYZSerie, ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw ? Color.BLACK : null);
                        whiteBoardYZ.repaint();
                    } else if (map.get(BoatBox3D.TYPE).equals(BoatBox3D.WATERLINE)) {
                        List<Bezier.Point3D> data = (List)map.get(BoatBox3D.DATA);
                        List<VectorUtils.Vector2D> waterlinePtsXYVectors = new ArrayList<>();
                        data.forEach(pt -> {
                            waterlinePtsXYVectors.add(new VectorUtils.Vector2D(pt.getX() + (this.defaultLHT / 2), pt.getY()));
                        });
                        WhiteBoardPanel.DataSerie frameXYSerie = new WhiteBoardPanel.DataSerie()
                                .data(waterlinePtsXYVectors)
                                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                                .lineThickness(1)
                                .color(Color.RED);
                        whiteBoardXY.addSerie(frameXYSerie, ((UserWBParameters)whiteBoardXY.getUserParameters()).bw ? Color.BLACK : null);
                        whiteBoardXY.repaint();
                    } else if (map.get(BoatBox3D.TYPE).equals(BoatBox3D.BUTTOCK)) {
                        List<Bezier.Point3D> data = (List)map.get(BoatBox3D.DATA);
                        List<VectorUtils.Vector2D> buttockPtsXZVectors = new ArrayList<>();
                        data.forEach(pt -> {
                            buttockPtsXZVectors.add(new VectorUtils.Vector2D(pt.getX() + (this.defaultLHT / 2), pt.getZ()));
                        });
                        WhiteBoardPanel.DataSerie buttockXZSerie = new WhiteBoardPanel.DataSerie()
                                .data(buttockPtsXZVectors)
                                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                                .lineThickness(1)
                                .color(Color.RED);
                        whiteBoardXZ.addSerie(buttockXZSerie, ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : null);
                        whiteBoardXZ.repaint();
                    } else {
                        // TODO Others, BEAM
                    }
                }
            });
            // Stop re-painter
            keepLooping.set(false);
            getLogger().log(Level.INFO, "Refresh completed!");
            refreshButton.setEnabled(true);
            stopRefreshButton.setEnabled(false);
            this.box3D.repaint();
            if (centerOfHull != null) {
                // XZ
                List<VectorUtils.Vector2D> xzCC = List.of(new VectorUtils.Vector2D(centerOfHull.getX(), centerOfHull.getZ()));
                WhiteBoardPanel.DataSerie ccXZSerie = new WhiteBoardPanel.DataSerie()
                        .data(xzCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                if (!((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCC) {
                    whiteBoardXZ.addSerie(ccXZSerie);
                }

                if (!((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCC) {
                    WhiteBoardPanel.TextSerie ccXZTextSerie = new WhiteBoardPanel.TextSerie(xzCC.get(0), "CC", 0, 6, WhiteBoardPanel.TextSerie.Justification.CENTER);
                    ccXZTextSerie.setTextColor(new Color(0, 102, 0, 200));
                    ccXZTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                    whiteBoardXZ.resetAllText();
                    whiteBoardXZ.addTextSerie(ccXZTextSerie);
                    whiteBoardXZ.repaint();
                }
                // YZ
                List<VectorUtils.Vector2D> yzCC = List.of(new VectorUtils.Vector2D(centerOfHull.getY(), centerOfHull.getZ()));
                WhiteBoardPanel.DataSerie ccYZSerie = new WhiteBoardPanel.DataSerie()
                        .data(yzCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                if (!((UserWBParameters)whiteBoardYZ.getUserParameters()).hideCC) {
                    whiteBoardYZ.addSerie(ccYZSerie);
                    WhiteBoardPanel.TextSerie ccYZTextSerie = new WhiteBoardPanel.TextSerie(yzCC.get(0), "CC", 7, -6, WhiteBoardPanel.TextSerie.Justification.LEFT);
                    ccYZTextSerie.setTextColor(new Color(0, 102, 0, 200));
                    ccYZTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                    whiteBoardYZ.resetAllText();
                    whiteBoardYZ.addTextSerie(ccYZTextSerie);
                }
                whiteBoardYZ.repaint();
                // XY
                List<VectorUtils.Vector2D> xyCC = List.of(new VectorUtils.Vector2D(centerOfHull.getX(), centerOfHull.getY()));
                WhiteBoardPanel.DataSerie ccXYSerie = new WhiteBoardPanel.DataSerie()
                        .data(xyCC)
                        .graphicType(WhiteBoardPanel.GraphicType.POINTS)
//                        .circleDiam(6)
                        .color(new Color(0, 102, 0, 200));
                if (!((UserWBParameters)whiteBoardXY.getUserParameters()).hideCC) {
                    whiteBoardXY.addSerie(ccXYSerie);
                    WhiteBoardPanel.TextSerie ccXYTextSerie = new WhiteBoardPanel.TextSerie(xyCC.get(0), "CC", 0, -14, WhiteBoardPanel.TextSerie.Justification.CENTER);
                    ccXYTextSerie.setTextColor(new Color(0, 102, 0, 200));
                    ccXYTextSerie.setFont(new Font("Courier", Font.BOLD, 12));
                    whiteBoardXY.resetAllText();
                    whiteBoardXY.addTextSerie(ccXYTextSerie);
                }
                whiteBoardXY.repaint();
            }
        });
        refresher.start();
    }

    private void killRefreshBoatShape() {
        keepLooping.set(false);
        this.box3D.setWorking(false);
    }

    private Map<String, Object> generateBezierJson() {
        return Map.of("rail", railCtrlPoints, "keel", keelCtrlPoints);
    }

    private String generateBezierScad() {
        List<List<Double>> railList = railCtrlPoints.stream().map(pt -> List.of(pt.getX(), pt.getY(), pt.getZ())).collect(Collectors.toList());
        List<List<Double>> keelList = keelCtrlPoints.stream().map(pt -> List.of(pt.getX(), pt.getY(), pt.getZ())).collect(Collectors.toList());

        String collectRail = railList.stream()
                .map(pt -> String.format("[ %s ]", pt.stream().map(coord -> String.format("%f", coord)).collect(Collectors.joining(", "))))
                .collect(Collectors.joining(", "));
        String collectKeel = keelList.stream()
                .map(pt -> String.format("[ %s ]", pt.stream().map(coord -> String.format("%f", coord)).collect(Collectors.joining(", "))))
                .collect(Collectors.joining(", "));

        return String.format("extVolume = %s;\nrail = %s;\nkeel = %s;\n",
                    String.format("[ %f, %f, %f ]", this.box3D.getDefaultLHT(), 0d, 0d),
                    String.format("[ %s ]", collectRail),
                    String.format("[ %s ]", collectKeel)
        );
    }

    private void setBezierData() {
        // Display in textArea
        try {
            if (jsonRadioButton.isSelected()) {
                String json = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(generateBezierJson());
                dataTextArea.setText(json);
            } else if (scadRadioButton.isSelected()) {
                String scad = generateBezierScad();
                dataTextArea.setText(scad);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void refreshData() {

        if (railCtrlPoints.size() > 0 && keelCtrlPoints.size() > 0) {

            // Tell the 3D box
            this.box3D.setRailCtrlPoints(railCtrlPoints); // The rail.
            this.box3D.setKeelCtrlPoints(keelCtrlPoints); // The keel.

            // Display in textArea
            setBezierData();

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
            List<VectorUtils.Vector2D> railDataYZVectorsOther = new ArrayList<>();
            double maxWidth = -1.0;
            this.xMaxWidth = -1d;
            for (int i = 0; i < yData.length; i++) {
                if (yData[i] > maxWidth) {
                    this.xMaxWidth = xData[i];
                    maxWidth = yData[i];
                }
                railDataYZVectors.add(new VectorUtils.Vector2D(yData[i], zData[i]));
                railDataYZVectorsOther.add(new VectorUtils.Vector2D(-yData[i], zData[i]));
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
            if (!((UserWBParameters)whiteBoardXY.getUserParameters()).hideCP) {
                whiteBoardXY.addSerie(railCtrlXYSerie, ((UserWBParameters)whiteBoardXY.getUserParameters()).bw ? Color.BLACK : null);
            }
            // XY - Keel
            WhiteBoardPanel.DataSerie keelCtrlXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            if (!((UserWBParameters)whiteBoardXY.getUserParameters()).hideCP) {
                whiteBoardXY.addSerie(keelCtrlXYSerie, ((UserWBParameters)whiteBoardXY.getUserParameters()).bw ? Color.BLACK : null);
            }

            // XZ - Rail
            WhiteBoardPanel.DataSerie railCtrlXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railCtrlPtsXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            if (!((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCP) {
                whiteBoardXZ.addSerie(railCtrlXZSerie, ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : null);
            }
            // XZ - Keel
            WhiteBoardPanel.DataSerie keelCtrlXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            if (!((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCP) {
                whiteBoardXZ.addSerie(keelCtrlXZSerie, ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : null);
            }

            // YZ - Rail
            WhiteBoardPanel.DataSerie railCtrlYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railCtrlPtsYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            if (!((UserWBParameters)whiteBoardYZ.getUserParameters()).hideCP) {
                whiteBoardYZ.addSerie(railCtrlYZSerie);
            }
            // YZ - Keel
            WhiteBoardPanel.DataSerie keelCtrlYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelCtrlPtsYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE_WITH_DOTS)
                    .lineThickness(1)
                    .color(Color.ORANGE);
            if (!((UserWBParameters)whiteBoardYZ.getUserParameters()).hideCC) {
                whiteBoardYZ.addSerie(keelCtrlYZSerie, ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw ? Color.BLACK : null);
            }
            // Bezier points series
            // XY - Rail
            WhiteBoardPanel.DataSerie railDataXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXY.addSerie(railDataXYSerie, ((UserWBParameters)whiteBoardXY.getUserParameters()).bw ? Color.BLACK : null);
            // XY - Keel
            WhiteBoardPanel.DataSerie keelDataXYSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataXYVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXY.addSerie(keelDataXYSerie, ((UserWBParameters)whiteBoardXY.getUserParameters()).bw ? Color.BLACK : null);

            // XZ - Rail
            WhiteBoardPanel.DataSerie railDataXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardXZ.addSerie(railDataXZSerie, ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : null);
            // XZ - Keel
            WhiteBoardPanel.DataSerie keelDataXZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataXZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE); // ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : Color.BLUE);
            whiteBoardXZ.addSerie(keelDataXZSerie, ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw ? Color.BLACK : null);

            // YZ - Rail
            WhiteBoardPanel.DataSerie railDataYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(railDataYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(railDataYZSerie, ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw ? Color.BLACK : null);
            // Other side (on the left of the axis)
            WhiteBoardPanel.DataSerie railDataYZSerieOther = new WhiteBoardPanel.DataSerie()
                    .data(railDataYZVectorsOther)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(railDataYZSerieOther, ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw ? Color.BLACK : null);

            // YZ - Keel
            WhiteBoardPanel.DataSerie keelDataYZSerie = new WhiteBoardPanel.DataSerie()
                    .data(keelDataYZVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoardYZ.addSerie(keelDataYZSerie, ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw ? Color.BLACK : null);

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
            this.boatName = (String) initConfig.get("boat-name");
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

        // XY - From above
        whiteBoardXY.setAxisColor(Color.BLACK);
        whiteBoardXY.setGridColor(Color.GRAY);
        whiteBoardXY.setForceTickIncrement(50);
        whiteBoardXY.setEnforceXAxisAt(0d);
        whiteBoardXY.setEnforceYAxisAt(0d);

        whiteBoardXY.setWithGrid(true);
        whiteBoardXY.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXY.setGraphicTitle(BoatDesignResourceBundle.buildMessage("water-lines")); // XY - Top"); // "X not equals Y, Y ampl enforced [0, 100]");
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
        // XZ - Side
        whiteBoardXZ.setAxisColor(Color.BLACK);
        whiteBoardXZ.setGridColor(Color.GRAY);
        whiteBoardXZ.setForceTickIncrement(50);
        whiteBoardXZ.setEnforceXAxisAt(0d);
        whiteBoardXZ.setEnforceYAxisAt(0d);

        whiteBoardXZ.setWithGrid(true);
        whiteBoardXZ.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardXZ.setGraphicTitle(BoatDesignResourceBundle.buildMessage("buttocks")); // XZ - Side"); // "X not equals Y, Y ampl enforced [0, 100]");
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

        // YZ - Facing
        whiteBoardYZ.setAxisColor(Color.BLACK);
        whiteBoardYZ.setGridColor(Color.GRAY);
        whiteBoardYZ.setForceTickIncrement(50);
        whiteBoardYZ.setEnforceXAxisAt(0d);
        whiteBoardYZ.setEnforceYAxisAt(0d);

        whiteBoardYZ.setWithGrid(true);
        whiteBoardYZ.setBgColor(new Color(250, 250, 250, 255));
        whiteBoardYZ.setGraphicTitle(BoatDesignResourceBundle.buildMessage("frames")); // YZ - Face"); // "X not equals Y, Y ampl enforced [0, 100]");
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
                int mask = e.getModifiersEx();
                if (SwingUtilities.isRightMouseButton(e) && ((mask & MouseEvent.SHIFT_DOWN_MASK) != 0)) { // Shift + Right-click
                    // Dialog for options (generate image, B&W, no ctrl-points,...)
                    System.out.println(">> Shift + Right click ?");
                    WhiteBoardOptionPanel wbop = new WhiteBoardOptionPanel();
                    UserWBParameters userPrms = (UserWBParameters)whiteBoardXY.getUserParameters();
                    wbop.setGenerateImage(userPrms.generateImage);
                    wbop.setBW(userPrms.bw);
                    wbop.setHideCP(userPrms.hideCP);
                    wbop.setHideCC(userPrms.hideCC);

                    int response = JOptionPane.showConfirmDialog(whiteBoardXY, wbop, "WhiteBoard Parameters - XY", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (response == JOptionPane.OK_OPTION) {
                        System.out.println("OK!");
//                        System.out.printf("Generate Image: %s\n", wbop.isGenerateImageChecked() ? "yes" : "no");
                        ((UserWBParameters)whiteBoardXY.getUserParameters()).generateImage = wbop.isGenerateImageChecked();
                        ((UserWBParameters)whiteBoardXY.getUserParameters()).bw = wbop.isBWChecked();
                        ((UserWBParameters)whiteBoardXY.getUserParameters()).hideCP = wbop.isHideCtrlPtsChecked();
                        ((UserWBParameters)whiteBoardXY.getUserParameters()).hideCC = wbop.isHideCCChecked();
                    } else {
                        System.out.println("Canceled.");
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
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
                    whiteBoardXY.setToolTipText(String.format("<html>X: %.02f<br/>Z: %.02f<br/><p>Use Shift + Right-Click<br/>for parameters</p></html>",
                            whiteBoardMousePos.getX(), whiteBoardMousePos.getY()));
                }
            }
        });

        whiteBoardXZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int mask = e.getModifiersEx();
//                getLogger().log(Level.INFO, "Click on whiteboard");
                if (SwingUtilities.isRightMouseButton(e) && ((mask & MouseEvent.SHIFT_DOWN_MASK) != 0)) { // Shift + Right-click
                    // Dialog for options (generate image, B&W, no ctrl-points,...)
                    System.out.println(">> Shift + Right click ?");
                    WhiteBoardOptionPanel wbop = new WhiteBoardOptionPanel();
                    UserWBParameters userPrms = (UserWBParameters)whiteBoardXZ.getUserParameters();
                    wbop.setGenerateImage(userPrms.generateImage);
                    wbop.setBW(userPrms.bw);
                    wbop.setHideCP(userPrms.hideCP);
                    wbop.setHideCC(userPrms.hideCC);

                    int response = JOptionPane.showConfirmDialog(whiteBoardXZ, wbop, "WhiteBoard Parameters - XZ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (response == JOptionPane.OK_OPTION) {
                        System.out.println("OK!");
//                        System.out.printf("Generate Image: %s\n", wbop.isGenerateImageChecked() ? "yes" : "no");
                        ((UserWBParameters)whiteBoardXZ.getUserParameters()).generateImage = wbop.isGenerateImageChecked();
                        ((UserWBParameters)whiteBoardXZ.getUserParameters()).bw = wbop.isBWChecked();
                        ((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCP = wbop.isHideCtrlPtsChecked();
                        ((UserWBParameters)whiteBoardXZ.getUserParameters()).hideCC = wbop.isHideCCChecked();
                    } else {
                        System.out.println("Canceled.");
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
                    System.out.println(">> Right click ?");
                    Bezier.Point3D closePoint = getClosePoint(e, whiteBoardXZ, Orientation.XZ);
                    if (closePoint != null) {
                        BezierPopup popup = new BezierPopup(instance, closePoint);
                        popup.show(whiteBoardXZ, e.getX(), e.getY());
                    } else {
                        System.out.println("Dummy right-click. No point close to it.");
                    }
                } else {
                    // Regular click.
                    System.out.println(">> Regular click ?");
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
                    whiteBoardXZ.setToolTipText(String.format("<html>X: %.02f<br/>Z: %.02f<br/><p>Use Shift + Right-Click<br/>for parameters</p></html>",
                            whiteBoardMousePos.getX(), whiteBoardMousePos.getY()));
                }
            }
        });

        whiteBoardYZ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
//                getLogger().log(Level.INFO, "Click on whiteboard");
                int mask = e.getModifiersEx();
                if (SwingUtilities.isRightMouseButton(e) && ((mask & MouseEvent.SHIFT_DOWN_MASK) != 0)) { // Shift + Right-click
                    // Dialog for options (generate image, B&W, no ctrl-points,...)
                    System.out.println(">> Shift + Right click ?");
                    WhiteBoardOptionPanel wbop = new WhiteBoardOptionPanel();
                    UserWBParameters userPrms = (UserWBParameters)whiteBoardYZ.getUserParameters();
                    wbop.setGenerateImage(userPrms.generateImage);
                    wbop.setBW(userPrms.bw);
                    wbop.setHideCP(userPrms.hideCP);
                    wbop.setHideCC(userPrms.hideCC);

                    int response = JOptionPane.showConfirmDialog(whiteBoardYZ, wbop, "WhiteBoard Parameters - YZ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (response == JOptionPane.OK_OPTION) {
                        System.out.println("OK!");
//                        System.out.printf("Generate Image: %s\n", wbop.isGenerateImageChecked() ? "yes" : "no");
                        ((UserWBParameters)whiteBoardYZ.getUserParameters()).generateImage = wbop.isGenerateImageChecked();
                        ((UserWBParameters)whiteBoardYZ.getUserParameters()).bw = wbop.isBWChecked();
                        ((UserWBParameters)whiteBoardYZ.getUserParameters()).hideCP = wbop.isHideCtrlPtsChecked();
                        ((UserWBParameters)whiteBoardYZ.getUserParameters()).hideCC = wbop.isHideCCChecked();
                    } else {
                        System.out.println("Canceled.");
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) { // e.isPopupTrigger()) { // Right-click
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
//              System.out.printf("Mouse clicked x: %d y: %d\n", e.getX(), e.getY());
                Bezier.Point3D closePoint = getClosePoint(e, whiteBoardYZ, Orientation.YZ);
                if (closePoint != null) {
//                  getLogger().log(Level.INFO, "Found it!");
                    whiteBoardYZ.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                    whiteBoardYZ.setToolTipText(String.format("<html>X: %.02f<br/>Z: %.02f<br/><p>Use Shift + Right-Click<br/>for parameters</p></html>",
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
        stopRefreshButton.addActionListener(e -> killRefreshBoatShape());

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
        ctrlPointsPanel.setBorder(BorderFactory.createTitledBorder("Bezier Ctrl Points"));
        dataTextArea = new JTextPane();
        dataTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.setPreferredSize(new Dimension(300, 225));
        // dataScrollPane.setSize(new Dimension(300, 250));
        ctrlPointsPanel.add(dataScrollPane, BorderLayout.CENTER);

        ButtonGroup langOptionGroup = new ButtonGroup();
        jsonRadioButton.setSelected(true);
        langOptionGroup.add(jsonRadioButton);
        jsonRadioButton.addActionListener(e -> {
            setBezierData();
        });
        scadRadioButton.setSelected(false);
        scadRadioButton.addActionListener(e -> {
            setBezierData();
        });
        langOptionGroup.add(scadRadioButton);
        JPanel radioButtonPanel = new JPanel(new BorderLayout());
        radioButtonPanel.add(jsonRadioButton, BorderLayout.WEST);
        radioButtonPanel.add(scadRadioButton, BorderLayout.CENTER);
        ctrlPointsPanel.add(radioButtonPanel, BorderLayout.NORTH);

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
//                        double val = Double.parseDouble(frameStepValue.getText());
                        Object value = frameStepValue.getValue();
                        double val = (value instanceof Long) ? (long)(value) : (double)(value);
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
//                        double val = Double.parseDouble(wlStepValue.getText());
                        Object value = wlStepValue.getValue();
                        double val = (value instanceof Long) ? (long)(value) : (double)(value);

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
//                        double val = Double.parseDouble(buttockStepValue.getText());
                        Object value = buttockStepValue.getValue();
                        double val = (value instanceof Long) ? (long)(value) : (double)(value);
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
        boatDataScrollPane.setPreferredSize(new Dimension(300, 175));
        boatDataPanel.add(boatDataScrollPane, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        messageTextArea = new JTextPane();
        messageTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        messageTextArea.setText("Messages...");
        JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
        messageScrollPane.setPreferredSize(new Dimension(300, 50));
        messagePanel.add(messageScrollPane, BorderLayout.NORTH);

        bottomRightPanel.add(messagePanel,
                new GridBagConstraints(1,
                        0,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
        bottomRightPanel.add(boatDataPanel,
                new GridBagConstraints(1,
                        1,
                        1,
                        1,
                        1.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        // Add XML Panel. TODO: define frame step, waterline step, etc... Here, or somewhere else.
        JPanel xmlPanel = new JPanel(new BorderLayout());
        xmlPanel.setBorder(BorderFactory.createTitledBorder("XML for Publication"));

        displayXMLTextArea = new JCheckBox("Calculate XML for publishing");
        xmlPanel.add(displayXMLTextArea, BorderLayout.NORTH);
        displayXMLTextArea.setSelected(false); // Uncheck by default
        displayXMLTextArea.addChangeListener(evt -> {
            boolean checked = ((JCheckBox) evt.getSource()).isSelected();
            if (checked) {
                System.out.println("Will calculate");
            } else {
                System.out.println("Will NOT calculate");
            }
        });

        xmlTextArea = new JTextPane();
        xmlTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        JScrollPane xmlDataScrollPane = new JScrollPane(xmlTextArea);
        xmlDataScrollPane.setPreferredSize(new Dimension(300, 225));

        // dataScrollPane.setSize(new Dimension(300, 250));
        xmlPanel.add(xmlDataScrollPane, BorderLayout.CENTER);

        bottomRightPanel.add(xmlPanel,
                new GridBagConstraints(0,
                        2,
                        2,
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
        JPanel smallButtonPanel = new JPanel(); // new BorderLayout());
        smallButtonPanel.add(refreshButton); // , BorderLayout.WEST);
        smallButtonPanel.add(stopRefreshButton); // , BorderLayout.WEST);
        stopRefreshButton.setEnabled(false);

        bottomPanel.add(smallButtonPanel, new GridBagConstraints(0,
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
        this.whiteBoardXY.setUserParameters(new UserWBParameters());

        this.whiteBoardXZ = new WhiteBoardPanel(); // side
        this.whiteBoardXZ.setUserParameters(new UserWBParameters());

        this.whiteBoardYZ = new WhiteBoardPanel(); // facing
        this.whiteBoardYZ.setUserParameters(new UserWBParameters());

        this.initConfiguration(true);
        /*BoatBox3D*/
        this.box3D = new BoatBox3D(minX, maxX, minY, maxY, minZ, maxZ, defaultLHT, this);
        threeDPanel = new ThreeDPanelWithWidgets(box3D);
        this.initComponents();
    }

    public static Logger getLogger() {
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
            Object value = this.xValue.getValue();
            return (value instanceof Long) ? (long)value : (double)value;
        }

        public double getYValue() {
            Object value = this.yValue.getValue();
            return (value instanceof Long) ? (long)value : (double)value;
        }

        public double getZValue() {
            Object value = this.zValue.getValue();
            return (value instanceof Long) ? (long)value : (double)value;
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

    /**
     * Main for the Swing app.
     * @param args Unused.
     */
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
