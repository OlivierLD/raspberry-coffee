package sunflower.gps;

import gnu.io.CommPortIdentifier;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.DumpUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class GPSReader implements SerialIOCallbacks {
    private boolean verbose = "true".equals(System.getProperty("gps.verbose", "false"));

    private List<String> filters = null;
    private Consumer<RMC> rmcConsumer = null;

    public GPSReader() {
        this(null);
    }

    public GPSReader(Consumer<RMC> rmcConsumer, String... filters) {
        if (rmcConsumer != null) {
            this.rmcConsumer = rmcConsumer;
        }
        this.filters = Arrays.asList(filters);
    }

    private void setSentenceFilter(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public void connected(boolean b) {
        System.out.println("GPS connected: " + b);
    }

    private int lenToRead = 0;
    private int bufferIdx = 0;
    private byte[] serialBuffer = new byte[256];

    @Override
    public void onSerialData(byte b) {
//  System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
        serialBuffer[bufferIdx++] = (byte) (b & 0xFF);
        if (b == 0xA) { // \n
            // Message completed
            byte[] mess = new byte[bufferIdx];
            for (int i = 0; i < bufferIdx; i++) {
                mess[i] = serialBuffer[i];
            }
            serialOutput(mess);
            // Reset
            lenToRead = 0;
            bufferIdx = 0;
        }
    }

    @Override
    public void onSerialData(byte[] b, int len) {
    }

    /**
     * Formatting the data read by {@link #onSerialData(byte)}
     * @param mess
     */
    public void serialOutput(byte[] mess) {
        if (verbose) { // verbose...
            try {
                String[] sa = DumpUtil.dualDump(mess);
                if (sa != null) {
                    System.out.println("\t>>> [From GPS] Received:");
                    for (String s : sa) {
                        System.out.println("\t\t" + s);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // Process
        String sentence = new String(mess);
        String id = sentence.substring(3, 6);
        if (filters == null || filters.contains(id)) {
            if (verbose) {
                System.out.print(sentence); // Regular output
            }
            if ("RMC".equals(id)) {
                RMC rmc = StringParsers.parseRMC(sentence);
//                Date rmcDate = rmc.getRmcDate();
                if (rmcConsumer != null) {
                    rmcConsumer.accept(rmc);
                }
            }
        }
    }

    final Thread thread = Thread.currentThread();

    public void stopReading() {
        try {
            synchronized (thread) {
                thread.notify();
                Thread.sleep(1_000L);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startReading(String portName, int baudRate) {
        SerialCommunicator sc = new SerialCommunicator(this);
        sc.setVerbose(false);

        Map<String, CommPortIdentifier> pm = sc.getPortList();
        Set<String> ports = pm.keySet();
        if (ports.size() == 0) {
            System.out.println("No serial port found.");
            System.out.println("Did you run as administrator (sudo) ?");
        }
        if (verbose) {
            System.out.println("== Serial Port List ==");
            for (String port : ports) {
                System.out.println("-> " + port);
            }
            System.out.println("======================");
        }

        System.out.println(String.format("Opening port %s:%d", portName, baudRate));
        CommPortIdentifier serialPort = pm.get(portName);
        if (serialPort == null) {
            System.out.println(String.format("Port %s not found, aborting", portName));
            System.exit(1);
        }

        try {
            sc.connect(serialPort, "GPS", baudRate);
            boolean b = sc.initIOStream();
            System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
            sc.initListener();

            synchronized (thread) {
                try {
                    thread.wait();
                    System.out.println("\nGPS Notified.");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            sc.disconnect();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static {
        SDF_DATETIME.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    /**
     * Standalone test
     * @param args
     */
    public static void main(String... args) {
        final GPSReader gpsReader = new GPSReader(date -> {
            System.out.println(SDF_DATETIME.format(date));
        }, "RMC");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gpsReader.stopReading();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, "Shutdown Hook"));

        String defaultValue = "/dev/tty.usbmodem141101";
        // "/dev/ttyUSB0"
        gpsReader.startReading(defaultValue, 4_800);

        System.out.println("Done.");
    }
}
