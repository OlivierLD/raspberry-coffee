package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sample01 {
    private JFrame frame;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private final static int WIDTH = 860;
    private final static int HEIGHT = 600;

    // The Box instantiation
    private static Box3D box3D = new Box3D();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(box3D, "This box is sample #1", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public Sample01() {
        // The JFrame
        frame = new JFrame("This is example #1");
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

        frame.getContentPane().add(new JLabel("This is a full sample"), BorderLayout.NORTH);

        // >> HERE: Add the box to the JFrame
        frame.getContentPane().add(box3D, BorderLayout.CENTER);

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

        new Sample01(); // This one has instantiated the box

        // Do something specific here.
        Consumer<Graphics2D> afterDrawer = g2d -> {
            g2d.setColor(Color.RED);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD | Font.ITALIC, 48f));
            g2d.drawString("AFTER DRAWING!", 10, 300);
            // Draw a 3D triangle
            // 1 - Define its vertex
            VectorUtils.Vector3D topV3 = new VectorUtils.Vector3D(1, -1.5, 2);
            VectorUtils.Vector3D bottomLeftV3 = new VectorUtils.Vector3D(-2, 2, -2);
            VectorUtils.Vector3D bottomRightV3 = new VectorUtils.Vector3D(2, 2.3, -2.1);
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
            // Plot!
            Function<VectorUtils.Vector3D, Point> transformer = box3D.getTransformer();
            Point topPoint = transformer.apply(rotatedTop);
            Point leftPoint = transformer.apply(rotatedLeft);
            Point rightPoint = transformer.apply(rotatedRight);

            Polygon triangle = new Polygon(new int[] {topPoint.x, leftPoint.x, rightPoint.x},
                    new int[] {topPoint.y, leftPoint.y, rightPoint.y},
                    3);
            g2d.setColor(new Color(0, 255, 0, 125));
            g2d.fillPolygon(triangle);
        };
        box3D.setAfterDrawer(afterDrawer);

        // Finally, display it.
//      whiteBoard.getImage(); // This is for a Notebook
        box3D.repaint();  // This is for a pure Swing context
    }
}
