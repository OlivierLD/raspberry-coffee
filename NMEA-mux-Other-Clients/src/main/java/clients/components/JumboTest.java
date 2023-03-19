package clients.components;

import calc.GeomUtil;
import utils.swing.components.JumboDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JumboTest {
    private final JFrame frame;
    private final JumboDisplay jumbo;

    private final static int WIDTH = 600;
    private final static int HEIGHT = 200;

    private boolean keepTicking = true;
    private double currentLatitude = 37.1234;
    private Thread laltitudeThread;

    private void initJumboClient() {
        // Start a thread to update the latitude (randomly)
        laltitudeThread = new Thread(() -> {
            while (keepTicking) {
                if (jumbo != null) {
                    double value = Math.random() * 0.0025;
                    int sign = (Math.random() >= 0.5) ? 1 : -1;
                    currentLatitude = (currentLatitude + (sign * value));
                    if (currentLatitude > 90) {
                        currentLatitude -= value;
                    }
                    if (currentLatitude < -90) {
                        currentLatitude += value;
                    }
                    jumbo.setValue(GeomUtil.decToSex(currentLatitude, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.NS));
                    jumbo.repaint();
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            }
            System.out.println("Bye headingThread");
        }, "Heading");
        laltitudeThread.start();
    }

    public JumboTest() {

        // The JFrame
        frame = new JFrame("Jumbo Test");
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
                    if (laltitudeThread != null) {
                        synchronized (laltitudeThread) {
                            laltitudeThread.join();
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

        jumbo = new JumboDisplay("LAT", GeomUtil.decToSex(currentLatitude, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.NS), "Latitude", 72, false);
        jumbo.setDisplayColor(Color.cyan);

//        Dimension compassDim = new Dimension(150, 150);
//        HeadingPanel.setPreferredSize(compassDim);
//        clockDisplay.setSize(compassDim);

        // >> HERE: Add the compass to the JFrame
        frame.getContentPane().add(jumbo, BorderLayout.CENTER);

        frame.setVisible(true); // Display all the frame

        // Init and start reading time. AFTER instantiating the JFrame.
        initJumboClient();
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

        new JumboTest();

        // Off we go!
    }
}
