package pi4j.email;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import email.EmailReceiver;
import email.EmailSender;
import org.json.JSONObject;

public class SampleMain {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("mail.verbose", "false"));

	private static String nbEmails = System.getProperty("nb.emails", "3");
	/**
	 * Invoked like:
	 * java pi4j.email.SampleMain [-verbose] -send:google -receive:yahoo
	 * <p>
	 * This will send emails using google, and receive using yahoo.
	 * Do check the file email.properties for the different values associated with email servers.
	 * <p>
	 * NO GPIO INTERACTION in this one.
	 * <p>
	 *   It sends (with an EmailSender) X emails (see -Dnb.emails, 3 by default) - with a picture (snap.jpg) attached to it, and then an 'exit' email.
	 *   The program stops when the 'exit' email is received by the EmailReceiver.
	 * </p>
	 * @param args See above
	 */
	public static void main(String... args)
	throws Exception {

		int nbEmailsToSend = 3;
		try {
			nbEmailsToSend = Integer.parseInt(nbEmails);
		} catch (NumberFormatException nfe) {
			System.err.println("Using 3 by default");
			nfe.printStackTrace();
		}

		String providerSend = "yahoo"; // Default
		String providerReceive = "google"; // Default
		String sendTo = "";
		String[] dest = null;

		for (int i = 0; i < args.length; i++) {
			if ("-verbose".equals(args[i])) {
				verbose = true;
				System.setProperty("verbose", "true");
			} else if (args[i].startsWith("-send:"))
				providerSend = args[i].substring("-send:".length());
			else if (args[i].startsWith("-receive:"))
				providerReceive = args[i].substring("-receive:".length());
			else if (args[i].startsWith("-sendto:")) {
				sendTo = args[i].substring("-sendto:".length());
				dest = sendTo.split(",");
			} else if ("-help".equals(args[i])) {
				System.out.println("Usage:");
				System.out.println("  java pi4j.email.SampleMain -verbose -send:google -receive:yahoo -sendto:me@home.net,you@yourplace.biz -help");
				System.exit(0);
			}
		}
		if (dest == null || dest.length == 0 || dest[0].trim().length() == 0) {
			throw new RuntimeException("No destination email (use -sendto:). Use the help (-help).");
		}

		final String[] destEmail = dest;
		final int emails = nbEmailsToSend;
		final EmailSender sender = new EmailSender(providerSend);
		Thread senderThread = new Thread(() -> {
				try {
					System.out.println("Preamble.");
					sender.send(destEmail,
							"PI Image",
							"This is a text, and an attachment.",
							"plain/text",
							"bonus.jpg",
							"image/jpg");
					for (int i = 0; i < emails; i++) {
						System.out.println(String.format("Sending (%d)...", (i+1)));
						sender.send(destEmail,
								"PI Request",
								"{ operation: 'read-loud', content: 'I am reading emails, be prepared.' }");
						System.out.println("Sent.");
						Thread.sleep(30_000L); // 30 seconds
					}
					System.out.println("Exiting (sending 'exit' email)...");
					sender.send(destEmail,
									"PI Request",
									"{ operation: 'exit' }");
					System.out.println("Sender. Bye.");
				} catch (Exception ex) {
					System.err.println("Sender thread:");
					ex.printStackTrace();
				}
		});
		senderThread.start(); // Bombarding

		EmailReceiver receiver = new EmailReceiver(providerReceive); // For Google, pop must be explicitly enabled at the account level
		boolean keepLooping = true;
		while (keepLooping) {
			try {
				System.out.println("Waiting on receive.");
				List<EmailReceiver.ReceivedMessage> received = receiver.receive();
				//	if (verbose || received.size() > 0)
				System.out.println("---------------------------------");
				System.out.println(SDF.format(new Date()) + " - Retrieved " + received.size() + " message(s).");
				System.out.println("---------------------------------");
				for (EmailReceiver.ReceivedMessage mess : received) {
					System.out.println("Received:\n" + mess.getContent().getContent());
					JSONObject json = null;
					String operation = "";
					try {
						json = new JSONObject(mess.getContent().getContent());
						operation = json.getString("operation"); // Expects a { 'operation': 'Blah' }
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
						System.err.println("Message is [" + mess.getContent().getContent() + "]");
					}
					if ("exit".equals(operation)) {                 // operation = 'exit'
						keepLooping = false;
						System.out.println("Will exit next batch.");
						//  break;
					} else if ("read-loud".equals(operation)) {    // operation = 'read-loud'
						if (json != null) {
							String content = json.getString("content"); // content member
							if (content != null) {
								utils.TextToSpeech.speak(content);
							} else {
								utils.TextToSpeech.speak("I do not know what to read.");
							}
						} else {
							utils.TextToSpeech.speak("Oops");
						}

					} else {
						System.out.println("Operation: [" + operation + "], sent for processing.");
						try {
							Thread.sleep(1_000L);
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}
			} catch (Exception ex) {
				System.err.println("Receiver loop:");
				ex.printStackTrace();
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		System.out.println("Receiver. Done.");
	}
}
