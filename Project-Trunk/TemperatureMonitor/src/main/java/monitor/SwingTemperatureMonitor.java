package monitor;

import gsg.SwingUtils.WhiteBoardPanel;
import gsg.VectorUtils;
import utils.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Monitor the CPU Temperature and Load (on Raspberry Pi)
 * This is an example...
 */
public class SwingTemperatureMonitor {

    //    private final static SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");
    private final static SimpleDateFormat SDF = new SimpleDateFormat("mm:ss"); // Minutes and seconds

    private final static String TITLE = "CPU Temperature and Load over time";
//    private final static String TITLE = "CPU Temperature over time";

    private final static class DataHolder {
        double temperature;
        double cpuLoad;
    }

    private JFrame frame;
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menuFile = new JMenu();
    private final JMenuItem menuFileExit = new JMenuItem();
    private final JMenu menuHelp = new JMenu();
    private final JMenuItem menuHelpAbout = new JMenuItem();
    private JLabel topLabel;

    private final static int WIDTH = 1_200;
    private final static int HEIGHT = 400;

    private static boolean verbose = false;
    private final static int DEFAULT_BUFFER_LEN = 900;
    private final static int DEFAULT_BETWEEN_LOOPS = 1_000; // in ms

    // The WhiteBoard instantiation
    private final static WhiteBoardPanel whiteBoard = new WhiteBoardPanel();

    private final List<DataHolder> displayData = new ArrayList<>();
    private final List<Long> abscissa = new ArrayList<>();

    private double minValue = Double.MAX_VALUE;
    private double maxValue = -Double.MAX_VALUE;

    private static int bufferLength = DEFAULT_BUFFER_LEN;
    private static long betweenLoops = DEFAULT_BETWEEN_LOOPS;

    private void fileExit_ActionPerformed(ActionEvent ae) {
        System.out.printf("Exit requested, %s\n", ae);
        System.exit(0);
    }

    private void helpAbout_ActionPerformed(ActionEvent ae) {
        System.out.printf("Help requested, %s\n", ae);
        JOptionPane.showMessageDialog(whiteBoard, TITLE, "GSG Help", JOptionPane.PLAIN_MESSAGE);
    }

    private final Supplier<DataHolder> dataGrabber = () -> {

        double temperature; //  = 0d;
        double cpuLoad; //  = 0d;

        try {
            String tempValue = SystemUtils.getCPUTemperature2();
            String value = tempValue.substring(tempValue.indexOf("=") + 1, tempValue.indexOf("'"));
            temperature = Double.parseDouble(value);
        } catch (Exception ex) {
            // ex.printStackTrace();
            if (verbose) {
                System.err.printf("Temp, Exception: %s, randomizing CPU temperature value. Not on a Pi?\n", ex.getMessage());
            }
            temperature = 100d * Math.random();
        }

        try {
            String cpuLoadValue = SystemUtils.getCPULoad2();
            String nbCPU = SystemUtils.getNBCpu();
            if (verbose) {
                System.out.printf("CPU Load: %s, %s CPU(s)", cpuLoadValue, nbCPU);
            }
            // TODO Check if that is right...
            cpuLoad = (Double.parseDouble(cpuLoadValue) * 100.0) / Double.parseDouble(nbCPU); // In %
        } catch (Exception ex) {
            // ex.printStackTrace();
            if (verbose) {
                System.err.printf("Load, Exception: %s, randomizing CPU load value. Not on a Pi?\n", ex.getMessage());
            }
            cpuLoad = 100d * Math.random();
        }

        DataHolder dh = new DataHolder();
        dh.temperature = temperature;
        dh.cpuLoad = cpuLoad;
        return dh;
    };

