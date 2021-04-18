package oliv.tcp.chat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Server {

    private final static String HELP_PREFIX              = "--help";
    private final static String HELP_SMALL_PREFIX        = "-h";
    private final static String SERVER_PORT_PREFIX       = "--server-port:";
    private final static String SERVER_PORT_SMALL_PREFIX = "-p:";
    private final static String SERVER_VERBOSE           = "--server-verbose:";
    private final static String SERVER_SMALL_VERBOSE     = "-v:";

    private final static int FIRST_COL_WIDTH = 16;
    private final static int SECOND_COL_WIDTH = 32;

    private static void displayHelp() {
        System.out.println("---- TCP Chat Server ----");
        System.out.println("- CLI Parameters:");
        System.out.printf("+-%s-+-%s-+--------------------------------------------%n",
                Utils.rpad("", FIRST_COL_WIDTH, "-"),
                Utils.rpad("", SECOND_COL_WIDTH, "-"));
        System.out.printf("| %s | %s | Display help and exit.%n",
                Utils.rpad(String.format("%s", HELP_SMALL_PREFIX), FIRST_COL_WIDTH),
                Utils.rpad(String.format("%s", HELP_PREFIX), SECOND_COL_WIDTH));
        System.out.printf("| %s | %s | Verbose mode, default false.%n",
                Utils.rpad(String.format("%strue|false", SERVER_SMALL_VERBOSE), FIRST_COL_WIDTH),
                Utils.rpad(String.format("%strue|false", SERVER_VERBOSE), SECOND_COL_WIDTH));
        System.out.printf("| %s | %s | TCP Port, default 7001.%n",
                Utils.rpad(String.format("%s7001", SERVER_PORT_SMALL_PREFIX), FIRST_COL_WIDTH),
                Utils.rpad(String.format("%s7001", SERVER_PORT_PREFIX), SECOND_COL_WIDTH));
        System.out.printf("+-%s-+-%s-+--------------------------------------------%n",
                Utils.rpad("", FIRST_COL_WIDTH, "-"),
                Utils.rpad("", SECOND_COL_WIDTH, "-"));
    }

    public static void main(String... args) throws Exception {

        int serverPort = 7001;
        boolean verbose = false;

        for (String arg : args) {
            if (arg.startsWith(SERVER_PORT_PREFIX)) {
                serverPort = Integer.parseInt(arg.substring(SERVER_PORT_PREFIX.length()));
            } else if (arg.startsWith(SERVER_PORT_SMALL_PREFIX)) {
                serverPort = Integer.parseInt(arg.substring(SERVER_PORT_SMALL_PREFIX.length()));
            } else if (arg.startsWith(SERVER_VERBOSE)) {
                verbose = "true".equals(arg.substring(SERVER_VERBOSE.length()));
            } else if (arg.startsWith(SERVER_SMALL_VERBOSE)) {
                verbose = "true".equals(arg.substring(SERVER_SMALL_VERBOSE.length()));
            } else if (arg.equals(HELP_PREFIX) || arg.equals(HELP_SMALL_PREFIX)) {
                displayHelp();
                System.exit(0);
            }
        }

        System.out.printf("Compiled: %s\n", Utils.getCompileDate(verbose));

        String ip;
        System.out.println("----- N E T W O R K -----");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iFace = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iFace.isLoopback() || !iFace.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iFace.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    ip = address.getHostAddress();
                    System.out.println(iFace.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("-------------------------");

        System.out.println("Use [Ctrl-C] to exit.");

        // All starts here.
        ChatTCPServer chatTCPServer = null;
        try {
            chatTCPServer = new ChatTCPServer(serverPort, verbose);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        final Thread itsMe = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nOops! Trapped exit signal...");
            synchronized (itsMe) {
                itsMe.notify();
                try {
                    itsMe.join(); // Give time to finish...
                    System.out.println("... Gone");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }));
        System.out.printf("Chat server started on port %d.\n", chatTCPServer.getTcpPort());
        try {
            synchronized (itsMe) {
                itsMe.wait();
            }
            System.out.println("Ok, ok! I'm leaving!");
            // Notify connected clients ?
            System.out.println("Notifying clients...");
            chatTCPServer.onMessage("Notice: Server is going down.\n".getBytes(), null);
            System.out.println("Now shutting down the server.");
            chatTCPServer.close();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
