package gsg.examples.wb.easy;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * More Abstraction, using default WhiteBoard Writer
 * Focus only on the data, not on the display.
 */
public class SwingSample9 {

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;

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
        JOptionPane.showMessageDialog(whiteBoard, "This is sample #9", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public SwingSample9() {
        // The JFrame
        frame = new JFrame("This is example #9");
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
            frameSize = new Dimension(WIDTH, HEIGHT + 60); // 60: ... menu, title bar, etc.
            frame.setSize(frameSize);
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        topLabel = new JLabel("This is a full sample");
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);

        frame.setVisible(true); // Display
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

        new SwingSample9(); // This one has instantiated the white board

        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(125, 0, 255, 255));
        whiteBoard.setWithGrid(false);
        whiteBoard.setBgColor(new Color(250, 250, 250, 255));
        whiteBoard.setGraphicTitle("X not equals Y, Y ampl enforced [0, 20]");
        whiteBoard.setSize(new Dimension(800, 600));
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoard.setGraphicMargins(30);
        whiteBoard.setXEqualsY(false);
        // Enforce Y amplitude
        whiteBoard.setForcedMinY(0d);
        whiteBoard.setForcedMaxY(20d);

        // Now, the Data
        double previousY = 10d;  // Y starts here
        double yAmpl = 20d;      // Y amplitude
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        for (double x=0; x<501; x++) {
            xs.add(x);
            double delta = Math.random() - 0.5;  // [-0.5, 0.5]
            double nextY = previousY + delta;
            if (nextY > yAmpl || nextY < 0) {
                nextY = previousY - delta;
            }
            ys.add(nextY);
            previousY = nextY;
        }

        double[] xData = xs.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        double[] yData = ys.stream()
                .mapToDouble(Double::doubleValue)
                .toArray();

        List<VectorUtils.Vector2D> dataVectors = new ArrayList<>();
        for (int i=0; i<xData.length; i++) {
            dataVectors.add(new VectorUtils.Vector2D(xData[i], yData[i]));
        }

        // As an area, under the curve.
        WhiteBoardPanel.DataSerie dataSerie = new WhiteBoardPanel.DataSerie()
                .data(dataVectors)
                .graphicType(WhiteBoardPanel.GraphicType.AREA)
                .lineThickness(3)
                .color(Color.BLUE)
                .areaGradient(new Color(1f, 0f, 0f, 0.5f), new Color(0f, 0f, 1f, 0.5f));
        whiteBoard.addSerie(dataSerie);

        // Second curve, based on first one, with some random diff
        List<VectorUtils.Vector2D> dataVectors2 = new ArrayList<>();
        for (int i=0; i<xData.length; i++) {
            dataVectors2.add(new VectorUtils.Vector2D(xData[i], (3 + yData[i]) + (Math.random() - 0.5)));
        }
        // As an area, over the curve
        WhiteBoardPanel.DataSerie dataSerie2 = new WhiteBoardPanel.DataSerie()
                .data(dataVectors2)
                .graphicType(WhiteBoardPanel.GraphicType.AREA_SUP)
                .lineThickness(3)
                .color(Color.RED)
                .areaGradient(new Color(0f, 1f, 1f, 0.5f), new Color(0f, 1f, 0f, 0.5f));
        whiteBoard.addSerie(dataSerie2);

        // Finally, display it.
        whiteBoard.repaint();  // This is for a pure Swing context
    }
}
