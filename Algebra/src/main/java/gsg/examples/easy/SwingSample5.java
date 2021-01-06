package gsg.examples.easy;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gsg.VectorUtils.Vector2D;

/**
 * More Abstraction, using default WhiteBoard Writer
 * Focus only on the data, not on the display.
 */
public class SwingSample5 {

    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private final static int WIDTH = 1024; // 860;
    private final static int HEIGHT = 760; // 600;

    // The WhiteBoard instantiation
    private static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }
    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(whiteBoard, "This is sample #5", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public SwingSample5() {
        // The JFrame
        frame = new JFrame("This is example #5");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
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

        frame.getContentPane().add(new JLabel("This is a full sample"), BorderLayout.NORTH);

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

        new SwingSample5(); // This one has instantiated the white board

        boolean dino121 = true;
        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(125, 0, 255, 255));
        whiteBoard.setWithGrid(true);
        whiteBoard.setBgColor(new Color(250, 250, 250, 255));
        whiteBoard.setGraphicTitle(dino121 ? "121 Dinos" : "A full test");
        whiteBoard.setDimension(new Dimension(/*1024*/ 800, /*760*/ 590)); // TODO Check dimensions. 1024x760 is no correct
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 48));
        whiteBoard.setGraphicMargins(30);

        // Now, the Data
        List<Vector2D> dinoVectors = Arrays.asList( // A dinosaure
                new Vector2D(6, 4),
                new Vector2D(3, 1),
                new Vector2D(1, 2),
                new Vector2D(-1, 5),
                new Vector2D(-2, 5),
                new Vector2D(-3, 4),
                new Vector2D(-4, 4),
                new Vector2D(-5, 3),
                new Vector2D(-5, 2),
                new Vector2D(-2, 2),
                new Vector2D(-5, 1),
                new Vector2D(-4, 0),
                new Vector2D(-2, 1),
                new Vector2D(-1, 0),
                new Vector2D(0, -3),
                new Vector2D(-1, -4),
                new Vector2D(1, -4),
                new Vector2D(2, -3),
                new Vector2D(1, -2),
                new Vector2D(3, -1),
                new Vector2D(5, 1));

        if (true || dino121) {
            // 121 dinos (11 x 11)
            double scale = 0.09;
            List<Vector2D> scaled = dinoVectors.stream().map(v -> VectorUtils.scale(scale, v)).collect(Collectors.toList());
            for (int x=-5; x<=5; x++) {
                for (int y=-5; y<=5; y++) {
                    Vector2D translation = new Vector2D(x, y);
                    List<Vector2D> oneSmallDino = scaled.stream().map(v -> VectorUtils.translate(translation, v)).collect(Collectors.toList());
                    WhiteBoardPanel.DataSerie serie100 = new WhiteBoardPanel.DataSerie()
                            .data(oneSmallDino)
                            .graphicType(WhiteBoardPanel.GraphicType.CLOSED_LINE)
                            .lineThickness(1)
                            .color(Color.BLACK);
                    whiteBoard.addSerie(serie100);
                }
            }
            whiteBoard.repaint();  // This is for a pure Swing context
        }
//        } else {
        if (true) {

            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                System.exit(0);
            }
            dino121 = false;
            whiteBoard.setGraphicTitle(dino121 ? "121 Dinos" : "A full test");
            whiteBoard.resetAllData();
            // Dino, raw, scale 1
            WhiteBoardPanel.DataSerie serieOne = new WhiteBoardPanel.DataSerie()
                    .data(dinoVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.CLOSED_LINE_WITH_DOTS)
                    .circleDiam(8)
                    .lineThickness(2)
                    .color(Color.BLUE);
            whiteBoard.addSerie(serieOne);

            // Dino, rotated
            double rotation = -30d;
            List<Vector2D> rotated = dinoVectors.stream().map(v -> VectorUtils.rotate(Math.toRadians(rotation), v)).collect(Collectors.toList());
            WhiteBoardPanel.DataSerie serieTwo = new WhiteBoardPanel.DataSerie()
                    .data(rotated)
                    .graphicType(WhiteBoardPanel.GraphicType.CLOSED_LINE)
                    .circleDiam(2)
                    .lineThickness(3)
                    .color(Color.RED);
            whiteBoard.addSerie(serieTwo);

            // Dino, scaled
            double scale = 0.25;
            List<Vector2D> scaled = dinoVectors.stream().map(v -> VectorUtils.scale(scale, v)).collect(Collectors.toList());
            WhiteBoardPanel.DataSerie serieThree = new WhiteBoardPanel.DataSerie()
                    .data(scaled)
                    .graphicType(WhiteBoardPanel.GraphicType.CLOSED_LINE)
                    .circleDiam(2)
                    .lineThickness(3)
                    .color(Color.GREEN);
            whiteBoard.addSerie(serieThree);

            // Dino, scaled & translated
            Vector2D translation = new Vector2D(3, 2);
            List<Vector2D> translated = scaled.stream().map(v -> VectorUtils.translate(translation, v)).collect(Collectors.toList());
            WhiteBoardPanel.DataSerie serieThreeBis = new WhiteBoardPanel.DataSerie()
                    .data(translated)
                    .graphicType(WhiteBoardPanel.GraphicType.CLOSED_LINE)
                    .circleDiam(2)
                    .lineThickness(3)
                    .color(Color.GREEN);
            whiteBoard.addSerie(serieThreeBis);

            // A Polynomial function
            List<Vector2D> polynom = new ArrayList<>();
            for (double i = -6; i <= 15; i += 0.25) {
                polynom.add(new Vector2D(i,
                        (0.01 * Math.pow(i, 3)) - (0.1 * Math.pow(i, 2)) - (0.2 * i) + 3));
            }
            WhiteBoardPanel.DataSerie serieFour = new WhiteBoardPanel.DataSerie()
                    .data(polynom)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .circleDiam(2)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoard.addSerie(serieFour);

            // Cloud of points
            List<Vector2D> cloud = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                // x & y in [-10, 10[
                cloud.add(new Vector2D((Math.random() * 20) - 10,
                        (Math.random() * 20) - 10));
            }
            WhiteBoardPanel.DataSerie serieFive = new WhiteBoardPanel.DataSerie()
                    .data(cloud)
                    .graphicType(WhiteBoardPanel.GraphicType.POINTS)
                    .circleDiam(18)
                    .color(new Color(255, 0, 0, 126));
            whiteBoard.addSerie(serieFive);

            whiteBoard.repaint();  // This is for a pure Swing context
        }
        // Finally, display it.
//      whiteBoard.getImage(); // This is for a Notebook
//      whiteBoard.repaint();  // This is for a pure Swing context
    }
}
