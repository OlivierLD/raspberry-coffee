package clients.components;

import utils.swing.components.SpeedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SpeedTest {
    private final JFrame frame;
    private final SpeedPanel speedoDisplay;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 300;

    private boolean keepTicking = true;
    private final double maxSpeed = 15d;
    private double currentSpeed = 7.5;

    private void initSpeedClient() {
        // Start a thread to update the speed (randomly)
        Thread speedThread = new Thread(() -> {
            while (keepTicking) {
                if (speedoDisplay != null) {
                    double delta = Math.random() * 0.5;
                    int sign = (Math.random() >= 0.5) ? 1 : -1;
                    if (currentSpeed + (sign * delta) < 0) {
                        sign *= -1;
                    }
                    if (currentSpeed + (sign * delta) > maxSpeed) {
                        sign *= -1;
                    }
                    currentSpeed += (sign * delta);
                    // System.out.printf("Speed is now: %f knt\n", currentSpeed);
                    speedoDisplay.setSpeed(currentSpeed);
                    speedoDisplay.repaint();
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            }
        }, "Speed");
        speedThread.start();
    }

    public SpeedTest() {

        // The JFrame
        frame = new JFrame("Speedometer Test");
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

        speedoDisplay = new SpeedPanel(maxSpeed, 0.25, 5, true);
        speedoDisplay.setLabel("BSP");
        speedoDisplay.setSpeedUnit(SpeedPanel.SpeedUnit.KNOT);

//        Dimension clockDim = new Dimension(150, 150);
//        clockDisplay.setPreferredSize(clockDim);
//        clockDisplay.setSize(clockDim);

        // >> HERE: Add the clock to the JFrame
        frame.getContentPane().add(speedoDisplay, BorderLayout.CENTER);

        frame.setVisible(true); // Display all the frame

        // Init and start reading time. AFTER instantiating the JFrame.
        initSpeedClient();
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

        new SpeedTest();

        // Off we go!
    }
}
