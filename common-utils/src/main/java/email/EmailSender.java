package email;
/**
 * Many accounts need an "App Password"...
 * See <https://www.codeproject.com/Articles/5266074/Send-Email-with-Java-and-Yahoo-Mail>
 *     <https://support.google.com/accounts/answer/185833?p=InvalidSecondFactor&visit_id=637762242149664166-1125601348&rd=1>
 */

import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.util.Properties;

public class EmailSender {
	private static String protocol;
	private static int outgoingPort;
	private static int incomingPort;
	private static String username;
	private static String password;
	private static String outgoing;
	private static String incoming;
	private static String replyto;
	private static boolean smtpauth;
	private static boolean useSSL;

	private static String sendEmailsTo;
	private static String eventSubject;

	private static boolean verbose = "true".equals(System.getProperty("email.verbose", "false"));

	private static final class HttpHeaders {
		public final static String CONTENT_TYPE = "Content-Type";
		public final static String CONTENT_LENGTH = "Content-Length";
		public final static String USER_AGENT = "User-Agent";
		public final static String ACCEPT = "Accept";

		public final static String TEXT_PLAIN = "text/plain";
		public final static String TEXT_XML = "text/xml";
		public final static String TEXT_HTML = "text/html";
		public final static String APPLICATION_JSON = "application/json";
	}

	public EmailSender(String provider) throws RuntimeException {
		EmailSender.protocol = "";
		EmailSender.outgoingPort = 0;
		EmailSender.incomingPort = 0;
		EmailSender.username = "";
		EmailSender.password = "";
		EmailSender.outgoing = "";
		EmailSender.incoming = "";
		EmailSender.replyto = "";
		EmailSender.smtpauth = false;
		EmailSender.useSSL = true;
		EmailSender.sendEmailsTo = "";
		EmailSender.eventSubject = "";

		Properties props = new Properties();
		String propFile = "email.properties";
		try {
			FileInputStream fis = new FileInputStream(propFile);
			props.load(fis);
		} catch (Exception e) {
			System.out.println("email.properties file problem...");
			throw new RuntimeException("File not found:email.properties");
		}
		EmailSender.sendEmailsTo = props.getProperty("pi.send.emails.to");
		EmailSender.eventSubject = props.getProperty("pi.event.subject");

		EmailSender.protocol = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.protocol");
		EmailSender.outgoingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server.port", "0"));
		EmailSender.incomingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server.port", "0"));
		EmailSender.username = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.username", "");
		EmailSender.password = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.password", "");
		EmailSender.outgoing = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server", "");
		EmailSender.incoming = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server", "");
		EmailSender.replyto = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.replyto", "");
		EmailSender.smtpauth = "true".equals(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.smtpauth", "false"));
		EmailSender.useSSL = "true".equals(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.use.ssl", "true"));

		if (verbose) {
			System.out.println("-------------------------------------");
			System.out.println("Protocol       : " + EmailSender.protocol);
			System.out.println("Usr/pswd       : " + EmailSender.username + "/" + EmailSender.password);
			System.out.println("Incoming server: " + EmailSender.incoming + ":" + EmailSender.incomingPort);
			System.out.println("Outgoing server: " + EmailSender.outgoing + ":" + EmailSender.outgoingPort);
			System.out.println("replyto        : " + EmailSender.replyto);
			System.out.println("SMTPAuth       : " + EmailSender.smtpauth);
			System.out.println("-------------------------------------");
		}
	}

	public boolean isAuthRequired() {
		return EmailSender.smtpauth;
	}

	public String getUserName() {
		return EmailSender.username;
	}

	public String getPassword() {
		return EmailSender.password;
	}

	public String getReplyTo() {
		return EmailSender.replyto;
	}

	public String getIncomingServer() {
		return EmailSender.incoming;
	}

	public String getOutgoingServer() {
		return EmailSender.outgoing;
	}

	public String getEmailDest() {
		return EmailSender.sendEmailsTo;
	}

	public String getEventSubject() {
		return EmailSender.eventSubject;
	}

	public void send(String[] dest,
	                 String subject,
	                 String content)
					throws MessagingException, AddressException {
		send(dest, subject, content, null);
	}

	public void send(String[] dest,
	                 String subject,
	                 String content,
	                 String mimeType)
			throws MessagingException, AddressException {
		send(dest, subject, content, mimeType, null, null);
	}

	public void send(String[] dest,
	                 String subject,
	                 String content,
	                 String mimeType,
	                 String attachment,
	                 String attachmentMimeType)
					throws MessagingException, AddressException {
		Properties props = setProps();

//    Session session = Session.getDefaultInstance(props);
		Session session = Session.getInstance(props,
						new javax.mail.Authenticator() {
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(username, password);
							}
						});
		session.setDebug(verbose);
		Transport tr = session.getTransport("smtp");
		if (!(tr instanceof SMTPTransport)) {
			System.out.println("This is NOT an SMTPTransport:[" + tr.getClass().getName() + "]");
		}
		Message msg = new MimeMessage(session);
  	msg.setFrom(new InternetAddress(EmailSender.username));
//	msg.setFrom(new InternetAddress("not-me@neverthere.com")); // Did not find the way to hide the from address...
		msg.setReplyTo(new Address[] { new InternetAddress(EmailSender.replyto) });
		if (dest == null || dest.length == 0) {
			throw new RuntimeException("Need at least one recipient.");
		}
		msg.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(dest[0]));
		for (int i = 1; i < dest.length; i++) {
			msg.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(dest[i]));
		}
		msg.setSubject(subject);

		if (attachment != null) {
			// 1 - Text Content
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(content);
			messageBodyPart.setHeader(HttpHeaders.CONTENT_TYPE, (mimeType == null ? HttpHeaders.TEXT_PLAIN : mimeType));
			Multipart multipart = new MimeMultipart();
			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setHeader(HttpHeaders.CONTENT_TYPE, (attachmentMimeType == null ? HttpHeaders.TEXT_PLAIN : attachmentMimeType));
			String filename = attachment;
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filename);
			multipart.addBodyPart(messageBodyPart);
			// Send the complete message parts
			msg.setContent(multipart);
		} else {
			msg.setText(content != null ? content : "");
			msg.setContent(content, (mimeType == null ? HttpHeaders.TEXT_PLAIN : mimeType));
		}
		msg.saveChanges();
		if (verbose) {
			System.out.println("sending:[" + content + "], " + Integer.toString(content.length()) + " characters");
		}
		Transport.send(msg);
	}

	private Properties setProps() {
		Properties props = new Properties();
		props.put("mail.debug", verbose ? "true" : "false");
		props.put("mail.smtp.host", EmailSender.outgoing);
		props.put("mail.smtp.port", Integer.toString(EmailSender.outgoingPort));

		props.put("mail.smtp.user", EmailSender.username);
		props.put("mail.smtp.password", EmailSender.password);

		props.put("mail.smtp.auth", EmailSender.smtpauth);

		props.put("mail.smtp.starttls.enable", "true"); //  See http://www.oracle.com/technetwork/java/faq-135477.html#yahoomail
//  props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.ssl.enable", EmailSender.useSSL ? "true" : "false");
		return props;
	}
}
