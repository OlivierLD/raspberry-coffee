package weatherstation.email;

import email.EmailReceiver;
import email.EmailSender;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EmailWatcher {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("mail.verbose", "false"));

	/**
	 * Invoked like:
	 * java weatherstation.email.EmailWatcher [-verbose] -send:google -receive:yahoo
	 * <p>
	 * This will send emails using google, and receive using yahoo.
	 * Do check the file email.properties for the different values associated with email servers.
	 * <p>
	 * NO GPIO INTERACTION in this one.
	 * <p>
	 *   The program stops when the 'exit' email is received by the EmailReceiver.
	 * </p>
	 * @param args See above
	 */
	public static void main_(String... args) {

		String providerSend = "yahoo"; // Default
		String providerReceive = "google"; // Default

		for (int i = 0; i < args.length; i++) {
			if ("-verbose".equals(args[i])) {
				verbose = true;
				System.setProperty("verbose", "true");
			} else if (args[i].startsWith("-send:")) {
				providerSend = args[i].substring("-send:".length());
			} else if (args[i].startsWith("-receive:")) {
				providerReceive = args[i].substring("-receive:".length());
			} else if ("-help".equals(args[i])) {
				System.out.println("Usage:");
				System.out.println("  java weatherstation.email.EmailWatcher -verbose -send:google -receive:yahoo -help");
				System.exit(0);
			}
		}
		final EmailSender sender = new EmailSender(providerSend);

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
					System.out.println("Received:\n" + mess.getContent());
					JSONObject json = null;
					String operation = "";
					try {
						json = new JSONObject(mess.getContent());
						operation = json.getString("operation"); // Expects a { 'operation': 'Blah' }
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
						System.err.println("Error in message payload [" + mess.getContent() + "]");
					}
					if ("exit".equals(operation)) {                 // operation = 'exit'
						keepLooping = false;
						System.out.println("Will exit next batch.");
						//  break;
					} else if ("last-snap".equals(operation)) {    // operation = 'last-snap', last snapshot, returned in the reply, as attachment.
						// Fetch last image
						int rot = 270, width = 640, height = 480; // Default ones. Taken from payload below
						String snapName = "email-snap"; // No Extension!
						try {
							rot = json.getInt("rot");
						} catch (JSONException je) {
						}
						try {
							width = json.getInt("width");
						} catch (JSONException je) {
						}
						try {
							height = json.getInt("height");
						} catch (JSONException je) {
						}
						try {
							snapName = json.getString("name");
						} catch (JSONException je) {
						}
						String cmd = String.format("./remote.snap.sh -rot:%d -width:%d -height:%d -name:%s", rot, width, height, snapName);
						Process p = Runtime.getRuntime().exec(cmd);
						BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = null;
						while ((line = stdout.readLine()) != null) {
							System.out.println(line);
						}
						int exitStatus = p.waitFor();

						Address[] sendTo = mess.getFrom();
						String[] dest = Arrays.asList(sendTo)
								.stream()
								.map(addr -> ((InternetAddress) addr).getAddress())
								.collect(Collectors.joining(","))
								.split(",");
						// Attachment and content are not compatible.
						sender.send(dest,
								"Weather Snapshot",
								"",
								"image/jpg",
								String.format("web/%s.jpg", snapName));

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

	public static void main(String... args) {
		String jsonStr = "{ 'rot': 123, 'name': 'Akeu' }";
		JSONObject json = new JSONObject(jsonStr);
		Integer rot = json.getInt("rot");
		Integer width = json.getInt("width");
		System.out.println("Bam");
	}
}
