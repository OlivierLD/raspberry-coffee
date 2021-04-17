package oliv.tcp.chat;

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
    private final boolean verbose;

    private PrintWriter out;     // Stream to the server
    private BufferedReader in;   // Stream from the server

    private boolean stayConnected = true;  // Set it to false to close the client.

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
    // This is the default. Feel free to override it... Speak out the message ;)
    public Consumer<String> messageConsumer = message -> {
        System.out.println(message);
        System.out.print("> ");
    };

    protected Consumer<String> getMessageConsumer() {
        return messageConsumer;
    }

    /**
     * Use it to override the default message Consumer
     * @param messageConsumer the Consumer to use. Note: The default one will still be used (as a backup...).
     */
    public void setMessageConsumer(Consumer<String> messageConsumer) {
        if (verbose) {
            System.out.println("Overriding the MessageConsumer");
        }
        this.messageConsumer = messageConsumer;
    }

    public int getPort() {
        return this.tcpPort;
    }

    /**
     * Listens in a loop.
     * @param whoToTell the Thread to notify when streams (in and out) are ready
     */
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
                        // optional delay between polling, to cool things down.
                        try { Thread.sleep(50); } catch (Exception ignore) { /* Absorb */ }
                    }
                    if (this.stayConnected) {
                        // WARNING!! We use readLine (needs to end with a NL)
                        // This would need to be changed for non-line messages.
                        String fromServer = in.readLine();    // <= blocking!! (hence the 'while loop' above, wait until there is something to read)
                        // Message received from server. Send to client for processing.
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
                System.out.println("No server found. Refused (1)");
            } else if (se.getMessage().contains("Connection reset")) {
                System.out.println("No server found. Reset (2)");
            } else {
                if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("No server found. No server found. Will try again (1)");
                    }
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("No server found. Will try again (2)");
                    }
                } else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
                    if ("true".equals(System.getProperty("tcp.data.verbose"))) {
                        System.out.println("No server found. Will try again (3)");
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