    private void refreshData() {
        IntStream xs = IntStream.range(0, this.displayData.size());
        try {
            // Prepare data for display
            double[] xData = xs.mapToDouble(x -> (double) x)
                    .toArray();
            double[] tData = this.displayData.stream()
                    .mapToDouble(dh -> dh.temperature)
                    .toArray();
            double[] cpuData = this.displayData.stream()
                    .mapToDouble(dh -> dh.cpuLoad)
                    .toArray();
            // Temperature
            List<VectorUtils.Vector2D> dataOneVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataOneVectors.add(new VectorUtils.Vector2D(xData[i], tData[i]));
            }
            // CPU Load
            List<VectorUtils.Vector2D> dataTwoVectors = new ArrayList<>();
            for (int i = 0; i < xData.length; i++) {
                dataTwoVectors.add(new VectorUtils.Vector2D(xData[i], cpuData[i]));
            }

            // Now, the graph
            whiteBoard.setAxisColor(Color.BLACK);
            whiteBoard.resetAllData();

            // Min & Max
            if (this.displayData.size() > 1) {
                // 1 - Min
                List<VectorUtils.Vector2D> minVectors = new ArrayList<>();
                minVectors.add(new VectorUtils.Vector2D(xData[0], minValue));
                minVectors.add(new VectorUtils.Vector2D(xData[xData.length - 1], minValue));
                WhiteBoardPanel.DataSerie minTempSerie = new WhiteBoardPanel.DataSerie()
                        .data(minVectors)
                        .graphicType(WhiteBoardPanel.GraphicType.DOTTED_LINE)
                        .lineThickness(2)
                        .color(Color.BLACK);
                whiteBoard.addSerie(minTempSerie);
                // 2 - Max
                List<VectorUtils.Vector2D> maxVectors = new ArrayList<>();
                maxVectors.add(new VectorUtils.Vector2D(xData[0], maxValue));
                maxVectors.add(new VectorUtils.Vector2D(xData[xData.length - 1], maxValue));
                WhiteBoardPanel.DataSerie maxTempSerie = new WhiteBoardPanel.DataSerie()
                        .data(maxVectors)
                        .graphicType(WhiteBoardPanel.GraphicType.DOTTED_LINE)
                        .lineThickness(2)
                        .color(Color.RED);
                whiteBoard.addSerie(maxTempSerie);
            }
            // Temperature series
            WhiteBoardPanel.DataSerie dataTempSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataOneVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.AREA)
                    .areaGradient(new Color(1f, 0f, 0f, 0.75f), // Transparent red
                            new Color(1f, 1f, 0f, 0.75f)) // Transparent yellow
                    .lineThickness(3)
                    .color(Color.BLUE);
            whiteBoard.addSerie(dataTempSerie);
            // CPU Load series
            WhiteBoardPanel.DataSerie dataCPUSerie = new WhiteBoardPanel.DataSerie()
                    .data(dataTwoVectors)
                    .graphicType(WhiteBoardPanel.GraphicType.LINE)
                    .lineThickness(2)
                    .color(Color.RED);
            whiteBoard.addSerie(dataCPUSerie);

