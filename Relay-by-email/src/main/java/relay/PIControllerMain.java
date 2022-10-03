package relay;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import org.json.JSONObject;
import relay.email.EmailReceiver;
import relay.email.EmailSender;
import relay.gpio.GPIOController;
import relay.gpio.RaspberryPIEventListener;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class PIControllerMain implements RaspberryPIEventListener {
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
    private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

    private static String providerSend = "google";
    private static String providerReceive = "google";

    EmailSender sender = null;

    /**
     * Invoked like:
     * java relay.email.PIControllerMain [-verbose] -send:google -receive:yahoo -help
     * <p>
     * This will send emails using google, and receive using yahoo.
     * Default values are:
     * java pi4j.email.PIControllerMain -send:google -receive:google
     * <p>
     * Do check the file email.properties for the different values associated with email servers.
     *
     * @param args See above
     *             <p>
     *             To try it:
     *             send email to the right destination (like olivier.lediouris@gmail.com, see in email.properties) with a plain text payload like
     *             { 'operation':'turn-relay-on' }
     *             or
     *             { 'operation':'turn-relay-off' }
     *             <p>
     *             Subject: 'PI Request'
     */
    public static void main(String... args) {
        for (int i = 0; i < args.length; i++) {
            if ("-verbose".equals(args[i])) {
                verbose = true;
                System.setProperty("verbose", "true");
            } else if (args[i].startsWith("-send:"))
                providerSend = args[i].substring("-send:".length());
            else if (args[i].startsWith("-receive:"))
                providerReceive = args[i].substring("-receive:".length());
            else if ("-help".equals(args[i])) {
                System.out.println("Usage:");
                System.out.println("  java relay.email.PIControllerMain -verbose -send:google -receive:yahoo -help");
                System.exit(0);
            }
        }

        final GPIOController piController = new GPIOController();
        EmailReceiver receiver = new EmailReceiver(providerReceive); // For Google, pop must be explicitly enabled at the account level

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                piController.switchRelay(false);
                piController.shutdown();
                System.out.println("\nExiting nicely.");
            }
        });

        try {
            String from = "";
            try {
                Properties props = new Properties();
                props.load(new FileReader("email.properties"));
                String emitters = props.getProperty("pi.accept.emails.from");
                from = ", from " + emitters;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Waiting for instructions" + from + ".");
            boolean keepLooping = true;
            while (keepLooping) {
                List<String> received = receiver.receive();
                if (verbose || received.size() > 0)
                    System.out.println(SDF.format(new Date()) + " - Retrieved " + received.size() + " message(s).");
                for (String s : received) {
                    //      System.out.println(s);
                    String operation = "";
                    try {
                        JSONObject json = new JSONObject(s);
                        operation = json.getString("operation");
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                        System.err.println("Message is [" + s + "]");
                    }
                    if ("exit".equals(operation)) {
                        keepLooping = false;
                        System.out.println("Will exit next batch.");
                        //  break;
                    } else {
                        if ("turn-relay-on".equals(operation)) {
                            System.out.println("Turning relay on");
                            piController.switchRelay(true);
                        } else if ("turn-relay-off".equals(operation)) {
                            System.out.println("Turning relay off");
                            piController.switchRelay(false);
                        }
                        try {
                            Thread.sleep(1_000L);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
            piController.shutdown();
            System.out.println("Done.");
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void manageEvent(GpioPinDigitalStateChangeEvent event) {
        if (sender == null)
            sender = new EmailSender(providerSend);
        try {
            String mess = "{ pin: '" + event.getPin() + "', state:'" + event.getState() + "' }";
            System.out.println("Sending:" + mess);
            sender.send(sender.getEmailDest().split(","),
                    sender.getEventSubject(),
                    mess);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
