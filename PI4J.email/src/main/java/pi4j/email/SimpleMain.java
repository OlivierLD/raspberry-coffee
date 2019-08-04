package pi4j.email;

import email.EmailSender;

import java.text.SimpleDateFormat;

public class SimpleMain {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("mail.verbose", "false"));

	public static void main(String... args)
	throws Exception {

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
				System.out.println("  java pi4j.email.SimpleMain -verbose -send:google -receive:yahoo -sendto:me@home.net,you@yourplace.biz -help");
				System.exit(0);
			}
		}
		if (dest == null || dest.length == 0 || dest[0].trim().length() == 0) {
			throw new RuntimeException("No destination email (use -sendto:). Use the help (-help).");
		}

		final String[] destEmail = dest;
		final EmailSender sender = new EmailSender(providerSend);

		sender.send(destEmail,
				"An Image",
				"This is a text, and an attachment.\n\n",
				"plain/text",
				"bonus.jpg",
				"image/jpg");

	}
}
