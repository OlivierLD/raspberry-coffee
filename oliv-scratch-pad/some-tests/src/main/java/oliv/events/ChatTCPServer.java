package oliv.events;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ChatTCPServer implements ServerInterface {

    private final static boolean VERBOSE = true;

    private List<Socket> clientSocketList = new ArrayList<>(1);

    private int tcpPort = 7001; // Default
    private ServerSocket serverSocket = null;

    public ChatTCPServer(int port) throws Exception {
        this.tcpPort = port;

        try {
            SocketThread socketThread = new SocketThread(this);
            socketThread.start();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public int getTcpPort() {
        return this.tcpPort;
    }

    protected void setSocket(Socket skt) {
        this.clientSocketList.add(skt);
        ChatTCPServer instance = this;
        Thread clientThread = new Thread(() -> {
            try {
                // out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                String clientMessage = in.readLine();
                System.out.printf("\t>> Got a client message [%s] from %s\n", clientMessage, skt);
                instance.onMessage((clientMessage + "\n").getBytes(), skt);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });
        clientThread.start();
    }

    @Override
    public void onMessage(byte[] message, Socket sender) {
        List<Socket> toRemove = new ArrayList<>();
        synchronized (clientSocketList) {
            // Broadcats to all connected clients
            clientSocketList.stream().forEach(tcpSocket -> { // TODO Synchronize the stream?
                if (!tcpSocket.equals(sender)) { // Do not send message to sender.
                    synchronized (tcpSocket) {
                        try {
                            DataOutputStream out = null;
                            if (out == null) {
                                out = new DataOutputStream(tcpSocket.getOutputStream());
                            }
                            System.out.printf(" Server sending [%s]\n", new String(message).trim());
                            out.write(message);
                            out.flush();
                        } catch (SocketException se) {
                            System.out.println("Will remove...");
                            toRemove.add(tcpSocket);
                        } catch (Exception ex) {
                            System.err.println("TCPWriter.write:" + ex.getLocalizedMessage());
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }

        if (toRemove.size() > 0) {
            // Removing disconnected clients
            synchronized (clientSocketList) {
                toRemove.stream().forEach(this.clientSocketList::remove);
            }
        }
    }

    private int getNbClients() {
        return clientSocketList.size();
    }

    @Override
    public void close() {
        System.out.println("- Stop writing to " + this.getClass().getName());
        try {
            for (Socket tcpSocket : clientSocketList) {
                tcpSocket.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private class SocketThread extends Thread {
        private ChatTCPServer parent = null;

        public SocketThread(ChatTCPServer parent) {
            super("TCPServer");
            this.parent = parent;
        }

        public void run() {
            try {
                parent.serverSocket = new ServerSocket(tcpPort);
                while (true) { // Wait for the clients
                    if (VERBOSE) {
                        System.out.println(".......... serverSocket waiting (TCP:" + tcpPort + ").");
                    }
                    Socket clientSocket = serverSocket.accept();
                    if (VERBOSE) {
                        System.out.println(".......... serverSocket accepted (TCP:" + tcpPort + ").");
                    }
                    parent.setSocket(clientSocket);
                }
            } catch (Exception ex) {
                System.err.println(String.format("SocketThread port %d: %s", tcpPort, ex.getLocalizedMessage()));
            }
            System.out.println("..... End of TCP SocketThread.");
        }
    }

    public static void main(String... args) {

        ChatTCPServer chatTCPServer = null;
        try {
            chatTCPServer = new ChatTCPServer(7001);
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
            // TODO: Notify connected clients
            if (chatTCPServer != null) {
                chatTCPServer.close();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!\n");
    }
}
