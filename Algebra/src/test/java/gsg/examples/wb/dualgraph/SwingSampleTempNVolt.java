package gsg.examples.wb.dualgraph;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;
import lowpass.Filter;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * More Abstraction, using default WhiteBoard Writer
 * You can focus only on the data, not on the display. See the main method.
 * Graph from the info returned by sysinfo.sh (in this project), in a JSON format
 */
public class SwingSampleTempNVolt {

    private final static String TITLE = "Temperature and Voltage";

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;
    private JButton refreshButton = new JButton("Refresh Data");

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;

    // The WhiteBoard instantiation
    private static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(whiteBoard, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    private void refreshData() {

        String fName = "";
        try {
            String name = this.getClass().getName();
            Class cls = Class.forName(name);
            ClassLoader classLoader = cls.getClassLoader();
            URL resource = classLoader.getResource("recording.txt"); // At the root of the resources folder.
            if (resource != null) {
                fName = resource.getFile();
                System.out.println(fName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        // Now, the Data
        List<Double> xs = new ArrayList<>();
        List<Double> tempData = new ArrayList<>();
        List<Double> voltData = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fName));
            String line = "";
            int x = 0;
            while ((line = br.readLine()) != null) {
                try {
                    JSONObject jsonObject = new JSONObject(line);
                    xs.add((double)x++);
                    tempData.add(jsonObject.getDouble("temp"));
                    voltData.add(jsonObject.getDouble("volt"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            // Prepare data for display
            double[] xData = xs.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            double[] tData = tempData.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            List<VectorUtils.Vector2D> dataVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataVectors.add(new VectorUtils.Vector2D(xData[i], tData[i]));
            }

            double[] vData = voltData.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            List<VectorUtils.Vector2D> filteredDataVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                filteredDataVectors.add(new VectorUtils.Vector2D(xData[i], vData[i]));
            }

            whiteBoard.setAxisColor(Color.BLACK);
            whiteBoard.resetAllData();

            // Raw series
            WhiteBoardPanel.DataSerie dataSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoard.addSerie(dataSerie);
            // Filtered series
            WhiteBoardPanel.DataSerie filteredDataSerie = new WhiteBoardPanel.DataSerie()
                    .data(filteredDataVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.RED);
            whiteBoard.addSerie(filteredDataSerie);

            // Finally, display it.
            whiteBoard.repaint();  // This is for a pure Swing context
        } catch (IOException ioe) {
            System.err.println("Bam!");
            ioe.printStackTrace();
        }
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
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
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

        topLabel = new JLabel(TITLE);
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

    public SwingSampleTempNVolt() {
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
        System.out.println(String.format("Running from folder %s", System.getProperty("user.dir")));
        System.out.println(String.format("Java Version %s", System.getProperty("java.version")));
        System.out.println("----------------------------------------------");

        SwingSampleTempNVolt thisThing = new SwingSampleTempNVolt();// This one has instantiated the white board
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
        whiteBoard.setXEqualsY(false);
        // Enforce Y amplitude
        whiteBoard.setForcedMinY(0d);
        whiteBoard.setForcedMaxY(100d);

        thisThing.refreshData();
        thisThing.show();

    }
}
