package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sample02 {
    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;

    // The Box instantiation
    private static Box3D box3D;

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(box3D, "This box is sample #2", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public Sample02() {
        // The JFrame
        frame = new JFrame("This is example #2");
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

        frame.getContentPane().add(new JLabel("Spinning sample"), BorderLayout.NORTH);

        // >> HERE: Add the box to the JFrame
        box3D = new Box3D();
        box3D.setyMax(5d);
        box3D.setZoom(0.75d);

        // Do something specific here, after the box drawing.
        Consumer<Graphics2D> afterDrawer = g2d -> {
            // Draw a Pyramid
            // 1 - Define its vertex
            VectorUtils.Vector3D topV3 = new VectorUtils.Vector3D(1, -0.5, 2);
            VectorUtils.Vector3D bottomLeftV3 = new VectorUtils.Vector3D(-2, 2, -2);
            VectorUtils.Vector3D bottomRightV3 = new VectorUtils.Vector3D(2, 2.3, -2.1);
            VectorUtils.Vector3D bottomBackV3 = new VectorUtils.Vector3D(2, -2, -2.0);
            // Rotate them
            VectorUtils.Vector3D rotatedTop = VectorUtils.rotate(topV3,
                    Math.toRadians(box3D.getRotOnX()),
                    Math.toRadians(box3D.getRotOnY()),
                    Math.toRadians(box3D.getRotOnZ()));
            VectorUtils.Vector3D rotatedLeft = VectorUtils.rotate(bottomLeftV3,
                    Math.toRadians(box3D.getRotOnX()),
                    Math.toRadians(box3D.getRotOnY()),
                    Math.toRadians(box3D.getRotOnZ()));
            VectorUtils.Vector3D rotatedRight = VectorUtils.rotate(bottomRightV3,
                    Math.toRadians(box3D.getRotOnX()),
                    Math.toRadians(box3D.getRotOnY()),
                    Math.toRadians(box3D.getRotOnZ()));
            VectorUtils.Vector3D rotatedBack = VectorUtils.rotate(bottomBackV3,
                    Math.toRadians(box3D.getRotOnX()),
                    Math.toRadians(box3D.getRotOnY()),
                    Math.toRadians(box3D.getRotOnZ()));
            // Plot!
            Function<VectorUtils.Vector3D, Point> transformer = box3D.getTransformer();
            Point topPoint = transformer.apply(rotatedTop);
            Point leftPoint = transformer.apply(rotatedLeft);
            Point rightPoint = transformer.apply(rotatedRight);
            Point backPoint = transformer.apply(rotatedBack);

            Color facesColor = new Color(0, 255, 0, 60);
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(facesColor);
            Polygon triangle1 = new Polygon(new int[] {topPoint.x, leftPoint.x, rightPoint.x},
                    new int[] {topPoint.y, leftPoint.y, rightPoint.y},
                    3);
            g2d.fillPolygon(triangle1);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(triangle1);

            g2d.setColor(facesColor);
            Polygon triangle2 = new Polygon(new int[] {topPoint.x, leftPoint.x, backPoint.x},
                    new int[] {topPoint.y, leftPoint.y, backPoint.y},
                    3);
            g2d.fillPolygon(triangle2);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(triangle2);

            g2d.setColor(facesColor);
            Polygon triangle3 = new Polygon(new int[] {topPoint.x, rightPoint.x, backPoint.x},
                    new int[] {topPoint.y, rightPoint.y, backPoint.y},
                    3);
            g2d.fillPolygon(triangle3);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(triangle3);

            g2d.setColor(facesColor);
            Polygon triangle4 = new Polygon(new int[] {leftPoint.x, rightPoint.x, backPoint.x},
                    new int[] {leftPoint.y, rightPoint.y, backPoint.y},
                    3);
            g2d.fillPolygon(triangle4);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(triangle4);
        };
        box3D.setAfterDrawer(afterDrawer);

        frame.getContentPane().add(box3D, BorderLayout.CENTER);

        frame.setVisible(true); // Display

        box3D.repaint();
        Thread rotator = new Thread(() -> {
            while (true) {
                double rotOnZ = box3D.getRotOnZ();
                box3D.setRotOnZ((rotOnZ + 0.25) % 360);
                System.out.printf("On Z: %.02f\272%n", box3D.getRotOnZ());
                box3D.repaint();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        });
        rotator.start();
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

        new Sample02(); // This one has instantiated the box

        // Finally, display it.
//      whiteBoard.getImage(); // This is for a Notebook
        box3D.repaint();  // This is for a pure Swing context
    }
}
