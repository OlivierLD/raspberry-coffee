package sample.email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.net.URI;
import java.util.Properties;

/*
 * From https://www.baeldung.com/java-email
 */
public class EmailService {

    private String username = "olivier.lediouris@gmail.com";
    private String password = "pqzlsjlxdrpflksz";
    private final Properties prop;

    public EmailService(String host, int port, String username, String password) {
        prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.ssl.trust", host);

        this.username = username;
        this.password = password;
    }

    public EmailService(String host, int port) {
        prop = new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
    }

    public static void main(String... args) {
        try {
            String username = "olivier.lediouris@gmail.com";
            String password = "pqzlsjlxdrpflksz";

            new EmailService("smtp.mailtrap.io", 25, username, password)
                    .sendMail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMail() throws Exception {

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("olivier@lediouris.net"));
        message.setSubject("Mail Subject");

        String msg = "This is my first email using JavaMailer";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        String msgStyled = "This is my <b style='color:red;'>bold-red email</b> using JavaMailer";
        MimeBodyPart mimeBodyPartWithStyledText = new MimeBodyPart();
        mimeBodyPartWithStyledText.setContent(msgStyled, "text/html; charset=utf-8");

        if (false) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();

            attachmentBodyPart.attachFile(getFile());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            multipart.addBodyPart(mimeBodyPartWithStyledText);
            multipart.addBodyPart(attachmentBodyPart);

            message.setContent(multipart);
        }
        Transport.send(message);
    }

    private File getFile() throws Exception {
        URI uri = this.getClass()
                .getClassLoader()
                .getResource("attachment.txt")
                .toURI();
        return new File(uri);
    }
}
