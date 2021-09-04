package gsg.examples.wb.both;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Using default WhiteBoard Writer,
 * AND THEN a custom one
 * Focus only on the data, not on the display.
 */
public class SwingSample7 {

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
        JOptionPane.showMessageDialog(whiteBoard, "This is sample #7", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public SwingSample7() {
        // The JFrame
        frame = new JFrame("This is example #7");
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
            frameSize = new Dimension(WIDTH, HEIGHT + 50); // 50: ... menu, title bar, etc.
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

        topLabel = new JLabel("Default display (cartesian)");
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);

        frame.setVisible(true); // Display
    }

    public void setLabel(String txt) {
        topLabel.setText(txt);;
    }

    public WhiteBoardPanel getWhiteBoard() {
        return whiteBoard;
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

        SwingSample7 app = new SwingSample7(); // This one has instantiated the white board

        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(0, 0, 0, 255));
        whiteBoard.setWithGrid(false);
        whiteBoard.setBgColor(new Color(250, 250, 250, 255));
        whiteBoard.setGraphicTitle("X not equals Y");
        whiteBoard.setSize(new Dimension(800, 600));
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoard.setGraphicMargins(30);
        whiteBoard.setXEqualsY(false);

//        whiteBoard.setEnforceXAxisAt(5d);
//        whiteBoard.setEnforceYAxisAt(10d);

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

        WhiteBoardPanel.DataSerie dataSerie = new WhiteBoardPanel.DataSerie()
                .data(dataVectors)
                .graphicType(WhiteBoardPanel.GraphicType.LINE)
                .lineThickness(3)
                .color(Color.BLUE);
        whiteBoard.addSerie(dataSerie);

        // Finally, display it.
        whiteBoard.repaint();  // This is for a pure Swing context

        // Wait a bit, change display
        try {
            Thread.sleep(5_000L);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        app.setLabel("Polar display");
        Dimension wbDim = app.getWhiteBoard().getSize();
        app.getWhiteBoard().setWhiteBoardWriter(g2d -> {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, wbDim.width, wbDim.height);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
            int xCenter = wbDim.width / 2;
            int yCenter = wbDim.height / 2;
            int radius = (int)Math.round(Math.min(xCenter, yCenter) * 0.9);
            int KNOB_RADIUS = 10;
            g2d.fillOval(xCenter - KNOB_RADIUS, yCenter - KNOB_RADIUS, 2 * KNOB_RADIUS, 2 * KNOB_RADIUS);
            g2d.drawOval(xCenter - radius, yCenter - radius, 2 * radius, 2 * radius);
            g2d.setColor(Color.BLUE);
            Point previous = null;
            for (int i=0; i<xData.length; i++) {
                int thisPointRadius = (int)Math.round(radius * (double)i/(double)xData.length);
                int x = xCenter + (int)Math.round(thisPointRadius * Math.sin(Math.toRadians(yData[i])));
                int y = yCenter - (int)Math.round(thisPointRadius * Math.cos(Math.toRadians(yData[i])));
                Point thisPoint = new Point(x, y);
                if (previous != null) {
                    g2d.drawLine(previous.x, previous.y, thisPoint.x, thisPoint.y);
                }
                previous = thisPoint;
            }
        });
        app.getWhiteBoard().repaint();

        // Wait again, reset display as it was
        try {
            Thread.sleep(5_000L);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        app.setLabel("Back to cartesian display");
        app.getWhiteBoard().resetDefaultWhiteBoardWriter();
        app.getWhiteBoard().repaint();
    }
}
