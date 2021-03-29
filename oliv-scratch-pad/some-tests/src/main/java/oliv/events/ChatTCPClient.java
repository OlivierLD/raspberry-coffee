package oliv.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class ChatTCPClient {
    private final static String DEFAULT_HOST_NAME = "localhost";
    private final static int DEFAULT_TCP_PORT = 80;
    private final int tcpPort;
    private final String hostName;
    private boolean verbose;

    private PrintWriter out;
    private BufferedReader in;

    private boolean stayConnected = true;

    public ChatTCPClient() {
        this(DEFAULT_HOST_NAME, DEFAULT_TCP_PORT, false);
    }

    public ChatTCPClient(int port) {
        this(DEFAULT_HOST_NAME, port, false);
    }

    public ChatTCPClient(String host, int port, boolean verbose) {
        this.hostName = host;
        this.tcpPort = port;
        this.verbose = verbose;
    }

    private Socket clientSocket = null;

    // What to do with the message.
    // This is the default. Feel free to change it... Speak out the message ;)
    public Consumer<String> messageConsumer = message -> {
        System.out.println(message);
        System.out.print("> ");
    };

    public void setMessageConsumer(Consumer<String> messageConsumer) {
        if (verbose) {
            System.out.println("Overriding the MessageConsumer");
        }
        this.messageConsumer = messageConsumer;
    }

    public int getPort() {
        return this.tcpPort;
    }

//    public String getHostname() {
//        return this.hostName;
//    }

    public void startClient(Thread whoToTell) {
        try {
            InetAddress address = InetAddress.getByName(hostName);
            clientSocket = new Socket(address, tcpPort);

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            synchronized(whoToTell) {
                whoToTell.notify();
            }

            while (this.stayConnected) {
                try {
                    while (!in.ready()) {
                        // optional delay between polling
                        try { Thread.sleep(50); } catch (Exception ignore) {}
                    }
                    if (this.stayConnected) {
                        String fromServer = in.readLine();    // <= blocking!! (hence the above)
                        // Message received from server
                        this.messageConsumer.accept(fromServer);
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
            if (se.getMessage().contains("Connection refused")) {
                System.out.println("Refused (1)");
            } else if (se.getMessage().contains("Connection reset")) {
                System.out.println("Reset (2)");
            } else {
                if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (1)");
                    }
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (2)");
                    }
                } else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("Will try again (3)");
                    }
                } else if (se instanceof ConnectException) { // Et hop!
                    System.err.println("TCP :" + se.getMessage());
                } else {
                    System.err.println("TCP Socket:" + se.getMessage());
                }
            }
        } catch (Exception e) {
            manageError(e);
        }
    }

    public void writeToServer(String message) {
        if (out != null) {
            out.println(message);
        } else {
            throw new RuntimeException("No output stream to server!");
        }
    }

    public void closeClient() throws Exception {
        try {
            if (clientSocket != null) {
                this.stayConnected = false;
                in.close();
                out.close();
                clientSocket.close();
            }
        } catch (Exception ex) {
            System.err.println("Exception when closing Client:");
            throw ex;
        }
    }

    public void manageError(Throwable t) {
        throw new RuntimeException(t);
    }
}
