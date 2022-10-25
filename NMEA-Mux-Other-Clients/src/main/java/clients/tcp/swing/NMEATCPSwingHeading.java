package clients.tcp.swing;


import clients.tcp.NMEATCPClient;
import nmea.parser.HDG;
import nmea.parser.StringParsers;
import nmea.parser.VHW;
import utils.swing.components.HeadingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Basic Swing UI, one display.
 * Just displays the heading, if found in the NMEA Sentence.
 */
public class NMEATCPSwingHeading {
    private final JFrame frame;
    private final HeadingPanel headingPanel;

    private NMEATCPClient tcpClient;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 100;

    private final boolean VERBOSE = "true".equals(System.getProperty("verbose"));

    private void initTCPClient() {
        this.tcpClient = new NMEATCPClient();
        // The NMEA Consumer
        this.tcpClient.setConsumer(nmea -> {
            if (headingPanel != null) {
                // System.out.println(nmea);
                final String sentenceID = StringParsers.getSentenceID(nmea);
                boolean foundHeading = false;
                double heading = 0.0;
                // VHW, HDT, HDM, HDG
                switch (sentenceID) {
                    case "VHW":
                        final VHW vhw = StringParsers.parseVHW(nmea);
                        foundHeading = true;
                        heading = vhw.getHdg();
                        if (heading == 0d) { // TODO Something better
                            heading = vhw.getHdm();
                        }
                        if (VERBOSE) {
                            System.out.printf("Found VHW [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                    case "HDT":
                        heading = StringParsers.parseHDT(nmea);
                        foundHeading = true;
                        if (VERBOSE) {
                            System.out.printf("Found HDT [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                    case "HDM":
                        heading = StringParsers.parseHDM(nmea);
                        foundHeading = true;
                        if (VERBOSE) {
                            System.out.printf("Found HDM [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                    case "HDG":
                        final HDG hdg = StringParsers.parseHDG(nmea);
                        heading = hdg.getHeading();
                        foundHeading = true;
                        if (VERBOSE) {
                            System.out.printf("Found HDG [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                }
                if (foundHeading) {
                    headingPanel.setValue(heading);
                    headingPanel.repaint();
                }
            } else {
                System.out.printf("Received [%s]\n", nmea); // In case headingPanel is null
            }
        });

        try {
            tcpClient.startConnection(
                    System.getProperty("tcp.host", "localhost"),
                    Integer.parseInt(System.getProperty("tcp.port", String.valueOf(7001)))
            );
        } catch (Exception ex) {
            // Ooch!
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println(new Date() + ": New " + tcpClient.getClass().getName() + " created.");

        // Read loop
        try {
            // Initiate. Request data
            tcpClient.read();
        } catch (Exception ex) {
            System.err.println("TCP Reader:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public NMEATCPSwingHeading() {

        // The JFrame
        frame = new JFrame("Heading");
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
                if (tcpClient != null) {
                    try {
                        tcpClient.stopConnection();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                frame.setVisible(false);
                System.exit(0);
            }
        });

        headingPanel = new HeadingPanel(HeadingPanel.ZERO_TO_360, true);
        headingPanel.setSmooth(true);
        // >> HERE: Add the label to the JFrame
        frame.getContentPane().add(headingPanel, BorderLayout.CENTER);

        frame.setVisible(true); // Display

        // Init and start reading. AFTER instantiating the JFrame.
        initTCPClient();
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

        new NMEATCPSwingHeading();

        // Off we go!
    }
}
