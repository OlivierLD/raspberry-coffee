package relay.email;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class EmailReceiver
{
  private static String protocol;
  private static int outgoingPort;
  private static int incomingPort;
  private static String username;
  private static String password;
  private static String outgoing;
  private static String incoming;
  private static String replyto;
  private static boolean smtpauth;
  
  private static String sendEmailsTo;
  private static String acceptEmailsFrom;
  private static String acceptSubject;
  private static String ackSubject;
  
  private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

  private EmailSender emailSender = null; // For Ack
  private String provider = null;
  
  public EmailReceiver(String provider) throws RuntimeException
  {
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

    EmailReceiver.sendEmailsTo = "";
    EmailReceiver.acceptEmailsFrom = "";
    EmailReceiver.acceptSubject = "";
    EmailReceiver.ackSubject = "";

    Properties props = new Properties();
    String propFile = "email.properties";
    try
    {
      FileInputStream fis = new FileInputStream(propFile);
      props.load(fis);
    }
    catch (Exception e)
    {
      System.out.println("email.properies file problem...");
      throw new RuntimeException("File not found:email.properies");
    }
    EmailReceiver.sendEmailsTo     = props.getProperty("pi.send.emails.to");
    EmailReceiver.acceptEmailsFrom = props.getProperty("pi.accept.emails.from");
    EmailReceiver.acceptSubject    = props.getProperty("pi.email.subject");
    EmailReceiver.ackSubject       = props.getProperty("pi.ack.subject");

    EmailReceiver.protocol     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.protocol");
    EmailReceiver.outgoingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server.port", "0"));
    EmailReceiver.incomingPort = Integer.parseInt(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server.port", "0"));
    EmailReceiver.username     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.username",   "");
    EmailReceiver.password     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.password",   "");
    EmailReceiver.outgoing     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "outgoing.server", "");
    EmailReceiver.incoming     = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "incoming.server", "");
    EmailReceiver.replyto      = props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.replyto",    "");
    EmailReceiver.smtpauth     = "true".equals(props.getProperty("pi." + (provider != null ? (provider + ".") : "") + "mail.smtpauth", "false"));
    
    if (verbose)
    {
      System.out.println("Protocol:" + EmailReceiver.protocol);
      System.out.println("Usr/pswd:" + EmailReceiver.username + "/" + EmailReceiver.password);
    }
  }

  private static SearchTerm[] buildSearchTerm(String str)
  {
    String[] sa = str.split(",");
    List<SearchTerm> lst = new ArrayList<SearchTerm>();
    for (String s : sa)
      lst.add(new FromStringTerm(s.trim()));
    SearchTerm[] sta = new SearchTerm[lst.size()];
    sta = lst.toArray(sta);
    return sta;
  }
  
  private Properties setProps()
  {
    Properties props = new Properties();
    props.put("mail.debug", verbose?"true":"false");
    
    // TASK smtp should be irrelevant for a receiver
    props.put("mail.smtp.host", EmailReceiver.outgoing);
    props.put("mail.smtp.port", Integer.toString(EmailReceiver.outgoingPort));

    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true"); //  See http://www.oracle.com/technetwork/java/faq-135477.html#yahoomail
  //  props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.ssl.enable", "true");

    if ("pop3".equals(EmailReceiver.protocol))
    {
      props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.setProperty("mail.pop3.socketFactory.fallback", "false");
      props.setProperty("mail.pop3.port", Integer.toString(EmailReceiver.incomingPort));
      props.setProperty("mail.pop3.socketFactory.port", Integer.toString(EmailReceiver.incomingPort));
    }            

    if ("imap".equals(protocol))
    {
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

  public boolean isAuthRequired()
  {
    return EmailReceiver.smtpauth;
  }

  public String getUserName()
  {
    return EmailReceiver.username;
  }

  public String getPassword()
  {
    return EmailReceiver.password;
  }

  public String getReplyTo()
  {
    return EmailReceiver.replyto;
  }

  public String getIncomingServer()
  {
    return EmailReceiver.incoming;
  }

  public String getOutgoingServer()
  {
    return EmailReceiver.outgoing;
  }

  public List<String> receive()
    throws Exception
  {
    return receive(null);
  }
  
  public List<String> receive(String dir)
    throws Exception
  {
    if (verbose) System.out.println("Receiving...");
    List<String> messList = new ArrayList<String>();
    Store store = null;
    Folder folder = null;
    try
    {
  //  Properties props = System.getProperties();
      Properties props = setProps();
      
      if (verbose)
      {
        Set<Object> keys = props.keySet();
        for (Object o : keys)
          System.out.println(o.toString() + ":" + props.get(o).toString());
      }
      if (verbose) System.out.println("Getting session...");
//    Session session = Session.getInstance(props, null);
      Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() 
                        {
                          protected PasswordAuthentication getPasswordAuthentication() 
                          {
                            return new PasswordAuthentication(username, password);
                          }
                        });    
      session.setDebug(verbose);
      if (verbose) System.out.println("Session established.");
      store = session.getStore(EmailReceiver.protocol);
      if (EmailReceiver.incomingPort == 0)
        store.connect(EmailReceiver.incoming, EmailReceiver.username, EmailReceiver.password);
      else
        store.connect(EmailReceiver.incoming, EmailReceiver.incomingPort, EmailReceiver.username,
                      EmailReceiver.password);
      if (verbose) System.out.println("Connected to store");        
      folder = store.getDefaultFolder();
      if (folder == null)
        throw new RuntimeException("No default folder");

      folder = store.getFolder("INBOX");
      if (folder == null)
        throw new RuntimeException("No INBOX");

      folder.open(Folder.READ_WRITE);
      if (verbose) System.out.println("Connected... filtering, please wait.");
      SearchTerm st = new AndTerm(new SearchTerm[] { new OrTerm(buildSearchTerm(sendEmailsTo)), 
                                                     new SubjectTerm(acceptSubject),
                                                     new FlagTerm(new Flags(Flags.Flag.SEEN), false) });
   // st = new SubjectTerm("PI Request");
      Message msgs[] = folder.search(st);
//    Message msgs[] = folder.getMessages();

      if (verbose) System.out.println("Search completed, " + msgs.length + " message(s).");
      for (int msgNum=0; msgNum<msgs.length; msgNum++)
      {
        try
        {
          Message mess = msgs[msgNum];
          Address from[] = mess.getFrom();
          String sender = "";
          try
          {
            sender = from[0].toString();
          }
          catch(Exception exception) 
          {
            exception.printStackTrace();
          }
//        System.out.println("Message from [" + sender + "], subject [" + subject + "], content [" + mess.getContent().toString().trim() + "]");
          
          if (true)
          {
            if (!mess.isSet(javax.mail.Flags.Flag.SEEN) && 
                !mess.isSet(javax.mail.Flags.Flag.DELETED))
            {
              String txtMess = printMessage(mess, dir);
              messList.add(txtMess);
              mess.setFlag(javax.mail.Flags.Flag.SEEN, true);
              mess.setFlag(javax.mail.Flags.Flag.DELETED, true);
              // Send an ack - by email.
              if (this.emailSender == null)
                this.emailSender = new EmailSender(this.provider);
              this.emailSender.send(new String[] { sender }, 
                                    ackSubject, 
                                    "Your request [" + txtMess.trim() + "] is being taken care of.");
              if (verbose) System.out.println("Sent an ack to " + sender);
            } 
            else
            {
              if (verbose) System.out.println("Old message in your inbox..., received " + mess.getReceivedDate().toString());
            }
          }
        }
        catch(Exception ex)
        {
//        System.err.println(ex.getMessage());
          ex.printStackTrace();
        }
      }
    }
    catch(Exception ex)
    {
      throw ex;
    }
    finally
    {
      try
      {
        if (folder != null)
          folder.close(true);
        if (store != null)
          store.close();
      }
      catch(Exception ex2)
      {
        System.err.println("Finally ...");
        ex2.printStackTrace();
      }
    }
    return messList;
  }

  public static String printMessage(Message message, String dir)
  {
    String ret = "";
    try
    {
      String from = ((InternetAddress)message.getFrom()[0]).getPersonal();
      if(from == null)
        from = ((InternetAddress)message.getFrom()[0]).getAddress();
      if (verbose) System.out.println("From: " + from);
      String subject = message.getSubject();
      if (verbose) System.out.println("Subject: " + subject);
      Part messagePart = message;
      Object content = messagePart.getContent();
      if (content instanceof Multipart)
      {
//      messagePart = ((Multipart)content).getBodyPart(0);
        int nbParts = ((Multipart)content).getCount();
        if (verbose) System.out.println("[ Multipart Message ], " + nbParts + " part(s).");
        for (int i=0; i<nbParts; i++)
        {
          messagePart = ((Multipart)content).getBodyPart(i);
          if (messagePart.getContentType().toUpperCase().startsWith("APPLICATION/OCTET-STREAM"))
          {
            if (verbose) System.out.println(messagePart.getContentType() + ":" + messagePart.getFileName());
            InputStream is = messagePart.getInputStream();
            String newFileName = "";
            if (dir != null)
              newFileName = dir + File.separator;
            newFileName += messagePart.getFileName();
            FileOutputStream fos = new FileOutputStream(newFileName);
            ret = messagePart.getFileName();
            if (verbose) System.out.println("Downloading " + messagePart.getFileName() + "...");
            copy(is, fos);
            if (verbose) System.out.println("...done.");
          } 
          else // text/plain, text/html
          {
            if (verbose) System.out.println("-- Part #" + i + " --, " + messagePart.getContentType().replace('\n', ' ').replace('\r', ' ').replace("\b", "").trim());
            InputStream is = messagePart.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while (line != null)
            {
              line = br.readLine();
              if (line != null)
              {
                if (verbose) System.out.println("[" + line + "]");
                if (messagePart.getContentType().toUpperCase().startsWith(HttpHeaders.TEXT_PLAIN))
                  ret += line;
              }
            }
            br.close();
            if (verbose) System.out.println("-------------------");
          }
        }
      }
      else
      {
//      System.out.println("  .Message is a " + content.getClass().getName());
//      System.out.println("Content:");
//      System.out.println(content.toString());
        ret = content.toString();
      }
      if (verbose) System.out.println("-----------------------------");
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    return ret;
  }

  private static void copy(InputStream in, OutputStream out)
    throws IOException
  {
    synchronized(in)
    {
      synchronized(out)
      {
        byte buffer[] = new byte[256];
        while (true)
        {
          int bytesRead = in.read(buffer);
          if(bytesRead == -1)
            break;
          out.write(buffer, 0, bytesRead);
        }
      }
    }
  }
}