            whiteBoard.setTitleJustification(WhiteBoardPanel.TitleJustification.RIGHT);
            if (this.displayData.size() > 0) { // We need at least 1 point to make any sense.
                double lastTempValue = this.displayData.get(this.displayData.size() - 1).temperature;
                int red = (int) (255 * (lastTempValue / 100f));
                int green = 0;
                int blue = (int) (255 * ((100f - lastTempValue) / 100f));
                if (verbose) {
                    System.out.printf(">> rgb(%d, %d, %d)\n", red, green, blue);
                }
                whiteBoard.setTextColor(new Color(red, green, blue));
                whiteBoard.setGraphicTitle(String.format("%.01f\272C", lastTempValue));
            }
            // Finally, display it.
            whiteBoard.repaint();  // This is for a pure Swing context, not for a NoteBook.
        } catch (Exception ex) {
            System.err.println("Bam!");
            ex.printStackTrace();
        }
    }

    private void show() {
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
    }

    private void startGrabber() {
        Thread grabberThread = new Thread(() -> {
            while (true) {
                DataHolder dh = this.dataGrabber.get();
                this.displayData.add(dh);
                this.abscissa.add(System.currentTimeMillis());
                this.maxValue = Math.max(maxValue, dh.temperature);
                this.minValue = Math.min(minValue, dh.temperature);
                while (this.displayData.size() > bufferLength) {
                    this.displayData.remove(0);
                    this.abscissa.remove(0);
                }
                this.refreshData();
                try {
                    Thread.sleep(1_000L); // Slow down !
                } catch (InterruptedException ie) {
                    // Oops
                }
            }
        });
        grabberThread.start();
    }

    private void initComponents() {
        // The JFrame
        this.frame = new JFrame(TITLE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
//      System.out.printf("Default frame width %d height %d %n", frameSize.width, frameSize.height);
        frameSize.height = Math.min(frameSize.height, screenSize.height);
        frameSize.width = Math.min(frameSize.width, screenSize.width);

        if (frameSize.width == 0 || frameSize.height == 0) {
            frameSize = new Dimension(WIDTH, HEIGHT + 50 + 10); // 50: ... menu, title bar, etc. 10: button
            this.frame.setSize(frameSize);
        }
        this.frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Get to the data
        this.startGrabber();

        this.frame.setJMenuBar(menuBar);
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.menuFile.setText("File");
        this.menuFileExit.setText("Exit");
        this.menuFileExit.addActionListener(this::fileExit_ActionPerformed);
        this.menuHelp.setText("Help");
        this.menuHelpAbout.setText("About");
        this.menuHelpAbout.addActionListener(this::helpAbout_ActionPerformed);
        this.menuFile.add(menuFileExit);
        this.menuBar.add(menuFile);
        this.menuHelp.add(menuHelpAbout);
        this.menuBar.add(menuHelp);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        this.topLabel = new JLabel(TITLE);
        this.topLabel.setFont(new Font("Courier New", Font.ITALIC | Font.BOLD, 16));
        // Inset below used for left padding.
        topPanel.add(this.topLabel,
                new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(0, 10, 0, 0), 0, 0));

        this.frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        whiteBoard.setFrameGraphic(false);
        // x labels generator. The abscissa contains System.currentTimeMillis. Here we convert it into a date, formatted mm:ss.
        whiteBoard.setXLabelGenerator(x -> {
            long epoch = 0L;
            try {
                if (x < abscissa.size()) {
                    epoch = abscissa.get(x); // Corner case...
                }
            } catch (IndexOutOfBoundsException iobe) {
                System.err.println("Oops");
            }
            return SDF.format(new Date(epoch));
        });

        // >> HERE: Add the WitheBoard to the JFrame
        this.frame.getContentPane().add(whiteBoard, BorderLayout.CENTER);
//      this.frame.pack();
    }

    public SwingTemperatureMonitor() {
    }

    private final static String VERBOSE_PREFIX = "--verbose:";
    private final static String BUFFER_LENGTH_PREFIX = "--buffer-length:";
    private final static String BETWEEN_LOOPS_PREFIX = "--between-loops:";

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
                verbose = ("true".equalsIgnoreCase(arg.substring(VERBOSE_PREFIX.length())) || "y".equalsIgnoreCase(arg.substring(VERBOSE_PREFIX.length())));
            } else if (arg.startsWith(BUFFER_LENGTH_PREFIX)) {
                bufferLength = Integer.parseInt(arg.substring(BUFFER_LENGTH_PREFIX.length()));
            } else if (arg.startsWith(BETWEEN_LOOPS_PREFIX)) {
                betweenLoops = Long.parseLong(arg.substring(BETWEEN_LOOPS_PREFIX.length()));
            } else {
                System.err.printf("Un-managed prefix: %s\n", arg);
            }
        }

        System.out.println("----------------------------------------------");
        System.out.printf("Running from folder %s\n", System.getProperty("user.dir"));
        System.out.printf("Java Version %s\n", System.getProperty("java.version"));
        System.out.println("----------------------------------------------");
        System.out.printf("Verbose: %b\n", verbose);
        System.out.printf("Buffer Length: %d element(s)\n", bufferLength);
        System.out.printf("Between Loops: %d milliseconds\n", betweenLoops);
        System.out.println("----------------------------------------------");

        SwingTemperatureMonitor thisThing = new SwingTemperatureMonitor();  // This one has instantiated the white board
        thisThing.initComponents();

        // Override defaults (not mandatory)
        whiteBoard.setAxisColor(new Color(125, 0, 255, 255));
        whiteBoard.setWithGrid(false);
        whiteBoard.setBgColor(new Color(0, 250, 250, 40));
        whiteBoard.setGraphicTitle(null); // "X not equals Y, Y ampl enforced [0, 100]");
        whiteBoard.setSize(new Dimension(800, 600));
        whiteBoard.setTextColor(Color.RED);
        whiteBoard.setTitleFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        whiteBoard.setGraphicMargins(30);
        whiteBoard.setXEqualsY(false);   // Not the same scale.

        // Enforced Y amplitude
        whiteBoard.setForcedMinY(0d);
        whiteBoard.setForcedMaxY(100d);

        thisThing.refreshData();
        try {
            Thread.sleep(betweenLoops);
        } catch (InterruptedException ie) {
            // Absorb
        }
        // May require a delay... Hence the wait above.
        thisThing.show();
    }
}
