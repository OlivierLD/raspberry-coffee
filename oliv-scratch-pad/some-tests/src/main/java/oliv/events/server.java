package oliv.events;

public class server {

    private final static String SERVER_PORT_PREFIX = "--server-port:";
    private final static String SERVER_VERBOSE = "--server-verbose:";

    public static void main(String... args) {

        System.out.println("Use [Ctrl-C] to exit.");

        int serverPort = 7001;
        boolean verbose = false;

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(SERVER_PORT_PREFIX)) {
                serverPort = Integer.parseInt(args[i].substring(SERVER_PORT_PREFIX.length()));
            } else if (args[i].startsWith(SERVER_VERBOSE)) {
                verbose = "true".equals(args[i].substring(SERVER_VERBOSE.length()));
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
            }
        }));
        System.out.printf("Chat server started on port %d.\n", chatTCPServer.getTcpPort());
        try {
            synchronized (itsMe) {
                itsMe.wait();
            }
            System.out.println("Ok, ok! I'm going!");
            // TODO: Notify connected clients ?
            if (chatTCPServer != null) {
                chatTCPServer.close();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!\n");
    }
}
