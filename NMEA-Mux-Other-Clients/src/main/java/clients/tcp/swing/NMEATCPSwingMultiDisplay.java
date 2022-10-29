package clients.tcp.swing;


import calc.GeomUtil;
import clients.tcp.NMEATCPClient;
import nmea.parser.*;
import utils.swing.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Swing UI, several displays.
 * Update the data, if found in the NMEA Sentences.
 */
public class NMEATCPSwingMultiDisplay {

    public static class LabeledJPanel {
        JPanel panel;
        String label;
        public LabeledJPanel(JPanel panel, String label) {
            this.panel = panel;
            this.label = label;
        }
    }

    private final JFrame frame;

    private final JPanel displayHolder = new JPanel(new BorderLayout()); // new FlowLayout()); // new GridBagLayout());
    private final JPanel buttonHolder = new JPanel(new BorderLayout());  // new FlowLayout()); // new GridBagLayout());

    private final JButton goRightButton = new JButton(">");
    private final JButton goLeftButton  = new JButton("<");

    private final JLabel displayLabel = new JLabel("-", SwingConstants.CENTER);

    private final HeadingPanel headingPanel;
    private final DirectionDisplay twdPanel;
    private final JumboDisplay jumboLatitude;
    private final JumboDisplay jumboLongitude;
    private final SpeedPanel bspPanel;
    private final ClockDisplay clockPanel;

    private NMEATCPClient tcpClient;

    private final static int WIDTH = 400;
    private final static int HEIGHT = 400;

    private final boolean VERBOSE = "true".equals(System.getProperty("verbose"));

    private final static boolean CONNECT_TO_TCP = true; // Set to false for development...

    private final LabeledJPanel[] displayArray;
    private int displayIndex = 0;

