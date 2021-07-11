package nmea.mux;

import context.ApplicationContext;
import http.HTTPServer;
import http.RESTRequestManager;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAParser;
import nmea.computers.Computer;
import nmea.forwarders.Forwarder;
import nmea.mux.context.Context;
import org.yaml.snakeyaml.Yaml;
import utils.DumpUtil;
import utils.StaticUtil;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <b>NMEA Multiplexer.</b><br>
 * Also contains the definition of the REST operations for admin purpose.<br>
 * See {@link RESTRequestManager} and {@link HTTPServer}.<br>
 * Also see below the definition of <code>List&lt;Operation&gt; operations</code>.
 */
public class GenericNMEAMultiplexer implements RESTRequestManager, Multiplexer {
    private HTTPServer adminServer = null;
    protected Properties muxProperties;

    private final List<NMEAClient> nmeaDataClients = new ArrayList<>();
    private final List<Forwarder> nmeaDataForwarders = new ArrayList<>();
    private final List<Computer> nmeaDataComputers = new ArrayList<>();

    private final RESTImplementation restImplementation;

    /**
     * Implements the management of the REST requests (see {@link RESTImplementation})
     * Dedicated Admin Server.
     * This method is called by the HTTPServer through the current RESTRequestManager
     *
     * @param request the parsed request.
     * @return the response, along with its HTTP status code.
     */
    @Override
    public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
//	HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_IMPLEMENTED);
        HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
        if (verbose) {
            System.out.println("======================================");
            System.out.println("Request :\n" + request.toString());
            System.out.println("Response :\n" + response.toString());
            System.out.println("======================================");
        }
        return response;
    }

    @Override
    public List<HTTPServer.Operation> getRESTOperationList() {
        return restImplementation.getOperations();
    }

    @Override
    public synchronized void onData(String mess) {
        // To measure the flow (in bytes per time)
        Context.getInstance().addManagedBytes(mess.length());

        // Last sentence (inbound)
        Context.getInstance().setLastDataSentence(mess); // That one also increments the nb of messages processed.

        if (verbose) {
            System.out.println("==== From MUX: " + mess);
            DumpUtil.displayDualDump(mess);
            System.out.println("==== End Mux =============");
        }
        // Cache, if initialized
        if (ApplicationContext.getInstance().getDataCache() != null) {
            ApplicationContext.getInstance().getDataCache().parseAndFeed(mess);
        }

        if (this.process) {
            // Computers. Must go first, as a computer may re-feed the present onData method.
            synchronized (nmeaDataComputers) {
                nmeaDataComputers
                        .forEach(computer -> computer.write(mess.getBytes()));
            }

            // Forwarders
            synchronized (nmeaDataForwarders) {
                nmeaDataForwarders
                        .forEach(fwd -> {
                            try {
                                fwd.write((mess.trim() + NMEAParser.STANDARD_NMEA_EOS).getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    @Override
    public void setVerbose(boolean b) {
        verbose = b;
    }

    @Override
    public void setEnableProcess(boolean b) {
        this.process = b;
    }

    @Override
    public boolean getEnableProcess() {
        return this.process;
    }

    @Override
    public void stopAll() {
        // Send Ctrl+C
        softStop = true;
        terminateMux();
//	System.exit(0);
//	try {  Thread.sleep(2_500L); } catch (InterruptedException ie) {}
        System.out.println("Soft Exit");
        Runtime.getRuntime().exit(0); // Ctrl-C for the HTTP Server
    }

    private static boolean verbose = "true".equals(System.getProperty("mux.data.verbose"));
    private final static boolean infraVerbose = "true".equals(System.getProperty("mux.infra.verbose"));
    private boolean process = true; // onData, forward to computers and forwarders

    private boolean softStop = false;

    public void terminateMux() {
        System.out.println("Shutting down multiplexer nicely.");
        if (adminServer != null && softStop) {
            // Delay for the REST response
            //	System.out.println("Waiting a bit (for REST terminate request to complete)...");
            try {
                Thread.sleep(1_000L);
            } catch (InterruptedException ie) {
                // Absorb
            }
//		System.out.println("Done waiting");
        }
        nmeaDataClients.forEach(NMEAClient::stopDataRead);
        nmeaDataForwarders.forEach(Forwarder::close);
        nmeaDataComputers.forEach(Computer::close);

        if (adminServer != null) {
            synchronized (adminServer) {
                System.out.println("Mux Stopping Admin server");
                adminServer.stopRunning();
            }
        }
    }

    /**
     * Constructor.
     *
     * @param muxProps Initial config. See {@link #main(String...)} method.
     */
    public GenericNMEAMultiplexer(Properties muxProps) { // TODO A Constructor with yaml?

        this.muxProperties = muxProps;
        Context.getInstance().setStartTime(System.currentTimeMillis());

        if (infraVerbose) {
            System.out.printf("\t>> %s - Constructor %s, Initializing RESTImplementation...\n", NumberFormat.getInstance().format(System.currentTimeMillis()), this.getClass().getName());
        }
        // Read initial config from the properties file. See the main method.
//		verbose = "true".equals(System.getProperty("mux.data.verbose", "false")); // Initial verbose.
        restImplementation = new RESTImplementation(nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this);
        MuxInitializer.setup(muxProps, nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this, verbose);

        if (infraVerbose) {
            System.out.printf("\t>> %s - RESTImplementation initialized.\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!softStop) {
                terminateMux();
            }
        }, "Multiplexer shutdown hook"));

        nmeaDataClients.forEach(client -> {
            if (infraVerbose) {
                System.out.printf("\t>> %s - NMEADataClient: Starting %s...\n", NumberFormat.getInstance().format(System.currentTimeMillis()), client.getClass().getName());
            }
            try {
                client.startWorking();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        if (infraVerbose) {
            System.out.printf("\t>> %s - %s constructor completed.\n", NumberFormat.getInstance().format(System.currentTimeMillis()), this.getClass().getName());
        }
    }

    //	@Override
    public Properties getMuxProperties() {
        return this.muxProperties;
    }

    public void startAdminServer(int port) {
        try {
            this.adminServer = new HTTPServer(port, this);
            this.adminServer.startServer();
            if (infraVerbose) {
                System.out.printf("\t>> %s - Starting Admin server on port %d\n", NumberFormat.getInstance().format(System.currentTimeMillis()), port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getDefinitions() {
        Properties properties = null;
        String propertiesFile = System.getProperty("mux.properties", "nmea.mux.properties");
        if (propertiesFile.endsWith(".yaml") || propertiesFile.endsWith(".yml")) {
            Yaml yaml = new Yaml();
            try {
                InputStream inputStream = new FileInputStream(propertiesFile);
                Map<String, Object> map = yaml.load(inputStream);
                properties = MuxInitializer.yamlToProperties(map);
            } catch (IOException ioe) {
                throw new RuntimeException(String.format("File [%s] not found in %s", propertiesFile, System.getProperty("user.dir")));
            }
        } else if (propertiesFile.endsWith(".properties")) {
            Properties definitions = new Properties();
            File propFile = new File(propertiesFile);
            if (!propFile.exists()) {
                throw new RuntimeException(String.format("File [%s] not found in %s", propertiesFile, System.getProperty("user.dir")));
            } else {
                try {
                    definitions.load(new FileReader(propFile));
                    properties = definitions;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return properties;
    }

    /**
     * WiP.
     * Triggered from the main, if a CLI parameter "--interactive-config" is present.
     *
     * @return the generated Properties Object.
     */
    private static Properties interactiveConfig() {
        Properties props;
        System.out.println("-- Enter Mux config interactively --");
        Yaml yaml = new Yaml();
        Map<String, Object> topMap = new HashMap<>();

        String input = StaticUtil.userInput("> Enter a name for this MUX > ");
        topMap.put("name", input);

        Map<String, Object> contextMap = new HashMap<>();
        input = StaticUtil.userInput("> With HTTP server y|n ? > ");
        boolean withHttpServer = input.equalsIgnoreCase("Y");
        contextMap.put("with.http.server", withHttpServer);
        if (withHttpServer) {
            // http port
            input = StaticUtil.userInput("> HTTP port ? > ");
            int port = Integer.parseInt(input);
            contextMap.put("http.port", port);
            // init.cache
            input = StaticUtil.userInput("> Init cache y|n ? > ");
            boolean initCache = input.equalsIgnoreCase("Y");
            contextMap.put("init.cache", initCache);
        }
        topMap.put("context", contextMap);

        // Channels (List)
        List<Map<String, Object>> channels = new ArrayList<>();

        input = StaticUtil.userInput("Replay log file y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            Map<String, Object> oneChannel = new HashMap<>();

            oneChannel.put("type", "file");
            oneChannel.put("filename", "./sample.data/logged.data.archive.zip");
            oneChannel.put("path.in.zip", "2010-11-08.Nuku-Hiva-Tuamotu.nmea");
            oneChannel.put("zip", true);
            oneChannel.put("verbose", false);

            channels.add(oneChannel);
        }
        input = StaticUtil.userInput("Read Serial port y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            Map<String, Object> oneChannel = new HashMap<>();

            oneChannel.put("type", "serial");
            oneChannel.put("port", "/dev/ttyS80");
            oneChannel.put("baudrate", 4800);
            oneChannel.put("verbose", false);

            channels.add(oneChannel);
        }

        topMap.put("channels", channels);

        // Forwarder (List)
        List<Map<String, Object>> forwarders = new ArrayList<>();
        Map<String, Object> oneForwarder;

        input = StaticUtil.userInput("Forwarder. REST y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "rest");
            oneForwarder.put("server.name", "192.168.42.6");
            oneForwarder.put("server.port", 9999);
            oneForwarder.put("rest.resource", "/mux/nmea-sentence");
            oneForwarder.put("rest.verb", "POST");
            oneForwarder.put("http.headers", "Content-Type:text/plain");
            oneForwarder.put("verbose", true);
            forwarders.add(oneForwarder);
        }

        input = StaticUtil.userInput("Forwarder. TCP (no AIS) y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "tcp");
            oneForwarder.put("port", 7002);
            oneForwarder.put("properties", "no.ais.properties");
            forwarders.add(oneForwarder);
        }

        input = StaticUtil.userInput("Forwarder. TCP (AIS only) y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "tcp");
            oneForwarder.put("subclass", "nmea.forwarders.AISTCPServer");
            oneForwarder.put("port", 7003);
            oneForwarder.put("verbose", false);
            forwarders.add(oneForwarder);
        }

        topMap.put("forwarders", forwarders);

        // Computers (List)

        // Others (dev curve, and so)

        String output = yaml.dump(topMap);
        System.out.println(output);

        props = MuxInitializer.yamlToProperties(topMap);
        return props;
    }

    /**
     * Start the Multiplexer from here.
     *
     * @param args unused.
     */
    public static void main(String... args) {
        AtomicBoolean interactiveConfig = new AtomicBoolean(false);
        Arrays.asList(args).forEach(prm -> {
            if ("--interactive-config".equals(prm)) {
                interactiveConfig.set(true);
            }
        });

        Properties definitions = interactiveConfig.get() ? GenericNMEAMultiplexer.interactiveConfig() :  GenericNMEAMultiplexer.getDefinitions();

        if (infraVerbose) {
            System.out.println("MUX Definitions:");
            definitions.list(System.out);
        }

        boolean startProcessingOnStart = "true".equals(System.getProperty("process.on.start", "true"));
        GenericNMEAMultiplexer mux = new GenericNMEAMultiplexer(definitions);
        mux.setEnableProcess(startProcessingOnStart);
        // with.http.server=yes
        // http.port=9999
        String withHttpServer = definitions.getProperty("with.http.server", "no");
        if ("yes".equals(withHttpServer) || "true".equals(withHttpServer)) {
            int httpPort = Integer.parseInt(definitions.getProperty("http.port", "9999"));
            if (infraVerbose) {
                System.out.printf("Starting Admin server on port %d\n", httpPort);
            }
            mux.startAdminServer(httpPort);
        } else {
            if (infraVerbose) {
                System.out.println("\t>> NO Admin server started");
            }
        }
    }
}
