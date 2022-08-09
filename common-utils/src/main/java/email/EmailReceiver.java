package email;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.SearchTerm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class EmailReceiver {
	private static String protocol;
	private static int outgoingPort;
	private static int incomingPort;
	private static String username;
	private static String password;
	private static String outgoing;
	private static String incoming;
	private static String replyto;
	private static boolean smtpauth;

	private static String acceptSubject;
	private static String ackSubject;

	private final static boolean verbose = "true".equals(System.getProperty("email.verbose", "false"));
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	private static final class HttpHeaders {
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String CONTENT_LENGTH = "Content-Length";
		public final static String USER_AGENT = "User-Agent";
		public final static String ACCEPT = "Accept";

		public final static String TEXT_PLAIN = "text/plain";
		public final static String TEXT_XML = "text/xml";
		public final static String APPLICATION_JSON = "application/json";
	}

	private EmailSender emailSender = null; // For Ack
	private String provider = null;

	public EmailReceiver(String provider) throws RuntimeException {
		this.provider = provider;
		EmailReceiver.protocol = "";
		EmailReceiver.outgoingPort = 0;
		EmailReceiver.incomingPort = 0;
		EmailReceiver.username = "";
		EmailReceiver.password = "";
		EmailReceiver.outgoing = "";
		EmailReceiver.incoming = "";
		EmailReceiver.replyto = "";
		EmailReceiver.smtpauth = false;

		EmailReceiver.acceptSubject = "";
		EmailReceiver.ackSubject = "";

		Properties props = new Properties();
		String propFile = "email.properties";
		try {
			FileInputStream fis = new FileInputStream(propFile);
			props.load(fis);
		} catch (Exception e) {
			System.out.println("email.properies file problem...");
			throw new RuntimeException("File not found:email.properies");
		}
		EmailReceiver.acceptSubject = props.getProperty("pi.email.subject");
		EmailReceiver.ackSubject = props.getProperty("pi.ack.subject");

		EmailReceiver.protocol = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.protocol");
		EmailReceiver.outgoingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server.port", "0"));
		EmailReceiver.incomingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server.port", "0"));
		EmailReceiver.username = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.username", "");
		EmailReceiver.password = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.password", "");
		EmailReceiver.outgoing = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server", "");
		EmailReceiver.incoming = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server", "");
		EmailReceiver.replyto = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.replyto", "");
		EmailReceiver.smtpauth = "true".equals(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.smtpauth", "false"));

		if (verbose) {
			System.out.println("Protocol:" + EmailReceiver.protocol);
			System.out.println("Usr/pswd:" + EmailReceiver.username + "/" + EmailReceiver.password);
		}
	}

	private static SearchTerm[] buildSearchTerm(String str) {
		String[] sa = str.split(",");
		List<SearchTerm> lst = new ArrayList<SearchTerm>();
		for (String s : sa) {
			lst.add(new FromStringTerm(s.trim()));
		}
		SearchTerm[] sta = new SearchTerm[lst.size()];
		sta = lst.toArray(sta);
		return sta;
	}

	private Properties setProps() {
		Properties props = new Properties();
		props.put("mail.debug", verbose ? "true" : "false");

		// TASK smtp should be irrelevant for a receiver
		props.put("mail.smtp.host", EmailReceiver.outgoing);
		props.put("mail.smtp.port", Integer.toString(EmailReceiver.outgoingPort));

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true"); //  See http://www.oracle.com/technetwork/java/faq-135477.html#yahoomail
		//  props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.ssl.enable", "true");

		if ("pop3".equals(EmailReceiver.protocol)) {
			props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.port", Integer.toString(EmailReceiver.incomingPort));
			props.setProperty("mail.pop3.socketFactory.port", Integer.toString(EmailReceiver.incomingPort));
		}

		if ("imap".equals(protocol)) {
			props.setProperty("mail.imap.starttls.enable", "false");
			// Use SSL
			props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.imap.socketFactory.fallback", "false");

			props.setProperty("mail.imap.port", Integer.toString(EmailReceiver.incomingPort));
			props.setProperty("mail.imap.socketFactory.port", Integer.toString(EmailReceiver.incomingPort));

			props.setProperty("mail.imaps.class", "com.sun.mail.imap.IMAPSSLStore");
		}
		return props;
	}

	public boolean isAuthRequired() {
		return EmailReceiver.smtpauth;
	}

	public String getUserName() {
		return EmailReceiver.username;
	}

	public String getPassword() {
		return EmailReceiver.password;
	}

	public String getReplyTo() {
		return EmailReceiver.replyto;
	}

	public String getIncomingServer() {
		return EmailReceiver.incoming;
	}

	public String getOutgoingServer() {
		return EmailReceiver.outgoing;
	}

	public List<ReceivedMessage> receive()
					throws Exception {
		return receive(null, null);
	}

	public List<ReceivedMessage> receive(String dir)
			throws Exception {
		return receive(dir, null);
	}

	public List<ReceivedMessage> receive(List<String> acceptedSubjects)
			throws Exception {
		return receive(null, acceptedSubjects);
	}

	public List<ReceivedMessage> receive(
			String dir,
			List<String> acceptedSubjects)
			throws Exception {
		return receive(dir, acceptedSubjects, true, true, ackSubject);
	}

	public List<ReceivedMessage> receive(
			String dir,
			List<String> acceptedSubjects,
			boolean sendAck,
			boolean deleteAfterReading,
			String ackTopic)
			throws Exception {
		if (verbose) {
			System.out.println("Receiving...");
		}
		List<ReceivedMessage> messList = new ArrayList<>();
		Store store = null;
		Folder folder = null;
		try {
			//  Properties props = System.getProperties();
			Properties props = setProps();

			if (verbose) {
				Set<Object> keys = props.keySet();
				for (Object o : keys) {
					System.out.println(o.toString() + ":" + props.get(o).toString());
				}
			}
			if (verbose) {
				System.out.println("Getting session...");
			}
//    Session session = Session.getInstance(props, null);
			Session session = Session.getInstance(props,
							new javax.mail.Authenticator() {
								protected PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(username, password);
								}
							});
			session.setDebug(verbose);
			if (verbose) {
				System.out.println("Session established.");
			}
			store = session.getStore(EmailReceiver.protocol);
			if (EmailReceiver.incomingPort == 0) {
				store.connect(EmailReceiver.incoming, EmailReceiver.username, EmailReceiver.password);
			} else {
				store.connect(EmailReceiver.incoming, EmailReceiver.incomingPort, EmailReceiver.username, EmailReceiver.password);
			}
			if (verbose) {
				System.out.println("Connected to store");
			}
			folder = store.getDefaultFolder();
			if (folder == null) {
				throw new RuntimeException("No default folder");
			}
			folder = store.getFolder("INBOX");
			if (folder == null) {
				throw new RuntimeException("No INBOX");
			}
			folder.open(Folder.READ_WRITE);
			if (verbose) {
				System.out.println("Connected... filtering, please wait.");
			}
//			SearchTerm st = new AndTerm(new SearchTerm[]{new OrTerm(buildSearchTerm(sendEmailsTo)),
//							new SubjectTerm(acceptSubject),
//							new FlagTerm(new Flags(Flags.Flag.SEEN), false)});

//			SearchTerm st = new AndTerm(new SubjectTerm(acceptSubject),
//							new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			SearchTerm st = new AndTerm(new FlagTerm(new Flags(Flags.Flag.SEEN), false),
							new FlagTerm(new Flags(Flags.Flag.DELETED), false));

			// st = new SubjectTerm("PI Request");
			Message[] msgs = folder.search(st);
//    Message msgs[] = folder.getMessages();

			if (verbose) {
				System.out.println("Search completed, " + msgs.length + " message(s).");
			}
			for (int msgNum = 0; msgNum < msgs.length; msgNum++) {
				try {
					Message mess = msgs[msgNum];
					Address[] from = mess.getFrom();
					String sender = "";
					try {
						sender = from[0].toString();
					} catch (Exception exception) {
						exception.printStackTrace();
					}
					String subject = mess.getSubject();
					if (true && (subject.equals(acceptSubject) || (acceptedSubjects != null && acceptedSubjects.contains(subject)))) { // Could not have the SubjectTerm to works properly...
						if (verbose) {
							System.out.println("Message from [" + sender + "], subject [" + subject + "], content [" + mess.getContent().toString().trim() + "]");
							System.out.printf("Seen   : %s\n", (mess.isSet(javax.mail.Flags.Flag.SEEN) ? "yes" : "no"));
							System.out.printf("Deleted: %s\n", (mess.isSet(javax.mail.Flags.Flag.DELETED) ? "yes" : "no"));
						}
						if (!mess.isSet(javax.mail.Flags.Flag.SEEN) && !mess.isSet(javax.mail.Flags.Flag.DELETED)) {
							MessageContent messageContent = printMessage(mess, dir);
							ReceivedMessage newMess = new ReceivedMessage().content(messageContent).from(from).subject(subject);
							messList.add(newMess);
							mess.setFlag(javax.mail.Flags.Flag.SEEN, true);
							if (deleteAfterReading) {
								mess.setFlag(javax.mail.Flags.Flag.DELETED, true);
							}

							if (sendAck) { // Send an ack - by email.
								if (this.emailSender == null) {
									this.emailSender = new EmailSender(this.provider);
								}
								this.emailSender.send(new String[]{sender},
										ackTopic != null ? ackTopic : ackSubject,
										"Your request [" + mess.getSubject().trim() + "] is being taken care of.\nContent is \n" + messageContent.getContent());
								if (verbose) {
									System.out.println("Sent an ack to " + sender);
								}
							}
						} else {
							if (verbose) {
								System.out.println("Old message in your inbox..., received " + mess.getReceivedDate().toString());
							}
						}
					}
				} catch (Exception ex) {
//        System.err.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (folder != null) {
					folder.close(true);
				}
				if (store != null) {
					store.close();
				}
			} catch (Exception ex2) {
				System.err.println("Finally ...");
				ex2.printStackTrace();
			}
		}
		return messList;
	}

	public static class Attachment {
		String mimeType;
		String fullPath;

		public String getFullPath() {
			return this.fullPath;
		}
		public String getMimeType() {
			return this.mimeType;
		}
		public Attachment mimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}
		public Attachment fullPath(String fullPath) {
			this.fullPath = fullPath;
			return this;
		}
	}
	public static class MessageContent {
		String content;
		List<Attachment> attachments;

		public String getContent() {
			return this.content;
		}
		public List<Attachment> getAttachments() {
			return this.attachments;
		}

		public MessageContent content(String content) {
			this.content = content;
			return this;
		}
		public MessageContent attachments(List<Attachment> attachments) {
			this.attachments = attachments;
			return this;
		}
	}

	public static class ReceivedMessage {
		MessageContent content;
		String subject;
		Address[] from;

		public MessageContent getContent() {
			return this.content;
		}
		public String getSubject() {
			return this.subject;
		}
		public Address[] getFrom() {
			return this.from;
		}

		public ReceivedMessage content(MessageContent content) {
			this.content = content;
			return this;
		}
		public ReceivedMessage subject(String subject) {
			this.subject = subject;
			return this;
		}
		public ReceivedMessage from(Address[] from) {
			this.from = from;
			return this;
		}
	}

	public static MessageContent printMessage(Message message, String dir) {
		String messContent = "";
		MessageContent fullMessage = new MessageContent();
		try {
			String from = ((InternetAddress) message.getFrom()[0]).getPersonal();
			if (from == null) {
				from = ((InternetAddress) message.getFrom()[0]).getAddress();
			}
			if (verbose) {
				System.out.println("From: " + from);
			}
			String subject = message.getSubject();
			if (verbose) {
				System.out.println("Subject: " + subject);
			}
			Part messagePart = message;
			Object content = messagePart.getContent();
			if (content instanceof Multipart) { // Attachment(s) ?
//      messagePart = ((Multipart)content).getBodyPart(0);
				int nbParts = ((Multipart) content).getCount();
				if (verbose) {
					System.out.println("[ Multipart Message ], " + nbParts + " part(s).");
				}
				List<Attachment> attachments = new ArrayList<>();
				for (int i = 0; i < nbParts; i++) {
					messagePart = ((Multipart) content).getBodyPart(i);
					if (verbose) {
						System.out.printf("Part #%d, Content-Type: %s, file %s\n", i, messagePart.getContentType(), messagePart.getFileName());
					}
					String dateBasedDirectoryName = SDF.format(new Date());
					File storageDir = new File((dir == null ? "." + File.separator : dir + File.separator) + dateBasedDirectoryName);
					if (!storageDir.exists()) {
						storageDir.mkdirs();
					}
					if (i == 0 && messagePart.getContentType() != null && (messagePart.getContentType().startsWith(HttpHeaders.TEXT_PLAIN) || messagePart.getContentType().startsWith("text/html")) && messagePart.getFileName() == null) { // Content?
						if (verbose) {
							System.out.println("-- Part #" + i + " --, " + messagePart.getContentType().replace('\n', ' ').replace('\r', ' ').replace("\b", "").trim());
						}
						InputStream is = messagePart.getInputStream();
						BufferedReader br = new BufferedReader(new InputStreamReader(is));
						String line = "";
						while (line != null) {
							line = br.readLine();
							if (line != null) {
								if (verbose) {
									System.out.println("[" + line + "]");
								}
								messContent += (line + "\n");
							}
						}
						br.close();
						if (verbose) {
							System.out.println("-------------------");
						}
					} else {
						if (verbose) {
							System.out.println(messagePart.getContentType() + ":" + messagePart.getFileName());
						}
						InputStream is = messagePart.getInputStream();
						String newFileName = "";
						if (dir != null) {
							newFileName = dir + File.separator;
						}
						newFileName += (dateBasedDirectoryName + File.separator);
						newFileName += messagePart.getFileName();
						FileOutputStream fos = new FileOutputStream(newFileName);
						if (verbose) {
							System.out.printf("Downloading %s into %s...\n", messagePart.getFileName(), newFileName);
						}
						copy(is, fos);
						if (verbose) {
							System.out.println("...done.");
						}
						attachments.add(new Attachment()
						.mimeType(messagePart.getContentType())
						.fullPath(newFileName));
					}
				}
				fullMessage = fullMessage
						.attachments(attachments);
			} else {
//      System.out.println("  .Message is a " + content.getClass().getName());
//      System.out.println("Content:");
//      System.out.println(content.toString());
				messContent = content.toString();
			}
			if (verbose) {
				System.out.println("-----------------------------");
			}
			fullMessage = fullMessage
					.content(messContent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fullMessage;
	}

	private static void copy(InputStream in, OutputStream out)
					throws IOException {
		synchronized (in) {
			synchronized (out) {
				byte[] buffer = new byte[256];
				while (true) {
					int bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	}
}