    private void initTCPClient() {

        this.tcpClient = new NMEATCPClient();

        // The NMEA Consumer. Update the displays
        this.tcpClient.setConsumer(nmea -> {
            if (true) {  //headingPanel != null) {
                // System.out.println(nmea);
                boolean foundHdg = false;
                boolean foundBsp = false;
                boolean foundPos = false;
                boolean foundTwd = false;
                boolean foundUTC = false;

                double heading = 0.0;
                double bsp = 0.0;
                double latitude = 0.0, longitude = 0.0;
                double twd = 0.0;
                Date utc = null;

                final String sentenceID = StringParsers.getSentenceID(nmea);

                // Heading: VHW, HDT, HDM, HDG
                // Position: RMC, GLL
                // Boat Speed: VHW
                // True Wind Dir: MWD (App Wind Dir: MWV)
                // UTC: RMC, ZDA
                switch (sentenceID) {
                    case "VHW":
                        final VHW vhw = StringParsers.parseVHW(nmea);
                        if (vhw != null) {
                            foundHdg = true;
                            heading = vhw.getHdg();
                            if (heading == -1d) {
                                heading = vhw.getHdm();
                            }
                            bsp = vhw.getBsp();
                            foundBsp = true;
                            if (VERBOSE) {
                                System.out.printf("Found VHW [%.02f] and [%.02f] in %s\n", heading, bsp, nmea);
                            }
                        }
                        break;
                    case "HDT":
                        heading = StringParsers.parseHDT(nmea);
                        foundHdg = true;
                        if (VERBOSE) {
                            System.out.printf("Found HDT [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                    case "HDM":
                        heading = StringParsers.parseHDM(nmea);
                        foundHdg = true;
                        if (VERBOSE) {
                            System.out.printf("Found HDM [%.02f] in %s\n", heading, nmea);
                        }
                        break;
                    case "HDG":
                        final HDG hdg = StringParsers.parseHDG(nmea);
                        if (hdg != null) {
                            heading = hdg.getHeading();
                            foundHdg = true;
                            if (VERBOSE) {
                                System.out.printf("Found HDG [%.02f] in %s\n", heading, nmea);
                            }
                        }
                        break;
                    case "RMC":
                        final RMC rmc = StringParsers.parseRMC(nmea);
                        if (rmc != null) {
                            final GeoPos gp = rmc.getGp();
                            latitude = gp.lat;
                            longitude = gp.lng;
                            foundPos = true;
                            utc = rmc.getRmcDate();
                            foundUTC = true;
                            if (VERBOSE) {
                                System.out.printf("Found Position [%s], and date [%s] in %s\n", gp, utc, nmea);
                            }
                        }
                        break;
                    case "GLL":
                        final GLL gll = StringParsers.parseGLL(nmea);
                        if (gll != null) {
                            final GeoPos gllPos = gll.getGllPos();
                            latitude = gllPos.lat;
                            longitude = gllPos.lng;
                            foundPos = true;
                            if (VERBOSE) {
                                System.out.printf("Found Position [%s] in %s\n", gllPos, nmea);
                            }
                        }
                        break;
                    case "ZDA":
                        final UTCDate utcDate = StringParsers.parseZDA(nmea);
                        if (utcDate != null) {
                            utc = utcDate.getValue();
                            foundUTC = true;
                            if (VERBOSE) {
                                System.out.printf("Found UTC date [%s] in %s\n", utc, nmea);
                            }
                        }
                        break;
                    case "MWD":
                        // System.out.println("Found MWD (TWD)");
                        final TrueWind trueWind = StringParsers.parseMWD(nmea);
                        twd = trueWind.getAngle();
                        foundTwd = true;
                        break;
//                    case "MWV":
//                        System.out.println("Found MWV");
//                        break;
                }
                if (foundHdg) {
                    headingPanel.setValue(heading);
                    headingPanel.repaint();
                }
                if (foundBsp) {
                    bspPanel.setSpeed(bsp);
                    bspPanel.repaint();
                }
                if (foundPos) {
                    jumboLatitude.setValue(GeomUtil.decToSex(latitude, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.NS));
                    jumboLongitude.setValue(GeomUtil.decToSex(longitude, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.EW));
                    jumboLatitude.repaint();
                    jumboLongitude.repaint();
                }
                if (foundTwd) {
                    twdPanel.setDirection(twd);
                    twdPanel.setAngleValue(twd);
                    twdPanel.repaint();
                }
                if (foundUTC && utc != null) {
                    clockPanel.setValue(utc.getTime());
                    clockPanel.repaint();
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

    public NMEATCPSwingMultiDisplay() {

        // The JFrame
        frame = new JFrame("Multiple Displays");
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
            int extra = 60; // title bar, buttons, etc.
            frameSize = new Dimension(WIDTH, (HEIGHT + extra));
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
        frame.getContentPane().setLayout(new BorderLayout());

        Dimension panelHolderDim = new Dimension(WIDTH, HEIGHT);

        headingPanel = new HeadingPanel(HeadingPanel.ROSE, true);
        headingPanel.setSmooth(true);
        Dimension headingDim = new Dimension(WIDTH, 60);
        headingPanel.setPreferredSize(headingDim);
        headingPanel.setSize(headingDim);
        JPanel headingHolder = new JPanel(new GridBagLayout());
        headingHolder.setSize(panelHolderDim);
        headingHolder.setPreferredSize(panelHolderDim);
        headingHolder.add(headingPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); // BorderLayout.CENTER);

        twdPanel = new DirectionDisplay("TWD", "000", "True Wind");
        Dimension twsDim = new Dimension(WIDTH, HEIGHT);
        twdPanel.setPreferredSize(twsDim);
        twdPanel.setSize(twsDim);
        JPanel twdHolder = new JPanel(new BorderLayout());
        twdHolder.setSize(panelHolderDim);
        twdHolder.setPreferredSize(panelHolderDim);
        twdHolder.add(twdPanel, BorderLayout.CENTER);

        jumboLatitude = new JumboDisplay("LAT", GeomUtil.decToSex(00.00, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.NS), "Latitude", 40, false);
        jumboLatitude.setDisplayColor(Color.cyan);
        Dimension jumboDim = new Dimension(WIDTH, 100);
        jumboLatitude.setPreferredSize(jumboDim);
        jumboLatitude.setSize(jumboDim);

        jumboLongitude = new JumboDisplay("LONG", GeomUtil.decToSex(00.00, GeomUtil.SWING /*.NO_DEG*/, GeomUtil.EW), "Longitude", 40, false);
        jumboLongitude.setDisplayColor(Color.cyan);
        // Dimension jumboDim = new Dimension(WIDTH, 100);
        jumboLongitude.setPreferredSize(jumboDim);
        jumboLongitude.setSize(jumboDim);

        JPanel jumboHolder = new JPanel(new GridBagLayout());
        jumboHolder.setSize(panelHolderDim);
        jumboHolder.setPreferredSize(panelHolderDim);
        jumboHolder.add(jumboLatitude, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); // BorderLayout.NORTH);
        jumboHolder.add(jumboLongitude, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); // BorderLayout.SOUTH);

        bspPanel = new SpeedPanel(15.0, 0.25, 5, true);
        bspPanel.setLabel("BSP");
        bspPanel.setSpeedUnit(SpeedPanel.SpeedUnit.KNOT);
        bspPanel.setSpeed(0.0);
        Dimension speedoDim = new Dimension(WIDTH, 250);
        bspPanel.setPreferredSize(speedoDim);
        bspPanel.setSize(speedoDim);
        JPanel bspHolder = new JPanel(new GridBagLayout());
        bspHolder.setSize(panelHolderDim);
        bspHolder.setPreferredSize(panelHolderDim);
        bspHolder.add(bspPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); // BorderLayout.NORTH);

        clockPanel = new ClockDisplay("UTC", "00:00:00", "UTC Clock", Color.darkGray);
        // clockPanel.setCustomBGColor(Color.white); // new Color(0f, 0f, 0f, 0f)); // Used if not glossy
        clockPanel.setWithGlossyBG(true);
        clockPanel.setDisplayColor(Color.cyan);
        clockPanel.setGridColor(Color.orange);
        Dimension clockDim = new Dimension(WIDTH, HEIGHT);
        clockPanel.setPreferredSize(clockDim);
        clockPanel.setSize(clockDim);
        JPanel clockHolder = new JPanel(new BorderLayout());
        clockHolder.setSize(panelHolderDim);
        clockHolder.setPreferredSize(panelHolderDim);
        clockHolder.add(clockPanel, BorderLayout.CENTER);

        displayHolder.add(headingHolder, BorderLayout.CENTER); //, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        displayHolder.add(twdHolder, BorderLayout.CENTER); //, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        displayHolder.add(jumboHolder, BorderLayout.CENTER); //, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        displayHolder.add(bspHolder, BorderLayout.CENTER); //, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        displayHolder.add(clockHolder, BorderLayout.CENTER); //, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        goRightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // +1
                if (displayArray != null) {
                    displayIndex++;
                    displayIndex %= displayArray.length;
                    for (int i = 0; i < displayArray.length; i++) {
                        // System.out.printf("CurrIndex=%d, Display #%d : %s\n", displayIndex, i, i == displayIndex ? "on" : "off");
                        displayArray[i].panel.setVisible(i == displayIndex);
                        if (i == displayIndex) {
                            displayLabel.setText(displayArray[i].label);
                            displayLabel.repaint();
                        }
                    }
                }
            }
        });
        goLeftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // -1
                if (displayArray != null) {
                    displayIndex--;
                    if (displayIndex < 0) {
                        displayIndex = displayArray.length - 1;
                    }
                    displayIndex %= displayArray.length;
                    for (int i = 0; i < displayArray.length; i++) {
                        // System.out.printf("CurrIndex=%d, Display #%d : %s\n", displayIndex, i, i == displayIndex ? "on" : "off");
                        displayArray[i].panel.setVisible(i == displayIndex);
                        if (i == displayIndex) {
                            displayLabel.setText(displayArray[i].label);
                            displayLabel.repaint();
                        }
                    }
                }
            }
        });
        buttonHolder.add(goLeftButton, BorderLayout.WEST);
        buttonHolder.add(displayLabel, BorderLayout.CENTER);
        buttonHolder.add(goRightButton, BorderLayout.EAST);

        // >> HERE: Add the components to the JFrame
        frame.getContentPane().add(buttonHolder, BorderLayout.NORTH);
        frame.getContentPane().add(displayHolder, BorderLayout.CENTER);

        frame.setVisible(true); // Display

        headingHolder.setVisible(true);
        twdHolder.setVisible(false);
        jumboHolder.setVisible(false);
        bspHolder.setVisible(false);
        clockHolder.setVisible(false);

        displayArray = new LabeledJPanel[] {
                new LabeledJPanel(headingHolder, "Heading"),
                new LabeledJPanel(twdHolder, "True Wind Direction"),
                new LabeledJPanel(jumboHolder, "GPS Position"),
                new LabeledJPanel(bspHolder, "Boat Speed"),
                new LabeledJPanel(clockHolder, "UTC Time")
        };
        displayLabel.setText(displayArray[0].label);
        final Font labelFont = displayLabel.getFont();
        displayLabel.setFont(labelFont.deriveFont(Font.BOLD | Font.ITALIC));

        // Init and start reading. AFTER instantiating the JFrame.
        if (CONNECT_TO_TCP) {
            initTCPClient();
        } else {
            Thread loopThread = new Thread(() -> {
                while (true) {
                    if (displayArray != null) {
                        displayIndex++;
                        displayIndex %= displayArray.length;
                        for (int i = 0; i < displayArray.length; i++) {
                            System.out.printf("CurrIndex=%d, Display #%d : %s\n", displayIndex, i, i == displayIndex ? "on" : "off");
                            displayArray[i].panel.setVisible(i == displayIndex);
                            if (i == displayIndex) {
                                displayLabel.setText(displayArray[i].label);
                                displayLabel.repaint();
                            }
                        }
                        System.out.println("---------------------------------------------");
                    }
                    try {
                        Thread.sleep(2_500L);
                    } catch (InterruptedException ie) {
                        // Absorb
                    }
                }
            });
            loopThread.start();
        }
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

        new NMEATCPSwingMultiDisplay();

        // Off we go!
    }
}
