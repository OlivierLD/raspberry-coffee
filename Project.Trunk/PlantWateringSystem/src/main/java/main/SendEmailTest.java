package main;

import email.EmailSender;

import java.text.SimpleDateFormat;

public class SendEmailTest {
	private static boolean verbose = "true".equals(System.getProperty("email.test.verbose", "false"));

	public static void main(String... args) {

		String providerSend = "google";  // Default

		if (verbose) {
			System.setProperty("email.verbose", "true");
		}

		final EmailSender sender = new EmailSender(providerSend);
		System.out.println(String.format("Will send to: %s", sender.getEmailDest()));

		try {
			sender.send(sender.getEmailDest().split(","), "Email test", "<h1>This is a test, no worries.</h1>", "text/html");
			System.out.println("Done. Check your inbox.");
		} catch (Exception oops) {
			oops.printStackTrace();
		}
	}
}

// See https://stackoverflow.com/questions/11356237/sending-mail-from-yahoo-id-to-other-email-ids-using-javamail-api
