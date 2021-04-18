package oliv.tcp.chat;

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

    private final boolean verbose;

    private static class ChatClient {
        String name;

        public ChatClient() {
        }

        public ChatClient name(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Map<Socket, ChatClient> clientMap = new HashMap<>();

    private final static int DEFAULT_PORT = 7001;

    private final int tcpPort;
    private ServerSocket serverSocket = null;

    public ChatTCPServer() {
        this(DEFAULT_PORT);
    }

    public ChatTCPServer(int port) {
        this(port, false);
    }

    public ChatTCPServer(boolean verbose) {
        this(DEFAULT_PORT, verbose);
    }

    public ChatTCPServer(int port, boolean verbose) {
        this.tcpPort = port;
        this.verbose = verbose;

        try {
            SocketThread socketThread = new SocketThread(this);
            socketThread.start();
        } catch (Exception ex) {
            System.err.println("Creating the SocketThread:");
            throw ex;
        }
    }

    public int getTcpPort() {
        return this.tcpPort;
    }

    /**
     * New client got connected.
     * Will be named later on.
     *
     * @param skt The client socket.
     */
    protected void setSocket(Socket skt) {
        synchronized(this.clientMap) {
            this.clientMap.put(skt, new ChatClient());
        }
        ChatTCPServer instance = this;
        Thread clientThread = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                while (true) {
                    String clientMessage = in.readLine();
                    if (clientMessage != null) {
                        if (verbose) {
                            System.out.printf("\t>> Got a client message [%s] from %s (%s)\n", clientMessage, clientMap.get(skt).getName(), skt);
                        }
                        boolean processed = false;
                        for (Utils.SERVER_COMMANDS serverCommand : Utils.SERVER_COMMANDS.values()) {
                            if (clientMessage.toUpperCase().startsWith(serverCommand.toString())) {
                                processed = true;
                                // Process it
                                if (verbose) {
                                    System.out.printf("Message starts with %s, processing it.\n", serverCommand);
                                }
                                switch (serverCommand.toString()) {
                                    case "I_AM":
                                        ChatClient chatClient = clientMap.get(skt);
                                        if (chatClient != null) {
                                            chatClient = chatClient.name(clientMessage.trim().substring(Utils.SERVER_COMMANDS.I_AM.toString().length() + 1)); // +1: ":"
                                            if (verbose) {
                                                System.out.printf("Naming client: %s%n", chatClient.toString());
                                            }
                                            clientMap.put(skt, chatClient);

                                            String message = String.format("[%s] just joined\n", chatClient.getName()); // Make sure it ends with \n
                                            // Broadcast to all connected clients
                                            this.onMessage(message.getBytes(), skt);
                                        } else {
                                            // What the French !? Not Found??
                                            System.err.println("ChatClient not found??");
                                        }
                                        break;
                                    case "I_M_OUT":
                                        if (verbose) {
                                            System.out.printf("Removing %s (%s) from the client map.\n", skt, clientMap.get(skt));
                                        }
                                        String message = String.format("[%s] just left\n", clientMap.get(skt).getName()); // Make sure it ends with \n
                                        // Broadcast to all connected clients
                                        this.onMessage(message.getBytes(), skt);
                                        clientMap.remove(skt); // Prevents memory leaks (from here...) !
                                        break;
                                    case "WHO_S_THERE":
                                        String clients = clientMap.keySet().stream().map(k -> clientMap.get(k).getName()).collect(Collectors.joining(", "));
                                        String mess = String.format("%d client%s: %s\n", this.getNbClients(), (this.getNbClients() > 1 ? "s" : ""), clients); // Ends with a \n
                                        DataOutputStream out = new DataOutputStream(skt.getOutputStream());
                                        out.write(mess.getBytes());
                                        out.flush();
                                        break;
                                    default:
                                        if (verbose) {
                                            System.out.println("Received unknown command???");
                                        }
                                        break;
                                }
                                break;
                            }
                        }
                        if (!processed) {
                            if (verbose) {
                                System.out.printf("Message [%s] not processed, using default 'onMessage'.\n", clientMessage);
                            }
                            synchronized (instance) {
                                // Do not forget the \n !!!
                                clientMessage = String.format("[From %s] %s\n", clientMap.get(skt).getName(), clientMessage);
                                instance.onMessage((clientMessage).getBytes(), skt); // \n is required !!
                            }
                        }
                    } else {
                        if (verbose) {
                            System.out.println("Client Message is null???");
                        }
                        this.clientMap.remove(skt);
                        break;
                    }
                }
                // in.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (verbose) {
                System.out.println("End of client thread.");
            }
        });
        clientThread.start();
    }

    /**
     * Defines THE behavior of the server.
     *
     * @param message Important !!!: In this implementation, MUST end with a NL (\n) for the client to read it (readLine) !!
     * @param sender Sender of the message. If null, message will be broadcasted to everyone. Otherwise, to everyone BUT the sender.
     */
    @Override
    public void onMessage(byte[] message, Socket sender) {
        List<Socket> toRemove = new ArrayList<>(); // Will contain closed sockets.
        synchronized (this.clientMap) {
            // Broadcast to all connected clients
            this.clientMap.keySet().forEach(tcpSocket -> {
                if (!tcpSocket.equals(sender)) { // Do not send the message back to its sender.
                    if (verbose) {
                        System.out.printf("Server sending %s to %s (%s)%n", new String(message), clientMap.get(tcpSocket).getName(), tcpSocket);
                    }
                    synchronized (tcpSocket) {
                        try {
                            DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());
                            out.write(message);
                            out.flush();
                            if (verbose) {
                                System.out.printf(" Server sent [%s] to %s\n", new String(message).trim(), clientMap.get(tcpSocket).getName());
                            }
                        } catch (SocketException se) {
                            if (verbose) {
                                System.out.println("Will remove...");
                            }
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
                toRemove.forEach(this.clientMap::remove);
            }
        }
    }

    private int getNbClients() {
        return this.clientMap.size();
    }

    @Override
    public void close() {
        System.out.println("- Stopping " + this.getClass().getName());
        try {
            for (Socket tcpSocket : this.clientMap.keySet()) {
                tcpSocket.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private class SocketThread extends Thread {
        private final ChatTCPServer parent;

        public SocketThread(ChatTCPServer parent) {
            super("TCPServer");
            this.parent = parent;
        }

        public void run() {
            try {
                parent.serverSocket = new ServerSocket(tcpPort);
                boolean keepLooping = true;
                while (keepLooping) { // Wait for the clients to connect
                    try {
                        if (verbose) {
                            System.out.println(".......... serverSocket waiting (TCP:" + tcpPort + ").");
                        }
                        Socket clientSocket = serverSocket.accept();
                        if (verbose) {
                            System.out.println(".......... serverSocket accepted (TCP:" + tcpPort + ").");
                        }
                        parent.setSocket(clientSocket);
                    } catch (Exception ex) {
                        System.err.printf("SocketThread port %d: %s%n", tcpPort, ex.getLocalizedMessage());
                        keepLooping = false;
                    }
                }
            } catch (IOException ex) {
                System.err.printf("SocketThread port %d: %s%n", tcpPort, ex.getLocalizedMessage());
            }
            System.out.println("..... End of TCP SocketThread.");
        }
    }
}
