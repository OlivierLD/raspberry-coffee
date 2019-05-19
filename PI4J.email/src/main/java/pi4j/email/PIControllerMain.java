package pi4j.email;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import email.EmailReceiver;
import email.EmailSender;
import org.json.JSONObject;

import pi4j.gpio.GPIOController;
import pi4j.gpio.RaspberryPIEventListener;

public class PIControllerMain implements RaspberryPIEventListener {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

	private static String providerSend = "google";
	private static String providerReceive = "google";

	EmailSender sender = null;

	/**
	 * Invoked like:
	 * java pi4j.email.PIControllerMain [-verbose] -send:google -receive:yahoo -help
	 * <p>
	 * This will send emails using google, and receive using yahoo.
	 * Default values are:
	 * java pi4j.email.PIControllerMain -send:google -receive:google
	 * <p>
	 * Do check the file email.properties for the different values associated with email servers.
	 *
	 * @param args See above
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
				System.out.println("  java pi4j.email.PIControllerMain -verbose -send:google -receive:yahoo -help");
				System.exit(0);
			}
		}

		PIControllerMain lmc = new PIControllerMain();
		GPIOController piController = new GPIOController(lmc);
		EmailReceiver receiver = new EmailReceiver(providerReceive); // For Google, pop must be explicitly enabled at the account level
		try {
			System.out.println("Waiting for instructions.");
			boolean keepLooping = true;
			while (keepLooping) {
				List<EmailReceiver.ReceivedMessage> received = receiver.receive();
				if (verbose || received.size() > 0)
					System.out.println(SDF.format(new Date()) + " - Retrieved " + received.size() + " message(s).");
				for (EmailReceiver.ReceivedMessage mess : received) {
					//      System.out.println(s);
					String operation = "";
					try {
						JSONObject json = new JSONObject(mess.getContent().getContent());
						operation = json.getString("operation");
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
						System.err.println("Message is [" + mess.getContent().getContent() + "]");
					}
					if ("exit".equals(operation)) {
						keepLooping = false;
						System.out.println("Will exit next batch.");
						//  break;
					} else {
						if ("turn-green-on".equals(operation)) {
							System.out.println("Turning green on");
							piController.switchGreen(true);
						} else if ("turn-green-off".equals(operation)) {
							System.out.println("Turning green off");
							piController.switchGreen(false);
						} else if ("turn-yellow-on".equals(operation)) {
							System.out.println("Turning yellow on");
							piController.switchYellow(true);
						} else if ("turn-yellow-off".equals(operation)) {
							System.out.println("Turning yellow off");
							piController.switchYellow(false);
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
