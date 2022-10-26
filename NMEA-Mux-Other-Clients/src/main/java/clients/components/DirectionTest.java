package clients.components;

import utils.swing.components.DirectionDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DirectionTest {
    private final JFrame frame;
    private final DirectionDisplay twsPanel;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 400;

    private boolean keepTicking = true;
    private double currentTWD = 0;
    private Thread twdThread;

    private void initHeadingClient() {
        // Start a thread to update the heading (randomly)
        twdThread = new Thread(() -> {
            while (keepTicking) {
                if (twsPanel != null) {
                    double value = Math.random() * 10;
                    int sign = (Math.random() >= 0.5) ? 1 : -1;
                    currentTWD = (currentTWD + (sign * value)) % 360;
                    if (currentTWD < 0) {
                        currentTWD = 360 - currentTWD;
                    }
                    // System.out.printf("Speed is now: %f knt\n", currentSpeed);
                    twsPanel.setDirection(currentTWD);
                    twsPanel.setAngleValue(currentTWD);
                    twsPanel.repaint();
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            }
            System.out.println("Bye headingThread");
        }, "Heading");
        twdThread.start();
    }

    public DirectionTest() {

        // The JFrame
        frame = new JFrame("TWD Test");
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
                    if (twdThread != null) {
                        synchronized (twdThread) {
                            twdThread.join();
                        }
                    }
                } catch (InterruptedException ie) {
                    // Absorb
                }
                frame.setVisible(false);
                System.out.println("Bye!");
                System.exit(0);
            }
        });

        twsPanel = new DirectionDisplay("TWD", "000", "True Wind");

//        Dimension compassDim = new Dimension(150, 150);
//        HeadingPanel.setPreferredSize(compassDim);
//        clockDisplay.setSize(compassDim);

        // >> HERE: Add the compass to the JFrame
        frame.getContentPane().add(twsPanel, BorderLayout.CENTER);

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

        new DirectionTest();

        // Off we go!
    }
}
