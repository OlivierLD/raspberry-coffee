package oliv.events;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class client {

    private final static String CLIENT_NAME_PREFIX = "--client-name:";
    private final static String SERVER_NAME_PREFIX = "--server-name:";
    private final static String SERVER_PORT_PREFIX = "--server-port:";

    public static class TextToSpeech {
        private static Map<String, Consumer<String>> speechTools = new HashMap<>();

        static Consumer<String> say = message -> {
            try {
                // User say -v ? for a list of voices.
//			Runtime.getRuntime().exec(new String[] { "say", "-v", "Thomas", "\"" + message + "\"" }); // French
                Runtime.getRuntime().exec(new String[] { "say", "-v", "Alex", "\"" + message + "\"" });  // English
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        static Consumer<String> espeak = message -> {
            try {
                Runtime.getRuntime().exec(new String[] { "espeak", "\"" + message + "\"" });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        static {
            speechTools.put("Mac OS X", say);
            speechTools.put("Linux", espeak);
        }

        public static void speak(String text) {
            Consumer<String> speechTool = speechTools.get(System.getProperty("os.name"));
            if (speechTool == null) {
                throw new RuntimeException("No speech tool found in this os [" + System.getProperty("os.name") + "]");
            }
            try {
                speechTool.accept(text);
//			Runtime.getRuntime().exec(new String[] { speechTool, "\"" + text + "\"" });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String... args) {

        String clientName = "It's Me!";
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

        // Option: override, make it speak...
        client.setMessageConsumer(TextToSpeech::speak);

        final String _clientName = clientName;
        final Thread me = Thread.currentThread();
        Thread listener = new Thread(() -> {
            client.startClient(me);
        });
        listener.start();

        // Wait for the stuff to start
        try {
//          Thread.sleep(1_000L); // Bad approach: See below something nicer
            synchronized (me) {
                me.wait();
                System.out.println("Done with client initialization.");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        String idMess = String.format("%s:%s", ChatTCPServer.SERVER_COMMANDS.I_AM.toString(), _clientName);
        System.out.printf(">>> Telling server who I am: %s\n", idMess);
        client.writeToServer(idMess);

        // Client input part

        System.out.println("Q or QUIT to quit");
        System.out.println("WHO_S_THERE to know who's there");
        System.out.println("Anything else will be broadcasted");

        TextToSpeech.speak("Client is ready!");

        Console console = System.console();
        boolean keepAsking = true;
        while (keepAsking) {
            if (console != null) {
                System.out.print("> ");
                String userInput = console.readLine();
                if (!userInput.isEmpty()) {
                    System.out.printf("Processing user input [%s]\n", userInput);
                    if ("Q".equalsIgnoreCase(userInput) || "QUIT".equalsIgnoreCase(userInput)) {
                        keepAsking = false;
                        client.writeToServer(ChatTCPServer.SERVER_COMMANDS.I_M_OUT.toString());
                    } else {
                        client.writeToServer(userInput);
                    }
                }
            } else {
                try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}
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
