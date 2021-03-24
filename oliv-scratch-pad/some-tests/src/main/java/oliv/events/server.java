package oliv.events;

public class server {

    private final static String SERVER_PORT_PREFIX = "--server-port:";

    public static void main(String... args) {

        int serverPort = 7001;

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(SERVER_PORT_PREFIX)) {
                serverPort = Integer.parseInt(args[i].substring(SERVER_PORT_PREFIX.length()));
            }
        }

        ChatTCPServer chatTCPServer = null;
        try {
            chatTCPServer = new ChatTCPServer(serverPort);
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
        System.out.println("Chat server is starting.");
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
