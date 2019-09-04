package sample.email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import email.EmailSender;

import static utils.StaticUtil.userInput;

public class EmailVoltage {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

	public static void main(String... args)
	throws Exception {

		String providerSend = "google"; // default
		String sendTo = "";
		String[] dest = null;

		for (int i = 0; i < args.length; i++) {
			if ("-verbose".equals(args[i])) {
				System.setProperty("verbose", "true");
			} else if (args[i].startsWith("-send:"))
				providerSend = args[i].substring("-send:".length());
			else if (args[i].startsWith("-sendto:")) {
				sendTo = args[i].substring("-sendto:".length());
				dest = sendTo.split(",");
			} else if ("-help".equals(args[i])) {
				System.out.println("Usage:");
				System.out.println("  java sample.email.EmailVoltage -verbose -send:google -sendto:me@home.net,you@yourplace.biz -help");
				System.exit(0);
			}
		}
		if (dest == null || dest.length == 0 || dest[0].trim().isEmpty()) {
			throw new RuntimeException("No destination email. Use the help (-help).");
		}

		final String[] destEmail = dest;
		final EmailSender sender = new EmailSender(providerSend);
		boolean go = true;
		System.out.println("Hit return to toggle the switch, Q to exit.");
		while (go) {
			String str = userInput("Voltage > ");
			if ("Q".equalsIgnoreCase(str)) {
				go = false;
				System.out.println("Bye.");
			} else {
				float data = Float.parseFloat(str);
				System.out.println("Sending");
				sender.send(destEmail,
								"PI Volt",
								"{ voltage: " + String.valueOf(data) + " }");
				System.out.println("Sent.");
			}
		}
	}
}
