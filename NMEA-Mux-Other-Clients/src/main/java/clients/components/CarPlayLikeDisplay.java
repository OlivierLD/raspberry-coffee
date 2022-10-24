package clients.components;

import utils.swing.components.ClockDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Eventually... WiP.
 */
public class CarPlayLikeDisplay {
    private final JFrame frame;
    private final ClockDisplay clockDisplay;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 400;

    private boolean keepTicking = true;

    private void initClockClient() {
        // Start a thread
        Thread clockThread = new Thread(() -> {
            while (keepTicking) {
                Date now = new Date();
                if (clockDisplay != null) {
                    clockDisplay.setValue(now.getTime());
                    clockDisplay.repaint();
                    try {
                        Thread.sleep(1_000L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            }
        }, "Clock");
        clockThread.start();
    }

    public CarPlayLikeDisplay() {

        // The JFrame
        frame = new JFrame("UTC Time");
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
                // Stop the thread here
                keepTicking = false;
                try {
                    Thread.sleep(1_000L); // Wait for the thread to finish
                } catch (InterruptedException ie) {
                    // Absorb
                }
                frame.setVisible(false);
                System.exit(0);
            }
        });

        clockDisplay = new ClockDisplay("UTC", "00:00:00", "UTC Clock test", Color.darkGray);
        // clockDisplay.setCustomBGColor(Color.white); // new Color(0f, 0f, 0f, 0f));
        clockDisplay.setWithGlossyBG(true);
        clockDisplay.setDisplayColor(Color.cyan);
        clockDisplay.setGridColor(Color.orange); // clockTextFontColor);

//        Dimension clockDim = new Dimension(150, 150);
//        clockDisplay.setPreferredSize(clockDim);
//        clockDisplay.setSize(clockDim);

        // >> HERE: Add the clock to the JFrame
        frame.getContentPane().add(clockDisplay, BorderLayout.CENTER);

        frame.setVisible(true); // Display all the frame

        // Init and start reading. AFTER instantiating the JFrame.
        initClockClient();
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

        new CarPlayLikeDisplay();

        // Off we go!
    }
}
