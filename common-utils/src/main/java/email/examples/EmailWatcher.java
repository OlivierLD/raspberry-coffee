package email.examples;

import email.EmailReceiver;
import email.EmailSender;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmailWatcher {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
	private static boolean verbose = "true".equals(System.getProperty("email.watcher.verbose", "false"));

	private static final class HttpHeaders {
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String CONTENT_LENGTH = "Content-Length";
		public final static String USER_AGENT = "User-Agent";
		public final static String ACCEPT = "Accept";

		public final static String TEXT_PLAIN = "text/plain";
		public final static String TEXT_XML = "text/xml";
		public final static String APPLICATION_JSON = "application/json";
	}

	// Assume the keys are unique in the list
	static final List<EmailProcessor> processors = Arrays.asList(
		new EmailProcessor("exit", null),
		new EmailProcessor("execute", EmailWatcher::cmdProcessor),
		new EmailProcessor("execute-script", EmailWatcher::scriptProcessor)
	);

	/**
	 * This is an example of an email interaction.
	 *
	 * A user is sending an email:
	 * <pre>
	 * - To: olivier.lediouris@gmail.com
	 * - Subject: "execute"
	 * - Content (text/plain):
	 *     whoami
	 *     ifconfig
	 *     uname -a
	 * </pre>
	 * Then the commands are executed, output (stdout & stderr) and status code are rturned
	 * After that, an email is returned to the requester, containing the result.
	 *
	 * Note the <code>processors</code> class member, defining the operation to be performed on received emails.
	 *
	 * <ul>
	 *   <li>PROs: It does not require a server, just this class running somewhere.</li>
	 *   <li>CONs: It is not synchronous, it can take some time.</li>
	 * </ul>
	 * Invoked like:
	 * java email.examples.EmailWatcher [-verbose] -send:google -receive:yahoo
	 * <p>
	 * This will send emails using google, and receive using yahoo.
	 * Do check the file email.properties for the different values associated with email servers.
	 * <p>
	 *
	 *
	 * NO GPIO INTERACTION in this one (no sudo access needed).
	 * <p>
	 *   The program stops when the 'exit' email is received by the EmailReceiver.
	 * </p>
	 *
	 * See also the email-reader node on Node-RED. It implements similar features.
	 *
	 * @param args See above
	 */
	public static void main(String... args) {

		String providerSend    = "yahoo";  // Default
		String providerReceive = "google"; // Default

		EmailWatcher emailWatcher = new EmailWatcher();

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
				System.out.println("  java email.examples.EmailWatcher -verbose -send:google -receive:yahoo -help");
				System.exit(0);
			}
		}
		final EmailSender sender = new EmailSender(providerSend);

		EmailReceiver receiver = new EmailReceiver(providerReceive); // For Google, pop must be explicitly enabled at the account level
		System.out.println("Start receiving.");
		boolean keepLooping = true;
		while (keepLooping) {
			try {
				if (verbose) {
					System.out.println("Waiting on receive.");
				}
				List<EmailReceiver.ReceivedMessage> received = receiver.receive(
						"attachments",
						processors.stream()
								.map(processor -> processor.getKey())
								.collect(Collectors.toList()),
						true,
						false,
						"Remote Manager");

				//	if (verbose || received.size() > 0)
				if (verbose) {
					System.out.println("---------------------------------");
					System.out.println(SDF.format(new Date()) + " - Retrieved " + received.size() + " message(s).");
					System.out.println("---------------------------------");
				}
				for (EmailReceiver.ReceivedMessage mess : received) {
					if (true || verbose) {
						System.out.println("Received:\n" + mess.getContent().getContent());
						if (mess.getContent().getAttachments() != null && mess.getContent().getAttachments().size() > 0) {
							System.out.println("With attachments:");
							mess.getContent().getAttachments()
									.stream()
									.forEach(att -> {
										System.out.println(String.format("File %s, type %s", att.getFullPath(), att.getMimeType()));
									});
						}
					}
					String operation = mess.getSubject();
					if ("exit".equals(operation)) {                 // operation = 'exit'
						keepLooping = false;
						System.out.println("Will exit next batch.");
					} else {
						System.out.println(String.format("Operation: [%s], sent for processing...", operation));

						final String finalOp = operation;
						MessageContext messCtx = new MessageContext()
								.message(mess)
								.sender(sender);
						Optional<EmailProcessor> processor = emailWatcher.processors
								.stream()
								.filter(p -> p.getKey().equals(finalOp))
								.findFirst();
						if (processor.isPresent()) {
							processor.get().getProcessor().accept(messCtx);
						} else {
							// Operation not found.
							System.out.println(String.format("No processor registered for operation [%s]", operation));
							try {
								Thread.sleep(1_000L);
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
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

	public static class MessageContext {
		EmailReceiver.ReceivedMessage message;
		EmailSender sender;

		public MessageContext message(EmailReceiver.ReceivedMessage message) {
			this.message = message;
			return this;
		}
		public MessageContext sender(EmailSender sender) {
			this.sender = sender;
			return this;
		}
	}

	public static class EmailProcessor {
		private String key;
		private Consumer<MessageContext> processor;

		public EmailProcessor(String key, Consumer<MessageContext> processor) {
			this.key = key;
			this.processor = processor;
		}
		public String getKey() {
			return this.key;
		}
		public Consumer<MessageContext> getProcessor() {
			return this.processor;
		}
	}

	// operation = 'execute', execute system command (message payload), returns exit code and command output (stdout, stderr).
	private static void cmdProcessor(MessageContext messContext) {
		try {
			String script = messContext.message.getContent().getContent();

			// Loop on lines of cmd
			String[] cmds = script.split("\n");
			StringBuffer output = new StringBuffer();
			for (String cmd : cmds) {
				if (!cmd.trim().isEmpty()) {
					Process p = Runtime.getRuntime().exec(cmd);
					BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); // stdout
					BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream())); // stderr
					String line;
					while ((line = stdout.readLine()) != null) {
						System.out.println(line);
						output.append(line + "\n");
					}
					while ((line = stderr.readLine()) != null) {
						System.out.println(line);
						output.append(line + "\n");
					}
					int exitStatus = p.waitFor(); // Sync call
					output.append(String.format(">> %s returned status %d\n", cmd.trim(), exitStatus));
				}
			}
			Address[] sendTo = messContext.message.getFrom();
			String[] dest = Arrays.asList(sendTo)
					.stream()
					.map(addr -> ((InternetAddress) addr).getAddress())
					.collect(Collectors.joining(","))
					.split(","); // Not nice, I know. A suitable toArray would help.
			messContext.sender.send(dest,
					"Command execution",
					String.format("cmd [%s] returned: \n%s", script, output.toString()),
					HttpHeaders.TEXT_PLAIN);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// operation = 'execute-script', execute attached scripts, returns exit code and command output (stdout, stderr).
	private static void scriptProcessor(MessageContext messContext) {
		try {
			final List<EmailReceiver.Attachment> attachments = messContext.message.getContent().getAttachments();

			// Loop on all the scripts
			final StringBuffer output = new StringBuffer();
			attachments.stream()
					.forEach(attachment -> {
						String cmd = null;
						if ("text/x-sh".equals(attachment.getMimeType()) || HttpHeaders.TEXT_PLAIN.equals(attachment.getMimeType())) {
							cmd = "sh ./" + attachment.getFullPath();
						} else {
							System.err.println(String.format("Mime-type %s not supported", attachment.getMimeType()));
						}
						if (cmd != null) {
							try {
								Process p = Runtime.getRuntime().exec(cmd); // ? new String[] {"/bin/bash", "-c",  cmd}
								BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream())); // stdout
								BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream())); // stderr
								String line;
								while ((line = stdout.readLine()) != null) {
									System.out.println(line);
									output.append(line + "\n");
								}
								while ((line = stderr.readLine()) != null) {
									System.out.println(line);
									output.append(line + "\n");
								}
								int exitStatus = p.waitFor(); // Sync call
								output.append(String.format(">> %s returned status %d\n", cmd.trim(), exitStatus));
							} catch (Exception ex) {
								output.append(ex.toString() + "\n");
							}
						}
					});

			Address[] sendTo = messContext.message.getFrom();
			String[] dest = Arrays.asList(sendTo)
					.stream()
					.map(addr -> ((InternetAddress) addr).getAddress())
					.collect(Collectors.joining(","))
					.split(","); // Not nice, I know. A suitable toArray would help.
			messContext.sender.send(dest,
					"Command execution",
					String.format("Scripts execution returned: \n%s", output.toString()),
					HttpHeaders.TEXT_PLAIN);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
