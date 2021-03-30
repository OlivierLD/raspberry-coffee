package oliv.events;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class server {

    private final static String SERVER_PORT_PREFIX = "--server-port:";
    private final static String SERVER_VERBOSE =     "--server-verbose:";

    public static void main(String... args) {

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
                while(addresses.hasMoreElements()) {
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

        int serverPort = 7001;
        boolean verbose = false;

        for (String arg : args) {
            if (arg.startsWith(SERVER_PORT_PREFIX)) {
                serverPort = Integer.parseInt(arg.substring(SERVER_PORT_PREFIX.length()));
            } else if (arg.startsWith(SERVER_VERBOSE)) {
                verbose = "true".equals(arg.substring(SERVER_VERBOSE.length()));
            }
        }

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
            chatTCPServer.onMessage("Notice: Server is going down.".getBytes(), null);
            System.out.println("Now shutting down the server.");
            chatTCPServer.close();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
