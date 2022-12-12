package tcp.clients;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Basic Swing UI
 * Just displays the NMEA Sentence, as it is.
 *
 * Client for the server TCP_ZDA_server.py
 *
 * Implements a 2-way communication with the TCP server.
 * Reads the data emitted by the server, continuously
 * Can send requests to the server, to produce ZDA strings slower or faster.
 */
public class ZDATCPSwing101 {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final JFrame frame;
    private final JLabel nmeaLabel;

    private final static int WIDTH = 1024;
    private final static int HEIGHT = 100;
    private final static int FONT_SIZE = 24;

    private Thread reader;
    private final AtomicBoolean keepReading = new AtomicBoolean(true);

    private final String defaultHost = "localhost";
    private final int defaultPort = 7001;

    private final boolean VERBOSE = "true".equals(System.getProperty("tcp.verbose"));

    private Consumer<String> dataConsumer;

    public void startConnection(String ip, int port) throws Exception {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (ConnectException ce) {
            throw new RuntimeException(String.format("ConnectException for %s, port %d", ip, port));
        }
    }

    public void sendMessage(String msg) throws Exception {
        out.println(msg);
    }

    public void stopConnection() throws Exception {
        in.close();
        out.close();
        clientSocket.close();
    }

    private void initTCPClient() {
        try {
            String tcpHost = System.getProperty("tcp.host", defaultHost);
            int tcpPort = Integer.parseInt(System.getProperty("tcp.port", String.valueOf(defaultPort)));
            if (VERBOSE) {
                System.out.printf("Connecting on %s:%d\n", tcpHost, tcpPort);
            }
            startConnection(tcpHost, tcpPort);
        } catch (Exception ex) {
            // Ooch!
            ex.printStackTrace();
            // System.exit(1);
        }
        System.out.println(new Date() + ": New " + this.getClass().getName() + " created.");

        dataConsumer = nmea -> {
            if (nmeaLabel != null) {
                if (VERBOSE) {
                    System.out.printf("\tReceived [%s]\n", nmea);
                }
                nmeaLabel.setText(nmea);
                nmeaLabel.repaint();
            } else {
                System.out.printf("Received [%s]\n", nmea);
            }
        };

        // Read loop
        try {
            reader = new Thread(() -> {
                keepReading.set(true);
                while (keepReading.get()) {
                    if (in != null) {
                        try {
                            String fromServer = in.readLine();
                            if (dataConsumer != null) {
                                dataConsumer.accept(fromServer);
                            } else {
                                System.out.println(fromServer);
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            keepReading.set(false);
                        }
                    } else {
                        System.out.println("InputStream not initialized...");
                        keepReading.set(false);
                    }
                }
                System.out.println("Exiting reader thread");
            }, "TCP-Reader");
            reader.start();
        } catch (Exception ex) {
            System.err.println("TCP Reader:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public ZDATCPSwing101() {

        // The JFrame
        frame = new JFrame("ZDA Sentences over TCP");
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
                keepReading.set(false);
                if (reader != null) {
                    if (reader.isAlive()) {
                        reader.interrupt();  // Bam! DTC!
                    }
                }
                if (clientSocket != null) {
                    try {
                        stopConnection();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                frame.setVisible(false);
                System.out.println("Bye!");
                System.exit(0);
            }
        });

        nmeaLabel = new JLabel();
//        nmeaLabel.setFont(nmeaLabel.getFont().deriveFont(Font.BOLD, 56f));
        nmeaLabel.setFont(new Font("Courier New", Font.BOLD, FONT_SIZE));
        // >> HERE: Add the label to the JFrame
        frame.getContentPane().add(nmeaLabel, BorderLayout.CENTER);

        JPanel buttonsHolder = new JPanel();
        // Buttons
        JButton slowerButton = new JButton("Slower");
        buttonsHolder.add(slowerButton);
        slowerButton.addActionListener(e -> {
            try {
                sendMessage("SLOWER");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        JButton fasterButton = new JButton("Faster");
        buttonsHolder.add(fasterButton);
        fasterButton.addActionListener(e -> {
            try {
                sendMessage("FASTER");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.getContentPane().add(buttonsHolder, BorderLayout.SOUTH);

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

        new ZDATCPSwing101();

        // Off we go!
    }
}
