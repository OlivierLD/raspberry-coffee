package pi4j.email;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class SampleMain
{
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
  private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

  /**
   * Invoked like:
   *   java pi4j.email.SampleMain [-verbose] -send:google -receive:yahoo
   *   
   * This will send emails using google, and receive using yahoo.
   * Do check the file email.properties for the different values associated with email servers.
   * 
   * NO GPIO INTERACTION in this one.
   * 
   * @param args See above
   */
  public static void main(String[] args)  
  {
//  String provider = "yahoo";
    String providerSend = "oracle";
//  String provider = "oracle";
//  provider = "yahoo";
    String providerReceive = "oracle";
//  provider = "oracle";

    for (int i=0; i<args.length; i++)
    {
      if ("-verbose".equals(args[i]))
      {
        verbose = true;
        System.setProperty("verbose", "true");
      }
      else if (args[i].startsWith("-send:"))
        providerSend = args[i].substring("-send:".length());      
      else if (args[i].startsWith("-receive:"))
        providerReceive =args[i].substring("-receive:".length());  
      else if ("-help".equals(args[i]))
      {
        System.out.println("Usage:");
        System.out.println("  java pi4j.email.SampleMain -verbose -send:google -receive:yahoo -help");
        System.exit(0);
      }
    }
      
    final EmailSender sender = new EmailSender(providerSend);
    Thread senderThread = new Thread()
      {
        public void run()
        {
          try
          {
            for (int i=0; i<10; i++)
            {
              System.out.println("Sending...");
              sender.send(new String[] { "olivier@lediouris.net", 
                                         "webmaster@lediouris.net", 
                                         "olivier.lediouris@gmail.com", 
                                         "olivier_le_diouris@yahoo.com",
                                         "olivier.lediouris@oracle.com" }, 
                          "PI Request", 
                          "{ operation: 'see-attached-" + Integer.toString(i + 1) + "' }",
                          "P8150115.JPG");
              System.out.println("Sent.");
              Thread.sleep(60000L); // 1 minute
            }
            System.out.println("Exiting...");
            sender.send(new String[] { "olivier@lediouris.net", 
                                       "webmaster@lediouris.net", 
                                       "olivier.lediouris@gmail.com", 
                                       "olivier_le_diouris@yahoo.com",
                                       "olivier.lediouris@oracle.com" }, 
                        "PI Request", 
                        "{ operation: 'exit' }");
            System.out.println("Bye.");
          }
          catch (Exception ex)
          {
            ex.printStackTrace();
          }
        
        }
      };
    senderThread.start(); // Bombarding
        
    if (args.length > 1)
      providerSend = args[1];      

    EmailReceiver receiver = new EmailReceiver(providerReceive); // For Google, pop must be explicitely enabled at the account level
    try
    {
      boolean keepLooping = true;
      while (keepLooping)
      {
        List<String> received = receiver.receive();
        if (verbose || received.size() > 0)
        System.out.println(SDF.format(new Date())  + " - Retrieved " + received.size() + " message(s).");
        for (String s : received)
        {
  //      System.out.println(s);
          String operation = "";
          try
          {
            JSONObject json = new JSONObject(s);
            operation = json.getString("operation");
          }
          catch (Exception ex)
          {
            System.err.println(ex.getMessage());
            System.err.println("Message is [" + s + "]");
          }
          if ("exit".equals(operation))
          {
            keepLooping = false;
            System.out.println("Will exit next batch.");
        //  break;
          }
          else
          {
            System.out.println("Operation: [" + operation + "], sent for processing.");
            try { Thread.sleep(1000L); } catch (InterruptedException ie) { ie.printStackTrace(); }
          }
        }
      }
      System.out.println("Done.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
