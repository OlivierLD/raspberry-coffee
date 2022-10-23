package clients.tcp;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

/**
 * Basic Swing UI
 * Just displays the NMEA Sentence, as it is.
 */
public class NMEATCPSwing101 {
    private final JFrame frame;
    private final JLabel nmeaLabel;

    private NMEATCPClient tcpClient;

    private final static int WIDTH = 1024;
    private final static int HEIGHT = 100;
    private final static int FONT_SIZE = 24;

    private void initTCPClient() {
        this.tcpClient = new NMEATCPClient();
        this.tcpClient.setConsumer(nmea -> {
            if (nmeaLabel != null) {
                nmeaLabel.setText(nmea);
                nmeaLabel.repaint();
            } else {
                System.out.printf("Received [%s]\n", nmea);
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

            long howMuch = 1_000L;
            System.out.println("Will try to reconnect in " + howMuch + "ms.");
            try {
                Thread.sleep(howMuch);
            } catch (InterruptedException ignored) {
                // Bam!
            }
        }
    }

    public NMEATCPSwing101() {

        // The JFrame
        frame = new JFrame("Raw NMEA Sentences");
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

        nmeaLabel = new JLabel();
//        nmeaLabel.setFont(nmeaLabel.getFont().deriveFont(Font.BOLD, 56f));
        nmeaLabel.setFont(new Font("Courier New", Font.BOLD, FONT_SIZE));
        // >> HERE: Add the box to the JFrame
        frame.getContentPane().add(nmeaLabel, BorderLayout.CENTER);

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

        new NMEATCPSwing101(); // This one has instantiated the box

        // Off we go!
    }
}
