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
	 * This is an example of an email interaction.
	 * A user is sending an email:
	 * <pre>
	 * - To: olivier.lediouris@gmail.com
	 * - Subject: "PI Request"
	 * - Content (plain): { 'operation': 'last-snap', 'width':480, 'height':640, 'name': 'email-snap' }
	 *     In the json object above, width, height and name are optional, default values above.
	 * </pre>
	 * Then the script `remote.snap.sh` is triggered to:
	 * <pre>
	 * - Send an http request is sent to the Raspberry PI to take the snapshot (see {@link weatherstation.logger.HTTPLogger} )
	 * - Download the corresponding picture
	 * </pre>
	 * After that, an email is returned to the requeter, with the snapshot attached to it.
	 * <ul>
	 *   <li>PROs: It does not require a server, just this class running somewhere.</li>
	 *   <li>CONs: It is not synchronous, it can take some time.</li>
	 * </ul>
	 * Invoked like:
	 * java weatherstation.email.EmailWatcher [-verbose] -send:google -receive:yahoo
	 * <p>
	 * This will send emails using google, and receive using yahoo.
	 * Do check the file email.properties for the different values associated with email servers.
	 * <p>
	 * NO GPIO INTERACTION in this one (no sudo access needed).
	 * <p>
	 *   The program stops when the 'exit' email is received by the EmailReceiver.
	 * </p>
	 * @param args See above
	 */
	public static void main(String... args) {

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
						if ("true".equals(System.getProperty("email.test.only", "false"))) {
							System.out.println(String.format("EmailWatcher Executing [%s]", cmd));
						} else {
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
									.split(","); // Not nice, I know. A suitable toArray would help.
							// Attachment and content are not compatible.
							sender.send(dest,
									"Weather Snapshot",
									"",  // TODO Some content?
									"image/jpg",
									String.format("web/%s.jpg", snapName));
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
