package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Demonstrates the drawBox utility.
 */
public class Sample03 {
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
        JOptionPane.showMessageDialog(box3D, "This box is sample #3", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public Sample03() {
        // The JFrame
        frame = new JFrame("This is example #3");
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

        frame.getContentPane().add(new JLabel("Draw boxes"), BorderLayout.NORTH);

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

        new Sample03(); // This one has instantiated the box

        // Do something specific here.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            // Draw a box
            g2d.setColor(Color.BLUE);
            // Dotted lines for the cube
            g2d.setStroke(new BasicStroke(1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[] { 2f, 0f, 2f },
                    2f));
            double[][] boxVertex = {
                    {0.0, 0.0, 1.5},
                    {0.0, 2.0, 1.5},
                    {2.0, 2.0, 1.5},
                    {2.0, 0.0, 1.5},
                    {0.0, 0.0, 0.0},
                    {0.0, 2.0, 0.0},
                    {2.0, 2.0, 0.0},
                    {2.0, 0.0, 0.0}
            };
            box3D.drawBox.accept(g2d, boxVertex);
            // An arrow
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            box3D.drawArrow(g2d,
                    new double[] {0.0, 0.0, 0.0},
                    new double[] {2.0, 2.0, 1.5});
        };
        box3D.setAfterDrawer(afterDrawer);

        // Finally, display it.
//      whiteBoard.getImage(); // This is for a Notebook
        box3D.repaint();  // This is for a pure Swing context
    }
}
