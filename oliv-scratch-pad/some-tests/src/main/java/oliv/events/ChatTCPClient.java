package oliv.events;

import java.io.*;
import java.net.*;

public class ChatTCPClient {
    private final static String DEFAULT_HOST_NAME = "localhost";
    private final static int DEFAULT_TCP_PORT = 80;
    private int tcpPort = DEFAULT_TCP_PORT;
    private String hostName = DEFAULT_HOST_NAME;
    private String clientName;

    private PrintWriter out;
    private BufferedReader in;

    private boolean stayConnected = true;

    public ChatTCPClient() {
        this(null, DEFAULT_HOST_NAME, DEFAULT_TCP_PORT);
    }

    public ChatTCPClient(int port) {
        this(null, DEFAULT_HOST_NAME, port);
    }

    public ChatTCPClient(String host, int port) {
        this(null, host, port);
    }
    public ChatTCPClient(String clientName, String host, int port) {
        this.clientName = (clientName != null ? clientName : "tcp-client");
        hostName = host;
        tcpPort = port;
    }

    private Socket clientSocket = null;

    public int getPort() {
        return this.tcpPort;
    }

    public String getHostname() {
        return this.hostName;
    }

    public void startClient() {
        try {
            InetAddress address = InetAddress.getByName(hostName);
            clientSocket = new Socket(address, tcpPort);

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (this.stayConnected) {
                try {
                    System.out.println("\tWaiting for server input...");
                    while (!in.ready()) {
                        // optional delay between polling
                        try { Thread.sleep(50); } catch (Exception ignore) {}
                    }
                    if (this.stayConnected) {
                        String fromServer = in.readLine();    // <= blocking!!
                        // Message received from server
                        System.out.println(("Received from server:"));
                        System.out.println(fromServer);
                    }
                } catch (IOException ioe) {
                    if (ioe.getMessage().equals("Stream closed")) {
                        System.out.println("Stream was closed.");
                    } else {
                        ioe.printStackTrace();
                    }
                }
            }
            System.out.println("Stop Reading TCP port.");
        } catch (BindException be) {
            System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpPort);
            be.printStackTrace();
            manageError(be);
        } catch (final SocketException se) {
//			if ("true".equals(System.getProperty("tcp.data.verbose"))) {
//		    se.printStackTrace();
//			}
            if (se.getMessage().indexOf("Connection refused") > -1) {
                System.out.println("Refused (1)");
            } else if (se.getMessage().indexOf("Connection reset") > -1) {
                System.out.println("Reset (2)");
            } else {
                boolean tryAgain = false;
                if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (1)");
                    }
                    tryAgain = true;
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (2)");
                    }
                } else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (3)");
                    }
                    tryAgain = true;
                } else if (se instanceof ConnectException) { // Et hop!
                    tryAgain = false;
                    System.err.println("TCP :" + se.getMessage());
                } else {
                    tryAgain = false;
                    System.err.println("TCP Socket:" + se.getMessage());
                }
            }
        } catch (Exception e) {
//    e.printStackTrace();
            manageError(e);
        }
    }

    public void writeToServer(String message) {
        out.println(message);
    }

    public void closeClient() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
        try {
            if (clientSocket != null) {
                this.stayConnected = false;
                in.close();
                out.close();
                clientSocket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void manageError(Throwable t) {
        throw new RuntimeException(t);
    }

    private final static String CLIENT_NAME_PREFIX = "--client-name:";
    private final static String SERVER_NAME_PREFIX = "--server-name:";
    private final static String SERVER_PORT_PREFIX = "--server-port:";

    public static void main(String... args) {
        String clientName = "Its Me!";
        String chatServerName = "localhost";
        int chatServerPort = 7001;
        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(CLIENT_NAME_PREFIX)) {
                clientName = args[i].substring(CLIENT_NAME_PREFIX.length());
            } else if (args[i].startsWith(SERVER_NAME_PREFIX)) {
                chatServerName = args[i].substring(SERVER_NAME_PREFIX.length());
            } else if (args[i].startsWith(SERVER_PORT_PREFIX)) {
                chatServerPort = Integer.parseInt(args[i].substring(SERVER_PORT_PREFIX.length()));
            }
        }

        ChatTCPClient client = new ChatTCPClient(clientName, chatServerName, chatServerPort);
        Thread listener = new Thread(() -> {
            client.startClient();
        });
        listener.start();
        // Client input part
        Console console = System.console();
        boolean keepAsking = true;
        while (keepAsking) {
            System.out.print("> ");
            String userInput = console.readLine();
            System.out.printf("Processing user input [%s]\n", userInput);
            if ("Q".equalsIgnoreCase(userInput) || "QUIT".equalsIgnoreCase(userInput)) {
                keepAsking = false;
            } else {
                client.writeToServer(userInput);
            }
        }
        System.out.println("Cleaning up...");
        try {
            client.closeClient();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
