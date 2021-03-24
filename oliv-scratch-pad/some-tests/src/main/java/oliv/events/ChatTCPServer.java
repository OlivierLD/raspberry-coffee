package oliv.events;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatTCPServer implements ServerInterface {

    private final static boolean VERBOSE = true;

    private static class ChatClient {
        String name;

        public ChatClient() {
        }

        public ChatClient name(String name) {
            this.name = name;
            return this;
        }
    }

    // private List<Socket> clientSocketList = new ArrayList<>(1);
    private Map<Socket, ChatClient> clientMap = new HashMap<>();

    private final static int DEFAULT_PORT = 7001;

    private int tcpPort;
    private ServerSocket serverSocket = null;

    public enum SERVER_COMMANDS {
        I_AM,
        WHO_S_THERE,
        I_M_OUT
    };

    public ChatTCPServer() throws Exception {
        this(DEFAULT_PORT);
    }

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
        // this.clientSocketList.add(skt);
        this.clientMap.put(skt, new ChatClient());
        ChatTCPServer instance = this;
        Thread clientThread = new Thread(() -> {
            try {
                // out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                while (true) {
                    String clientMessage = in.readLine();
                    if (clientMessage != null) {
                        System.out.printf("\t>> Got a client message [%s] from %s\n", clientMessage, skt);
                        boolean processed = false;
                        for (SERVER_COMMANDS serverCommand : SERVER_COMMANDS.values()) {
                            if (clientMessage.startsWith(serverCommand.toString())) {
                                processed = true;
                                // Process it
                                System.out.printf("Message starts with %s, processing it.\n", serverCommand);
                                switch (serverCommand.toString()) { // TODO Something nicer
                                    case "I_AM":
                                        ChatClient chatClient = clientMap.get(skt);
                                        if (chatClient != null) {
                                            chatClient = chatClient.name(clientMessage.trim().substring(SERVER_COMMANDS.I_AM.toString().length() + 1)); // +1: ":"
                                            clientMap.put(skt, chatClient);
                                        } else {
                                            // What the French !? Not Found??
                                        }
                                        break;
                                    case "I_M_OUT":
                                        clientMap.remove(skt);
                                        break;
                                    case "WHO_S_THERE":
                                        String clients = clientMap.keySet().stream().map(k -> clientMap.get(k).name).collect(Collectors.joining(", "));
                                        clients += "\n";
                                        DataOutputStream out = new DataOutputStream(skt.getOutputStream());
                                        out.write(clients.getBytes());
                                        out.flush();
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            }
                        }
                        if (!processed) {
                            instance.onMessage((clientMessage + "\n").getBytes(), skt);
                        }
                    } else {
                        System.out.println("Client Message is null???");
                        this.clientMap.remove(skt);
                        break;
                    }
                }
                // in.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("End of client thread.");
        });
        clientThread.start();
    }

    @Override
    public void onMessage(byte[] message, Socket sender) {
        List<Socket> toRemove = new ArrayList<>();
        synchronized (this.clientMap) {
            // Broadcast to all connected clients
            this.clientMap.keySet().forEach(tcpSocket -> { // TODO Synchronize the stream?
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
            synchronized (this.clientMap) {
                toRemove.stream().forEach(this.clientMap::remove);
            }
        }
    }

    private int getNbClients() {
        return this.clientMap.size();
    }

    @Override
    public void close() {
        System.out.println("- Stop writing to " + this.getClass().getName());
        try {
            for (Socket tcpSocket : this.clientMap.keySet()) {
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
}
