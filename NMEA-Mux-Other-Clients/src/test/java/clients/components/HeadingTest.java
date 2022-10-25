package clients.components;

import utils.swing.components.HeadingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HeadingTest {
    private final JFrame frame;
    private final HeadingPanel headingPanel;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 100;

    private boolean keepTicking = true;
    private double currentHeading = 0;
    private Thread headingThread;

    private void initHeadingClient() {
        // Start a thread to update the heading (randomly)
        headingThread = new Thread(() -> {
            while (keepTicking) {
                if (headingPanel != null) {
                    double value = Math.random() * 10;
                    int sign = (Math.random() >= 0.5) ? 1 : -1;
                    currentHeading = (currentHeading + (sign * value)) % 360;
                    // System.out.printf("Speed is now: %f knt\n", currentSpeed);
                    headingPanel.setValue(currentHeading);
                    headingPanel.repaint();
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            }
            System.out.println("Bye headingThread");
        }, "Heading");
        headingThread.start();
    }

    public HeadingTest() {

        // The JFrame
        frame = new JFrame("Compass Test");
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
            frameSize = new Dimension(WIDTH, HEIGHT);
            frame.setSize(frameSize);
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.out.println("Exiting...");
                // Stop the thread here
                keepTicking = false;
                try {
                    if (headingThread != null) {
                        synchronized (headingThread) {
                            headingThread.join();
                        }
                    }
                } catch (InterruptedException ie) {
                    // Absorb
                }
                frame.setVisible(false);
                System.exit(0);
            }
        });

        headingPanel = new HeadingPanel(HeadingPanel.ZERO_TO_360, true); // HeadingPanel.ROSE, true);
        headingPanel.setSmooth(true);
        headingPanel.setValue(0.0); // heading

//        Dimension compassDim = new Dimension(150, 150);
//        HeadingPanel.setPreferredSize(compassDim);
//        clockDisplay.setSize(compassDim);

        // >> HERE: Add the compass to the JFrame
        frame.getContentPane().add(headingPanel, BorderLayout.CENTER);

        frame.setVisible(true); // Display all the frame

        // Init and start reading time. AFTER instantiating the JFrame.
        initHeadingClient();
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

        new HeadingTest();

        // Off we go!
    }
}
