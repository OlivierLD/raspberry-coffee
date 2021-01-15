package gsg.examples.box3d;

import gsg.SwingUtils.Box3D;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Sample05 {
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

    private AtomicBoolean keepSpinning = new AtomicBoolean(true);

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.println("Exit requested");
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.println("Help requested");
        JOptionPane.showMessageDialog(box3D, "This box is sample #5", "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    public Sample05() {
        // The JFrame
        frame = new JFrame("This is example #5");
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

        frame.getContentPane().add(new JLabel("Spinning sample. Click the figure to start or stop the spinning."), BorderLayout.NORTH);

        // >> HERE: Add the box to the JFrame
        box3D = new Box3D();
        box3D.setyMax(5d);
        box3D.setZoom(0.75d);

        box3D.setRotOnY(-5d); // Wow!

        box3D.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                keepSpinning.set(!keepSpinning.get()); // Start/Stop the spinning
                System.out.printf("Spinning is %s\n", keepSpinning.get() ? "on" : "off");
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        // Do something specific here, after the box drawing.
        Consumer<Graphics2D> afterDrawer = g2d -> {

            double[] spatialPointOne = new double[] { 1d, 1d, 1.5d };
            double[] spatialPointTwo = new double[] { 2d, 3d, -0.5d };
            double[] spatialPointThree = new double[] { -2d, -2d, -1.5d };

            // Draw surrounding boxes
            g2d.setColor(Color.BLUE);
            // Dotted lines for the cube
            g2d.setStroke(new BasicStroke(1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[] { 2f, 0f, 2f },
                    2f));

            box3D.drawSurroundingBox(g2d, spatialPointOne, spatialPointTwo);
            g2d.setStroke(new BasicStroke(2));
//            box3D.drawSegment(g2d, spatialPointOne, spatialPointTwo);
            box3D.drawArrow(g2d, spatialPointOne, spatialPointTwo);
            VectorUtils.Vector3D middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointOne),
                    new VectorUtils.Vector3D(spatialPointTwo));
            box3D.plotStringAt(g2d, "Vector A", middle, true);

            g2d.setColor(Color.RED);
            // Dotted lines for the cube
            g2d.setStroke(new BasicStroke(1,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND,
                    1.0f,
                    new float[] { 4f, 0f, 5f },
                    2f));
            box3D.drawSurroundingBox(g2d, spatialPointTwo, spatialPointThree);
            g2d.setStroke(new BasicStroke(2));
//            box3D.drawSegment(g2d, spatialPointTwo, spatialPointThree);
            box3D.drawArrow(g2d, spatialPointTwo, spatialPointThree);
            middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointTwo),
                    new VectorUtils.Vector3D(spatialPointThree));
            box3D.plotStringAt(g2d, "Vector B", middle, true);

            // Draw sum
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.GREEN);
//            box3D.drawSegment(g2d, spatialPointOne, spatialPointThree);
            box3D.drawArrow(g2d, spatialPointOne, spatialPointThree);
            middle = VectorUtils.findMiddle(new VectorUtils.Vector3D(spatialPointOne),
                    new VectorUtils.Vector3D(spatialPointThree));
            box3D.plotStringAt(g2d, "(A+B)", middle, true);

            g2d.setColor(Color.BLUE);
            g2d.drawString("Vector (A)",10, 18 + 16);
            g2d.setColor(Color.RED);
            g2d.drawString("Vector (B)",10, 18 + 16 + 16);
            g2d.setColor(Color.GREEN);
            g2d.drawString("Vector (A+B)",10, 18 + 16 + 16 + 16);
        };
        box3D.setAfterDrawer(afterDrawer);

        frame.getContentPane().add(box3D, BorderLayout.CENTER);

        frame.setVisible(true); // Display

        box3D.repaint();
        Thread rotator = new Thread(() -> {
            while (true) {
                if (keepSpinning.get()) {
                    double rotOnZ = box3D.getRotOnZ();
                    box3D.setRotOnZ((rotOnZ + 0.25) % 360);
//                    System.out.printf("On Z: %.02f\272%n", box3D.getRotOnZ());
                    box3D.repaint();
                }
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

        new Sample05(); // This one has instantiated the box

        // Finally, display it.
//      whiteBoard.getImage(); // This is for a Notebook
        box3D.repaint();  // This is for a pure Swing context
    }
}
