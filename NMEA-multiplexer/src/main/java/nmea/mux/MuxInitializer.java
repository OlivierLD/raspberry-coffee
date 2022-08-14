package nmea.mux;

import context.ApplicationContext;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAReader;
import nmea.computers.Computer;
import nmea.computers.ExtraDataComputer;
import nmea.consumers.client.*;
import nmea.consumers.reader.*;
import nmea.forwarders.*;
import nmea.forwarders.rmi.RMIServer;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Initialize the configuration of the Multiplexer, at startup,
 * with the properties read from the file system, through one
 * package-private method named <code>{@link #setup}</code>.
 * <br/>
 * Initializes:
 * <ul>
 *   <li>NMEA Channels</li>
 *   <li>NMEA Forwarders</li>
 *   <li>NMEA Computers</li>
 * </ul>
 * All those objects can be also managed later on, through the REST Admin Interface
 * <br/>
 * (see {@link RESTImplementation}).
 */
public class MuxInitializer {

    private final static NumberFormat MUX_IDX_FMT = new DecimalFormat("00");

    public static void setup(Properties muxProps,
                             List<NMEAClient> nmeaDataClients,
                             List<Forwarder> nmeaDataForwarders,
                             List<Computer> nmeaDataComputers,
                             Multiplexer mux) {
        setup(muxProps, nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, mux, false);
    }

    /**
     * This is the method to call to initialize the {@link Multiplexer}.
     * The 3 <code>List</code>s must have been created in it, as they will be populated here.
     *
     * @param muxProps           The properties to get the data from. See <a href="../../../../README.md">here</a> for more details.
     * @param nmeaDataClients    List of the input channels
     * @param nmeaDataForwarders List of the output channels
     * @param nmeaDataComputers  List of the data computers
     * @param mux                the Multiplexer instance to initialize
     * @param verbose            Speak up!
     */
    public static void setup(Properties muxProps,
                             List<NMEAClient> nmeaDataClients,
                             List<Forwarder> nmeaDataForwarders,
                             List<Computer> nmeaDataComputers,
                             Multiplexer mux,
                             boolean verbose) {
        int muxIdx = 1;
        boolean thereIsMore = true;
        // 1 - Input channels
        while (thereIsMore) {
            String classProp = String.format("mux.%s.class", MUX_IDX_FMT.format(muxIdx));
            String clss = muxProps.getProperty(classProp);
            if (clss != null) { // Dynamic loading
                if (verbose) {
                    System.out.printf("\t>> %s - Dynamic loading for input channel %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                }
                try {
                    // Devices and Sentences filters.
                    String deviceFilters = "";
                    String sentenceFilters = "";
                    deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    Object dynamic = Class.forName(clss)
                            .getDeclaredConstructor(String[].class, String[].class, Multiplexer.class)
                            .newInstance(
                                    !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                    !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                    mux);
                    if (dynamic instanceof NMEAClient) {
                        NMEAClient nmeaClient = (NMEAClient) dynamic;
                        String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                        String propFileName = muxProps.getProperty(propProp);
                        Properties readerProperties = null;
                        if (propFileName != null) {
                            try {
                                readerProperties = new Properties();
                                readerProperties.load(new FileReader(propFileName));
                                nmeaClient.setProperties(readerProperties);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        nmeaClient.initClient();
                        NMEAReader reader = null;
                        try {
                            String readerProp = String.format("mux.%s.reader", MUX_IDX_FMT.format(muxIdx));
                            String readerClass = muxProps.getProperty(readerProp);
                            // Cannot invoke declared constructor with a generic type... :(
                            if (readerProperties == null) {
                                reader = (NMEAReader) Class.forName(readerClass).getDeclaredConstructor(String.class, List.class).newInstance(readerProp, nmeaClient.getListeners());
                            } else {
                                reader = (NMEAReader) Class.forName(readerClass).getDeclaredConstructor(String.class, List.class, Properties.class).newInstance(readerProp, nmeaClient.getListeners(), readerProperties);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (reader != null) {
                            nmeaClient.setReader(reader);
                        }
                        nmeaDataClients.add(nmeaClient);
                    } else {
                        throw new RuntimeException(String.format("Expected an NMEAClient, found a [%s]", dynamic.getClass().getName()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                String typeProp = String.format("mux.%s.type", MUX_IDX_FMT.format(muxIdx));
                String type = muxProps.getProperty(typeProp);
                if (type == null) {
                    thereIsMore = false;
                } else {
                    if (verbose) {
                        System.out.printf("\t>> %s - Loading channel %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                    }
                    String deviceFilters = "";
                    String sentenceFilters = "";
                    switch (type) {
                        case "serial":
                            try {
                                String serialPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
                                String br = muxProps.getProperty(String.format("mux.%s.baudrate", MUX_IDX_FMT.format(muxIdx)));
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String resetIntervalStr = muxProps.getProperty(String.format("mux.%s.reset.interval", MUX_IDX_FMT.format(muxIdx)));
                                Long resetInterval = null;
                                if (resetIntervalStr != null) {
                                    try {
                                        resetInterval = Long.parseLong(resetIntervalStr);
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
                                    }
                                }
                                SerialClient serialClient = new SerialClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                serialClient.initClient();
                                serialClient.setReader(new SerialReader("MUX-SerialReader", serialClient.getListeners(), serialPort, Integer.parseInt(br), resetInterval));
                                serialClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(serialClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "tcp":
                            try {
                                String tcpPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
                                String tcpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                TCPClient tcpClient = new TCPClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                tcpClient.initClient();
                                tcpClient.setReader(new TCPReader("MUX-TCPReader", tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort)));
                                tcpClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(tcpClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "file":
//                            System.out.println("MUX Props:\n" + muxProps);
                            try {
                                String filename = muxProps.getProperty(String.format("mux.%s.filename", MUX_IDX_FMT.format(muxIdx)));
                                long betweenRec = 500;
                                boolean zip = false;
                                String pathInArchive = null;
                                try {
                                    betweenRec = Long.parseLong(muxProps.getProperty(String.format("mux.%s.between-records", MUX_IDX_FMT.format(muxIdx)), "500"));
                                } catch (NumberFormatException nfe) {
                                    betweenRec = 500; // Default value
                                }
                                try {
                                    zip = "true".equals(muxProps.getProperty(String.format("mux.%s.zip", MUX_IDX_FMT.format(muxIdx)), "false"));
                                } catch (NumberFormatException nfe) {
                                    zip = false; // Default value
                                }
                                try {
                                    pathInArchive = muxProps.getProperty(String.format("mux.%s.path.in.zip", MUX_IDX_FMT.format(muxIdx)));
                                } catch (NumberFormatException nfe) {
                                    pathInArchive = null; // Default value
                                }
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                System.out.printf("From props: %s=%s%n",
//                                        String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx)),
//                                        muxProps.getProperty(String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx))));
                                boolean loop = "true".equals(muxProps.getProperty(String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx)), "true").trim());
                                DataFileClient fileClient = new DataFileClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                fileClient.initClient();
								fileClient.setLoop(loop);
                                fileClient.setReader(new DataFileReader("MUX-FileReader", fileClient.getListeners(), filename, betweenRec, loop, zip, pathInArchive));
                                fileClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                fileClient.setZip(zip);
                                fileClient.setPathInArchive(pathInArchive);
                                nmeaDataClients.add(fileClient);
//                                System.out.printf(">>> Loop: %b", fileClient.isLoop()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "ws":
                            try {
                                String wsUri = muxProps.getProperty(String.format("mux.%s.wsuri", MUX_IDX_FMT.format(muxIdx)));
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                WebSocketClient wsClient = new WebSocketClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                wsClient.initClient();
                                wsClient.setReader(new WebSocketReader("MUX-WSReader", wsClient.getListeners(), wsUri));
                                wsClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(wsClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "htu21df": // Humidity & Temperature sensor
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String htu21dfDevicePrefix = muxProps.getProperty(String.format("mux.%s.device.prefix", MUX_IDX_FMT.format(muxIdx)), "");
                                HTU21DFClient htu21dfClient = new HTU21DFClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                htu21dfClient.initClient();
                                htu21dfClient.setReader(new HTU21DFReader("MUX-HTU21DFReader", htu21dfClient.getListeners()));
                                htu21dfClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                // Important: after the setReader
                                if (!htu21dfDevicePrefix.trim().isEmpty()) {
                                    if (htu21dfDevicePrefix.trim().length() == 2) {
                                        htu21dfClient.setSpecificDevicePrefix(htu21dfDevicePrefix.trim());
                                    } else {
                                        throw new RuntimeException(String.format("Bad prefix [%s] for HTU21DF. Must be 2 character long, exactly.", htu21dfDevicePrefix.trim()));
                                    }
                                }
                                nmeaDataClients.add(htu21dfClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "rnd": // Random generator, for debugging
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                RandomClient rndClient = new RandomClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                rndClient.initClient();
                                rndClient.setReader(new RandomReader("MUX-RndReader", rndClient.getListeners()));
                                rndClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(rndClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "zda": // ZDA generator
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                ZDAClient zdaClient = new ZDAClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                zdaClient.initClient();
                                zdaClient.setReader(new ZDAReader("MUX-ZDAReader", zdaClient.getListeners()));
                                zdaClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(zdaClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "hmc5883l": // Heading, Pitch & Roll. Requires calibration
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String hmc5883lDevicePrefix = muxProps.getProperty(String.format("mux.%s.device.prefix", MUX_IDX_FMT.format(muxIdx)), "");
                                // Calibration properties
                                String hmc5883lCalPropFileName = muxProps.getProperty(String.format("mux.%s.hmc5883l.cal.prop.file", MUX_IDX_FMT.format(muxIdx)), "hmc5883l.cal.properties");
                                System.setProperty("hmc5883l.cal.prop.file", hmc5883lCalPropFileName);
                                int headingOffset = 0;
                                try {
                                    headingOffset = Integer.parseInt(muxProps.getProperty(String.format("mux.%s.heading.offset", MUX_IDX_FMT.format(muxIdx)), "0"));
                                    if (headingOffset > 180 || headingOffset < -180) {
                                        System.err.printf("Warning: Bad range for Heading Offset, must be in [-180..180], found %d. Defaulting to 0\n", headingOffset);
                                        headingOffset = 0;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                Long readFrequency = null;
                                try {
                                    readFrequency = Long.parseLong(muxProps.getProperty(String.format("mux.%s.read.frequency", MUX_IDX_FMT.format(muxIdx))));
                                    if (readFrequency < 0) {
                                        System.err.printf("Warning: Bad value for Read Frequency, must be positive, found %d.\n", readFrequency);
                                        readFrequency = null;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                Integer dampingSize = null;
                                try {
                                    dampingSize = Integer.parseInt(muxProps.getProperty(String.format("mux.%s.damping.size", MUX_IDX_FMT.format(muxIdx))));
                                    if (dampingSize < 0) {
                                        System.err.printf("Warning: Bad value for Damping Size, must be positive, found %d.\n", dampingSize);
                                        dampingSize = null;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                if (verbose) {
                                    System.out.printf("For HMC5883L channel: deviceFilters: %s, sentenceFilters: %s, devicePrefix: %s, headingOffset: %d, readFrequency: %d, dampingSize: %d\n",
                                            deviceFilters,
                                            sentenceFilters,
                                            hmc5883lDevicePrefix,
                                            headingOffset,
                                            readFrequency,
                                            dampingSize);
                                }
                                HMC5883LClient hmc5883lClient = new HMC5883LClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                hmc5883lClient.initClient();
                                hmc5883lClient.setReader(new HMC5883LReader("MUX-HMC5883LReader", hmc5883lClient.getListeners()));
                                hmc5883lClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                // Important: after the setReader
                                if (headingOffset != 0) {
                                    hmc5883lClient.setHeadingOffset(headingOffset);
                                }
                                if (readFrequency != null) {
                                    hmc5883lClient.setReadFrequency(readFrequency);
                                }
                                if (dampingSize != null) {
                                    hmc5883lClient.setDampingSize(dampingSize);
                                }
                                if (!hmc5883lDevicePrefix.trim().isEmpty()) {
                                    if (hmc5883lDevicePrefix.trim().length() == 2) {
                                        hmc5883lClient.setSpecificDevicePrefix(hmc5883lDevicePrefix.trim());
                                    } else {
                                        throw new RuntimeException(String.format("Bad prefix [%s] for HMC5883L. Must be 2 character long, exactly.", hmc5883lDevicePrefix.trim()));
                                    }
                                }
                                nmeaDataClients.add(hmc5883lClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "lsm303": // Pitch & Roll, plus Heading (possibly). Requires calibration.
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String lsm303DevicePrefix = muxProps.getProperty(String.format("mux.%s.device.prefix", MUX_IDX_FMT.format(muxIdx)), "");
                                String lsm303DeviceFeature = muxProps.getProperty(String.format("mux.%s.device.feature", MUX_IDX_FMT.format(muxIdx)), "BOTH"); // BOTH, MAGNETOMETER, or ACCELEROMETER
                                // Calibration properties
                                String lsm303CalPropFileName = muxProps.getProperty(String.format("mux.%s.lsm303.cal.prop.file", MUX_IDX_FMT.format(muxIdx)), "lsm303.cal.properties");
                                System.setProperty("lsm303.cal.prop.file", lsm303CalPropFileName);
                                int headingOffset = 0;
                                try {
                                    headingOffset = Integer.parseInt(muxProps.getProperty(String.format("mux.%s.heading.offset", MUX_IDX_FMT.format(muxIdx)), "0"));
                                    if (headingOffset > 180 || headingOffset < -180) {
                                        System.err.printf("Warning: Bad range for Heading Offset, must be in [-180..180], found %d. Defaulting to 0\n", headingOffset);
                                        headingOffset = 0;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                Long readFrequency = null;
                                try {
                                    readFrequency = Long.parseLong(muxProps.getProperty(String.format("mux.%s.read.frequency", MUX_IDX_FMT.format(muxIdx))));
                                    if (readFrequency < 0) {
                                        System.err.printf("Warning: Bad value for Read Frequency, must be positive, found %d.\n", readFrequency);
                                        readFrequency = null;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                Integer dampingSize = null;
                                try {
                                    dampingSize = Integer.parseInt(muxProps.getProperty(String.format("mux.%s.damping.size", MUX_IDX_FMT.format(muxIdx))));
                                    if (dampingSize < 0) {
                                        System.err.printf("Warning: Bad value for Damping Size, must be positive, found %d.\n", dampingSize);
                                        dampingSize = null;
                                    }
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                                if (verbose) {
                                    System.out.printf("For LSM303 channel: deviceFilters: %s, sentenceFilters: %s, devicePrefix: %s, headingOffset: %d, readFrequency: %d, dampingSize: %d\n",
                                            deviceFilters,
                                            sentenceFilters,
                                            lsm303DevicePrefix,
                                            headingOffset,
                                            readFrequency,
                                            dampingSize);
                                }
                                LSM303Client lsm303Client = new LSM303Client(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                lsm303Client.initClient();
                                lsm303Client.setReader(new LSM303Reader("MUX-LSM303Reader", lsm303Client.getListeners()));
                                lsm303Client.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                // Important: after the setReader
                                if (headingOffset != 0) {
                                    lsm303Client.setHeadingOffset(headingOffset);
                                }
                                if (readFrequency != null) {
                                    lsm303Client.setReadFrequency(readFrequency);
                                }
                                if (dampingSize != null) {
                                    lsm303Client.setDampingSize(dampingSize);
                                }
                                if (!lsm303DevicePrefix.trim().isEmpty()) {
                                    if (lsm303DevicePrefix.trim().length() == 2) {
                                        lsm303Client.setSpecificDevicePrefix(lsm303DevicePrefix.trim());
                                    } else {
                                        throw new RuntimeException(String.format("Bad prefix [%s] for LSM303. Must be 2 character long, exactly.", lsm303DevicePrefix.trim()));
                                    }
                                }
                                if (!lsm303DeviceFeature.trim().isEmpty()) {
                                    try {
                                        lsm303Client.setDeviceFeature(lsm303DeviceFeature.trim());
                                    } catch (Exception ex) {
                                        throw ex;
                                    }
                                }
                                nmeaDataClients.add(lsm303Client);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "bme280": // Humidity, Temperature, Pressure
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String bme280DevicePrefix = muxProps.getProperty(String.format("mux.%s.device.prefix", MUX_IDX_FMT.format(muxIdx)), "");
                                BME280Client bme280Client = new BME280Client(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                bme280Client.initClient();
                                bme280Client.setReader(new BME280Reader("MUX-BME280", bme280Client.getListeners()));
                                bme280Client.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                // Important: after the setReader
                                if (!bme280DevicePrefix.trim().isEmpty()) {
                                    if (bme280DevicePrefix.trim().length() == 2) {
                                        bme280Client.setSpecificDevicePrefix(bme280DevicePrefix.trim());
                                    } else {
                                        throw new RuntimeException(String.format("Bad prefix [%s] for BME280. Must be 2 character long, exactly.", bme280DevicePrefix.trim()));
                                    }
                                }
                                nmeaDataClients.add(bme280Client);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "bmp180": // Temperature, Pressure
                            try {
                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String bmp180DevicePrefix = muxProps.getProperty(String.format("mux.%s.device.prefix", MUX_IDX_FMT.format(muxIdx)), "");
                                BMP180Client bmp180Client = new BMP180Client(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                bmp180Client.initClient();
                                bmp180Client.setReader(new BMP180Reader("MUX-BMP180Reader", bmp180Client.getListeners()));
                                bmp180Client.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                // Important: after the setReader
                                if (!bmp180DevicePrefix.trim().isEmpty()) {
                                    if (bmp180DevicePrefix.trim().length() == 2) {
                                        bmp180Client.setSpecificDevicePrefix(bmp180DevicePrefix.trim());
                                    } else {
                                        throw new RuntimeException(String.format("Bad prefix [%s] for BMP180. Must be 2 character long, exactly.", bmp180DevicePrefix.trim()));
                                    }
                                }
                                nmeaDataClients.add(bmp180Client);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "batt":   // Battery Voltage, use XDR
                        default:
                            throw new RuntimeException(String.format("mux type [%s] not supported yet.", type));
                    }
                }
            }
            muxIdx++;
        }
        if (verbose) {
            System.out.printf("\t>> %s - Done with input channels\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
        }

        // Data Cache
        if ("true".equals(muxProps.getProperty("init.cache", "false"))) {
            try {
                if (verbose) {
                    System.out.printf("\t>> %s - Initializing Cache\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
                }
                String deviationFile = muxProps.getProperty("deviation.file.name", "zero-deviation.csv");
                double maxLeeway = Double.parseDouble(muxProps.getProperty("max.leeway", "0"));
                double bspFactor = Double.parseDouble(muxProps.getProperty("bsp.factor", "1"));
                double awsFactor = Double.parseDouble(muxProps.getProperty("aws.factor", "1"));
                double awaOffset = Double.parseDouble(muxProps.getProperty("awa.offset", "0"));
                double hdgOffset = Double.parseDouble(muxProps.getProperty("hdg.offset", "0"));
                double defaultDeclination = Double.parseDouble(muxProps.getProperty("default.declination", "0"));
                int damping = Integer.parseInt(muxProps.getProperty("damping", "1"));
                ApplicationContext.getInstance().initCache(deviationFile, maxLeeway, bspFactor, awsFactor, awaOffset, hdgOffset, defaultDeclination, damping);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        thereIsMore = true;
        int fwdIdx = 1;
        // 2 - Output channels, aka forwarders
        while (thereIsMore) {
            String classProp = String.format("forward.%s.class", MUX_IDX_FMT.format(fwdIdx));
            String clss = muxProps.getProperty(classProp);
            if (clss != null) { // Dynamic loading
                if (verbose) {
                    System.out.printf("\t>> %s - Dynamic loading for output %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                }
                try {
                    Object dynamic = Class.forName(clss).getDeclaredConstructor().newInstance();
                    if (dynamic instanceof Forwarder) {
                        Forwarder forwarder = (Forwarder) dynamic;
                        String propProp = String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx));
                        String propFileName = muxProps.getProperty(propProp);
                        if (propFileName != null) {
                            try {
                                Properties properties = new Properties();
                                properties.load(new FileReader(propFileName));
                                forwarder.setProperties(properties);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        forwarder.init();
                        nmeaDataForwarders.add(forwarder);
                    } else {
                        throw new RuntimeException(String.format("Expected a Forwarder, found a [%s]", dynamic.getClass().getName()));
                    }
                } catch (Exception ioe) {
                    // Some I2C device was not found?
                    System.err.println("---------------------------");
                    ioe.printStackTrace();
                    System.err.println("---------------------------");
                } catch (Throwable ex) {
                    System.err.println("===========================");
                    System.err.println("Classpath: " + System.getProperty("java.class.path"));
                    ex.printStackTrace();
                    System.err.println("===========================");
                }
            } else {
                String typeProp = String.format("forward.%s.type", MUX_IDX_FMT.format(fwdIdx));
                String type = muxProps.getProperty(typeProp);
                if (type == null) {
                    thereIsMore = false;
                } else {
                    if (verbose) {
                        System.out.printf("\t>> %s - Loading for output channel %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                    }
                    switch (type) {
                        case "serial":
                            String serialPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            int baudrate = Integer.parseInt(muxProps.getProperty(String.format("forward.%s.baudrate", MUX_IDX_FMT.format(fwdIdx))));
                            String propFileSerial = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String serialSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder serialForwarder;
                                if (serialSubClass == null) {
                                    serialForwarder = new SerialWriter(serialPort, baudrate);
                                } else {
                                    serialForwarder = (SerialWriter) Class.forName(serialSubClass.trim()).getConstructor(String.class, Integer.class).newInstance(serialPort, baudrate);
                                }
                                if (propFileSerial != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(propFileSerial));
                                    serialForwarder.setProperties(forwarderProps);
                                }
                                serialForwarder.init();
                                nmeaDataForwarders.add(serialForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "tcp":
                            String tcpPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String tcpPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String tcpSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder tcpForwarder;
                                if (tcpSubClass == null) {
                                    tcpForwarder = new TCPServer(Integer.parseInt(tcpPort));
                                } else {
                                    tcpForwarder = (TCPServer) Class.forName(tcpSubClass.trim()).getConstructor(Integer.class).newInstance(Integer.parseInt(tcpPort));
                                }
                                if (tcpPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(tcpPropFile));
                                    tcpForwarder.setProperties(forwarderProps);
                                }
                                tcpForwarder.init();
                                nmeaDataForwarders.add(tcpForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "rest":
                            String restPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String restSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String verboseStr = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));

                            List<String> properties = Arrays.asList(
                                    "server.name", "server.port", "rest.resource", "rest.verb", "http.headers", "rest.protocol"
                            );
                            final int idx = fwdIdx;
                            Properties configProps = new Properties();
                            if (verboseStr != null) {
                                System.out.printf("Setting verbose to %s (%s)\n", verboseStr, verboseStr.trim());
                                configProps.put("verbose", verboseStr.trim());
                            }
                            properties.forEach(prop -> {
                                String propVal = muxProps.getProperty(String.format("forward.%s.%s", MUX_IDX_FMT.format(idx), prop));
                                if (propVal != null) {
                                    configProps.put(prop, propVal);
                                }
                            });
//							props.put("server.name", "192.168.42.6");
//							props.put("server.port", "8080");
//							props.put("rest.resource", "/rest/endpoint?qs=prm");
//							props.put("rest.verb", "POST");
//							props.put("rest.protocol", "http[s]");
//							props.put("http.headers", "Content-Type:plain/text");
                            try {
                                Forwarder restForwarder;
                                if (restSubClass == null) {
                                    restForwarder = new RESTPublisher();
                                } else {
                                    restForwarder = (RESTPublisher) Class.forName(restSubClass.trim()).getConstructor(Integer.class).newInstance();
                                }
                                if (restPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(restPropFile));
                                    restForwarder.setProperties(forwarderProps);
                                }
                                if ("true".equals(System.getProperty("mux.props.verbose"))) {
                                    System.out.printf("Props for forwarder %s\n", restForwarder.getClass().getName());
                                    configProps.forEach((name, value) -> System.out.printf("%s : %s\n", name, value));
                                }
                                restForwarder.setProperties(configProps);
                                restForwarder.init();
                                nmeaDataForwarders.add(restForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "gpsd":
                            String gpsdPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String gpsdPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String gpsdSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder gpsdForwarder;
                                if (gpsdSubClass == null) {
                                    gpsdForwarder = new GPSdServer(Integer.parseInt(gpsdPort));
                                } else {
                                    gpsdForwarder = (GPSdServer) Class.forName(gpsdSubClass.trim()).getConstructor(Integer.class).newInstance(Integer.parseInt(gpsdPort));
                                }
                                if (gpsdPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(gpsdPropFile));
                                    gpsdForwarder.setProperties(forwarderProps);
                                }
                                gpsdForwarder.init();
                                nmeaDataForwarders.add(gpsdForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "file":
                            String fName = muxProps.getProperty(String.format("forward.%s.filename", MUX_IDX_FMT.format(fwdIdx)), "data.nmea");
                            boolean append = "true".equals(muxProps.getProperty(String.format("forward.%s.append", MUX_IDX_FMT.format(fwdIdx)), "false"));
                            boolean timeBased = "true".equals(muxProps.getProperty(String.format("forward.%s.timebase.filename", MUX_IDX_FMT.format(fwdIdx)), "false"));
                            String propFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String fSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String radix = muxProps.getProperty(String.format("forward.%s.filename.suffix", MUX_IDX_FMT.format(fwdIdx)));
                            String logDir = muxProps.getProperty(String.format("forward.%s.log.dir", MUX_IDX_FMT.format(fwdIdx)));
                            String split = muxProps.getProperty(String.format("forward.%s.split", MUX_IDX_FMT.format(fwdIdx)));
                            String flush = muxProps.getProperty(String.format("forward.%s.flush", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder fileForwarder;
                                if (fSubClass == null) {
                                    fileForwarder = new DataFileWriter(fName, append, timeBased, radix, logDir, split, "true".equals(flush));
                                } else {
                                    try {
                                        fileForwarder = (DataFileWriter) Class.forName(fSubClass.trim())
                                                .getConstructor(String.class, Boolean.class, Boolean.class, String.class, String.class, String.class, Boolean.class)
                                                .newInstance(fName, append, timeBased, radix, logDir, split, "true".equals(flush));
                                    } catch (NoSuchMethodException nsme) {
                                        fileForwarder = (DataFileWriter) Class.forName(fSubClass.trim()) // Fallback on previous constructor
                                                .getConstructor(String.class, Boolean.class)
                                                .newInstance(fName, append);
                                    }
                                }
                                if (propFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(propFile));
                                    fileForwarder.setProperties(forwarderProps);
                                }
                                fileForwarder.init();
                                nmeaDataForwarders.add(fileForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "ws":
                            String wsUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
                            String wsPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String wsSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder wsForwarder;
                                if (wsSubClass == null) {
                                    wsForwarder = new WebSocketWriter(wsUri);
                                } else {
                                    wsForwarder = (WebSocketWriter) Class.forName(wsSubClass.trim()).getConstructor(String.class).newInstance(wsUri);
                                }
                                if (wsPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(wsPropFile));
                                    wsForwarder.setProperties(forwarderProps);
                                }
                                wsForwarder.init();
                                nmeaDataForwarders.add(wsForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "wsp":
                            String wspUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
                            String wspPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String wspSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder wspForwarder;
                                if (wspSubClass == null) {
                                    wspForwarder = new WebSocketProcessor(wspUri);
                                } else {
                                    wspForwarder = (WebSocketProcessor) Class.forName(wspSubClass.trim()).getConstructor(String.class).newInstance(wspUri);
                                }
                                if (wspPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(wspPropFile));
                                    wspForwarder.setProperties(forwarderProps);
                                }
                                wspForwarder.init();
                                nmeaDataForwarders.add(wspForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "console":
                            try {
                                String consolePropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                                String consoleSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                                Forwarder consoleForwarder = new ConsoleWriter();
                                if (consoleSubClass == null) {
                                    consoleForwarder = new ConsoleWriter();
                                } else {
                                    consoleForwarder = (ConsoleWriter) Class.forName(consoleSubClass.trim()).getConstructor().newInstance();
                                }
                                if (consolePropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(consolePropFile));
                                    consoleForwarder.setProperties(forwarderProps);
                                }
                                consoleForwarder.init();
                                nmeaDataForwarders.add(consoleForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "rmi":
                            String rmiPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String rmiName = muxProps.getProperty(String.format("forward.%s.name", MUX_IDX_FMT.format(fwdIdx)));
                            String rmiPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String subClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx))); // TODO Manage that one...
                            try {
                                Forwarder rmiServerForwarder;
                                if (rmiName != null && !rmiName.trim().isEmpty()) {
                                    rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort), rmiName);
                                } else {
                                    rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort));
                                }
                                if (rmiPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(rmiPropFile));
                                    rmiServerForwarder.setProperties(forwarderProps);
                                }
                                rmiServerForwarder.init();
                                nmeaDataForwarders.add(rmiServerForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        default:
                            throw new RuntimeException(String.format("forward type [%s] not supported yet.", type));
                    }
                }
            }
            fwdIdx++;
        }
        if (verbose) {
            System.out.printf("\t>> %s - Don with forwarders\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
        }
        // Init cache (for Computers).
        if ("true".equals(muxProps.getProperty("init.cache", "false"))) {
            try {
                // If there is a cache, then let's see what computers to start.
                thereIsMore = true;
                int cptrIdx = 1;
                // 3 - Computers
                while (thereIsMore) {
                    String classProp = String.format("computer.%s.class", MUX_IDX_FMT.format(cptrIdx));
                    String clss = muxProps.getProperty(classProp);
                    if (clss != null) { // Dynamic loading
                        if (verbose) {
                            System.out.printf("\t>> %s - Dynamic loading for computer %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                        }
                        try {
                            Object dynamic = Class.forName(clss).getDeclaredConstructor(Multiplexer.class).newInstance(mux);
                            if (dynamic instanceof Computer) {
                                Computer computer = (Computer) dynamic;
                                String propProp = String.format("computer.%s.properties", MUX_IDX_FMT.format(cptrIdx));
                                Properties properties = new Properties();
                                properties.put("verbose", String.valueOf("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx))))));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        properties.load(new FileReader(propFileName));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                computer.setProperties(properties);
                                nmeaDataComputers.add(computer);
                            } else {
                                throw new RuntimeException(String.format("Expected a Computer, found a [%s]", dynamic.getClass().getName()));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        String typeProp = String.format("computer.%s.type", MUX_IDX_FMT.format(cptrIdx));
                        String type = muxProps.getProperty(typeProp);
                        if (type == null) {
                            thereIsMore = false;
                        } else {
                            if (verbose) {
                                System.out.printf("\t>> %s - Loading computer %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                            }
                            switch (type) {
                                case "tw-current": // True Wind and Current computer. True Wind is calculated with GPS COG & SOG), as it should. Also involves the LongTimeCurrentCalculator.
                                    String prefix = muxProps.getProperty(String.format("computer.%s.prefix", MUX_IDX_FMT.format(cptrIdx)), "OS");
                                    String[] timeBuffers = muxProps.getProperty(String.format("computer.%s.time.buffer.length", MUX_IDX_FMT.format(cptrIdx)), "600000").split(",");
                                    List<Long> timeBufferLengths = Arrays.stream(timeBuffers).map(tbl -> Long.parseLong(tbl.trim())).collect(Collectors.toList());
                                    // Check duplicates
                                    for (int i = 0; i < timeBufferLengths.size() - 1; i++) {
                                        for (int j = i + 1; j < timeBufferLengths.size(); j++) {
                                            if (timeBufferLengths.get(i).equals(timeBufferLengths.get(j))) {
                                                throw new RuntimeException(String.format("Duplicates in time buffer lengths: %d ms.", timeBufferLengths.get(i)));
                                            }
                                        }
                                    }
                                    try {
                                        Computer twCurrentComputer = new ExtraDataComputer(mux, prefix, timeBufferLengths.toArray(new Long[timeBufferLengths.size()]));
                                        twCurrentComputer.setVerbose("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx)))));
                                        nmeaDataComputers.add(twCurrentComputer);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                default:
                                    System.err.printf("Computer type [%s] not supported.\n", type);
                                    break;
                            }
                        }
                    }
                    cptrIdx++;
                }
                if (verbose) {
                    System.out.printf("\t>> %s - Done with computers\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Properties yamlToProperties(Map<String, Object> yamlMap) {
        Properties properties = new Properties();

        yamlMap.keySet().forEach(k -> {
//		System.out.printf("%s -> %s", k, yamlMap.get(k).getClass().getName()));
            switch (k) {
                case "name":
                    System.out.printf("Definition Name: %s\n", yamlMap.get(k));
                    break;
                case "context":
                    Map<String, Object> context = (Map<String, Object>) yamlMap.get(k);
                    context.keySet().forEach(ck -> properties.setProperty(ck, context.get(ck).toString()));
                    System.out.println(context);
                    break;
                case "channels":
                    List<Map<String, Object>> channels = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbChannels = new AtomicInteger(0);
                    channels.forEach(channel -> {
                        int nb = nbChannels.incrementAndGet();
                        channel.keySet().forEach(channelKey -> {
                            String propName = String.format("mux.%02d.%s", nb, channelKey);
                            properties.setProperty(propName, channel.get(channelKey).toString());
                            if ("yes".equals(System.getProperty("yaml.tx.verbose", "no"))) {
                                System.out.printf("Setting [%s] to [%s]\n", propName, channel.get(channelKey).toString());
                            }
                        });
                    });
                    break;
                case "forwarders":
                    List<Map<String, Object>> forwarders = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbForwarders = new AtomicInteger(0);
                    forwarders.forEach(channel -> {
                        int nb = nbForwarders.incrementAndGet();
                        channel.keySet().forEach(forwardKey -> {
                            String propName = String.format("forward.%02d.%s", nb, forwardKey);
                            properties.setProperty(propName, channel.get(forwardKey).toString());
                        });
                    });
                    break;
                case "computers":
                    List<Map<String, Object>> computers = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbComputers = new AtomicInteger(0);
                    computers.forEach(channel -> {
                        int nb = nbComputers.incrementAndGet();
                        channel.keySet().forEach(computeKey -> {
                            String propName = String.format("computer.%02d.%s", nb, computeKey);
                            properties.setProperty(propName, channel.get(computeKey).toString());
                        });
                    });
                    break;
                default:
                    break;
            }
        });

        // For tests
        if ("yes".equals(System.getProperty("yaml.tx.verbose", "no"))) {
            try {
                properties.store(new FileOutputStream("multiplexer.properties"), "Generated from yaml");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return properties;
    }
}
