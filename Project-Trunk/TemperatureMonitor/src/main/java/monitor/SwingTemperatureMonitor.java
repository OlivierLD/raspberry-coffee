package monitor;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Monitor the CPU Temperature (on Raspberry Pi)
 */
public class SwingTemperatureMonitor {

    private final static String TITLE = "CPU Temperature over time";

    private JFrame frame;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;

    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;

    private static boolean verbose = false;

    // The WhiteBoard instantiation
    private final static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private final int BUFFER_LEN = 900;  // 900 sec: 15 minutes
    private List<Double> tempData = new ArrayList<>();

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.printf("Exit requested, %s\n", ae);
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.printf("Help requested, %s\n", ae);
        JOptionPane.showMessageDialog(whiteBoard, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    private Supplier<Double> getData = () -> {
        double temperature = 0d;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("vcgencmd", "measure_temp");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream(); // Process output

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            boolean keepReading = true;
            while (keepReading) {
                String line = reader.readLine();
                if (line == null) {
                    keepReading = false;
                } else {
                    if (verbose) {
                        System.out.println(line);
                    }
                    String value = line.substring(line.indexOf("=") + 1, line.indexOf("'"));
                    if (verbose) {
                        System.out.printf("Value: [%s]\n", value);
                    }
                    temperature = Double.parseDouble(value);
                }
            }
            reader.close();
            inputStream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            temperature = 100d * Math.random();
        }

        return temperature;
    };

    private void refreshData() {
        if (tempData.size() == 0) {
            if (verbose) {
                System.out.println("No data...");
            }
            return;
        }

        IntStream xs = IntStream.range(0, tempData.size());
        try {
            // Prepare data for display
            double[] xData = xs.mapToDouble(x -> (double) x)
                    .toArray();
            double[] tData = tempData.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();
            List<VectorUtils.Vector2D> dataOneVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataOneVectors.add(new VectorUtils.Vector2D(xData[i], tData[i]));
            }
            whiteBoard.setAxisColor(Color.BLACK);
            whiteBoard.resetAllData();
            // Temp series
            WhiteBoardPanel.DataSerie dataTempSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataOneVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoard.addSerie(dataTempSerie);
            // Finally, display it.
            whiteBoard.repaint();  // This is for a pure Swing context
        } catch (Exception ex) {
            System.err.println("Bam!");
            ex.printStackTrace();
        }
    }

    private void show() {
        this.frame.setVisible(true);
    }

    private void startGrabber() {
        Thread dataGrabber = new Thread(() -> {
            if (verbose) {
                System.out.println("Staring grabber thread...");
            }
            try {
                Thread.sleep(1_000L); // some slack
            } catch (InterruptedException ie) {
                // Oops
            }
            if (verbose) {
                System.out.println("Grabber thread, in the loop.");
            }
            while (true) {
                double temperature = getData.get();
                synchronized (tempData) {
                    tempData.add(temperature);
                    while (tempData.size() > BUFFER_LEN) {
                        tempData.remove(0);
                    }
                }
                refreshData();
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    // Oops
                }
            }
        });
        dataGrabber.start();
    }

    private void initComponents() {
        // The JFrame
        frame = new JFrame(TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//      System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        frameSize.height = Math.min(frameSize.height, screenSize.height);
        frameSize.width = Math.min(frameSize.width, screenSize.width);

        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(WIDTH, HEIGHT + 50 + 10); // 50: ... menu, title bar, etc. 10: button
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

        topLabel = new JLabel(TITLE);
        topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        frame.getContentPane().add(topLabel, BorderLayout.NORTH);

        // >> HERE: Add the WitheBoard to the JFrame
        frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);
//        frame.pack();
    }

    public SwingTemperatureMonitor() {
    }

    private final static String VERBOSE_PREFIX = "--verbose:";

    public static void main(String... args) {

        try {
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String arg : args) {
            if (arg.startsWith(VERBOSE_PREFIX)) {
                verbose = "true".equals(arg.substring(VERBOSE_PREFIX.length()));
            }
        }

        System.out.println("----------------------------------------------");
        System.out.printf("Running from folder %s\n", System.getProperty("user.dir"));
        System.out.printf("Java Version %s\n", System.getProperty("java.version"));
        System.out.println("----------------------------------------------");

        SwingTemperatureMonitor thisThing = new SwingTemperatureMonitor();// This one has instantiated the white board
        thisThing.initComponents();

//        thisThing.startGrabber();

        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(125, 0, 255, 255));
        whiteBoard.setWithGrid(false);
        whiteBoard.setBgColor(new Color(250, 250, 250, 255));
        whiteBoard.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoard.setSize(new Dimension(800, 600));
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoard.setGraphicMargins(30);
        whiteBoard.setXEqualsY(false);
        // Enforce Y amplitude
        whiteBoard.setForcedMinY(0d);
        whiteBoard.setForcedMaxY(100d);

        thisThing.show();

//        thisThing.refreshData();
        thisThing.startGrabber();
    }
}
